package com.answersolutions.runandread.ui.settings

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.answersolutions.runandread.data.datasource.PrefsStore
import com.answersolutions.runandread.data.model.AudioBook
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.Bookmark
import com.answersolutions.runandread.data.repository.LibraryRepository
import com.answersolutions.runandread.data.model.EBookFile
import com.answersolutions.runandread.data.model.RunAndReadBook
import com.answersolutions.runandread.data.model.TextPart
import com.answersolutions.runandread.voice.SimpleSpeakingCallBack
import com.answersolutions.runandread.voice.SimpleSpeechProvider
import com.answersolutions.runandread.voice.languageId
import com.answersolutions.runandread.voice.toLocale
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.math.min

class LimitedDictionary(val limit: UInt) {
    var values: MutableList<String> = mutableListOf()

    fun push(value: String) {
        val currentValueIndex = values.indexOf(value)

        if (currentValueIndex != -1) {
            val tmp = values[0]
            values[0] = value
            values[currentValueIndex] = tmp
        } else if (values.size < limit.toInt()) {
            values.add(0, value)
        } else {
            values.removeAt(values.lastIndex)
            values.add(0, value)
        }
    }
}

@HiltViewModel
class BookSettingsViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val repository: LibraryRepository,
    private val prefsStore: PrefsStore
) : ViewModel() {

    private var textToSpeech: SimpleSpeechProvider? = null
    private var mediaPlayer: MediaPlayer? = null
    val recentSelectionsL: LimitedDictionary = LimitedDictionary(limit = 5U)

    fun payTextSample(language: Locale, voice: Voice?, rate: Float) {
        voice?.let {
            if (bookState.value.book is Book) {
                val sampleText = currentPage().substring(0, min(currentPage().length, 100))

                if (textToSpeech == null) {
                    textToSpeech = SimpleSpeechProvider(
                        application,
                        currentLocale = language,
                        currentVoice = voice,
                        speechRate = rate,
                        speakingCallBack = object : SimpleSpeakingCallBack {
                            override fun onError(utteranceId: String?, errorCode: Int) {
                                Timber.d("textToSpeech=>1 onError=>$errorCode")
                                if (errorCode == TextToSpeech.ERROR_NETWORK_TIMEOUT ||
                                    errorCode == TextToSpeech.ERROR_NETWORK ||
                                    errorCode == TextToSpeech.ERROR_SERVICE
                                ) {
                                    Timber.e("Network error1, retrying with offline voice...")
                                    // Retry with offline voices or notify the user
                                    viewModelScope.launch {
                                        _viewState.emit(_viewState.value.copy(showVoiceError = true))
                                    }
                                }
                            }
                        }
                    )
                }
                if (textToSpeech?.isSpeaking() == true) {
                    textToSpeech?.stop()
                } else {
                    textToSpeech?.updateLocale(language, voice, rate)
                    textToSpeech?.speak(sampleText)
                }
            }
        }?: run {
            Toast.makeText(
                application,
                "Please select a voice first!",
                Toast.LENGTH_SHORT
            ).show()
        }


    }

    fun payAudioSample() {
        if (bookState.value.book is AudioBook) {
            val book = (bookState.value.book as AudioBook)
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(book.audioFilePath)
                    prepare()
                }
            }

            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.apply {
                    stop()
                    release()
                }
                mediaPlayer = null
            } else {
                mediaPlayer?.apply {
                    playbackParams = playbackParams.setSpeed(bookState.value.voiceRate)
                    start()
                }
            }
        }
    }

    fun dismissVoiceError() {
        viewModelScope.launch {
            _viewState.emit(_viewState.value.copy(showVoiceError = false))
        }
    }

    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking() == true
    }

    fun stopTTS() {
        textToSpeech?.stop()
    }

    data class BookUIState(
        val book: RunAndReadBook? = null,
        val title: String = "",
        val author: String = "",
        val language: String = "",
        val voiceIdentifier: String = "",
        val voiceRate: Float = 1.0f,
        val text: List<String> = emptyList(),


        val audioPath: String = "",
        val parts: List<TextPart> = emptyList(),
        val voice: String = "",
        val model: String = "",
        val bookSource: String = ""
    )

    data class SettingsUIState(
        val newBook: Boolean = false,
        val loading: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val isSpeaking: Boolean = false,
        val selectedPage: Int = 0,
        val showVoiceError: Boolean = false
    )

    private val _state = MutableStateFlow(BookUIState())
    val bookState: StateFlow<BookUIState> get() = _state.asStateFlow()

    private val _viewState = MutableStateFlow(SettingsUIState())
    val viewState: StateFlow<SettingsUIState> get() = _viewState.asStateFlow()


    fun setUpBook() {
        Timber.d("setUpBook=>")
        viewModelScope.launch {
            if (viewState.value.newBook) {
                return@launch
            }
            _state.value = BookUIState()
            _viewState.emit(SettingsUIState(newBook = false, loading = true)) // Immediate UI update

            val book = withContext(Dispatchers.IO) {
                repository.getSelectedBook()
            }
            val recentSelections = prefsStore.selectedLanguages().first() // Get latest value only
            recentSelections.forEach { selected ->
                recentSelectionsL.push(value = selected)
            }
            Timber.d("setUpBook=>${book?.voiceRate}")
            if (book is Book) {
                _state.emit(
                    _state.value.copy(
                        book = book,
                        title = book.title,
                        author = book.author,
                        language = book.language,
                        voiceIdentifier = book.voiceIdentifier,
                        voiceRate = book.voiceRate,
                        text = book.text,
                        audioPath = "",
                        parts = emptyList(),
                        voice = "",
                        model = ""
                    )
                )
            } else if (book is AudioBook) {
                _state.emit(
                    _state.value.copy(
                        book = book,
                        title = book.title,
                        author = book.author,
                        language = book.language,
                        voiceIdentifier = "",
                        voiceRate = book.voiceRate,
                        text = emptyList(),
                        audioPath = book.audioFilePath,
                        parts = book.parts,
                        voice = book.voice,
                        model = book.model
                    )
                )
            }
            _viewState.emit(_viewState.value.copy(loading = false)) // Ensure loading indicator stops
        }
    }

    fun createANewBook(ebook: EBookFile) {
        Timber.d("BookSettingsScreenView.createANewBook=>")
        viewModelScope.launch {

            val language = if (ebook.audioPath.isNotEmpty()) {
                ebook.language
            } else {
                Locale.getDefault().languageId()
            }
            val book = if (ebook.audioPath.isNotEmpty()) {
                AudioBook(
                    title = ebook.title,
                    author = ebook.author,
                    language = language,
                    voiceRate = ebook.rate,
                    audioFilePath = ebook.audioPath,
                    parts = ebook.text,
                    lastPosition = 0,
                    voice = ebook.voice,
                    model = ebook.model,
                    bookSource = ebook.bookSource,
                    updated = System.currentTimeMillis(),
                    bookmarks = emptyList<Bookmark>().toMutableList()
                )
            } else {

                Book(
                    title = ebook.title,
                    author = ebook.author,
                    language = language,
                    voiceIdentifier = "en",
                    voiceRate = 1.0f,
                    text = ebook.content,
                    lastPosition = 0,
                    updated = System.currentTimeMillis(),
                    bookmarks = emptyList<Bookmark>().toMutableList(),
                )
            }
            _state.value = _state.value.copy(
                book = book,
                title = ebook.title,
                author = ebook.author,
                language = language,
                voiceIdentifier = "en",
                voiceRate = ebook.rate,
                text = ebook.content,

                audioPath = ebook.audioPath,
                parts = ebook.text,
                voice = ebook.voice,
                model = ebook.model,
                bookSource = ebook.bookSource
            )
            _viewState.value = SettingsUIState(newBook = true)
        }
    }

    private fun currentPage(): String {
        return if (viewState.value.selectedPage <= _state.value.text.size - 1) {
            _state.value.text[viewState.value.selectedPage]
        } else "1, 2, 3, 4, 5, 5, 4, 3, 2, 1!"
    }


    fun updateBookDetails(
        title: String? = null,
        author: String? = null,
        voiceRate: Float? = null,
        language: String? = null,
        voiceIdentifier: String? = null
    ) {
        _state.value = _state.value.copy(
            title = title ?: _state.value.title,
            author = author ?: _state.value.author,
            voiceRate = voiceRate ?: _state.value.voiceRate,
            language = language ?: _state.value.language,
            voiceIdentifier = voiceIdentifier ?: _state.value.voiceIdentifier
        )
        language?.let {
            recentSelectionsL.push(it)
        }
    }

    fun onSave(completed: () -> Unit) {
        Timber.d("BookSettingsScreenView.onSave=>")
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(loading = true)

            withContext(Dispatchers.IO) {
                _state.value.book?.let {
                    val text = if (bookState.value.text.size > 1) {
                        val from = _viewState.value.selectedPage
                        val to = bookState.value.text.lastIndex
                        bookState.value.text.subList(from, to)
                    } else {
                        bookState.value.text
                    }

                    val b = when (it) {
                        is Book -> {
                            it.copy(
                                title = bookState.value.title,
                                author = bookState.value.author,
                                language = bookState.value.language,
                                voiceIdentifier = bookState.value.voiceIdentifier,
                                voiceRate = bookState.value.voiceRate,
                                text = text,
                            )
                        }

                        is AudioBook -> {
                            it.copy(
                                title = bookState.value.title,
                                author = bookState.value.author,
                                voiceRate = bookState.value.voiceRate,
                            )
                        }
                    }

                    if (viewState.value.newBook) {
                        repository.addBook(b)
                        repository.selectBook(b.id)
                    } else {
                        repository.updateBook(b)
                    }
                }
                prefsStore.saveSelectedLanguages(recentSelectionsL.values)
            }

            // Back to UI thread
            completed()
            _viewState.value = _viewState.value.copy(loading = false)
            mediaPlayer?.apply {
                stop()
                release()
            }
            mediaPlayer = null
        }
    }


    fun onPageSelected(page: Int) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(selectedPage = page)
        }

    }

    fun onShowDelete(show: Boolean) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(showDeleteDialog = show)
        }
    }

    fun onDelete(onBookDeleted: () -> Unit) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(showDeleteDialog = false)
            withContext(Dispatchers.IO) {
                _state.value.book?.let {
                    repository.deleteBook(it)
                }
                withContext(Dispatchers.Main) {
                    onBookDeleted()
                }
            }
        }
    }
}