package com.answersolutions.runandread.ui.settings

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.answersolutions.runandread.data.datasource.PrefsStore
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.Bookmark
import com.answersolutions.runandread.data.repository.LibraryRepository
import com.answersolutions.runandread.data.model.EBookFile
import com.answersolutions.runandread.voice.SimpleSpeakingCallBack
import com.answersolutions.runandread.voice.SimpleSpeechProvider
import com.answersolutions.runandread.voice.languageId
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
    val recentSelectionsL: LimitedDictionary = LimitedDictionary(limit = 5U)

    fun payTextSample(language: Locale, voice: Voice, rate: Float) {
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
                            errorCode == TextToSpeech.ERROR_SERVICE) {
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
        val book: Book? = null,
        val title: String = "",
        val author: String = "",
        val language: String = "",
        val voiceIdentifier: String = "",
        val voiceRate: Float = 1.0f
    )

    data class SettingsUIState(
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
        viewModelScope.launch {
            _viewState.emit(_viewState.value.copy(loading = true)) // Immediate UI update

            val book = withContext(Dispatchers.IO) {
                repository.getSelectedBook()
            }
            val recentSelections = prefsStore.selectedLanguages().first() // Get latest value only
            recentSelections.forEach { selected ->
                recentSelectionsL.push(value = selected)
            }

//            withContext(Dispatchers.Main) {
                _state.value = _state.value.copy(
                    book = book,
                    title = book?.title ?: "",
                    author = book?.author ?: "",
                    language = book?.language ?: Locale.getDefault().languageId(),
                    voiceIdentifier = book?.voiceIdentifier ?: "",
                    voiceRate = book?.voiceRate ?: 1.0f
                )
                _viewState.emit(_viewState.value.copy(loading = false)) // Ensure loading indicator stops
//            }
        }
    }


    fun createANewBook(book: EBookFile?) {
        Timber.d("BookSettingsScreenView.createANewBook=>")
        viewModelScope.launch {
            _state.value = _state.value.copy(
                title = book?.title ?: "",
                author = book?.author ?: "",
                language = Locale.getDefault().languageId(),
                voiceIdentifier = "en",
                voiceRate = 1.0f,
            )
        }
    }

    fun contextText(): List<String> {
        return _state.value.book?.text ?: emptyList()
    }

    fun currentPage(): String {
        return if (viewState.value.selectedPage < contextText().size - 1) {
            contextText()[viewState.value.selectedPage]
        } else "1, 2, 3, 4, 5. 5, 4, 3, 2, 1!"
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
                    repository.updateBook(it)
                } ?: run {
                    val book = Book(
                        title = bookState.value.title,
                        author = bookState.value.author,
                        language = bookState.value.language,
                        voiceIdentifier = bookState.value.voiceIdentifier,
                        voiceRate = bookState.value.voiceRate,
                        text = contextText(),
                        lastPosition = 0,
                        created = System.currentTimeMillis(),
                        bookmarks = emptyList<Bookmark>().toMutableList(),
                    )
                    repository.addBook(book)
                    repository.selectBook(book.id)
                }
                prefsStore.saveSelectedLanguages(recentSelectionsL.values)
            }

            // Back to UI thread
            completed()
            _viewState.value = _viewState.value.copy(loading = false)
        }
    }


    fun onPageSelected(page: Int) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(selectedPage = page)
        }

    }

    fun onShowDelete(show: Boolean) {
        _viewState.value = _viewState.value.copy(showDeleteDialog = show)
    }

    fun onDelete() {
        // Handle showing "About" section
        println("onDelete clicked")
        _viewState.value = _viewState.value.copy(showDeleteDialog = false)
    }
}