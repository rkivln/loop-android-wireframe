package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Apartment
import com.example.data.HouseRentalRepository
import com.example.data.LocationHelper
import com.example.data.RentalRepository
import com.example.data.models.AVATAR_PRESETS
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.HouseListing
import com.example.data.models.HouseVisitRequest
import com.example.data.models.LocationSharingPreference
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.data.models.UserActivityItem
import com.example.data.models.UserActivityType
import com.example.data.models.UserCommunityPost
import com.example.data.models.UserProfile
import com.example.ui.components.ListingPhoto
import com.example.ui.components.PhotoBitmapProcessor
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.ProfileOverviewSkeleton
import com.example.ui.components.SecondaryCTA
import com.example.ui.components.SectionLabel
import com.example.ui.components.UserAvatar
import com.example.update.AppUpdateCoordinator
import com.example.update.AppUpdateUiState
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.AccentYellowSoft
import com.example.ui.theme.AlertCrimson
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
import com.example.ui.theme.getIconVector
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ProfileTab(val label: String) {
    OVERVIEW("Overview & Bio"),
    MY_POSTS("Posted Activities"),
    ACTIVITY("Activity Ledger")
}

@Composable
fun ProfileScreen(
    onNavigateBookings: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSignOut: () -> Unit = {},
    initialOpenBookings: Boolean = false,
    highlightedBookingId: String? = null,
    onBookingsSheetOpened: () -> Unit = {},
    onBookingsSheetDismissed: () -> Unit = {},
    initialOpenCreatePost: Boolean = false,
    onCreatePostOpened: () -> Unit = {},
    initialOpenHouseVisits: Boolean = false,
    onHouseVisitsSheetOpened: () -> Unit = {},
    onOpenItem: (RentalItem) -> Unit = {},
    onOpenHouse: (HouseListing) -> Unit = {},
    onMessageUser: (RentalOwner, RentalItem?) -> Unit = { _, _ -> },
    onMessageHouseUser: (HouseVisitRequest) -> Unit = {},
    bottomBarPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val userProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val userActivities by RentalRepository.userActivityFlow.collectAsStateWithLifecycle()
    val userPosts by RentalRepository.myCommunityPostsFlow.collectAsStateWithLifecycle()
    val allItems by RentalRepository.itemsFlow.collectAsStateWithLifecycle()
    val bookings by RentalRepository.bookingsFlow.collectAsStateWithLifecycle()
    val myHouses by HouseRentalRepository.myPublishedHousesFlow.collectAsStateWithLifecycle()
    val myTenantVisits by HouseRentalRepository.myTenantVisitsFlow.collectAsStateWithLifecycle()
    val incomingHouseVisits by HouseRentalRepository.incomingLandlordVisitsFlow.collectAsStateWithLifecycle()
    val isProfileLoading by RentalRepository.isProfileLoadingFlow.collectAsStateWithLifecycle()
    val isBookingsLoading by RentalRepository.isBookingsLoadingFlow.collectAsStateWithLifecycle()
    val updateState by AppUpdateCoordinator.updateState.collectAsStateWithLifecycle()
    val availableUpdateTag = (updateState as? AppUpdateUiState.UpdateAvailable)?.release?.tagName

    var selectedTab by remember { mutableStateOf(if (initialOpenCreatePost) ProfileTab.MY_POSTS else ProfileTab.OVERVIEW) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isCreatingPost by remember { mutableStateOf(initialOpenCreatePost) }
    var editingPost by remember { mutableStateOf<UserCommunityPost?>(null) }
    var pendingDeletePost by remember { mutableStateOf<UserCommunityPost?>(null) }
    var isShowingBookings by remember { mutableStateOf(initialOpenBookings) }
    var isShowingHouseVisits by remember { mutableStateOf(initialOpenHouseVisits) }
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

    LaunchedEffect(initialOpenBookings) {
        if (initialOpenBookings) {
            isShowingBookings = true
            onBookingsSheetOpened()
        }
    }

    LaunchedEffect(initialOpenCreatePost) {
        if (initialOpenCreatePost) {
            selectedTab = ProfileTab.MY_POSTS
            isCreatingPost = true
            onCreatePostOpened()
        }
    }

    LaunchedEffect(initialOpenHouseVisits) {
        if (initialOpenHouseVisits) {
            isShowingHouseVisits = true
            onHouseVisitsSheetOpened()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperIvory)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp + bottomBarPadding)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Studio Shelf",
                            style = LoopType.HeroDisplayLarge.copy(fontSize = 28.sp),
                            color = InkCharcoal
                        )
                        Text(
                            text = "Creator identity, posted requests & activity ledger",
                            style = LoopType.Metadata,
                            color = InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Edit Profile CTA Pill
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorder, RoundedCornerShape(24.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { isEditingProfile = true }
                                )
                                .padding(horizontal = 14.dp)
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

                        // Settings Icon Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorder, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = onOpenSettings
                                )
                                .testTag("profile_settings_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = if (availableUpdateTag != null) {
                                    "Settings, update $availableUpdateTag available"
                                } else {
                                    "Settings"
                                },
                                tint = InkCharcoal,
                                modifier = Modifier.size(20.dp)
                            )
                            if (availableUpdateTag != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(AccentForestGreen)
                                        .border(1.5.dp, PaperPureWhite, CircleShape)
                                        .testTag("profile_settings_update_dot")
                                )
                            }
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
                        val authPhotoUrl = remember { Firebase.auth.currentUser?.photoUrl?.toString() }
                        UserAvatar(
                            avatarUrl = userProfile.resolvedAvatarUrl(authPhotoUrl),
                            initials = userProfile.initials,
                            size = 68.dp,
                            avatarPresetIndex = userProfile.avatarPresetIndex,
                            borderColor = PaperBorderSubtle,
                            borderWidth = 1.dp,
                            contentDescription = "Your profile photo"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.displayName.ifBlank { "Community Member" },
                                    style = LoopType.HeadlineMedium.copy(fontSize = 19.sp),
                                    color = InkCharcoal
                                )
                                if (userProfile.isVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verified Identity",
                                        tint = AccentForestGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = userProfile.location.ifBlank { "Location not set" },
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
                                        text = if (userProfile.reviewCount > 0 && userProfile.rating > 0f) {
                                            "${userProfile.rating} (${userProfile.reviewCount})"
                                        } else {
                                            "No reviews yet"
                                        },
                                        style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, color = InkCharcoal)
                                    )
                                }

                                if (userProfile.memberSince.isNotBlank()) {
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = tab.getIconVector(),
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isSelected) InkWhite else InkCharcoal
                                )
                                Text(
                                    text = tab.label,
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
            }

            // Tab Content Rendering
            when (selectedTab) {
                ProfileTab.OVERVIEW -> {
                    if (isProfileLoading && userProfile.uid.isBlank()) {
                        item {
                            ProfileOverviewSkeleton()
                        }
                    } else {
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
                    if (userProfile.badges.isNotEmpty()) {
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
                                count = "${userProfile.listingsCount}",
                                label = "Pieces Listed",
                                modifier = Modifier.weight(1f)
                            )
                            EditorialMetricBox(
                                count = "${userProfile.rentalsCompleted}",
                                label = "Completed",
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
                            val activeBookingsCount = bookings.count { !it.isCompleted }
                            val totalVisitsCount = myTenantVisits.size + incomingHouseVisits.size
                            EditorialMenuRow(
                                icon = Icons.Default.ShoppingBag,
                                title = "Active Reservations",
                                badge = "$activeBookingsCount Active",
                                onClick = {
                                    isShowingBookings = true
                                    onNavigateBookings()
                                }
                            )
                            EditorialMenuRow(
                                icon = Icons.Default.Apartment,
                                title = "House Visits & Inquiries",
                                badge = if (myHouses.isNotEmpty()) {
                                    "$totalVisitsCount Visits · ${myHouses.size} Homes"
                                } else {
                                    "$totalVisitsCount Visits"
                                },
                                onClick = { isShowingHouseVisits = true }
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
                                title = "Settings & Privacy",
                                badge = if (availableUpdateTag != null) {
                                    "Update $availableUpdateTag"
                                } else {
                                    userProfile.settings.profileVisibility.label
                                },
                                onClick = onOpenSettings
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AlertCrimson
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp)
                                .height(48.dp)
                                .testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign Out of Google Account",
                                style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = pType.getIconVector(),
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp),
                                            tint = if (isSelected) InkWhite else InkCharcoal
                                        )
                                        Text(
                                            text = "${pType.title} ($count)",
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
                                    Icon(
                                        imageVector = Icons.Default.Handshake,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = InkCharcoal
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
                                onEdit = { editingPost = post },
                                onToggleStatus = {
                                    RentalRepository.toggleCommunityPostStatus(post.id)
                                    showFeedback("Status updated for '${post.title}'.")
                                },
                                onDelete = {
                                    pendingDeletePost = post
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
                            ActivityTimelineItem(
                                activity = activity,
                                onClick = {
                                    when (activity.type) {
                                        UserActivityType.RESERVATION -> isShowingBookings = true
                                        UserActivityType.LISTING_PUBLISHED -> {
                                            allItems.find { it.id == activity.relatedId }?.let(onOpenItem)
                                        }
                                        UserActivityType.COMMUNITY_EVENT,
                                        UserActivityType.HELP_REQUEST,
                                        UserActivityType.STUDY_GROUP -> {
                                            selectedTab = ProfileTab.MY_POSTS
                                        }
                                        UserActivityType.HOUSE_VISIT,
                                        UserActivityType.HOUSE_LISTED -> isShowingHouseVisits = true
                                        UserActivityType.HUB_CHECKIN -> Unit
                                    }
                                }
                            )
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
            BackHandler { isEditingProfile = false }
            EditProfileModalSheet(
                currentProfile = userProfile,
                bottomBarPadding = bottomBarPadding,
                onSave = { updatedProfile ->
                    RentalRepository.updateUserProfile(updatedProfile) { res ->
                        res.onSuccess {
                            showFeedback("Bio & profile updated successfully.")
                        }
                    }
                    isEditingProfile = false
                },
                onDismiss = { isEditingProfile = false }
            )
        }

        // Create or Edit Community Event / Help Request Modal Sheet
        AnimatedVisibility(
            visible = isCreatingPost || editingPost != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            BackHandler {
                isCreatingPost = false
                editingPost = null
            }
            CreateCommunityPostModalSheet(
                editingPost = editingPost,
                onPostCreated = { title, type, desc, loc, dt, lat, lng, imageUrl ->
                    RentalRepository.createCommunityPost(
                        title = title,
                        type = type,
                        description = desc,
                        location = loc,
                        dateTime = dt,
                        latitude = lat,
                        longitude = lng,
                        imageUrl = imageUrl
                    )
                    isCreatingPost = false
                    selectedTab = ProfileTab.MY_POSTS
                    showFeedback("Community ${type.title} published!")
                },
                onPostUpdated = { updated ->
                    RentalRepository.updateCommunityPost(updated)
                    editingPost = null
                    showFeedback("Updated '${updated.title}'.")
                },
                onPostDeleted = { toDelete ->
                    editingPost = null
                    pendingDeletePost = toDelete
                },
                onDismiss = {
                    isCreatingPost = false
                    editingPost = null
                }
            )
        }

        // Active Reservations Modal Sheet
        AnimatedVisibility(
            visible = isShowingBookings,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            BookingsModalSheet(
                bookings = bookings,
                currentUserId = userProfile.uid,
                highlightedBookingId = highlightedBookingId,
                isLoading = isBookingsLoading,
                onVerifyHandoverPin = { bookingId, pin ->
                    RentalRepository.verifyHandoverPin(bookingId, pin).onSuccess {
                        showFeedback("Handover verified — rental is now active!")
                    }
                },
                onCompleteBooking = { bookingId ->
                    RentalRepository.completeBooking(bookingId).onSuccess {
                        showFeedback("Gear marked returned and relisted!")
                    }
                },
                onCancelBooking = { bookingId ->
                    RentalRepository.cancelBooking(bookingId).onSuccess {
                        showFeedback("Reservation cancelled and gear relisted.")
                    }
                },
                onSubmitReview = { bookingId, stars, comment ->
                    RentalRepository.submitBookingReview(bookingId, stars, comment).onSuccess {
                        showFeedback("Thank you for rating!")
                    }
                },
                onOpenItem = onOpenItem,
                onMessageCounterparty = { booking ->
                    val isHost = booking.hostId == userProfile.uid
                    val target = if (isHost) {
                        val name = booking.renterName.ifBlank { "Member" }
                        RentalOwner(
                            id = booking.renterId,
                            name = name,
                            badge = "Verified Borrower",
                            initials = name.trim().split(" ").filter { it.isNotBlank() }
                                .take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "M" }
                        )
                    } else {
                        booking.item.owner
                    }
                    onMessageUser(target, booking.item)
                },
                onDismiss = {
                    isShowingBookings = false
                    onBookingsSheetDismissed()
                }
            )
        }

        AnimatedVisibility(
            visible = isShowingHouseVisits,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            HouseVisitsSheet(
                bottomBarPadding = bottomBarPadding,
                onOpenHouse = { house ->
                    isShowingHouseVisits = false
                    onOpenHouse(house)
                },
                onMessageCounterparty = { visit ->
                    isShowingHouseVisits = false
                    onMessageHouseUser(visit)
                },
                onDismiss = { isShowingHouseVisits = false }
            )
        }

        pendingDeletePost?.let { postToDelete ->
            AlertDialog(
                onDismissRequest = { pendingDeletePost = null },
                containerColor = PaperPureWhite,
                titleContentColor = InkCharcoal,
                textContentColor = InkSecondary,
                title = {
                    Text(
                        text = "Delete community post?",
                        style = LoopType.HeadlineMedium.copy(fontSize = 18.sp)
                    )
                },
                text = {
                    Text(
                        text = "'${postToDelete.title}' will be permanently removed from your profile and the neighborhood feed.",
                        style = LoopType.BodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            RentalRepository.deleteCommunityPost(postToDelete.id)
                            pendingDeletePost = null
                            showFeedback("Post '${postToDelete.title}' removed.")
                        },
                        modifier = Modifier.testTag("confirm_delete_post_btn")
                    ) {
                        Text(
                            text = "Delete",
                            color = AlertCrimson,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeletePost = null }) {
                        Text("Cancel", color = InkCharcoal)
                    }
                }
            )
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: UserCommunityPost,
    onEdit: () -> Unit,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = post.type.getIconVector(),
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = InkCharcoal
                            )
                            Text(
                                text = post.type.title,
                                style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                color = InkCharcoal
                            )
                        }
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
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Post Options",
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
                                    text = "Edit Post",
                                    style = LoopType.BodySmall,
                                    color = InkCharcoal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = InkCharcoal,
                                    modifier = Modifier.size(17.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )

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
                                    color = AlertCrimson
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = AlertCrimson,
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

            if (!post.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                ListingPhoto(
                    imageUrl = post.imageUrl,
                    fallbackRes = 0,
                    contentDescription = post.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
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

            // Quick Edit & Status Toggle Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                        .clickable(onClick = onEdit)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("edit_profile_post_${post.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Post",
                            tint = InkCharcoal,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Edit",
                            style = LoopType.Metadata.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkCharcoal
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isCompleted) PaperPureWhite else AccentForestGreen)
                        .border(1.dp, if (isCompleted) PaperBorder else AccentForestGreen, RoundedCornerShape(100.dp))
                        .clickable(onClick = onToggleStatus)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = InkWhite
                            )
                        }
                        Text(
                            text = if (isCompleted) "Reopen" else "Mark Resolved",
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

@Composable
private fun ActivityTimelineItem(
    activity: UserActivityItem,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Pill
            val iconBg = when (activity.type) {
                UserActivityType.RESERVATION -> AccentPowderBlue
                UserActivityType.COMMUNITY_EVENT, UserActivityType.HOUSE_VISIT, UserActivityType.HOUSE_LISTED -> AccentMint
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
                Icon(
                    imageVector = activity.getIconVector(),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = InkCharcoal
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
    onDismiss: () -> Unit,
    bottomBarPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authPhotoUrl = remember {
        Firebase.auth.currentUser?.photoUrl?.toString()?.trim()?.takeIf { it.isNotBlank() }?.take(500)
    }
    var displayName by remember { mutableStateOf(currentProfile.displayName) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var location by remember { mutableStateOf(currentProfile.location) }
    var selectedAvatarIndex by remember { mutableIntStateOf(currentProfile.avatarPresetIndex) }
    var selectedAvatarUri by remember { mutableStateOf(currentProfile.customAvatarUri) }
    var isLocatingGps by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val sheetBottomClearance = maxOf(
        imeBottom + 12.dp,
        if (bottomBarPadding > 0.dp) bottomBarPadding + 10.dp else navBottom + 16.dp
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
            .statusBarsPadding()
            .padding(top = 28.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(PaperPureWhite)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Avatar Source & Theme Picker
                val previewInitials = remember(displayName) {
                    displayName.trim().split(" ").filter { it.isNotBlank() }
                        .take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "U" }
                }
                val effectivePreviewUrl = remember(selectedAvatarUri, authPhotoUrl) {
                    when {
                        selectedAvatarUri == null -> authPhotoUrl
                        selectedAvatarUri.isNullOrBlank() -> null
                        else -> selectedAvatarUri
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserAvatar(
                            avatarUrl = effectivePreviewUrl,
                            initials = previewInitials,
                            size = 52.dp,
                            avatarPresetIndex = selectedAvatarIndex,
                            borderColor = PaperBorderSubtle,
                            borderWidth = 1.dp,
                            contentDescription = "Selected profile avatar preview"
                        )
                        Column {
                            Text(
                                text = "STUDIO AVATAR",
                                style = LoopType.EditorialTag,
                                color = InkSecondary
                            )
                            Text(
                                text = if (!effectivePreviewUrl.isNullOrBlank()) {
                                    "Email account photo active"
                                } else {
                                    "${AVATAR_PRESETS.getOrElse(selectedAvatarIndex) { AVATAR_PRESETS[0] }.label} preset"
                                },
                                style = LoopType.BodySmall,
                                color = InkCharcoal
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (authPhotoUrl != null) {
                            val isEmailSelected = !effectivePreviewUrl.isNullOrBlank()
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedAvatarUri = authPhotoUrl }
                                    .testTag("edit_profile_email_avatar_option"),
                                contentAlignment = Alignment.Center
                            ) {
                                UserAvatar(
                                    avatarUrl = authPhotoUrl,
                                    initials = previewInitials,
                                    size = 48.dp,
                                    avatarPresetIndex = selectedAvatarIndex,
                                    borderColor = if (isEmailSelected) AccentForestGreen else PaperBorder,
                                    borderWidth = if (isEmailSelected) 3.dp else 1.dp,
                                    contentDescription = "Use email profile photo"
                                )
                            }
                        }

                        AVATAR_PRESETS.forEach { preset ->
                            val isSelected = effectivePreviewUrl.isNullOrBlank() && selectedAvatarIndex == preset.id
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.bgHex1))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) AccentForestGreen else PaperBorder,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedAvatarIndex = preset.id
                                        selectedAvatarUri = ""
                                    }
                                    .testTag("edit_profile_preset_${preset.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = preset.getIconVector(),
                                    contentDescription = preset.label,
                                    modifier = Modifier.size(20.dp),
                                    tint = InkWhite
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
                        if (displayName.isEmpty()) {
                            Text(
                                text = "Your full name",
                                style = TextStyle(fontSize = 14.sp, color = InkMuted)
                            )
                        }
                        BasicTextField(
                            value = displayName,
                            onValueChange = { displayName = it; errorMessage = null },
                            textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOCATION / NEIGHBORHOOD",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(AccentMint)
                                .clickable {
                                    if (LocationHelper.hasLocationPermission(context)) {
                                        detectLocation()
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("edit_profile_gps_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Detect GPS",
                                tint = AccentForestGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isLocatingGps) "Locating..." else "Use GPS",
                                style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                                color = AccentForestGreen
                            )
                        }
                    }
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
                        if (location.isEmpty()) {
                            Text(
                                text = "Neighborhood or city",
                                style = TextStyle(fontSize = 14.sp, color = InkMuted)
                            )
                        }
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
                        if (bio.isEmpty()) {
                            Text(
                                text = "Share what you create, lend, or explore...",
                                style = TextStyle(fontSize = 13.5.sp, color = InkMuted, lineHeight = 19.sp)
                            )
                        }
                        BasicTextField(
                            value = bio,
                            onValueChange = { if (it.length <= 250) bio = it },
                            textStyle = TextStyle(fontSize = 13.5.sp, color = InkCharcoal, lineHeight = 19.sp),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = LoopType.Metadata.copy(color = AlertCrimson)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 22.dp,
                        end = 22.dp,
                        top = 10.dp,
                        bottom = sheetBottomClearance
                    )
            ) {
                PrimaryCTA(
                    text = "Save Bio & Profile",
                    onClick = {
                        val sanitizedName = displayName.trim().take(60)
                        if (sanitizedName.isBlank()) {
                            errorMessage = "Please enter your display name."
                            return@PrimaryCTA
                        }
                        val sanitizedBio = bio.trim().take(250)
                        val sanitizedLocation = location.trim().take(80)
                        val persistedAvatarUri = when {
                            selectedAvatarUri == null -> authPhotoUrl ?: ""
                            else -> selectedAvatarUri?.trim().orEmpty()
                        }
                        val updated = currentProfile.copy(
                            displayName = sanitizedName,
                            bio = sanitizedBio,
                            location = sanitizedLocation,
                            avatarPresetIndex = selectedAvatarIndex,
                            customAvatarUri = persistedAvatarUri
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
    editingPost: UserCommunityPost? = null,
    onPostCreated: (
        title: String,
        type: CommunityPostType,
        desc: String,
        loc: String,
        dt: String,
        lat: Double?,
        lng: Double?,
        imageUrl: String?
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onPostUpdated: (UserCommunityPost) -> Unit = {},
    onPostDeleted: ((UserCommunityPost) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isEditMode = editingPost != null
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var title by remember(editingPost?.id) { mutableStateOf(editingPost?.title ?: "") }
    var selectedType by remember(editingPost?.id) {
        mutableStateOf(editingPost?.type ?: CommunityPostType.EVENT)
    }
    var description by remember(editingPost?.id) { mutableStateOf(editingPost?.description ?: "") }
    var location by remember(editingPost?.id) { mutableStateOf(editingPost?.location ?: "") }
    var dateTime by remember(editingPost?.id) { mutableStateOf(editingPost?.dateTime ?: "") }
    var latitude by remember(editingPost?.id) { mutableStateOf(editingPost?.latitude) }
    var longitude by remember(editingPost?.id) { mutableStateOf(editingPost?.longitude) }
    var status by remember(editingPost?.id) {
        mutableStateOf(editingPost?.status ?: CommunityPostStatus.ACTIVE)
    }
    var imageUrl by remember(editingPost?.id) { mutableStateOf(editingPost?.imageUrl) }
    var isProcessingPhoto by remember { mutableStateOf(false) }
    var isLocatingGps by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coverPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isProcessingPhoto = true
            coroutineScope.launch {
                val encoded = withContext(Dispatchers.IO) {
                    val bmp = PhotoBitmapProcessor.loadOrientedSourceBitmap(
                        context = context,
                        uri = uri,
                        maxEdge = 768
                    )
                    bmp?.let {
                        PhotoBitmapProcessor.bitmapToBase64DataUri(it, targetMaxBytes = 140_000)
                    }
                }
                if (encoded != null) {
                    imageUrl = encoded
                    errorMessage = null
                } else {
                    errorMessage = "Could not load selected photo. Please try another image."
                }
                isProcessingPhoto = false
            }
        }
    }

    val detectPostGps: (Boolean) -> Unit = { overwriteManual ->
        isLocatingGps = true
        LocationHelper.fetchCurrentLocation(
            context = context,
            onLocationReceived = { lat, lng ->
                latitude = lat
                longitude = lng
                coroutineScope.launch {
                    val resolved = LocationHelper.reverseGeocodeLocation(context, lat, lng)
                    if (!resolved.isNullOrBlank() && (overwriteManual || location.isBlank())) {
                        location = resolved
                    }
                    isLocatingGps = false
                }
            },
            onLocationUnavailable = { isLocatingGps = false }
        )
    }

    val postLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) detectPostGps(true)
    }

    val userSettings by RentalRepository.userSettingsFlow.collectAsStateWithLifecycle()

    LaunchedEffect(editingPost?.id) {
        if (editingPost == null &&
            userSettings.locationSharing != LocationSharingPreference.HIDDEN &&
            LocationHelper.hasLocationPermission(context)
        ) {
            detectPostGps(false)
        }
    }

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
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(PaperPureWhite)
                .clickable(enabled = false) {}
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditMode) "Edit Community Initiative" else "New Community Initiative",
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
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) InkCharcoal else PaperIvory)
                                .border(1.dp, if (isSelected) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                                .clickable { selectedType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = type.getIconVector(),
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isSelected) InkWhite else InkCharcoal
                                )
                                Text(
                                    text = type.title,
                                    style = LoopType.Metadata.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) InkWhite else InkCharcoal
                                )
                            }
                        }
                    }
                }

                // Status toggle in Edit Mode
                if (isEditMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "POST STATUS",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                CommunityPostStatus.ACTIVE to "Active / Open",
                                CommunityPostStatus.COMPLETED to "Resolved"
                            ).forEach { (candidateStatus, label) ->
                                val isSelected = status == candidateStatus
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) AccentMint else PaperIvory)
                                        .border(
                                            1.dp,
                                            if (isSelected) AccentForestGreen else PaperBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { status = candidateStatus }
                                        .testTag("post_status_${candidateStatus.name}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = LoopType.Metadata.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isSelected) AccentForestGreen else InkCharcoal
                                    )
                                }
                            }
                        }
                    }
                }

                // Optional Cover Photo Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "COVER PHOTO (OPTIONAL)",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    if (!imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(PaperIvory)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ListingPhoto(
                                    imageUrl = imageUrl,
                                    fallbackRes = 0,
                                    contentDescription = "Community cover photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isProcessingPhoto) "Optimizing image..." else "Cover photo attached",
                                        style = LoopType.BodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = InkCharcoal
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(PaperPureWhite)
                                                .border(1.dp, PaperBorder, RoundedCornerShape(10.dp))
                                                .clickable(enabled = !isProcessingPhoto) {
                                                    coverPhotoPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                                .testTag("post_replace_photo_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Replace",
                                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                                color = InkCharcoal
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(PaperPureWhite)
                                                .border(1.dp, AlertCrimson.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                                .clickable { imageUrl = null }
                                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                                .testTag("post_remove_photo_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Remove",
                                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                                color = AlertCrimson
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PaperIvory)
                                .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
                                .clickable(enabled = !isProcessingPhoto) {
                                    coverPhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("post_add_photo_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = "Add Cover Photo",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isProcessingPhoto) "Processing photo..." else "Add Cover Photo",
                                    style = LoopType.Metadata.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                                    color = InkCharcoal
                                )
                            }
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
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaperIvory)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (title.isEmpty()) {
                            Text(
                                text = "e.g. Weekend Repair Circle or Tripod Needed",
                                style = TextStyle(fontSize = 13.5.sp, color = InkMuted)
                            )
                        }
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it; errorMessage = null },
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
                        if (description.isEmpty()) {
                            Text(
                                text = "Share details, needed items, or meeting instructions...",
                                style = TextStyle(fontSize = 13.sp, color = InkMuted)
                            )
                        }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LOCATION",
                                style = LoopType.EditorialTag,
                                color = InkSecondary
                            )
                            Text(
                                text = if (isLocatingGps) "GPS..." else "Use GPS",
                                style = LoopType.Metadata.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = AccentForestGreen,
                                modifier = Modifier
                                    .clickable {
                                        if (LocationHelper.hasLocationPermission(context)) {
                                            detectPostGps(true)
                                        } else {
                                            postLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    }
                                    .testTag("post_detect_gps_btn")
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PaperIvory)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (location.isEmpty()) {
                                Text(
                                    text = "Neighborhood or GPS",
                                    style = TextStyle(fontSize = 13.sp, color = InkMuted)
                                )
                            }
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
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PaperIvory)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (dateTime.isEmpty()) {
                                Text(
                                    text = "e.g. Today · 5:00 PM",
                                    style = TextStyle(fontSize = 13.sp, color = InkMuted)
                                )
                            }
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
                        style = LoopType.Metadata.copy(color = AlertCrimson),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                PrimaryCTA(
                    text = if (isEditMode) "Save Changes" else "Publish to Community Feed",
                    enabled = !isProcessingPhoto,
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Please enter an initiative title."
                        } else if (editingPost != null) {
                            onPostUpdated(
                                editingPost.copy(
                                    title = title.trim(),
                                    type = selectedType,
                                    description = description.trim(),
                                    location = location.trim(),
                                    dateTime = dateTime.trim(),
                                    status = status,
                                    latitude = latitude,
                                    longitude = longitude,
                                    imageUrl = imageUrl
                                )
                            )
                        } else {
                            onPostCreated(
                                title.trim(),
                                selectedType,
                                description.trim(),
                                location.trim(),
                                dateTime.trim(),
                                latitude,
                                longitude,
                                imageUrl
                            )
                        }
                    },
                    testTag = "publish_community_post_btn"
                )

                if (editingPost != null && onPostDeleted != null) {
                    OutlinedButton(
                        onClick = { onPostDeleted(editingPost) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AlertCrimson
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("delete_community_post_modal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Post",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Delete Post",
                            style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}
