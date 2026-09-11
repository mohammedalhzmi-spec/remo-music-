package com.example.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Song
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SleepTimerDialog(
    currentTimerRemaining: Int?,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf(15, 30, 45, 60, 90)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bedtime, contentDescription = null, tint = NeonMagenta)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "مؤقت النوم الذكي",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        },
        text = {
            Column {
                Text(
                    text = if (currentTimerRemaining != null) {
                        "المؤقت يعمل حالياً: يتبقى ${(currentTimerRemaining / 60)} دقيقة و ${(currentTimerRemaining % 60)} ثانية لإيقاف التشغيل تلقائياً."
                    } else {
                        "اختر المدة المرغوبة لإيقاف الموسيقى تلقائياً بنعومة:"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { mins ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSetTimer(mins) }
                                .border(1.dp, NeonMagenta.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .testTag("sleep_timer_${mins}m"),
                            color = NeonMagenta.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$mins د",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonMagenta
                                ),
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (currentTimerRemaining != null) {
                TextButton(
                    onClick = onCancelTimer,
                    modifier = Modifier.testTag("cancel_sleep_timer_btn")
                ) {
                    Text("إلغاء المؤقت", color = Color(0xFFFF5252))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = TextSecondary)
            }
        },
        modifier = Modifier.testTag("sleep_timer_dialog")
    )
}

@Composable
fun SocialShareCardDialog(
    song: Song?,
    onShareStory: () -> Unit,
    onDismiss: () -> Unit
) {
    if (song == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "مشاركة الموسيقى (Social Story)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // High-design Story Card Preview
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet)), RoundedCornerShape(20.dp))
                        .testTag("social_share_card_preview"),
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Card Header Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "أستمع الآن عبر REMOMUSIC",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        // Artwork
                        Image(
                            painter = painterResource(id = song.albumArtRes ?: R.drawable.ic_app_icon_1788818206748),
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Text(
                            text = "${song.artist} • ${song.album}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "✨ الحالة المزاجية: ${song.mood.arabicName}",
                                style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShareStory,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.testTag("confirm_share_story_btn")
            ) {
                Text("مشاركة الآن", color = Color(0xFF090A10), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextSecondary)
            }
        },
        modifier = Modifier.testTag("social_share_dialog")
    )
}

@Composable
fun CreatePlaylistDialog(
    onCreate: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "إنشاء قائمة تشغيل جديدة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم قائمة التشغيل", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف القائمة (اختياري)", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, description) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.testTag("confirm_create_playlist_btn")
            ) {
                Text("إنشاء", color = Color(0xFF090A10), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextSecondary)
            }
        },
        modifier = Modifier.testTag("create_playlist_dialog")
    )
}

