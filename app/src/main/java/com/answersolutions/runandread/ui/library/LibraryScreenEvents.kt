package com.answersolutions.runandread.ui.library

import android.widget.Toast
import com.answersolutions.runandread.data.model.EBookFile
import com.answersolutions.runandread.data.model.RunAndReadBook

sealed class LibraryScreenEvents {
    data object AboutClicked : LibraryScreenEvents()
    data object NewBookClicked : LibraryScreenEvents()
    data class FilterWithText(val text: String) : LibraryScreenEvents()
    data class SelectBook(val book: RunAndReadBook) : LibraryScreenEvents()
    data object DismissRequest : LibraryScreenEvents()
    data object FileOptionSelected : LibraryScreenEvents()
    data object ClipboardOptionSelected : LibraryScreenEvents()
}

fun LibraryScreenEvents.onEvent(
    context: android.content.Context,
    model: LibraryScreenViewModel,
    onSelect: (RunAndReadBook) -> Unit,
    onAboutClicked: () -> Unit,
    onFileSelected: (EBookFile) -> Unit,
    onLauncher: () -> Unit
) {

    when (this) {
        LibraryScreenEvents.AboutClicked -> onAboutClicked()
        LibraryScreenEvents.NewBookClicked -> model.onShowNewBookPicker(true)
        is LibraryScreenEvents.FilterWithText -> model.onFilterWithText(this.text)
        is LibraryScreenEvents.SelectBook -> onSelect(this.book)
        LibraryScreenEvents.DismissRequest -> model.onShowNewBookPicker(false)
        LibraryScreenEvents.FileOptionSelected -> {
            model.onShowNewBookPicker(false)
            // File picker only allows EPUB, TXT, and PDF files
            onLauncher()
        }

        LibraryScreenEvents.ClipboardOptionSelected -> {
            model.onShowNewBookPicker(false)
            model.loadEBookFromClipboard {
                it?.let {
                    onFileSelected(it)
                } ?: run {
                    Toast.makeText(context, "The clipboard is empty!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}