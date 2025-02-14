package com.answersolutions.runandread.ui.library

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.EBookFile
import com.answersolutions.runandread.ui.theme.RunAndReadTheme


@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    RunAndReadTheme {
        LibraryScreenContent(
            books = Book.stab(),
            filterBooks = Book.stab(),
            filterText = "",
            isLoading = false,
            onAboutClicked = {},
            onNewBookClicked = {},
            onFilterWithText = {},
            onSelect = {},
            expanded = false,
            onDismissRequest = {
            },
            onFileOptionSelected = {

            },
            onClipboardOptionSelected = {

            }
        )
    }
}

@Composable
fun LibraryScreenView(
    viewModel: LibraryScreenViewModel,
    onSelect: (Book) -> Unit,
    onAboutClicked: () -> Unit,
    onFileSelected:(EBookFile)->Unit
) {
    val books by viewModel.libraryBooks.collectAsState(initial = emptyList())
    val isLoading = viewModel.isLoading()
    var filterText by remember { mutableStateOf("") }
    var filterBooks by remember { mutableStateOf(emptyList<Book>()) }

    val context = LocalContext.current

//    val clipboardManager = LocalClipboardManager.current
    var expanded by remember { mutableStateOf(false) }

    // File Picker Launcher
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.loadEBookFromUri(it) { selected->
                    selected?.let {
                        onFileSelected(selected)
                    }?: run {
                        Toast.makeText(context, "Wrong file is selected, please try a different book!", Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }

    LaunchedEffect("init") {
        viewModel.loadBooks()
    }

    filterBooks = if (filterText.isNotEmpty()) {
        books.filter {
            it.title.contains(filterText, true) ||
                    it.author.contains(filterText, true)
        }
    } else {
        books
    }
    LibraryScreenContent(
        books = books,
        filterBooks = filterBooks,
        filterText = filterText,
        isLoading = isLoading,
        onAboutClicked = onAboutClicked,
        onNewBookClicked = {
            expanded = true
//            viewModel.onShowFilePicker()
        },
        onFilterWithText = { filterText = it },
        onSelect = onSelect,
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        },
        onFileOptionSelected = {
            expanded = false
            // File picker only allows EPUB, TXT, and PDF files
            launcher.launch(arrayOf("application/epub+zip", "text/plain", "application/pdf"))
        },
        onClipboardOptionSelected = {
            expanded = false
            viewModel.loadEBookFromClipboard {
                it?.let {
                    onFileSelected(it)
                }?: run {
                    Toast.makeText(context, "The clipboard is empty!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenContent(
    books: List<Book>,
    filterBooks: List<Book>,
    filterText: String,
    isLoading: Boolean,
    onAboutClicked: () -> Unit,
    onNewBookClicked: () -> Unit,
    onFilterWithText: (String) -> Unit,
    onSelect: (Book) -> Unit,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onFileOptionSelected: () -> Unit,
    onClipboardOptionSelected: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eyes-Free Library") },
                actions = {
                    IconButton(onClick = onAboutClicked) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                    Spacer(modifier = Modifier.weight(1F))
                    Text("Eyes-Free Library", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.weight(1F))
                    Column {
                        IconButton(onClick = onNewBookClicked) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = onDismissRequest
                        ) {
                            DropdownMenuItem(
                                text = { Text("From File") },
                                onClick = onFileOptionSelected,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.FolderOpen,
                                        contentDescription = "File Picker"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("From Clipboard") },
                                onClick = onClipboardOptionSelected,
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
                SearchBar(text = filterText, onTextChanged = onFilterWithText)

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
                                    onSelect(book)
                                }
                            )
                        }
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
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
