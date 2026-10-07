package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.PedalBike
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocationHelper
import com.example.data.RentalRepository
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.UserCommunityPost
import com.example.ui.components.CommunityFormData
import com.example.ui.components.CommunityInitiativeForm
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
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
import com.example.ui.theme.ParchmentBg
import kotlinx.coroutines.launch

enum class CommunityFeedFilter(val label: String, val emoji: String) {
    ALL("All Active", "✨"),
    HELP_NEEDED("Help Requests", "🤝"),
    EVENTS("Local Events", "📸"),
    STUDY_GROUPS("Study Groups", "📚")
}

data class PostWithDistance(
    val post: UserCommunityPost,
    val distanceMeters: Float,
    val distanceFormatted: String,
    val walkingTime: String,
    val cyclingTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen(
    onNavigateToChat: (hostId: String, title: String) -> Unit = { _, _ -> },
    onOpenDiscoveryMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Real-time Firestore Stream of Community Posts
    val rawPosts by RentalRepository.userCommunityPostsFlow.collectAsStateWithLifecycle()

    // GPS & Proximity State
    var userLat by remember { mutableDoubleStateOf(LocationHelper.DEFAULT_LAT) }
    var userLng by remember { mutableDoubleStateOf(LocationHelper.DEFAULT_LNG) }
    var userLocationLabel by remember { mutableStateOf("Suffren St, White Town") }
    var isGpsLive by remember { mutableStateOf(false) }

    // Search & Filter State
    var selectedFilter by remember { mutableStateOf(CommunityFeedFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isCreationSheetVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            LocationHelper.fetchCurrentLocation(
                context = context,
                onLocationReceived = { lat, lng ->
                    userLat = lat
                    userLng = lng
                    isGpsLive = true
                    userLocationLabel = "Live GPS (%.4f, %.4f)".format(lat, lng)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Synced GPS! Feed sorted by your live location.")
                    }
                }
            )
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Location permission denied. Sorting from White Town anchor.")
            }
        }
    }

    // Try fetching GPS on start if permitted
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            LocationHelper.fetchCurrentLocation(
                context = context,
                onLocationReceived = { lat, lng ->
                    userLat = lat
                    userLng = lng
                    isGpsLive = true
                    userLocationLabel = "Live GPS (%.4f, %.4f)".format(lat, lng)
                }
            )
        }
    }

    // Compute distance and sort strictly by proximity (Ascending distance)
    val proximitySortedPosts by remember(rawPosts, userLat, userLng, selectedFilter, searchQuery) {
        derivedStateOf {
            rawPosts
                .filter { it.status == CommunityPostStatus.ACTIVE }
                .filter { post ->
                    when (selectedFilter) {
                        CommunityFeedFilter.ALL -> true
                        CommunityFeedFilter.HELP_NEEDED -> post.type == CommunityPostType.HELP_REQUEST
                        CommunityFeedFilter.EVENTS -> post.type == CommunityPostType.EVENT
                        CommunityFeedFilter.STUDY_GROUPS -> post.type == CommunityPostType.STUDY_GROUP
                    }
                }
                .filter { post ->
                    if (searchQuery.isBlank()) true
                    else {
                        post.title.contains(searchQuery, ignoreCase = true) ||
                                post.description.contains(searchQuery, ignoreCase = true) ||
                                post.location.contains(searchQuery, ignoreCase = true) ||
                                post.category.title.contains(searchQuery, ignoreCase = true)
                    }
                }
                .map { post ->
                    val dist = LocationHelper.calculateDistanceMeters(
                        startLat = userLat,
                        startLng = userLng,
                        endLat = post.latitude,
                        endLng = post.longitude
                    )
                    PostWithDistance(
                        post = post,
                        distanceMeters = dist,
                        distanceFormatted = LocationHelper.formatDistance(dist),
                        walkingTime = LocationHelper.formatWalkingTime(dist),
                        cyclingTime = LocationHelper.formatCyclingTime(dist)
                    )
                }
                // Strict proximity sorting: closest items first
                .sortedBy { it.distanceMeters }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ParchmentBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // 1. Editorial Masthead
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "COMMUNITY BOARD",
                                style = LoopType.EditorialTag.copy(fontSize = 11.sp),
                                color = InkSecondary
                            )
                            Text(
                                text = "Real-time Feed",
                                style = LoopType.HeroDisplayLarge.copy(fontSize = 28.sp),
                                color = InkCharcoal
                            )
                        }

                        // Real-time Firestore Status Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AccentForestGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Firestore Live",
                                style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                                color = InkCharcoal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Active help inquiries & creator gatherings sorted by proximity to your current location in Puducherry.",
                        style = LoopType.BodyEditorial.copy(fontSize = 13.sp),
                        color = InkSecondary
                    )
                }
            }

            // 2. Proximity GPS Anchor Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isGpsLive) AccentMint.copy(alpha = 0.5f) else PaperIvory)
                        .border(1.dp, if (isGpsLive) AccentForestGreen.copy(alpha = 0.4f) else PaperBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isGpsLive) AccentForestGreen else PaperPureWhite)
                                    .border(1.dp, PaperBorderSubtle, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isGpsLive) Icons.Default.MyLocation else Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = if (isGpsLive) InkWhite else InkCharcoal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isGpsLive) "LIVE GPS PROXIMITY" else "PROXIMITY ANCHOR",
                                        style = LoopType.Metadata.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                                        color = if (isGpsLive) AccentForestGreen else InkMuted
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Nearest First",
                                        style = LoopType.Metadata.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                        color = InkSecondary
                                    )
                                }
                                Text(
                                    text = userLocationLabel,
                                    style = LoopType.BodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                                    color = InkCharcoal
                                )
                            }
                        }

                        // GPS Sync Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isGpsLive) PaperPureWhite else InkCharcoal)
                                .clickable {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        LocationHelper.fetchCurrentLocation(
                                            context = context,
                                            onLocationReceived = { lat, lng ->
                                                userLat = lat
                                                userLng = lng
                                                isGpsLive = true
                                                userLocationLabel = "Live GPS (%.4f, %.4f)".format(lat, lng)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Location refreshed! Distance updated.")
                                                }
                                            }
                                        )
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("feed_sync_gps_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = null,
                                    tint = if (isGpsLive) InkCharcoal else InkWhite,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isGpsLive) "Refresh" else "Use GPS",
                                    style = LoopType.Metadata.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGpsLive) InkCharcoal else InkWhite
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search help requests, photowalks, workshops...",
                                style = LoopType.BodyEditorial.copy(fontSize = 13.sp),
                                color = InkMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = InkMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = InkMuted,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = PaperPureWhite,
                            unfocusedContainerColor = PaperPureWhite,
                            focusedIndicatorColor = InkCharcoal,
                            unfocusedIndicatorColor = PaperBorder,
                            focusedTextColor = InkCharcoal,
                            unfocusedTextColor = InkCharcoal
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feed_search_input")
                    )
                }
            }

            // 4. Category Filter Chip Group
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommunityFeedFilter.values().forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val count = when (filter) {
                            CommunityFeedFilter.ALL -> rawPosts.count { it.status == CommunityPostStatus.ACTIVE }
                            CommunityFeedFilter.HELP_NEEDED -> rawPosts.count { it.status == CommunityPostStatus.ACTIVE && it.type == CommunityPostType.HELP_REQUEST }
                            CommunityFeedFilter.EVENTS -> rawPosts.count { it.status == CommunityPostStatus.ACTIVE && it.type == CommunityPostType.EVENT }
                            CommunityFeedFilter.STUDY_GROUPS -> rawPosts.count { it.status == CommunityPostStatus.ACTIVE && it.type == CommunityPostType.STUDY_GROUP }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) InkCharcoal else PaperPureWhite)
                                .border(
                                    1.dp,
                                    if (isSelected) InkCharcoal else PaperBorder,
                                    RoundedCornerShape(100.dp)
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("feed_filter_${filter.name}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = filter.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${filter.label} ($count)",
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
            }

            // 5. Section Counter & Map View Shortcut
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${proximitySortedPosts.size} ACTIVE INITIATIVES",
                        style = LoopType.EditorialTag.copy(fontSize = 10.5.sp),
                        color = InkSecondary
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable(onClick = onOpenDiscoveryMap)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View on Map",
                            style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = InkCharcoal)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = InkCharcoal,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // 6. Proximity-Sorted Feed Items
            if (proximitySortedPosts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🤝", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No active initiatives match",
                                style = LoopType.HeadlineMedium.copy(fontSize = 16.sp),
                                color = InkCharcoal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Be the first to post a help inquiry or organize a gathering in your quarter.",
                                style = LoopType.BodyEditorial.copy(fontSize = 12.5.sp),
                                color = InkSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(InkCharcoal)
                                    .clickable { isCreationSheetVisible = true }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "+ Post First Initiative",
                                    style = LoopType.Metadata.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = InkWhite)
                                )
                            }
                        }
                    }
                }
            } else {
                items(proximitySortedPosts, key = { it.post.id }) { item ->
                    CommunityFeedCard(
                        item = item,
                        onDirectionsClick = {
                            LocationHelper.openNavigationIntent(
                                context = context,
                                latitude = item.post.latitude,
                                longitude = item.post.longitude,
                                label = item.post.title
                            )
                        },
                        onRsvpClick = {
                            val feedback = RentalRepository.rsvpToCommunityPost(item.post.id)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(feedback)
                            }
                        },
                        onMessageCreator = {
                            onNavigateToChat("user_me", item.post.title)
                        },
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Floating Action Button: Create New Help Request or Event
        FloatingActionButton(
            onClick = { isCreationSheetVisible = true },
            containerColor = InkCharcoal,
            contentColor = InkWhite,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 80.dp)
                .testTag("feed_create_initiative_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Initiative",
                    tint = InkWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Post",
                    style = LoopType.Metadata.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = InkWhite
                )
            }
        }

        // Snackbar host for notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 70.dp)
        )
    }

    // Modal Bottom Sheet: Create New Community Initiative Form
    if (isCreationSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isCreationSheetVisible = false },
            sheetState = sheetState,
            containerColor = PaperPureWhite,
            tonalElevation = 6.dp,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 4.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PaperBorder)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMMUNITY BOARD",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Text(
                            text = "Post Request or Event",
                            style = LoopType.HeadlineMedium.copy(fontSize = 20.sp),
                            color = InkCharcoal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PaperIvory)
                            .clickable { isCreationSheetVisible = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkCharcoal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reusable Community Initiative Form
                CommunityInitiativeForm(
                    initialLatitude = userLat,
                    initialLongitude = userLng,
                    submitButtonLabel = "Publish to Community Board",
                    onSubmit = { formData ->
                        val newPost = RentalRepository.createCommunityPost(
                            title = formData.title,
                            type = formData.type,
                            description = formData.description,
                            location = formData.locationName,
                            dateTime = formData.dateTime,
                            latitude = formData.latitude,
                            longitude = formData.longitude,
                            category = formData.category
                        )
                        isCreationSheetVisible = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Published '${newPost.title}'! Real-time Firestore sync active.")
                        }
                    },
                    onCancel = { isCreationSheetVisible = false }
                )
            }
        }
    }
}

