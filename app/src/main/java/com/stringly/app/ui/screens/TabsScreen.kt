package com.stringly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stringly.app.ui.components.GuitarFretboardDiagram
import com.stringly.app.ui.theme.AmberGold
import com.stringly.app.ui.theme.StageSuccess

data class SongSheet(
    val id: Int,
    val title: String,
    val artist: String,
    val difficulty: String,
    val key: String,
    val bpm: Int,
    val timeSignature: String,
    val tuning: String,
    val chords: List<String>,
    val chordSheetLines: List<String>
)

@Composable
fun TabsScreen(
    onOpenPedalboard: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("All") }

    val songs = remember {
        listOf(
            SongSheet(
                id = 1,
                title = "Wish You Were Here",
                artist = "Pink Floyd",
                difficulty = "MED",
                key = "G Major",
                bpm = 60,
                timeSignature = "4/4",
                tuning = "Standard (E A D G B E)",
                chords = listOf("Em7", "G", "C", "D", "Am"),
                chordSheetLines = listOf(
                    "[Verse 1]",
                    "C                        D",
                    "  So, so you think you can tell",
                    "             Am                 G",
                    "Heaven from Hell, blue skies from pain."
                )
            ),
            SongSheet(
                id = 2,
                title = "Blackbird",
                artist = "The Beatles",
                difficulty = "EASY",
                key = "G Major",
                bpm = 94,
                timeSignature = "4/4",
                tuning = "Standard (E A D G B E)",
                chords = listOf("G", "Am7", "G/B", "C", "A7"),
                chordSheetLines = listOf(
                    "[Verse 1]",
                    "G         Am7       G/B",
                    "Blackbird singing in the dead of night",
                    "C         A7        D7",
                    "Take these broken wings and learn to fly."
                )
            ),
            SongSheet(
                id = 3,
                title = "Slow Dancing in a Burning Room",
                artist = "John Mayer",
                difficulty = "MED",
                key = "C#m",
                bpm = 66,
                timeSignature = "6/8",
                tuning = "Standard (E A D G B E)",
                chords = listOf("C#m7", "A", "E", "B"),
                chordSheetLines = listOf(
                    "[Chorus]",
                    "A                 E",
                    "It's not a silly little moment,",
                    "C#m7              B",
                    "It's not the storm before the calm."
                )
            ),
            SongSheet(
                id = 4,
                title = "Little Wing",
                artist = "Jimi Hendrix",
                difficulty = "HARD",
                key = "Em",
                bpm = 71,
                timeSignature = "4/4",
                tuning = "Eb Standard",
                chords = listOf("Em", "G", "Am", "Bm", "C", "D"),
                chordSheetLines = listOf(
                    "[Verse 1]",
                    "Em          G",
                    "Well she's walking through the clouds",
                    "Am          Em",
                    "With a circus mind that's running wild."
                )
            ),
            SongSheet(
                id = 5,
                title = "Neon",
                artist = "John Mayer",
                difficulty = "HARD",
                key = "Cm",
                bpm = 102,
                timeSignature = "4/4",
                tuning = "Drop C",
                chords = listOf("Cm7", "Eb", "F", "Ab", "Bb"),
                chordSheetLines = listOf(
                    "[Intro / Main Riff]",
                    "Cm7        Eb/G       F",
                    "When the sky turns neon blue..."
                )
            )
        )
    }

    var selectedSong by remember { mutableStateOf(songs.first()) }
    val difficulties = listOf("All", "EASY", "MED", "HARD")

    val filteredSongs = songs.filter { song ->
        val matchesDiff = selectedDifficulty == "All" || song.difficulty == selectedDifficulty
        val q = searchQuery.trim()
        val matchesQuery = q.isEmpty() ||
            song.title.contains(q, ignoreCase = true) ||
            song.artist.contains(q, ignoreCase = true) ||
            song.tuning.contains(q, ignoreCase = true) ||
            song.chords.any { it.contains(q, ignoreCase = true) }
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
                            text = "${selectedSong.title} — ${selectedSong.artist}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${selectedSong.tuning} • ${selectedSong.bpm} BPM (${selectedSong.timeSignature}) • ${selectedSong.key}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = selectedSong.difficulty,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Chords used in this song
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedSong.chords.forEach { chord ->
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

                // Chord + Lyric Preview
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        selectedSong.chordSheetLines.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
                    val isSelected = song.id == selectedSong.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSong = song },
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
                                Text(
                                    text = "${song.artist} • ${song.tuning} • ${song.bpm} BPM",
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
