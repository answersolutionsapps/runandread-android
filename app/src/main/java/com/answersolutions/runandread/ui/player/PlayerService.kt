package com.answersolutions.runandread.ui.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.answersolutions.runandread.R


class PlayerService : Service() {

    companion object {
        const val ACTION_PLAY = "com.answersolutions.runandread.ACTION_PLAY"
        const val ACTION_PAUSE = "com.answersolutions.runandread.ACTION_PAUSE"
        const val ACTION_FF = "com.answersolutions.runandread.ACTION_FF"
        const val ACTION_FR = "com.answersolutions.runandread.ACTION_FR"
        const val ACTION_SERVICE_STOP = "com.answersolutions.runandread.ACTION_SERVICE_STOP"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "audio_channel"
        var playerViewModel: PlayerViewModel? = null
    }

    private lateinit var mediaSession: MediaSessionCompat


    override fun onCreate() {
        super.onCreate()

//        playerViewModel = ViewModelProvider(
//            application as ViewModelStoreOwner, viewModelFactory
//        )[PlayerViewModel::class.java]

        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(mediaSessionCallback)
            isActive = true
        }

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> mediaSessionCallback.onPlay()
            ACTION_PAUSE -> mediaSessionCallback.onPause()
            ACTION_FF -> mediaSessionCallback.onFastForward()
            ACTION_FR -> mediaSessionCallback.onRewind()
            ACTION_SERVICE_STOP -> stopSelf()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            playerViewModel?.speak()
            updateNotification(true)
        }

        override fun onPause() {
            playerViewModel?.stopSpeaking()
            updateNotification(false)
        }

        override fun onFastForward() {
//            super.onFastForward()
            playerViewModel?.fastForward()

        }

        override fun onRewind() {
//            super.onRewind()
            playerViewModel?.fastRewind()
        }

    }

    private fun updateNotification(isPlaying: Boolean) {

        playerViewModel?.selectedBook?.let {
            val playIntent = PendingIntent.getService(
                this, 0, Intent(this, PlayerService::class.java).setAction(ACTION_PLAY),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val pauseIntent = PendingIntent.getService(
                this, 0, Intent(this, PlayerService::class.java).setAction(ACTION_PAUSE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val ffIntent = PendingIntent.getService(
                this, 0, Intent(this, PlayerService::class.java).setAction(ACTION_FF),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val frIntent = PendingIntent.getService(
                this, 0, Intent(this, PlayerService::class.java).setAction(ACTION_FR),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setLargeIcon(Icon.createWithResource(this, R.drawable.ic_launcher_foreground))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(it.title)
                .setContentText(it.author)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                )
                .apply {
                    addAction(R.drawable.ic_fr, "Fast Rewind", frIntent)
                    addAction(R.drawable.ic_ff, "Fast Forward", ffIntent)

                    if (isPlaying) {
                        addAction(R.drawable.ic_pause, "Pause", pauseIntent)
                    } else {
                        addAction(R.drawable.ic_play, "Play", playIntent)
                    }
                }
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }
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
