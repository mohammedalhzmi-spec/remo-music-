package com.example.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.audio.AudioEngine
import com.example.ai.MusicAiAssistant
import com.example.data.MusicRepository
import com.example.data.db.AppDatabase
import com.example.data.db.PlaylistEntity
import com.example.model.Album
import com.example.model.Artist
import com.example.model.EqualizerPreset
import com.example.model.Folder
import com.example.model.LyricsLine
import com.example.model.Mood
import com.example.model.PlaybackMode
import com.example.model.Song
import com.example.voice.VoiceAction
import com.example.voice.VoiceCommandHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab(val arabicName: String, val englishName: String) {
    SONGS("الأغاني", "Songs"),
    ARTISTS("الفنانون", "Artists"),
    ALBUMS("الألبومات", "Albums"),
    FOLDERS("المجلدات", "Folders"),
    PLAYLISTS("القوائم", "Playlists"),
    CLOUD("السحابة", "Cloud")
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "remomusic_database.db"
    ).fallbackToDestructiveMigration().build()

    val repository = MusicRepository(application, db)
    val audioEngine = AudioEngine(application)
    val aiAssistant = MusicAiAssistant()
    val voiceHandler = VoiceCommandHandler(application)

    // Navigation and Modals
    private val _currentTab = MutableStateFlow(MainTab.SONGS)
    val currentTab = _currentTab.asStateFlow()

    private val _selectedMood = MutableStateFlow(Mood.ALL)
    val selectedMood = _selectedMood.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Library Data
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs = _allSongs.asStateFlow()

    val playlists: StateFlow<List<PlaylistEntity>> = repository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Songs by mood and search query
    val filteredSongs: StateFlow<List<Song>> = combine(_allSongs, _selectedMood, _searchQuery) { songs, mood, query ->
        var list = if (mood == Mood.ALL) songs else songs.filter { it.mood == mood }
        if (query.isNotBlank()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true) ||
                it.genre.contains(query, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Artists, Albums, Folders
    val artists: StateFlow<List<Artist>> = _allSongs.combine(_allSongs) { songs, _ ->
        songs.groupBy { it.artist }.map { (artistName, songList) ->
            Artist(
                name = artistName,
                songCount = songList.size,
                albumCount = songList.map { it.album }.distinct().size,
                albumArtRes = songList.firstOrNull()?.albumArtRes
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = _allSongs.combine(_allSongs) { songs, _ ->
        songs.groupBy { it.album }.map { (albumName, songList) ->
            Album(
                name = albumName,
                artist = songList.firstOrNull()?.artist ?: "",
                songCount = songList.size,
                albumArtRes = songList.firstOrNull()?.albumArtRes
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<Folder>> = _allSongs.combine(_allSongs) { songs, _ ->
        // Group strictly by unique directory path to ensure separate listings for same-named folders across storages
        songs.groupBy { song ->
            if (song.folderPath.isNotBlank()) song.folderPath else "/storage/emulated/0/${song.folder}"
        }.map { (uniquePath, songList) ->
            val folderName = songList.firstOrNull()?.folder?.takeIf { it.isNotBlank() }
                ?: java.io.File(uniquePath).name.takeIf { it.isNotBlank() }
                ?: "Music"
            val storageType = songList.firstOrNull()?.storageType?.takeIf { it.isNotBlank() }
                ?: when {
                    uniquePath.contains("/emulated/0") || uniquePath.contains("/sdcard/0") -> "ذاكرة الهاتف الداخلية"
                    uniquePath.contains("/storage/") && !uniquePath.contains("emulated") -> "بطاقة SD الخارجية"
                    uniquePath.contains("sdcard") || uniquePath.contains("extsd") -> "بطاقة SD"
                    uniquePath.contains("usb") || uniquePath.contains("otg") -> "تخزين خارجي (USB)"
                    else -> "ذاكرة الهاتف الداخلية"
                }
            Folder(
                id = uniquePath,
                name = folderName,
                path = uniquePath,
                storageType = storageType,
                songCount = songList.size
            )
        }.sortedWith(compareBy({ it.storageType }, { it.name }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Folder and its dedicated song list
    private val _selectedFolder = MutableStateFlow<Folder?>(null)
    val selectedFolder = _selectedFolder.asStateFlow()

    // Last folder from which a song was played, for sequential back navigation
    private val _lastPlayedFolder = MutableStateFlow<Folder?>(null)
    val lastPlayedFolder = _lastPlayedFolder.asStateFlow()

    fun selectFolder(folder: Folder?) {
        _selectedFolder.value = folder
    }

    val songsForSelectedFolder: StateFlow<List<Song>> = combine(_allSongs, _selectedFolder) { songs, folder ->
        if (folder == null) emptyList()
        else {
            songs.filter { song ->
                (song.folderPath.isNotBlank() && song.folderPath == folder.path) ||
                (song.folderPath.isBlank() && song.folder == folder.name)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Playlist
    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist = _selectedPlaylist.asStateFlow()

    // Last playlist from which a song was played, for sequential back navigation
    private val _lastPlayedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val lastPlayedPlaylist = _lastPlayedPlaylist.asStateFlow()

    enum class PlaybackOrigin { SONGS, FOLDER, PLAYLIST, ALBUM, ARTIST, CLOUD }
    private var lastPlaybackOrigin: PlaybackOrigin = PlaybackOrigin.SONGS

    fun setPlaybackOrigin(origin: PlaybackOrigin) {
        lastPlaybackOrigin = origin
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val songsForSelectedPlaylist: StateFlow<List<Song>> = _selectedPlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList())
        else repository.getPlaylistSongIds(playlist.id).combine(_allSongs) { ids, songs ->
            val idSet = ids.toSet()
            songs.filter { it.id in idSet }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
    }

    // Player State
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue = _currentQueue.asStateFlow()

    private val _playbackMode = MutableStateFlow(PlaybackMode.REPEAT_ALL)
    val playbackMode = _playbackMode.asStateFlow()

    val isPlaying = audioEngine.isPlaying
    val currentPositionMs = audioEngine.currentPositionMs
    val durationMs = audioEngine.durationMs
    val playbackSpeed = audioEngine.playbackSpeed
    val sleepTimerRemainingSeconds = audioEngine.sleepTimerRemainingSeconds
    val audioFrequencies = audioEngine.audioFrequencies

    // Ultimate Audio Booster (الصوت المطلق المعزز)
    val isUltimateBoostEnabled = audioEngine.isUltimateBoostEnabled
    val ultimateBoostLevel = audioEngine.ultimateBoostLevel

    fun toggleUltimateBoost() {
        audioEngine.setUltimateBoost(!isUltimateBoostEnabled.value)
    }

    fun setUltimateBoostLevel(level: Int) {
        audioEngine.setUltimateBoostLevel(level)
    }

    // Equalizer State
    val eqPreset = audioEngine.currentPreset
    val eqBandLevels = audioEngine.bandLevels
    val eqBassBoost = audioEngine.bassBoostLevel
    val eqVirtualizer = audioEngine.virtualizerLevel
    val isAutoAdaptiveEq = audioEngine.isAutoAdaptiveEq

    // Exclusive Pro Audio Suite (الميزات الحصرية الخرافية)
    val selectedChamber = audioEngine.selectedChamber
    val isOrbitEnabled = audioEngine.isOrbitEnabled
    val orbitSpeed = audioEngine.orbitSpeed
    val currentPan = audioEngine.currentPan
    val isKaraokeMode = audioEngine.isKaraokeMode
    val vocalAttenuateLevel = audioEngine.vocalAttenuateLevel
    val isHapticBassEnabled = audioEngine.isHapticBassEnabled
    val selectedAmbience = audioEngine.selectedAmbience
    val detectedBpm = audioEngine.detectedBpm

    private val _isProAudioSheetOpen = MutableStateFlow(false)
    val isProAudioSheetOpen = _isProAudioSheetOpen.asStateFlow()

    fun setProAudioSheetOpen(open: Boolean) {
        _isProAudioSheetOpen.value = open
    }

    fun selectSpatialChamber(chamber: com.example.model.SpatialChamber) {
        audioEngine.setSpatialChamber(chamber)
    }

    fun toggleOrbitAudio(enabled: Boolean) {
        audioEngine.setOrbitAudio(enabled)
    }

    fun setOrbitSpeed(speed: Float) {
        audioEngine.setOrbitSpeed(speed)
    }

    fun toggleKaraokeMode(enabled: Boolean) {
        audioEngine.setKaraokeMode(enabled)
    }

    fun setKaraokeAttenuation(level: Int) {
        audioEngine.setKaraokeMode(isKaraokeMode.value, level)
    }

    fun toggleHapticBass(enabled: Boolean) {
        audioEngine.setHapticBass(enabled)
    }

    fun selectAcousticAmbience(ambience: com.example.model.AcousticAmbience) {
        audioEngine.setAcousticAmbience(ambience)
    }

    // Lyrics and Instant Translation
    private val _isLyricsExpanded = MutableStateFlow(false)
    val isLyricsExpanded = _isLyricsExpanded.asStateFlow()

    private val _showTranslatedLyrics = MutableStateFlow(true)
    val showTranslatedLyrics = _showTranslatedLyrics.asStateFlow()

    private val _aiExplanation = MutableStateFlow<String?>(null)
    val aiExplanation = _aiExplanation.asStateFlow()

    private val _isLoadingAi = MutableStateFlow(false)
    val isLoadingAi = _isLoadingAi.asStateFlow()

    // AI Suggestions Sheet
    private val _aiSuggestions = MutableStateFlow<List<Song>>(emptyList())
    val aiSuggestions = _aiSuggestions.asStateFlow()

    // Drive Mode / Voice Mode
    private val _isDriveMode = MutableStateFlow(false)
    val isDriveMode = _isDriveMode.asStateFlow()

    val isVoiceListening = voiceHandler.isListening
    val lastVoiceCommand = voiceHandler.lastHeardText

    // Dialogs / Sheets
    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded = _isNowPlayingExpanded.asStateFlow()

    private val _isEqualizerSheetOpen = MutableStateFlow(false)
    val isEqualizerSheetOpen = _isEqualizerSheetOpen.asStateFlow()

    private val _isAiSheetOpen = MutableStateFlow(false)
    val isAiSheetOpen = _isAiSheetOpen.asStateFlow()

    private val _isSleepTimerOpen = MutableStateFlow(false)
    val isSleepTimerOpen = _isSleepTimerOpen.asStateFlow()

    private val _isShareDialogOpen = MutableStateFlow(false)
    val isShareDialogOpen = _isShareDialogOpen.asStateFlow()

    private val _isCreatePlaylistDialogOpen = MutableStateFlow(false)
    val isCreatePlaylistDialogOpen = _isCreatePlaylistDialogOpen.asStateFlow()

    // New Splash, Settings, Wallpaper & Chroma, Metadata, Trim
    private val _isSplashScreenVisible = MutableStateFlow(true)
    val isSplashScreenVisible = _isSplashScreenVisible.asStateFlow()

    private val _isSettingsDialogOpen = MutableStateFlow(false)
    val isSettingsDialogOpen = _isSettingsDialogOpen.asStateFlow()

    private val _isEditMetadataDialogOpen = MutableStateFlow(false)
    val isEditMetadataDialogOpen = _isEditMetadataDialogOpen.asStateFlow()

    private val _isAudioTrimDialogOpen = MutableStateFlow(false)
    val isAudioTrimDialogOpen = _isAudioTrimDialogOpen.asStateFlow()

    private val _currentWallpaper = MutableStateFlow(com.example.model.AppWallpaper.OBSIDIAN_PRO)
    val currentWallpaper = _currentWallpaper.asStateFlow()

    private val _currentAccentColor = MutableStateFlow(com.example.model.AccentColorChoice.NEON_CYBER)
    val currentAccentColor = _currentAccentColor.asStateFlow()

    private val _currentChromaStyle = MutableStateFlow(com.example.model.ChromaStyle.NEON_CYBER)
    val currentChromaStyle = _currentChromaStyle.asStateFlow()

    private val _currentLanguage = MutableStateFlow("العربية")
    val currentLanguage = _currentLanguage.asStateFlow()

    // Folders view mode: "grid" or "list"
    private val _foldersViewMode = MutableStateFlow("grid")
    val foldersViewMode = _foldersViewMode.asStateFlow()

    fun toggleFoldersViewMode() {
        _foldersViewMode.value = if (_foldersViewMode.value == "grid") "list" else "grid"
    }

    fun setFoldersViewMode(mode: String) {
        _foldersViewMode.value = mode
    }

    val isCrossfadeEnabled = audioEngine.isCrossfadeEnabled
    val crossfadeDurationSeconds = audioEngine.crossfadeDurationSeconds

    fun setCrossfadeEnabled(enabled: Boolean) {
        audioEngine.setCrossfadeEnabled(enabled)
    }

    fun setCrossfadeDuration(seconds: Int) {
        audioEngine.setCrossfadeDuration(seconds)
    }

    fun setAccentColor(accentColor: com.example.model.AccentColorChoice) {
        _currentAccentColor.value = accentColor
    }

    init {
        loadSongs()

        audioEngine.onSongCompletionListener = {
            playNextSong(isAuto = true)
        }

        voiceHandler.onActionDetected = { action, text ->
            handleVoiceAction(action)
        }
    }

    fun loadSongs() {
        viewModelScope.launch {
            val songs = repository.getAllSongs()
            _allSongs.value = songs
            if (_currentSong.value == null && songs.isNotEmpty()) {
                _currentSong.value = songs.first()
                _currentQueue.value = songs
            }
        }
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setMood(mood: Mood) {
        _selectedMood.value = mood
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playSong(song: Song, queue: List<Song> = _allSongs.value) {
        _currentSong.value = song
        _currentQueue.value = queue
        audioEngine.playSong(song)
        audioEngine.estimateSongBpm(song)
        _aiExplanation.value = null // reset cached explanation for new song

        // Track playback origin for sequential back navigation
        if (_selectedFolder.value != null) {
            _lastPlayedFolder.value = _selectedFolder.value
            lastPlaybackOrigin = PlaybackOrigin.FOLDER
        } else if (_selectedPlaylist.value != null) {
            _lastPlayedPlaylist.value = _selectedPlaylist.value
            lastPlaybackOrigin = PlaybackOrigin.PLAYLIST
        } else {
            val matchingFolder = folders.value.firstOrNull { folder ->
                (song.folderPath.isNotBlank() && song.folderPath == folder.path) ||
                (song.folderPath.isBlank() && song.folder == folder.name)
            }
            if (matchingFolder != null) {
                _lastPlayedFolder.value = matchingFolder
            }
        }

        viewModelScope.launch {
            repository.logHistory(song.id)
        }
    }

    fun togglePlayPause() {
        if (_currentSong.value == null && _allSongs.value.isNotEmpty()) {
            playSong(_allSongs.value.first())
        } else {
            audioEngine.togglePlayPause()
        }
    }

    fun pause() {
        audioEngine.pause()
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun playNextSong(isAuto: Boolean = false) {
        val queue = _currentQueue.value
        if (queue.isEmpty()) return

        if (_playbackMode.value == PlaybackMode.REPEAT_ONE && isAuto) {
            _currentSong.value?.let { audioEngine.playSong(it) }
            return
        }

        val currentIndex = queue.indexOfFirst { it.id == _currentSong.value?.id }
        val nextIndex = when (_playbackMode.value) {
            PlaybackMode.SHUFFLE -> queue.indices.random()
            PlaybackMode.REPEAT_ONE -> if (isAuto) currentIndex else (currentIndex + 1) % queue.size
            PlaybackMode.REPEAT_ALL -> (currentIndex + 1) % queue.size
            PlaybackMode.SEQUENCE -> {
                if (currentIndex + 1 < queue.size) currentIndex + 1 else 0
            }
        }

        val nextSong = queue.getOrNull(nextIndex) ?: queue.first()
        playSong(nextSong, queue)
    }

    fun playPreviousSong() {
        val queue = _currentQueue.value
        if (queue.isEmpty()) return

        val currentIndex = queue.indexOfFirst { it.id == _currentSong.value?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else queue.lastIndex
        val prevSong = queue.getOrNull(prevIndex) ?: queue.first()
        playSong(prevSong, queue)
    }

    fun togglePlaybackMode() {
        _playbackMode.value = when (_playbackMode.value) {
            PlaybackMode.REPEAT_ALL -> PlaybackMode.REPEAT_ONE
            PlaybackMode.REPEAT_ONE -> PlaybackMode.SHUFFLE
            PlaybackMode.SHUFFLE -> PlaybackMode.SEQUENCE
            PlaybackMode.SEQUENCE -> PlaybackMode.REPEAT_ALL
        }
    }

    fun setSpeed(speed: Float) {
        audioEngine.setPlaybackSpeed(speed)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
            _allSongs.value = _allSongs.value.map {
                if (it.id == song.id) it.copy(isFavorite = !song.isFavorite) else it
            }
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = _currentSong.value?.copy(isFavorite = !song.isFavorite)
            }
        }
    }

    // Equalizer
    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        audioEngine.setBandLevel(bandIndex, levelDb)
    }

    fun setBassBoost(level: Int) {
        audioEngine.setBassBoost(level)
    }

    fun setVirtualizer(level: Int) {
        audioEngine.setVirtualizer(level)
    }

    fun setAutoAdaptiveEq(enabled: Boolean) {
        audioEngine.setAutoAdaptive(enabled)
    }

    fun applyEqPreset(preset: EqualizerPreset) {
        audioEngine.applyPreset(preset)
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int) {
        audioEngine.startSleepTimer(minutes)
        _isSleepTimerOpen.value = false
    }

    fun cancelSleepTimer() {
        audioEngine.cancelSleepTimer()
        _isSleepTimerOpen.value = false
    }

    // UI sheet toggles
    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
        if (!expanded) {
            val folder = _lastPlayedFolder.value
            val playlist = _lastPlayedPlaylist.value
            if (lastPlaybackOrigin == PlaybackOrigin.FOLDER && folder != null) {
                _currentTab.value = MainTab.FOLDERS
                _selectedFolder.value = folder
            } else if (lastPlaybackOrigin == PlaybackOrigin.PLAYLIST && playlist != null) {
                _currentTab.value = MainTab.PLAYLISTS
                _selectedPlaylist.value = playlist
            }
        }
    }

    fun setEqualizerSheetOpen(open: Boolean) {
        _isEqualizerSheetOpen.value = open
    }

    fun setAiSheetOpen(open: Boolean) {
        _isAiSheetOpen.value = open
    }

    fun setSleepTimerOpen(open: Boolean) {
        _isSleepTimerOpen.value = open
    }

    fun setShareDialogOpen(open: Boolean) {
        _isShareDialogOpen.value = open
    }

    fun setCreatePlaylistDialogOpen(open: Boolean) {
        _isCreatePlaylistDialogOpen.value = open
    }

    fun setDriveMode(enabled: Boolean) {
        _isDriveMode.value = enabled
    }

    fun toggleLyricsExpanded() {
        _isLyricsExpanded.value = !_isLyricsExpanded.value
    }

    fun toggleShowTranslatedLyrics() {
        _showTranslatedLyrics.value = !_showTranslatedLyrics.value
    }

    // AI Features
    fun generateAiRecommendations(prompt: String) {
        viewModelScope.launch {
            _isLoadingAi.value = true
            val recs = aiAssistant.getSongRecommendations(prompt, _currentSong.value, _allSongs.value)
            _aiSuggestions.value = recs
            _isLoadingAi.value = false
        }
    }

    fun explainCurrentSong() {
        val song = _currentSong.value ?: return
        viewModelScope.launch {
            _isLoadingAi.value = true
            val explanation = aiAssistant.explainSongMeaning(song)
            _aiExplanation.value = explanation
            _isLoadingAi.value = false
        }
    }

    // Voice commands
    fun startVoiceListening() {
        voiceHandler.startListening()
    }

    fun stopVoiceListening() {
        voiceHandler.stopListening()
    }

    private fun handleVoiceAction(action: VoiceAction) {
        when (action) {
            VoiceAction.PLAY -> audioEngine.resume()
            VoiceAction.PAUSE -> audioEngine.pause()
            VoiceAction.NEXT -> playNextSong()
            VoiceAction.PREVIOUS -> playPreviousSong()
            VoiceAction.SHUFFLE -> _playbackMode.value = PlaybackMode.SHUFFLE
            VoiceAction.REPEAT -> _playbackMode.value = PlaybackMode.REPEAT_ALL
            VoiceAction.OPEN_EQ -> _isEqualizerSheetOpen.value = true
            VoiceAction.TOGGLE_LYRICS -> _isLyricsExpanded.value = !_isLyricsExpanded.value
            VoiceAction.MOOD_ENERGETIC -> setMood(Mood.ENERGETIC)
            VoiceAction.MOOD_CHILL -> setMood(Mood.CHILL)
            VoiceAction.MOOD_WORKOUT -> setMood(Mood.WORKOUT)
            VoiceAction.UNKNOWN -> {}
        }
    }

    // Social Sharing
    fun shareCurrentSong() {
        val song = _currentSong.value ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_SUBJECT,
                "أستمع الآن عبر REMOMUSIC: ${song.title}"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                """
                🎵 أستمع الآن إلى '${song.title}' للمبدع '${song.artist}' 
                🎧 جودة صوت فائقة ومؤثرات 3D ومعادل ذكي عبر تطبيق REMOMUSIC!
                ✨ الحالة المزاجية: ${song.mood.arabicName}
                """.trimIndent()
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(shareIntent, "مشاركة الأغنية عبر").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(chooser)
        _isShareDialogOpen.value = false
    }

    // Playlists
    fun createPlaylist(name: String, description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createPlaylist(name, description)
            _currentSong.value?.let { song ->
                repository.addSongToPlaylist(id, song.id)
            }
            _isCreatePlaylistDialogOpen.value = false
        }
    }

    // Splash and Settings Controls
    fun dismissSplashScreen() {
        _isSplashScreenVisible.value = false
    }

    fun openSettingsDialog() {
        _isSettingsDialogOpen.value = true
    }

    fun closeSettingsDialog() {
        _isSettingsDialogOpen.value = false
    }

    fun openEditMetadataDialog() {
        _isEditMetadataDialogOpen.value = true
    }

    fun closeEditMetadataDialog() {
        _isEditMetadataDialogOpen.value = false
    }

    fun openAudioTrimDialog() {
        _isAudioTrimDialogOpen.value = true
    }

    fun closeAudioTrimDialog() {
        _isAudioTrimDialogOpen.value = false
    }

    fun setWallpaper(wallpaper: com.example.model.AppWallpaper) {
        _currentWallpaper.value = wallpaper
    }

    fun setChromaStyle(chroma: com.example.model.ChromaStyle) {
        _currentChromaStyle.value = chroma
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun updateSongMetadata(
        title: String,
        artist: String,
        album: String,
        genre: String,
        notes: String
    ) {
        val current = _currentSong.value ?: return
        val updated = current.copy(
            title = title,
            artist = artist,
            album = album,
            genre = genre,
            userNotes = notes
        )
        _currentSong.value = updated
        _allSongs.value = _allSongs.value.map { if (it.id == updated.id) updated else it }
        _currentQueue.value = _currentQueue.value.map { if (it.id == updated.id) updated else it }
        _isEditMetadataDialogOpen.value = false
        viewModelScope.launch {
            repository.updateSongNotesAndMetadata(updated.id, title, artist, album, genre, notes)
        }
    }

    fun updateSongCover(coverUri: String) {
        val current = _currentSong.value ?: return
        val updated = current.copy(albumArtUri = coverUri)
        _currentSong.value = updated
        _allSongs.value = _allSongs.value.map { if (it.id == updated.id) updated else it }
        _currentQueue.value = _currentQueue.value.map { if (it.id == updated.id) updated else it }
        viewModelScope.launch {
            repository.updateSongCover(updated.id, coverUri)
        }
    }

    fun setAsRingtone(song: Song) {
        try {
            val context = getApplication<Application>()
            // Attempt to launch settings or show toast
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (!android.provider.Settings.System.canWrite(context)) {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = android.net.Uri.parse("package:" + context.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } else {
                    android.media.RingtoneManager.setActualDefaultRingtoneUri(
                        context,
                        android.media.RingtoneManager.TYPE_RINGTONE,
                        android.net.Uri.parse(song.mediaUri)
                    )
                }
            }
        } catch (e: Exception) {
            // Graceful fallback
        }
    }

    /**
     * Sequential Back Navigation (الخروج التسلسلي):
     * 1. If Dialog / Sheet is open -> Close it
     * 2. If Drive Mode is open -> Close Drive mode
     * 3. If Now Playing screen is open -> Collapse to Mini Player (stay in current folder/view)
     * 4. If viewing inside a specific folder -> Exit folder back to folders list
     * 5. If viewing inside a specific playlist -> Exit playlist back to playlists list
     * 6. If on any other tab than SONGS -> Return to main SONGS tab
     * 7. If on SONGS with an active search query -> Clear search query
     * 8. Return false (allows system back to exit app)
     */
    fun handleBack(): Boolean = handleBackNavigation()

    fun handleBackNavigation(): Boolean {
        if (_isSettingsDialogOpen.value) {
            _isSettingsDialogOpen.value = false
            return true
        }
        if (_isEqualizerSheetOpen.value) {
            _isEqualizerSheetOpen.value = false
            return true
        }
        if (_isAiSheetOpen.value) {
            _isAiSheetOpen.value = false
            return true
        }
        if (_isSleepTimerOpen.value) {
            _isSleepTimerOpen.value = false
            return true
        }
        if (_isShareDialogOpen.value) {
            _isShareDialogOpen.value = false
            return true
        }
        if (_isCreatePlaylistDialogOpen.value) {
            _isCreatePlaylistDialogOpen.value = false
            return true
        }
        if (_isEditMetadataDialogOpen.value) {
            _isEditMetadataDialogOpen.value = false
            return true
        }
        if (_isAudioTrimDialogOpen.value) {
            _isAudioTrimDialogOpen.value = false
            return true
        }
        if (_isDriveMode.value) {
            _isDriveMode.value = false
            return true
        }
        if (_isNowPlayingExpanded.value) {
            _isNowPlayingExpanded.value = false
            // Return sequentially to the folder or playlist from which the song was played
            val folder = _lastPlayedFolder.value
            val playlist = _lastPlayedPlaylist.value
            if (lastPlaybackOrigin == PlaybackOrigin.FOLDER && folder != null) {
                _currentTab.value = MainTab.FOLDERS
                _selectedFolder.value = folder
            } else if (lastPlaybackOrigin == PlaybackOrigin.PLAYLIST && playlist != null) {
                _currentTab.value = MainTab.PLAYLISTS
                _selectedPlaylist.value = playlist
            } else if (folder != null) {
                _currentTab.value = MainTab.FOLDERS
                _selectedFolder.value = folder
            }
            return true
        }
        if (_selectedFolder.value != null) {
            _selectedFolder.value = null
            _currentTab.value = MainTab.FOLDERS
            return true
        }
        if (_selectedPlaylist.value != null) {
            _selectedPlaylist.value = null
            _currentTab.value = MainTab.PLAYLISTS
            return true
        }
        if (_searchQuery.value.isNotEmpty()) {
            _searchQuery.value = ""
            return true
        }
        if (_currentTab.value != MainTab.SONGS) {
            _currentTab.value = MainTab.SONGS
            return true
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
        voiceHandler.release()
    }
}
