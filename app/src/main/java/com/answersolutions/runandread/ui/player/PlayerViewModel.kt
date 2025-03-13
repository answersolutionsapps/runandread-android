package com.answersolutions.runandread.ui.player

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.answersolutions.extensions.formatSecondsToHMS
import com.answersolutions.runandread.audio.AudioBookPlayer
import com.answersolutions.runandread.data.model.AudioBook
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.Book.Companion.SECONDS_PER_CHARACTER
import com.answersolutions.runandread.data.model.Bookmark
import com.answersolutions.runandread.data.model.RunAndReadBook
import com.answersolutions.runandread.data.repository.LibraryRepository
import com.answersolutions.runandread.data.repository.VoiceRepository
import com.answersolutions.runandread.voice.SpeakingCallBack
import com.answersolutions.runandread.voice.SpeechProvider
import com.answersolutions.runandread.voice.toVoice
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val repository: VoiceRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    companion object {
        private const val FRAME_SIZE = 100
        private const val SEEK_STEP = 60
    }

    var selectedBook: RunAndReadBook? = null

    fun saveBookChanges() {
        viewModelScope.launch {
            selectedBook?.let {
                libraryRepository.updateBook(it)
            }
        }
    }

    private var textToSpeech: SpeechProvider? = null
    private var audioBookPlayer: AudioBookPlayer? = null

    private var words = listOf<String>()
    private var selectedSpeechRate: Float = 1f
    private var currentWordIndex = 0

    private var totalWords: Int = 0
    private var totalTime: Double = 0.0

    data class PlayerUIState(
        val isSpeaking: Boolean = false,
        val spokenTextRange: IntRange = 0..0,
        val progress: Float = 0f,
        val progressTime: String = "00:00",
        val totalTimeString: String = "00:00",
        val bookmarks: List<Bookmark> = emptyList()
    )

    data class HighlightingUIState(
        val currentWordIndexInFrame: Int = 0, val currentFrame: List<String> = listOf()
    )


    private val _highlightingState = MutableStateFlow(HighlightingUIState())
    val highlightingState: StateFlow<HighlightingUIState> get() = _highlightingState.asStateFlow()

    private val _state = MutableStateFlow(PlayerUIState())
    val viewState: StateFlow<PlayerUIState> get() = _state.asStateFlow()

    fun setUpBook() {
        viewModelScope.launch {
            selectedBook = withContext(Dispatchers.IO) {
                libraryRepository.getSelectedBook()
            }

            selectedBook?.let { book ->
                val selectedLanguage = Locale(book.language)

                if (book is Book) {
                    val voice = repository.nameToVoice(book.voiceIdentifier, book.language)
                    words = book.text.flatMap { it.split("\\s+".toRegex()) }
                        .mapNotNull { it.takeIf { it.isNotEmpty() } }

                    totalWords = words.size
                    selectedSpeechRate = book.voiceRate

                    totalTime = calculateElapsedTime(totalWords - 1)

                    withContext(Dispatchers.Main) {
                        _highlightingState.value =
                            _highlightingState.value.copy(currentWordIndexInFrame = 0)
                        _state.value =
                            _state.value.copy(totalTimeString = totalTime.formatSecondsToHMS(),
                                bookmarks = book.bookmarks.map {
                                    it.title = titleForBookmark(it.position); it
                                })
                        updatePosition(book.lastPosition.toFloat())
                        textToSpeech = SpeechProvider(
                            application,
                            currentLocale = selectedLanguage,
                            currentVoice = voice.toVoice(),
                            speechRate = selectedSpeechRate,
                            callBack = speechRangeCallBack,
                            speakingCallBack = speakingCallBack
                        )
                    }
                } else if (book is AudioBook) {
                    book.lazyCalculate {
                        _state.value =
                            _state.value.copy(
                                totalTimeString = book.viewState.value.totalTime,
                                progressTime = book.viewState.value.progressTime
                            )
                    }
                    withContext(Dispatchers.Main) {
                        audioBookPlayer = AudioBookPlayer(
                            filePath = book.audioFilePath,
                            playbackRate = book.voiceRate,
                            listener = object : AudioBookPlayer.PlayerListener {
                                override fun onStart() {
                                    _state.value = _state.value.copy(isSpeaking = true)
                                }

                                override fun onPause() {
                                    _state.value = _state.value.copy(isSpeaking = false)
                                }

                                override fun onCompleted() {
                                    _state.value = _state.value.copy(isSpeaking = false)
                                }

                                override fun onProgressUpdate(progress: Int, duration: Int) {
                                    println("Progress: $progress / $duration seconds")
                                    _state.value = _state.value.copy(
                                        progress = progress.toFloat(),
                                        progressTime = progress.toDouble().formatSecondsToHMS()
                                    )
                                }
                            }
                        )
                        updatePosition(book.lastPosition.toFloat())
                    }
                }
            }
        }
    }

    private fun titleForBookmark(position: Int): String {
        val from = max(0, position - 5)
        val to = min(words.size - 1, position + 10)

        if (to <= words.size && words.isNotEmpty()) {
            val t = words.subList(from, to)
            return t.joinToString(" ")
        } else {
            return "Unknown Bookmark"
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        Timber.d("Deleting bookmark at position: ${bookmark.position}")
        Timber.d("Before delete, bookmarks size: ${selectedBook?.bookmarks?.size}")

        viewModelScope.launch {
            selectedBook?.let { book ->
                val updatedBookmarks = book.bookmarks.filter { it.position != bookmark.position }
                book.bookmarks.clear()
                book.bookmarks.addAll(updatedBookmarks)

                _state.value = _state.value.copy(bookmarks = updatedBookmarks)

                Timber.d("After delete, bookmarks size: ${book.bookmarks.size}")
            } ?: Timber.w("No selected book found")
        }
    }

    fun playFromBookmark(position: Int) {
        if (selectedBook is Book) {
            textToSpeech?.stop()
            updatePosition(position.toFloat())
            speak()
        } else {
            //todo:
        }
    }

    fun sliderRange(): ClosedFloatingPointRange<Float> {
        return 0f..words.count().toFloat()
    }

    fun speak() {
        if (selectedBook is Book) {
            if (isSpeaking()) return
            val toIndex = minOf(currentWordIndex + FRAME_SIZE, totalWords - 1)
            _highlightingState.value = _highlightingState.value.copy(
                currentWordIndexInFrame = 0, currentFrame = words.subList(currentWordIndex, toIndex)
            )

            textToSpeech?.speak(_highlightingState.value.currentFrame.joinToString(" "))
        } else {
            audioBookPlayer?.play()
        }
    }

    fun isSpeaking(): Boolean {
        return if (selectedBook is Book) {
            textToSpeech?.isSpeaking() == true
        } else {
            audioBookPlayer?.isPlaying() == true
        }
    }

    fun stopSpeaking() {
        viewModelScope.launch {
            if (selectedBook is Book) {
                textToSpeech?.stop()
            } else {
                audioBookPlayer?.pause()
            }
        }
    }

    private val speechRangeCallBack: (range: IntRange) -> Unit = { range ->
        if (_highlightingState.value.currentWordIndexInFrame < _highlightingState.value.currentFrame.size - 1) {
            _state.value = _state.value.copy(
                spokenTextRange = range
            )
            currentWordIndex += 1
            _highlightingState.value =
                _highlightingState.value.copy(currentWordIndexInFrame = _highlightingState.value.currentWordIndexInFrame + 1)
            val secondsElapsed = (words.take(currentWordIndex)
                .joinToString(" ").length * SECONDS_PER_CHARACTER) / selectedSpeechRate.toDouble()

            _state.value = _state.value.copy(
                progress = currentWordIndex.toFloat(),
                progressTime = secondsElapsed.formatSecondsToHMS()
            )
            if (selectedBook is Book) {
                selectedBook = (selectedBook as? Book)?.copy(lastPosition = currentWordIndex)
            } else if (selectedBook is AudioBook) {
                selectedBook = (selectedBook as? AudioBook)?.copy(lastPosition = currentWordIndex)
            }

            playbackProgressCallBack(
                secondsElapsed.toLong() * 1000, totalTime.toLong() * 1000, isSpeaking()
            )
        }
    }

    fun currentTimeElapsed(): Long {
        val secondsElapsed = (words.take(currentWordIndex)
            .joinToString(" ").length * SECONDS_PER_CHARACTER) / selectedSpeechRate.toDouble()

        return secondsElapsed.toLong() * 1000L
    }

    fun saveBookmark() {
        viewModelScope.launch {
            selectedBook?.bookmarks?.add(Bookmark(currentWordIndex))
            _state.value = _state.value.copy(
                bookmarks = selectedBook?.bookmarks?.map {
                    if (it.title.isEmpty()) {
                        it.title = titleForBookmark(it.position)
                    }; it
                } ?: emptyList(),
            )
        }
    }

    fun fastForward() = seek(SEEK_STEP)
    fun fastRewind() = seek(-SEEK_STEP)

    private fun seek(offset: Int) {
        if (selectedBook is Book) {
            textToSpeech?.stop()
            updatePosition(currentWordIndex.toFloat() + offset)
            speak()
        } else {
            audioBookPlayer?.stop()
            audioBookPlayer?.fastForward(offset)
        }
    }

    fun updatePosition(value: Float) {
        viewModelScope.launch {
            if (selectedBook is Book) {
                currentWordIndex = value.coerceIn(0f, totalWords.toFloat() - 1).toInt()
                _highlightingState.value = _highlightingState.value.copy(
                    currentWordIndexInFrame = 0, currentFrame = emptyList()
                )
                updateProgress()
                selectedBook = (selectedBook as? Book)?.copy(
                    lastPosition = currentWordIndex,
                    updated = System.currentTimeMillis()
                )
            } else if (selectedBook is AudioBook) {
                //todo:
//                currentWordIndex = value.coerceIn(0f, audioBookPlayer.).toInt()
                selectedBook = (selectedBook as? AudioBook)?.copy(
                    lastPosition = currentWordIndex,
                    updated = System.currentTimeMillis()
                )
            }

        }
    }

    private fun updateProgress() {
        val elapsedSeconds = calculateElapsedTime(progress = currentWordIndex)
        _state.value = _state.value.copy(
            progress = currentWordIndex.toFloat(),
            progressTime = elapsedSeconds.formatSecondsToHMS()
        )
    }

    private fun calculateElapsedTime(progress: Int): Double {
        val chars = words.take(progress).joinToString(" ").length
        val seconds = (chars * SECONDS_PER_CHARACTER) / selectedSpeechRate
        return seconds
    }

    private val speakingCallBack = object : SpeakingCallBack {
        override fun onStop() = resetSpeakingState()

        override fun onDone() {
            if (currentWordIndex < totalWords - 1) {
                currentWordIndex++
                playNextFrame()
            } else resetSpeakingState()
        }

        override fun onStart() {
            _state.value = _state.value.copy(isSpeaking = true)
        }

        private fun resetSpeakingState() {
            viewModelScope.launch {
                _state.value = _state.value.copy(isSpeaking = false)
            }
        }

        private fun playNextFrame() {
            val toIndex = minOf(currentWordIndex + FRAME_SIZE, totalWords - 1)
            _highlightingState.value = _highlightingState.value.copy(
                currentWordIndexInFrame = 0, currentFrame = words.subList(currentWordIndex, toIndex)
            )
            textToSpeech?.speak(_highlightingState.value.currentFrame.joinToString(" "))
        }
    }

    fun startPlaybackService() {
        val intent =
            Intent(application, PlayerService::class.java).setAction(PlayerService.ACTION_PLAY)
        //todo: find a better solution
        PlayerService.playerViewModel = this
        ContextCompat.startForegroundService(application, intent)
    }

    fun stopPlaybackService() {
        val intent = Intent(
            application, PlayerService::class.java
        ).setAction(PlayerService.ACTION_SERVICE_STOP)
        application.startService(intent)
    }

    var playbackProgressCallBack: (Long, Long, Boolean) -> Unit = { _, _, _ -> }

}