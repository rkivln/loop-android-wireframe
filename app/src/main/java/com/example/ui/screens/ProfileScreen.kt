package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.data.models.AVATAR_PRESETS
import com.example.data.models.UserProfile
import com.example.ui.components.LoopPrimaryButton
import com.example.ui.components.LoopSecondaryButton
import com.example.ui.theme.AccentCobalt
import com.example.ui.theme.CanvasDark
import com.example.ui.theme.CanvasGround
import com.example.ui.theme.CanvasSubtle
import com.example.ui.theme.CanvasWhite
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LineHairline
import com.example.ui.theme.RatingAmber
import com.example.ui.theme.StatusLive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onNavigateBookings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    var isEditingProfile by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasGround)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Identity & Profile",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = "Verified equipment credentials & community activity",
                            fontSize = 12.sp,
                            color = InkSecondary
                        )
                    }

                    // Edit Profile Action Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CanvasDark)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White),
                                onClick = { isEditingProfile = true }
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("edit_profile_header_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "EDIT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Success feedback toast banner if recently saved
            if (saveSuccessMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusLive.copy(alpha = 0.1f))
                            .border(1.dp, StatusLive.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusLive,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = saveSuccessMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusLive
                            )
                        }
                    }
                }
            }

            // User Identity Dossier (Avatar, Name, Handle, Rating, Verified Badge)
            item {
                val currentPreset = AVATAR_PRESETS.getOrElse(userProfile.avatarPresetIndex) { AVATAR_PRESETS[0] }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CanvasWhite)
                        .border(1.dp, LineHairline, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Monogram Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CanvasDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.initials,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.displayName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (userProfile.isVerified) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verified Identity",
                                        tint = AccentCobalt,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = InkMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = userProfile.location,
                                    fontSize = 12.sp,
                                    color = InkSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = RatingAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${userProfile.rating} (${userProfile.reviewCount} reviews)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = InkPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "· ${userProfile.memberSince}",
                                    fontSize = 11.5.sp,
                                    color = InkMuted
                                )
                            }
                        }
                    }
                }
            }

            // User Bio Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CanvasWhite)
                        .border(1.dp, LineHairline, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "// CREATOR BIO & HARDWARE FOCUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp,
                                color = InkMuted
                            )

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit bio",
                                tint = InkMuted,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { isEditingProfile = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (userProfile.bio.isNotBlank()) userProfile.bio else "No bio added yet. Tap EDIT to document your equipment setup.",
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = if (userProfile.bio.isNotBlank()) InkPrimary else InkMuted,
                            fontStyle = if (userProfile.bio.isBlank()) FontStyle.Italic else FontStyle.Normal
                        )
                    }
                }
            }

            // Trust Badges Strip
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "COMMUNITY TRUST BADGES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp,
                        color = InkMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        userProfile.badges.forEach { badge ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CanvasWhite)
                                    .border(1.dp, LineHairline, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = InkPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Tabular Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        count = "${userProfile.rentalsCompleted}",
                        label = "RENTALS",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        count = "${userProfile.listingsCount}",
                        label = "LISTINGS",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        count = "₹${userProfile.earnedAmount}",
                        label = "EARNED",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Contact & Verification Details Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CanvasWhite)
                        .border(1.dp, LineHairline, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "// VERIFIED CONTACT RECORD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                            color = InkMuted
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = InkMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = userProfile.email,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = InkPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = InkMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = userProfile.phone,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = InkPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VERIFIED ✓",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = StatusLive
                            )
                        }
                    }
                }
            }

            // Profile Navigation Menu Options
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CanvasWhite)
                        .border(1.dp, LineHairline, RoundedCornerShape(8.dp))
                ) {
                    ProfileMenuRow(
                        icon = Icons.Default.ShoppingBag,
                        title = "My Rental Bookings",
                        badge = "1 Active",
                        onClick = onNavigateBookings
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Saved Hardware",
                        badge = "3 items"
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.Security,
                        title = "Security Deposit & KYC Ledger",
                        badge = "₹0 Hold"
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.Notifications,
                        title = "Alerts & Notifications",
                        badge = "Live"
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.Lock,
                        title = "Handover Protocol & Terms"
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.HelpOutline,
                        title = "Community Help & Support"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }

        // Edit Profile Sheet Modal
        AnimatedVisibility(
            visible = isEditingProfile,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            EditProfileModalSheet(
                currentProfile = userProfile,
                onSave = { updatedProfile ->
                    RentalRepository.updateUserProfile(updatedProfile)
                    isEditingProfile = false
                    saveSuccessMessage = "Profile updated and synchronized."
                    coroutineScope.launch {
                        delay(3000)
                        saveSuccessMessage = null
                    }
                },
                onDismiss = { isEditingProfile = false }
            )
        }
    }
}

