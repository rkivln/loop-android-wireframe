package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.data.models.UserCommunityPost
import com.example.ui.components.CatalogSkeletonFeed
import com.example.ui.components.EditorialPhotoCard
import com.example.ui.components.ItemImagePreviewModal
import com.example.ui.components.PrimaryCTA
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onItemClick: (RentalItem) -> Unit = {},
    onContactOwner: (RentalOwner) -> Unit = {},
    onContactHostWithItem: (RentalOwner, RentalItem?) -> Unit = { owner, _ -> onContactOwner(owner) },
    onMessageOrganizer: (UserCommunityPost) -> Unit = {},
    onBackToCatalog: () -> Unit = {},
    onPublishClick: () -> Unit = {},
    initialShowMap: Boolean = false,
    onMapModeChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMapMode by remember(initialShowMap) { mutableStateOf(initialShowMap) }
    var previewItem by remember { mutableStateOf<RentalItem?>(null) }
    val searchQuery by RentalRepository.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by RentalRepository.selectedCategory.collectAsStateWithLifecycle()
    val allItems by RentalRepository.itemsFlow.collectAsStateWithLifecycle()
    val filteredItems by RentalRepository.filteredItems.collectAsStateWithLifecycle()
    val userProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val isCatalogLoading by RentalRepository.isCatalogLoadingFlow.collectAsStateWithLifecycle()
    val isRefreshing by RentalRepository.isRefreshingFlow.collectAsStateWithLifecycle()

    if (showMapMode) {
        BackHandler {
            showMapMode = false
            onMapModeChange(false)
        }
        Box(modifier = modifier.fillMaxSize()) {
            DiscoveryMapScreen(
                onBackToCatalog = {
                    showMapMode = false
                    onMapModeChange(false)
                    onBackToCatalog()
                },
                onItemClick = onItemClick,
                onContactOwner = onContactOwner,
                onContactHostWithItem = onContactHostWithItem,
                onMessageOrganizer = onMessageOrganizer,
                onPublishPiece = onPublishClick,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperIvory)
            .statusBarsPadding()
    ) {
        // Editorial Header: "The Catalog" + verified count + Map pill button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "The Catalog",
                    style = LoopType.HeroDisplayLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Normal),
                    color = InkCharcoal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${filteredItems.size} ${if (filteredItems.size == 1) "piece" else "pieces"} available",
                    style = LoopType.BodySmall,
                    color = InkSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = {
                            showMapMode = true
                            onMapModeChange(true)
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("explore_mode_map"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = "Open Map",
                        tint = InkCharcoal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Map",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp),
                        color = InkCharcoal
                    )
                }
            }
        }

        // Rounded Pill Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(PaperPureWhite)
                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(100.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = InkMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search cameras, tools, furniture, vehicles...",
                        style = LoopType.BodyMedium,
                        color = InkMuted,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { RentalRepository.setSearchQuery(it) },
                    singleLine = true,
                    textStyle = LoopType.BodyMedium.copy(color = InkCharcoal),
                    cursorBrush = SolidColor(InkCharcoal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_input")
                )
            }
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { RentalRepository.setSearchQuery("") },
                    modifier = Modifier.size(28.dp)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Filter Chips ("All Pieces", "Electronics", "Furniture", "Vehicles", ...)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isAll = selectedCategory == null
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isAll) InkCharcoal else PaperPureWhite)
                    .border(
                        1.dp,
                        if (isAll) InkCharcoal else PaperBorder,
                        RoundedCornerShape(100.dp)
                    )
                    .clickable { RentalRepository.setSelectedCategory(null) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("category_chip_all")
            ) {
                Text(
                    text = "All Pieces",
                    style = LoopType.Metadata.copy(
                        fontSize = 12.sp,
                        fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isAll) InkWhite else InkCharcoal
                )
            }

            RentalCategory.entries.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) InkCharcoal else PaperPureWhite)
                        .border(
                            1.dp,
                            if (isSelected) InkCharcoal else PaperBorder,
                            RoundedCornerShape(100.dp)
                        )
                        .clickable {
                            RentalRepository.setSelectedCategory(if (isSelected) null else category)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("category_chip_${category.name.lowercase()}")
                ) {
                    Text(
                        text = "${category.iconEmoji} ${category.title}",
                        style = LoopType.Metadata.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) InkWhite else InkCharcoal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isCatalogLoading && allItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 4.dp)
            ) {
                CatalogSkeletonFeed(count = 3)
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { RentalRepository.refreshSessionData(context) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                            .testTag(if (allItems.isEmpty()) "empty_catalog_state" else "empty_search_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "No results",
                                tint = InkMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            if (allItems.isEmpty()) {
                                Text(
                                    text = "No pieces listed yet",
                                    style = LoopType.HeadlineMedium,
                                    color = InkCharcoal,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Publish the first item with your own photos to start the community marketplace.",
                                    style = LoopType.BodySmall,
                                    color = InkSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                PrimaryCTA(
                                    text = "Publish Piece",
                                    onClick = onPublishClick,
                                    testTag = "empty_catalog_publish_button"
                                )
                            } else {
                                Text(
                                    text = "No objects match your filter",
                                    style = LoopType.HeadlineMedium,
                                    color = InkCharcoal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try clearing your search or choosing another department.",
                                    style = LoopType.BodySmall,
                                    color = InkSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                TextButton(onClick = {
                                    RentalRepository.setSearchQuery("")
                                    RentalRepository.setSelectedCategory(null)
                                }) {
                                    Text("Reset Catalog Filters", color = InkCharcoal)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            val isOwn = item.owner.id.isNotBlank() && item.owner.id == userProfile.uid
                            EditorialPhotoCard(
                                item = item,
                                onClick = { onItemClick(item) },
                                onToggleFavorite = { RentalRepository.toggleFavorite(item.id) },
                                isLarge = true,
                                isOwnedByCurrentUser = isOwn,
                                onPreviewImages = { previewItem = item }
                            )
                        }
                    }
                }
            }
        }
    }

    previewItem?.let { itemToPreview ->
        ItemImagePreviewModal(
            item = itemToPreview,
            onDismiss = { previewItem = null },
            onViewDetails = { onItemClick(itemToPreview) }
        )
    }
}
