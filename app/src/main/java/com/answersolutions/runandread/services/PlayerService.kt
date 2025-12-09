package com.answersolutions.runandread.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.answersolutions.runandread.R
import com.answersolutions.runandread.data.repository.PlayerStateRepository
import com.answersolutions.runandread.domain.usecase.BookmarkUseCase
import com.answersolutions.runandread.domain.usecase.PlayerUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class PlayerService : Service() {

    @Inject
    lateinit var playerUseCase: PlayerUseCase

    @Inject
    lateinit var bookmarkUseCase: BookmarkUseCase

    @Inject
    lateinit var playerStateRepository: PlayerStateRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val ACTION_PLAY = "com.answersolutions.runandread.ACTION_PLAY"
        const val ACTION_PAUSE = "com.answersolutions.runandread.ACTION_PAUSE"
        const val ACTION_FF = "com.answersolutions.runandread.ACTION_FF"
        const val ACTION_FR = "com.answersolutions.runandread.ACTION_FR"
        const val ACTION_FAVORITE = "com.answersolutions.runandread.ACTION_FAVORITE"
        const val ACTION_SERVICE_STOP = "com.answersolutions.runandread.ACTION_SERVICE_STOP"
        const val NOTIFICATION_ID = 1974
        const val CHANNEL_ID = "audio_channel"
    }

    private lateinit var mediaSession: MediaSessionCompat

    private fun initMediaSession(context: Context) {
        mediaSession = MediaSessionCompat(context, "TTSPlaybackSession").apply {
            setCallback(mediaSessionCallback)
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PAUSE or
                                PlaybackStateCompat.ACTION_STOP or
                                PlaybackStateCompat.ACTION_FAST_FORWARD or
                                PlaybackStateCompat.ACTION_REWIND or
                                PlaybackStateCompat.ACTION_SEEK_TO
                    )
                    .setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        0,
                        1f
                    )
                    .build()
            )
            isActive = true
        }
    }


    override fun onCreate() {
        super.onCreate()
        initMediaSession(this)
        createNotificationChannel()

        // Observe state changes instead of callback
        serviceScope.launch {
            playerStateRepository.getPlaybackState().collect { state ->
                updatePlaybackState(state.position, state.duration, state.isPlaying)
            }
        }

        updateNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> mediaSessionCallback.onPlay()
            ACTION_PAUSE -> mediaSessionCallback.onPause()
            ACTION_FF -> mediaSessionCallback.onFastForward()
            ACTION_FR -> mediaSessionCallback.onRewind()
            ACTION_FAVORITE -> {
                mediaSessionCallback.onCommand(command = null, extras = null, cb = null)
            }

            ACTION_SERVICE_STOP -> stopSelf()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            Timber.d("PlayerService.onPlay()")
            serviceScope.launch {
                playerUseCase.play()
            }
        }

        override fun onPause() {
            Timber.d("PlayerService.onPause()")
            serviceScope.launch {
                playerUseCase.pause()
            }
        }

        override fun onFastForward() {
            Timber.d("PlayerService.onFastForward()")
            serviceScope.launch {
                playerUseCase.fastForward()
            }
        }

        override fun onRewind() {
            Timber.d("PlayerService.onRewind()")
            serviceScope.launch {
                playerUseCase.fastRewind()
            }
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
            Timber.d("onCommand()=>${command}")
            serviceScope.launch {
                bookmarkUseCase.saveBookmark()
            }
        }

    }

    private fun updatePlaybackState(position: Long, duration: Long, isPlaying: Boolean) {
        val state =
            if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
//        Timber.d("updatePlaybackState()=>$position of $duration")
        val durationMsc = duration * 1000
        val positionMsc = position * 1000
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, positionMsc, 1f) // Position updates
                .setBufferedPosition(durationMsc)
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_FAST_FORWARD or
                            PlaybackStateCompat.ACTION_REWIND or
                            PlaybackStateCompat.ACTION_SEEK_TO or
                            PlaybackStateCompat.ACTION_SET_RATING // Might be useful for favorite
                )
                .build()
        )

        mediaSession.setExtras(Bundle().apply {
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMsc)
        })
        // Get book info from repository instead of ViewModel
        serviceScope.launch {
            val book = playerStateRepository.getCurrentBook().first()

            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMsc)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, book?.title ?: "Unknown")
                    .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, book?.author ?: "Unknown")
                    .build()
            )
        }

        mediaSession.isActive = true // Ensure it's active!
    }

    private fun updateNotification() {
        serviceScope.launch {
            val book = playerStateRepository.getCurrentBook().first()
            book?.let {
                val playIntent = getServiceIntent(ACTION_PLAY, 0)
                val pauseIntent = getServiceIntent(ACTION_PAUSE, 1)
                val ffIntent = getServiceIntent(ACTION_FF, 2)
                val frIntent = getServiceIntent(ACTION_FR, 3)
//                val favoriteIntent = getServiceIntent(ACTION_FAVORITE, 4)

                val builder = NotificationCompat.Builder(this@PlayerService, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(it.title)
                    .setContentText(it.author)
                    .setUsesChronometer(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(true) // Keeps the notification visible
                    .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowActionsInCompactView(0, 1, 2, 3, 4) // Shows Play/Pause, FF, FR
                    )

                builder.addAction(R.drawable.ic_play, "Play", playIntent)
                builder.addAction(R.drawable.ic_pause, "Pause", pauseIntent)

//                builder.addAction(R.drawable.ic_bookmark, "Favorite", favoriteIntent)
                builder.addAction(R.drawable.ic_ff, "Fast Forward", ffIntent)
                builder.addAction(R.drawable.ic_fr, "Rewind", frIntent)

                startForeground(NOTIFICATION_ID, builder.build())
            }
        }
    }

    private fun getServiceIntent(action: String, requestCode: Int): PendingIntent {
        return PendingIntent.getService(
            this, requestCode, Intent(this, PlayerService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
