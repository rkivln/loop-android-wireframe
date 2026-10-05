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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.data.models.AVATAR_PRESETS
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.UserActivityItem
import com.example.data.models.UserActivityType
import com.example.data.models.UserCommunityPost
import com.example.data.models.UserProfile
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SecondaryCTA
import com.example.ui.components.SectionLabel
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.AccentYellowSoft
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

enum class ProfileTab(val label: String, val emoji: String) {
    OVERVIEW("Overview & Bio", "👤"),
    MY_POSTS("Posted Activities", "🤝"),
    ACTIVITY("Activity Ledger", "⏱️")
}

@Composable
fun ProfileScreen(
    onNavigateBookings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val userActivities by RentalRepository.userActivityFlow.collectAsStateWithLifecycle()
    val userPosts by RentalRepository.userCommunityPostsFlow.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(ProfileTab.OVERVIEW) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isCreatingPost by remember { mutableStateOf(false) }
    var selectedPostFilter by remember { mutableStateOf<CommunityPostType?>(null) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val showFeedback: (String) -> Unit = { msg ->
        saveSuccessMessage = msg
        coroutineScope.launch {
            delay(2600)
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
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
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
                            text = "Creator identity, posted requests & activity ledger",
                            style = LoopType.Metadata,
                            color = InkSecondary
                        )
                    }

                    // Edit Profile CTA Pill
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = InkCharcoal,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Bio",
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                color = InkCharcoal
                            )
                        }
                    }
                }
            }

            // Success feedback banner
            if (saveSuccessMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AccentMint)
                            .border(1.dp, AccentForestGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
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

            // User Identity Dossier Card
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
                        // Avatar preset badge with initials
                        val currentAvatarPreset = AVATAR_PRESETS.getOrElse(userProfile.avatarPresetIndex) { AVATAR_PRESETS[0] }
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(currentAvatarPreset.bgHex1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.initials,
                                style = LoopType.HeadlineMedium.copy(fontSize = 22.sp),
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

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AccentWarmYellow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${userProfile.rating} (${userProfile.reviewCount})",
                                        style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, color = InkCharcoal)
                                    )
                                }

                                Text(
                                    text = "·",
                                    style = LoopType.Metadata,
                                    color = InkMuted
                                )

                                Text(
                                    text = userProfile.memberSince,
                                    style = LoopType.Metadata.copy(fontSize = 11.sp),
                                    color = InkSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Sub-Section Tab Selector Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) InkCharcoal else PaperPureWhite)
                                .border(
                                    1.dp,
                                    if (isSelected) InkCharcoal else PaperBorder,
                                    RoundedCornerShape(100.dp)
                                )
                                .clickable { selectedTab = tab }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${tab.emoji} ${tab.label}",
                                style = LoopType.Metadata.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) InkWhite else InkCharcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Tab Content Rendering
            when (selectedTab) {
                ProfileTab.OVERVIEW -> {
                    // Creator Bio Box
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CREATOR BIO & PHILOSOPHY",
                                        style = LoopType.EditorialTag,
                                        color = InkSecondary
                                    )

                                    Text(
                                        text = "Tap to edit",
                                        style = LoopType.Metadata.copy(fontSize = 10.5.sp, color = AccentForestGreen),
                                        modifier = Modifier.clickable { isEditingProfile = true }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = userProfile.bio.ifBlank { "Add your creator bio and personal gear aesthetic..." },
                                    style = LoopType.BodyEditorial,
                                    color = InkCharcoal
                                )
                            }
                        }
                    }

                    // Community Trust Badges
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
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
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
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
                                count = "${userPosts.size}",
                                label = "Posted Activities",
                                modifier = Modifier.weight(1f)
                            )
                            EditorialMetricBox(
                                count = "₹${userProfile.earnedAmount}",
                                label = "Host Earnings",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Quick Actions / Ledger Rows
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
                                icon = Icons.Default.Handshake,
                                title = "Manage Community Posts",
                                badge = "${userPosts.count { it.status == CommunityPostStatus.ACTIVE }} Open",
                                onClick = { selectedTab = ProfileTab.MY_POSTS }
                            )
                            EditorialMenuRow(
                                icon = Icons.Default.Schedule,
                                title = "Activity Ledger",
                                badge = "${userActivities.size} records",
                                onClick = { selectedTab = ProfileTab.ACTIVITY }
                            )
                            EditorialMenuRow(
                                icon = Icons.Default.Security,
                                title = "Security Deposit & Ledger",
                                badge = "₹0 on hold"
                            )
                        }
                    }
                }

                ProfileTab.MY_POSTS -> {
                    // Manage Posted Events or Help Requests Section
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Community Initiatives",
                                style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                                color = InkCharcoal
                            )

                            // Post New Activity Action Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(InkCharcoal)
                                    .clickable { isCreatingPost = true }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .testTag("create_community_post_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = InkWhite,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Post Request/Event",
                                        style = LoopType.Metadata.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                                        color = InkWhite
                                    )
                                }
                            }
                        }
                    }

                    // Category Filter Pills for user's posts
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 22.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (selectedPostFilter == null) InkCharcoal else PaperPureWhite)
                                    .border(1.dp, if (selectedPostFilter == null) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                                    .clickable { selectedPostFilter = null }
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "All (${userPosts.size})",
                                    style = LoopType.Metadata.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedPostFilter == null) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (selectedPostFilter == null) InkWhite else InkCharcoal
                                )
                            }

                            CommunityPostType.values().forEach { pType ->
                                val isSelected = selectedPostFilter == pType
                                val count = userPosts.count { it.type == pType }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(if (isSelected) InkCharcoal else PaperPureWhite)
                                        .border(1.dp, if (isSelected) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                                        .clickable { selectedPostFilter = if (isSelected) null else pType }
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "${pType.emoji} ${pType.title} ($count)",
                                        style = LoopType.Metadata.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) InkWhite else InkCharcoal
                                    )
                                }
                            }
                        }
                    }

                    val displayedPosts = userPosts.filter {
                        selectedPostFilter == null || it.type == selectedPostFilter
                    }

                    if (displayedPosts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 22.dp, vertical = 32.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(PaperPureWhite)
                                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                                    .padding(26.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "🤝",
                                        fontSize = 32.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No community posts in this category",
                                        style = LoopType.HeadlineMedium.copy(fontSize = 16.sp),
                                        color = InkCharcoal
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Share a skill inquiry, photowalk event, or study table with neighbors.",
                                        style = LoopType.BodySmall,
                                        color = InkSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    SecondaryCTA(
                                        text = "Create New Initiative",
                                        onClick = { isCreatingPost = true }
                                    )
                                }
                            }
                        }
                    } else {
                        items(displayedPosts, key = { it.id }) { post ->
                            CommunityPostCard(
                                post = post,
                                onToggleStatus = {
                                    RentalRepository.toggleCommunityPostStatus(post.id)
                                    showFeedback("Status updated for '${post.title}'.")
                                },
                                onDelete = {
                                    RentalRepository.deleteCommunityPost(post.id)
                                    showFeedback("Post '${post.title}' removed.")
                                }
                            )
                        }
                    }
                }

                ProfileTab.ACTIVITY -> {
                    // Activity Timeline Ledger
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Ledger Activity",
                                style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                                color = InkCharcoal
                            )

                            Text(
                                text = "${userActivities.size} records",
                                style = LoopType.Metadata,
                                color = InkSecondary
                            )
                        }
                    }

                    if (userActivities.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 22.dp, vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recent activity yet.",
                                    style = LoopType.BodyEditorial,
                                    color = InkSecondary
                                )
                            }
                        }
                    } else {
                        items(userActivities, key = { it.id }) { activity ->
                            ActivityTimelineItem(activity = activity)
                        }
                    }
                }
            }
        }

        // Edit Profile & Bio Modal Sheet
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
                    showFeedback("Bio & profile updated successfully.")
                },
                onDismiss = { isEditingProfile = false }
            )
        }

        // Create Community Event / Help Request Modal Sheet
        AnimatedVisibility(
            visible = isCreatingPost,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            CreateCommunityPostModalSheet(
                onPostCreated = { title, type, desc, loc, dt ->
                    RentalRepository.createCommunityPost(title, type, desc, loc, dt)
                    isCreatingPost = false
                    selectedTab = ProfileTab.MY_POSTS
                    showFeedback("Community ${type.title} published!")
                },
                onDismiss = { isCreatingPost = false }
            )
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: UserCommunityPost,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val isCompleted = post.status == CommunityPostStatus.COMPLETED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isCompleted) PaperIvory else PaperPureWhite)
            .border(
                1.dp,
                if (isCompleted) PaperBorder else PaperBorderSubtle,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .testTag("user_post_card_${post.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type & Status Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val badgeBg = when (post.type) {
                        CommunityPostType.EVENT -> AccentMint
                        CommunityPostType.HELP_REQUEST -> AccentPeach
                        CommunityPostType.STUDY_GROUP -> AccentPowderBlue
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(badgeBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${post.type.emoji} ${post.type.title}",
                            style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = InkCharcoal
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isCompleted) AccentBeigeOat else AccentForestGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isCompleted) "Resolved" else "Active / Open",
                            style = LoopType.Metadata.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) InkSecondary else AccentForestGreen
                            )
                        )
                    }
                }

                // Overflow Options Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = InkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(PaperPureWhite)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isCompleted) "Reopen Request" else "Mark as Resolved",
                                    style = LoopType.BodySmall,
                                    color = InkCharcoal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.Schedule else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(17.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleStatus()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Delete Post",
                                    style = LoopType.BodySmall,
                                    color = Color(0xFFDC2626)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(17.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.title,
                style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                color = InkCharcoal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = post.description,
                style = LoopType.BodyEditorial.copy(fontSize = 13.sp),
                color = InkSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = InkMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = post.location,
                        style = LoopType.Metadata.copy(fontSize = 11.sp),
                        color = InkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = post.attendeesOrResponses,
                    style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp),
                    color = AccentForestGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Status Toggle Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isCompleted) PaperPureWhite else AccentForestGreen)
                        .border(1.dp, if (isCompleted) PaperBorder else AccentForestGreen, RoundedCornerShape(100.dp))
                        .clickable(onClick = onToggleStatus)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isCompleted) "Reopen" else "✓ Mark Resolved",
                        style = LoopType.Metadata.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) InkCharcoal else InkWhite
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityTimelineItem(activity: UserActivityItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Pill
            val iconBg = when (activity.type) {
                UserActivityType.RESERVATION -> AccentPowderBlue
                UserActivityType.COMMUNITY_EVENT -> AccentMint
                UserActivityType.HELP_REQUEST -> AccentPeach
                UserActivityType.HUB_CHECKIN -> AccentYellowSoft
                else -> PaperIvory
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activity.iconEmoji,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activity.title,
                        style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = InkCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperIvory)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = activity.statusBadge,
                            style = LoopType.Metadata.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = if (activity.statusBadge == "Active") AccentForestGreen else InkSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = activity.subtitle,
                    style = LoopType.BodySmall.copy(fontSize = 12.sp),
                    color = InkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.timestamp,
                    style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                    color = InkMuted
                )
            }
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
                .padding(horizontal = 22.dp, vertical = 22.dp)
                .navigationBarsPadding()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile & Bio",
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

                // Avatar Presets Picker
                Column {
                    Text(
                        text = "STUDIO AVATAR THEME",
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
                                    .size(46.dp)
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
                                    fontSize = 18.sp
                                )
                            }
                        }
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
                        text = "LOCATION / QUARTER",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CREATOR BIO & TASTE",
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
                            .height(96.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperIvory)
                            .padding(14.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        BasicTextField(
                            value = bio,
                            onValueChange = { if (it.length <= 250) bio = it },
                            textStyle = TextStyle(fontSize = 13.5.sp, color = InkCharcoal, lineHeight = 19.sp),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                PrimaryCTA(
                    text = "Save Bio & Profile",
                    onClick = {
                        val sanitizedName = displayName.trim().take(60).ifBlank { "Gokulan R" }
                        val sanitizedBio = bio.trim().take(250)
                        val sanitizedLocation = location.trim().take(80).ifBlank { "White Town, Puducherry" }
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

@Composable
fun CreateCommunityPostModalSheet(
    onPostCreated: (title: String, type: CommunityPostType, desc: String, loc: String, dt: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CommunityPostType.EVENT) }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("White Town, Puducherry") }
    var dateTime by remember { mutableStateOf("Today · 5:00 PM") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkSecondary
                        )
                    }
                }

                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommunityPostType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) InkCharcoal else PaperIvory)
                                .border(1.dp, if (isSelected) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${type.emoji} ${type.title}",
                                style = LoopType.Metadata.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) InkWhite else InkCharcoal
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = "TITLE / SUBJECT",
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
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Column {
                    Text(
                        text = "DESCRIPTION & OBJECTIVES",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaperIvory)
                            .padding(12.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        BasicTextField(
                            value = description,
                            onValueChange = { description = it },
                            textStyle = TextStyle(fontSize = 13.sp, color = InkCharcoal),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LOCATION",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PaperIvory)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = location,
                                onValueChange = { location = it },
                                textStyle = TextStyle(fontSize = 13.sp, color = InkCharcoal),
                                cursorBrush = SolidColor(InkCharcoal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DATE & TIME",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PaperIvory)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = dateTime,
                                onValueChange = { dateTime = it },
                                textStyle = TextStyle(fontSize = 13.sp, color = InkCharcoal),
                                cursorBrush = SolidColor(InkCharcoal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = LoopType.Metadata.copy(color = Color(0xFFDC2626)),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                PrimaryCTA(
                    text = "Publish to Community Feed",
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Please enter an initiative title."
                        } else {
                            onPostCreated(title.trim(), selectedType, description.trim(), location.trim(), dateTime.trim())
                        }
                    },
                    testTag = "publish_community_post_btn"
                )
            }
        }
    }
}
