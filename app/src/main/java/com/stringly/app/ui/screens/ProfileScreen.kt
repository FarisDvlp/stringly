package com.stringly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    defaultTuning: String,
    onChangeDefaultTuning: (String) -> Unit,
    a4Calibration: String,
    onChangeA4Calibration: (String) -> Unit,
    onBack: () -> Unit,
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser

    var displayName by remember { mutableStateOf(currentUser?.displayName?.ifBlank { null } ?: "Musician") }
    var handle by remember { mutableStateOf(currentUser?.email ?: "@stringly.user") }
    var skillLevel by remember { mutableStateOf("Intermediate") }
    var mainGuitar by remember { mutableStateOf("Fender Player Stratocaster") }
    var mainAmp by remember { mutableStateOf("Boss Katana 50") }

    var showEditSheet by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(statusBanner) {
        if (statusBanner != null) {
            kotlinx.coroutines.delay(3000)
            statusBanner = null
        }
    }

    val tunings = listOf("Standard", "Eb Standard", "Drop D", "Drop C")
    val calibrations = listOf("432 Hz", "440 Hz", "442 Hz")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Settings",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (statusBanner != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusBanner!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { statusBanner = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Profile & Main Rig Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName.firstOrNull()?.uppercase() ?: "L",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = skillLevel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = handle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showEditSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "MY MAIN RIG",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "$mainGuitar → $mainAmp",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Saved & Community Activity
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
                    Text(
                        text = "Saved & Community",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActivityStatTile(
                            count = "5",
                            label = "Songs",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTo("tabs") }
                        )
                        ActivityStatTile(
                            count = "4",
                            label = "Setups",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTo("pedalboard") }
                        )
                        ActivityStatTile(
                            count = "4",
                            label = "Jam Posts",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTo("jam-spot") }
                        )
                    }
                }
            }

            // Practice & App Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Practice & App Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dark Stage Theme",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Low-glare palette for rehearsal rooms",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onToggleDarkTheme
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Default Tuning",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tunings.forEach { tuning ->
                                FilterChip(
                                    selected = defaultTuning == tuning,
                                    onClick = { onChangeDefaultTuning(tuning) },
                                    label = { Text(tuning, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Tuner A4 Calibration",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Standard concert pitch is 440 Hz",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            calibrations.forEach { cal ->
                                FilterChip(
                                    selected = a4Calibration == cal,
                                    onClick = { onChangeA4Calibration(cal) },
                                    label = { Text(cal, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }

            // Account Actions
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
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedButton(
                        onClick = { showPasswordDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Password")
                    }
                    Button(
                        onClick = { showLogoutDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showEditSheet) {
        EditProfileBottomSheet(
            initialName = displayName,
            initialSkill = skillLevel,
            initialGuitar = mainGuitar,
            initialAmp = mainAmp,
            onDismiss = { showEditSheet = false },
            onSave = { newName, newSkill, newGuitar, newAmp ->
                displayName = newName
                skillLevel = newSkill
                mainGuitar = newGuitar
                mainAmp = newAmp
                showEditSheet = false
                currentUser?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()
                )
                statusBanner = "Profile updated."
            }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { newPass ->
                currentUser?.updatePassword(newPass)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        statusBanner = "Password updated successfully."
                    } else {
                        statusBanner = task.exception?.localizedMessage ?: "Failed to update password."
                    }
                }
                showPasswordDialog = false
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out of Stringly?") },
            text = { Text("You will return to the login screen.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        auth.signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ActivityStatTile(
    count: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    initialName: String,
    initialSkill: String,
    initialGuitar: String,
    initialAmp: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var skill by remember { mutableStateOf(initialSkill) }
    var guitar by remember { mutableStateOf(initialGuitar) }
    var amp by remember { mutableStateOf(initialAmp) }
    var error by remember { mutableStateOf<String?>(null) }

    val levels = listOf("Beginner", "Intermediate", "Advanced")

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
                text = "Edit Profile & Rig",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; error = null },
                label = { Text("Display Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                levels.forEach { lvl ->
                    FilterChip(
                        selected = skill == lvl,
                        onClick = { skill = lvl },
                        label = { Text(lvl, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            OutlinedTextField(
                value = guitar,
                onValueChange = { guitar = it },
                label = { Text("Main Guitar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amp,
                onValueChange = { amp = it },
                label = { Text("Main Amp") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            error = "Display name cannot be empty."
                        } else {
                            onSave(
                                name.trim(),
                                skill,
                                guitar.trim().ifBlank { "Acoustic / Electric" },
                                amp.trim().ifBlank { "Practice Amp" }
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it; error = null },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = next,
                    onValueChange = { next = it; error = null },
                    label = { Text("New Password (min 6 chars)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it; error = null },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    current.isBlank() -> error = "Enter your current password."
                    next.length < 6 -> error = "New password must be at least 6 characters."
                    confirm != next -> error = "New passwords do not match."
                    else -> onConfirm(next)
                }
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
