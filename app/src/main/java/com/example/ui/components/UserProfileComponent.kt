package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AVATAR_PRESETS
import com.example.data.models.CommunityPostStatus
import com.example.data.models.UserActivityItem
import com.example.data.models.UserCommunityPost
import com.example.data.models.UserProfile
import com.example.data.models.UserRentalBooking
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

enum class ProfileHistoryViewMode(val label: String, val emoji: String) {
    COMMUNITY_INTERACTIONS("Activity & Rentals", "📦"),
    CURRENT_RENTALS("Current Rentals", "📦"),
    LEDGER("Account Settings", "⚙️")
}

/**
 * A clean, simple, and elegant User Profile component.
 * Displays crisp avatar & bio, high-level key stats, and clean tabbed content for
 * active rentals, community initiatives, and account preferences.
 */
@Composable
fun UserProfileComponent(
    userProfile: UserProfile,
    communityPosts: List<UserCommunityPost>,
    currentRentals: List<UserRentalBooking>,
    userActivities: List<UserActivityItem> = emptyList(),
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
    var selectedTab by remember { mutableStateOf(0) } // 0: Rentals & Community, 1: Account Settings

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_profile_component"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Clean Simple Profile Card
        SimpleProfileHeroCard(
            profile = userProfile,
            onEditClick = onEditBioClick,
            showEditActions = showEditActions
        )

        // 2. Minimalist Key Stats Bar
        SimpleProfileStatsRow(
            rentalsCompleted = userProfile.rentalsCompleted,
            activeCount = currentRentals.count { !it.isCompleted },
            rating = userProfile.rating,
            reviewCount = userProfile.reviewCount
        )

        // 3. Simple Segmented Pill Tabs
        SimpleProfileTabSwitcher(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // 4. Tab Content
        when (selectedTab) {
            0 -> {
                SimpleActivityAndRentalsSection(
                    rentals = currentRentals,
                    communityPosts = communityPosts,
                    onRentalClick = onRentalClick,
                    onReturnRental = onReturnRental,
                    onExtendRental = onExtendRental,
                    onTogglePostStatus = onTogglePostStatus,
                    onCreateNewPost = onCreateNewPost
                )
            }
            1 -> {
                SimpleAccountSettingsSection(
                    profile = userProfile,
                    onEditInfoClick = onEditBioClick
                )
            }
        }
    }
}

/**
 * Simple, clean hero card for user identity: Avatar, Name, Verified status, Location, Bio, and Action buttons.
 */
@Composable
fun SimpleProfileHeroCard(
    profile: UserProfile,
    onEditClick: () -> Unit,
    showEditActions: Boolean = true,
    modifier: Modifier = Modifier
) {
    val avatarPreset = AVATAR_PRESETS.getOrElse(profile.avatarPresetIndex) { AVATAR_PRESETS[0] }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
            .padding(20.dp)
            .testTag("user_profile_hero_card")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Circular Avatar with Initials
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(avatarPreset.bgHex1)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.initials,
                    style = LoopType.HeadlineMedium.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = InkWhite
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Display Name + Verified Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = profile.displayName,
                    style = LoopType.HeadlineMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                    color = InkCharcoal
                )
                if (profile.isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified Member",
                        tint = AccentForestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Location & Membership
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = InkMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${profile.location} · ${profile.memberSince}",
                    style = LoopType.Metadata,
                    color = InkSecondary
                )
            }

            // Bio
            if (profile.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = profile.bio,
                    style = LoopType.BodyEditorial.copy(fontSize = 13.5.sp, lineHeight = 20.sp),
                    color = InkCharcoal,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Buttons Row: Edit Profile & Share
            if (showEditActions) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperWarm)
                            .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onEditClick
                            )
                            .testTag("simple_edit_profile_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = InkCharcoal,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Profile",
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                color = InkCharcoal
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PaperWarm)
                            .border(1.dp, PaperBorder, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { /* Share link */ }
                            )
                            .testTag("simple_share_profile_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Profile",
                            tint = InkCharcoal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean 3-column stats bar: Completed Rentals, Active Items, Rating.
 */
@Composable
fun SimpleProfileStatsRow(
    rentalsCompleted: Int,
    activeCount: Int,
    rating: Float,
    reviewCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimpleStatItem(
                value = "$rentalsCompleted",
                label = "Completed"
            )
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(PaperBorderSubtle))
            SimpleStatItem(
                value = "$activeCount",
                label = "Active"
            )
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(PaperBorderSubtle))
            SimpleStatItem(
                value = "${rating} ★",
                label = "$reviewCount reviews"
            )
        }
    }
}

@Composable
private fun SimpleStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = LoopType.HeadlineMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
            color = InkCharcoal
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = LoopType.Metadata.copy(fontSize = 11.sp),
            color = InkSecondary
        )
    }
}

/**
 * Minimalist Segmented Tabs switcher.
 */
@Composable
fun SimpleProfileTabSwitcher(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .background(PaperIvory)
            .padding(4.dp)
    ) {
        val tabs = listOf("Rentals & Events", "Account Settings")
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isSelected) InkCharcoal else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onTabSelected(index) }
                    )
                    .padding(vertical = 10.dp)
                    .testTag("profile_tab_$index"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = LoopType.Metadata.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = if (isSelected) InkWhite else InkSecondary
                )
            }
        }
    }
}

/**
 * Tab 1: Clean, concise view of active rentals & community events.
 */
