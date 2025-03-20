package com.answersolutions.runandread.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.answersolutions.extensions.formatSecondsToHMS
import com.answersolutions.runandread.BookPlayer
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.Book.Companion.SECONDS_PER_CHARACTER
import com.answersolutions.runandread.data.model.Bookmark
import com.answersolutions.runandread.data.model.RunAndReadBook
import com.answersolutions.runandread.ui.player.PlayerViewModel.HighlightingUIState
import com.answersolutions.runandread.ui.player.PlayerViewModel.PlayerUIState
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.*
import kotlin.math.max
import kotlin.math.min

interface SpeakingCallBack {
    fun onReady(uiState: PlayerUIState)
    fun onStart()
    fun onProgressUpdate(
        updatedBook: RunAndReadBook,
        pUIState: PlayerUIState,
        hUIState: HighlightingUIState
    )
    fun onStop()
    fun onCompleted()

    val viewState: StateFlow<PlayerUIState>
    fun onUpdateUI(state: PlayerUIState)
    val highlightingState: StateFlow<HighlightingUIState>
    fun onUpdateHighlightingUI(state: HighlightingUIState)

    val book: RunAndReadBook?
}

class SpeechBookPlayer(
    private val context: Context,
    private var voice: Voice,
    private val speakingCallback: SpeakingCallBack
) : BookPlayer {
    companion object {
        const val FRAME_SIZE = 100
        const val SEEK_STEP_TEXT = 60
    }

    private var textToSpeech: TextToSpeech? = null

    private var words = listOf<String>()
    private var selectedSpeechRate: Float = 1f
    private var currentWordIndex = 0
    private var totalWords: Int = 0
    private var isPlaying = false

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        val book = speakingCallback.book ?: return
        if (book !is Book) {
            Timber.e("Invalid book type")
            return
        }
        val selectedLanguage = Locale(book.language)
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val player = textToSpeech!!
                words = book.text.flatMap { it.split("\\s+".toRegex()) }
                    .mapNotNull { it.takeIf { it.isNotEmpty() } }

                totalWords = words.size
                selectedSpeechRate = book.voiceRate
                currentWordIndex = book.lastPosition

                player.language = selectedLanguage
                player.voice = voice
                player.setSpeechRate(book.voiceRate)
                player.setOnUtteranceProgressListener(ttsListener)
                val hState = speakingCallback.highlightingState.value
                speakingCallback.onUpdateHighlightingUI(
                    hState.copy(currentWordIndexInFrame = 0)
                )
                book.lazyCalculate {
                    speakingCallback.onReady(
                        uiState = PlayerUIState(
                            progress = book.lastPosition.toFloat(),
                            totalTimeString = book.viewState.value.totalTime,
                            progressTime = book.viewState.value.progressTime,
                            sliderRange = 0f..book.viewState.value.totalTimeSeconds.toFloat(),
                            totalTime = book.viewState.value.totalTimeSeconds.toDouble(),
                            bookmarks = book.bookmarks.map {
                                it.title = titleForBookmark(it.position); it
                            })
                    )
                }
            } else {
                Timber.e("TTS Initialization failed with status: $status")
            }
        }
    }

    private val ttsListener = object : UtteranceProgressListener() {
        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            Timber.d("onRangeStart: $currentWordIndex")
            val hState = speakingCallback.highlightingState.value
            if (hState.currentWordIndexInFrame < hState.currentFrame.size - 1) {
                val book = speakingCallback.book as Book
                val secondsElapsed = calculateElapsedTime(currentWordIndex)
                currentWordIndex += 1

                speakingCallback.onProgressUpdate(
                    updatedBook = book.copy(
                        lastPosition = currentWordIndex,
                        updated = System.currentTimeMillis()
                    ),
                    pUIState = speakingCallback.viewState.value.copy(
                        progress = currentWordIndex.toFloat(),
                        progressTime = secondsElapsed.formatSecondsToHMS()
                    ),
                    hUIState = hState.copy(
                        currentWordIndexInFrame = hState.currentWordIndexInFrame + 1
                    )
                )
            }

        }

        override fun onStart(utteranceId: String?) {
            Timber.d("onStart: $utteranceId")
            speakingCallback.onStart()
        }

        override fun onDone(utteranceId: String?) {
            Timber.d("onDone: $utteranceId")
            onNextWord()
        }

        @Deprecated("Deprecated in Java")
        override fun onError(p0: String?) {
            Timber.d("onError: $p0")
            speakingCallback.onStop()
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            Timber.e("TTS Error ($errorCode) for utterance: $utteranceId")
            if (errorCode in listOf(
                    TextToSpeech.ERROR_NETWORK,
                    TextToSpeech.ERROR_NETWORK_TIMEOUT
                )
            ) {
                Timber.w("Network error encountered, consider switching to offline voices.")
            }
            speakingCallback.onStop()
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            Timber.d("onStop: $utteranceId")
            speakingCallback.onStop()
            isPlaying = false
        }
    }

    fun onNextWord() {
        if (currentWordIndex < totalWords - 1) {
            currentWordIndex++
            playNextFrame()
        } else {
            speakingCallback.onUpdateUI(
                speakingCallback.viewState.value.copy(
                    isSpeaking = false
                )
            )
        }
    }

    fun speak(text: String) {
        val utteranceId = "my_utterance_id"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun calculateElapsedTime(progress: Int): Double {
        val chars = words.take(progress).joinToString(" ").length
        val seconds = (chars * SECONDS_PER_CHARACTER) / selectedSpeechRate
        return seconds
    }

    private fun isSpeaking(): Boolean = textToSpeech?.isSpeaking == true || isPlaying

    override fun onPlay(source: Int) {
        if (isPlaying) return
        isPlaying = true
        playNextFrame()
    }

    private fun playNextFrame() {
        if (words.isEmpty() || currentWordIndex >= words.size) return
        val toIndex = minOf(currentWordIndex + FRAME_SIZE, totalWords - 1)
        val hState = speakingCallback.highlightingState.value
        val frame = words.subList(currentWordIndex, toIndex)
        speakingCallback.onUpdateHighlightingUI(
            hState.copy(
                currentWordIndexInFrame = 0,
                currentFrame = frame
            )
        )
        speak(frame.joinToString(" "))
    }

    override fun onDeleteBookmark(bookmark: Bookmark) {
        val book = speakingCallback.book as Book
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
        val book = speakingCallback.book as Book
        book.bookmarks.add(Bookmark(currentWordIndex))
        speakingCallback.onUpdateUI(speakingCallback.viewState.value.copy(
            bookmarks = book.bookmarks.map {
                if (it.title.isEmpty()) {
                    it.title = titleForBookmark(it.position)
                }; it
            }
        ))
    }

    private fun titleForBookmark(position: Int): String {
        val elapsedTimeToShow = calculateElapsedTime(position).formatSecondsToHMS()//
        val from = max(0, position - 5)
        val to = min(words.size - 1, position + 10)

        if (to <= words.size && words.isNotEmpty()) {
            val t = words.subList(from, to)
            return "$elapsedTimeToShow | ${t.joinToString(" ")}"
        } else {
            return "$elapsedTimeToShow | Unknown Bookmark"
        }
    }

    override fun currentTimeElapsed(): Long {
        return calculateElapsedTime(currentWordIndex).toLong() * 1000L
    }

    override fun onPlayFromBookmark(position: Int) {
        isPlaying = false
        onStopSpeaking()
        onUserChangePosition(position.toFloat())
        if (isSpeaking()) return
        onPlay(source = 3)
    }

    override fun onStopSpeaking() {
        isPlaying = false
        speakingCallback.onStop()
        textToSpeech?.stop()
    }

    override fun onClose() {
        onStopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null
    }

    override fun onFastForward() {
        onUserChangePosition(currentWordIndex.toFloat() + (SEEK_STEP_TEXT))
    }

    override fun onRewind() {
        onUserChangePosition(currentWordIndex.toFloat() - (SEEK_STEP_TEXT))
    }

    override fun onUserChangePosition(value: Float) {
        isPlaying = false
        onStopSpeaking()
        currentWordIndex = value.coerceIn(0f, totalWords.toFloat() - 1).toInt()
        val hState = speakingCallback.highlightingState.value

        val book = speakingCallback.book as Book
        val elapsedSeconds = calculateElapsedTime(progress = currentWordIndex)

        speakingCallback.onProgressUpdate(
            updatedBook = book.copy(
                lastPosition = currentWordIndex,
                updated = System.currentTimeMillis()
            ),
            pUIState = speakingCallback.viewState.value.copy(
                progress = currentWordIndex.toFloat(),
                progressTime = elapsedSeconds.formatSecondsToHMS()
            ),
            hUIState = hState.copy(
                currentWordIndexInFrame = 0, currentFrame = emptyList()
            )
        )
        if (isSpeaking()) return
        onPlay(source = 5)
    }
}
