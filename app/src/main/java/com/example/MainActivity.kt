package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AppWallpaper
import com.example.model.Mood
import com.example.model.Song
import com.example.service.MusicPlaybackService
import com.example.ui.components.AnimatedSplashScreen
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.TopHeader
import com.example.ui.dialogs.AudioTrimDialog
import com.example.ui.dialogs.CreatePlaylistDialog
import com.example.ui.dialogs.EditMetadataDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.SleepTimerDialog
import com.example.ui.dialogs.SocialShareCardDialog
import com.example.ui.screens.AlbumsView
import com.example.ui.screens.ArtistsView
import com.example.ui.screens.CloudStreamView
import com.example.ui.screens.DriveModeScreen
import com.example.ui.screens.FoldersView
import com.example.ui.screens.FolderDetailView
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PlaylistsView
import com.example.ui.screens.PlaylistDetailView
import com.example.ui.screens.SongsListView
import com.example.ui.sheets.AiAssistantSheet
import com.example.ui.sheets.EqualizerSheet
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainTab
import com.example.viewmodel.MusicViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val isSplashScreenVisible by viewModel.isSplashScreenVisible.collectAsState()

                // Request runtime permissions on first launch
                RequestPermissionsEffect(onPermissionsGranted = {
                    viewModel.loadSongs()
                })

                if (isSplashScreenVisible) {
                    AnimatedSplashScreen(
                        onDismiss = { viewModel.dismissSplashScreen() }
                    )
                } else {
                    RemoMusicApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RequestPermissionsEffect(onPermissionsGranted: () -> Unit) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap.values.any { it }) {
            onPermissionsGranted()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(Manifest.permission.RECORD_AUDIO)

        permissionLauncher.launch(permissions.toTypedArray())
    }
}

