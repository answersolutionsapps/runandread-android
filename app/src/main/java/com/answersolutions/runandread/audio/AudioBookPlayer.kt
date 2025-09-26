package com.answersolutions.runandread.audio

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.answersolutions.extensions.formatSecondsToHMS
import com.answersolutions.runandread.BookPlayer
import com.answersolutions.runandread.data.model.AudioBook
import com.answersolutions.runandread.data.model.Bookmark
import com.answersolutions.runandread.ui.player.PlayerViewModel
import com.answersolutions.runandread.ui.player.TextTimeRelationsTools.getCurrentBookmarkText
import com.answersolutions.runandread.ui.player.TextTimeRelationsTools.getCurrentWordIndex
import com.answersolutions.runandread.voice.SpeakingCallBack
import timber.log.Timber

class AudioBookPlayer(
    context: Context,
    private val speakingCallback: SpeakingCallBack
) : BookPlayer {
    companion object {
        const val SEEK_STEP_AUDIO = 30
    }

    private var mediaPlayer: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false

    private var nextPartStartTime = 0
    private var currentStartTime = 0
    private var frame: List<String> = emptyList()

    init {
        val book = speakingCallback.book as AudioBook
        mediaPlayer = ExoPlayer.Builder(context).build().apply {
            val player = this
            val mediaItem = MediaItem.fromUri(Uri.parse(book.audioFilePath))
            setMediaItem(mediaItem)
            prepare()
            pause()
            seekTo((book.lastPosition * 1000L))
            playbackParameters = PlaybackParameters(book.voiceRate)

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    Timber.d("onPlaybackStateChanged: $state")
                    when (state) {
                        Player.STATE_ENDED -> {
                            this@AudioBookPlayer.isPlaying = false
                            speakingCallback.onCompleted()
                        }

                        Player.STATE_READY -> {
                            Timber.d("STATE_READY1: ${book.viewState.value.totalTimeSeconds}")
                            if (book.viewState.value.totalTimeSeconds == 0L) {
                                book.lazyCalculate {
                                    speakingCallback.onReady(uiState = PlayerViewModel.PlayerUIState(
                                        progress = (book.lastPosition / book.voiceRate),
                                        totalTimeString = book.viewState.value.totalTime,
                                        progressTime = book.viewState.value.progressTime,
                                        sliderRange = 0f..book.viewState.value.totalTimeSeconds.toFloat(),
                                        totalTime = book.viewState.value.totalTimeSeconds.toDouble(),
                                        bookmarks = book.bookmarks.map {
                                            it.title = titleForAudioBookmark(book, it.position); it
                                        }
                                    ))
                                }
                            } else {
                                Timber.d("STATE_READY2: ${player.currentPosition}")
                                frame = emptyList()
                                val elapsedSeconds = player.currentPosition / 1000
                                val elapsedTimeToShow =
                                    (elapsedSeconds / book.voiceRate).toDouble()
                                        .formatSecondsToHMS()
                                val hState = speakingCallback.highlightingState.value
                                if ((frame.isEmpty() || elapsedSeconds >= (nextPartStartTime / 1000.0))) {
                                    val textFrame = book.getCurrentText(elapsedMilliseconds = elapsedSeconds.toDouble())
                                    frame =
                                        textFrame.text.trim().split(" ").filter { it.isNotEmpty() }
                                    nextPartStartTime =
                                        textFrame.nextStartTime ?: (textFrame.startTimeMms + 30_000)
                                    currentStartTime = textFrame.startTimeMms
                                }
                                val currentWordIndexInFrame = getCurrentWordIndex(
                                    elapsedSeconds.toDouble(),
                                    frame,
                                    currentStartTime,
                                    nextPartStartTime
                                )
                                speakingCallback.onProgressUpdate(
                                    updatedBook = book.copy(
                                        lastPosition = elapsedSeconds.toInt(),
                                        updated = System.currentTimeMillis()
                                    ),
                                    speakingCallback.viewState.value.copy(
                                        progress = (elapsedSeconds / book.voiceRate),
                                        progressTime = elapsedTimeToShow
                                    ),
                                    hState.copy(
                                        currentWordIndexInFrame = currentWordIndexInFrame,
                                        currentFrame = frame
                                    )
                                )
                                if (playOnReady) {
                                    playOnReady = false
                                    onPlay(source = 2)
                                }
                            }
                        }

                        Player.STATE_BUFFERING -> {

                        }

                        Player.STATE_IDLE -> {

                        }
                    }
                }
            })
        }
    }

    override fun onPlay(source: Int) {

        mediaPlayer?.apply {
                play()
                this@AudioBookPlayer.isPlaying = true
                speakingCallback.onStart()
                startProgressUpdates()
        } ?: Timber.e("onPlay() called but mediaPlayer is null")
    }

    fun duration(): Float {
        return mediaPlayer?.duration?.toFloat() ?: 0f
    }

    override fun onStopSpeaking() {
        mediaPlayer?.takeIf { this@AudioBookPlayer.isPlaying }?.apply {
            pause()
            this@AudioBookPlayer.isPlaying = false
            speakingCallback.onStop()
            handler.removeCallbacksAndMessages(null) // Stop progress updates
        }
    }

    override fun onClose() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        isPlaying = false
    }

    override fun onFastForward() {
        mediaPlayer?.apply {
            pause()
            playOnReady = true
            val newPosition = (currentPosition + (SEEK_STEP_AUDIO * 1000L)).coerceAtMost(duration)
            Timber.d("fastForward: $newPosition")
            seekTo(newPosition)
        }
    }

    override fun onRewind() {
        mediaPlayer?.apply {
            pause()
            playOnReady = true
            val newPosition = (currentPosition - (SEEK_STEP_AUDIO * 1000L)).coerceAtMost(duration)
            seekTo(newPosition)
        }
    }

    override fun onUserChangePosition(value: Float) {
        mediaPlayer?.apply {
            pause()
            playOnReady = true
            val newPosition = (value.toInt() * 1000L).coerceAtMost(duration)
            seekTo(newPosition)
        }
    }

    private var playOnReady = false
    override fun onPlayFromBookmark(position: Int) {
        frame = emptyList()
        onStopSpeaking()
        mediaPlayer?.apply {
            pause()
            playOnReady = true
            seekTo((position * 1000L).coerceAtMost(mediaPlayer?.duration ?: 0L))
        }
    }

    override fun onDeleteBookmark(bookmark: Bookmark) {
        val book = speakingCallback.book as AudioBook
        val updatedBookmarks = book.bookmarks.filter { it.position != bookmark.position }
        book.bookmarks.clear()
        book.bookmarks.addAll(updatedBookmarks)

        speakingCallback.onUpdateUI(
            speakingCallback.viewState.value.copy(
                bookmarks = updatedBookmarks
            )
        )
    }

    override fun onSaveBookmark() {
        mediaPlayer?.let { player ->
            val book = speakingCallback.book as AudioBook
            val elapsedSeconds = player.currentPosition / 1000
            val b = Bookmark(elapsedSeconds.toInt())
            b.title = titleForAudioBookmark(book, elapsedSeconds.toInt())
            book.bookmarks.add(b)
            speakingCallback.onUpdateUI(speakingCallback.viewState.value.copy(
                bookmarks = book.bookmarks
            ))
        }

    }

    override fun currentTimeElapsed(): Long {
        mediaPlayer?.let {
            val book = speakingCallback.book as AudioBook
            return (it.currentPosition / book.voiceRate).toLong()
        } ?: return 0
    }

    private fun startProgressUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (this@AudioBookPlayer.isPlaying) {
                        val book = speakingCallback.book as AudioBook
                        val elapsedSeconds = it.currentPosition / 1000
                        val elapsedTimeToShow =
                            (elapsedSeconds / book.voiceRate).toDouble().formatSecondsToHMS()

                        if ((frame.isEmpty() || elapsedSeconds >= (nextPartStartTime / 1000.0))) {
                            val textFrame = book.getCurrentText(elapsedMilliseconds = it.currentPosition.toDouble())
                            frame = textFrame.text.trim().split(" ").filter { it.isNotEmpty() }
                            nextPartStartTime =
                                textFrame.nextStartTime ?: (textFrame.startTimeMms + 30_000)
                            currentStartTime = textFrame.startTimeMms
                            val hState = speakingCallback.highlightingState.value
                            speakingCallback.onUpdateHighlightingUI(
                                hState.copy(
                                    currentFrame = frame
                                )
                            )
                        }
                        val currentWordIndexInFrame = getCurrentWordIndex(
                            elapsedSeconds.toDouble(),
                            frame,
                            currentStartTime,
                            nextPartStartTime
                        )
                        val hState = speakingCallback.highlightingState.value
                        speakingCallback.onProgressUpdate(
                            updatedBook = book.copy(
                                lastPosition = elapsedSeconds.toInt(),
                                updated = System.currentTimeMillis()
                            ),
                            speakingCallback.viewState.value.copy(
                                progress = (elapsedSeconds / book.voiceRate),
                                progressTime = elapsedTimeToShow
                            ),
                            hState.copy(
                                currentWordIndexInFrame = currentWordIndexInFrame
                            )
                        )
                        handler.postDelayed(this, 250) // Update 4 time per second
                    }
                }
            }
        })
    }

    private fun titleForAudioBookmark(book: AudioBook, position: Int): String {
        val elapsedSeconds = position.toDouble()
        val forElapsedTimeMilliseconds = (elapsedSeconds * 1000)
        val textFrame = book.getCurrentText(elapsedMilliseconds = forElapsedTimeMilliseconds)
        val bookmarkTitle = getCurrentBookmarkText(
            elapsedSeconds = elapsedSeconds,
            textFrame.text,
            textFrame.startTimeMms,
            textFrame.nextStartTime ?: (textFrame.startTimeMms + 30000),
            textFrame.nextText
        )

        val elapsedTimeToShow = (elapsedSeconds / book.voiceRate).formatSecondsToHMS()
        return "$elapsedTimeToShow | $bookmarkTitle"
    }
}
