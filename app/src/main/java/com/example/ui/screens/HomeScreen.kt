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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.ui.components.CategoryTile
import com.example.ui.components.EditorialPhotoCard
import com.example.ui.components.SectionLabel
import com.example.ui.theme.AccentBeigeOat
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

@Composable
fun HomeScreen(
    items: List<RentalItem>,
    onItemClick: (RentalItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSelectCategory: (RentalCategory) -> Unit,
    onSearchClick: () -> Unit,
    onOpenMapDiscovery: () -> Unit,
    onOpenCommunityFeed: () -> Unit = {},
    onNotificationsClick: () -> Unit,
    onSeeAllPopular: () -> Unit,
    onSeeAllCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeCategoryFilter by remember { mutableStateOf<RentalCategory?>(null) }

    val isFiltered = searchQuery.isNotBlank() || activeCategoryFilter != null

    val filteredItems = remember(items, searchQuery, activeCategoryFilter) {
        items.filter { item ->
            val matchesCategory = activeCategoryFilter == null || item.category == activeCategoryFilter
            val matchesKeyword = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.location.contains(searchQuery, ignoreCase = true) ||
                    item.owner.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesKeyword
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PaperWarm),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Editorial Masthead Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LOOP",
                            style = LoopType.HeroDisplayLarge.copy(
                                fontSize = 34.sp,
                                letterSpacing = (-1.0).sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = InkCharcoal
                        )
                        Text(
                            text = "THE LIVING CATALOG · ISSUE 08",
                            style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                            color = InkSecondary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Live Community Feed shortcut button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = onOpenCommunityFeed
                                )
                                .testTag("home_feed_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DynamicFeed,
                                contentDescription = "Community Feed",
                                tint = InkCharcoal,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Map shortcut button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = onOpenMapDiscovery
                                )
                                .testTag("home_map_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Map,
                                contentDescription = "Neighborhood Map",
                                tint = InkCharcoal,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Search shortcut button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = onSearchClick
                                )
                                .testTag("home_search_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = InkCharcoal,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar at Top of Main Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PaperPureWhite)
                        .border(1.5.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("home_search_bar")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon",
                            tint = InkCharcoal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search by item, camera, cycle, studio...",
                                    style = LoopType.BodyMedium,
                                    color = InkMuted
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    color = InkCharcoal,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(InkCharcoal),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("main_search_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("home_clear_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = InkMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Pills directly under Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAllSelected = activeCategoryFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isAllSelected) InkCharcoal else PaperPureWhite)
                            .border(1.dp, if (isAllSelected) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { activeCategoryFilter = null }
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("home_category_filter_all"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All Items",
                            style = LoopType.Metadata.copy(
                                fontSize = 11.5.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isAllSelected) InkWhite else InkCharcoal
                        )
                    }

                    RentalCategory.values().forEach { cat ->
                        val isSelected = activeCategoryFilter == cat
                        val (label, emoji) = when (cat) {
                            RentalCategory.ELECTRONICS -> "Electronics" to "📸"
                            RentalCategory.STUDY_OFFICE -> "Study & Office" to "📚"
                            RentalCategory.VEHICLES -> "Vehicles" to "🚲"
                            RentalCategory.FURNITURE -> "Furniture" to "🛋️"
                            RentalCategory.EVENTS -> "Events" to "✨"
                            else -> "Gear" to "📦"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) InkCharcoal else PaperPureWhite)
                                .border(1.dp, if (isSelected) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = {
                                        activeCategoryFilter = if (activeCategoryFilter == cat) null else cat
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("home_category_filter_${cat.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
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
        }

        if (isFiltered) {
            // Filter Results Header
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FILTERED RESULTS · ${filteredItems.size} ITEMS",
                        style = LoopType.EditorialTag.copy(fontSize = 11.sp),
                        color = InkSecondary
                    )

                    Text(
                        text = "Clear all",
                        style = LoopType.Metadata.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkCharcoal
                        ),
                        modifier = Modifier
                            .clickable {
                                searchQuery = ""
                                activeCategoryFilter = null
                            }
                            .testTag("home_clear_all_filters_btn")
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (filteredItems.isNotEmpty()) {
                items(filteredItems) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 8.dp)
                    ) {
                        EditorialPhotoCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            onToggleFavorite = { onToggleFavorite(item.id) },
                            isLarge = false
                        )
                    }
                }
            } else {
                // Empty search result card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 30.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                            .padding(28.dp)
                            .testTag("empty_search_results_card"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🔍", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No matching rental items",
                                style = LoopType.HeadlineMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                color = InkCharcoal
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching with a different keyword or resetting your category filter.",
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(InkCharcoal)
                                    .clickable {
                                        searchQuery = ""
                                        activeCategoryFilter = null
                                    }
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                                    .testTag("reset_search_filters_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Reset Search & Filters",
                                    style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                                    color = InkWhite
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Hero Editorial Story (The Analog Renaissance)
            item {
                val heroItem = items.firstOrNull()
            if (heroItem != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(26.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onItemClick(heroItem) }
                            )
                            .testTag("home_hero_story_card")
                    ) {
                        Image(
                            painter = painterResource(id = heroItem.primaryImageRes),
                            contentDescription = heroItem.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle dark gradient overlay at bottom for legibility
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                                    )
                                )
                        )

                        // Top curated label
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
                                style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                                color = InkCharcoal
                            )
                        }

                        // Bottom Hero Content
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Text(
                                text = heroItem.title,
                                style = LoopType.HeroDisplay.copy(fontSize = 24.sp, color = InkWhite),
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "By ${heroItem.owner.name} · Available today for ₹${heroItem.pricePerDay}/day",
                                style = LoopType.BodySmall.copy(color = InkWhite.copy(alpha = 0.85f))
                            )
                        }
                    }
                }
            }
        }

        // Tactile Category Flow (Section 20)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionLabel(
                title = "Curated Departments",
                actionText = "View All",
                onActionClick = onSeeAllCategories
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(RentalCategory.values()) { category ->
                    CategoryTile(
                        category = category,
                        isSelected = false,
                        onClick = { onSelectCategory(category) }
                    )
                }
            }
        }

        // Curated Pieces Section (Asymmetric photo cards)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionLabel(
                title = "Objects in Circulation",
                actionText = "Explore",
                onActionClick = onSeeAllPopular
            )
        }

        // Staggered Items Feed
        items(items.drop(1)) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 10.dp)
            ) {
                EditorialPhotoCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onToggleFavorite = { onToggleFavorite(item.id) },
                    isLarge = false
                )
            }
        }

        // Community Photowalk Dispatch Banner
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(PaperIvory)
                    .border(1.dp, PaperBorder, RoundedCornerShape(22.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onOpenCommunityFeed
                    )
                    .padding(20.dp)
                    .testTag("home_community_dispatch_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "COMMUNITY DISPATCH",
                            style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                            color = InkSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Promenade Sunset Walk",
                            style = LoopType.HeadlineMedium.copy(fontSize = 19.sp),
                            color = InkCharcoal
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "18 creators meeting today at 5:00 PM along Rock Beach Promenade. Try out 35mm glass.",
                            style = LoopType.BodySmall,
                            color = InkSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(InkCharcoal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = InkWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        }
    }
}
