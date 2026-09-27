package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.LoopCategory
import com.example.data.models.MapPinItem
import com.example.ui.components.AvatarPile
import com.example.ui.components.CategoryChipSelector
import com.example.ui.components.DarkVectorMapView
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.MapViewContainer
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceInteractive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.android.gms.maps.model.LatLng

@Composable
fun ExploreMapScreen(
    pins: List<MapPinItem>,
    selectedPin: MapPinItem?,
    onPinSelect: (MapPinItem) -> Unit,
    onNavigateEventDetails: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var activeCategory by remember { mutableStateOf<LoopCategory?>(null) }
    var isJoined by remember { mutableStateOf(false) }
    var useGoogleMaps by remember { mutableStateOf(true) }

    val filteredPins = remember(pins, activeCategory) {
        if (activeCategory == null || activeCategory == LoopCategory.ALL) pins
        else pins.filter { it.category == activeCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0E16))
    ) {
        if (useGoogleMaps) {
            MapViewContainer(
                pins = filteredPins,
                selectedPin = selectedPin,
                onPinSelected = onPinSelect,
                initialCenter = LatLng(11.9338, 79.8297),
                initialZoom = 14.5f,
                showZoomControls = true,
                showLocationControl = true,
                showActiveNodesBadge = true,
                modifier = Modifier.fillMaxSize()
            ) {
                MapOverlays(
                    activeCategory = activeCategory,
                    onSelectCategory = { cat ->
                        activeCategory = if (activeCategory == cat) null else cat
                    },
                    useGoogleMaps = useGoogleMaps,
                    onToggleMapMode = { useGoogleMaps = !useGoogleMaps },
                    selectedPin = selectedPin,
                    isJoined = isJoined,
                    onToggleJoin = { isJoined = !isJoined },
                    onNavigateEventDetails = onNavigateEventDetails,
                    onBack = onBack
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                DarkVectorMapView(
                    pins = filteredPins,
                    selectedPin = selectedPin,
                    onPinSelected = onPinSelect,
                    modifier = Modifier.fillMaxSize()
                )

                MapOverlays(
                    activeCategory = activeCategory,
                    onSelectCategory = { cat ->
                        activeCategory = if (activeCategory == cat) null else cat
                    },
                    useGoogleMaps = useGoogleMaps,
                    onToggleMapMode = { useGoogleMaps = !useGoogleMaps },
                    selectedPin = selectedPin,
                    isJoined = isJoined,
                    onToggleJoin = { isJoined = !isJoined },
                    onNavigateEventDetails = onNavigateEventDetails,
                    onBack = onBack
                )
            }
        }
    }
}

@Composable
private fun MapOverlays(
    activeCategory: LoopCategory?,
    onSelectCategory: (LoopCategory) -> Unit,
    useGoogleMaps: Boolean,
    onToggleMapMode: () -> Unit,
    selectedPin: MapPinItem?,
    isJoined: Boolean,
    onToggleJoin: () -> Unit,
    onNavigateEventDetails: (String) -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top Navigation & Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBack,
                    contentDescription = "Back",
                    testTag = "map_back_btn"
                )

                Text(
                    text = "Nearby",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassIconButton(
                        icon = if (useGoogleMaps) Icons.Default.Radar else Icons.Default.Map,
                        onClick = onToggleMapMode,
                        contentDescription = if (useGoogleMaps) "Switch to Vector" else "Switch to Google Map",
                        tint = BrandSecondary,
                        testTag = "map_mode_toggle_btn"
                    )

                    GlassIconButton(
                        icon = Icons.Default.FilterList,
                        onClick = { /* filter modal */ },
                        contentDescription = "Filter",
                        testTag = "map_filter_btn"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CategoryChipSelector(
                categories = listOf(
                    LoopCategory.PEOPLE,
                    LoopCategory.EVENTS,
                    LoopCategory.HELP,
                    LoopCategory.STUDY
                ),
                selectedCategory = activeCategory ?: LoopCategory.PEOPLE,
                onSelectCategory = onSelectCategory
            )
        }

        // Bottom Selected Pin Card
        AnimatedVisibility(
            visible = selectedPin != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 76.dp)
        ) {
            selectedPin?.let { pin ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = SurfaceDark,
                    borderColor = BorderDefault
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Header: Tag & Distance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = pin.category.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BrandSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${pin.distance} away",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = pin.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = pin.snippet,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Attendees row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AvatarPile(
                                initialsList = listOf("AK", "SR", "RM", "PS"),
                                avatarSize = 24.dp,
                                overflowCount = if (pin.memberCount > 4) pin.memberCount - 4 else 0
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "${pin.memberCount} members active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: View Details & Join
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceInteractive)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                    .clickable {
                                        onNavigateEventDetails(pin.id)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "View Details",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isJoined) StatusSuccess else BrandPrimary)
                                    .clickable(onClick = onToggleJoin),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isJoined) "Joined ✓" else "Join Group",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
