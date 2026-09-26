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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.ButtonCtaGradient
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceGlassHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple

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

    val filteredPins = remember(pins, activeCategory) {
        if (activeCategory == null || activeCategory == LoopCategory.ALL) pins
        else pins.filter { it.category == activeCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0E16))
    ) {
        // Dark Map Canvas
        DarkVectorMapView(
            pins = filteredPins,
            selectedPin = selectedPin,
            onPinSelected = onPinSelect,
            modifier = Modifier.fillMaxSize()
        )

        // Top Navigation Bar and Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                GlassIconButton(
                    icon = Icons.Default.FilterList,
                    onClick = { /* filter modal or toggle */ },
                    contentDescription = "Filter",
                    testTag = "map_filter_btn"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Map Filter Chips
            CategoryChipSelector(
                categories = listOf(
                    LoopCategory.PEOPLE,
                    LoopCategory.EVENTS,
                    LoopCategory.HELP,
                    LoopCategory.STUDY
                ),
                selectedCategory = activeCategory ?: LoopCategory.PEOPLE,
                onSelectCategory = { cat ->
                    activeCategory = if (activeCategory == cat) null else cat
                }
            )
        }

        // Floating Target / My Location Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp, bottom = 120.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xE61B1E2D), CircleShape)
                .border(1.dp, GlassBorder, CircleShape)
                .clickable { /* center on user */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My Location",
                tint = NeonBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        // Draggable / Interactive Glass Bottom Sheet Card for Selected Pin
        AnimatedVisibility(
            visible = selectedPin != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 84.dp)
        ) {
            selectedPin?.let { pin ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0xF0151825),
                    borderColor = GlassBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Image / Thumbnail visual and metadata header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Badge & Distance
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2E1F4D))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = pin.category.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFC4B5FD)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = pin.distance,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Title
                        Text(
                            text = pin.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Snippet
                        Text(
                            text = pin.snippet,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Attendees Avatar Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AvatarPile(
                                initialsList = listOf("AK", "SR", "RM", "PS"),
                                avatarSize = 26.dp,
                                overflowCount = if (pin.memberCount > 4) pin.memberCount - 4 else 0
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${pin.memberCount} members",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: View Details & Join
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // View Details
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(23.dp))
                                    .background(Color(0x33282C40))
                                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(23.dp))
                                    .clickable {
                                        onNavigateEventDetails(pin.id)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "View Details",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }

                            // Join
                            val joinShape = RoundedCornerShape(23.dp)
                            val joinBgModifier = if (isJoined) {
                                Modifier.background(Color(0xFF10B981), joinShape)
                            } else {
                                Modifier.background(ButtonCtaGradient, joinShape)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(joinShape)
                                    .then(joinBgModifier)
                                    .border(1.dp, Color(0x33FFFFFF), joinShape)
                                    .clickable {
                                        isJoined = !isJoined
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isJoined) "Joined ✓" else "Join",
                                    fontSize = 14.sp,
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
