package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Surfing
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.outlined.Map
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.ui.components.CategoryTile
import com.example.ui.components.CommunityPhotowalksSheet
import com.example.ui.components.EditorialPhotoCard
import com.example.ui.components.SectionLabel
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
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

@Composable
fun HomeScreen(
    onItemClick: (RentalItem) -> Unit = {},
    onExploreMapClick: () -> Unit = {},
    onNavigateToObjects: (RentalCategory?) -> Unit = {},
    onPromptLocation: () -> Unit = {},
    isLocationGranted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val allItems by RentalRepository.itemsFlow.collectAsStateWithLifecycle()
    val syncError by RentalRepository.syncErrorFlow.collectAsStateWithLifecycle()
    var showEventsSheet by remember { mutableStateOf(false) }

    val featuredItem = allItems.firstOrNull()
    val circulationItems = if (allItems.size > 1) allItems.drop(1) else allItems
    val homeCategories = remember {
        listOf(
            RentalCategory.ELECTRONICS,
            RentalCategory.FURNITURE,
            RentalCategory.VEHICLES,
            RentalCategory.TOOLS,
            RentalCategory.EVENTS,
            RentalCategory.STUDY_OFFICE,
            RentalCategory.HOME_LIVING
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperIvory)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("rental_items_list"),
            contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Masthead Header (LOOP · THE LIVING CATALOG · ISSUE 08 + Map & Search icons)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LOOP",
                            style = LoopType.HeroDisplayLarge.copy(
                                fontSize = 29.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = InkCharcoal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "THE LIVING CATALOG · ISSUE 08",
                            style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                            color = InkMuted
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = onExploreMapClick
                                )
                                .testTag("home_map_shortcut_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Map,
                                contentDescription = "Discovery Map",
                                tint = InkCharcoal,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { onNavigateToObjects(null) }
                                )
                                .testTag("home_search_shortcut_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Catalog",
                                tint = InkCharcoal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Optional Sync Error Banner
            if (syncError != null) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AccentPeach.copy(alpha = 0.5f))
                            .border(1.dp, AlertCrimson.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("home_sync_error_banner")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Sync warning",
                            tint = AlertCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = syncError ?: "",
                            style = LoopType.BodySmall,
                            color = InkCharcoal,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { RentalRepository.clearSyncError() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = InkCharcoal,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Featured Cover Story Hero Card
            if (featuredItem != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                            .height(276.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(24.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onItemClick(featuredItem) }
                            )
                            .testTag("home_cover_story_card")
                    ) {
                        Image(
                            painter = painterResource(id = featuredItem.primaryImageRes),
                            contentDescription = featuredItem.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.15f),
                                            Color.Black.copy(alpha = 0.72f)
                                        )
                                    )
                                )
                        )

                        // Top-left white pill tag
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(PaperPureWhite.copy(alpha = 0.94f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "COVER STORY — WHITE TOWN",
                                style = LoopType.EditorialTag.copy(fontSize = 9.5.sp),
                                color = InkCharcoal
                            )
                        }

                        // Bottom-left white editorial headline & subline
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Text(
                                text = featuredItem.title,
                                style = LoopType.HeroDisplay.copy(fontSize = 24.sp, lineHeight = 29.sp),
                                color = InkWhite,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "By ${featuredItem.owner.name} · Available today for ₹${featuredItem.pricePerDay}/day",
                                style = LoopType.BodySmall.copy(fontSize = 12.5.sp),
                                color = InkWhite.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Curated Departments
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(
                        title = "Curated Departments",
                        actionText = "View All",
                        onActionClick = { onNavigateToObjects(null) }
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(homeCategories, key = { it.name }) { category ->
                            CategoryTile(
                                category = category,
                                isSelected = false,
                                onClick = { onNavigateToObjects(category) }
                            )
                        }
                    }
                }
            }

            // Subtle Location Prompt Pill if not yet granted
            if (!isLocationGranted) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                            .clickable(onClick = onPromptLocation)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("home_location_permission_banner")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(AccentMint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = "Location",
                                tint = AccentForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Enable location to sort nearby gear",
                            style = LoopType.BodySmall,
                            color = InkSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Enable",
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                            color = AccentForestGreen
                        )
                    }
                }
            }

            // Community Photowalks & Neighborhood Events Link Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorder, RoundedCornerShape(20.dp))
                        .clickable { showEventsSheet = true }
                        .padding(16.dp)
                        .testTag("home_community_events_card"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AccentMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Neighborhood Events",
                            tint = AccentForestGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "COMMUNITY PHOTOWALKS & JAMS",
                            style = LoopType.EditorialTag.copy(fontSize = 9.5.sp),
                            color = AccentForestGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "White Town Sunset Walks & Skill Circles",
                            style = LoopType.TitleMedium.copy(fontSize = 14.5.sp),
                            color = InkCharcoal
                        )
                    }
                    Text(
                        text = "View →",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                        color = InkCharcoal
                    )
                }
            }

            // Objects in Circulation Section Header
            item {
                SectionLabel(
                    title = "Objects in Circulation",
                    actionText = "Explore",
                    onActionClick = { onNavigateToObjects(null) }
                )
            }

            // Editorial Photo Cards
            items(circulationItems, key = { it.id }) { item ->
                EditorialPhotoCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onToggleFavorite = { RentalRepository.toggleFavorite(item.id) },
                    modifier = Modifier.padding(horizontal = 22.dp)
                )
            }
        }

        if (showEventsSheet) {
            CommunityPhotowalksSheet(onDismiss = { showEventsSheet = false })
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "photography", "electronics" -> Icons.Default.CameraAlt
        "surfing" -> Icons.Default.Surfing
        "crafts", "events" -> Icons.Default.Palette
        "mobility", "vehicles" -> Icons.AutoMirrored.Filled.DirectionsBike
        "diving" -> Icons.Default.Waves
        "study & office", "tools" -> Icons.Default.Tv
        "furniture", "home & living" -> Icons.Default.Forest
        else -> Icons.Default.Star
    }
}
