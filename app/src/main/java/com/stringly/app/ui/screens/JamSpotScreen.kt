package com.stringly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stringly.app.ui.theme.PrimaryRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MediaType {
    TEXT, IMAGE, AUDIO_TAB
}

data class JamReply(
    val id: String = "",
    val author: String = "",
    val authorUid: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val likedBy: List<String> = emptyList()
) {
    val formattedTime: String
        get() = formatTimestamp(createdAt)
}

data class JamPost(
    val id: String = "",
    val author: String = "",
    val authorUid: String = "",
    val title: String = "",
    val description: String = "",
    val mediaUrl: String? = null,
    val mediaType: MediaType = MediaType.TEXT,
    val likesCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val tag: String = "Technique",
    val createdAt: Long = System.currentTimeMillis(),
    val replyCount: Int = 0
) {
    val formattedTime: String
        get() = formatTimestamp(createdAt)

    fun isLikedBy(userUid: String?): Boolean {
        return userUid != null && likedBy.contains(userUid)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun JamSpotScreen() {
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val currentUid = currentUser?.uid ?: ""
    val currentUserName = remember(currentUser) {
        val name = currentUser?.displayName?.ifBlank { null }
        name?.trim() ?: currentUser?.email?.split("@")?.firstOrNull() ?: "Musician"
    }

    var posts by remember { mutableStateOf<List<JamPost>>(emptyList()) }
    var isLoadingPosts by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showComposer by remember { mutableStateOf(false) }
    var selectedPostForReplies by remember { mutableStateOf<JamPost?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val categories = listOf("All", "Media", "Tabs Included", "Technique", "Gear Rig", "Acoustic")

    var firestoreErrorMessage by remember { mutableStateOf<String?>(null) }

    // Real-time Firestore Snapshot Listener for Posts
    DisposableEffect(Unit) {
        val listener = db.collection("jam_posts")
            .addSnapshotListener { snapshot, error ->
                isLoadingPosts = false
                if (error != null) {
                    firestoreErrorMessage = error.localizedMessage
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Firestore: ${error.localizedMessage}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val newPosts = snapshot.documents.mapNotNull { doc ->
                    try {
                        val mediaTypeStr = doc.getString("mediaType") ?: "TEXT"
                        val mediaType = try { MediaType.valueOf(mediaTypeStr) } catch (e: Exception) { MediaType.TEXT }
                        @Suppress("UNCHECKED_CAST")
                        val likedByList = (doc.get("likedBy") as? List<String>) ?: emptyList()

                        JamPost(
                            id = doc.id,
                            author = doc.getString("author") ?: "Anonymous",
                            authorUid = doc.getString("authorUid") ?: "",
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            mediaUrl = doc.getString("mediaUrl"),
                            mediaType = mediaType,
                            likesCount = (doc.getLong("likesCount") ?: 0L).toInt(),
                            likedBy = likedByList,
                            tag = doc.getString("tag") ?: "Technique",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            replyCount = (doc.getLong("replyCount") ?: 0L).toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }

                posts = newPosts
            }

        onDispose {
            listener.remove()
        }
    }

    val filteredPosts = remember(posts, selectedFilter, searchQuery) {
        posts.filter { post ->
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Media" -> !post.mediaUrl.isNullOrEmpty() || post.mediaType != MediaType.TEXT
                else -> post.tag.equals(selectedFilter, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                    post.title.contains(searchQuery, ignoreCase = true) ||
                    post.description.contains(searchQuery, ignoreCase = true) ||
                    post.author.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Jam Spot Forum",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time community licks, gear & media",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showComposer = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New Post",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search topics, licks, gear...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedFilter == cat,
                        onClick = { selectedFilter = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Posts Feed
            if (isLoadingPosts) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "No posts in Jam Spot yet." else "No posts found for '$searchQuery'",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Be the first to start a topic or share a lick!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        val isLikedByMe = post.isLikedBy(currentUid)

                        JamPostCard(
                            post = post,
                            currentUid = currentUid,
                            isLikedByMe = isLikedByMe,
                            onToggleLike = {
                                if (currentUid.isBlank()) return@JamPostCard
                                val postRef = db.collection("jam_posts").document(post.id)
                                if (isLikedByMe) {
                                    postRef.update(
                                        "likedBy", FieldValue.arrayRemove(currentUid),
                                        "likesCount", FieldValue.increment(-1)
                                    )
                                } else {
                                    postRef.update(
                                        "likedBy", FieldValue.arrayUnion(currentUid),
                                        "likesCount", FieldValue.increment(1)
                                    )
                                }
                            },
                            onOpenReplies = {
                                selectedPostForReplies = post
                            },
                            onShare = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Link to '${post.title}' copied!")
                                }
                            },
                            onDeletePost = {
                                if (post.authorUid == currentUid) {
                                    db.collection("jam_posts").document(post.id).delete()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // New Post Composer Bottom Sheet
    if (showComposer) {
        CreatePostBottomSheet(
            onDismiss = { showComposer = false },
            onPostCreated = { title, desc, mediaUrl, mediaType, tag ->
                val newPostData = hashMapOf(
                    "title" to title,
                    "description" to desc,
                    "author" to currentUserName,
                    "authorUid" to currentUid,
                    "mediaUrl" to mediaUrl,
                    "mediaType" to mediaType.name,
                    "tag" to tag,
                    "likesCount" to 0,
                    "likedBy" to emptyList<String>(),
                    "replyCount" to 0,
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("jam_posts").add(newPostData)
                    .addOnSuccessListener {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Post published successfully!")
                        }
                    }
                    .addOnFailureListener { e ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Failed to post: ${e.localizedMessage}")
                        }
                    }
                showComposer = false
            }
        )
    }

    // Replies & Comments Bottom Sheet
    selectedPostForReplies?.let { activePost ->
        RepliesBottomSheet(
            post = activePost,
            currentUid = currentUid,
            currentUserName = currentUserName,
            db = db,
            onDismiss = { selectedPostForReplies = null }
        )
    }
}

@Composable
private fun JamPostCard(
    post: JamPost,
    currentUid: String,
    isLikedByMe: Boolean,
    onToggleLike: () -> Unit,
    onOpenReplies: () -> Unit,
    onShare: () -> Unit,
    onDeletePost: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.author.firstOrNull()?.uppercase() ?: "M",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column {
                        Text(
                            text = post.author,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = post.formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = post.tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (post.authorUid == currentUid && currentUid.isNotBlank()) {
                        IconButton(
                            onClick = onDeletePost,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete post",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Content
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Attached Media Preview
            if (!post.mediaUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                if (post.mediaType == MediaType.IMAGE) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(post.mediaUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Post image attachment",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else if (post.mediaType == MediaType.AUDIO_TAB) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play audio clip",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Audio Lick / Tab Sample",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "0:30 • 44.1 kHz FLAC",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(8.dp))

            // Action bar: Likes, Comments, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Like button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleLike() }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like post",
                            tint = if (isLikedByMe) PrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${post.likesCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isLikedByMe) FontWeight.Bold else FontWeight.Medium,
                            color = if (isLikedByMe) PrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Reply / Comment button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenReplies() }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Replies",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${post.replyCount} replies",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share post",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostBottomSheet(
    onDismiss: () -> Unit,
    onPostCreated: (title: String, desc: String, mediaUrl: String?, mediaType: MediaType, tag: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("Technique") }
    var selectedMediaType by remember { mutableStateOf(MediaType.TEXT) }
    var mediaUrl by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val tags = listOf("Technique", "Tabs Included", "Gear Rig", "Acoustic", "Lick Video")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Post to Jam Spot Forum",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; error = null },
                label = { Text("Title *") },
                placeholder = { Text("e.g. Neo-Soul chord progression lick") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Content
            OutlinedTextField(
                value = body,
                onValueChange = { body = it; error = null },
                label = { Text("Description / Notes / Tab *") },
                placeholder = { Text("Share your lick, equipment setup, or question...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Select Tag
            Text(
                text = "Category Tag",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { t ->
                    FilterChip(
                        selected = tag == t,
                        onClick = { tag = t },
                        label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Media Type Selection
            Text(
                text = "Attach Media (Optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = selectedMediaType == MediaType.TEXT,
                    onClick = { selectedMediaType = MediaType.TEXT; mediaUrl = "" },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Text Only") }
                )
                FilterChip(
                    selected = selectedMediaType == MediaType.IMAGE,
                    onClick = {
                        selectedMediaType = MediaType.IMAGE
                        if (mediaUrl.isBlank()) {
                            mediaUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=800&q=80"
                        }
                    },
                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Photo") }
                )
                FilterChip(
                    selected = selectedMediaType == MediaType.AUDIO_TAB,
                    onClick = {
                        selectedMediaType = MediaType.AUDIO_TAB
                        mediaUrl = "https://sample.audio"
                    },
                    leadingIcon = { Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Audio Lick") }
                )
            }

            if (selectedMediaType == MediaType.IMAGE) {
                OutlinedTextField(
                    value = mediaUrl,
                    onValueChange = { mediaUrl = it },
                    label = { Text("Image URL") },
                    placeholder = { Text("https://example.com/image.jpg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (error != null) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (title.isBlank() || body.isBlank()) {
                            error = "Title and description cannot be empty."
                        } else {
                            onPostCreated(
                                title.trim(),
                                body.trim(),
                                mediaUrl.trim().ifBlank { null },
                                selectedMediaType,
                                tag
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Publish Post")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepliesBottomSheet(
    post: JamPost,
    currentUid: String,
    currentUserName: String,
    db: FirebaseFirestore,
    onDismiss: () -> Unit
) {
    var replies by remember { mutableStateOf<List<JamReply>>(emptyList()) }
    var isLoadingReplies by remember { mutableStateOf(true) }
    var replyInput by remember { mutableStateOf("") }
    var replyError by remember { mutableStateOf<String?>(null) }

    // Real-time listener for replies
    DisposableEffect(post.id) {
        val listener = db.collection("jam_posts")
            .document(post.id)
            .collection("replies")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                isLoadingReplies = false
                if (error != null || snapshot == null) return@addSnapshotListener

                val newReplies = snapshot.documents.mapNotNull { doc ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val likedByList = (doc.get("likedBy") as? List<String>) ?: emptyList()
                        JamReply(
                            id = doc.id,
                            author = doc.getString("author") ?: "Anonymous",
                            authorUid = doc.getString("authorUid") ?: "",
                            content = doc.getString("content") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            likesCount = (doc.getLong("likesCount") ?: 0L).toInt(),
                            likedBy = likedByList
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                replies = newReplies
            }

        onDispose {
            listener.remove()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Discussion & Replies",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Summary of Original Post
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Posted by ${post.author} • ${post.tag}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Replies List
            if (isLoadingReplies) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (replies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No replies yet. Be the first to start the discussion!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(replies, key = { it.id }) { reply ->
                        val isReplyLikedByMe = currentUid.isNotBlank() && reply.likedBy.contains(currentUid)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = reply.author.firstOrNull()?.uppercase() ?: "U",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        Text(
                                            text = reply.author,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = reply.formattedTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = reply.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                if (currentUid.isBlank()) return@clickable
                                                val replyRef = db.collection("jam_posts")
                                                    .document(post.id)
                                                    .collection("replies")
                                                    .document(reply.id)

                                                if (isReplyLikedByMe) {
                                                    replyRef.update(
                                                        "likedBy", FieldValue.arrayRemove(currentUid),
                                                        "likesCount", FieldValue.increment(-1)
                                                    )
                                                } else {
                                                    replyRef.update(
                                                        "likedBy", FieldValue.arrayUnion(currentUid),
                                                        "likesCount", FieldValue.increment(1)
                                                    )
                                                }
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isReplyLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Like reply",
                                            tint = if (isReplyLikedByMe) PrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "${reply.likesCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isReplyLikedByMe) PrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reply input bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = replyInput,
                    onValueChange = { replyInput = it; replyError = null },
                    placeholder = { Text("Write a reply...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
                )

                IconButton(
                    onClick = {
                        if (replyInput.isNotBlank()) {
                            val text = replyInput.trim()
                            replyInput = ""

                            val newReplyData = hashMapOf(
                                "author" to currentUserName,
                                "authorUid" to currentUid,
                                "content" to text,
                                "createdAt" to System.currentTimeMillis(),
                                "likesCount" to 0,
                                "likedBy" to emptyList<String>()
                            )

                            db.collection("jam_posts")
                                .document(post.id)
                                .collection("replies")
                                .add(newReplyData)

                            db.collection("jam_posts")
                                .document(post.id)
                                .update("replyCount", FieldValue.increment(1))
                        } else {
                            replyError = "Reply cannot be empty."
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send reply",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (replyError != null) {
                Text(
                    text = replyError!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}