@Composable
fun SimpleActivityAndRentalsSection(
    rentals: List<UserRentalBooking>,
    communityPosts: List<UserCommunityPost>,
    onRentalClick: (UserRentalBooking) -> Unit,
    onReturnRental: (String) -> Unit,
    onExtendRental: (String) -> Unit,
    onTogglePostStatus: (String) -> Unit,
    onCreateNewPost: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Rentals Subsection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Rentals",
                style = LoopType.HeadlineMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                color = InkCharcoal
            )
            Text(
                text = "${rentals.count { !it.isCompleted }} items",
                style = LoopType.Metadata,
                color = InkSecondary
            )
        }

        if (rentals.isEmpty() || rentals.all { it.isCompleted }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active rentals at the moment.",
                    style = LoopType.BodyEditorial.copy(fontSize = 13.sp),
                    color = InkSecondary
                )
            }
        } else {
            rentals.filter { !it.isCompleted }.forEach { rental ->
                SimpleRentalCard(
                    rental = rental,
                    onReturn = { onReturnRental(rental.id) },
                    onExtend = { onExtendRental(rental.id) }
                )
            }
        }

        // Community Initiatives & Events Subsection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Community Initiatives",
                style = LoopType.HeadlineMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                color = InkCharcoal
            )

            // + Create Event Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onCreateNewPost
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("create_event_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Event",
                    tint = InkCharcoal,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "New Event",
                    style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp),
                    color = InkCharcoal
                )
            }
        }

        if (communityPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No community initiatives posted yet.",
                    style = LoopType.BodyEditorial.copy(fontSize = 13.sp),
                    color = InkSecondary
                )
            }
        } else {
            communityPosts.take(4).forEach { post ->
                SimpleCommunityPostCard(
                    post = post,
                    onToggleStatus = { onTogglePostStatus(post.id) }
                )
            }
        }
    }
}

/**
 * Clean compact rental card.
 */
@Composable
private fun SimpleRentalCard(
    rental: UserRentalBooking,
    onReturn: () -> Unit,
    onExtend: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rental.item.title,
                        style = LoopType.HeadlineMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = InkCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Host: ${rental.hostName} · Due in ${rental.daysRemaining} days",
                        style = LoopType.Metadata,
                        color = InkSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentMint)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "₹${rental.totalPaid}",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, color = AccentForestGreen),
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = PaperBorderSubtle, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperWarm)
                        .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                        .clickable(onClick = onExtend)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+1 Day",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp),
                        color = InkCharcoal
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(InkCharcoal)
                        .clickable(onClick = onReturn)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Return",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp),
                        color = InkWhite
                    )
                }
            }
        }
    }
}

/**
 * Clean compact community initiative card.
 */
@Composable
private fun SimpleCommunityPostCard(
    post: UserCommunityPost,
    onToggleStatus: () -> Unit
) {
    val isCompleted = post.status == CommunityPostStatus.COMPLETED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = post.type.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.title,
                        style = LoopType.HeadlineMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = InkCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${post.location} · ${post.dateTime}",
                    style = LoopType.Metadata.copy(fontSize = 11.5.sp),
                    color = InkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status chip button (toggles between active and completed)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isCompleted) PaperIvory else AccentMint)
                    .clickable(onClick = onToggleStatus)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isCompleted) "Completed" else "Active",
                    style = LoopType.Metadata.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) InkSecondary else AccentForestGreen,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

/**
 * Tab 2: Clean, iOS/Airbnb style Account Settings list.
 */
@Composable
fun SimpleAccountSettingsSection(
    profile: UserProfile,
    onEditInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
            .padding(vertical = 8.dp)
    ) {
        SimpleSettingsRow(
            icon = Icons.Default.Person,
            title = "Personal Information",
            subtitle = profile.email,
            onClick = onEditInfoClick
        )
        HorizontalDivider(color = PaperBorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        SimpleSettingsRow(
            icon = Icons.Default.CreditCard,
            title = "Payments & Payouts",
            subtitle = "UPI, bank account & security deposits",
            onClick = {}
        )
        HorizontalDivider(color = PaperBorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        SimpleSettingsRow(
            icon = Icons.Default.NotificationsNone,
            title = "Notifications",
            subtitle = "Push notifications & booking reminders",
            onClick = {}
        )
        HorizontalDivider(color = PaperBorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        SimpleSettingsRow(
            icon = Icons.Default.VerifiedUser,
            title = "Trust & Verification",
            subtitle = if (profile.isVerified) "Identity verified" else "Pending verification",
            onClick = {}
        )
        HorizontalDivider(color = PaperBorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        SimpleSettingsRow(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            title = "Help & Support",
            subtitle = "FAQ, safety guidelines & contact",
            onClick = {}
        )
        HorizontalDivider(color = PaperBorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        SimpleSettingsRow(
            icon = Icons.Default.ExitToApp,
            title = "Log Out",
            subtitle = "Sign out of Loop on this device",
            isDestructive = true,
            onClick = {}
        )
    }
}

@Composable
private fun SimpleSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isDestructive) PaperIvory else PaperWarm),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) Color(0xFFD9534F) else InkCharcoal,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = LoopType.HeadlineMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold),
                color = if (isDestructive) Color(0xFFD9534F) else InkCharcoal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = LoopType.Metadata.copy(fontSize = 12.sp),
                color = InkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = InkMuted,
            modifier = Modifier.size(13.dp)
        )
    }
}
