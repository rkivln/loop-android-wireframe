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
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.data.models.AVATAR_PRESETS
import com.example.data.models.UserProfile
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandStarAmber
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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
            .background(BackgroundLight)
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
                            text = "Profile",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Manage identity, bio & cloud preferences",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Edit Profile Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BrandDark)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White),
                                onClick = { isEditingProfile = true }
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("edit_profile_header_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Profile",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandGreen.copy(alpha = 0.15f))
                            .border(1.dp, BrandGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BrandGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = saveSuccessMessage ?: "",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandGreen
                            )
                        }
                    }
                }
            }

            // User Identity Card (Avatar, Name, Handle, Rating, Verified Badge)
            item {
                val currentPreset = AVATAR_PRESETS.getOrElse(userProfile.avatarPresetIndex) { AVATAR_PRESETS[0] }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dynamic Avatar with Gradient and Emoji/Initials
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(elevation = 6.dp, shape = CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(currentPreset.bgHex1),
                                            Color(currentPreset.bgHex2)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.initials,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.displayName,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (userProfile.isVerified) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verified Identity",
                                        tint = BrandBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = userProfile.location,
                                    fontSize = 12.5.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = BrandStarAmber,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${userProfile.rating} (${userProfile.reviewCount} reviews)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "· ${userProfile.memberSince}",
                                    fontSize = 11.5.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // User Bio Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "About Me & Equipment",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit bio",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(15.dp)
                                    .clickable { isEditingProfile = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (userProfile.bio.isNotBlank()) userProfile.bio else "No bio added yet. Tap Edit Profile to add a bio.",
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            color = if (userProfile.bio.isNotBlank()) TextPrimary else TextMuted,
                            fontStyle = if (userProfile.bio.isBlank()) FontStyle.Italic else FontStyle.Normal
                        )
                    }
                }
            }

            // Trust Badges Carousel
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Loop Trust Badges",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        userProfile.badges.forEach { badge ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue
                                )
                            }
                        }
                    }
                }
            }

            // Quick Metrics Row (Rentals, Listings, Earned)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        count = "${userProfile.rentalsCompleted}",
                        label = "Rentals Done",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        count = "${userProfile.listingsCount}",
                        label = "My Listings",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        count = "₹${userProfile.earnedAmount}",
                        label = "Earned",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Contact & Verification Details Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Verified Contact Info",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = userProfile.email,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = userProfile.phone,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verified ✓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen
                            )
                        }
                    }
                }
            }

            // Profile Action Menu List
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(vertical = 4.dp)
                ) {
                    ProfileMenuRow(
                        icon = Icons.Default.ShoppingBag,
                        title = "My Rental Bookings",
                        badge = "1 Active",
                        onClick = onNavigateBookings
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Saved & Wishlist",
                        badge = "3 items"
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.Security,
                        title = "Security Deposit & Wallet",
                        badge = "₹0 Hold"
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications & Alerts",
                        badge = "On"
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.Lock,
                        title = "Privacy, Terms & KYC Safety"
                    )
                    ProfileMenuRow(
                        icon = Icons.Default.HelpOutline,
                        title = "Help Center & Community Guidelines"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        // Interactive Edit Profile Bottom Sheet Modal
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
                    saveSuccessMessage = "Profile updated & saved to Firestore cloud!"
                    coroutineScope.launch {
                        delay(3500)
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
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
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
                                text = "Edit User Profile",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Changes synchronize with Firestore cloud",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                // Avatar Preset Selector
                item {
                    Column {
                        Text(
                            text = "Choose Profile Avatar Style",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AVATAR_PRESETS.forEach { preset ->
                                val isSelected = selectedPresetIndex == preset.id
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(preset.bgHex1),
                                                    Color(preset.bgHex2)
                                                )
                                            )
                                        )
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) BrandBlue else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedPresetIndex = preset.id }
                                        .testTag("avatar_preset_${preset.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else {
                                        Text(
                                            text = preset.emoji,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Display Name Input
                item {
                    Column {
                        Text(
                            text = "Display Name",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceSecondary)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                textStyle = TextStyle(
                                    fontSize = 14.5.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(BrandDark),
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
                                text = "Bio & Equipment Details",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${bio.length}/180",
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceSecondary)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            BasicTextField(
                                value = bio,
                                onValueChange = { if (it.length <= 180) bio = it },
                                textStyle = TextStyle(
                                    fontSize = 13.5.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                ),
                                cursorBrush = SolidColor(BrandDark),
                                decorationBox = { inner ->
                                    if (bio.isEmpty()) {
                                        Text(
                                            text = "Tell other community members what camera, laptop, or tools you rent...",
                                            fontSize = 13.sp,
                                            color = TextMuted
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
                        // Phone
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Phone Number",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceSecondary)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    textStyle = TextStyle(fontSize = 13.5.sp, color = TextPrimary),
                                    cursorBrush = SolidColor(BrandDark),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("edit_phone_input")
                                )
                            }
                        }

                        // Location
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Location / City",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceSecondary)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    textStyle = TextStyle(fontSize = 13.5.sp, color = TextPrimary),
                                    cursorBrush = SolidColor(BrandDark),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("edit_location_input")
                                )
                            }
                        }
                    }
                }

                // Action Buttons: Save Changes & Cancel
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cancel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(SurfaceSecondary)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        // Save Changes
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(BrandDark)
                                .clickable {
                                    isSaving = true
                                    coroutineScope.launch {
                                        delay(400)
                                        val updated = currentProfile.copy(
                                            displayName = displayName.ifBlank { "Gokulan R" },
                                            bio = bio,
                                            phone = phone,
                                            location = location.ifBlank { "White Town, Puducherry" },
                                            avatarPresetIndex = selectedPresetIndex
                                        )
                                        onSave(updated)
                                        isSaving = false
                                    }
                                }
                                .testTag("save_profile_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Save Profile",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                color = TextSecondary,
                maxLines = 1
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
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceSecondary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
