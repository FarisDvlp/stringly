package com.stringly.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stringly.app.tuner.GuitarTunerEngine
import com.stringly.app.tuner.TuningStatus
import com.stringly.app.ui.theme.ElectricOrange
import com.stringly.app.ui.theme.GraphiteMute
import com.stringly.app.ui.theme.PrimaryRed
import com.stringly.app.ui.theme.StageSuccess
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class GuitarString(
    val note: String,
    val octave: Int,
    val freq: Float
)

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun TunerScreen(
    defaultTuning: String = "Standard",
    a4Calibration: String = "440 Hz"
) {
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

    val centsOffset = if (uiState.hasSignal) uiState.centsOffset else 0f

    val animatedCents by animateFloatAsState(
        targetValue = centsOffset.coerceIn(-50f, 50f),
        animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
        label = "centsBall"
    )

    val absCents = abs(animatedCents)
    val isInTune = uiState.status == TuningStatus.IN_TUNE || (uiState.hasSignal && absCents <= 5f)

    val indicatorColor = remember(absCents, uiState.status) {
        when {
            uiState.status == TuningStatus.IN_TUNE -> StageSuccess
            absCents <= 5f -> StageSuccess
            absCents <= 18f -> lerp(StageSuccess, ElectricOrange, (absCents - 5f) / 13f)
            else -> lerp(ElectricOrange, PrimaryRed, ((absCents - 18f) / 22f).coerceIn(0f, 1f))
        }
    }

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar: Tunings Icon (Left) | Active Tuning | Settings Icon (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                        if (detectAllNotes || uiState.isAutoMode) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = if (uiState.isAutoMode) "Auto Mode" else "Chromatic",
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

            // Hero Tuner Stage Card
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
                        indicatorColor = indicatorColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                    )

                    // Center Note Readout
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            val displayedNote = if (uiState.hasSignal) uiState.noteName else currentString.note
                            val displayedOctave = if (uiState.hasSignal) uiState.octave else currentString.octave

                            Text(
                                text = displayedNote,
                                fontSize = 72.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isInTune) StageSuccess else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$displayedOctave",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = indicatorColor,
                                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                            )
                        }
                        if (showAdvancedInfo) {
                            val freqText = if (uiState.hasSignal) {
                                String.format(Locale.US, "%.1f Hz (Target: %.2f Hz)", uiState.detectedFrequency, uiState.targetFrequency)
                            } else {
                                String.format(Locale.US, "Target: %.2f Hz", currentString.freq * (validBaseHz / 440f))
                            }
                            Text(
                                text = freqText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = indicatorColor.copy(alpha = 0.14f)
                        ) {
                            val statusText = when (uiState.status) {
                                TuningStatus.IN_TUNE -> String.format(Locale.US, "PERFECTLY IN TUNE (±%.0f¢)", uiState.centsOffset)
                                TuningStatus.FLAT -> String.format(Locale.US, "FLAT (%.0f¢) • TIGHTEN STRING", uiState.centsOffset)
                                TuningStatus.SHARP -> String.format(Locale.US, "SHARP (+%.0f¢) • LOOSEN STRING", uiState.centsOffset)
                                TuningStatus.NO_PERMISSION -> "MICROPHONE PERMISSION NEEDED"
                                TuningStatus.ERROR -> uiState.errorMessage ?: "MICROPHONE ERROR"
                                TuningStatus.LISTENING -> "LISTENING... PLUCK A STRING"
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = indicatorColor,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 6-String Selector Card & Auto Button
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STRINGS TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.isAutoMode) PrimaryRed else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.clickable {
                                tunerEngine.setSelectedStringIndex(-1)
                            }
                        ) {
                            Text(
                                text = "AUTO DETECT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isAutoMode) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        strings.forEachIndexed { idx, s ->
                            val label = s.note
                            val isSelected = !uiState.isAutoMode && selectedStringIndex == idx

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TuningNoteButton(
                                    label = label,
                                    isSelected = isSelected,
                                    isInTune = isInTune && isSelected,
                                    pulsingBg = pulsingBg,
                                    pulsingFg = pulsingFg,
                                    onClick = {
                                        selectedStringIndex = idx
                                        tunerEngine.setSelectedStringIndex(idx)
                                    }
                                )
                                Text(
                                    text = "${6 - idx}th",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Mic Listen Button / Permission Button
            if (!uiState.permissionGranted) {
                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    shape = RoundedCornerShape(14.dp),
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (uiState.isListening) {
                            tunerEngine.stopTuning()
                        } else {
                            tunerEngine.startTuning()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isListening) StageSuccess else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Microphone toggle",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isListening) "STOP MICROPHONE" else "START TUNER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Outside-tap scrim for open top pop-ups
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

        // Top Pop-up Card: Available Tunings
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
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant,
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
        }

        // Top Pop-up Card: Tuner Settings
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
