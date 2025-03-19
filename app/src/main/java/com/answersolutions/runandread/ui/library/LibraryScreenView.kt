package com.answersolutions.runandread.ui.library

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.answersolutions.runandread.data.model.EBookFile
import com.answersolutions.runandread.data.model.RunAndReadBook
import com.answersolutions.runandread.ui.library.components.BookItemView
import com.answersolutions.runandread.ui.theme.RunAndReadTheme


@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    RunAndReadTheme {
        LibraryScreenContent(
            uiState = LibraryScreenViewModel.LibraryScreenUIState(
                filterText = "",
                showNewBookPicker = false,
            ),
            books = RunAndReadBook.sampleBooks(),
            filterBooks = RunAndReadBook.sampleBooks(),
            onEvent = {
            }
        )
    }
}

@Composable
fun LibraryScreenView(
    viewModel: LibraryScreenViewModel,
    onSelect: (RunAndReadBook) -> Unit,
    onAboutClicked: () -> Unit,
    onFileSelected: (EBookFile) -> Unit
) {
    val books by viewModel.libraryBooks.collectAsState(initial = emptyList())
    val uiState by viewModel.viewState.collectAsState()
    var filterBooks by remember { mutableStateOf(emptyList<RunAndReadBook>()) }

    val context = LocalContext.current

    // File Picker Launcher
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.loadEBookFromUri(it) { selected ->
                    selected?.let { onFileSelected(selected) } ?: run {
                        Toast.makeText(context, "Invalid file selected. Please choose a valid book! \n [.pdf, .epub, .txt, .randr]", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    LaunchedEffect("init") {
        viewModel.loadBooks()
    }

    filterBooks = if (uiState.filterText.isNotEmpty()) {
        books.filter {
            it.title.contains(uiState.filterText, true) ||
                    it.author.contains(uiState.filterText, true)
        }.sortedByDescending { it.updated }
    } else {
        books.sortedByDescending { it.updated }
    }

    LibraryScreenContent(
        uiState = uiState,
        books = books,
        filterBooks = filterBooks,
        onEvent = { event ->
            event.onEvent(
                context,
                viewModel,
                onSelect = onSelect,
                onAboutClicked = onAboutClicked,
                onFileSelected = onFileSelected,
                onLauncher = {
                    // File picker only allows RANDR, EPUB, TXT, and PDF files
                    launcher.launch(arrayOf("*/*"))
                })
        }
    )
    if (uiState.loading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xcF7f7f7f))
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = colorScheme.primary
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenContent(
    uiState: LibraryScreenViewModel.LibraryScreenUIState,
    books: List<RunAndReadBook>,
    filterBooks: List<RunAndReadBook>,
    onEvent: (LibraryScreenEvents) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eyes-Free Library") },
                actions = {
                    IconButton(onClick = { onEvent(LibraryScreenEvents.AboutClicked) }) {
                        Icon(
                            Icons.Default.Info, contentDescription = "About",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1F))
                    Text("Eyes-Free Library", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.weight(1F))
                    Column {
                        IconButton(onClick = { onEvent(LibraryScreenEvents.NewBookClicked) }) {
                            Icon(
                                Icons.Default.Add, contentDescription = "Add",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = uiState.showNewBookPicker,
                            onDismissRequest = { onEvent(LibraryScreenEvents.DismissRequest) }
                        ) {
                            DropdownMenuItem(
                                text = { Text("From File") },
                                onClick = { onEvent(LibraryScreenEvents.FileOptionSelected) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.FolderOpen,
                                        contentDescription = "File Picker"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("From Clipboard") },
                                onClick = { onEvent(LibraryScreenEvents.ClipboardOptionSelected) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ContentPaste,
                                        contentDescription = "Clipboard"
                                    )
                                }
                            )
                        }
                    }
                }
            )
        },
        content = { padding ->
            Column(Modifier.padding(padding)) {
                SearchBar(
                    text = uiState.filterText,
                    onTextChanged = { onEvent(LibraryScreenEvents.FilterWithText(it)) })

                if (books.isEmpty()) {
                    EmptyLibraryView()
                } else if (filterBooks.isEmpty()) {
                    EmptyFilterLibraryView()
                } else {
                    LazyColumn {
                        items(filterBooks) { book ->
                            LaunchedEffect(book.id) { // Runs once per book when it enters composition
                                book.lazyCalculate { /* No-op or handle completion */ }
                            }
                            BookItemView(
                                item = book,
                                onSelect = {
                                    onEvent(LibraryScreenEvents.SelectBook(book))
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun EmptyLibraryView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Hit the plus button to open your first book and enjoy eyes-free reading!",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyFilterLibraryView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "There are no books with this search criteria!",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SearchBar(text: String, onTextChanged: (String) -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChanged,
        label = { Text("Search") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { onTextChanged("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        }
    )
}
