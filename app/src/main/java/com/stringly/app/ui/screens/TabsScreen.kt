package com.stringly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.stringly.app.musicchord.autoscroll.AutoScrollController
import com.stringly.app.musicchord.model.ChordLine
import com.stringly.app.musicchord.model.Song
import com.stringly.app.musicchord.repository.SongRepositoryImpl
import com.stringly.app.musicchord.transpose.ChordTransposer
import com.stringly.app.ui.theme.AmberGold
import com.stringly.app.ui.theme.StageSuccess

@Composable
fun TabsScreen(
    onOpenPedalboard: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember(context) { SongRepositoryImpl(context) }
    val scope = rememberCoroutineScope()
    val autoScrollController = remember { AutoScrollController(scope) }

    DisposableEffect(autoScrollController) {
        onDispose { autoScrollController.release() }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("All") }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var transposeSemitones by remember { mutableIntStateOf(0) }

    LaunchedEffect(repository) {
        val loaded = repository.getAllSongs()
        songs = loaded
        if (selectedSong == null) {
            selectedSong = loaded.firstOrNull()
        }
    }

    val previewScrollState = rememberScrollState()
    val autoScrollState by autoScrollController.uiState.collectAsState()

    LaunchedEffect(autoScrollController) {
        autoScrollController.scrollDeltaFlow.collect { delta ->
            previewScrollState.scrollBy(delta)
        }
    }

    val transposedSong = remember(selectedSong, transposeSemitones) {
        selectedSong?.let { ChordTransposer.transposeSong(it, transposeSemitones) }
    }

    val difficulties = listOf("All", "EASY", "MED", "HARD")

    val filteredSongs = songs.filter { song ->
        val matchesDiff = selectedDifficulty == "All" || song.difficulty.equals(selectedDifficulty, ignoreCase = true)
        val q = searchQuery.trim()
        val matchesQuery = q.isEmpty() ||
            song.title.contains(q, ignoreCase = true) ||
            song.artist.contains(q, ignoreCase = true) ||
            song.tuning.contains(q, ignoreCase = true) ||
            song.chordSheet.sections.any { sec ->
                sec.lines.any { line ->
                    line.chords.any { it.chord.contains(q, ignoreCase = true) }
                }
            }
        matchesDiff && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Songs",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Search chords, lyrics, tuning, and tempo by song",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search song, artist, chord, or tuning...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Difficulty Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            difficulties.forEach { diff ->
                FilterChip(
                    selected = selectedDifficulty == diff,
                    onClick = { selectedDifficulty = diff },
                    label = { Text(diff, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        // Active Song Chord & Lyric Sheet
        transposedSong?.let { activeTransposed ->
            val activeSong = activeTransposed.song
            val activeChords = activeTransposed.transposedChordSheet.sections
                .flatMap { it.lines }
                .flatMap { it.chords }
                .map { it.chord }
                .distinct()

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
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activeSong.title} — ${activeSong.artist}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val bpmText = activeSong.bpm?.let { " • $it BPM" } ?: ""
                            Text(
                                text = "${activeSong.tuning}$bpmText • Key: ${activeTransposed.transposedKey} • ${activeSong.capoLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = activeSong.difficulty,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Transpose & Auto-Scroll Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { transposeSemitones -= 1 },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Transpose down", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = if (transposeSemitones == 0) "Transpose: 0" else "Transpose: ${if (transposeSemitones > 0) "+$transposeSemitones" else transposeSemitones}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FilledTonalIconButton(
                            onClick = { transposeSemitones += 1 },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Transpose up", modifier = Modifier.size(16.dp))
                        }
                        if (transposeSemitones != 0) {
                            TextButton(onClick = { transposeSemitones = 0 }) {
                                Text("Reset", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        FilledTonalButton(
                            onClick = {
                                if (autoScrollState.isScrolling) {
                                    autoScrollController.pause()
                                } else if (autoScrollState.isPaused) {
                                    autoScrollController.resume()
                                } else {
                                    autoScrollController.start()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = if (autoScrollState.isScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Auto-scroll",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (autoScrollState.isScrolling) "Pause" else "Auto-Scroll",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (autoScrollState.isScrolling || autoScrollState.isPaused) {
                            FilledTonalIconButton(
                                onClick = { autoScrollController.stop() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop auto-scroll", modifier = Modifier.size(16.dp))
                            }
                        }
                        AssistChip(
                            onClick = {
                                val nextSpeed = when (autoScrollState.speedMultiplier) {
                                    0.5f -> 1.0f
                                    1.0f -> 1.5f
                                    1.5f -> 2.0f
                                    else -> 0.5f
                                }
                                autoScrollController.setSpeed(nextSpeed)
                            },
                            label = {
                                Text("${autoScrollState.speedMultiplier}x", style = MaterialTheme.typography.labelSmall)
                            }
                        )
                    }

                    // Chords used in this song
                    if (activeChords.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeChords.forEach { chord ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = chord,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Chord + Lyric Preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 220.dp)
                                .verticalScroll(previewScrollState)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            activeTransposed.transposedChordSheet.sections.forEach { section ->
                                Text(
                                    text = "[${section.name}]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace
                                )
                                section.lines.forEach { line ->
                                    val chordLineStr = formatChordLine(line)
                                    if (chordLineStr.isNotEmpty()) {
                                        Text(
                                            text = chordLineStr,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (line.lyric.isNotEmpty()) {
                                        Text(
                                            text = line.lyric,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenPedalboard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speaker,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Community Pedalboard Setups")
                    }
                }
            }
        }

        // Song Catalog List
        Text(
            text = "Song Catalog",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No matching songs found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try searching another song, artist, chord, or tuning.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            searchQuery = ""
                            selectedDifficulty = "All"
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset filters")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSongs, key = { it.id }) { song ->
                    val isSelected = song.id == selectedSong?.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSong = song
                                transposeSemitones = 0
                                autoScrollController.stop()
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val bpmLabel = song.bpm?.let { " • $it BPM" } ?: ""
                                Text(
                                    text = "${song.artist} • ${song.tuning}$bpmLabel",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (song.difficulty) {
                                    "EASY" -> StageSuccess.copy(alpha = 0.15f)
                                    "MED" -> AmberGold.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = song.difficulty,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (song.difficulty) {
                                        "EASY" -> StageSuccess
                                        "MED" -> AmberGold
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatChordLine(line: ChordLine): String {
    if (line.chords.isEmpty()) return ""
    val sb = StringBuilder()
    for (placement in line.chords.sortedBy { it.position }) {
        val targetPos = placement.position.coerceAtLeast(0)
        while (sb.length < targetPos) {
            sb.append(' ')
        }
        if (sb.isNotEmpty() && sb.last() != ' ') {
            sb.append(' ')
        }
        sb.append(placement.chord)
    }
    return sb.toString()
}
