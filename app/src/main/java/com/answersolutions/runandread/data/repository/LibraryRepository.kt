package com.answersolutions.runandread.data.repository

import com.answersolutions.runandread.data.datasource.LibraryDataSource
import com.answersolutions.runandread.data.datasource.LibraryDiskDataSource
import com.answersolutions.runandread.data.model.RunAndReadBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface LibraryRepository {
    fun getLibraryBooks(): Flow<List<RunAndReadBook>>
    suspend fun addBook(book: RunAndReadBook)
    suspend fun updateBook(book: RunAndReadBook)
    suspend fun deleteBook(book: RunAndReadBook)

    suspend fun selectBook(bookId: String)
    suspend fun getSelectedBook(): RunAndReadBook?
    suspend fun unselectBook()
}

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val diskDataSource: LibraryDiskDataSource,
    private val assetDataSource: LibraryDataSource
) : LibraryRepository {

    override fun getLibraryBooks(): Flow<List<RunAndReadBook>> = flow {
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

    override suspend fun addBook(book: RunAndReadBook) = diskDataSource.addBook(book)
    override suspend fun updateBook(book: RunAndReadBook) = diskDataSource.updateBook(book)
    override suspend fun deleteBook(book: RunAndReadBook) = diskDataSource.deleteBook(book)

    override suspend fun selectBook(bookId: String) = diskDataSource.selectBook(bookId)
    override suspend fun getSelectedBook(): RunAndReadBook? = diskDataSource.getSelectedBook()
    override suspend fun unselectBook() = diskDataSource.unselectBook()

}