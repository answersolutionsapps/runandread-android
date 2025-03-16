package com.answersolutions.runandread

import com.answersolutions.runandread.data.model.Bookmark

interface BookPlayer {
    fun onPlay()
    fun onPlayFromBookmark(position: Int)
    fun onStopSpeaking()
    fun onFastForward()
    fun onRewind()
    fun onUserChangePosition(value: Float)
    fun onSaveBookmark()
    fun onDeleteBookmark(bookmark: Bookmark)
    fun onClose()

    fun currentTimeElapsed(): Long
}