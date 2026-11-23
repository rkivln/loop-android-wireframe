package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HouseRentalRepository
import com.example.data.models.FurnishingStatus
import com.example.data.models.HouseAmenity
import com.example.data.models.HouseFilterState
import com.example.data.models.HouseListing
import com.example.data.models.HousePropertyType
import com.example.data.models.HouseSortOption
import com.example.data.models.TenantPreference
import com.example.ui.components.CatalogSkeletonFeed
import com.example.ui.components.ListingPhoto
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousesScreen(
    onHouseClick: (HouseListing) -> Unit = {},
    onOpenMapWithHouses: () -> Unit = {},
    onPublishHouseClick: () -> Unit = {},
    bottomBarPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val houses by HouseRentalRepository.filteredHousesFlow.collectAsStateWithLifecycle()
    val allHouses by HouseRentalRepository.housesFlow.collectAsStateWithLifecycle()
    val searchQuery by HouseRentalRepository.houseSearchQuery.collectAsStateWithLifecycle()
    val filterState by HouseRentalRepository.houseFilterState.collectAsStateWithLifecycle()
    val isLoading by HouseRentalRepository.isHousesLoadingFlow.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperIvory)
            .testTag("houses_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 28.dp + bottomBarPadding
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Editorial Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Neighborhood Homes",
                            style = LoopType.HeroDisplayLarge.copy(fontSize = 27.sp),
                            color = InkCharcoal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Verified long-term monthly rentals · ${allHouses.count { it.isAvailableForRent }} open for visits",
                            style = LoopType.Metadata,
                            color = InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable(onClick = onOpenMapWithHouses)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("houses_map_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "View houses on map",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Map",
                                    style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                                    color = InkCharcoal
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (filterState.activeFilterCount > 0) AccentForestGreen else PaperPureWhite)
                                .border(
                                    1.dp,
                                    if (filterState.activeFilterCount > 0) AccentForestGreen else PaperBorderSubtle,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { showFilterSheet = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("houses_filter_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter houses",
                                    tint = if (filterState.activeFilterCount > 0) InkWhite else InkCharcoal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (filterState.activeFilterCount > 0) "Filters (${filterState.activeFilterCount})" else "Filter",
                                    style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                                    color = if (filterState.activeFilterCount > 0) InkWhite else InkCharcoal
                                )
                            }
                        }
                    }
                }
            }

            // Search Field
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search homes",
                        tint = InkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search neighborhood, 2 BHK, villa, White Town...",
                                style = LoopType.BodyMedium,
                                color = InkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { HouseRentalRepository.setSearchQuery(it) },
                            singleLine = true,
                            textStyle = LoopType.BodyMedium.copy(color = InkCharcoal),
                            cursorBrush = SolidColor(AccentForestGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("houses_search_input")
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = InkMuted,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { HouseRentalRepository.setSearchQuery("") }
                        )
                    }
                }
            }

            // Quick Residential Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HouseQuickChip(
                        label = "All Homes",
                        selected = filterState.minBhk == null && filterState.furnishing == null && !filterState.petFriendlyOnly && !filterState.savedOnly,
                        onClick = { HouseRentalRepository.resetFilters() }
                    )
                    HouseQuickChip(
                        label = "1 BHK",
                        selected = filterState.minBhk == 1,
                        onClick = {
                            val next = if (filterState.minBhk == 1) null else 1
                            HouseRentalRepository.setFilterState(filterState.copy(minBhk = next))
                        }
                    )
                    HouseQuickChip(
                        label = "2 BHK",
                        selected = filterState.minBhk == 2,
                        onClick = {
                            val next = if (filterState.minBhk == 2) null else 2
                            HouseRentalRepository.setFilterState(filterState.copy(minBhk = next))
                        }
                    )
                    HouseQuickChip(
                        label = "3+ BHK",
                        selected = filterState.minBhk == 3,
                        onClick = {
                            val next = if (filterState.minBhk == 3) null else 3
                            HouseRentalRepository.setFilterState(filterState.copy(minBhk = next))
                        }
                    )
                    HouseQuickChip(
                        label = "Furnished",
                        selected = filterState.furnishing == FurnishingStatus.FULLY_FURNISHED,
                        onClick = {
                            val next = if (filterState.furnishing == FurnishingStatus.FULLY_FURNISHED) null else FurnishingStatus.FULLY_FURNISHED
                            HouseRentalRepository.setFilterState(filterState.copy(furnishing = next))
                        }
                    )
                    HouseQuickChip(
                        label = "Pet-Friendly",
                        selected = filterState.petFriendlyOnly,
                        onClick = {
                            HouseRentalRepository.setFilterState(filterState.copy(petFriendlyOnly = !filterState.petFriendlyOnly))
                        }
                    )
                    HouseQuickChip(
                        label = "Saved",
                        selected = filterState.savedOnly,
                        onClick = {
                            HouseRentalRepository.setFilterState(filterState.copy(savedOnly = !filterState.savedOnly))
                        }
                    )
                }
            }

            if (isLoading && houses.isEmpty()) {
                item {
                    CatalogSkeletonFeed(count = 2)
                }
            } else if (houses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AccentMint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = "No homes listed",
                                tint = AccentForestGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (allHouses.isEmpty()) "Be the first to list a home" else "No matching homes found",
                            style = LoopType.HeadlineMedium.copy(fontSize = 19.sp),
                            color = InkCharcoal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (allHouses.isEmpty()) {
                                "Publish long-term monthly apartments, independent houses, or studios for trusted local visits."
                            } else {
                                "Try clearing your search or widening your monthly budget and bedroom filters."
                            },
                            style = LoopType.BodyMedium,
                            color = InkSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (filterState.activeFilterCount > 0 || searchQuery.isNotBlank()) {
                            TextButton(onClick = { HouseRentalRepository.resetFilters() }) {
                                Text("Reset Filters", color = AccentForestGreen, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        PrimaryCTA(
                            text = "List a House for Rent",
                            onClick = onPublishHouseClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("houses_empty_publish_btn")
                        )
                    }
                }
            } else {
                items(houses, key = { it.id }) { house ->
                    HouseListingCard(
                        house = house,
                        onClick = { onHouseClick(house) },
                        onToggleFavorite = { HouseRentalRepository.toggleFavoriteHouse(house.id) }
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = PaperPureWhite
        ) {
            HouseFilterSheetContent(
                initialState = filterState,
                onApply = { updated ->
                    HouseRentalRepository.setFilterState(updated)
                    showFilterSheet = false
                },
                onReset = {
                    HouseRentalRepository.resetFilters()
                    showFilterSheet = false
                }
            )
        }
    }
}

@Composable
private fun HouseQuickChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) AccentForestGreen else PaperPureWhite)
            .border(1.dp, if (selected) AccentForestGreen else PaperBorderSubtle, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
            color = if (selected) InkWhite else InkCharcoal
        )
    }
}

