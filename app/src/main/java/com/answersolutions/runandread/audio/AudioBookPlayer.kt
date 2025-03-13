package com.answersolutions.runandread.audio

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class AudioBookPlayer (
    private val filePath: String,
    private val playbackRate: Float = 1.0f,
    private val listener: PlayerListener
){
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false

    interface PlayerListener {
        fun onStart()
        fun onPause()
        fun onCompleted()
        fun onProgressUpdate(progress: Int, duration: Int)
    }

    fun play() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setPlaybackSpeed(playbackRate)
                setOnCompletionListener {
                    this@AudioBookPlayer.isPlaying = false
                    listener.onCompleted()
                }
                start()
                this@AudioBookPlayer.isPlaying = true
                listener.onStart()
                startProgressUpdates()
            }
        } else if (!isPlaying) {
            mediaPlayer?.apply {
                start()
                this@AudioBookPlayer.isPlaying = true
                listener.onStart()
                startProgressUpdates()
            }
        }
    }

    fun pause() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                this@AudioBookPlayer.isPlaying = false
                listener.onPause()
            }
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        isPlaying = false
    }

    fun fastForward(seconds: Int) {
        mediaPlayer?.apply {
            val newPosition = currentPosition + (seconds * 1000)
            seekTo(newPosition.coerceAtMost(duration))
        }
    }

    fun rewind(seconds: Int) {
        mediaPlayer?.apply {
            val newPosition = currentPosition - (seconds * 1000)
            seekTo(newPosition.coerceAtLeast(0))
        }
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    private fun MediaPlayer.setPlaybackSpeed(speed: Float) {
        playbackParams = playbackParams.setSpeed(speed)
    }

    private fun startProgressUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (isPlaying) {
                        listener.onProgressUpdate(it.currentPosition / 1000, it.duration / 1000)
                        handler.postDelayed(this, 500) // Update every second
                    }
                }
            }
        })
    }
}