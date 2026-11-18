package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.LocationHelper
import com.example.data.ThemePreferencesStore
import com.example.data.models.BlockedUserEntry
import com.example.data.models.CommunityPostStatus
import com.example.data.models.LocationSharingPreference
import com.example.data.models.ProfileVisibility
import com.example.data.models.RentalItem
import com.example.data.models.UserActivityItem
import com.example.data.models.UserActivityType
import com.example.data.models.UserCommunityPost
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.ListingPhoto
import com.example.ui.components.SecondaryCTA
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.getIconVector
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite

@Composable
fun MyPostsSubScreen(
    myItems: List<RentalItem>,
    myCommunityPosts: List<UserCommunityPost>,
    onBack: () -> Unit,
    onOpenItem: (RentalItem) -> Unit,
    onEditItem: (RentalItem) -> Unit = {},
    onToggleItemAvailability: (String) -> Unit = {},
    onDeleteItem: (String) -> Unit,
    onEditPost: (UserCommunityPost) -> Unit = {},
    onTogglePostStatus: (String) -> Unit,
    onDeletePost: (String) -> Unit,
    onCreateListing: () -> Unit
) {
    BackHandler(onBack = onBack)
    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Gear, 2 = Community

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .navigationBarsPadding()
            .testTag("settings_my_posts_screen")
    ) {
        EditorialTopBar(title = "My Posts", onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                0 to "All (${myItems.size + myCommunityPosts.size})",
                1 to "Published Gear (${myItems.size})",
                2 to "Community (${myCommunityPosts.size})"
            ).forEach { (idx, label) ->
                val selected = selectedFilter == idx
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (selected) InkCharcoal else PaperPureWhite)
                        .border(1.dp, if (selected) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                        .clickable { selectedFilter = idx }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = LoopType.Metadata.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                        color = if (selected) InkWhite else InkCharcoal
                    )
                }
            }
        }

        val showItems = selectedFilter == 0 || selectedFilter == 1
        val showPosts = selectedFilter == 0 || selectedFilter == 2
        val isEmpty = (!showItems || myItems.isEmpty()) && (!showPosts || myCommunityPosts.isEmpty())

        if (isEmpty) {
            SettingsEmptyStateCard(
                icon = Icons.Default.CameraAlt,
                title = "No posts published yet",
                subtitle = "Listings you publish and neighborhood community posts will appear here for easy management.",
                actionText = "Publish a Piece",
                onAction = onCreateListing
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showItems && myItems.isNotEmpty()) {
                    items(myItems, key = { "item_${it.id}" }) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                                .clickable { onOpenItem(item) }
                                .padding(12.dp)
                                .testTag("my_post_item_${item.id}"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isBooked = !item.availableToday && item.activeBookingId != null
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ListingPhoto(
                                    imageUrl = item.imageUrl,
                                    fallbackRes = item.primaryImageRes,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.category.title.uppercase(),
                                            style = LoopType.EditorialTag.copy(fontSize = 9.5.sp),
                                            color = AccentForestGreen
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(
                                                    when {
                                                        isBooked -> AccentPeach
                                                        item.availableToday -> AccentMint
                                                        else -> AccentBeigeOat
                                                    }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = when {
                                                    isBooked -> "Booked"
                                                    item.availableToday -> "Available"
                                                    else -> "Paused"
                                                },
                                                style = LoopType.Metadata.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = when {
                                                    isBooked -> AlertCrimson
                                                    item.availableToday -> AccentForestGreen
                                                    else -> InkSecondary
                                                }
                                            )
                                        }
                                    }
                                    Text(
                                        text = item.title,
                                        style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                                        color = InkCharcoal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "₹${item.pricePerDay}/day · ${item.location.ifBlank { "Neighborhood" }}",
                                        style = LoopType.Metadata.copy(fontSize = 11.5.sp),
                                        color = InkSecondary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(
                                            when {
                                                isBooked -> AccentPeach.copy(alpha = 0.6f)
                                                item.availableToday -> PaperIvory
                                                else -> AccentMint
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            when {
                                                isBooked -> AlertCrimson.copy(alpha = 0.25f)
                                                item.availableToday -> PaperBorder
                                                else -> AccentForestGreen
                                            },
                                            RoundedCornerShape(100.dp)
                                        )
                                        .clickable(enabled = !isBooked) { onToggleItemAvailability(item.id) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("toggle_my_item_${item.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when {
                                            isBooked -> "Booked"
                                            item.availableToday -> "Pause"
                                            else -> "Activate"
                                        },
                                        style = LoopType.Metadata.copy(
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = when {
                                            isBooked -> AlertCrimson
                                            item.availableToday -> InkCharcoal
                                            else -> AccentForestGreen
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(PaperIvory)
                                        .border(1.dp, PaperBorder, CircleShape)
                                        .clickable { onEditItem(item) }
                                        .testTag("edit_my_item_${item.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit listing",
                                        tint = InkCharcoal,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                if (!isBooked) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .clickable { onDeleteItem(item.id) }
                                            .testTag("delete_my_item_${item.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete listing",
                                            tint = AlertCrimson,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (showPosts && myCommunityPosts.isNotEmpty()) {
                    items(myCommunityPosts, key = { "post_${it.id}" }) { post ->
                        val isResolved = post.status == CommunityPostStatus.COMPLETED
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                                .padding(14.dp)
                                .testTag("my_community_post_${post.id}"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(if (isResolved) AccentBeigeOat else AccentMint)
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = post.type.getIconVector(),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (isResolved) InkSecondary else AccentForestGreen
                                    )
                                    Text(
                                        text = if (isResolved) "Resolved" else "Active",
                                        style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                        color = if (isResolved) InkSecondary else AccentForestGreen
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .clip(RoundedCornerShape(100.dp))
                                            .clickable { onTogglePostStatus(post.id) }
                                            .padding(horizontal = 10.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isResolved) "Reopen" else "Resolve",
                                            style = LoopType.Metadata.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                                            color = AccentForestGreen
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .clickable { onEditPost(post) }
                                            .testTag("edit_my_post_${post.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit post",
                                            tint = InkCharcoal,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .clickable { onDeletePost(post.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete post",
                                            tint = AlertCrimson,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }
                            if (!post.imageUrl.isNullOrBlank()) {
                                ListingPhoto(
                                    imageUrl = post.imageUrl,
                                    fallbackRes = 0,
                                    contentDescription = post.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                            Text(
                                text = post.title,
                                style = LoopType.TitleMedium.copy(fontSize = 15.5.sp),
                                color = InkCharcoal
                            )
                            Text(
                                text = post.description,
                                style = LoopType.BodySmall,
                                color = InkSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedPostsSubScreen(
    savedItems: List<RentalItem>,
    onBack: () -> Unit,
    onOpenItem: (RentalItem) -> Unit,
    onUnsaveItem: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .navigationBarsPadding()
            .testTag("settings_saved_posts_screen")
    ) {
        EditorialTopBar(title = "Saved Posts", onBack = onBack)

        if (savedItems.isEmpty()) {
            SettingsEmptyStateCard(
                icon = Icons.Default.Bookmark,
                title = "No saved posts yet",
                subtitle = "Tap the heart icon on any marketplace listing to save it here for quick booking later."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedItems, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                            .clickable { onOpenItem(item) }
                            .padding(12.dp)
                            .testTag("saved_post_card_${item.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ListingPhoto(
                            imageUrl = item.imageUrl,
                            fallbackRes = item.primaryImageRes,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(66.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                                color = InkCharcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "₹${item.pricePerDay}/day · Hosted by ${item.owner.name}",
                                style = LoopType.Metadata.copy(fontSize = 11.5.sp),
                                color = InkSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { onUnsaveItem(item.id) }
                                .testTag("unsave_item_${item.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkRemove,
                                contentDescription = "Remove from saved posts",
                                tint = AccentForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyActivitySubScreen(
    activities: List<UserActivityItem>,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    var selectedType by remember { mutableStateOf<UserActivityType?>(null) }

    val filtered = remember(activities, selectedType) {
        if (selectedType == null) activities else activities.filter { it.type == selectedType }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .navigationBarsPadding()
            .testTag("settings_my_activity_screen")
    ) {
        EditorialTopBar(title = "My Activity", onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                null to "All (${activities.size})",
                UserActivityType.RESERVATION to "Bookings",
                UserActivityType.LISTING_PUBLISHED to "Listings",
                UserActivityType.COMMUNITY_EVENT to "Events"
            ).forEach { (type, label) ->
                val selected = selectedType == type
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (selected) InkCharcoal else PaperPureWhite)
                        .border(1.dp, if (selected) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                        .clickable { selectedType = type }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = LoopType.Metadata.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                        color = if (selected) InkWhite else InkCharcoal
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            SettingsEmptyStateCard(
                icon = Icons.Default.Schedule,
                title = "No activity recorded yet",
                subtitle = "Your reservations, published listings, and community posts will appear here automatically."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { act ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AccentMint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = act.getIconVector(),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = AccentForestGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = act.title,
                                style = LoopType.TitleMedium.copy(fontSize = 14.5.sp),
                                color = InkCharcoal
                            )
                            Text(
                                text = act.subtitle,
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                            Text(
                                text = act.timestamp,
                                style = LoopType.Metadata.copy(fontSize = 11.sp),
                                color = InkMuted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(AccentBeigeOat)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = act.statusBadge,
                                style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                                color = InkCharcoal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileVisibilitySubScreen(
    selectedVisibility: ProfileVisibility,
    onSelect: (ProfileVisibility) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .navigationBarsPadding()
            .testTag("settings_visibility_screen")
    ) {
        EditorialTopBar(title = "Profile Visibility", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Choose who can discover your creator profile, activity highlights, and hosted pieces.",
                style = LoopType.BodyMedium,
                color = InkSecondary
            )

            ProfileVisibility.entries.forEach { option ->
                val isSelected = selectedVisibility == option
                SelectablePreferenceCard(
                    icon = if (option == ProfileVisibility.PRIVATE) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    title = option.label,
                    subtitle = option.subtitle,
                    selected = isSelected,
                    onClick = { onSelect(option) },
                    testTag = "visibility_option_${option.name}"
                )
            }
        }
    }
}

@Composable
fun BlockedUsersSubScreen(
    blockedUsers: List<BlockedUserEntry>,
    onUnblock: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .navigationBarsPadding()
            .testTag("settings_blocked_users_screen")
    ) {
        EditorialTopBar(title = "Blocked Users", onBack = onBack)

        if (blockedUsers.isEmpty()) {
            SettingsEmptyStateCard(
                icon = Icons.Default.Security,
                title = "No blocked accounts",
                subtitle = "You can block a member anytime from a chat thread or listing page. Blocked users' pieces, posts, and chats are hidden from your feeds."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(blockedUsers, key = { it.userId }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                            .padding(14.dp)
                            .testTag("blocked_user_row_${entry.userId}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AccentPeach),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = InkCharcoal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.displayName.ifBlank { "Community Member" },
                                style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                                color = InkCharcoal
                            )
                            Text(
                                text = entry.blockedAtLabel.ifBlank { "Hidden from feeds & chats" },
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(AccentMint)
                                .clickable { onUnblock(entry.userId) }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("unblock_user_btn_${entry.userId}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Unblock",
                                style = LoopType.Metadata.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                color = AccentForestGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationSharingSubScreen(
    selectedPreference: LocationSharingPreference,
    onSelect: (LocationSharingPreference) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .navigationBarsPadding()
            .testTag("settings_location_sharing_screen")
    ) {
        EditorialTopBar(title = "Location Sharing", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Control how your location is attached when publishing pieces or community initiatives.",
                style = LoopType.BodyMedium,
                color = InkSecondary
            )

            LocationSharingPreference.entries.forEach { pref ->
                SelectablePreferenceCard(
                    icon = Icons.Default.LocationOn,
                    title = pref.label,
                    subtitle = pref.subtitle,
                    selected = selectedPreference == pref,
                    onClick = { onSelect(pref) },
                    testTag = "location_pref_${pref.name}"
                )
            }
        }
    }
}

@Composable
fun PrivacyPolicySubScreen(
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .navigationBarsPadding()
            .testTag("settings_privacy_policy_screen")
    ) {
        EditorialTopBar(title = "Data & Privacy Policy", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PolicySectionCard(
                title = "1. Account & Identity",
                body = "Authentication uses Google Sign-In via Jetpack Credential Manager. Only your chosen display name, email, neighborhood label, and public bio are stored on your encrypted profile document."
            )
            PolicySectionCard(
                title = "2. Zero-Permission Photo Picker",
                body = "When uploading equipment photos, Loop uses Android's system Photo Picker. The app never scans your device library or requests broad media/storage permissions—only photos you explicitly select and edit are compressed and uploaded."
            )
            PolicySectionCard(
                title = "3. Location Controls",
                body = "Location access is strictly foreground-only and never tracked in the background. You can choose Precise GPS, Neighborhood Only, or Hidden at any time inside Privacy & Security."
            )
            PolicySectionCard(
                title = "4. Blocking & User Safety",
                body = "Blocking a member immediately hides their catalog pieces, neighborhood posts, map pins, and conversation threads from your feeds. You can review or unblock accounts at any time."
            )
        }
    }
}

@Composable
fun ManagePermissionsSubScreen(
    onRequestLocation: () -> Unit,
    onRequestNotifications: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var locationGranted by remember { mutableStateOf(LocationHelper.hasLocationPermission(context)) }
    var notificationsGranted by remember {
        mutableStateOf(ThemePreferencesStore.isNotificationPermissionGranted(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationGranted = LocationHelper.hasLocationPermission(context)
                notificationsGranted = ThemePreferencesStore.isNotificationPermissionGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val openSystemAppSettings: () -> Unit = {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .navigationBarsPadding()
            .testTag("settings_manage_permissions_screen")
    ) {
        EditorialTopBar(title = "Manage Permissions", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PermissionStatusCard(
                icon = Icons.Default.LocationOn,
                title = "Location Access",
                statusLabel = if (locationGranted) "Granted (Foreground)" else "Not Granted",
                isGranted = locationGranted,
                description = "Used only while the app is open to detect pickup neighborhood and center the local discovery map.",
                actionLabel = if (locationGranted) "System Settings" else "Grant Location",
                onAction = {
                    if (locationGranted) openSystemAppSettings() else onRequestLocation()
                },
                testTag = "permission_location_btn"
            )

            PermissionStatusCard(
                icon = Icons.Default.NotificationsActive,
                title = "Push Notifications",
                statusLabel = if (notificationsGranted) "Allowed" else "Permission Needed",
                isGranted = notificationsGranted,
                description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    "Android 13+ runtime permission for neighborhood posts, replies, and announcements."
                } else {
                    "Managed via Android notification channel preferences."
                },
                actionLabel = if (notificationsGranted) "System Settings" else "Allow Notifications",
                onAction = {
                    if (notificationsGranted) openSystemAppSettings() else onRequestNotifications()
                },
                testTag = "permission_notifications_btn"
            )

            PermissionStatusCard(
                icon = Icons.Default.PhotoLibrary,
                title = "Photos & Media",
                statusLabel = "Photo Picker Active",
                isGranted = true,
                description = "Privacy-preserving system Photo Picker is used on demand; no broad storage permission is required.",
                actionLabel = "System Settings",
                onAction = openSystemAppSettings,
                testTag = "permission_media_btn"
            )
        }
    }
}

@Composable
private fun SelectablePreferenceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) AccentMint.copy(alpha = 0.45f) else PaperPureWhite)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) AccentForestGreen else PaperBorderSubtle,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (selected) AccentMint else PaperIvory),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) AccentForestGreen else InkSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                color = InkCharcoal
            )
            Text(
                text = subtitle,
                style = LoopType.BodySmall,
                color = InkSecondary
            )
        }
        if (selected) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(AccentForestGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = InkWhite,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun PolicySectionCard(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = AccentForestGreen,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = title,
                style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                color = InkCharcoal
            )
        }
        Text(
            text = body,
            style = LoopType.BodySmall.copy(lineHeight = 19.sp),
            color = InkSecondary
        )
    }
}

@Composable
private fun PermissionStatusCard(
    icon: ImageVector,
    title: String,
    statusLabel: String,
    isGranted: Boolean,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) AccentMint else AccentPeach),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) AccentForestGreen else InkCharcoal,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Text(
                    text = title,
                    style = LoopType.TitleMedium.copy(fontSize = 15.5.sp),
                    color = InkCharcoal
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isGranted) AccentMint else AccentPeach)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusLabel,
                    style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                    color = if (isGranted) AccentForestGreen else InkCharcoal
                )
            }
        }
        Text(
            text = description,
            style = LoopType.BodySmall,
            color = InkSecondary
        )
        Row(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PaperIvory)
                .border(1.dp, PaperBorder, RoundedCornerShape(12.dp))
                .clickable(onClick = onAction)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = InkCharcoal,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = actionLabel,
                style = LoopType.Metadata.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                color = InkCharcoal
            )
        }
    }
}

@Composable
private fun SettingsEmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
            .padding(horizontal = 22.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = InkCharcoal
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                color = InkCharcoal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = LoopType.BodySmall,
                color = InkSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                SecondaryCTA(text = actionText, onClick = onAction)
            }
        }
    }
}
