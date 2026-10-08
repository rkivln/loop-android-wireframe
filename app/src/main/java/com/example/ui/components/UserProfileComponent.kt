package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AVATAR_PRESETS
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.UserActivityItem
import com.example.data.models.UserActivityType
import com.example.data.models.UserCommunityPost
import com.example.data.models.UserProfile
import com.example.data.models.UserRentalBooking
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

enum class ProfileHistoryViewMode(val label: String, val emoji: String) {
    COMMUNITY_INTERACTIONS("Community History", "🤝"),
    CURRENT_RENTALS("Current Rentals", "📦"),
    LEDGER("Ledger Timeline", "⏱️")
}

/**
 * Reusable User Profile UI component.
 * Displays comprehensive personal information, trust verifications, metrics,
 * and an interactive switcher for history of community interactions or current rentals.
 */
@Composable
fun UserProfileComponent(
    userProfile: UserProfile,
    communityPosts: List<UserCommunityPost>,
    currentRentals: List<UserRentalBooking>,
    userActivities: List<UserActivityItem>,
    modifier: Modifier = Modifier,
    initialViewMode: ProfileHistoryViewMode = ProfileHistoryViewMode.COMMUNITY_INTERACTIONS,
    onEditBioClick: () -> Unit = {},
    onTogglePostStatus: (String) -> Unit = {},
    onDeletePost: (String) -> Unit = {},
    onRentalClick: (UserRentalBooking) -> Unit = {},
    onReturnRental: (String) -> Unit = {},
    onExtendRental: (String) -> Unit = {},
    onContactHost: (String) -> Unit = {},
    onCreateNewPost: () -> Unit = {},
    showEditActions: Boolean = true
) {
    var selectedViewMode by remember { mutableStateOf(initialViewMode) }
    var selectedPostCategoryFilter by remember { mutableStateOf<CommunityPostType?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_profile_component")
    ) {
        // 1. Personal Information Dossier Card
        UserProfilePersonalCard(
            profile = userProfile,
            onEditBioClick = onEditBioClick,
            showEditActions = showEditActions,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // 2. Metrics Strip
        UserProfileMetricsRow(
            activeRentalsCount = currentRentals.count { !it.isCompleted },
            communityPostsCount = communityPosts.size,
            hostEarnings = userProfile.earnedAmount,
            rating = userProfile.rating,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Segmented View Switcher: Community History vs Current Rentals vs Ledger
        UserProfileHistoryViewSwitcher(
            currentMode = selectedViewMode,
            communityCount = communityPosts.size,
            rentalsCount = currentRentals.count { !it.isCompleted },
            ledgerCount = userActivities.size,
            onSelectMode = { selectedViewMode = it },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Content Display based on selected View Mode
        when (selectedViewMode) {
            ProfileHistoryViewMode.COMMUNITY_INTERACTIONS -> {
                UserProfileCommunitySection(
                    posts = communityPosts,
                    selectedFilter = selectedPostCategoryFilter,
                    onSelectFilter = { selectedPostCategoryFilter = it },
                    onToggleStatus = onTogglePostStatus,
                    onDeletePost = onDeletePost,
                    onCreateNewPost = onCreateNewPost,
                    showEditActions = showEditActions,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            ProfileHistoryViewMode.CURRENT_RENTALS -> {
                UserProfileRentalsSection(
                    rentals = currentRentals,
                    onRentalClick = onRentalClick,
                    onReturnRental = onReturnRental,
                    onExtendRental = onExtendRental,
                    onContactHost = onContactHost,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            ProfileHistoryViewMode.LEDGER -> {
                UserProfileLedgerSection(
                    activities = userActivities,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

/**
 * Card displaying comprehensive personal information:
 * Avatar with preset styling, display name, verified badge, bio snippet,
 * contact details, and trust badges.
 */
@Composable
fun UserProfilePersonalCard(
    profile: UserProfile,
    onEditBioClick: () -> Unit,
    modifier: Modifier = Modifier,
    showEditActions: Boolean = true
) {
    val avatarPreset = AVATAR_PRESETS.getOrElse(profile.avatarPresetIndex) { AVATAR_PRESETS[0] }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .testTag("user_profile_personal_card")
    ) {
        Column {
            // Top Row: Avatar + Name + Edit Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with colored background preset and initials
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(avatarPreset.bgHex1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.initials,
                        style = LoopType.HeadlineMedium.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                        color = InkWhite
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = profile.displayName,
                            style = LoopType.HeadlineMedium.copy(fontSize = 20.sp),
                            color = InkCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (profile.isVerified) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Verified Member",
                                tint = AccentForestGreen,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = InkMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = profile.location,
                            style = LoopType.BodySmall,
                            color = InkSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                text = "${profile.rating} (${profile.reviewCount} reviews)",
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                                color = InkCharcoal
                            )
                        }

                        Text(
                            text = "·",
                            style = LoopType.Metadata,
                            color = InkMuted
                        )

                        Text(
                            text = profile.memberSince,
                            style = LoopType.Metadata.copy(fontSize = 11.sp),
                            color = InkSecondary
                        )
                    }
                }

                if (showEditActions) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperWarm)
                            .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onEditBioClick
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("profile_bio_edit_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Bio",
                                tint = InkCharcoal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit Bio",
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp),
                                color = InkCharcoal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bio Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PaperWarm.copy(alpha = 0.5f))
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "CREATOR BIO & PHILOSOPHY",
                        style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = profile.bio.ifBlank { "Add your personal bio and studio aesthetic to share with neighbors." },
                        style = LoopType.BodyEditorial.copy(fontSize = 13.5.sp, lineHeight = 20.sp),
                        color = InkCharcoal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contact Info Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Email
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = InkSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = profile.email,
                            style = LoopType.Metadata.copy(fontSize = 11.sp),
                            color = InkCharcoal
                        )
                    }
                }

                // Phone
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = InkSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = profile.phone,
                            style = LoopType.Metadata.copy(fontSize = 11.sp),
                            color = InkCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trust Badges Carousel
            Text(
                text = "COMMUNITY TRUST BADGES",
                style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                color = InkSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profile.badges.forEach { badge ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = badge,
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                            color = InkCharcoal
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3-Card Metrics Row for key user stats
 */
@Composable
fun UserProfileMetricsRow(
    activeRentalsCount: Int,
    communityPostsCount: Int,
    hostEarnings: Int,
    rating: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricTile(
            number = "$activeRentalsCount",
            label = "Active Rentals",
            badgeEmoji = "📦",
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            number = "$communityPostsCount",
            label = "Community Posts",
            badgeEmoji = "🤝",
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            number = "₹$hostEarnings",
            label = "Host Earnings",
            badgeEmoji = "💰",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricTile(
    number: String,
    label: String,
    badgeEmoji: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = badgeEmoji, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = number,
                    style = LoopType.PriceHeadline.copy(fontSize = 17.sp),
                    color = InkCharcoal
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                color = InkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Segmented switch tabs between Community History, Current Rentals, and Ledger
 */
@Composable
fun UserProfileHistoryViewSwitcher(
    currentMode: ProfileHistoryViewMode,
    communityCount: Int,
    rentalsCount: Int,
    ledgerCount: Int,
    onSelectMode: (ProfileHistoryViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProfileHistoryViewMode.values().forEach { mode ->
            val isSelected = currentMode == mode
            val count = when (mode) {
                ProfileHistoryViewMode.COMMUNITY_INTERACTIONS -> communityCount
                ProfileHistoryViewMode.CURRENT_RENTALS -> rentalsCount
                ProfileHistoryViewMode.LEDGER -> ledgerCount
            }

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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onSelectMode(mode) }
                    )
                    .padding(vertical = 10.dp, horizontal = 4.dp)
                    .testTag("profile_tab_${mode.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${mode.emoji} ${mode.label} ($count)",
                    style = LoopType.Metadata.copy(
                        fontSize = 11.sp,
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

/**
 * Community History Section: displays user's help requests, events, study groups
 */
@Composable
fun UserProfileCommunitySection(
    posts: List<UserCommunityPost>,
    selectedFilter: CommunityPostType?,
    onSelectFilter: (CommunityPostType?) -> Unit,
    onToggleStatus: (String) -> Unit,
    onDeletePost: (String) -> Unit,
    onCreateNewPost: () -> Unit,
    modifier: Modifier = Modifier,
    showEditActions: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Community Interactions History",
                    style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                    color = InkCharcoal
                )
                Text(
                    text = "Help requests, local events & study tables",
                    style = LoopType.Metadata,
                    color = InkSecondary
                )
            }

            if (showEditActions) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(InkCharcoal)
                        .clickable(onClick = onCreateNewPost)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("btn_create_new_community_post")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = InkWhite,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "New Post",
                            style = LoopType.Metadata.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                            color = InkWhite
                        )
                    }
                }
            }
        }

        // Category filter pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (selectedFilter == null) InkCharcoal else PaperPureWhite)
                    .border(1.dp, if (selectedFilter == null) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                    .clickable { onSelectFilter(null) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "All (${posts.size})",
                    style = LoopType.Metadata.copy(
                        fontSize = 11.sp,
                        fontWeight = if (selectedFilter == null) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (selectedFilter == null) InkWhite else InkCharcoal
                )
            }

            CommunityPostType.values().forEach { pType ->
                val isSelected = selectedFilter == pType
                val count = posts.count { it.type == pType }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) InkCharcoal else PaperPureWhite)
                        .border(1.dp, if (isSelected) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                        .clickable { onSelectFilter(if (isSelected) null else pType) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
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

        Spacer(modifier = Modifier.height(6.dp))

        val filteredPosts = posts.filter { selectedFilter == null || it.type == selectedFilter }

        if (filteredPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🤝", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No community interactions yet",
                        style = LoopType.HeadlineMedium.copy(fontSize = 16.sp),
                        color = InkCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ask for gear advice or host a meetup with neighborhood creators.",
                        style = LoopType.BodySmall,
                        color = InkSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredPosts.forEach { post ->
                    CommunityInteractionCard(
                        post = post,
                        onToggleStatus = { onToggleStatus(post.id) },
                        onDelete = { onDeletePost(post.id) },
                        showEditActions = showEditActions
                    )
                }
            }
        }
    }
}

/**
 * Card for an individual community interaction (Help request, Event, Study group)
 */
@Composable
fun CommunityInteractionCard(
    post: UserCommunityPost,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    showEditActions: Boolean = true
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isCompleted = post.status == CommunityPostStatus.COMPLETED

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isCompleted) PaperIvory else PaperPureWhite)
            .border(
                1.dp,
                if (isCompleted) PaperBorder else PaperBorderSubtle,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .testTag("community_interaction_card_${post.id}")
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
                            text = if (isCompleted) "✓ Resolved" else "● Active / Open",
                            style = LoopType.Metadata.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) InkSecondary else AccentForestGreen
                            )
                        )
                    }
                }

                if (showEditActions) {
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
                                        modifier = Modifier.size(16.dp)
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
                                        modifier = Modifier.size(16.dp)
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

            if (showEditActions) {
                Spacer(modifier = Modifier.height(12.dp))

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
                            .testTag("toggle_post_status_${post.id}")
                    ) {
                        Text(
                            text = if (isCompleted) "Reopen Initiative" else "✓ Mark Resolved",
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
}

/**
 * Current Rentals Section: displays currently borrowed items and active reservations
 */
@Composable
fun UserProfileRentalsSection(
    rentals: List<UserRentalBooking>,
    onRentalClick: (UserRentalBooking) -> Unit,
    onReturnRental: (String) -> Unit,
    onExtendRental: (String) -> Unit,
    onContactHost: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Current Equipment Rentals",
                    style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                    color = InkCharcoal
                )
                Text(
                    text = "Items currently in your custody and return status",
                    style = LoopType.Metadata,
                    color = InkSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(AccentForestGreen.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${rentals.count { !it.isCompleted }} Active",
                    style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, color = AccentForestGreen),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (rentals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📦", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No active rentals right now",
                        style = LoopType.HeadlineMedium.copy(fontSize = 16.sp),
                        color = InkCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Explore neighborhood cameras, sound gear, and studio tools in the catalog.",
                        style = LoopType.BodySmall,
                        color = InkSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rentals.forEach { rental ->
                    CurrentRentalCard(
                        rental = rental,
                        onClick = { onRentalClick(rental) },
                        onReturn = { onReturnRental(rental.id) },
                        onExtend = { onExtendRental(rental.id) },
                        onContactHost = { onContactHost(rental.id) }
                    )
                }
            }
        }
    }
}

/**
 * Card for an active or current equipment rental
 */
@Composable
fun CurrentRentalCard(
    rental: UserRentalBooking,
    onClick: () -> Unit,
    onReturn: () -> Unit,
    onExtend: () -> Unit,
    onContactHost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDueSoon = rental.daysRemaining <= 1 && !rental.isCompleted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(PaperPureWhite)
            .border(
                1.dp,
                if (isDueSoon) AccentWarmYellow.copy(alpha = 0.5f) else PaperBorderSubtle,
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
            .testTag("current_rental_card_${rental.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item primary image thumbnail
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PaperIvory)
                ) {
                    Image(
                        painter = painterResource(id = rental.item.primaryImageRes),
                        contentDescription = rental.item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(76.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(rental.item.category.bgHex))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${rental.item.category.iconEmoji} ${rental.item.category.title}",
                                style = LoopType.Metadata.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(rental.item.category.iconHex)
                                )
                            )
                        }

                        // Status badge pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    when {
                                        rental.isCompleted -> PaperIvory
                                        isDueSoon -> AccentWarmYellow.copy(alpha = 0.2f)
                                        else -> AccentMint
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = rental.status,
                                style = LoopType.Metadata.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        rental.isCompleted -> InkSecondary
                                        isDueSoon -> Color(0xFFB45309)
                                        else -> AccentForestGreen
                                    }
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = rental.item.title,
                        style = LoopType.HeadlineMedium.copy(fontSize = 16.sp),
                        color = InkCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Host: ${rental.hostName} · ₹${rental.item.pricePerDay}/day",
                        style = LoopType.BodySmall,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${rental.startDate} – ${rental.endDate}",
                            style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                            color = InkCharcoal
                        )
                        Text(
                            text = "·",
                            style = LoopType.Metadata,
                            color = InkMuted
                        )
                        Text(
                            text = "Paid ₹${rental.totalPaid}",
                            style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                            color = AccentForestGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lockbox / Delivery Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PaperWarm.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔒", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = rental.deliveryType,
                            style = LoopType.Metadata.copy(fontSize = 11.sp),
                            color = InkCharcoal
                        )
                    }

                    Text(
                        text = "Pickup PIN: ${rental.pickupCode}",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = InkCharcoal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Message Host
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperWarm)
                        .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                        .clickable(onClick = onContactHost)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chat Host",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                        color = InkCharcoal
                    )
                }

                if (!rental.isCompleted) {
                    // Extend +1 Day
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperWarm)
                            .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                            .clickable(onClick = onExtend)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+1 Day (Extend)",
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                            color = InkCharcoal
                        )
                    }

                    // Return Gear CTA
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(100.dp))
                            .background(AccentForestGreen)
                            .clickable(onClick = onReturn)
                            .padding(vertical = 8.dp)
                            .testTag("rental_return_btn_${rental.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Return Gear",
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = InkWhite
                        )
                    }
                }
            }
        }
    }
}

/**
 * Activity Ledger Section: Chronological audit trail
 */
@Composable
fun UserProfileLedgerSection(
    activities: List<UserActivityItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Activity Ledger & Logs",
                    style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                    color = InkCharcoal
                )
                Text(
                    text = "Handoff receipts, checkins, and community records",
                    style = LoopType.Metadata,
                    color = InkSecondary
                )
            }

            Text(
                text = "${activities.size} records",
                style = LoopType.Metadata,
                color = InkSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No ledger entries recorded yet.",
                    style = LoopType.BodyEditorial,
                    color = InkSecondary
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activities.forEach { act ->
                    LedgerTimelineItem(activity = act)
                }
            }
        }
    }
}

@Composable
private fun LedgerTimelineItem(activity: UserActivityItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
