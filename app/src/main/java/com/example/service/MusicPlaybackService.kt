package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.Song

/**
 * Foreground Service that manages a professional playback notification
 * with full controls (Previous, Play/Pause, Next, Stop) in the status bar
 * and lock screen.
 */
class MusicPlaybackService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action != null) {
            when (action) {
                ACTION_PLAY_PAUSE -> {
                    onPlaybackActionListener?.invoke(PlaybackAction.PLAY_PAUSE)
                }
                ACTION_PREV -> {
                    onPlaybackActionListener?.invoke(PlaybackAction.PREV)
                }
                ACTION_NEXT -> {
                    onPlaybackActionListener?.invoke(PlaybackAction.NEXT)
                }
                ACTION_STOP -> {
                    onPlaybackActionListener?.invoke(PlaybackAction.STOP)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return START_NOT_STICKY
                }
                ACTION_UPDATE -> {
                    val title = intent.getStringExtra(EXTRA_TITLE) ?: "REMOMUSIC"
                    val artist = intent.getStringExtra(EXTRA_ARTIST) ?: "مشغل الموسيقى الاحترافي"
                    val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                    val artRes = intent.getIntExtra(EXTRA_ART_RES, R.drawable.ic_remo_brand)
                    val artUri = intent.getStringExtra(EXTRA_ART_URI)

                    val notification = buildNotification(title, artist, isPlaying, artRes, artUri)
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "مشغل الموسيقى REMOMUSIC",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "التحكم في تشغيل الموسيقى من شريط الإشعارات وقفل الشاشة"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        title: String,
        artist: String,
        isPlaying: Boolean,
        artRes: Int,
        artUri: String?
    ): Notification {
        // Main intent to return to MainActivity
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Play/Pause Action
        val playPauseIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Next Action
        val nextIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Previous Action
        val prevIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Stop Action
        val stopIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Large icon bitmap
        val largeIconBitmap = try {
            if (!artUri.isNullOrEmpty()) {
                val input = contentResolver.openInputStream(Uri.parse(artUri))
                BitmapFactory.decodeStream(input)
            } else {
                BitmapFactory.decodeResource(resources, artRes)
            }
        } catch (e: Exception) {
            BitmapFactory.decodeResource(resources, R.drawable.ic_remo_brand)
        }

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }

        val playPauseTitle = if (isPlaying) "إيقاف مؤقت" else "تشغيل"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText("REMOMUSIC Pro")
            .setSmallIcon(R.drawable.ic_remo_brand)
            .setLargeIcon(largeIconBitmap)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .addAction(android.R.drawable.ic_media_previous, "السابق", prevIntent)
            .addAction(playPauseIcon, playPauseTitle, playPauseIntent)
            .addAction(android.R.drawable.ic_media_next, "التالي", nextIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إغلاق", stopIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$artist • REMOMUSIC Studio Audio Engine")
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    enum class PlaybackAction {
        PLAY_PAUSE, PREV, NEXT, STOP
    }

    companion object {
        const val CHANNEL_ID = "remomusic_playback_channel"
        const val NOTIFICATION_ID = 2026

        const val ACTION_PLAY_PAUSE = "com.example.service.PLAY_PAUSE"
        const val ACTION_PREV = "com.example.service.PREV"
        const val ACTION_NEXT = "com.example.service.NEXT"
        const val ACTION_STOP = "com.example.service.STOP"
        const val ACTION_UPDATE = "com.example.service.UPDATE"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_ART_RES = "extra_art_res"
        const val EXTRA_ART_URI = "extra_art_uri"

        var onPlaybackActionListener: ((PlaybackAction) -> Unit)? = null

        fun startOrUpdate(
            context: Context,
            song: Song?,
            isPlaying: Boolean
        ) {
            if (song == null) return
            try {
                val intent = Intent(context, MusicPlaybackService::class.java).apply {
                    action = ACTION_UPDATE
                    putExtra(EXTRA_TITLE, song.title)
                    putExtra(EXTRA_ARTIST, song.artist)
                    putExtra(EXTRA_IS_PLAYING, isPlaying)
                    putExtra(EXTRA_ART_RES, song.albumArtRes ?: R.drawable.ic_remo_brand)
                    putExtra(EXTRA_ART_URI, song.albumArtUri)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Ignore service start exceptions on restricted backgrounds
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, MusicPlaybackService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
