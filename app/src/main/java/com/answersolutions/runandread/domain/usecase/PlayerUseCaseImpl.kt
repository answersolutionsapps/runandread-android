package com.answersolutions.runandread.domain.usecase

import com.answersolutions.runandread.BookPlayer
import com.answersolutions.runandread.data.repository.LibraryRepository
import com.answersolutions.runandread.data.repository.PlaybackState
import com.answersolutions.runandread.data.repository.PlayerStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerUseCaseImpl @Inject constructor(
    private val playerStateRepository: PlayerStateRepository,
    private val libraryRepository: LibraryRepository
) : PlayerUseCase {
    
    private var bookPlayer: BookPlayer? = null
    
    override suspend fun play() {
        bookPlayer?.onPlay(source = 1)
        playerStateRepository.updatePlaybackState(
            playerStateRepository.getPlaybackState().first().copy(isPlaying = true)
        )
    }
    
    override suspend fun pause() {
        bookPlayer?.onStopSpeaking()
        playerStateRepository.updatePlaybackState(
            playerStateRepository.getPlaybackState().first().copy(isPlaying = false)
        )
    }
    
    override suspend fun fastForward() {
        bookPlayer?.onFastForward()
    }
    
    override suspend fun fastRewind() {
        bookPlayer?.onRewind()
    }
    
    override suspend fun seekTo(position: Long) {
        bookPlayer?.onUserChangePosition(position.toFloat())
    }
    
    override fun getCurrentPosition(): Flow<Long> = 
        playerStateRepository.getCurrentPosition()
    
    override fun getPlaybackState(): Flow<PlaybackState> = 
        playerStateRepository.getPlaybackState()
    
    fun setBookPlayer(player: BookPlayer) {
        this.bookPlayer = player
    }
    
    fun getCurrentTimeElapsed(): Long {
        return bookPlayer?.currentTimeElapsed() ?: 0
    }
}