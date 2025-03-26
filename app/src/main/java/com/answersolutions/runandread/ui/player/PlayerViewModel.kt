package com.answersolutions.runandread.ui.player

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.answersolutions.runandread.BookPlayer
import com.answersolutions.runandread.audio.AudioBookPlayer
import com.answersolutions.runandread.data.model.AudioBook
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.Bookmark
import com.answersolutions.runandread.data.model.RunAndReadBook
import com.answersolutions.runandread.data.repository.LibraryRepository
import com.answersolutions.runandread.data.repository.VoiceRepository
import com.answersolutions.runandread.services.PlayerService
import com.answersolutions.runandread.voice.SpeakingCallBack
import com.answersolutions.runandread.voice.SpeechBookPlayer
import com.answersolutions.runandread.voice.toVoice
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

object TextTimeRelationsTools {
    fun getCurrentWordIndex(
        elapsedSeconds: Double,
        words: List<String>,
        currentStartTime: Int,
        nextStartTime: Int
    ): Int {
        // Compute duration between parts in seconds
        val durationBetweenParts = (nextStartTime - currentStartTime)

        // Prevent division by zero if words list is empty
        if (words.isEmpty() || durationBetweenParts <= 0) return 0

        // Approximate time per word
        val millisecondsPerWord = durationBetweenParts / words.size

        // Calculate relative time within the current segment
        val relativeTimeInSegment = (elapsedSeconds * 1000.0) - (currentStartTime)

        // Compute word index, ensuring it's within valid bounds
        return relativeTimeInSegment.div(millisecondsPerWord).toInt().coerceIn(0, words.size - 1)
    }

    fun getCurrentBookmarkText(
        elapsedSeconds: Double,
        currentText: String,
        currentStartTime: Int,
        nextStartTime: Int,
        nextText: String?,
    ): String {
        // Calculate duration between text parts in seconds
        val durationBetweenPartsMs = (nextStartTime - currentStartTime)

        // Split text into words, removing empty ones
        val words = currentText.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val nextWords = nextText?.split(Regex("\\s+"))?.filter { it.isNotEmpty() } ?: emptyList()

        // Prevent division by zero if words list is empty
        if (words.isEmpty() || durationBetweenPartsMs <= 0) return ""

        // Approximate time per word
        val millisecondsPerWord = durationBetweenPartsMs / words.size

        // Calculate relative elapsed time within the current segment
        val relativeTimeInSegment = (elapsedSeconds * 1000.0) - currentStartTime

        // Compute the word index, ensuring it's within valid bounds
        val wordIndex =
            relativeTimeInSegment.div(millisecondsPerWord).toInt().coerceIn(0, words.size - 1)

        // Bookmark offsets (Replace with actual values from TextToSpeechPlayer)
        val bookmarkOffset = 5
        val bookmarkTextLength = 30
        val extendedText = words + nextWords
        // Determine start and end indices for bookmark text
        val startIndex = (wordIndex - bookmarkOffset).coerceAtLeast(0)
        val endIndex = (startIndex + bookmarkTextLength).coerceAtMost(extendedText.size)

        // Join words into a substring for bookmark preview
        return extendedText.subList(startIndex, endIndex).joinToString(" ")
    }
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val repository: VoiceRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel(), SpeakingCallBack {

    override var book: RunAndReadBook? = null
    private var player: BookPlayer? = null

    fun saveBookChanges() {
        viewModelScope.launch {
            book?.let {
                libraryRepository.updateBook(it)
            }
        }
    }

    data class PlayerUIState(
        val totalTime: Double = 0.0,
        val isSpeaking: Boolean = false,
        val progress: Float = 0f,
        val progressTime: String = "00:00",
        val totalTimeString: String = "00:00",
        val bookmarks: List<Bookmark> = emptyList(),
        val sliderRange: ClosedFloatingPointRange<Float> = 0f..0f
    )

    data class HighlightingUIState(
        val currentWordIndexInFrame: Int = 0, val currentFrame: List<String> = listOf()
    )


    private val _highlightingState = MutableStateFlow(HighlightingUIState())
    override val highlightingState: StateFlow<HighlightingUIState> get() = _highlightingState.asStateFlow()

    override fun onUpdateHighlightingUI(state: HighlightingUIState) {
        viewModelScope.launch {
            _highlightingState.emit(state)
        }
    }

    private val _state = MutableStateFlow(PlayerUIState())
    override val viewState: StateFlow<PlayerUIState> get() = _state.asStateFlow()
    override fun onUpdateUI(state: PlayerUIState) {
        viewModelScope.launch {
            _state.emit(state)
        }
    }

    fun setUpBook() {
        viewModelScope.launch {

            _highlightingState.value = HighlightingUIState()
            _state.value = PlayerUIState()

            book = withContext(Dispatchers.IO) {
                libraryRepository.getSelectedBook()
            }
            book?.let { book ->
                player = when (book) {
                    is Book -> {
                        val voice =
                            repository.nameToVoice(book.voiceIdentifier, book.language)
                        SpeechBookPlayer(
                            application,
                            voice = voice.toVoice(),
                            speakingCallback = this@PlayerViewModel
                        )
                    }

                    is AudioBook -> AudioBookPlayer(
                        application,
                        speakingCallback = this@PlayerViewModel
                    )
                }
                startPlaybackService()
            }
        }
    }


    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            player?.onDeleteBookmark(bookmark)
        }
    }

    fun playFromBookmark(position: Int) {
        viewModelScope.launch {
            player?.onPlayFromBookmark(position)
        }
    }

    fun onPause() {
        player?.onStopSpeaking()
    }

    fun onPlay() {
        player?.onPlay(source = 1)
    }


    fun onClose() {
        viewModelScope.launch {
            stopPlaybackService()
            player?.onClose()
        }
    }

    fun saveBookmark() {
        viewModelScope.launch {
            player?.onSaveBookmark()
        }
    }

    fun fastForward() = player?.onFastForward()
    fun fastRewind() = player?.onRewind()


    fun onSliderValueChange(value: Float) {
        viewModelScope.launch {
            player?.onUserChangePosition(value)
        }
    }

    override fun onCompleted() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSpeaking = false)
        }
    }

    override fun onStop() = resetSpeakingState()

    override fun onReady(uiState: PlayerUIState) {
        viewModelScope.launch {
            _state.value = uiState
        }
    }

    override fun onStart() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSpeaking = true)

        }
    }

    override fun onProgressUpdate(
        updatedBook: RunAndReadBook,
        pUIState: PlayerUIState,
        hUIState: HighlightingUIState
    ) {
        viewModelScope.launch {
            book = updatedBook
            _state.value = pUIState
            _highlightingState.value = hUIState
            playbackProgressCallBack(
                _state.value.progress.toLong(),
                _state.value.totalTime.toLong(),
                _state.value.isSpeaking
            )
        }

    }

    private fun resetSpeakingState() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSpeaking = false)
        }
    }

    private fun startPlaybackService() {
        val intent =
            Intent(application, PlayerService::class.java)
        //todo: find a better solution
        PlayerService.playerViewModel = this
        ContextCompat.startForegroundService(application, intent)
    }

    private fun stopPlaybackService() {
        val intent = Intent(
            application, PlayerService::class.java
        ).setAction(PlayerService.ACTION_SERVICE_STOP)
        application.startService(intent)
    }

    var playbackProgressCallBack: (Long, Long, Boolean) -> Unit = { _, _, _ -> }

    fun currentTimeElapsed(): Long {
        return player?.currentTimeElapsed() ?: 0
    }
}