package com.answersolutions.runandread.ui.player

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
import com.answersolutions.runandread.R
import timber.log.Timber


class PlayerService : Service() {

    companion object {
        const val ACTION_PLAY = "com.answersolutions.runandread.ACTION_PLAY"
        const val ACTION_PAUSE = "com.answersolutions.runandread.ACTION_PAUSE"
        const val ACTION_FF = "com.answersolutions.runandread.ACTION_FF"
        const val ACTION_FR = "com.answersolutions.runandread.ACTION_FR"
        const val ACTION_FAVORITE = "com.answersolutions.runandread.ACTION_FAVORITE"
        const val ACTION_SERVICE_STOP = "com.answersolutions.runandread.ACTION_SERVICE_STOP"
        const val NOTIFICATION_ID = 1974
        const val CHANNEL_ID = "audio_channel"
        var playerViewModel: PlayerViewModel? = null
    }

    private lateinit var mediaSession: MediaSessionCompat

//    private fun initMediaSession(context: Context) {
//        mediaSession = MediaSessionCompat(context, "TTSPlaybackSession").apply {
//            setFlags(
//                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
//                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
//            )
//            setPlaybackState(
//                PlaybackStateCompat.Builder()
//                    .setActions(
//                        PlaybackStateCompat.ACTION_PLAY or
//                                PlaybackStateCompat.ACTION_PAUSE or
//                                PlaybackStateCompat.ACTION_STOP or
//                                PlaybackStateCompat.ACTION_FAST_FORWARD or
//                                PlaybackStateCompat.ACTION_REWIND or
//                                PlaybackStateCompat.ACTION_SEEK_TO
//                    )
//                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
//                    .build()
//            )
//            setCallback(mediaSessionCallback)
//            isActive = true
//        }
//    }

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
                        playerViewModel?.currentTimeElapsed() ?: 0,
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

        playerViewModel?.playbackProgressCallBack = { position, duration, isPlaying ->
            updatePlaybackState(position = position, duration = duration, isPlaying)
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
            Timber.d("onPlay()")
            playerViewModel?.speak()
            updatePlaybackState(
                position = 0,
                duration = 0,
                isPlaying = true
            ) // Ensure state updates
//            updateNotification()
        }

        override fun onPause() {
            Timber.d("onPause()")
            playerViewModel?.stopSpeaking()
            updatePlaybackState(
                position = 0,
                duration = 0,
                isPlaying = false
            ) // Ensure state updates
//            updateNotification(false)
        }

        override fun onFastForward() {
            Timber.d("onFastForward()")
            playerViewModel?.fastForward()

        }

        override fun onRewind() {
            Timber.d("onRewind()")
            playerViewModel?.fastRewind()
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
            Timber.d("onCommand()=>${command}")
            playerViewModel?.saveBookmark()
        }

    }

    private fun updatePlaybackState(position: Long, duration: Long, isPlaying: Boolean) {
        val state =
            if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
//        Timber.d("updatePlaybackState: $position, $duration, $isPlaying")
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, position, 1f) // Position updates
                .setBufferedPosition(duration)
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_FAST_FORWARD or
                            PlaybackStateCompat.ACTION_REWIND or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
                .build()
        )

        mediaSession.setExtras(Bundle().apply {
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        })
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    playerViewModel?.book?.title ?: "Unknown"
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_AUTHOR,
                    playerViewModel?.book?.author ?: "Unknown"
                )
                .build()
        )

        mediaSession.isActive = true // Ensure it's active!
    }

    private fun updateNotification() {
        playerViewModel?.book?.let {
            val playIntent = getServiceIntent(ACTION_PLAY, 1)
            val pauseIntent = getServiceIntent(ACTION_PAUSE, 2)
            val ffIntent = getServiceIntent(ACTION_FF, 3)
            val frIntent = getServiceIntent(ACTION_FR, 4)
            val favoriteIntent = getServiceIntent(ACTION_FAVORITE, 5)

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
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
                        .setShowActionsInCompactView(1, 2, 3, 4) // Shows Play/Pause, FF, FR
                )


//                .apply {
//            if (isPlaying) {
            builder.addAction(R.drawable.ic_play, "Play", playIntent)
            builder.addAction(R.drawable.ic_pause, "Pause", pauseIntent)
//            } else {

//            }
//                }
//            builder.addAction(R.drawable.ic_bookmark, "Favorite", favoriteIntent)
            builder.addAction(R.drawable.ic_ff, "Fast Forward", ffIntent)
            builder.addAction(R.drawable.ic_fr, "Rewind", frIntent)


//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.cancelAll()//.cancel(NOTIFICATION_ID)
//            notificationManager.notify(NOTIFICATION_ID, notification)

//            startForeground(NOTIFICATION_ID, builder.build())

//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//            if (isPlaying) {
            startForeground(NOTIFICATION_ID, builder.build())
//            } else {
//                stopForeground(false)
//                notificationManager.notify(NOTIFICATION_ID, builder.build())
//            }
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
