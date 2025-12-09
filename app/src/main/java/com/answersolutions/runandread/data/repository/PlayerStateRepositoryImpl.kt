package com.answersolutions.runandread.data.repository

import com.answersolutions.runandread.data.model.RunAndReadBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerStateRepositoryImpl @Inject constructor() : PlayerStateRepository {
    private val _currentBook = MutableStateFlow<RunAndReadBook?>(null)
    private val _playbackState = MutableStateFlow(PlaybackState())
    private val _currentPosition = MutableStateFlow(0L)
    
    override fun getCurrentBook(): Flow<RunAndReadBook?> = _currentBook.asStateFlow()
    override fun getPlaybackState(): Flow<PlaybackState> = _playbackState.asStateFlow()
    override fun getCurrentPosition(): Flow<Long> = _currentPosition.asStateFlow()
    
    override suspend fun updatePlaybackState(state: PlaybackState) {
        _playbackState.value = state
    }
    
    override suspend fun updateCurrentPosition(position: Long) {
        _currentPosition.value = position
    }
    
    override suspend fun setCurrentBook(book: RunAndReadBook?) {
        _currentBook.value = book
    }
}