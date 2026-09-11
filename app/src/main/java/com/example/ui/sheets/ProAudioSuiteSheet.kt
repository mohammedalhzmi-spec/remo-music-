package com.example.ui.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.MicExternalOn
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcousticAmbience
import com.example.model.SpatialChamber
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
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProAudioSuiteSheet(
    selectedChamber: SpatialChamber,
    onSelectChamber: (SpatialChamber) -> Unit,
    isOrbitEnabled: Boolean,
    onToggleOrbit: (Boolean) -> Unit,
    orbitSpeed: Float,
    onOrbitSpeedChange: (Float) -> Unit,
    currentPan: Float,
    isKaraokeMode: Boolean,
    onToggleKaraoke: (Boolean) -> Unit,
    vocalAttenuateLevel: Int,
    onVocalAttenuateChange: (Int) -> Unit,
    isHapticBassEnabled: Boolean,
    onToggleHapticBass: (Boolean) -> Unit,
    selectedAmbience: AcousticAmbience,
    onSelectAmbience: (AcousticAmbience) -> Unit,
    detectedBpm: Int,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF353C58))
            )
        },
        modifier = Modifier.testTag("pro_audio_suite_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.3f), AmberGold.copy(alpha = 0.3f)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "جناح الصوت الخارق (PRO SUITE)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberGold.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                            ) {
                                Text(
                                    text = "حصري 2026",
                                    color = AmberGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "ميزات صوتية ثورية حصرية لمشغل REMOMUSIC",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 1: 3D Spatial Acoustic Chambers (محاكي البيئات والقاعات الصوتية ثلاثية الأبعاد)
            Text(
                text = "🏛️ محاكي القاعات والمسارح الصوتية ثلاثية الأبعاد (3D Chamber)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
            Text(
                text = "يحاكي الصدى الصوتي الواقعي وتوزيع الترددات في قاعات عالمية واستوديوهات فاخرة",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpatialChamber.values().forEach { chamber ->
                    val isSelected = selectedChamber == chamber
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectChamber(chamber) }
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) NeonCyan else DarkBorder,
                                shape = RoundedCornerShape(14.dp)
                            ),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.12f) else DarkSurfaceElevated,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = chamber.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chamber.arabicName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NeonCyan else TextPrimary
                                    )
                                )
                                Text(
                                    text = chamber.description,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) TextPrimary.copy(alpha = 0.8f) else TextSecondary
                                    )
                                )
                            }
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = NeonCyan,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: 8D / 16D Orbit Audio (تدوير الصوت السينمائي)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isOrbitEnabled) ElectricViolet else DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElectricViolet.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SurroundSound, contentDescription = null, tint = ElectricViolet)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تقنية التدوير المكاني 8D / 16D Audio",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "يدور الصوت حول رأسك في دائرة 360° (يُفضل استخدام سماعات الرأس)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }
                        Switch(
                            checked = isOrbitEnabled,
                            onCheckedChange = onToggleOrbit,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricViolet
                            )
                        )
                    }

                    if (isOrbitEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Orbit Visualizer Radar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F111E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxWidth().height(68.dp)) {
                                val w = size.width
                                val h = size.height
                                val centerX = w / 2f
                                val centerY = h / 2f
                                val radius = h * 0.38f

                                // Draw circular orbital trajectory
                                drawCircle(
                                    color = ElectricViolet.copy(alpha = 0.35f),
                                    radius = radius,
                                    center = Offset(centerX, centerY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                                )

                                // Listener head center
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.7f),
                                    radius = 6f,
                                    center = Offset(centerX, centerY)
                                )

                                // Sound particle position based on pan
                                val soundX = centerX + currentPan * radius * 1.6f
                                val soundY = centerY - (1f - currentPan * currentPan) * radius * 0.4f

                                drawCircle(
                                    color = NeonCyan,
                                    radius = 8f,
                                    center = Offset(soundX, soundY)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("يسار L 🎧", fontSize = 11.sp, color = TextSecondary)
                                Text("يمين R 🎧", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("سرعة الدوران المحيطي:", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text("${String.format("%.1f", orbitSpeed)}x", style = MaterialTheme.typography.bodySmall.copy(color = ElectricViolet, fontWeight = FontWeight.Bold))
                        }

                        Slider(
                            value = orbitSpeed,
                            onValueChange = onOrbitSpeedChange,
                            valueRange = 0.2f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricViolet,
                                activeTrackColor = ElectricViolet,
                                inactiveTrackColor = Color(0xFF28243C)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 3: Karaoke Vocal Remover & Isolation (عزل صوت المغني)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isKaraokeMode) NeonMagenta else DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonMagenta.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MicExternalOn, contentDescription = null, tint = NeonMagenta)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "عازل صوت المغني والكاريوكي (Vocal Remover)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "إخماد الترددات الصوتية للبشر وتحويل الأغنية إلى معزوفة موسيقية",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }
                        Switch(
                            checked = isKaraokeMode,
                            onCheckedChange = onToggleKaraoke,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonMagenta
                            )
                        )
                    }

                    if (isKaraokeMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نسبة خفض صوت الغناء:", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text("$vocalAttenuateLevel%", style = MaterialTheme.typography.bodySmall.copy(color = NeonMagenta, fontWeight = FontWeight.Bold))
                        }
                        Slider(
                            value = vocalAttenuateLevel.toFloat(),
                            onValueChange = { onVocalAttenuateChange(it.toInt()) },
                            valueRange = 20f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonMagenta,
                                activeTrackColor = NeonMagenta,
                                inactiveTrackColor = Color(0xFF331D30)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 4: Haptic Subwoofer (نبض الاهتزاز مع صوت البيس)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isHapticBassEnabled) AmberGold else DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmberGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = AmberGold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "مضخم النبض اللمسي (Haptic Bass Subwoofer)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "اهتزاز فيزيائي متزامن مع ضربات البيس في يدك كأنك أمام مكبر صوت حفلة",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                    Switch(
                        checked = isHapticBassEnabled,
                        onCheckedChange = onToggleHapticBass,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AmberGold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 5: Acoustic Nature Ambience (أصوات طبيعية مرافقة للموسيقى)
            Text(
                text = "🌿 خلفيات صوتية طبيعية مرافقة (Ambient Relax)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
            Text(
                text = "ادمج مؤثرات الطبيعة الحية مع موسيقاك لمزيد من الاسترخاء أو التركيز أو النوم",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AcousticAmbience.values().forEach { ambience ->
                    val isSelected = selectedAmbience == ambience
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectAmbience(ambience) }
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) NeonCyan else DarkBorder,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceElevated,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = ambience.icon, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ambience.arabicName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) NeonCyan else TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 6: Live BPM & Rhythm Pulse Indicator
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "سرعة النبض الموسيقي (BPM Tracker)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "تحليل ترددات الإيقاع بالذكاء الاصطناعي للمسار الحالي",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                    Text(
                        text = "$detectedBpm BPM",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = AmberGold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(34.dp))
        }
    }
}
