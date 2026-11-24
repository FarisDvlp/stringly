package com.stringly.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.stringly.app.ui.components.GuitarFretboardDiagram
import com.stringly.app.ui.theme.AmberGold
import com.stringly.app.ui.theme.StageSuccess
import java.util.Calendar

@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit
) {
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val firstName = remember(currentUser) {
        val name = currentUser?.displayName?.ifBlank { null }
        name?.trim()?.split("\\s+".toRegex())?.firstOrNull() ?: "Musician"
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }

    var bpm by remember { mutableIntStateOf(120) }
    var isMetronomePlaying by remember { mutableStateOf(false) }
    var currentBeat by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(isMetronomePlaying, bpm) {
        if (isMetronomePlaying) {
            val interval = 60_000L / bpm
            while (true) {
                kotlinx.coroutines.delay(interval)
                currentBeat = (currentBeat + 1) % 4
            }
        } else {
            currentBeat = 0
        }
    }

    // Subtle live pulse for Tuner indicator
    val infiniteTransition = rememberInfiniteTransition(label = "tunerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Greeting + Profile Avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$greeting, $firstName",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ready for today's session",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                    .clickable { onNavigateTo("profile") },
                contentAlignment = Alignment.Center
            ) {
                if (firstName.isNotBlank() && firstName != "Musician") {
                    Text(
                        text = firstName.first().uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Open profile and settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Quick Tuner & Metronome (Equal-height, aligned 2-up utility cards)
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 12 }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tuner Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onNavigateTo("tuner") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TUNER",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(StageSuccess.copy(alpha = pulseAlpha))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Standard",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "E A D G B E",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Open Tuner",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Metronome Card
                val beatButtonColor by animateColorAsState(
                    targetValue = if (isMetronomePlaying) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                    animationSpec = tween(200),
                    label = "beatButtonColor"
                )

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onNavigateTo("metronome") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "METRONOME",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(4) { i ->
                                    val active = currentBeat == i && isMetronomePlaying
                                    val pipSize by animateDpAsState(
                                        targetValue = if (active) 9.dp else 6.dp,
                                        animationSpec = tween(100),
                                        label = "pipSize"
                                    )
                                    val pipColor by animateColorAsState(
                                        targetValue = if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                        animationSpec = tween(100),
                                        label = "pipColor"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(pipSize)
                                            .clip(CircleShape)
                                            .background(pipColor)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "$bpm BPM",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "4/4 Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { if (bpm > 40) bpm -= 2 }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("−", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { if (bpm < 240) bpm += 2 }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("+", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = beatButtonColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isMetronomePlaying = !isMetronomePlaying }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isMetronomePlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isMetronomePlaying) "Pause metronome" else "Play metronome",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isMetronomePlaying) "Stop" else "Start Beat",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pedalboard Setups (Unique route not in bottom bar)
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(320, delayMillis = 60)) + slideInVertically(tween(320, delayMillis = 60)) { it / 12 }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo("pedalboard") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speaker,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Pedalboard Setups",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Community gear & knob settings by song",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Pedalboard Setups",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Chord of the Day with Visual Guitar Fretboard
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(360, delayMillis = 120)) + slideInVertically(tween(360, delayMillis = 120)) { it / 12 }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo("chords") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Cadd9",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Open voicing • x-3-2-0-3-0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Chord of the Day",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Visual Guitar Fretboard Diagram
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GuitarFretboardDiagram(
                            frets = listOf(-1, 3, 2, 0, 3, 0), // Cadd9: x-3-2-0-3-0
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        )
                    }
                }
            }
        }

        // Song of the Week
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400, delayMillis = 180)) + slideInVertically(tween(400, delayMillis = 180)) { it / 12 }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            text = "Song of the Week",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "MED",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Wish You Were Here",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pink Floyd • Key of G • 60 BPM",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { onNavigateTo("tabs") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Open Song Sheet",
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