@Composable
private fun HouseListingCard(
    house: HouseListing,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val gallery = house.resolvedGalleryUrls
    val coverUrl = gallery.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("house_card_${house.id}")
    ) {
        // Photo Hero with room tag & availability badges
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .background(PaperWarm)
        ) {
            ListingPhoto(
                imageUrl = coverUrl,
                fallbackRes = 0,
                contentDescription = house.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top row: availability + favorite heart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (house.isAvailableForRent) AccentMint else PaperPureWhite.copy(alpha = 0.92f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (house.isAvailableForRent) "Open for Visits • ${house.availableFromLabel}" else "Currently Rented",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                        color = if (house.isAvailableForRent) AccentForestGreen else InkSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PaperPureWhite.copy(alpha = 0.92f))
                        .clickable(onClick = onToggleFavorite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (house.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (house.isFavorite) "Remove saved home" else "Save home",
                        tint = if (house.isFavorite) Color(0xFFE11D48) else InkCharcoal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom photo room label + count pill
            if (gallery.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.62f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${house.roomLabelAt(0)}${if (gallery.size > 1) " • ${gallery.size} photos" else ""}",
                            style = LoopType.Metadata,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = house.title,
                        style = LoopType.HeadlineMedium.copy(fontSize = 18.sp),
                        color = InkCharcoal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Neighborhood",
                            tint = AccentForestGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = house.location.ifBlank { "Neighborhood listed on visit" },
                            style = LoopType.Metadata,
                            color = InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${house.monthlyRent}",
                        style = LoopType.TitleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        color = AccentForestGreen
                    )
                    Text(
                        text = "per month",
                        style = LoopType.Metadata,
                        color = InkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Spec chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HouseMiniSpec(Icons.Default.Bed, house.configurationLabel)
                HouseMiniSpec(Icons.Default.Bathtub, "${house.bathrooms} Bath")
                HouseMiniSpec(Icons.Default.SquareFoot, "${house.areaSqFt} sq ft")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PaperWarm)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = house.furnishing.label,
                        style = LoopType.Metadata,
                        color = InkSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(
                        avatarUrl = house.owner.avatarUrl,
                        initials = house.owner.initials,
                        avatarPresetIndex = house.owner.avatarPresetIndex,
                        contentDescription = house.owner.name,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = house.owner.name,
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.Medium),
                        color = InkSecondary
                    )
                }
                Text(
                    text = if (house.securityDeposit > 0) "Deposit ₹${house.securityDeposit} • ${house.minLeaseMonths}m min" else "${house.minLeaseMonths}m min lease",
                    style = LoopType.Metadata,
                    color = InkMuted
                )
            }
        }
    }
}

