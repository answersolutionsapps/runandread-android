package com.answersolutions.runandread.domain.usecase

import com.answersolutions.runandread.data.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkUseCase {
    suspend fun saveBookmark()
    suspend fun deleteBookmark(bookmark: Bookmark)
    suspend fun playFromBookmark(position: Int)
    fun getBookmarks(): Flow<List<Bookmark>>
}