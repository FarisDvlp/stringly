package com.stringly.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stringly.app.tuner.GuitarTunerConstants
import com.stringly.app.tuner.GuitarTunerEngine
import com.stringly.app.tuner.TuningStatus
import com.stringly.app.ui.theme.*
import java.util.Locale
import com.stringly.app.ui.theme.ElectricOrange
import com.stringly.app.ui.theme.GraphiteMute
import com.stringly.app.ui.theme.PrimaryRed
import com.stringly.app.ui.theme.StageSuccess
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun TunerScreen(
    defaultTuning: String = "Standard",
    a4Calibration: String = "440 Hz"
) {
    var activeTuning by remember(defaultTuning) { mutableStateOf(defaultTuning) }
    val initialHz = remember(a4Calibration) {
        a4Calibration.filter { it.isDigit() }.ifBlank { "440" }
    }
    var baseFreqInput by remember(initialHz) { mutableStateOf(initialHz) }
    var baseFreqError by remember { mutableStateOf<String?>(null) }
    val validBaseHz = baseFreqInput.toIntOrNull()?.takeIf { it in 380..480 } ?: 440

    var showTuningsPopup by remember { mutableStateOf(false) }
    var showSettingsPopup by remember { mutableStateOf(false) }

    var detectAllNotes by remember { mutableStateOf(false) }
    var showAdvancedInfo by remember { mutableStateOf(true) }
    var tuneByEar by remember { mutableStateOf(false) }

    val availableTunings = listOf("Standard", "Eb Standard", "Drop D", "Drop C", "Open G", "DADGAD")

    val strings = remember(activeTuning) {
        when (activeTuning) {
            "Eb Standard" -> listOf(
                GuitarString("Eb", 2, 77.78f),
                GuitarString("Ab", 2, 103.83f),
                GuitarString("Db", 3, 138.59f),
                GuitarString("Gb", 3, 185.00f),
                GuitarString("Bb", 3, 233.08f),
                GuitarString("Eb", 4, 311.13f)
            )
            "Drop D" -> listOf(
                GuitarString("D", 2, 73.42f),
                GuitarString("A", 2, 110.00f),
                GuitarString("D", 3, 146.83f),
                GuitarString("G", 3, 196.00f),
                GuitarString("B", 3, 246.94f),
                GuitarString("E", 4, 329.63f)
            )
            "Drop C" -> listOf(
                GuitarString("C", 2, 65.41f),
                GuitarString("G", 2, 98.00f),
                GuitarString("C", 3, 130.81f),
                GuitarString("F", 3, 174.61f),
                GuitarString("A", 3, 220.00f),
                GuitarString("D", 4, 293.66f)
            )
            "Open G" -> listOf(
                GuitarString("D", 2, 73.42f),
                GuitarString("G", 2, 98.00f),
                GuitarString("D", 3, 146.83f),
                GuitarString("G", 3, 196.00f),
                GuitarString("B", 3, 246.94f),
                GuitarString("D", 4, 293.66f)
            )
            "DADGAD" -> listOf(
                GuitarString("D", 2, 73.42f),
                GuitarString("A", 2, 110.00f),
                GuitarString("D", 3, 146.83f),
                GuitarString("G", 3, 196.00f),
                GuitarString("A", 3, 220.00f),
                GuitarString("D", 4, 293.66f)
            )
            else -> listOf(
                GuitarString("E", 2, 82.41f),
                GuitarString("A", 2, 110.00f),
                GuitarString("D", 3, 146.83f),
                GuitarString("G", 3, 196.00f),
                GuitarString("B", 3, 246.94f),
                GuitarString("E", 4, 329.63f)
            )
        }
    }

    var selectedStringIndex by remember { mutableIntStateOf(0) }
    val currentString = strings[selectedStringIndex]

    var centsOffset by remember { mutableFloatStateOf(-14f) }
    var isMicListening by remember { mutableStateOf(false) }

    LaunchedEffect(isMicListening, selectedStringIndex) {
        if (isMicListening) {
            kotlinx.coroutines.delay(900)
            centsOffset = 0f
        }
    }

    val animatedCents by animateFloatAsState(
        targetValue = centsOffset.coerceIn(-50f, 50f),
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "centsBall"
    )

    val absCents = abs(animatedCents)
    val isInTune = absCents <= 3f

    // Continuous color gradient transition: Green (0) -> Orange (15) -> Red (35+)
    val indicatorColor = remember(absCents) {
        when {
            absCents <= 3f -> StageSuccess
            absCents <= 18f -> lerp(StageSuccess, ElectricOrange, (absCents - 3f) / 15f)
            else -> lerp(ElectricOrange, PrimaryRed, ((absCents - 18f) / 22f).coerceIn(0f, 1f))
        }
    }

    // Brand-cohesive pulse for active string button while tuning (soft primary tint <-> solid primary)
    val pulseTransition = rememberInfiniteTransition(label = "pegNotePulse")
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val pulsingBg by pulseTransition.animateColor(
        initialValue = primaryColor.copy(alpha = 0.20f),
        targetValue = primaryColor,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingBg"
    )
    val pulsingFg by pulseTransition.animateColor(
        initialValue = primaryColor,
        targetValue = onPrimaryColor,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingFg"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GUITAR TUNER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = if (uiState.isAutoMode) "Auto Tuning" else "Manual String Tuning",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = "A4 = 440 Hz",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = GraphiteMute
                )
            }
        }

        // Circular Gauge Meter
        Box(
            modifier = Modifier
                .size(270.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            // Gauge Dial Ring with scale marks
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 24.dp.toPx()

                // Draw background arc scale (-60 deg to +60 deg)
                val totalTicks = 20
                for (i in 0..totalTicks) {
                    val angleDeg = -60f + (120f / totalTicks) * i
                    val angleRad = Math.toRadians(angleDeg.toDouble() - 90).toFloat()

                    val isMajor = i % 5 == 0
                    val tickLength = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                    val strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx()

                    val startX = center.x + (radius - tickLength) * cos(angleRad)
                    val startY = center.y + (radius - tickLength) * sin(angleRad)
                    val endX = center.x + radius * cos(angleRad)
                    val endY = center.y + radius * sin(angleRad)

                    val tickColor = when {
                        i == totalTicks / 2 -> StageSuccess
                        isMajor -> GraphiteMute
                        else -> Color(0xFFCCCCCC)
                    }

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeWidth
                    )
                }
            }

            // Outer ring accent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F4F6))
            )

            // Needle Pointer
            val needleColor = when (uiState.status) {
                TuningStatus.IN_TUNE -> StageSuccess
                TuningStatus.FLAT, TuningStatus.SHARP -> PrimaryRed
                else -> GraphiteMute
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(115.dp)
                    .rotate(needleRotation.value)
                    .background(needleColor, shape = RoundedCornerShape(2.dp))
            )

            // Center Readout
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        showTuningsPopup = !showTuningsPopup
                        if (showTuningsPopup) showSettingsPopup = false
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Available tunings",
                        tint = if (showTuningsPopup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                                                text = activeTuning,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!uiState.isAutoMode || detectAllNotes) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "Chromatic",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            if (showAdvancedInfo) {
                Text(
                    text = "A4 = $validBaseHz Hz",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(
            onClick = {
                showSettingsPopup = !showSettingsPopup
                if (showSettingsPopup) showTuningsPopup = false
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Tuner settings",
                tint = if (showSettingsPopup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }

    // Hero Tuner Stage Card (fills remaining vertical space cleanly)
    val isInTune = uiState.status == TuningStatus.IN_TUNE
    val activeIndicatorColor = when {
        !uiState.hasSignal && uiState.isAutoMode -> MaterialTheme.colorScheme.onSurfaceVariant
        isInTune -> StageSuccess
        else -> indicatorColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            ArcPitchMeter(
                cents = animatedCents,
                indicatorColor = activeIndicatorColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
            )

            // Center Note Readout (Merged Backend uiState + Main UI Styling)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    val displayNote = if (uiState.hasSignal || !uiState.isAutoMode) {
                        if (selectedStringIndex == 5 && uiState.noteName == "E") "e" else uiState.noteName
                    } else {
                        "—"
                    }

                    Text(
                        text = displayNote,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isInTune) StageSuccess else MaterialTheme.colorScheme.onSurface
                    )

                    if (uiState.hasSignal || !uiState.isAutoMode) {
                        Text(
                            text = "${uiState.octave}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeIndicatorColor,
                            modifier = Modifier.padding(bottom = 14.dp, start = 4.dp)
                        )
                    }
                }

                // Detected Frequency & Target Frequency from Backend
                Text(
                    text = if (uiState.hasSignal) {
                        String.format(Locale.US, "%.1f Hz", uiState.detectedFrequency)
                    } else {
                        "— Hz"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (showAdvancedInfo) {
                    Text(
                        text = String.format(Locale.US, "Target: %.2f Hz", uiState.targetFrequency),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tuning Status Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = activeIndicatorColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = when {
                            !uiState.hasSignal && uiState.isAutoMode -> "Listening..."
                            isInTune -> "In Tune"
                            animatedCents < 0f -> "Flat — Tune Up"
                            else -> "Sharp — Tune Down"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = activeIndicatorColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // 6-String Selector Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "STRINGS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        strings.forEachIndexed { idx, s ->
                            val label = if (idx == 5 && s.note == "E") "e" else s.note
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TuningNoteButton(
                                    label = label,
                                    isSelected = selectedStringIndex == idx,
                                    isInTune = isInTune,
                                    pulsingBg = pulsingBg,
                                    pulsingFg = pulsingFg,
                                    onClick = {
                                        selectedStringIndex = idx
                                        centsOffset = if (tuneByEar) 0f else listOf(-28f, -12f, 9f, 24f).random()
                                    }
                                )
                                Text(
                                    text = "${6 - idx}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

        // Tuning Status Badge
        val statusBgColor = when (uiState.status) {
            TuningStatus.IN_TUNE -> StageSuccess.copy(alpha = 0.15f)
            TuningStatus.FLAT, TuningStatus.SHARP -> PrimaryRed.copy(alpha = 0.15f)
            TuningStatus.NO_PERMISSION, TuningStatus.ERROR -> PrimaryRed.copy(alpha = 0.15f)
            TuningStatus.LISTENING -> Color(0xFFF0F0F3)
        }

        val statusTextColor = when (uiState.status) {
            TuningStatus.IN_TUNE -> StageSuccess
            TuningStatus.FLAT, TuningStatus.SHARP -> PrimaryRed
            TuningStatus.NO_PERMISSION, TuningStatus.ERROR -> PrimaryRed
            TuningStatus.LISTENING -> GraphiteMute
        }

        val statusIcon = when (uiState.status) {
            TuningStatus.IN_TUNE -> Icons.Default.CheckCircle
            TuningStatus.FLAT -> Icons.Default.ArrowDownward
            TuningStatus.SHARP -> Icons.Default.ArrowUpward
            TuningStatus.NO_PERMISSION -> Icons.Default.MicOff
            TuningStatus.ERROR -> Icons.Default.Warning
            TuningStatus.LISTENING -> Icons.Default.GraphicEq
        }

        val statusText = when (uiState.status) {
            TuningStatus.IN_TUNE -> String.format(Locale.US, "PERFECTLY IN TUNE (%.0f¢)", uiState.centsOffset)
            TuningStatus.FLAT -> String.format(Locale.US, "FLAT (%.0f¢) • TIGHTEN STRING", uiState.centsOffset)
            TuningStatus.SHARP -> String.format(Locale.US, "SHARP (+%.0f¢) • LOOSEN STRING", uiState.centsOffset)
            TuningStatus.NO_PERMISSION -> "MICROPHONE PERMISSION NEEDED"
            TuningStatus.ERROR -> uiState.errorMessage ?: "MICROPHONE ERROR"
            TuningStatus.LISTENING -> "LISTENING... PLUCK A STRING"
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = statusBgColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusTextColor,
                    modifier = Modifier.size(18.dp)
            // Bottom Mic Listen Button
            Button(
                onClick = {
                    isMicListening = !isMicListening
                    if (!isMicListening) {
                        centsOffset = -14f
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMicListening) StageSuccess else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = if (isMicListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Microphone toggle",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusTextColor
                )
            }
        }

        // Real-Time Waveform & Signal Strength Card (from Back-End_Faishal)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDIO SIGNAL & WAVEFORM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GraphiteMute,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Level",
                            fontSize = 10.sp,
                            color = GraphiteMute
                        )
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFE5E5E8))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(uiState.signalStrength)
                                    .background(if (uiState.hasSignal) StageSuccess else PrimaryRed)
                            )
                        }
                    }
                }

                // Waveform Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF9F9FB)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val points = uiState.waveformPoints
                        if (points.isNotEmpty()) {
                            val path = Path()
                            val width = size.width
                            val height = size.height
                            val centerY = height / 2f
                            val stepX = width / (points.size - 1).coerceAtLeast(1)

                            points.forEachIndexed { i, sample ->
                                val x = i * stepX
                                val y = centerY - (sample * (height / 2f - 4.dp.toPx()))
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(
                                path = path,
                                color = if (uiState.hasSignal) StageSuccess else PrimaryRed.copy(alpha = 0.7f),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        } else {
                            // Flat line when silent
                            drawLine(
                                color = Color(0xFFDDDDDD),
                                start = Offset(0f, size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }
                }
            }
        }

        // Quick String Selector & Auto Toggle Bar (from Back-End_Faishal)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT STRING TARGET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GraphiteMute,
                    letterSpacing = 1.sp
                )
                // AUTO Mode Pill Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (uiState.isAutoMode) PrimaryRed else Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.clickable { tunerEngine.setSelectedStringIndex(-1) }
                ) {
                    Text(
                        text = "AUTO DETECT",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (uiState.isAutoMode) Color.White else OnSurfaceLight
                    )
                }
            }
        }
    } // <-- Closes the main screen Column (from main)

    // Outside-tap scrim for open top pop-ups (from main)
    if (showTuningsPopup || showSettingsPopup) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable {
                    showTuningsPopup = false
                    showSettingsPopup = false
                }
        )
    }

    // Top Pop-up Card: Available Tunings (from main)
    AnimatedVisibility(
        visible = showTuningsPopup,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 72.dp, start = 16.dp, end = 16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Tunings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { showTuningsPopup = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                availableTunings.forEach { tuning ->
                    val selected = tuning == activeTuning
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeTuning = tuning
                                showTuningsPopup = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tuning,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                }
            }

            // 6 Strings Quick Selector Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GuitarTunerConstants.STANDARD_STRINGS.forEachIndexed { idx, s ->
                    val isSelected = !uiState.isAutoMode && uiState.selectedStringIndex == idx

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            tunerEngine.setSelectedStringIndex(idx)
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryRed else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = s.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else OnSurfaceLight
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${s.stringNumber}th",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GraphiteMute
                        )
                    }
                }
            }
        }

