package com.stringly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PedalGearItem(
    val name: String,
    val settings: String
)

data class PedalSetupPost(
    val id: Int,
    val author: String,
    val songTitle: String,
    val artist: String,
    val toneTag: String,
    val guitarAndAmp: String,
    val pedals: List<PedalGearItem>,
    val notes: String,
    val likes: Int,
    val replies: Int,
    val isLiked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedalboardScreen(
    onBack: () -> Unit = {}
) {
    val setups = remember {
        mutableStateListOf(
            PedalSetupPost(
                id = 1,
                author = "Mateo R.",
                songTitle = "Slow Dancing in a Burning Room",
                artist = "John Mayer",
                toneTag = "Overdrive",
                guitarAndAmp = "Fender Stratocaster (Neck) -> Twin Reverb",
                pedals = listOf(
                    PedalGearItem("Tube Screamer", "Drive 3 · Tone 6 · Level 7"),
                    PedalGearItem("Bluesbreaker", "Gain 4 · Tone 5 · Vol 6"),
                    PedalGearItem("Hall Reverb", "Decay 5 · Mix 4")
                ),
                notes = "Roll guitar volume back to 8 for the intro clean breakup, then bring it to 10 on the solo.",
                likes = 428,
                replies = 39
            ),
            PedalSetupPost(
                id = 2,
                author = "Sarah K.",
                songTitle = "Cherub Rock",
                artist = "The Smashing Pumpkins",
                toneTag = "Fuzz",
                guitarAndAmp = "Stratocaster (Bridge) -> JCM800 Clean",
                pedals = listOf(
                    PedalGearItem("Big Muff Pi", "Sustain 9 · Tone 6 · Vol 7"),
                    PedalGearItem("Phase 90", "Speed 2")
                ),
                notes = "Keep the amp clean with plenty of headroom so the Big Muff mids cut through the mix.",
                likes = 312,
                replies = 24
            ),
            PedalSetupPost(
                id = 3,
                author = "David G.",
                songTitle = "Come As You Are",
                artist = "Nirvana",
                toneTag = "Chorus",
                guitarAndAmp = "Jaguar (Neck) -> Mesa Studio .22",
                pedals = listOf(
                    PedalGearItem("Small Clone", "Rate 4 · Depth Switch Up"),
                    PedalGearItem("DS-1 Distortion", "Dist 6 · Tone 5 · Level 7")
                ),
                notes = "Chorus stays on for the whole track; kick in the DS-1 only for the chorus lift.",
                likes = 264,
                replies = 18
            ),
            PedalSetupPost(
                id = 4,
                author = "Elena V.",
                songTitle = "Where the Streets Have No Name",
                artist = "U2",
                toneTag = "Ambient",
                guitarAndAmp = "Stratocaster (Pos 2) -> Vox AC30",
                pedals = listOf(
                    PedalGearItem("Flashback Delay", "Dotted 8th · Feedback 5 · FX 6"),
                    PedalGearItem("Tube Screamer", "Drive 2 · Tone 7 · Level 6")
                ),
                notes = "Set dotted-eighth delay to 126 BPM and let the AC30 chime carry the harmonics.",
                likes = 195,
                replies = 15
            )
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("All") }
    var showShareSheet by remember { mutableStateOf(false) }

    val tags = listOf("All", "Overdrive", "Fuzz", "Chorus", "Ambient", "Clean")
    val filteredSetups = setups.filter { post ->
        val matchesTag = selectedTag == "All" || post.toneTag.equals(selectedTag, ignoreCase = true)
        val query = searchQuery.trim()
        val matchesQuery = query.isEmpty() ||
            post.songTitle.contains(query, ignoreCase = true) ||
            post.artist.contains(query, ignoreCase = true) ||
            post.guitarAndAmp.contains(query, ignoreCase = true) ||
            post.pedals.any { it.name.contains(query, ignoreCase = true) }
        matchesTag && matchesQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pedalboard Setups",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showShareSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Setup",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Browse guitars, pedal chains, and knob settings shared by players for specific songs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search song, artist, guitar, or pedal...",
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
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(text = tag, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (filteredSetups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speaker,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No matching setups found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try another song or pedal name, or share your own rig.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                searchQuery = ""
                                selectedTag = "All"
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSetups, key = { it.id }) { post ->
                        PedalSetupCard(
                            post = post,
                            onToggleLike = {
                                val idx = setups.indexOfFirst { it.id == post.id }
                                if (idx != -1) {
                                    val current = setups[idx]
                                    setups[idx] = current.copy(
                                        isLiked = !current.isLiked,
                                        likes = if (current.isLiked) current.likes - 1 else current.likes + 1
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showShareSheet) {
        ShareSetupBottomSheet(
            onDismiss = { showShareSheet = false },
            onPublish = { song, artist, tone, guitarAmp, pedalSummary, notes ->
                val parsedPedals = pedalSummary.split(";")
                    .mapNotNull { entry ->
                        val parts = entry.split(":", limit = 2)
                        val name = parts.getOrNull(0)?.trim().orEmpty()
                        if (name.isEmpty()) null
                        else PedalGearItem(
                            name = name,
                            settings = parts.getOrNull(1)?.trim().orEmpty().ifEmpty { "Default settings" }
                        )
                    }
                    .ifEmpty { listOf(PedalGearItem("Direct Rig", "Standard settings")) }

                setups.add(
                    0,
                    PedalSetupPost(
                        id = (setups.maxOfOrNull { it.id } ?: 0) + 1,
                        author = "Leo (You)",
                        songTitle = song,
                        artist = artist.ifBlank { "Community Song" },
                        toneTag = tone,
                        guitarAndAmp = guitarAmp,
                        pedals = parsedPedals,
                        notes = notes.ifBlank { "Shared from my practice rig." },
                        likes = 1,
                        replies = 0,
                        isLiked = true
                    )
                )
                showShareSheet = false
            }
        )
    }
}

@Composable
private fun PedalSetupCard(
    post: PedalSetupPost,
    onToggleLike: () -> Unit
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${post.songTitle} — ${post.artist}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Shared by ${post.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = post.toneTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Guitar & Amp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = post.guitarAndAmp,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Pedal Chain & Knob Settings
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                post.pedals.forEach { pedal ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pedal.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = pedal.settings,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Text(
                text = post.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onToggleLike,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like setup",
                            tint = if (post.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.likes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${post.replies} replies",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share setup",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareSetupBottomSheet(
    onDismiss: () -> Unit,
    onPublish: (song: String, artist: String, tone: String, guitarAmp: String, pedals: String, notes: String) -> Unit
) {
    var song by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var tone by remember { mutableStateOf("Overdrive") }
    var guitarAmp by remember { mutableStateOf("") }
    var pedals by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Share Song Setup",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = song,
                onValueChange = { song = it; errorText = null },
                label = { Text("Song Title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = { Text("Artist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = guitarAmp,
                onValueChange = { guitarAmp = it; errorText = null },
                label = { Text("Guitar & Amp *") },
                placeholder = { Text("e.g., Fender Strat -> Twin Reverb") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pedals,
                onValueChange = { pedals = it },
                label = { Text("Pedals & Settings (Pedal: Settings; ...)") },
                placeholder = { Text("Tube Screamer: Drive 3 · Tone 6; Delay: Mix 4") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Playing Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (song.isBlank() || guitarAmp.isBlank()) {
                            errorText = "Please enter both the song title and your guitar/amp."
                        } else {
                            onPublish(song.trim(), artist.trim(), tone, guitarAmp.trim(), pedals.trim(), notes.trim())
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Publish Setup")
                }
            }
        }
    }
}
