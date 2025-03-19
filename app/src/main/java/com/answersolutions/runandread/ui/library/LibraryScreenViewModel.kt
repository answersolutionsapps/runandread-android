package com.answersolutions.runandread.ui.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.answersolutions.runandread.data.model.EBookFile
import com.answersolutions.runandread.data.model.RunAndReadBook
import com.answersolutions.runandread.data.repository.EBookRepository
import com.answersolutions.runandread.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryScreenViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val fileRepository: EBookRepository
) : ViewModel() {

    private val _libraryBooks = MutableStateFlow<List<RunAndReadBook>>(emptyList())
    val libraryBooks = _libraryBooks.asStateFlow()

    private val _selectedBook = MutableStateFlow<RunAndReadBook?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    data class LibraryScreenUIState(
        val loading: Boolean = false,
        val showNewBookPicker: Boolean = false,
        val filterText: String = ""
    )

    private val _viewState = MutableStateFlow(LibraryScreenUIState())
    val viewState: StateFlow<LibraryScreenUIState> get() = _viewState.asStateFlow()

    fun loadBooks() {
        viewModelScope.launch {
            libraryRepository.getLibraryBooks().collect { books ->
                _libraryBooks.value = books
            }
            _selectedBook.emit(libraryRepository.getSelectedBook())
        }
    }

    fun onSelectBook(book: RunAndReadBook) {
        viewModelScope.launch {
            libraryRepository.selectBook(book.id)
            _selectedBook.emit(libraryRepository.getSelectedBook())
        }
    }

    fun onShowNewBookPicker(show: Boolean) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(showNewBookPicker = show)
        }
    }

    fun onFilterWithText(text: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(filterText = text)
        }
    }

    fun onUnselectBook() {
        viewModelScope.launch {
            libraryRepository.unselectBook()
            _selectedBook.emit(null)
        }
    }

    fun loadEBookFromUri(uri: Uri, onLoaded:(EBookFile?)-> Unit) {
        viewModelScope.launch {
            _viewState.emit(_viewState.value.copy(loading = true))
            onLoaded(fileRepository.getEBookFileFromUri(uri))
            _viewState.emit(_viewState.value.copy(loading = false))
        }


    }

    fun loadEBookFromClipboard(onLoaded:(EBookFile?)-> Unit) {
        viewModelScope.launch {
            _viewState.emit(_viewState.value.copy(loading = true))
            onLoaded(fileRepository.getEbookFileFromClipboard())
            _viewState.emit(_viewState.value.copy(loading = false))
        }
    }
}