@Composable
fun EditProfileModalSheet(
    currentProfile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var displayName by remember { mutableStateOf(currentProfile.displayName) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var phone by remember { mutableStateOf(currentProfile.phone) }
    var location by remember { mutableStateOf(currentProfile.location) }
    var selectedPresetIndex by remember { mutableIntStateOf(currentProfile.avatarPresetIndex) }
    var isSaving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(CanvasWhite)
                .clickable(enabled = false) {}
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Modal Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Edit Profile Record",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            Text(
                                text = "Changes synchronize with Firestore cloud ledger",
                                fontSize = 12.sp,
                                color = InkSecondary
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = InkSecondary
                            )
                        }
                    }
                }

                // Display Name Input
                item {
                    Column {
                        Text(
                            text = "DISPLAY NAME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = InkMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CanvasSubtle)
                                .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = InkPrimary,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(CanvasDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_display_name_input")
                            )
                        }
                    }
                }

                // Bio Input (Multiline)
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BIO & GEAR FOCUS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = InkMuted
                            )
                            Text(
                                text = "${bio.length}/180",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = InkMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CanvasSubtle)
                                .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            BasicTextField(
                                value = bio,
                                onValueChange = { if (it.length <= 180) bio = it },
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = InkPrimary,
                                    lineHeight = 18.sp
                                ),
                                cursorBrush = SolidColor(CanvasDark),
                                decorationBox = { inner ->
                                    if (bio.isEmpty()) {
                                        Text(
                                            text = "Tell neighbors about cameras, audio, or tools you share...",
                                            fontSize = 13.sp,
                                            color = InkMuted
                                        )
                                    }
                                    inner()
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("edit_bio_input")
                            )
                        }
                    }
                }

                // Phone & Location Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PHONE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = InkMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CanvasSubtle)
                                    .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    textStyle = TextStyle(fontSize = 13.sp, color = InkPrimary),
                                    cursorBrush = SolidColor(CanvasDark),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("edit_phone_input")
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LOCATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = InkMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CanvasSubtle)
                                    .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    textStyle = TextStyle(fontSize = 13.sp, color = InkPrimary),
                                    cursorBrush = SolidColor(CanvasDark),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("edit_location_input")
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LoopSecondaryButton(
                            text = "CANCEL",
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        )

                        LoopPrimaryButton(
                            text = if (isSaving) "SAVING..." else "SAVE PROFILE",
                            onClick = {
                                isSaving = true
                                coroutineScope.launch {
                                    delay(400)
                                    val sanitizedName = displayName.trim().take(60).ifBlank { "Gokulan R" }
                                    val sanitizedBio = bio.trim().take(200)
                                    val sanitizedPhone = phone.filter { it.isDigit() || it == '+' || it == ' ' || it == '-' }.take(20).ifBlank { "+91 98401 22345" }
                                    val sanitizedLocation = location.trim().take(80).ifBlank { "White Town, Puducherry" }
                                    val updated = currentProfile.copy(
                                        displayName = sanitizedName,
                                        bio = sanitizedBio,
                                        phone = sanitizedPhone,
                                        location = sanitizedLocation,
                                        avatarPresetIndex = selectedPresetIndex.coerceIn(0, 5)
                                    )
                                    onSave(updated)
                                    isSaving = false
                                }
                            },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1.3f),
                            testTag = "save_profile_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CanvasWhite)
            .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = InkMuted
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = InkPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = InkPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CanvasSubtle)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = InkSecondary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = InkMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
