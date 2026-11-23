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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CatalogFilterState
import com.example.data.CatalogSortOption
import com.example.data.RentalRepository
import com.example.data.models.HouseListing
import com.example.data.models.MapCategoryFilter
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.data.models.UserCommunityPost
import com.example.ui.components.CatalogSkeletonFeed
import com.example.ui.components.EditorialPhotoCard
import com.example.ui.components.ItemImagePreviewModal
import com.example.ui.components.PrimaryCTA
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.HeartRed
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite
import com.example.ui.theme.getIconVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onItemClick: (RentalItem) -> Unit = {},
    onContactOwner: (RentalOwner) -> Unit = {},
    onContactHostWithItem: (RentalOwner, RentalItem?) -> Unit = { owner, _ -> onContactOwner(owner) },
    onHouseClick: (HouseListing) -> Unit = {},
    onMessageLandlord: (HouseListing) -> Unit = {},
    onMessageOrganizer: (UserCommunityPost) -> Unit = {},
    onBackToCatalog: () -> Unit = {},
    onPublishClick: () -> Unit = {},
    initialShowMap: Boolean = false,
    initialMapFilter: MapCategoryFilter = MapCategoryFilter.ALL,
    onMapModeChange: (Boolean) -> Unit = {},
    bottomBarPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMapMode by remember(initialShowMap) { mutableStateOf(initialShowMap) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var previewItem by remember { mutableStateOf<RentalItem?>(null) }
    val searchQuery by RentalRepository.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by RentalRepository.selectedCategory.collectAsStateWithLifecycle()
    val catalogFilterState by RentalRepository.catalogFilterState.collectAsStateWithLifecycle()
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
                onHouseClick = onHouseClick,
                onMessageLandlord = onMessageLandlord,
                onMessageOrganizer = onMessageOrganizer,
                onPublishPiece = onPublishClick,
                initialFilter = initialMapFilter,
                bottomOverlayPadding = bottomBarPadding,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PaperIvory)
                .statusBarsPadding()
        ) {
            // Editorial Header: "The Catalog" + Filter/Sort & Map pill buttons
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
                    val activeCount = catalogFilterState.activeFilterCount
                    Text(
                        text = if (activeCount > 0) {
                            "${filteredItems.size} ${if (filteredItems.size == 1) "piece" else "pieces"} · ${catalogFilterState.sortOption.label}"
                        } else {
                            "${filteredItems.size} ${if (filteredItems.size == 1) "piece" else "pieces"} available"
                        },
                        style = LoopType.BodySmall,
                        color = InkSecondary,
                        maxLines = 1
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hasActiveFilters = catalogFilterState.activeFilterCount > 0
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (hasActiveFilters) InkCharcoal else PaperPureWhite)
                            .border(1.dp, if (hasActiveFilters) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { showFilterSheet = true }
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("explore_filter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter and sort catalog",
                                tint = if (hasActiveFilters) InkWhite else InkCharcoal,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Filter",
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp),
                                color = if (hasActiveFilters) InkWhite else InkCharcoal
                            )
                            if (hasActiveFilters) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(AccentMint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${catalogFilterState.activeFilterCount}",
                                        style = LoopType.Metadata.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = AccentForestGreen
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
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
                            .padding(horizontal = 14.dp, vertical = 10.dp)
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
            val isAll = selectedCategory == null && !catalogFilterState.savedOnly
            Box(
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isAll) InkCharcoal else PaperPureWhite)
                    .border(
                        1.dp,
                        if (isAll) InkCharcoal else PaperBorder,
                        RoundedCornerShape(100.dp)
                    )
                    .clickable {
                        RentalRepository.setSelectedCategory(null)
                        RentalRepository.setCatalogSavedOnly(false)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("category_chip_all"),
                contentAlignment = Alignment.Center
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

            val isSavedOnly = catalogFilterState.savedOnly
            Box(
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isSavedOnly) InkCharcoal else PaperPureWhite)
                    .border(
                        1.dp,
                        if (isSavedOnly) InkCharcoal else PaperBorder,
                        RoundedCornerShape(100.dp)
                    )
                    .clickable {
                        RentalRepository.setCatalogSavedOnly(!isSavedOnly)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("category_chip_saved"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isSavedOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Saved wishlist filter",
                        tint = if (isSavedOnly) HeartRed else InkCharcoal,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Saved",
                        style = LoopType.Metadata.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSavedOnly) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSavedOnly) InkWhite else InkCharcoal
                    )
                }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = category.getIconVector(),
                            contentDescription = null,
                            tint = if (isSelected) InkWhite else InkCharcoal,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = category.title,
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
                                TextButton(
                                    onClick = { RentalRepository.resetCatalogFilters() },
                                    modifier = Modifier.testTag("reset_catalog_filters_button")
                                ) {
                                    Text("Reset Catalog Filters", color = InkCharcoal)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 22.dp,
                            end = 22.dp,
                            top = 4.dp,
                            bottom = 28.dp + bottomBarPadding
                        ),
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

        if (showFilterSheet) {
            ExploreFilterModalSheet(
                filterState = catalogFilterState,
                filteredCount = filteredItems.size,
                onSortOptionSelected = { RentalRepository.setCatalogSortOption(it) },
                onAvailableOnlyChanged = { RentalRepository.setCatalogAvailableOnly(it) },
                onSavedOnlyChanged = { RentalRepository.setCatalogSavedOnly(it) },
                onMaxPriceSelected = { RentalRepository.setCatalogMaxDailyPrice(it) },
                onResetAll = { RentalRepository.resetCatalogFilters() },
                bottomBarPadding = bottomBarPadding,
                onDismiss = { showFilterSheet = false }
            )
        }

        previewItem?.let { itemToPreview ->
            ItemImagePreviewModal(
                item = itemToPreview,
                onDismiss = { previewItem = null },
                onViewDetails = { onItemClick(itemToPreview) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExploreFilterModalSheet(
    filterState: CatalogFilterState,
    filteredCount: Int,
    onSortOptionSelected: (CatalogSortOption) -> Unit,
    onAvailableOnlyChanged: (Boolean) -> Unit,
    onSavedOnlyChanged: (Boolean) -> Unit,
    onMaxPriceSelected: (Int?) -> Unit,
    onResetAll: () -> Unit,
    bottomBarPadding: Dp = 0.dp,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val pricePresets: List<Pair<String, Int?>> = remember {
        listOf(
            "Any Budget" to null,
            "Up to ₹500" to 500,
            "Up to ₹1,500" to 1500,
            "Up to ₹3,000" to 3000
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(PaperPureWhite)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .then(
                    if (bottomBarPadding > 0.dp) {
                        Modifier.padding(bottom = bottomBarPadding)
                    } else {
                        Modifier.navigationBarsPadding()
                    }
                )
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CURATE & SORT",
                        style = LoopType.EditorialTag,
                        color = AccentForestGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Refine Catalog",
                        style = LoopType.HeroDisplay.copy(fontSize = 22.sp),
                        color = InkCharcoal
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("filter_sheet_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close filter sheet",
                        tint = InkCharcoal
                    )
                }
            }

            // Sort Order
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SORT ORDER",
                    style = LoopType.EditorialTag,
                    color = InkSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CatalogSortOption.entries.forEach { option ->
                        val isSelected = filterState.sortOption == option
                        Box(
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) InkCharcoal else PaperIvory)
                                .border(
                                    1.dp,
                                    if (isSelected) InkCharcoal else PaperBorder,
                                    RoundedCornerShape(100.dp)
                                )
                                .clickable { onSortOptionSelected(option) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("sort_option_${option.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = InkWhite,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = option.label,
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

            HorizontalDivider(color = PaperBorderSubtle)

            // Availability & Wishlist Toggles
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "AVAILABILITY & WISHLIST",
                    style = LoopType.EditorialTag,
                    color = InkSecondary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                        .clickable { onAvailableOnlyChanged(!filterState.availableOnly) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Available Today Only",
                            style = LoopType.TitleMedium.copy(fontSize = 14.sp),
                            color = InkCharcoal
                        )
                        Text(
                            text = "Hide currently booked or paused pieces",
                            style = LoopType.Metadata,
                            color = InkSecondary
                        )
                    }
                    Switch(
                        checked = filterState.availableOnly,
                        onCheckedChange = onAvailableOnlyChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = InkWhite,
                            checkedTrackColor = AccentForestGreen,
                            uncheckedThumbColor = InkSecondary,
                            uncheckedTrackColor = PaperPureWhite
                        ),
                        modifier = Modifier.testTag("filter_available_only_switch")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                        .clickable { onSavedOnlyChanged(!filterState.savedOnly) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Saved Wishlist Only",
                            style = LoopType.TitleMedium.copy(fontSize = 14.sp),
                            color = InkCharcoal
                        )
                        Text(
                            text = "Show pieces you bookmarked",
                            style = LoopType.Metadata,
                            color = InkSecondary
                        )
                    }
                    Switch(
                        checked = filterState.savedOnly,
                        onCheckedChange = onSavedOnlyChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = InkWhite,
                            checkedTrackColor = AccentForestGreen,
                            uncheckedThumbColor = InkSecondary,
                            uncheckedTrackColor = PaperPureWhite
                        ),
                        modifier = Modifier.testTag("filter_saved_only_switch")
                    )
                }
            }

            HorizontalDivider(color = PaperBorderSubtle)

            // Max Daily Rate Presets
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "MAX DAILY RATE",
                    style = LoopType.EditorialTag,
                    color = InkSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pricePresets.forEach { (label, priceCap) ->
                        val isSelected = filterState.maxDailyPrice == priceCap
                        val priceTag = priceCap?.toString() ?: "any"
                        Box(
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) InkCharcoal else PaperIvory)
                                .border(
                                    1.dp,
                                    if (isSelected) InkCharcoal else PaperBorder,
                                    RoundedCornerShape(100.dp)
                                )
                                .clickable { onMaxPriceSelected(priceCap) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("filter_price_$priceTag"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
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

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorder, RoundedCornerShape(26.dp))
                        .clickable { onResetAll() }
                        .testTag("filter_reset_all_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reset All",
                        style = LoopType.ButtonSecondaryLabel,
                        color = InkCharcoal
                    )
                }

                Box(modifier = Modifier.weight(1.4f)) {
                    PrimaryCTA(
                        text = "Show $filteredCount ${if (filteredCount == 1) "Piece" else "Pieces"}",
                        onClick = onDismiss,
                        testTag = "filter_apply_button"
                    )
                }
            }
        }
    }
}
