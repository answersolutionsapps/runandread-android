package com.answersolutions.runandread.data.datasource

import android.content.Context
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.RunAndReadBook
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

interface LibraryDataSource {
    fun loadBooks(): List<RunAndReadBook>

    suspend fun addBook(book: RunAndReadBook)
    suspend fun updateBook(book: RunAndReadBook)
    suspend fun deleteBook(book: RunAndReadBook)

     suspend fun selectBook(bookId: String)
     suspend fun getSelectedBook(): RunAndReadBook?
     suspend fun unselectBook()
}

@Singleton
class LibraryAssetDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    LibraryDataSource {
    override fun loadBooks(): List<RunAndReadBook> {
        val books = mutableListOf<RunAndReadBook>()
        val assetManager = context.assets
        val fileNames = assetManager.list("default") ?: emptyArray()

        for (fileName in fileNames) {
            val inputStream = assetManager.open("default/$fileName")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonText = reader.readText()
            reader.close()
            inputStream.close()
            val json = Json { ignoreUnknownKeys = true }
            val book = json.decodeFromString(Book.serializer(), jsonText)
            books.add(book)
        }
        return books
    }

    override suspend fun addBook(book: RunAndReadBook) = throw UnsupportedOperationException()
    override suspend fun updateBook(book: RunAndReadBook) = throw UnsupportedOperationException()
    override suspend fun deleteBook(book: RunAndReadBook) = throw UnsupportedOperationException()

    override suspend fun selectBook(bookId: String) = throw UnsupportedOperationException()
    override suspend fun getSelectedBook(): RunAndReadBook? = throw UnsupportedOperationException()
    override suspend fun unselectBook() = throw UnsupportedOperationException()



}