// Permission / Listening Action Button
            if (!uiState.permissionGranted) {
                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Grant Mic",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GRANT MICROPHONE PERMISSION",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = {
                        if (uiState.isListening) {
                            tunerEngine.stopTuning()
                        } else {
                            tunerEngine.startTuning()
                        }
                    },
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isListening) Icons.Default.Pause else Icons.Default.Mic,
                        contentDescription = "Mic State",
                        tint = PrimaryRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isListening) "STOP MICROPHONE" else "START TUNER",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Top Pop-up Card: Tuner Settings (with typeable Base Frequency)
        AnimatedVisibility(
            visible = showSettingsPopup,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tuner Settings",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showSettingsPopup = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = baseFreqInput,
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(3)
                            baseFreqInput = digits
                            val parsed = digits.toIntOrNull()
                            baseFreqError = if (parsed == null || parsed !in 380..480) {
                                "Enter a frequency between 380 and 480 Hz"
                            } else {
                                null
                            }
                        },
                        label = { Text("Base Frequency (A4 Hz)") },
                        suffix = { Text("Hz") },
                        isError = baseFreqError != null,
                        supportingText = {
                            Text(baseFreqError ?: "Concert pitch default is 440 Hz")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    SettingSwitchRow(
                        title = "Detect All Notes",
                        subtitle = "Chromatic auto-detect across any pitch",
                        checked = detectAllNotes,
                        onCheckedChange = { detectAllNotes = it }
                    )
                    SettingSwitchRow(
                        title = "Advanced Info",
                        subtitle = "Show target Hz & A4 calibration",
                        checked = showAdvancedInfo,
                        onCheckedChange = { showAdvancedInfo = it }
                    )
                    SettingSwitchRow(
                        title = "Tune by Ear",
                        subtitle = "Play reference pitch when tapping a note",
                        checked = tuneByEar,
                        onCheckedChange = { tuneByEar = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArcPitchMeter(
    cents: Float,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val trackColor = GraphiteMute.copy(alpha = 0.72f)

    Canvas(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        val ballRadius = 18.dp.toPx()
        val trackWidth = ballRadius * 2f
        val arcWidth = size.width
        val arcHeight = size.height * 1.45f
        val topLeft = Offset(0f, size.height * 0.30f)

        // Solid grey arc track matching the indicator ball's diameter
        drawArc(
            color = trackColor,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(arcWidth, arcHeight),
            style = Stroke(width = trackWidth, cap = StrokeCap.Round)
        )

        val rx = arcWidth / 2f
        val ry = arcHeight / 2f
        val cx = topLeft.x + rx
        val cy = topLeft.y + ry

        // Flat (♭) inside left end of the meter in white
        val flatRad = Math.toRadians(205.0)
        val flatX = cx + rx * cos(flatRad).toFloat()
        val flatY = cy + ry * sin(flatRad).toFloat()
        val flatMeasured = textMeasurer.measure(
            text = "♭",
            style = TextStyle(
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Black
            )
        )
        drawText(
            textLayoutResult = flatMeasured,
            topLeft = Offset(
                flatX - flatMeasured.size.width / 2f,
                flatY - flatMeasured.size.height / 2f
            )
        )

        // Sharp (♯) inside right end of the meter in white
        val sharpRad = Math.toRadians(335.0)
        val sharpX = cx + rx * cos(sharpRad).toFloat()
        val sharpY = cy + ry * sin(sharpRad).toFloat()
        val sharpMeasured = textMeasurer.measure(
            text = "♯",
            style = TextStyle(
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Black
            )
        )
        drawText(
            textLayoutResult = sharpMeasured,
            topLeft = Offset(
                sharpX - sharpMeasured.size.width / 2f,
                sharpY - sharpMeasured.size.height / 2f
            )
        )

        // Center 0-cent subtle white reference tick
        val centerRad = Math.toRadians(270.0)
        val tickX = cx + rx * cos(centerRad).toFloat()
        val tickY = cy + ry * sin(centerRad).toFloat()
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(tickX, tickY - ballRadius * 0.65f),
            end = Offset(tickX, tickY + ballRadius * 0.65f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Sliding indicator ball (mapped across the inner arc range so it doesn't cover ♭/♯ at rest)
        val t = ((cents + 50f) / 100f).coerceIn(0f, 1f)
        val ballAngleDeg = 213f + t * 114f
        val ballAngleRad = Math.toRadians(ballAngleDeg.toDouble())
        val ballX = cx + rx * cos(ballAngleRad).toFloat()
        val ballY = cy + ry * sin(ballAngleRad).toFloat()

        drawCircle(
            color = indicatorColor,
            radius = ballRadius,
            center = Offset(ballX, ballY)
        )

        // Cent offset number centered inside the indicator ball
        val centsInt = cents.toInt()
        val label = if (centsInt > 0) "+$centsInt" else "$centsInt"
        val measured = textMeasurer.measure(
            text = label,
            style = TextStyle(
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                ballX - measured.size.width / 2f,
                ballY - measured.size.height / 2f
            )
        )
    }
}

@Composable
private fun TuningNoteButton(
    label: String,
    isSelected: Boolean,
    isInTune: Boolean,
    pulsingBg: Color,
    pulsingFg: Color,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected && isInTune -> StageSuccess
        isSelected -> pulsingBg
        else -> MaterialTheme.colorScheme.surface
    }
    val fgColor = when {
        isSelected && isInTune -> Color.White
        isSelected -> pulsingFg
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected && isInTune) StageSuccess else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = fgColor
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
    }
}
