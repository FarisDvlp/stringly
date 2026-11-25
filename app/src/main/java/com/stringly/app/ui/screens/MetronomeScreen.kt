package com.stringly.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stringly.app.metronome.controller.MetronomeController
import com.stringly.app.metronome.model.MetronomeStatus
import com.stringly.app.ui.theme.*

@Composable
fun MetronomeScreen(
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val controller = remember { MetronomeController(scope) }
    DisposableEffect(controller) {
        onDispose { controller.release() }
    }

    val state by controller.state.collectAsState()
    val isPlaying = state.status == MetronomeStatus.RUNNING
    val bpm = state.bpm
    val timeSignature = state.timeSignature.numerator
    val tempoName = state.tempoMarking

    // Pendulum visual rotation
    val pendulumRotation = remember { Animatable(0f) }
    LaunchedEffect(isPlaying, bpm) {
        if (isPlaying) {
            val halfBeatMs = (30_000 / bpm.coerceAtLeast(1)).coerceAtLeast(50)
            while (true) {
                pendulumRotation.animateTo(24f, animationSpec = tween(halfBeatMs, easing = FastOutSlowInEasing))
                pendulumRotation.animateTo(-24f, animationSpec = tween(halfBeatMs, easing = FastOutSlowInEasing))
            }
        } else {
            pendulumRotation.snapTo(0f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Precision Clock",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable {
                    when (state.timeSignature.numerator to state.timeSignature.denominator) {
                        4 to 4 -> controller.setTimeSignature(3, 4)
                        3 to 4 -> controller.setTimeSignature(6, 8)
                        6 to 8 -> controller.setTimeSignature(2, 4)
                        else -> controller.setTimeSignature(4, 4)
                    }
                }
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(
                        text = state.timeSignature.label,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Tactile Pendulum / Visualizer
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            // Visual Tick Ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F3F5))
            )

            // Pendulum Arm
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(140.dp)
                    .rotate(pendulumRotation.value)
                    .background(
                        Brush.verticalGradient(
                            listOf(PrimaryRed, ElectricOrange)
                        )
                    )
            )

            // Center Knob Readout (tap for Tap Tempo)
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { controller.tapTempo() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$bpm",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceLight
                    )
                    Text(
                        text = tempoName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryRed
                    )
                }
            }
        }

        // Beat Indicator Pips
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(timeSignature) { idx ->
                val isActive = (state.currentBeat - 1) == idx && isPlaying
                val isAccent = idx == 0
                Box(
                    modifier = Modifier
                        .size(if (isActive) 16.dp else 12.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isActive && isAccent -> ElectricOrange
                                isActive -> PrimaryRed
                                else -> Color(0xFFD9DADC)
                            }
                        )
                )
            }
        }

        // Stepper Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { controller.decreaseBpm(5) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Text(text = "−5", fontWeight = FontWeight.Bold, color = OnSurfaceLight)
            }
            IconButton(
                onClick = { controller.decreaseBpm(1) },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Text(text = "−1", fontWeight = FontWeight.Bold, color = OnSurfaceLight)
            }
            IconButton(
                onClick = { controller.increaseBpm(1) },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Text(text = "+1", fontWeight = FontWeight.Bold, color = OnSurfaceLight)
            }
            IconButton(
                onClick = { controller.increaseBpm(5) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Text(text = "+5", fontWeight = FontWeight.Bold, color = OnSurfaceLight)
            }
        }

        // Large Play / Stop CTA Button
        Button(
            onClick = { if (isPlaying) controller.stop() else controller.start() },
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) CharcoalHardware else PrimaryRed
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isPlaying) "STOP METRONOME" else "START BEAT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