/**
 * High-Polish Community Feed Card Item
 */
@Composable
private fun CommunityFeedCard(
    item: PostWithDistance,
    onDirectionsClick: () -> Unit,
    onRsvpClick: () -> Unit,
    onMessageCreator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val post = item.post

    val typeColor = when (post.type) {
        CommunityPostType.EVENT -> AccentPeach
        CommunityPostType.HELP_REQUEST -> AccentMint
        CommunityPostType.STUDY_GROUP -> AccentWarmYellow
    }

    val typeTextColor = when (post.type) {
        CommunityPostType.EVENT -> Color(0xFFC2410C)
        CommunityPostType.HELP_REQUEST -> AccentForestGreen
        CommunityPostType.STUDY_GROUP -> Color(0xFFB45309)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("feed_card_${post.id}")
    ) {
        Column {
            // Header Row: Type Chip + Proximity Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(typeColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = post.type.emoji, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = post.type.title.uppercase(),
                        style = LoopType.Metadata.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeTextColor
                        )
                    )
                }

                // Proximity Distance Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(100.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = InkCharcoal,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.distanceFormatted,
                        style = LoopType.Metadata.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkCharcoal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = post.title,
                style = LoopType.HeadlineMedium.copy(fontSize = 17.5.sp, lineHeight = 22.sp),
                color = InkCharcoal
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Topic / Category Tag
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PaperIvory)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = post.category.iconEmoji, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.category.title,
                    style = LoopType.Metadata.copy(fontSize = 10.5.sp, color = InkSecondary)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description Body
            Text(
                text = post.description,
                style = LoopType.BodyEditorial.copy(fontSize = 13.5.sp, lineHeight = 19.sp),
                color = InkCharcoal
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location & Transit Estimates
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PaperIvory.copy(alpha = 0.6f))
                    .padding(10.dp)
            ) {
                // Location row with Directions action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = InkMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = post.location,
                            style = LoopType.Metadata.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                            color = InkCharcoal,
                            maxLines = 1
                        )
                    }

                    // Directions pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                            .clickable(onClick = onDirectionsClick)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("directions_btn_${post.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            tint = AccentForestGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Directions",
                            style = LoopType.Metadata.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentForestGreen)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Transit time estimates
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.DirectionsWalk,
                            contentDescription = null,
                            tint = InkSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = item.walkingTime,
                            style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                            color = InkSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.PedalBike,
                            contentDescription = null,
                            tint = InkSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = item.cyclingTime,
                            style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                            color = InkSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = InkSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.dateTime,
                            style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                            color = InkSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Action Row: Attendees Count + Interactive Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Response status tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AccentForestGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.attendeesOrResponses,
                        style = LoopType.Metadata.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                        color = InkCharcoal
                    )
                }

                // Interactive Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Chat / Message creator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperIvory)
                            .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                            .clickable(onClick = onMessageCreator)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("feed_chat_btn_${post.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = InkCharcoal,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Message",
                                style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                color = InkCharcoal
                            )
                        }
                    }

                    // RSVP / Offer Help CTA
                    val ctaText = when (post.type) {
                        CommunityPostType.EVENT -> "RSVP / Join"
                        CommunityPostType.HELP_REQUEST -> "Offer Help"
                        CommunityPostType.STUDY_GROUP -> "Book Desk"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(InkCharcoal)
                            .clickable(onClick = onRsvpClick)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("feed_rsvp_btn_${post.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (post.type == CommunityPostType.HELP_REQUEST) Icons.Outlined.Handshake else Icons.Default.Check,
                                contentDescription = null,
                                tint = InkWhite,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = ctaText,
                                style = LoopType.Metadata.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkWhite
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
