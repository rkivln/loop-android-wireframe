package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RentalRepository
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperPureWhite
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

/**
 * Filter tags for community activity map points.
 */
enum class CommunityMapFilter(val label: String, val emoji: String) {
    ALL("All Activities", "📍"),
    EVENTS("Photo & Gatherings", "📸"),
    HELP("Skill & Help", "🤝"),
    STUDY("Study Circles", "📚"),
    HUBS("Smart Hubs", "🔒"),
    MAKERS("Creators", "🛠️")
}

/**
 * A composable integrating Google Maps using maps-compose to display nearby community activity points:
 * local gatherings, study groups, help exchanges, Smart Hub pickup lockers, and maker gear hosts.
 */
@Composable
fun CommunityActivityMap(
    modifier: Modifier = Modifier,
    activityPoints: List<DiscoveryPinItem> = RentalRepository.discoveryPins,
    initialCenter: LatLng = LatLng(11.9338, 79.8350), // White Town Promenade, Puducherry
    initialZoom: Float = 15.2f,
    onPointSelected: (DiscoveryPinItem) -> Unit = {},
    onDirectionsClick: (DiscoveryPinItem) -> Unit = {},
    onRsvpClick: (DiscoveryPinItem) -> Unit = {},
    showFilterRow: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf(CommunityMapFilter.ALL) }
    var selectedPin by remember { mutableStateOf<DiscoveryPinItem?>(null) }
    var isSatelliteView by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCenter, initialZoom)
    }

    val mapProperties = remember(isSatelliteView) {
        MapProperties(
            isMyLocationEnabled = false,
            mapType = if (isSatelliteView) MapType.HYBRID else MapType.NORMAL
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false
        )
    }

    // Filter points based on user selected category
    val filteredPoints = remember(activityPoints, selectedFilter) {
        when (selectedFilter) {
            CommunityMapFilter.ALL -> activityPoints
            CommunityMapFilter.EVENTS -> activityPoints.filter { it.type == DiscoveryPinType.COMMUNITY_EVENT }
            CommunityMapFilter.HELP -> activityPoints.filter { it.type == DiscoveryPinType.HELP_REQUEST }
            CommunityMapFilter.STUDY -> activityPoints.filter { it.type == DiscoveryPinType.STUDY_GROUP }
            CommunityMapFilter.HUBS -> activityPoints.filter { it.type == DiscoveryPinType.PICKUP_HUB }
            CommunityMapFilter.MAKERS -> activityPoints.filter { it.type == DiscoveryPinType.USER_HOST }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("community_activity_map")
    ) {
        // Real Google Map Canvas via maps-compose
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .testTag("google_map_container"),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapClick = {
                selectedPin = null
            }
        ) {
            // Walkable Neighborhood Activity Radius Circle (500m)
            Circle(
                center = initialCenter,
                radius = 500.0,
                fillColor = AccentMint.copy(alpha = 0.22f),
                strokeColor = AccentForestGreen.copy(alpha = 0.5f),
                strokeWidth = 3f
            )

            // Dynamic Custom Activity Markers
            filteredPoints.forEach { point ->
                val pointLatLng = LatLng(point.latitude, point.longitude)
                val isSelected = selectedPin?.id == point.id
                val markerState = rememberMarkerState(key = point.id, position = pointLatLng)

                MarkerComposable(
                    keys = arrayOf(point.id, isSelected, selectedFilter),
                    state = markerState,
                    title = point.title,
                    snippet = point.locationName,
                    onClick = {
                        selectedPin = point
                        onPointSelected(point)
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLng(pointLatLng),
                                500
                            )
                        }
                        true
                    }
                ) {
                    CommunityActivityMapMarker(
                        point = point,
                        isSelected = isSelected
                    )
                }
            }
        }

        // Top Filter Bar & Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp)
        ) {
            if (showFilterRow) {
                // Category Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommunityMapFilter.values().forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
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
                                    onClick = { selectedFilter = filter }
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("map_filter_${filter.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = filter.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = filter.label,
                                    style = LoopType.Metadata.copy(
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) InkWhite else InkCharcoal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nearby Points Count Badge
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperPureWhite.copy(alpha = 0.95f))
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "● ${filteredPoints.size} Community Points in 500m Walkable Zone",
                        style = LoopType.Metadata.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentForestGreen
                        )
                    )
                }
            }
        }

        // Floating Control Buttons: Recenter & Map Type Toggle
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 64.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recenter to Neighborhood Center
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(PaperPureWhite)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(initialCenter, initialZoom),
                                    600
                                )
                            }
                        }
                    )
                    .testTag("map_recenter_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter Map",
                    tint = InkCharcoal,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Map Layer Toggle (Normal vs Hybrid)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(if (isSatelliteView) InkCharcoal else PaperPureWhite)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { isSatelliteView = !isSatelliteView }
                    )
                    .testTag("map_type_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Toggle Satellite View",
                    tint = if (isSatelliteView) InkWhite else InkCharcoal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom Selected Activity Point Callout Card
        AnimatedVisibility(
            visible = selectedPin != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp)
        ) {
            selectedPin?.let { pin ->
                EventMapMarkerCallout(
                    pin = pin,
                    onRsvpClick = {
                        onRsvpClick(pin)
                    },
                    onDirectionsClick = {
                        onDirectionsClick(pin)
                    },
                    onDismiss = {
                        selectedPin = null
                    }
                )
            }
        }
    }
}

/**
 * Custom visual marker for Google Maps rendered via [MarkerComposable].
 */
@Composable
private fun CommunityActivityMapMarker(
    point: DiscoveryPinItem,
    isSelected: Boolean
) {
    val (bgColor, emoji) = when (point.type) {
        DiscoveryPinType.COMMUNITY_EVENT -> AccentWarmYellow to "📸"
        DiscoveryPinType.HELP_REQUEST -> Color(0xFFF0A288) to "🤝"
        DiscoveryPinType.STUDY_GROUP -> AccentMint to "📚"
        DiscoveryPinType.PICKUP_HUB -> Color(0xFFD6E3E9) to "🔒"
        DiscoveryPinType.USER_HOST -> PaperPureWhite to "🛠️"
        DiscoveryPinType.RENTAL_ITEM -> AccentMint to "📦"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("map_marker_${point.id}")
    ) {
        // Selected Marker Label Bubble
        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(InkCharcoal)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = point.title,
                    style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = InkWhite,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Marker Pin Pinhead
        Box(
            modifier = Modifier
                .size(if (isSelected) 44.dp else 36.dp)
                .shadow(elevation = if (isSelected) 8.dp else 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(bgColor)
                .border(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    color = if (isSelected) InkCharcoal else PaperBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = if (isSelected) 18.sp else 15.sp
            )
        }

        // Marker Pin Pointer triangle/dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(InkCharcoal)
        )
    }
}
