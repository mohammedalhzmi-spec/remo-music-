package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Translate
import com.example.model.ChromaStyle
import com.example.ui.components.ChromaVisualizerRing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackMode
import com.example.model.Song
import com.example.ui.components.AudioWaveVisualizer
import com.example.ui.components.LiveAudioFrequencyVisualizer
import com.example.ui.components.RotatingVinylArtwork
import com.example.ui.components.formatDuration
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    playbackMode: PlaybackMode,
    playbackSpeed: Float,
    isLyricsExpanded: Boolean,
    showTranslatedLyrics: Boolean,
    aiExplanation: String?,
    isLoadingAi: Boolean,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onTogglePlaybackMode: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onToggleLyrics: () -> Unit,
    onToggleTranslation: () -> Unit,
    onExplainSongAi: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenShareDialog: () -> Unit,
    chromaStyle: ChromaStyle = ChromaStyle.NEON_CYBER,
    frequencies: List<Float> = emptyList(),
    onSetAsRingtone: () -> Unit = {},
    onOpenTrimDialog: () -> Unit = {},
    onOpenEditMetadata: () -> Unit = {},
    onChangeAlbumCover: () -> Unit = {},
    onOpenPlaylistDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (song == null) return

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragSliderValue by remember { mutableStateOf(0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showMoreOptionsMenu by remember { mutableStateOf(false) }

    val currentSliderValue = if (isDraggingSlider) {
        dragSliderValue
    } else {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val activeLyricIndex = remember(song, currentPositionMs) {
        val lyrics = song.lyrics
        if (lyrics.isEmpty()) -1
        else {
            val idx = lyrics.indexOfLast { currentPositionMs >= it.timeMs }
            if (idx >= 0) idx else 0
        }
    }

    val lyricsListState = rememberLazyListState()

    LaunchedEffect(activeLyricIndex) {
        if (activeLyricIndex >= 0 && isLyricsExpanded) {
            lyricsListState.animateScrollToItem((activeLyricIndex - 1).coerceAtLeast(0))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF141126),
                        DarkBackground,
                        Color(0xFF07080E)
                    )
                )
            )
            .testTag("now_playing_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.testTag("now_playing_collapse_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "يعمل الآن",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = song.genre,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("now_playing_fav_btn")
                    ) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (song.isFavorite) AmberGold else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onOpenShareDialog,
                        modifier = Modifier.testTag("now_playing_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextPrimary
                        )
                    }

                    // More Options Menu (Three dots)
                    Box {
                        IconButton(
                            onClick = { showMoreOptionsMenu = true },
                            modifier = Modifier.testTag("now_playing_more_menu_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = TextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreOptionsMenu,
                            onDismissRequest = { showMoreOptionsMenu = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("تشغيل المعادل الصوتي 🎚️", color = NeonCyan) },
                                onClick = {
                                    showMoreOptionsMenu = false
                                    onOpenEqualizer()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("تعيين كـ نغمة رنين للهاتف 🔔", color = Color(0xFFFF8500)) },
                                onClick = {
                                    showMoreOptionsMenu = false
                                    onSetAsRingtone()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("تحرير وقص الموسيقى ✂️", color = TextPrimary) },
                                onClick = {
                                    showMoreOptionsMenu = false
                                    onOpenTrimDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("تعديل بيانات الموسيقى والكتابة ✍️", color = TextPrimary) },
                                onClick = {
                                    showMoreOptionsMenu = false
                                    onOpenEditMetadata()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("تغيير صورة الغلاف 🖼️", color = TextPrimary) },
                                onClick = {
                                    showMoreOptionsMenu = false
                                    onChangeAlbumCover()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("إضافة إلى قائمة تشغيل 📑", color = TextPrimary) },
                                onClick = {
                                    showMoreOptionsMenu = false
                                    onOpenPlaylistDialog()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Central Area: Chroma Visualizer Ring (Click to toggle lyrics) or Synchronized Lyrics
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (!isLyricsExpanded) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ChromaVisualizerRing(
                            albumArtRes = song.albumArtRes,
                            albumArtUri = song.albumArtUri,
                            isPlaying = isPlaying,
                            chromaStyle = chromaStyle,
                            frequencies = frequencies,
                            onClick = onToggleLyrics,
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Real-time audio frequency visualizer synchronized with playback frequencies
                        LiveAudioFrequencyVisualizer(
                            frequencies = frequencies,
                            isPlaying = isPlaying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 16.dp),
                            showLabels = true
                        )
                    }
                } else {
                    // Synchronized Karaoke Lyrics View with Instant Translation
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = DarkSurfaceCard.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            // Lyrics header & translation toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onToggleLyrics() }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Album,
                                        contentDescription = "Return to cover",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "الكلمات (انقر للغلاف 💽)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onToggleTranslation() }
                                        .border(
                                            1.dp,
                                            if (showTranslatedLyrics) NeonCyan else DarkBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .testTag("toggle_lyrics_translation_btn"),
                                    color = if (showTranslatedLyrics) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceElevated
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Translate,
                                            contentDescription = "Translate",
                                            tint = if (showTranslatedLyrics) NeonCyan else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (showTranslatedLyrics) "ترجمة فورية: مفعلة" else "ترجمة فورية: معطلة",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (showTranslatedLyrics) NeonCyan else TextSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Lyrics list
                            LazyColumn(
                                state = lyricsListState,
                                modifier = Modifier.weight(1f)
                            ) {
                                itemsIndexed(song.lyrics) { index, lyricLine ->
                                    val isActive = index == activeLyricIndex
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable { onSeek(lyricLine.timeMs) },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = lyricLine.originalText,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                                                fontSize = if (isActive) 19.sp else 15.sp,
                                                color = if (isActive) NeonCyan else TextSecondary.copy(alpha = 0.7f)
                                            ),
                                            textAlign = TextAlign.Center
                                        )

                                        if (showTranslatedLyrics) {
                                            Text(
                                                text = lyricLine.translatedText,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = if (isActive) 16.sp else 13.sp,
                                                    color = if (isActive) Color(0xFFFF85B3) else TextTertiary
                                                ),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(top = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // AI Song Meaning Analysis Button
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onExplainSongAi() }
                                    .background(ElectricViolet.copy(alpha = 0.15f))
                                    .border(1.dp, ElectricViolet, RoundedCornerShape(12.dp))
                                    .testTag("explain_song_ai_btn"),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isLoadingAi) {
                                        CircularProgressIndicator(
                                            color = ElectricViolet,
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "جاري التحليل الموسيقي بواسطة Gemini...",
                                            style = MaterialTheme.typography.bodySmall.copy(color = ElectricViolet)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = ElectricViolet,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "شرح معاني الأغنية والمشاعر الكامنة بالذكاء الاصطناعي",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ElectricViolet
                                            )
                                        )
                                    }
                                }
                            }

                            // AI Analysis Result box
                            aiExplanation?.let { explanation ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = explanation,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                                        modifier = Modifier.padding(10.dp),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Song Title and Artist with Mood badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.artist} • ${song.album}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Lyrics Toggle Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleLyrics() }
                        .border(
                            1.dp,
                            if (isLyricsExpanded) NeonCyan else DarkBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("now_playing_lyrics_toggle_btn"),
                    color = if (isLyricsExpanded) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceElevated
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Lyrics",
                            tint = if (isLyricsExpanded) NeonCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "الكلمات",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isLyricsExpanded) NeonCyan else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = currentSliderValue,
                    onValueChange = {
                        isDraggingSlider = true
                        dragSliderValue = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        onSeek((dragSliderValue * durationMs).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color(0xFF242845)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("now_playing_progress_slider")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(if (isDraggingSlider) (dragSliderValue * durationMs).toLong() else currentPositionMs),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = formatDuration(durationMs),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Playback Control Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Shuffle / Mode Toggle
                IconButton(
                    onClick = onTogglePlaybackMode,
                    modifier = Modifier.testTag("btn_playback_mode")
                ) {
                    val icon = when (playbackMode) {
                        PlaybackMode.REPEAT_ALL -> Icons.Default.Repeat
                        PlaybackMode.REPEAT_ONE -> Icons.Default.RepeatOne
                        PlaybackMode.SHUFFLE -> Icons.Default.Shuffle
                        PlaybackMode.SEQUENCE -> Icons.Default.Repeat
                    }
                    val tint = if (playbackMode == PlaybackMode.SEQUENCE) TextSecondary else NeonCyan
                    Icon(
                        imageVector = icon,
                        contentDescription = "Mode",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_prev_song")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Play / Pause (Giant glowing button)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(16.dp, CircleShape, spotColor = NeonCyan)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonCyan, Color(0xFF00B4D8)))
                        )
                        .clickable { onTogglePlayPause() }
                        .testTag("btn_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF090A10),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_next_song")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Speed Selector
                Box {
                    IconButton(
                        onClick = { showSpeedMenu = true },
                        modifier = Modifier.testTag("btn_speed")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${playbackSpeed}x",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (playbackSpeed != 1.0f) NeonCyan else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSpeedMenu,
                        onDismissRequest = { showSpeedMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${speed}x",
                                        color = if (playbackSpeed == speed) NeonCyan else TextPrimary
                                    )
                                },
                                onClick = {
                                    onSetSpeed(speed)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Utilities Row: Equalizer, Sleep Timer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equalizer Shortcut
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenEqualizer() }
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .testTag("shortcut_eq_btn"),
                    color = DarkSurfaceElevated
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "EQ",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "المعادل الصوتي",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary)
                        )
                    }
                }

                // Sleep Timer Shortcut
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenSleepTimer() }
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .testTag("shortcut_sleep_timer_btn"),
                    color = DarkSurfaceElevated
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep Timer",
                            tint = NeonMagenta,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مؤقت النوم",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary)
                        )
                    }
                }
            }
        }
    }
}
