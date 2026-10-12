package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.example.data.models.UserProfile
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SecondaryCTA
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
 * Dedicated screen to display and edit user profile information:
 * Avatar selection, display name, bio, location, and contact information.
 * Persists updates directly to Room SQLite database via [RentalRepository.updateUserProfile].
 */
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val userProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()

    var displayName by remember(userProfile.displayName) { mutableStateOf(userProfile.displayName) }
    var bio by remember(userProfile.bio) { mutableStateOf(userProfile.bio) }
    var location by remember(userProfile.location) { mutableStateOf(userProfile.location) }
    var phone by remember(userProfile.phone) { mutableStateOf(userProfile.phone) }
    var selectedAvatarIndex by remember(userProfile.avatarPresetIndex) { mutableIntStateOf(userProfile.avatarPresetIndex) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val currentPreset = AVATAR_PRESETS.getOrElse(selectedAvatarIndex) { AVATAR_PRESETS[0] }

    val computedInitials = remember(displayName) {
        val parts = displayName.trim().split(" ")
        when {
            parts.isEmpty() || displayName.isBlank() -> "U"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperWarm)
            .testTag("edit_profile_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("edit_profile_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = InkCharcoal
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Edit Profile",
                        style = LoopType.HeadlineMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = InkCharcoal
                    )
                }

                // Quick Save Pill in Top Bar
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(InkCharcoal)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = {
                                val updated = userProfile.copy(
                                    displayName = displayName.trim().take(50).ifBlank { "Gokulan R" },
                                    bio = bio.trim().take(250),
                                    location = location.trim().take(60).ifBlank { "White Town, Puducherry" },
                                    phone = phone.trim().take(20),
                                    avatarPresetIndex = selectedAvatarIndex
                                )
                                RentalRepository.updateUserProfile(updated)
                                successMessage = "Profile saved to local database"
                                coroutineScope.launch {
                                    delay(1500)
                                    onBack()
                                }
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("top_bar_save_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                        color = InkWhite
                    )
                }
            }

            // Success Toast Banner
            AnimatedVisibility(
                visible = successMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
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
                            text = successMessage ?: "",
                            style = LoopType.Metadata.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AccentForestGreen
                            )
                        )
                    }
                }
            }

            // Scrollable Editor Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Live Avatar Display & Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Live Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color(currentPreset.bgHex1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = computedInitials,
                                style = LoopType.HeadlineMedium.copy(
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = InkWhite
                            )

                            // Emoji badge overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(PaperPureWhite)
                                    .border(1.5.dp, PaperBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = currentPreset.emoji, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = displayName.ifBlank { "Your Name" },
                            style = LoopType.HeadlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            color = InkCharcoal
                        )

                        Text(
                            text = currentPreset.label,
                            style = LoopType.Metadata,
                            color = InkSecondary
                        )
                    }
                }

                // 2. Avatar Theme Preset Picker
                Column {
                    Text(
                        text = "CHOOSE AVATAR THEME",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AVATAR_PRESETS.forEach { preset ->
                            val isSelected = selectedAvatarIndex == preset.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) PaperIvory else PaperPureWhite)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) AccentForestGreen else PaperBorderSubtle,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedAvatarIndex = preset.id }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("avatar_preset_${preset.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(preset.bgHex1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = preset.emoji, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = preset.label,
                                    style = LoopType.Metadata.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) InkCharcoal else InkSecondary
                                )
                            }
                        }
                    }
                }

                // 3. Display Name Input
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "DISPLAY NAME",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Text(
                            text = "${displayName.length}/50",
                            style = LoopType.Metadata.copy(fontSize = 10.sp),
                            color = InkMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = displayName,
                            onValueChange = { if (it.length <= 50) displayName = it },
                            textStyle = TextStyle(fontSize = 14.5.sp, color = InkCharcoal, fontWeight = FontWeight.Medium),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_display_name_input")
                        )
                    }
                }

                // 4. Bio Input
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "BIO & ABOUT YOU",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Text(
                            text = "${bio.length}/250",
                            style = LoopType.Metadata.copy(fontSize = 10.sp),
                            color = InkMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        BasicTextField(
                            value = bio,
                            onValueChange = { if (it.length <= 250) bio = it },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = InkCharcoal,
                                lineHeight = 20.sp
                            ),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("edit_bio_input")
                        )
                    }
                }

                // 5. Location Input
                Column {
                    Text(
                        text = "LOCATION / QUARTER",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = InkMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = location,
                                onValueChange = { if (it.length <= 60) location = it },
                                textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                                cursorBrush = SolidColor(InkCharcoal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_location_input")
                            )
                        }
                    }
                }

                // 6. Phone Contact Input
                Column {
                    Text(
                        text = "PHONE NUMBER (FOR RENTAL COORDINATION)",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = InkMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = phone,
                                onValueChange = { if (it.length <= 20) phone = it },
                                textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                                cursorBrush = SolidColor(InkCharcoal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_phone_input")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons: Save & Cancel
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryCTA(
                        text = "Save Profile to Database",
                        onClick = {
                            val updated = userProfile.copy(
                                displayName = displayName.trim().take(50).ifBlank { "Gokulan R" },
                                bio = bio.trim().take(250),
                                location = location.trim().take(60).ifBlank { "White Town, Puducherry" },
                                phone = phone.trim().take(20),
                                avatarPresetIndex = selectedAvatarIndex
                            )
                            RentalRepository.updateUserProfile(updated)
                            successMessage = "Profile saved successfully!"
                            coroutineScope.launch {
                                delay(1200)
                                onBack()
                            }
                        },
                        testTag = "save_profile_button"
                    )

                    SecondaryCTA(
                        text = "Cancel",
                        onClick = onBack,
                        testTag = "cancel_profile_button"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
