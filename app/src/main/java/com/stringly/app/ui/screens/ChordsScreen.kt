package com.stringly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.stringly.app.ui.components.GuitarFretboardDiagram
import com.stringly.app.ui.theme.StageSuccess

data class ChordItem(
    val name: String,
    val suffix: String,
    val formula: String,
    val frets: List<Int>,
    val fingering: String,
    val tag: String
)

@Composable
fun ChordsScreen() {
    val roots = listOf("C", "D", "E", "F", "G", "A", "B")
    var selectedRoot by remember { mutableStateOf("C") }

    val chordList = remember {
        listOf(
            ChordItem("C", "Major", "1 - 3 - 5", listOf(-1, 3, 2, 0, 1, 0), "x-3-2-0-1-0", "Standard Open"),
            ChordItem("Cadd9", "Add 9", "1 - 3 - 5 - 9", listOf(-1, 3, 2, 0, 3, 0), "x-3-2-0-3-0", "Rock Ballad"),
            ChordItem("Cmaj7", "Major 7th", "1 - 3 - 5 - 7", listOf(-1, 3, 2, 0, 0, 0), "x-3-2-0-0-0", "Lush Jazz"),
            ChordItem("C7", "Dominant 7", "1 - 3 - 5 - b7", listOf(-1, 3, 2, 3, 1, 0), "x-3-2-3-1-0", "Blues Core"),
            ChordItem("Cm", "Minor", "1 - b3 - 5", listOf(-1, 3, 5, 5, 4, 3), "x-3-5-5-4-3", "Barre Fret 3")
        )
    }

    var selectedChord by remember { mutableStateOf(chordList[1]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Text(
            text = "Chord Library",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Root Note Selector Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(roots) { root ->
                val isSelected = root == selectedRoot
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedRoot = root }
                ) {
                    Text(
                        text = root,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Featured Chord Breakdown with Visual Fretboard
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedChord.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StageSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = selectedChord.tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StageSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = "${selectedChord.suffix} • Formula: ${selectedChord.formula} • ${selectedChord.fingering}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Shared Visual Guitar Fretboard
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GuitarFretboardDiagram(
                        frets = selectedChord.frets,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    )
                }
            }
        }

        // Voicing Variations List
        Text(
            text = "Variations & Extensions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        chordList.forEach { chord ->
            val isCurrent = chord.name == selectedChord.name
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedChord = chord },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = chord.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = chord.suffix,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${chord.fingering} • ${chord.tag}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Select ${chord.name}",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