@Composable
private fun HouseMiniSpec(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = InkSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = LoopType.Metadata,
            color = InkCharcoal
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HouseFilterSheetContent(
    initialState: HouseFilterState,
    onApply: (HouseFilterState) -> Unit,
    onReset: () -> Unit
) {
    var draft by remember(initialState) { mutableStateOf(initialState) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .heightIn(max = 620.dp)
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filter Houses", style = LoopType.HeroDisplayLarge.copy(fontSize = 22.sp), color = InkCharcoal)
            TextButton(onClick = onReset) {
                Text("Reset All", color = AccentForestGreen, fontWeight = FontWeight.SemiBold)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sort
            Text("SORT BY", style = LoopType.EditorialTag, color = InkMuted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HouseSortOption.entries.forEach { option ->
                    HouseQuickChip(
                        label = option.label,
                        selected = draft.sortOption == option,
                        onClick = { draft = draft.copy(sortOption = option) }
                    )
                }
            }

            // Property Type
            Text("PROPERTY TYPE", style = LoopType.EditorialTag, color = InkMuted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HousePropertyType.entries.forEach { type ->
                    HouseQuickChip(
                        label = type.label,
                        selected = draft.propertyType == type,
                        onClick = {
                            draft = draft.copy(propertyType = if (draft.propertyType == type) null else type)
                        }
                    )
                }
            }

            // Budget Cap
            Text("MAX MONTHLY RENT", style = LoopType.EditorialTag, color = InkMuted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(12000, 20000, 30000, 45000, 75000).forEach { cap ->
                    HouseQuickChip(
                        label = "Up to ₹${cap / 1000}k",
                        selected = draft.maxMonthlyRent == cap,
                        onClick = {
                            draft = draft.copy(maxMonthlyRent = if (draft.maxMonthlyRent == cap) null else cap)
                        }
                    )
                }
            }

            // Furnishing
            Text("FURNISHING", style = LoopType.EditorialTag, color = InkMuted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FurnishingStatus.entries.forEach { furn ->
                    HouseQuickChip(
                        label = furn.label,
                        selected = draft.furnishing == furn,
                        onClick = {
                            draft = draft.copy(furnishing = if (draft.furnishing == furn) null else furn)
                        }
                    )
                }
            }

            // Tenant Preference
            Text("TENANT PREFERENCE", style = LoopType.EditorialTag, color = InkMuted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TenantPreference.entries.forEach { pref ->
                    HouseQuickChip(
                        label = pref.label,
                        selected = draft.tenantPreference == pref,
                        onClick = {
                            draft = draft.copy(tenantPreference = if (draft.tenantPreference == pref) null else pref)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Available homes only", style = LoopType.BodyMedium, color = InkCharcoal)
                    Text("Hide homes already marked as rented", style = LoopType.Metadata, color = InkMuted)
                }
                Switch(
                    checked = draft.availableOnly,
                    onCheckedChange = { draft = draft.copy(availableOnly = it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = AccentForestGreen)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pet-friendly only", style = LoopType.BodyMedium, color = InkCharcoal)
                    Text("Show homes welcoming pets", style = LoopType.Metadata, color = InkMuted)
                }
                Switch(
                    checked = draft.petFriendlyOnly,
                    onCheckedChange = { draft = draft.copy(petFriendlyOnly = it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = AccentForestGreen)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryCTA(
            text = "Apply Filters",
            onClick = { onApply(draft) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
