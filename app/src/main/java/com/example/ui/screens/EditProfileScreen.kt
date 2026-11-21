package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocationHelper
import com.example.data.RentalRepository
import com.example.data.models.AVATAR_PRESETS
import com.example.ui.components.UserAvatar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val authPhotoUrl = remember {
        Firebase.auth.currentUser?.photoUrl?.toString()?.trim()?.takeIf { it.isNotBlank() }?.take(500)
    }

    var displayName by remember(currentProfile) { mutableStateOf(currentProfile.displayName) }
    var bio by remember(currentProfile) { mutableStateOf(currentProfile.bio) }
    var location by remember(currentProfile) { mutableStateOf(currentProfile.location) }
    var phone by remember(currentProfile) { mutableStateOf(currentProfile.phone) }
    var selectedAvatarIndex by remember(currentProfile) { mutableIntStateOf(currentProfile.avatarPresetIndex) }
    var selectedAvatarUri by remember(currentProfile) { mutableStateOf(currentProfile.customAvatarUri) }
    var isLocatingGps by remember { mutableStateOf(false) }

    val detectLocation: () -> Unit = {
        isLocatingGps = true
        LocationHelper.fetchCurrentLocation(
            context = context,
            onLocationReceived = { lat, lng ->
                coroutineScope.launch {
                    val resolved = LocationHelper.reverseGeocodeLocation(context, lat, lng)
                    if (!resolved.isNullOrBlank()) {
                        location = resolved
                    }
                    isLocatingGps = false
                }
            },
            onLocationUnavailable = { isLocatingGps = false }
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) detectLocation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("edit_profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("edit_profile_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Choose Avatar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            val avatarPresetColors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1D4ED8),
                Color(0xFF065F46),
                Color(0xFFC2410C),
                Color(0xFF5B21B6),
                Color(0xFF9F1239)
            )
            val previewInitials = remember(displayName) {
                displayName.trim()
                    .split(Regex("\\s+"))
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .joinToString("")
                    .ifEmpty { "?" }
            }
            val effectivePreviewUrl = remember(selectedAvatarUri, authPhotoUrl) {
                when {
                    selectedAvatarUri == null -> authPhotoUrl
                    selectedAvatarUri.isNullOrBlank() -> null
                    else -> selectedAvatarUri
                }
            }

            UserAvatar(
                avatarUrl = effectivePreviewUrl,
                initials = previewInitials,
                size = 80.dp,
                avatarPresetIndex = selectedAvatarIndex,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                borderWidth = 1.dp,
                contentDescription = "Profile avatar preview",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(6.dp))
            val activePresetLabel = AVATAR_PRESETS.getOrElse(selectedAvatarIndex % AVATAR_PRESETS.size) { AVATAR_PRESETS[0] }.label
            Text(
                text = when {
                    !effectivePreviewUrl.isNullOrBlank() -> "Using your email account photo"
                    authPhotoUrl == null -> "No email photo available — using $activePresetLabel"
                    else -> "Using $activePresetLabel initials preset"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                if (authPhotoUrl != null) {
                    val isEmailSelected = !effectivePreviewUrl.isNullOrBlank()
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { selectedAvatarUri = authPhotoUrl }
                            .testTag("avatar_option_email")
                    ) {
                        UserAvatar(
                            avatarUrl = authPhotoUrl,
                            initials = previewInitials,
                            size = 48.dp,
                            avatarPresetIndex = selectedAvatarIndex,
                            borderColor = if (isEmailSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            borderWidth = if (isEmailSelected) 3.dp else 1.dp,
                            contentDescription = "Use email profile photo"
                        )
                        if (isEmailSelected) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                for ((index, color) in avatarPresetColors.withIndex()) {
                    val isSelected = effectivePreviewUrl.isNullOrBlank() && selectedAvatarIndex == index
                    val itemMod = if (isSelected) {
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = itemMod
                            .clickable {
                                selectedAvatarIndex = index
                                selectedAvatarUri = ""
                            }
                            .testTag("avatar_option_$index")
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Display Name",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_display_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bio",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_bio_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location / Neighborhood",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = {
                        if (LocationHelper.hasLocationPermission(context)) {
                            detectLocation()
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.testTag("edit_profile_detect_gps_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Detect GPS",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(text = if (isLocatingGps) "Locating..." else "Use Live GPS")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("Neighborhood or city") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_location_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Phone Number",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_phone_input")
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("cancel_edit_button")
                ) {
                    Text(text = "Cancel")
                }

                Button(
                    onClick = {
                        val persistedAvatarUri = when {
                            selectedAvatarUri == null -> authPhotoUrl ?: ""
                            else -> selectedAvatarUri?.trim().orEmpty()
                        }
                        val updated = currentProfile.copy(
                            displayName = displayName.trim(),
                            bio = bio.trim(),
                            location = location.trim(),
                            phone = phone.trim(),
                            avatarPresetIndex = selectedAvatarIndex,
                            customAvatarUri = persistedAvatarUri
                        )
                        RentalRepository.updateUserProfile(updated)
                        onNavigateBack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("save_profile_button")
                ) {
                    Text(
                        text = "Save Profile",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
