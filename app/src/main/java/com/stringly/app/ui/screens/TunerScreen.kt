package com.stringly.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stringly.app.tuner.GuitarTunerConstants
import com.stringly.app.tuner.GuitarTunerEngine
import com.stringly.app.tuner.TuningStatus
import com.stringly.app.ui.theme.*
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun TunerScreen() {
    val context = LocalContext.current
    val tunerEngine = remember { GuitarTunerEngine(context) }
    val uiState by tunerEngine.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tunerEngine.startTuning()
        }
    }

    DisposableEffect(Unit) {
        if (tunerEngine.checkPermission()) {
            tunerEngine.startTuning()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        onDispose {
            tunerEngine.release()
        }
    }

    // Needle rotation animation (-50 cents maps to -55 degrees, +50 cents maps to +55 degrees)
    val needleRotation = remember { Animatable(0f) }

    LaunchedEffect(uiState.centsOffset, uiState.hasSignal) {
        val targetAngle = if (uiState.hasSignal) {
            (uiState.centsOffset / 50f * 55f).coerceIn(-65f, 65f)
        } else {
            0f
        }
        needleRotation.animateTo(targetAngle, animationSpec = tween(120))
    }

    val scrollState = rememberScrollState()

    Column(
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (uiState.hasSignal || !uiState.isAutoMode) uiState.noteName else "—",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurfaceLight
                        )
                        if (uiState.hasSignal || !uiState.isAutoMode) {
                            Text(
                                text = "${uiState.octave}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.status == TuningStatus.IN_TUNE) StageSuccess else PrimaryRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (uiState.hasSignal) {
                            String.format(Locale.US, "%.1f Hz", uiState.detectedFrequency)
                        } else {
                            "— Hz"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceLight
                    )

                    Text(
                        text = String.format(Locale.US, "Target: %.2f Hz", uiState.targetFrequency),
                        fontSize = 10.sp,
                        color = GraphiteMute
                    )
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
                )
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusTextColor
                )
            }
        }

        // Real-Time Waveform & Signal Strength Card
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

        // Quick String Selector & Auto Toggle Bar
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
                    modifier = Modifier.clickable {
                        tunerEngine.setSelectedStringIndex(-1)
                    }
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
}
