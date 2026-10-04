package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.data.models.UserProfile
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SecondaryCTA
import com.example.ui.components.SectionLabel
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentWarmYellow
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
            .background(PaperWarm)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Editorial Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Studio Shelf",
                            style = LoopType.HeroDisplayLarge.copy(fontSize = 28.sp),
                            color = InkCharcoal
                        )
                        Text(
                            text = "Verified maker credentials & circulation ledger",
                            style = LoopType.Metadata,
                            color = InkSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorder, RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { isEditingProfile = true }
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("profile_edit_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Edit Profile",
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                            color = InkCharcoal
                        )
                    }
                }
            }

            // Success feedback toast
            if (saveSuccessMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentMint)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = saveSuccessMessage ?: "",
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, color = AccentForestGreen)
                        )
                    }
                }
            }

            // User Identity Dossier
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(InkCharcoal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.initials,
                                style = LoopType.HeadlineMedium.copy(fontSize = 20.sp),
                                color = InkWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.displayName,
                                    style = LoopType.HeadlineMedium.copy(fontSize = 19.sp),
                                    color = InkCharcoal
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Identity",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = userProfile.location,
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AccentWarmYellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${userProfile.rating} (${userProfile.reviewCount} reviews)",
                                    style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, color = InkCharcoal)
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
                        .padding(horizontal = 22.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "CREATOR BIO & TASTE",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = userProfile.bio.ifBlank { "No bio added yet." },
                            style = LoopType.BodyEditorial,
                            color = InkCharcoal
                        )
                    }
                }
            }

            // Trust Badges Strip
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionLabel(title = "Community Verification")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    userProfile.badges.forEach { badge ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = badge,
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                color = InkCharcoal
                            )
                        }
                    }
                }
            }

            // Metrics Summary Row
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditorialMetricBox(
                        count = "${userProfile.rentalsCompleted}",
                        label = "Pieces Borrowed",
                        modifier = Modifier.weight(1f)
                    )
                    EditorialMetricBox(
                        count = "${userProfile.listingsCount}",
                        label = "Listed Gear",
                        modifier = Modifier.weight(1f)
                    )
                    EditorialMetricBox(
                        count = "₹${userProfile.earnedAmount}",
                        label = "Host Earnings",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Profile Menu Options
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                ) {
                    EditorialMenuRow(
                        icon = Icons.Default.ShoppingBag,
                        title = "Active Reservations",
                        badge = "1 Active",
                        onClick = onNavigateBookings
                    )
                    EditorialMenuRow(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Saved Hardware",
                        badge = "3 items"
                    )
                    EditorialMenuRow(
                        icon = Icons.Default.Security,
                        title = "Security Deposit & Ledger",
                        badge = "₹0 on hold"
                    )
                    EditorialMenuRow(
                        icon = Icons.Default.HelpOutline,
                        title = "Community Guild & Support"
                    )
                }
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
                    saveSuccessMessage = "Profile updated."
                    coroutineScope.launch {
                        delay(2500)
                        saveSuccessMessage = null
                    }
                },
                onDismiss = { isEditingProfile = false }
            )
        }
    }
}

@Composable
private fun EditorialMetricBox(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count,
                style = LoopType.PriceHeadline.copy(fontSize = 18.sp),
                color = InkCharcoal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                color = InkSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EditorialMenuRow(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = InkCharcoal,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = LoopType.BodyMedium,
                color = InkCharcoal
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperIvory)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badge,
                        style = LoopType.Metadata.copy(fontSize = 10.5.sp),
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

@Composable
fun EditProfileModalSheet(
    currentProfile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var displayName by remember { mutableStateOf(currentProfile.displayName) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var location by remember { mutableStateOf(currentProfile.location) }

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
                .padding(horizontal = 22.dp, vertical = 22.dp)
                .navigationBarsPadding()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile",
                        style = LoopType.HeadlineMedium,
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

                Column {
                    Text(
                        text = "DISPLAY NAME",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
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

                Column {
                    Text(
                        text = "LOCATION",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
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

                Column {
                    Text(
                        text = "BIO & GEAR FOCUS",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperIvory)
                            .padding(14.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        BasicTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            textStyle = TextStyle(fontSize = 13.5.sp, color = InkCharcoal, lineHeight = 19.sp),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                PrimaryCTA(
                    text = "Save Changes",
                    onClick = {
                        val sanitizedName = displayName.trim().take(60).ifBlank { "Gokulan R" }
                        val sanitizedBio = bio.trim().take(200)
                        val sanitizedLocation = location.trim().take(80).ifBlank { "White Town, Puducherry" }
                        val updated = currentProfile.copy(
                            displayName = sanitizedName,
                            bio = sanitizedBio,
                            location = sanitizedLocation
                        )
                        onSave(updated)
                    },
                    testTag = "save_profile_button"
                )
            }
        }
    }
}
