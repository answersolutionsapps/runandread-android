package com.answersolutions.runandread.domain.usecase

import com.answersolutions.runandread.BookPlayer
import com.answersolutions.runandread.data.model.Bookmark
import com.answersolutions.runandread.data.repository.PlayerStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkUseCaseImpl @Inject constructor(
    private val playerStateRepository: PlayerStateRepository
) : BookmarkUseCase {
    
    private var bookPlayer: BookPlayer? = null
    
    override suspend fun saveBookmark() {
        bookPlayer?.onSaveBookmark()
    }
    
    override suspend fun deleteBookmark(bookmark: Bookmark) {
        bookPlayer?.onDeleteBookmark(bookmark)
    }
    
    override suspend fun playFromBookmark(position: Int) {
        bookPlayer?.onPlayFromBookmark(position)
    }
    
    override fun getBookmarks(): Flow<List<Bookmark>> {
        return playerStateRepository.getCurrentBook().map { book ->
            book?.bookmarks ?: emptyList()
        }
    }
    
    fun setBookPlayer(player: BookPlayer) {
        this.bookPlayer = player
    }
}