package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.db.FavoriteEntity
import com.example.data.db.HistoryEntity
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistItemEntity
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Folder
import com.example.model.LyricsLine
import com.example.model.Mood
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val dao = database.musicDao()

    // Bundled Mastered Tracks with lyrics and instant translation
    private val defaultCatalog = listOf(
        Song(
            id = "synth_01",
            title = "Cyber Horizon (أفق السايبر)",
            artist = "Neon Dreamer",
            album = "Midnight Drive 2088",
            durationMs = 214_000L,
            mediaUri = "synth://cyber_horizon",
            albumArtRes = R.drawable.art_neon_nights_1788818275997,
            genre = "Synthwave",
            mood = Mood.ENERGETIC,
            folder = "Electronic",
            isCloud = false,
            lyrics = listOf(
                LyricsLine(0L, "Night city lights reflecting in the rain", "أضواء المدينة الليلية تنعكس في حبات المطر"),
                LyricsLine(12_000L, "Speeding down the highway, breaking every chain", "ننطلق بسرعة على الطريق السريع، نحطم كل القيود"),
                LyricsLine(26_000L, "Neon pulse through my veins, feeling alive", "نبضات النيون تسري في عروقي، أشعر بالحياة من جديد"),
                LyricsLine(40_000L, "Nothing can stop the rhythm of this drive", "لا شيء يمكن أن يوقف إيقاع هذه الرحلة الجريئة"),
                LyricsLine(58_000L, "Electric sky, painted in violet and blue", "سماء كهربائية متلألئة، مطلية بالبنفسجي والأزرق"),
                LyricsLine(75_000L, "Lost in the sound, heading towards the new", "تائه في سحر الصوت، متجهاً نحو عالم جديد"),
                LyricsLine(95_000L, "Synthesizer waves carrying our dreams away", "أمواج السنثسيزر الرنانة تحمل أحلامنا بعيداً"),
                LyricsLine(120_000L, "Until the golden dawn washes the shadows away", "حتى يمحو الفجر الذهبي كل الظلال المتراكمة"),
                LyricsLine(150_000L, "Feel the bass resonance rising inside", "اشعر بصدى الباس وهو يتصاعد عميقاً بداخلك"),
                LyricsLine(180_000L, "In REMOMUSIC we forever reside", "في عالم ريمو ميوزك، نعيش للأبد في أمان")
            )
        ),
        Song(
            id = "synth_02",
            title = "Desert Mirage (سراب الصحراء)",
            artist = "Oasis Ensemble",
            album = "Arabesque Fusion",
            durationMs = 195_000L,
            mediaUri = "synth://desert_mirage",
            albumArtRes = R.drawable.art_oriental_echoes_1788818287549,
            genre = "Oriental",
            mood = Mood.ROMANTIC,
            folder = "Oriental & World",
            isCloud = false,
            lyrics = listOf(
                LyricsLine(0L, "Whispering sands under the midnight crescent", "رمال تهمس بأسرارها تحت هلال منتصف الليل"),
                LyricsLine(15_000L, "Echoes of oud in the warm desert wind", "أصداء العود تتهادى مع نسيم الصحراء الدافئ"),
                LyricsLine(32_000L, "A timeless melody that knows no end", "لحن خالد يتجاوز حدود الزمان والمكان"),
                LyricsLine(50_000L, "Stars guide our steps across the golden dunes", "النجوم ترشد خطواتنا عبر الكثبان الذهبية الساحرة"),
                LyricsLine(70_000L, "Dancing softly to mystical oriental tunes", "نرقص بهدوء على أنغام شرقية صوفية تأسر الوجدان"),
                LyricsLine(95_000L, "Sweet cardamom air and burning oud wood", "عبير الهيل الزكي ونفحات خشب العود المعتق"),
                LyricsLine(125_000L, "Moments of serenity deeply understood", "لحظات من السكينة التامة تغمر الروح والبال"),
                LyricsLine(160_000L, "Peace flows like water in the oasis spring", "السلام يتدفق كينبوع رقراق في قلب الواحة")
            )
        ),
        Song(
            id = "synth_03",
            title = "Rainy Café Lo-Fi (مقهى المطر)",
            artist = "Cozy Cloud",
            album = "Midnight Coffee Beats",
            durationMs = 172_000L,
            mediaUri = "synth://rainy_cafe",
            albumArtRes = R.drawable.art_chill_beats_1788818302063,
            genre = "Lofi",
            mood = Mood.CHILL,
            folder = "Chill & Study",
            isCloud = false,
            lyrics = listOf(
                LyricsLine(0L, "Gentle rain tapping on the window glass", "قطرات المطر الرقيقة تنقر على زجاج النافذة"),
                LyricsLine(14_000L, "Hot steam rising from the ceramic mug", "بخار ساخن يتصاعد من فنجان القهوة الخزفي"),
                LyricsLine(28_000L, "Pages turning softly, calm and relaxed", "صفحات كتاب تقلب بهدوء، راحة واسترخاء تام"),
                LyricsLine(45_000L, "Watching raindrops slip and fall as moments pass", "نراقب حبات المطر وهي تنزلق مع مرور اللحظات"),
                LyricsLine(65_000L, "Muffled jazz chords warm the chilly room", "نغمات جاز دافئة تبدد برودة المكان"),
                LyricsLine(90_000L, "No rush today, just peace in the afternoon", "لا داعي للاستعجال اليوم، مجرد هدوء وراحة بال"),
                LyricsLine(120_000L, "Low fidelity vinyl crackle softly speaks", "صوت خشخشة الفينيل الهادئ يداعب المسامع"),
                LyricsLine(150_000L, "The quiet meditation everyone seeks", "هذا هو الصفاء الذهني الذي يبحث عنه الجميع")
            )
        ),
        Song(
            id = "synth_04",
            title = "Infinite Pulse (نبض لا نهائي)",
            artist = "Aura Sonic",
            album = "Future Atmosphere",
            durationMs = 240_000L,
            mediaUri = "synth://infinite_pulse",
            albumArtRes = R.drawable.art_neon_nights_1788818275997,
            genre = "Electronic",
            mood = Mood.WORKOUT,
            folder = "Electronic Studio",
            isCloud = false,
            lyrics = listOf(
                LyricsLine(0L, "Push past your limits, feel the acceleration", "تجاوز حدودك القديمة، واشعر بسرعة الانطلاق"),
                LyricsLine(20_000L, "Every heartbeat fuels pure determination", "كل نبضة قلب تغذي العزيمة والإصرار بداخلك"),
                LyricsLine(45_000L, "Run faster, breathe deeper, climb the peak", "اركض أسرع، تنفس بعمق، واصعد نحو القمة"),
                LyricsLine(70_000L, "It is the triumph of the will you seek", "إنه انتصار الإرادة القوية الذي تبحث عنه"),
                LyricsLine(100_000L, "No gravity holding us down anymore", "لم تعد هناك جاذبية تقيد حركتنا بعد الآن"),
                LyricsLine(140_000L, "Break every ceiling and unlock the door", "حطم كل الحواجز وافتح الأبواب للمستقبل")
            )
        ),
        Song(
            id = "synth_05",
            title = "Deep Space Zen (تأمل الفضاء)",
            artist = "Cosmic Mind",
            album = "Starlight Focus",
            durationMs = 260_000L,
            mediaUri = "synth://deep_space_zen",
            albumArtRes = R.drawable.art_oriental_echoes_1788818287549,
            genre = "Ambient",
            mood = Mood.FOCUS,
            folder = "Ambient Masters",
            isCloud = false,
            lyrics = listOf(
                LyricsLine(0L, "Deep breath in, clear every wandering thought", "تنفس بعمق، ونقِ ذهنك من كل الأفكار المشتتة"),
                LyricsLine(25_000L, "Focus your mind on the masterpiece you sought", "ركز كل طاقتك على الإنجاز العظيم الذي تصبو إليه"),
                LyricsLine(55_000L, "Harmony of frequencies in perfect tone", "تناغم الترددات بنقاء صوتي مبهر ومريح"),
                LyricsLine(90_000L, "You are in the zone, entirely your own", "أنت الآن في قمة التركيز والتألق الإبداعي")
            )
        ),
        Song(
            id = "synth_06",
            title = "Autumn Melancholy (شجن الخريف)",
            artist = "Elena Rostova",
            album = "Piano Solitude",
            durationMs = 210_000L,
            mediaUri = "synth://autumn_piano",
            albumArtRes = R.drawable.art_chill_beats_1788818302063,
            genre = "Classical",
            mood = Mood.MELANCHOLY,
            folder = "Acoustic",
            isCloud = false,
            lyrics = listOf(
                LyricsLine(0L, "Golden leaves drifting slowly on cold stone", "أوراق شجر ذهبية تتساقط ببطء على الحجر البارد"),
                LyricsLine(18_000L, "A solitary melody played all alone", "لحن هادئ ومنفرد يُعزف في صمت الليل"),
                LyricsLine(40_000L, "Bittersweet memories written in the rain", "ذكريات حلوة ومرة كُتبت بحروف قطرات المطر"),
                LyricsLine(70_000L, "Healing the heart from every hidden pain", "تداوي القلب الجريح من كل ألم خفي"),
                LyricsLine(110_000L, "Beauty found in sorrow, deep and true", "جمال نقي يُولد من رحم الشجن الصادق"),
                LyricsLine(150_000L, "Music that whispers: tomorrow is new", "موسيقى تهمس في أذنك: غداً يوم أمل جديد")
            )
        )
    )

    val localRepository = LocalMusicRepository(context, database)

    suspend fun getAllSongs(): List<Song> = withContext(Dispatchers.IO) {
        // Sync local songs into Room DB
        val localSongs = try {
            val synced = localRepository.scanAndSyncLocalTracks()
            if (synced.isEmpty()) {
                localRepository.cachedTracksFlow.first()
            } else synced
        } catch (e: Exception) {
            localRepository.cachedTracksFlow.first()
        }

        val favorites = dao.getFavoriteSongIds().first().toSet()

        val combined = (defaultCatalog + localSongs).distinctBy { it.id }.map { song ->
            song.copy(isFavorite = favorites.contains(song.id))
        }
        combined
    }

    private fun queryLocalDeviceSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            val queryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            context.contentResolver.query(
                queryUri,
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
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "مقطع بدون اسم"
                    val artist = cursor.getString(artistCol) ?: "فنان غير معروف"
                    val album = cursor.getString(albumCol) ?: "ألبوم غير معروف"
                    val duration = cursor.getLong(durationCol)
                    val filePath = if (dataCol != -1) cursor.getString(dataCol) else ""
                    val albumId = if (albumIdCol != -1) cursor.getLong(albumIdCol) else -1L
                    val albumArtUri = if (albumId != -1L) {
                        ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId).toString()
                    } else null

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val parentDir = if (!filePath.isNullOrEmpty()) {
                        try { File(filePath).parentFile } catch (e: Exception) { null }
                    } else null

                    val folderName = parentDir?.name ?: "Music"
                    val folderPath = parentDir?.absolutePath ?: "/storage/emulated/0/$folderName"
                    val storageType = when {
                        folderPath.contains("/emulated/0") || folderPath.contains("/sdcard/0") -> "ذاكرة الهاتف الداخلية"
                        folderPath.contains("/storage/") && !folderPath.contains("emulated") -> "بطاقة SD الخارجية"
                        folderPath.contains("sdcard") || folderPath.contains("extsd") -> "بطاقة SD"
                        folderPath.contains("usb") || folderPath.contains("otg") -> "تخزين خارجي (USB)"
                        else -> "ذاكرة الهاتف الداخلية"
                    }

                    // Auto-assign mood based on title / artist heuristics
                    val mood = guessMoodFromMetadata(title, artist, album)

                    if (duration > 10_000L) { // Filter out short ringtones/notifications
                        songs.add(
                            Song(
                                id = "local_$id",
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = duration,
                                mediaUri = contentUri.toString(),
                                albumArtRes = R.drawable.ic_remo_brand,
                                albumArtUri = albumArtUri,
                                genre = "Local Audio",
                                mood = mood,
                                folder = folderName,
                                folderPath = folderPath,
                                storageType = storageType,
                                isCloud = false,
                                lyrics = listOf(
                                    LyricsLine(0L, title, title),
                                    LyricsLine(15_000L, "Enjoying music on REMOMUSIC", "استمتع بالموسيقى عبر مشغل ريمو ميوزك"),
                                    LyricsLine(45_000L, "High fidelity acoustic reproduction", "أداء صوتي فائق النقاء والجودة")
                                )
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("MusicRepository", "Could not query device audio files (permissions or empty)", e)
        }
        return songs
    }

    private fun guessMoodFromMetadata(title: String, artist: String, album: String): Mood {
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

    // Room DB wrappers
    fun getPlaylists(): Flow<List<PlaylistEntity>> = dao.getAllPlaylists()

    suspend fun createPlaylist(name: String, description: String = ""): Long {
        return dao.insertPlaylist(PlaylistEntity(name = name, description = description))
    }

    suspend fun deletePlaylist(id: Long) = dao.deletePlaylist(id)

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        dao.insertPlaylistItem(PlaylistItemEntity(playlistId = playlistId, songId = songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        dao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getPlaylistSongIds(playlistId: Long): Flow<List<String>> = dao.getSongIdsForPlaylist(playlistId)

    suspend fun toggleFavorite(songId: String, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            dao.removeFavorite(songId)
        } else {
            dao.addFavorite(FavoriteEntity(songId = songId))
        }
    }

    suspend fun logHistory(songId: String) {
        dao.addHistory(HistoryEntity(songId = songId))
    }

    suspend fun updateSongNotesAndMetadata(id: String, title: String, artist: String, album: String, genre: String, notes: String) {
        localRepository.updateTrackMetadata(id, title, artist, album, genre, notes)
    }

    suspend fun updateSongCover(id: String, coverUri: String) {
        localRepository.updateTrackCover(id, coverUri)
    }
}
