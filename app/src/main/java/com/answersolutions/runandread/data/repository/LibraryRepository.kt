package com.answersolutions.runandread.data.repository

import com.answersolutions.runandread.data.datasource.LibraryDataSource
import com.answersolutions.runandread.data.datasource.LibraryDiskDataSource
import com.answersolutions.runandread.data.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface LibraryRepository {
    fun getLibraryBooks(): Flow<List<Book>>
    suspend fun addBook(book: Book)
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(bookId: String)

    suspend fun selectBook(bookId: String)
    suspend fun getSelectedBook(): Book?
    suspend fun unselectBook()
}

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val diskDataSource: LibraryDiskDataSource,
    private val assetDataSource: LibraryDataSource
) : LibraryRepository {

    override fun getLibraryBooks(): Flow<List<Book>> = flow {
        val books = diskDataSource.loadBooks()
        if (books.isEmpty()) {
            // Load from assets if disk library is empty
            val defaultBooks = assetDataSource.loadBooks()
            defaultBooks.forEach { book -> diskDataSource.addBook(book) }
            emit(defaultBooks)
        } else {
            emit(books)
        }
    }

    override suspend fun addBook(book: Book) = diskDataSource.addBook(book)
    override suspend fun updateBook(book: Book) = diskDataSource.updateBook(book)
    override suspend fun deleteBook(bookId: String) = diskDataSource.deleteBook(bookId)

    override suspend fun selectBook(bookId: String) = diskDataSource.selectBook(bookId)
    override suspend fun getSelectedBook(): Book? = diskDataSource.getSelectedBook()
    override suspend fun unselectBook() = diskDataSource.unselectBook()

}