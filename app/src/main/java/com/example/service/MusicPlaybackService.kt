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
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.MainActivity
import com.example.R
import com.example.model.Song

/**
 * Foreground Service that manages a professional playback notification
 * with full controls (Previous, Play/Pause, Next, Stop) and seekbar support
 * in the status bar and lock screen using Android MediaStyle and MediaSessionCompat.
 */
class MusicPlaybackService : Service() {

    private var mediaSession: MediaSessionCompat? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        initMediaSession()
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "RemoMusicSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    onPlaybackActionListener?.invoke(PlaybackAction.PLAY_PAUSE)
                }

                override fun onPause() {
                    onPlaybackActionListener?.invoke(PlaybackAction.PLAY_PAUSE)
                }

                override fun onSkipToNext() {
                    onPlaybackActionListener?.invoke(PlaybackAction.NEXT)
                }

                override fun onSkipToPrevious() {
                    onPlaybackActionListener?.invoke(PlaybackAction.PREV)
                }

                override fun onStop() {
                    onPlaybackActionListener?.invoke(PlaybackAction.STOP)
                }

                override fun onSeekTo(pos: Long) {
                    onSeekActionListener?.invoke(pos)
                }
            })
            isActive = true
        }
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
                ACTION_SEEK -> {
                    val pos = intent.getLongExtra(EXTRA_SEEK_POSITION, 0L)
                    onSeekActionListener?.invoke(pos)
                }
                ACTION_UPDATE -> {
                    val title = intent.getStringExtra(EXTRA_TITLE) ?: "ريمو مشغل الموسيقى"
                    val artist = intent.getStringExtra(EXTRA_ARTIST) ?: "محمد الحزمي"
                    val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                    val artRes = intent.getIntExtra(EXTRA_ART_RES, R.drawable.ic_remo_brand)
                    val artUri = intent.getStringExtra(EXTRA_ART_URI)
                    val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 0L)
                    val positionMs = intent.getLongExtra(EXTRA_POSITION_MS, 0L)

                    updateMediaSessionState(title, artist, isPlaying, durationMs, positionMs, artRes, artUri)
                    val notification = buildNotification(title, artist, isPlaying, durationMs, positionMs, artRes, artUri)
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun loadAlbumArtBitmap(artUri: String?, artRes: Int): Bitmap {
        if (!artUri.isNullOrBlank()) {
            try {
                if (artUri.startsWith("content://") || artUri.startsWith("android.resource://") || artUri.startsWith("file://")) {
                    contentResolver.openInputStream(Uri.parse(artUri))?.use { input ->
                        val bmp = BitmapFactory.decodeStream(input)
                        if (bmp != null) return bmp
                    }
                } else {
                    val bmp = BitmapFactory.decodeFile(artUri)
                    if (bmp != null) return bmp
                }
            } catch (e: Exception) {
                // Ignore and fall through to resource
            }
        }
        return try {
            val resId = if (artRes != 0) artRes else R.drawable.ic_remo_brand
            BitmapFactory.decodeResource(resources, resId) ?: BitmapFactory.decodeResource(resources, R.drawable.ic_remo_brand)
        } catch (e: Exception) {
            val fallback = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(fallback)
            canvas.drawColor(android.graphics.Color.parseColor("#090A10"))
            fallback
        }
    }

    private fun updateMediaSessionState(
        title: String,
        artist: String,
        isPlaying: Boolean,
        durationMs: Long,
        positionMs: Long,
        artRes: Int,
        artUri: String?
    ) {
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                positionMs,
                if (isPlaying) 1.0f else 0.0f,
                SystemClock.elapsedRealtime()
            )
            .build()

        mediaSession?.setPlaybackState(playbackState)

        val albumArtBitmap = loadAlbumArtBitmap(artUri, artRes)

        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "ريمو مشغل الموسيقى الاحترافي")
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, if (durationMs > 0) durationMs else -1L)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, albumArtBitmap)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, albumArtBitmap)

        mediaSession?.setMetadata(metadataBuilder.build())
    }

    fun updatePositionOnly(positionMs: Long, durationMs: Long, isPlaying: Boolean) {
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                positionMs,
                if (isPlaying) 1.0f else 0.0f,
                SystemClock.elapsedRealtime()
            )
            .build()

        mediaSession?.setPlaybackState(playbackState)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ريمو مشغل الموسيقى الاحترافي",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "التحكم في تشغيل الموسيقى وسحب شريط الوقت من شريط الإشعارات وقفل الشاشة"
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
        durationMs: Long,
        positionMs: Long,
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

        // Previous Action
        val prevIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Play/Pause Action
        val playPauseIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Next Action
        val nextIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Stop Action
        val stopIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Large icon bitmap (music cover image)
        val largeIconBitmap = loadAlbumArtBitmap(artUri, artRes)

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }

        val playPauseTitle = if (isPlaying) "إيقاف مؤقت" else "تشغيل"

        val mediaStyle = MediaStyle()
            .setShowActionsInCompactView(0, 1, 2)
        mediaSession?.sessionToken?.let { token ->
            mediaStyle.setMediaSession(token)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText("ريمو مشغل الموسيقى الاحترافي")
            .setSmallIcon(R.drawable.ic_remo_brand)
            .setLargeIcon(largeIconBitmap)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .addAction(android.R.drawable.ic_media_previous, "السابق", prevIntent)
            .addAction(playPauseIcon, playPauseTitle, playPauseIntent)
            .addAction(android.R.drawable.ic_media_next, "التالي", nextIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف", stopIntent)
            .setStyle(mediaStyle)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (durationMs > 0) {
            builder.setProgress(durationMs.toInt(), positionMs.toInt(), false)
        }

        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        mediaSession?.release()
        mediaSession = null
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
        const val ACTION_SEEK = "com.example.service.SEEK"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_ART_RES = "extra_art_res"
        const val EXTRA_ART_URI = "extra_art_uri"
        const val EXTRA_DURATION_MS = "extra_duration_ms"
        const val EXTRA_POSITION_MS = "extra_position_ms"
        const val EXTRA_SEEK_POSITION = "extra_seek_position"

        var onPlaybackActionListener: ((PlaybackAction) -> Unit)? = null
        var onSeekActionListener: ((Long) -> Unit)? = null

        private var instance: MusicPlaybackService? = null

        fun startOrUpdate(
            context: Context,
            song: Song?,
            isPlaying: Boolean,
            durationMs: Long = 0L,
            positionMs: Long = 0L
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
                    putExtra(EXTRA_DURATION_MS, durationMs)
                    putExtra(EXTRA_POSITION_MS, positionMs)
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

        fun updateProgress(positionMs: Long, durationMs: Long, isPlaying: Boolean) {
            instance?.updatePositionOnly(positionMs, durationMs, isPlaying)
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
