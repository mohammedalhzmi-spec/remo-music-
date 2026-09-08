package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.db.LocalTrackEntity
import com.example.model.LyricsLine
import com.example.model.Mood
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository for discovering, managing, and caching local audio files stored on the device
 * using Room Database for 100% offline persistence and instant startup.
 */
class LocalMusicRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val localTrackDao = database.localTrackDao()

    /**
     * Flow of all locally discovered tracks persisted in the Room database.
     * Works 100% offline without needing to scan MediaStore on every app launch.
     */
    val cachedTracksFlow: Flow<List<Song>> = localTrackDao.getAllTracks().map { entities ->
        entities.map { it.toSong() }
    }

    /**
     * Scans local device audio files via MediaStore, stores them in Room DB,
     * and returns the persisted list of songs.
     */
    suspend fun scanAndSyncLocalTracks(): List<Song> = withContext(Dispatchers.IO) {
        val discoveredTracks = queryMediaStore()
        if (discoveredTracks.isNotEmpty()) {
            localTrackDao.insertAll(discoveredTracks)
        }
        // Retrieve all tracks currently stored in Room
        val saved = mutableListOf<Song>()
        try {
            // We return the newly synced tracks
            discoveredTracks.map { it.toSong() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Updates track metadata in Room Database
     */
    suspend fun updateTrackMetadata(
        id: String,
        title: String,
        artist: String,
        album: String,
        genre: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        localTrackDao.updateMetadata(id, title, artist, album, genre, notes)
    }

    /**
     * Updates custom album cover URI in Room Database
     */
    suspend fun updateTrackCover(id: String, coverUri: String) = withContext(Dispatchers.IO) {
        localTrackDao.updateCover(id, coverUri)
    }

    /**
     * Toggles favorite status in Room Database
     */
    suspend fun setTrackFavorite(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        localTrackDao.updateFavorite(id, isFavorite)
    }

    /**
     * Search locally stored tracks in Room Database offline
     */
    fun searchTracks(query: String): Flow<List<Song>> {
        return localTrackDao.searchTracks(query).map { entities ->
            entities.map { it.toSong() }
        }
    }

    private fun resolveStorageType(path: String): String {
        val lower = path.lowercase()
        return when {
            lower.contains("/emulated/0") || lower.contains("/sdcard/0") -> "ذاكرة الهاتف الداخلية"
            lower.contains("/storage/") && !lower.contains("emulated") -> "بطاقة SD الخارجية"
            lower.contains("sdcard") || lower.contains("extsd") -> "بطاقة SD"
            lower.contains("usb") || lower.contains("otg") -> "تخزين خارجي (USB)"
            else -> "ذاكرة الهاتف الداخلية"
        }
    }

    private fun queryMediaStore(): List<LocalTrackEntity> {
        val tracks = mutableListOf<LocalTrackEntity>()
        val seenIds = mutableSetOf<String>()

        val queryUris = mutableListOf<Uri>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val volumeNames = MediaStore.getExternalVolumeNames(context)
                for (vol in volumeNames) {
                    queryUris.add(MediaStore.Audio.Media.getContentUri(vol))
                }
            } catch (e: Exception) {
                queryUris.add(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL))
            }
        } else {
            queryUris.add(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        }
        try {
            queryUris.add(MediaStore.Audio.Media.INTERNAL_CONTENT_URI)
        } catch (e: Exception) {
            // ignore
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        for (uri in queryUris.distinct()) {
            try {
                context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    null,
                    sortOrder
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val uniqueTrackId = "local_${uri.lastPathSegment ?: "ext"}_$id"
                        if (!seenIds.add(uniqueTrackId)) continue

                        val title = cursor.getString(titleCol) ?: "مقطع بدون اسم"
                        val artist = cursor.getString(artistCol) ?: "فنان غير معروف"
                        val album = cursor.getString(albumCol) ?: "ألبوم غير معروف"
                        val duration = cursor.getLong(durationCol)
                        val filePath = if (dataCol != -1) cursor.getString(dataCol) else ""

                        val contentUri: Uri = ContentUris.withAppendedId(uri, id)

                        val parentDir = if (!filePath.isNullOrEmpty()) {
                            try { File(filePath).parentFile } catch (e: Exception) { null }
                        } else null

                        val folderName = parentDir?.name ?: "Music"
                        val folderPath = parentDir?.absolutePath ?: "/storage/emulated/0/$folderName"
                        val storageType = resolveStorageType(folderPath)

                        val mood = guessMood(title, artist, album)

                        if (duration > 10_000L) { // Filter out short notification sounds
                            tracks.add(
                                LocalTrackEntity(
                                    id = uniqueTrackId,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    durationMs = duration,
                                    mediaUri = contentUri.toString(),
                                    albumArtUri = null,
                                    genre = "Local Audio",
                                    mood = mood.name,
                                    folder = folderName,
                                    folderPath = folderPath,
                                    storageType = storageType,
                                    isFavorite = false,
                                    userNotes = "",
                                    discoveredAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("LocalMusicRepository", "Could not query MediaStore URI: $uri", e)
            }
        }
        return tracks
    }

    private fun guessMood(title: String, artist: String, album: String): Mood {
        val text = "$title $artist $album".lowercase()
        return when {
            text.contains("chill") || text.contains("lofi") || text.contains("rain") || text.contains("relax") || text.contains("هادئ") -> Mood.CHILL
            text.contains("gym") || text.contains("workout") || text.contains("run") || text.contains("تمرين") || text.contains("رياضة") -> Mood.WORKOUT
            text.contains("focus") || text.contains("study") || text.contains("zen") || text.contains("تركيز") || text.contains("دراسة") -> Mood.FOCUS
            text.contains("love") || text.contains("heart") || text.contains("حب") || text.contains("غرام") || text.contains("رومانسي") -> Mood.ROMANTIC
            text.contains("sad") || text.contains("tears") || text.contains("cry") || text.contains("شجن") || text.contains("حزن") -> Mood.MELANCHOLY
            else -> Mood.ENERGETIC
        }
    }

    private fun LocalTrackEntity.toSong(): Song {
        val resolvedMood = try {
            Mood.valueOf(this.mood)
        } catch (e: Exception) {
            Mood.ENERGETIC
        }

        return Song(
            id = this.id,
            title = this.title,
            artist = this.artist,
            album = this.album,
            durationMs = this.durationMs,
            mediaUri = this.mediaUri,
            albumArtRes = R.drawable.ic_remo_brand,
            albumArtUri = this.albumArtUri,
            genre = this.genre,
            mood = resolvedMood,
            folder = this.folder,
            folderPath = this.folderPath,
            storageType = this.storageType,
            isFavorite = this.isFavorite,
            userNotes = this.userNotes,
            isCloud = false,
            lyrics = listOf(
                LyricsLine(0L, this.title, this.title),
                LyricsLine(12_000L, "مقطع محلي مخزن في ${this.storageType}", "مقطع محلي مخزن في ${this.storageType}"),
                LyricsLine(30_000L, "تشغيل أوفلاين فائق الجودة بتقنية REMOMUSIC", "تشغيل أوفلاين فائق الجودة بتقنية REMOMUSIC")
            )
        )
    }
}
