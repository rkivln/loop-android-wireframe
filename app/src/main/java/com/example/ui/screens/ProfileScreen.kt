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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.data.models.AVATAR_PRESETS
import com.example.data.models.CommunityPostType
import com.example.data.models.RentalCategory
import com.example.data.models.UserProfile
import com.example.ui.components.CommunityInitiativeForm
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.UserProfileComponent
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite
import com.example.ui.theme.PaperWarm
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Clean, simple, and intuitive Profile Screen.
 */
@Composable
fun ProfileScreen(
    onNavigateBookings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val userActivities by RentalRepository.userActivityFlow.collectAsStateWithLifecycle()
    val userPosts by RentalRepository.userCommunityPostsFlow.collectAsStateWithLifecycle()
    val userRentals by RentalRepository.userCurrentRentalsFlow.collectAsStateWithLifecycle()

    var isEditingProfile by remember { mutableStateOf(false) }
    var isCreatingPost by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val showFeedback: (String) -> Unit = { msg ->
        saveSuccessMessage = msg
        coroutineScope.launch {
            delay(2400)
            saveSuccessMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperWarm)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Simple Header: "Profile" title + Edit Profile icon button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile",
                        style = LoopType.HeroDisplayLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        color = InkCharcoal
                    )

                    IconButton(
                        onClick = { isEditingProfile = true },
                        modifier = Modifier.testTag("profile_settings_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = InkCharcoal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Success feedback toast banner
            if (saveSuccessMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentMint)
                            .border(1.dp, AccentForestGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = saveSuccessMessage ?: "",
                                style = LoopType.Metadata.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccentForestGreen
                                )
                            )
                        }
                    }
                }
            }

            // Reusable Clean User Profile UI Component
            item {
                UserProfileComponent(
                    userProfile = userProfile,
                    communityPosts = userPosts,
                    currentRentals = userRentals,
                    userActivities = userActivities,
                    onEditBioClick = { isEditingProfile = true },
                    onTogglePostStatus = { postId ->
                        RentalRepository.toggleCommunityPostStatus(postId)
                        showFeedback("Initiative status updated")
                    },
                    onDeletePost = { postId ->
                        RentalRepository.deleteCommunityPost(postId)
                        showFeedback("Initiative removed")
                    },
                    onReturnRental = { rentalId ->
                        RentalRepository.completeRentalReturn(rentalId)
                        showFeedback("Equipment return completed")
                    },
                    onExtendRental = { rentalId ->
                        RentalRepository.extendRental(rentalId, 1)
                        showFeedback("Rental extended by 1 day")
                    },
                    onContactHost = { _ -> onNavigateBookings() },
                    onCreateNewPost = { isCreatingPost = true }
                )
            }
        }

        // Edit Profile Sheet
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
                    showFeedback("Profile saved successfully")
                },
                onDismiss = { isEditingProfile = false }
            )
        }

        // Create Community Event Modal Sheet
        AnimatedVisibility(
            visible = isCreatingPost,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isCreatingPost = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(PaperPureWhite)
                        .clickable(enabled = false) {}
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                        .navigationBarsPadding()
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New Community Initiative",
                                style = LoopType.HeadlineMedium,
                                color = InkCharcoal
                            )

                            IconButton(onClick = { isCreatingPost = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = InkSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        CommunityInitiativeForm(
                            onSubmit = { formData ->
                                RentalRepository.createCommunityPost(
                                    title = formData.title,
                                    type = formData.type,
                                    description = formData.description,
                                    location = formData.locationName,
                                    dateTime = formData.dateTime,
                                    latitude = formData.latitude,
                                    longitude = formData.longitude,
                                    category = formData.category
                                )
                                isCreatingPost = false
                                showFeedback("Event created and saved!")
                            },
                            onCancel = { isCreatingPost = false },
                            submitButtonLabel = "Publish Event"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean & Simple Edit Profile Sheet.
 */
@Composable
fun EditProfileModalSheet(
    currentProfile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var displayName by remember { mutableStateOf(currentProfile.displayName) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var location by remember { mutableStateOf(currentProfile.location) }
    var phone by remember { mutableStateOf(currentProfile.phone) }
    var selectedAvatarIndex by remember { mutableIntStateOf(currentProfile.avatarPresetIndex) }

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
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(PaperPureWhite)
                .clickable(enabled = false) {}
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .navigationBarsPadding()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile",
                        style = LoopType.HeadlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = InkCharcoal
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkSecondary
                        )
                    }
                }

                // Avatar color preset selector
                Column {
                    Text(
                        text = "AVATAR THEME",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AVATAR_PRESETS.forEach { preset ->
                            val isSelected = selectedAvatarIndex == preset.id
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.bgHex1))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) AccentForestGreen else PaperBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedAvatarIndex = preset.id },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.emoji,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // Name Field
                Column {
                    Text(
                        text = "NAME",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaperIvory)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Location Field
                Column {
                    Text(
                        text = "LOCATION",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaperIvory)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = location,
                            onValueChange = { location = it },
                            textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Bio Field
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "BIO",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Text(
                            text = "${bio.length}/200",
                            style = LoopType.Metadata.copy(fontSize = 10.sp),
                            color = InkMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaperIvory)
                            .padding(12.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        BasicTextField(
                            value = bio,
                            onValueChange = { if (it.length <= 200) bio = it },
                            textStyle = TextStyle(fontSize = 13.sp, color = InkCharcoal, lineHeight = 18.sp),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                PrimaryCTA(
                    text = "Save Profile",
                    onClick = {
                        val sanitizedName = displayName.trim().take(50).ifBlank { "Gokulan R" }
                        val sanitizedBio = bio.trim().take(200)
                        val sanitizedLocation = location.trim().take(60).ifBlank { "White Town, Puducherry" }
                        val updated = currentProfile.copy(
                            displayName = sanitizedName,
                            bio = sanitizedBio,
                            location = sanitizedLocation,
                            avatarPresetIndex = selectedAvatarIndex
                        )
                        onSave(updated)
                    },
                    testTag = "save_profile_button"
                )
            }
        }
    }
}
