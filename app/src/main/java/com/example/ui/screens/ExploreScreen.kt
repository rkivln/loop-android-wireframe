package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.ui.components.EditorialPhotoCard
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
fun ExploreScreen(
    items: List<RentalItem>,
    selectedCategory: RentalCategory?,
    onSelectCategory: (RentalCategory?) -> Unit,
    onItemClick: (RentalItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onContactOwnerFromMap: (RentalOwner) -> Unit = {},
    initialMapView: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isMapView by remember { mutableStateOf(initialMapView) }

    val filteredItems = items.filter { item ->
        val matchesCategory = selectedCategory == null || item.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                item.location.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    if (isMapView) {
        DiscoveryMapScreen(
            onBackToCatalog = { isMapView = false },
            onItemClick = onItemClick,
            onContactOwner = onContactOwnerFromMap
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(PaperWarm)
                .statusBarsPadding()
        ) {
            // Header with Title and Mode Switcher (Catalog / Map)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "The Catalog",
                        style = LoopType.HeroDisplayLarge.copy(fontSize = 28.sp),
                        color = InkCharcoal
                    )
                    Text(
                        text = "${filteredItems.size} verified pieces available",
                        style = LoopType.Metadata,
                        color = InkSecondary
                    )
                }

                // Map View Toggle Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorder, RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = { isMapView = true }
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("explore_toggle_map_view"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Map,
                            contentDescription = null,
                            tint = InkCharcoal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Map",
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                            color = InkCharcoal
                        )
                    }
                }
            }

            // Minimalist Search Input
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
                            text = "Search 35mm cameras, e-bikes, synthesizers...",
                            style = LoopType.BodyEditorial.copy(fontSize = 13.5.sp),
                            color = InkMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = InkMuted,
                            modifier = Modifier.size(19.dp)
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
                        unfocusedTextColor = InkCharcoal,
                        cursorColor = InkCharcoal
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("explore_search_input")
                )
            }

            // Horizontal Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChipPill(
                        label = "All Pieces",
                        isSelected = selectedCategory == null,
                        onClick = { onSelectCategory(null) }
                    )
                }

                items(RentalCategory.values()) { category ->
                    FilterChipPill(
                        label = "${category.iconEmoji} ${category.title}",
                        isSelected = selectedCategory == category,
                        onClick = { onSelectCategory(if (selectedCategory == category) null else category) }
                    )
                }
            }

            // Catalog Photo Feed
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 90.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredItems.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No pieces found",
                                style = LoopType.HeadlineMedium,
                                color = InkCharcoal
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching for another object or clearing your filter.",
                                style = LoopType.BodyEditorial,
                                color = InkSecondary
                            )
                        }
                    }
                } else {
                    items(filteredItems) { item ->
                        EditorialPhotoCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            onToggleFavorite = { onToggleFavorite(item.id) },
                            isLarge = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
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
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = LoopType.Metadata.copy(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (isSelected) InkWhite else InkCharcoal
        )
    }
}
