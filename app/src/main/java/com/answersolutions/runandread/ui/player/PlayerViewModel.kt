package com.answersolutions.runandread.ui.player

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.answersolutions.extensions.formatSecondsToHMS
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.Book.Companion.SECONDS_PER_CHARACTER
import com.answersolutions.runandread.data.repository.LibraryRepository
import com.answersolutions.runandread.data.repository.VoiceRepository
import com.answersolutions.runandread.voice.SpeakingCallBack
import com.answersolutions.runandread.voice.SpeechProvider
import com.answersolutions.runandread.voice.toVoice
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

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

    lateinit var selectedBook: Book

    fun saveBookChanges() {
        viewModelScope.launch {
            libraryRepository.updateBook(selectedBook)
        }
    }

    private lateinit var textToSpeech: SpeechProvider

    private var words = listOf<String>()
    private var selectedSpeechRate: Float = 1f
    private var currentWordIndex = 0
    private var currentFrame = listOf<String>()
    private var currentWordIndexInFrame = 0
    private var totalWords: Int = 0

    data class PlayerUIState(
        val isSpeaking: Boolean = false,
        val spokenTextRange: IntRange = 0..0,
        val progress: Float = 0f,
        val progressTime: String = "00:00",
        val totalTimeString: String = "00:00"
    )

    private val _state = MutableStateFlow(PlayerUIState())
    val viewState: StateFlow<PlayerUIState> get() = _state.asStateFlow()

    fun setUpBook(book: Book) {
        viewModelScope.launch {
            selectedBook = book

            val selectedLanguage = Locale(book.language)
            val voice = repository.nameToVoice(book.voiceIdentifier, book.language)
            words = book.text.flatMap { it.split("\\s+".toRegex()) }
                .mapNotNull { it.takeIf { it.isNotEmpty() } }

            totalWords = words.size
            currentWordIndexInFrame = 0
//            selectedVoice = voice.toVoice()
//            selectedLocale = selectedLanguage
            selectedSpeechRate = book.voiceRate

            val secondsElapsed = calculateElapsedTime(totalWords - 1)
            _state.value = _state.value.copy(
                totalTimeString = secondsElapsed.formatSecondsToHMS()
            )
            updatePosition(book.lastPosition.toFloat())

//            safeLet(selectedLocale, selectedVoice) { l, v ->
            textToSpeech = SpeechProvider(
                application,
                currentLocale = selectedLanguage,
                currentVoice = voice.toVoice(),
                speechRate = selectedSpeechRate,
                callBack = speechRangeCallBack,
                speakingCallBack = speakingCallBack
            )
//            }
        }
    }

    fun sliderRange(): ClosedFloatingPointRange<Float> {
        return 0f..words.count().toFloat()
    }

    fun speak() {
        if (isSpeaking()) return
        currentWordIndexInFrame = 0
        val toIndex = minOf(currentWordIndex + FRAME_SIZE, totalWords - 1)
        currentFrame =
            words.subList(fromIndex = currentWordIndex, toIndex = toIndex)
                .toList()
        textToSpeech.speak(currentFrame.joinToString(" "))
    }

    fun isSpeaking(): Boolean {
        return textToSpeech.isSpeaking()
    }

    fun stopSpeaking() {
        viewModelScope.launch {
            textToSpeech.stop()
        }
    }

    private val speechRangeCallBack: (range: IntRange) -> Unit = { range ->
        if (currentWordIndexInFrame < currentFrame.size - 1) {
            _state.value = _state.value.copy(
                spokenTextRange = range
            )
            currentWordIndex += 1
            currentWordIndexInFrame += 1
            val secondsElapsed = (words.take(currentWordIndex)
                .joinToString(" ").length * SECONDS_PER_CHARACTER) / selectedSpeechRate.toDouble()

            _state.value = _state.value.copy(
                progress = currentWordIndex.toFloat(),
                progressTime = secondsElapsed.formatSecondsToHMS()
            )
            selectedBook = selectedBook.copy(lastPosition = currentWordIndex)
        }
    }

    fun fastForward() = seek(SEEK_STEP)
    fun fastRewind() = seek(-SEEK_STEP)

    private fun seek(offset: Int) {
        textToSpeech.stop()
        updatePosition(currentWordIndex.toFloat() + offset)
        speak()
    }

    fun updatePosition(value: Float) {
        viewModelScope.launch {
            currentWordIndex = value.coerceIn(0f, totalWords.toFloat() - 1).toInt()
            currentWordIndexInFrame = 0
            currentFrame = emptyList()
            updateProgress()
            selectedBook = selectedBook.copy(lastPosition = currentWordIndex)
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
        val chars = words.take(progress)
            .joinToString(" ").length
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
            currentWordIndexInFrame = 0
            val toIndex = minOf(currentWordIndex + FRAME_SIZE, totalWords - 1)
            currentFrame = words.subList(currentWordIndex, toIndex)
            textToSpeech.speak(currentFrame.joinToString(" "))
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
            application,
            PlayerService::class.java
        ).setAction(PlayerService.ACTION_SERVICE_STOP)
        application.startService(intent)
    }

}