package com.answersolutions.runandread.ui.library

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.EBookFile
import com.answersolutions.runandread.data.repository.EBookRepository
import com.answersolutions.runandread.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LibraryScreenViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val fileRepository: EBookRepository
) : ViewModel() {

    private val _libraryBooks = MutableStateFlow<List<Book>>(emptyList())
    val libraryBooks = _libraryBooks.asStateFlow()

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    fun loadBooks() {
        viewModelScope.launch {
            libraryRepository.getLibraryBooks().collect { books ->
                _libraryBooks.value = books
            }
            _selectedBook.emit(libraryRepository.getSelectedBook())
        }
    }

    fun onSelectBook(book: Book) {
        viewModelScope.launch {
            libraryRepository.selectBook(book.id)
            _selectedBook.emit(libraryRepository.getSelectedBook())
        }
    }

    fun onUnselectBook() {
        viewModelScope.launch {
            libraryRepository.unselectBook()
            _selectedBook.emit(null)
        }
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            libraryRepository.addBook(book)
            loadBooks()
        }
    }

    fun saveBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            libraryRepository.updateBook(book)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            libraryRepository.updateBook(book)
            loadBooks()
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            libraryRepository.deleteBook(bookId)
            loadBooks()
        }
    }

    fun loadEBookFromUri(uri: Uri, onLoaded:(EBookFile?)-> Unit) {
        viewModelScope.launch {
            onLoaded(fileRepository.getEBookFileFromUri(uri))
        }
    }

    fun loadEBookFromClipboard(onLoaded:(EBookFile?)-> Unit) {
        viewModelScope.launch {
            onLoaded(fileRepository.getEbookFileFromClipboard())
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val libraryBooks: List<Book>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Success(emptyList()))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    var searchText = mutableStateOf("")
    var showFilePicker = mutableStateOf(false)
    var isLoading = mutableStateOf(false)

//    var dataSource = listOf<Book>()
    var filteredBooks = mutableStateOf(listOf<Book>())

//    fun loadBooks() {
//        // Simulate loading books from a data source
//        val book1 = Book("Book 1", "Author 1", "English", false, false, "30 mins", "60 mins")
//        val book2 = Book("Book 2", "Author 2", "English", true, false, "50 mins", "80 mins")
//        val book3 = Book("Book 3", "Author 3", "English", false, true, "50 mins", "80 mins")
//        dataSource = listOf(book1, book2, book3)
//        filteredBooks.value = dataSource
//    }

//    fun onSelectBook(book: Book) {
////        // Handle book selection
//        println("Book selected: ${book.title}")
////        navController.navigate(Screen.Player.route)
//    }

    fun onShowFilePicker() {
        showFilePicker.value = true
    }

//    fun onFileSelected(fileURL: String) {
//        // Handle file selection
//        println("File selected: $fileURL")
//    }

    fun onPasteFromClipboard(text: String) {
        // Handle clipboard paste
        println("Text from clipboard: $text")
    }

//    fun onShowAbout() {
//        // Handle showing "About" section
//        println("About clicked")
//        navController.navigate(Screen.About.route)
//    }

    fun onBackToForegraund() {
        // Handle app returning to foreground
        println("App is active")
    }

    fun isLoading() = isLoading.value
}

//class FakeLibraryScreenViewModel : LibraryScreenViewModel {
//    init {
//        // Populate with mock data
//        _libraryBooks.value = listOf(
//            Book(
//                id = "1",
//                title = "Moby Dick",
//                author = "Herman Melville",
//                language = "en",
//                voiceRate = 1.25f,
//                text = listOf("Call me Ishmael."),
//                lastPosition = 0,
//                created = System.currentTimeMillis()
//            )
//        )
//    }
//}