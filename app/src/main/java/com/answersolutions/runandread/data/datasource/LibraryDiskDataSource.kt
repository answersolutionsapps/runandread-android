package com.answersolutions.runandread.data.datasource

import android.content.Context
import com.answersolutions.runandread.data.model.Book
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
class LibraryDiskDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    LibraryDataSource {
    private val libraryDir: File
        get() = File(context.filesDir, "library").apply { if (!exists()) mkdirs() }

    override fun loadBooks(): List<Book>{
        val books = mutableListOf<Book>()
        libraryDir.listFiles()?.forEach { file ->
            val jsonText = file.readText()
            val book = Json.decodeFromString(Book.serializer(), jsonText)
            books.add(book)
        }
        return books
    }

    override suspend fun addBook(book: Book) {
        val bookFile = File(libraryDir, "${book.id}.json")
        bookFile.writeText(Json.encodeToString(book))
    }

    override suspend fun updateBook(book: Book) {
        val bookFile = File(libraryDir, "${book.id}.json")
        if (bookFile.exists()) {
            bookFile.writeText(Json.encodeToString(book))
        }
    }

    override suspend fun deleteBook(bookId: String) {
        val bookFile = File(libraryDir, "$bookId.json")
        bookFile.delete()
    }

    private val selectedDir: File
        get() = File(context.filesDir, "selected").apply { if (!exists()) mkdirs() }

    private val selectedFile: File
        get() = File(selectedDir, "selected_book.txt")

    override suspend fun selectBook(bookId: String) {
        selectedFile.writeText(bookId)
    }

    override suspend fun getSelectedBook(): Book? = withContext(Dispatchers.IO) {
        if (!selectedFile.exists()) return@withContext null

//        val startTime = System.currentTimeMillis()

        val selectedId = async { selectedFile.inputStream().bufferedReader().use { it.readText().trim() } }.await()
        val selectedBookFile = File(libraryDir, "$selectedId.json")

        val book = if (selectedBookFile.exists()) {
            val jsonText = async { selectedBookFile.inputStream().bufferedReader().use { it.readText() } }.await()
            async { Json.decodeFromString(Book.serializer(), jsonText) }.await()
        } else {
            null
        }

//        val endTime = System.currentTimeMillis()
//        Timber.d("getSelectedBook() took ${endTime - startTime}ms")

        return@withContext book
    }

    override suspend fun unselectBook() {
        if (selectedFile.exists()) {
            selectedFile.delete()
        }
    }
}
