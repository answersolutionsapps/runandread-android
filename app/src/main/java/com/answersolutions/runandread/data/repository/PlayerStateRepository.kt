package com.answersolutions.runandread.data.repository

import com.answersolutions.runandread.data.model.RunAndReadBook
import kotlinx.coroutines.flow.Flow

interface PlayerStateRepository {
    fun getCurrentBook(): Flow<RunAndReadBook?>
    fun getPlaybackState(): Flow<PlaybackState>
    fun getCurrentPosition(): Flow<Long>
    suspend fun updatePlaybackState(state: PlaybackState)
    suspend fun updateCurrentPosition(position: Long)
    suspend fun setCurrentBook(book: RunAndReadBook?)
}

data class PlaybackState(
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val speed: Float = 1.0f
)