@Composable
fun RemoMusicApp(viewModel: MusicViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val filteredSongs by viewModel.filteredSongs.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val playbackMode by viewModel.playbackMode.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val sleepTimerRemaining by viewModel.sleepTimerRemainingSeconds.collectAsState()
    val audioFrequencies by viewModel.audioFrequencies.collectAsState()

    val context = LocalContext.current
    var showDeveloperNotice by remember { mutableStateOf(true) }

    LaunchedEffect(showDeveloperNotice) {
        if (showDeveloperNotice) {
            delay(5500)
            showDeveloperNotice = false
        }
    }

    LaunchedEffect(Unit) {
        MusicPlaybackService.onPlaybackActionListener = { action ->
            when (action) {
                MusicPlaybackService.PlaybackAction.PLAY_PAUSE -> viewModel.togglePlayPause()
                MusicPlaybackService.PlaybackAction.NEXT -> viewModel.playNextSong()
                MusicPlaybackService.PlaybackAction.PREV -> viewModel.playPreviousSong()
                MusicPlaybackService.PlaybackAction.STOP -> viewModel.pause()
            }
        }
        MusicPlaybackService.onSeekActionListener = { seekPos ->
            viewModel.seekTo(seekPos)
        }
    }

    LaunchedEffect(currentSong?.id, isPlaying, durationMs) {
        MusicPlaybackService.startOrUpdate(
            context = context,
            song = currentSong,
            isPlaying = isPlaying,
            durationMs = durationMs,
            positionMs = currentPositionMs
        )
    }

    LaunchedEffect(currentPositionMs) {
        if (isPlaying && durationMs > 0) {
            MusicPlaybackService.updateProgress(currentPositionMs, durationMs, isPlaying)
        }
    }

    // Intercept back button for sequential back navigation
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val exitToast = remember {
        Toast.makeText(context, "اضغط مرة أخرى للخروج من التطبيق", Toast.LENGTH_SHORT)
    }

    BackHandler {
        val handled = viewModel.handleBack()
        if (!handled) {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2000L) {
                exitToast.cancel()
                (context as? ComponentActivity)?.finish()
            } else {
                lastBackPressTime = now
                exitToast.show()
            }
        }
    }

    // Themes, Wallpapers & Chroma
    val currentWallpaper by viewModel.currentWallpaper.collectAsState()
    val currentChromaStyle by viewModel.currentChromaStyle.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    // Selected Folder and Playlist (for sequential navigation)
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val folderSongs by viewModel.songsForSelectedFolder.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val playlistSongs by viewModel.songsForSelectedPlaylist.collectAsState()

    // Equalizer
    val currentEqPreset by viewModel.eqPreset.collectAsState()
    val bandLevels by viewModel.eqBandLevels.collectAsState()
    val bassBoost by viewModel.eqBassBoost.collectAsState()
    val virtualizer by viewModel.eqVirtualizer.collectAsState()
    val isAutoAdaptiveEq by viewModel.isAutoAdaptiveEq.collectAsState()

    // Lyrics & AI
    val isLyricsExpanded by viewModel.isLyricsExpanded.collectAsState()
    val showTranslatedLyrics by viewModel.showTranslatedLyrics.collectAsState()
    val aiExplanation by viewModel.aiExplanation.collectAsState()
    val isLoadingAi by viewModel.isLoadingAi.collectAsState()
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()

    // Sheets & Dialogs
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val isEqualizerSheetOpen by viewModel.isEqualizerSheetOpen.collectAsState()
    val isAiSheetOpen by viewModel.isAiSheetOpen.collectAsState()
    val isSleepTimerOpen by viewModel.isSleepTimerOpen.collectAsState()
    val isShareDialogOpen by viewModel.isShareDialogOpen.collectAsState()
    val isCreatePlaylistDialogOpen by viewModel.isCreatePlaylistDialogOpen.collectAsState()
    val isDriveMode by viewModel.isDriveMode.collectAsState()
    val isSettingsDialogOpen by viewModel.isSettingsDialogOpen.collectAsState()
    val isEditMetadataDialogOpen by viewModel.isEditMetadataDialogOpen.collectAsState()
    val isAudioTrimDialogOpen by viewModel.isAudioTrimDialogOpen.collectAsState()

    // Voice
    val isVoiceListening by viewModel.isVoiceListening.collectAsState()
    val lastVoiceCommand by viewModel.lastVoiceCommand.collectAsState()

    // Photo Picker for album cover change
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.updateSongCover(it.toString()) }
    }

    val wallpaperBrush = Brush.verticalGradient(
        currentWallpaper.gradientHexes.map { Color(it) }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(wallpaperBrush)
                .padding(innerPadding)
        ) {
            // Main Library View
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Branding, Search, Mood Chips, Action Shortcuts, Settings)
                TopHeader(
                    selectedMood = selectedMood,
                    onSelectMood = { viewModel.setMood(it) },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onOpenAiSheet = { viewModel.setAiSheetOpen(true) },
                    onOpenEqualizer = { viewModel.setEqualizerSheetOpen(true) },
                    onOpenDriveMode = { viewModel.setDriveMode(true) },
                    onOpenSleepTimer = { viewModel.setSleepTimerOpen(true) },
                    onOpenSettings = { viewModel.openSettingsDialog() },
                    sleepTimerRemaining = sleepTimerRemaining,
                    onLogoClick = { showDeveloperNotice = true }
                )

                // Navigation Tabs Bar (Songs, Artists, Albums, Folders, Playlists, Cloud)
                TabsNavigationRow(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )

                // Content View according to tab
                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        MainTab.SONGS -> SongsListView(
                            songs = filteredSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSongClick = { song -> viewModel.playSong(song, filteredSongs) },
                            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                            onAddToPlaylist = { viewModel.setCreatePlaylistDialogOpen(true) },
                            onShareSong = { viewModel.setShareDialogOpen(true) },
                            onExplainAi = {
                                viewModel.playSong(it, filteredSongs)
                                viewModel.setNowPlayingExpanded(true)
                                viewModel.explainCurrentSong()
                            }
                        )
                        MainTab.ARTISTS -> ArtistsView(
                            artists = artists,
                            onArtistClick = { artist ->
                                viewModel.setSearchQuery(artist.name)
                                viewModel.setTab(MainTab.SONGS)
                            }
                        )
                        MainTab.ALBUMS -> AlbumsView(
                            albums = albums,
                            onAlbumClick = { album ->
                                viewModel.setSearchQuery(album.name)
                                viewModel.setTab(MainTab.SONGS)
                            }
                        )
                        MainTab.FOLDERS -> {
                            if (selectedFolder != null) {
                                FolderDetailView(
                                    folder = selectedFolder!!,
                                    songs = folderSongs,
                                    currentSong = currentSong,
                                    isPlaying = isPlaying,
                                    onBack = { viewModel.selectFolder(null) },
                                    onPlayAll = {
                                        if (folderSongs.isNotEmpty()) {
                                            viewModel.playSong(folderSongs.first(), folderSongs)
                                            viewModel.setNowPlayingExpanded(true)
                                        }
                                    },
                                    onSongClick = { song: Song ->
                                        viewModel.playSong(song, folderSongs)
                                        viewModel.setNowPlayingExpanded(true)
                                    },
                                    onToggleFavorite = { song: Song -> viewModel.toggleFavorite(song) },
                                    onAddToPlaylist = { _: Song -> viewModel.setCreatePlaylistDialogOpen(true) },
                                    onShareSong = { _: Song -> viewModel.setShareDialogOpen(true) },
                                    onExplainAi = { song: Song ->
                                        viewModel.playSong(song, folderSongs)
                                        viewModel.setNowPlayingExpanded(true)
                                        viewModel.explainCurrentSong()
                                    }
                                )
                            } else {
                                FoldersView(
                                    folders = folders,
                                    onFolderClick = { folder ->
                                        viewModel.selectFolder(folder)
                                    }
                                )
                            }
                        }
                        MainTab.PLAYLISTS -> {
                            if (selectedPlaylist != null) {
                                PlaylistDetailView(
                                    playlist = selectedPlaylist!!,
                                    songs = playlistSongs,
                                    currentSong = currentSong,
                                    isPlaying = isPlaying,
                                    onBack = { viewModel.selectPlaylist(null) },
                                    onPlayAll = {
                                        if (playlistSongs.isNotEmpty()) {
                                            viewModel.playSong(playlistSongs.first(), playlistSongs)
                                            viewModel.setNowPlayingExpanded(true)
                                        }
                                    },
                                    onSongClick = { song: Song ->
                                        viewModel.playSong(song, playlistSongs)
                                        viewModel.setNowPlayingExpanded(true)
                                    },
                                    onToggleFavorite = { song: Song -> viewModel.toggleFavorite(song) },
                                    onAddToPlaylist = { _: Song -> viewModel.setCreatePlaylistDialogOpen(true) },
                                    onShareSong = { _: Song -> viewModel.setShareDialogOpen(true) },
                                    onExplainAi = { song: Song ->
                                        viewModel.playSong(song, playlistSongs)
                                        viewModel.setNowPlayingExpanded(true)
                                        viewModel.explainCurrentSong()
                                    }
                                )
                            } else {
                                PlaylistsView(
                                    playlists = playlists,
                                    onCreatePlaylist = { viewModel.setCreatePlaylistDialogOpen(true) },
                                    onPlaylistClick = { playlist ->
                                        viewModel.selectPlaylist(playlist)
                                    }
                                )
                            }
                        }
                        MainTab.CLOUD -> CloudStreamView(
                            cloudSongs = filteredSongs.filter { it.isCloud },
                            onSongClick = { song -> viewModel.playSong(song, filteredSongs) }
                        )
                    }
                }
            }

            // Anchored Mini Player at bottom
            if (currentSong != null && !isNowPlayingExpanded && !isDriveMode) {
                MiniPlayerBar(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.playNextSong() },
                    onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it) } },
                    onClick = { viewModel.setNowPlayingExpanded(true) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }

            // Animated Full Screen Now Playing Screen
            AnimatedVisibility(
                visible = isNowPlayingExpanded && !isDriveMode,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NowPlayingScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    playbackMode = playbackMode,
                    playbackSpeed = playbackSpeed,
                    isLyricsExpanded = isLyricsExpanded,
                    showTranslatedLyrics = showTranslatedLyrics,
                    aiExplanation = aiExplanation,
                    isLoadingAi = isLoadingAi,
                    chromaStyle = currentChromaStyle,
                    frequencies = audioFrequencies,
                    onCollapse = { viewModel.setNowPlayingExpanded(false) },
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.playNextSong() },
                    onPrevious = { viewModel.playPreviousSong() },
                    onSeek = { viewModel.seekTo(it) },
                    onTogglePlaybackMode = { viewModel.togglePlaybackMode() },
                    onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it) } },
                    onSetSpeed = { viewModel.setSpeed(it) },
                    onToggleLyrics = { viewModel.toggleLyricsExpanded() },
                    onToggleTranslation = { viewModel.toggleShowTranslatedLyrics() },
                    onExplainSongAi = { viewModel.explainCurrentSong() },
                    onOpenEqualizer = { viewModel.setEqualizerSheetOpen(true) },
                    onOpenSleepTimer = { viewModel.setSleepTimerOpen(true) },
                    onOpenShareDialog = { viewModel.setShareDialogOpen(true) },
                    onSetAsRingtone = { currentSong?.let { viewModel.setAsRingtone(it) } },
                    onOpenTrimDialog = { viewModel.openAudioTrimDialog() },
                    onOpenEditMetadata = { viewModel.openEditMetadataDialog() },
                    onChangeAlbumCover = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onOpenPlaylistDialog = { viewModel.setCreatePlaylistDialogOpen(true) }
                )
            }

            // Drive Mode Screen Overlay
            AnimatedVisibility(
                visible = isDriveMode,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                DriveModeScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    isListeningVoice = isVoiceListening,
                    lastVoiceCommand = lastVoiceCommand,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.playNextSong() },
                    onPrevious = { viewModel.playPreviousSong() },
                    onStartVoiceListening = { viewModel.startVoiceListening() },
                    onStopVoiceListening = { viewModel.stopVoiceListening() },
                    onExitDriveMode = { viewModel.setDriveMode(false) }
                )
            }

            // Startup Developer Credit Notice with auto-dismissal
            AnimatedVisibility(
                visible = showDeveloperNotice,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberGold),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .clickable { showDeveloperNotice = false }
                        .testTag("developer_startup_notice")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AmberGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✨", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "هذا التطبيق برمجة وتطوير المطور محمد الحزمي",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "جميع الحقوق محفوظة للمطور 2026",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AmberGold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Equalizer Sheet
    if (isEqualizerSheetOpen) {
        EqualizerSheet(
            currentPresetId = currentEqPreset,
            bandLevels = bandLevels,
            bassBoost = bassBoost,
            virtualizer = virtualizer,
            isAutoAdaptive = isAutoAdaptiveEq,
            onSelectPreset = { viewModel.applyEqPreset(it) },
            onBandChange = { band, level -> viewModel.setBandLevel(band, level) },
            onBassBoostChange = { viewModel.setBassBoost(it) },
            onVirtualizerChange = { viewModel.setVirtualizer(it) },
            onToggleAutoAdaptive = { viewModel.setAutoAdaptiveEq(it) },
            onDismiss = { viewModel.setEqualizerSheetOpen(false) }
        )
    }

    // Modal AI Assistant Sheet
    if (isAiSheetOpen) {
        AiAssistantSheet(
            isLoading = isLoadingAi,
            suggestions = aiSuggestions,
            onGenerateRecommendations = { prompt -> viewModel.generateAiRecommendations(prompt) },
            onPlaySong = { song, list -> viewModel.playSong(song, list) },
            onDismiss = { viewModel.setAiSheetOpen(false) }
        )
    }

    // Settings Dialog
    if (isSettingsDialogOpen) {
        SettingsDialog(
            currentWallpaper = currentWallpaper,
            onSelectWallpaper = { viewModel.setWallpaper(it) },
            currentChroma = currentChromaStyle,
            onSelectChroma = { viewModel.setChromaStyle(it) },
            currentLanguage = currentLanguage,
            onSelectLanguage = { viewModel.setLanguage(it) },
            onOpenSleepTimer = {
                viewModel.closeSettingsDialog()
                viewModel.setSleepTimerOpen(true)
            },
            onDismiss = { viewModel.closeSettingsDialog() }
        )
    }

    // Edit Metadata Dialog (Write notes on songs, change title/artist/album/genre)
    if (isEditMetadataDialogOpen) {
        EditMetadataDialog(
            song = currentSong,
            onSave = { title, artist, album, genre, notes ->
                viewModel.updateSongMetadata(title, artist, album, genre, notes)
            },
            onChangeCover = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismiss = { viewModel.closeEditMetadataDialog() }
        )
    }

    // Audio Trim & Ringtone Dialog
    if (isAudioTrimDialogOpen) {
        AudioTrimDialog(
            song = currentSong,
            durationMs = durationMs,
            onSetAsRingtone = {
                currentSong?.let { viewModel.setAsRingtone(it) }
            },
            onDismiss = { viewModel.closeAudioTrimDialog() }
        )
    }

    // Dialogs
    if (isSleepTimerOpen) {
        SleepTimerDialog(
            currentTimerRemaining = sleepTimerRemaining,
            onSetTimer = { mins -> viewModel.startSleepTimer(mins) },
            onCancelTimer = { viewModel.cancelSleepTimer() },
            onDismiss = { viewModel.setSleepTimerOpen(false) }
        )
    }

    if (isShareDialogOpen) {
        SocialShareCardDialog(
            song = currentSong,
            onShareStory = { viewModel.shareCurrentSong() },
            onDismiss = { viewModel.setShareDialogOpen(false) }
        )
    }

    if (isCreatePlaylistDialogOpen) {
        CreatePlaylistDialog(
            onCreate = { name, desc -> viewModel.createPlaylist(name, desc) },
            onDismiss = { viewModel.setCreatePlaylistDialogOpen(false) }
        )
    }
}

@Composable
fun TabsNavigationRow(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MainTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(tab) }
                    .testTag("tab_${tab.name}"),
                color = if (isSelected) NeonCyan else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = tab.arabicName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF090A10) else TextSecondary,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
