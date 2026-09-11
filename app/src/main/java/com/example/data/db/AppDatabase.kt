package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_items", primaryKeys = ["playlistId", "songId"])
data class PlaylistItemEntity(
    val playlistId: Long,
    val songId: String,
    val orderIndex: Int = 0
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: String,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "eq_settings")
data class EqSettingEntity(
    @PrimaryKey val id: String = "current",
    val presetId: String = "auto",
    val band0: Int = 0,
    val band1: Int = 0,
    val band2: Int = 0,
    val band3: Int = 0,
    val band4: Int = 0,
    val bassBoost: Int = 30,
    val virtualizer: Int = 25,
    val isAutoAdaptive: Boolean = true
)

@Entity(tableName = "local_tracks")
data class LocalTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String,
    val albumArtUri: String? = null,
    val genre: String = "Local Audio",
    val mood: String = "ENERGETIC",
    val folder: String = "Music",
    val folderPath: String = "",
    val storageType: String = "ذاكرة الهاتف",
    val isFavorite: Boolean = false,
    val userNotes: String = "",
    val discoveredAt: Long = System.currentTimeMillis()
)

@Dao
interface LocalTrackDao {
    @Query("SELECT * FROM local_tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<LocalTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<LocalTrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: LocalTrackEntity)

    @Query("DELETE FROM local_tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)

    @Query("DELETE FROM local_tracks")
    suspend fun clearAll()

    @Query("UPDATE local_tracks SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: String, isFav: Boolean)

    @Query("UPDATE local_tracks SET title = :title, artist = :artist, album = :album, genre = :genre, userNotes = :notes WHERE id = :id")
    suspend fun updateMetadata(id: String, title: String, artist: String, album: String, genre: String, notes: String)

    @Query("UPDATE local_tracks SET albumArtUri = :coverUri WHERE id = :id")
    suspend fun updateCover(id: String, coverUri: String)

    @Query("SELECT * FROM local_tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchTracks(query: String): Flow<List<LocalTrackEntity>>
}

@Dao
interface MusicDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("SELECT songId FROM playlist_items WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getSongIdsForPlaylist(playlistId: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)

    @Query("SELECT songId FROM favorites ORDER BY addedAt DESC")
    fun getFavoriteSongIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: String)

    @Query("SELECT songId FROM history ORDER BY playedAt DESC LIMIT 50")
    fun getRecentHistorySongIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistory(history: HistoryEntity)

    @Query("SELECT * FROM eq_settings WHERE id = 'current' LIMIT 1")
    fun getEqSetting(): Flow<EqSettingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEqSetting(eq: EqSettingEntity)
}

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        FavoriteEntity::class,
        HistoryEntity::class,
        EqSettingEntity::class,
        LocalTrackEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
    abstract fun localTrackDao(): LocalTrackDao
}
