package com.example.ui.sheets

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioEngine
import com.example.model.EqualizerPreset
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(
    currentPresetId: String,
    bandLevels: List<Int>,
    bassBoost: Int,
    virtualizer: Int,
    isAutoAdaptive: Boolean,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onBandChange: (Int, Int) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onToggleAutoAdaptive: (Boolean) -> Unit,
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
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF323854))
            )
        },
        modifier = Modifier.testTag("equalizer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "المعادل الصوتي الاحترافي (Equalizer)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "معالجة صوتية دقيقة ونقاء استوديو فائق",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-Adaptive EQ Toggle Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isAutoAdaptive) NeonCyan.copy(alpha = 0.12f) else DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAutoAdaptive) NeonCyan else DarkBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_adaptive_eq_toggle")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "المعادل التلقائي الذكي (Auto-Adaptive EQ)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoAdaptive) NeonCyan else TextPrimary
                            )
                        )
                        Text(
                            text = "ضبط الترددات تلقائياً وفق نوع الموسيقى والحالة المزاجية للأغنية الحالية",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = isAutoAdaptive,
                        onCheckedChange = onToggleAutoAdaptive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF090A10),
                            checkedTrackColor = NeonCyan,
                            uncheckedTrackColor = Color(0xFF20253B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // EQ Frequency Curve Graphic
            Text(
                text = "منحنى الاستجابة الترددية (DSP Frequency Response)",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            EqCurveVisualizer(
                bandLevels = bandLevels,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preset Chips
            Text(
                text = "الإعدادات المسبقة (Presets)",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AudioEngine.PresetList.forEach { preset ->
                    val isSelected = currentPresetId == preset.id
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectPreset(preset) }
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else DarkBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("preset_${preset.id}"),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceElevated
                    ) {
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else TextPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5 Manual Bands Sliders
            Text(
                text = "التحكم اليدوي في نطاقات التردد (5 Bands -12dB ~ +12dB)",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val freqLabels = listOf("60 Hz\nباس عميق", "230 Hz\nباس متوسط", "910 Hz\nأصوات وميد", "3.6 kHz\nوضوح", "14 kHz\nتريبل وهواء")

            for (i in 0 until 5) {
                val db = bandLevels.getOrElse(i) { 0 }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = freqLabels[i],
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp),
                        modifier = Modifier.width(76.dp)
                    )

                    Slider(
                        value = db.toFloat(),
                        onValueChange = { onBandChange(i, it.toInt()) },
                        valueRange = -12f..12f,
                        steps = 23,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color(0xFF22263F)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("band_slider_$i")
                    )

                    Text(
                        text = "${if (db > 0) "+" else ""}$db dB",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (db != 0) NeonCyan else TextTertiary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.width(52.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bass Boost & Virtualizer Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bass Boost Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bass Boost",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonMagenta
                                )
                            )
                            Text(
                                text = "$bassBoost%",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonMagenta
                                )
                            )
                        }
                        Text(
                            text = "تضخيم الباس والجهير",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        Slider(
                            value = bassBoost.toFloat(),
                            onValueChange = { onBassBoostChange(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonMagenta,
                                activeTrackColor = NeonMagenta,
                                inactiveTrackColor = Color(0xFF272035)
                            ),
                            modifier = Modifier.testTag("bass_boost_slider")
                        )
                    }
                }

                // Virtualizer / Spatial Audio Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3D Virtualizer",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricViolet
                                )
                            )
                            Text(
                                text = "$virtualizer%",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricViolet
                                )
                            )
                        }
                        Text(
                            text = "صوت محيطي سينمائي",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        Slider(
                            value = virtualizer.toFloat(),
                            onValueChange = { onVirtualizerChange(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricViolet,
                                activeTrackColor = ElectricViolet,
                                inactiveTrackColor = Color(0xFF251E38)
                            ),
                            modifier = Modifier.testTag("virtualizer_slider")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EqCurveVisualizer(
    bandLevels: List<Int>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Draw central 0dB reference line
        drawLine(
            color = Color(0xFF252A42),
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = 1.5f
        )

        val points = mutableListOf<Offset>()
        for (i in 0 until 5) {
            val db = bandLevels.getOrElse(i) { 0 }
            val x = (width / 6f) * (i + 1)
            // -12 dB -> bottom, +12 dB -> top
            val y = midY - (db / 12f) * (height * 0.4f)
            points.add(Offset(x, y))
        }

        val path = Path()
        if (points.isNotEmpty()) {
            path.moveTo(0f, midY)
            path.lineTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX = (p0.x + p1.x) / 2f
                path.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
            }
            path.lineTo(width, midY)
        }

        // Stroke curve
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet, NeonMagenta)),
            style = Stroke(width = 3.5f)
        )

        // Draw dots on key band frequencies
        points.forEach { pt ->
            drawCircle(color = NeonCyan, radius = 4.5f, center = pt)
        }
    }
}
