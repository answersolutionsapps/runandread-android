package com.answersolutions.runandread.domain.usecase

import com.answersolutions.runandread.data.repository.PlaybackState
import kotlinx.coroutines.flow.Flow

interface PlayerUseCase {
    suspend fun play()
    suspend fun pause()
    suspend fun fastForward()
    suspend fun fastRewind()
    suspend fun seekTo(position: Long)
    fun getCurrentPosition(): Flow<Long>
    fun getPlaybackState(): Flow<PlaybackState>
}