package com.answersolutions.runandread.data.datasource

import android.content.Context
import com.answersolutions.runandread.data.model.Book
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

interface LibraryDataSource {
    fun loadBooks(): List<Book>

    suspend fun addBook(book: Book)
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(bookId: String)

     suspend fun selectBook(bookId: String)
     suspend fun getSelectedBook(): Book?
     suspend fun unselectBook()
}

@Singleton
class LibraryAssetDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    LibraryDataSource {
    override fun loadBooks(): List<Book> {
        val books = mutableListOf<Book>()
        val assetManager = context.assets
        val fileNames = assetManager.list("default") ?: emptyArray()

        for (fileName in fileNames) {
            val inputStream = assetManager.open("default/$fileName")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonText = reader.readText()
            reader.close()
            inputStream.close()

            val book = Json.decodeFromString(Book.serializer(), jsonText)
            books.add(book)
        }
        return books
    }

    override suspend fun addBook(book: Book) = throw UnsupportedOperationException()
    override suspend fun updateBook(book: Book) = throw UnsupportedOperationException()
    override suspend fun deleteBook(bookId: String) = throw UnsupportedOperationException()

    override suspend fun selectBook(bookId: String) = throw UnsupportedOperationException()
    override suspend fun getSelectedBook(): Book? = throw UnsupportedOperationException()
    override suspend fun unselectBook() = throw UnsupportedOperationException()



}