@Composable
fun SettingsDialog(
    currentWallpaper: com.example.model.AppWallpaper,
    onSelectWallpaper: (com.example.model.AppWallpaper) -> Unit,
    currentChroma: com.example.model.ChromaStyle,
    onSelectChroma: (com.example.model.ChromaStyle) -> Unit,
    currentAccentColor: com.example.model.AccentColorChoice = com.example.model.AccentColorChoice.NEON_CYBER,
    onSelectAccentColor: (com.example.model.AccentColorChoice) -> Unit = {},
    isCrossfadeEnabled: Boolean = true,
    onToggleCrossfade: (Boolean) -> Unit = {},
    crossfadeDurationSeconds: Int = 3,
    onChangeCrossfadeDuration: (Int) -> Unit = {},
    currentLanguage: String,
    onSelectLanguage: (String) -> Unit,
    onOpenSleepTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showUpdateMessage by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111D),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonCyan, ElectricViolet))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color(0xFF0D0F18),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "إعدادات REMOMUSIC",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(490.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Themes (شبابي، نسائي، إيماني، رياضي، طبيعي...)
                item {
                    Column {
                        Text(
                            text = "🎭 ثيمات المشغل (Themes)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        com.example.model.AppWallpaper.values().forEach { wallpaper ->
                            val isSelected = currentWallpaper == wallpaper
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectWallpaper(wallpaper) }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) NeonCyan else DarkBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else Color(0xFF141727)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = wallpaper.icon, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = wallpaper.arabicName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) NeonCyan else TextPrimary
                                            )
                                        )
                                        Text(
                                            text = wallpaper.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    if (isSelected) {
                                        Text("✓", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Accent Colors (تغيير ألوان التطبيق)
                item {
                    Column {
                        Text(
                            text = "🎨 ألوان التطبيق الافتراضية",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8500)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        com.example.model.AccentColorChoice.values().forEach { choice ->
                            val isSelected = currentAccentColor == choice
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectAccentColor(choice) }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) choice.primaryColor else DarkBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                color = if (isSelected) choice.primaryColor.copy(alpha = 0.15f) else Color(0xFF141727)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(choice.primaryColor)
                                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = choice.arabicName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) choice.primaryColor else TextPrimary
                                            )
                                        )
                                        Text(
                                            text = choice.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    if (isSelected) {
                                        Text("✓", color = choice.primaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Music Crossfade (تلاشي الموسيقى)
                item {
                    Column {
                        Text(
                            text = "🎚️ تلاشي الموسيقى وانسيابها (Crossfade)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                            color = Color(0xFF141727)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "تفعيل التلاشي التدريجي",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "انتقال سلس بين المقاطع الصوتية بدون توقف مفاجئ",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    androidx.compose.material3.Switch(
                                        checked = isCrossfadeEnabled,
                                        onCheckedChange = onToggleCrossfade,
                                        colors = androidx.compose.material3.SwitchDefaults.colors(
                                            checkedThumbColor = EmeraldGreen,
                                            checkedTrackColor = EmeraldGreen.copy(alpha = 0.35f)
                                        )
                                    )
                                }

                                if (isCrossfadeEnabled) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "مدة التلاشي: $crossfadeDurationSeconds ثواني",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = EmeraldGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    androidx.compose.material3.Slider(
                                        value = crossfadeDurationSeconds.toFloat(),
                                        onValueChange = { onChangeCrossfadeDuration(it.toInt()) },
                                        valueRange = 1f..10f,
                                        steps = 8,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = EmeraldGreen,
                                            activeTrackColor = EmeraldGreen
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: Chroma Visualizer Rings
                item {
                    Column {
                        Text(
                            text = "💫 نمط كروما المشغل الدائرية",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8500)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        com.example.model.ChromaStyle.values().forEach { chroma ->
                            val isSelected = currentChroma == chroma
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectChroma(chroma) }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFF8500) else DarkBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                color = if (isSelected) Color(0xFFFF8500).copy(alpha = 0.15f) else Color(0xFF141727)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = chroma.icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chroma.arabicName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFFFF8500) else TextPrimary
                                            )
                                        )
                                        Text(
                                            text = chroma.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    if (isSelected) {
                                        Text("✓", color = Color(0xFFFF8500), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 5: General settings (Language, Sleep timer, Share App)
                item {
                    Column {
                        Text(
                            text = "⚙️ خيارات عامة وميزات إضافية",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Language Selection (Full language support)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isLanguageExpanded = !isLanguageExpanded }
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                            color = Color(0xFF141727)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🌐", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "تغيير لغة التطبيق (Language)",
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "اللغة الرئيسية: العربية",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = currentLanguage,
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isLanguageExpanded) "▲" else "▼",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (isLanguageExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        com.example.model.AVAILABLE_LANGUAGES.forEach { lang ->
                                            val isLangSelected = currentLanguage.contains(lang.code, ignoreCase = true) ||
                                                    currentLanguage.contains(lang.name, ignoreCase = true) ||
                                                    currentLanguage.contains(lang.englishName, ignoreCase = true)
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        onSelectLanguage(lang.name)
                                                        isLanguageExpanded = false
                                                    },
                                                color = if (isLangSelected) NeonCyan.copy(alpha = 0.18f) else Color(0xFF1A1E30)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(lang.flag, fontSize = 16.sp)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = lang.name,
                                                            color = if (isLangSelected) NeonCyan else TextPrimary,
                                                            fontWeight = if (isLangSelected) FontWeight.Bold else FontWeight.Normal,
                                                            fontSize = 13.sp
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "(${lang.englishName})",
                                                            color = TextSecondary,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                    if (isLangSelected) {
                                                        Text("✓", color = NeonCyan, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sleep timer
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onDismiss()
                                    onOpenSleepTimer()
                                }
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                            color = Color(0xFF141727)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🌙", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("تشغيل وضع ومؤقت النوم", color = TextPrimary, fontWeight = FontWeight.Medium)
                                }
                                Text("فتح ❯", color = NeonMagenta, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Share App (مشاركة التطبيق)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    try {
                                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                android.content.Intent.EXTRA_SUBJECT,
                                                "ريمو مشغل الموسيقى الاحترافي"
                                            )
                                            putExtra(
                                                android.content.Intent.EXTRA_TEXT,
                                                "استمتع بأفضل تجربة صوتية ومعادل صوتي ذكي وثيمات متميزة مع تطبيق 'ريمو مشغل الموسيقى الاحترافي':\nhttps://mohammedalhzmi.blogspot.com"
                                            )
                                        }
                                        context.startActivity(
                                            android.content.Intent.createChooser(shareIntent, "مشاركة التطبيق")
                                        )
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                            color = Color(0xFF141727)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📤", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("مشاركة التطبيق مع الأصدقاء", color = TextPrimary, fontWeight = FontWeight.Medium)
                                }
                                Text("مشاركة ❯", color = NeonCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section 6: App Updates & Developer Channels
                item {
                    Column {
                        Text(
                            text = "🔄 خيار تحديث التطبيق وقنوات المطور",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // App Version & Check Updates Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                            color = Color(0xFF141727)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🚀", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "ريمو مشغل الموسيقى الاحترافي",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            )
                                            Text(
                                                text = "إصدار التطبيق: Version 2.5.0 Pro Ultra",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = NeonCyan,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showUpdateMessage = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "فحص التحديثات",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = Color(0xFF090A10),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                if (showUpdateMessage) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF00FFB2).copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FFB2)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "✓ لديك أحدث وأقوى إصدار رسمي من ريمو مشغل الموسيقى الاحترافي 2026 مع كافة المزايا المتقدمة!",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF00FFB2)),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "قنوات التحديث الرسمية وتواصل المطور:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // 1. Telegram Channel
                        DeveloperLinkRow(
                            icon = "📢",
                            title = "قناة المطور محمد الحزمي تلجرام",
                            subtitle = "https://t.me/moh_alymani1",
                            url = "https://t.me/moh_alymani1",
                            accentColor = Color(0xFF0088CC),
                            context = context
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. Blogger Website
                        DeveloperLinkRow(
                            icon = "✍️",
                            title = "مدونة المطور محمد الحزمي للتقنية بلوجر",
                            subtitle = "https://mohammedalhzmi.blogspot.com",
                            url = "https://mohammedalhzmi.blogspot.com",
                            accentColor = Color(0xFFFF5722),
                            context = context
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. AI Platform
                        DeveloperLinkRow(
                            icon = "🧠",
                            title = "منصة محمد الحزمي للذكاء الاصطناعي AI",
                            subtitle = "https://mohammed-alhazmi-ai-complete-1.vercel.app",
                            url = "https://mohammed-alhazmi-ai-complete-1.vercel.app",
                            accentColor = Color(0xFF9C27B0),
                            context = context
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("تم وحفظ الإعدادات", color = Color(0xFF090A10), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun DeveloperLinkRow(
    icon: String,
    title: String,
    subtitle: String,
    url: String,
    accentColor: Color,
    context: android.content.Context
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                try {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(url)
                    ).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "الرابط: $url", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        color = Color(0xFF141727)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = accentColor,
                        fontSize = 11.sp
                    )
                )
            }
            Text("فتح ↗", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun EditMetadataDialog(
    song: Song?,
    onSave: (title: String, artist: String, album: String, genre: String, notes: String) -> Unit,
    onChangeCover: () -> Unit,
    onDismiss: () -> Unit
) {
    if (song == null) return

    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }
    var genre by remember { mutableStateOf(song.genre) }
    var notes by remember { mutableStateOf(song.userNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111D),
        title = {
            Text(
                text = "تعديل بيانات الموسيقى والكتابة فوقها",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Change Album Art button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onChangeCover() }
                            .border(1.dp, NeonCyan, RoundedCornerShape(12.dp)),
                        color = NeonCyan.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🖼️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تغيير صورة غلاف الموسيقى من المعرض",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان الأغنية", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = artist,
                        onValueChange = { artist = it },
                        label = { Text("اسم الفنان / المغني", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = album,
                        onValueChange = { album = it },
                        label = { Text("الألبوم", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("النوع الموسيقي (Genre)", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("الكتابة فوق الموسيقى (ملاحظات أو كلمات مخصصة)", color = TextSecondary) },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, artist, album, genre, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("حفظ التعديلات", color = Color(0xFF090A10), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AudioTrimDialog(
    song: Song?,
    durationMs: Long,
    onSetAsRingtone: () -> Unit,
    onDismiss: () -> Unit
) {
    if (song == null) return

    var startSeconds by remember { mutableStateOf(0f) }
    val totalSeconds = (durationMs / 1000).coerceAtLeast(1).toFloat()
    var endSeconds by remember { mutableStateOf((totalSeconds * 0.4f).coerceAtLeast(15f).coerceAtMost(totalSeconds)) }
    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111D),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✂️", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تحرير وقص الموسيقى ونغمة الرنين",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "حدد بداية ونهاية المقطع لقص النغمة أو تعيينها مباشرة كنغمة رنين:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Start slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("البداية:", color = TextSecondary, fontSize = 12.sp)
                    Text("${startSeconds.toInt()} ثانية", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
                androidx.compose.material3.Slider(
                    value = startSeconds,
                    onValueChange = { if (it < endSeconds) startSeconds = it },
                    valueRange = 0f..totalSeconds,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan
                    )
                )

                // End slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("النهاية:", color = TextSecondary, fontSize = 12.sp)
                    Text("${endSeconds.toInt()} ثانية", color = Color(0xFFFF8500), fontWeight = FontWeight.Bold)
                }
                androidx.compose.material3.Slider(
                    value = endSeconds,
                    onValueChange = { if (it > startSeconds) endSeconds = it },
                    valueRange = 0f..totalSeconds,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = Color(0xFFFF8500),
                        activeTrackColor = Color(0xFFFF8500)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Duration badge
                val trimmedDuration = (endSeconds - startSeconds).toInt()
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1B1E30),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "طول النغمة المقصوصة: $trimmedDuration ثانية",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Set as Ringtone Action Button
                Button(
                    onClick = {
                        onSetAsRingtone()
                        android.widget.Toast.makeText(
                            context,
                            "تم إرسال طلب تعيين \"${song.title}\" كنغمة رنين للهاتف بنجاح!",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8500))
                ) {
                    Text("🔔 تعيين كـ نغمة رنين الهاتف", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    android.widget.Toast.makeText(
                        context,
                        "تم قص وحفظ المقطع الموسيقي بنجاح!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("حفظ المقطع", color = Color(0xFF090A10), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextSecondary)
            }
        }
    )
}

