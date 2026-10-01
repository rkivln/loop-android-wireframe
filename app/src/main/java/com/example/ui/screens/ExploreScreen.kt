package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RentalRepository
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.ui.components.CategoryAvatarRow
import com.example.ui.components.PopularItemCard
import com.example.ui.theme.CanvasDark
import com.example.ui.theme.CanvasGround
import com.example.ui.theme.CanvasSubtle
import com.example.ui.theme.CanvasWhite
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LineHairline

@Composable
fun ExploreScreen(
    items: List<RentalItem>,
    selectedCategory: RentalCategory?,
    onSelectCategory: (RentalCategory) -> Unit,
    onItemClick: (RentalItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onContactOwnerFromMap: (RentalOwner) -> Unit,
    initialMapView: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isMapView by remember { mutableStateOf(initialMapView) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, selectedCategory, searchQuery) {
        items.filter { item ->
            val matchesCategory = selectedCategory == null || item.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.location.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    if (isMapView) {
        DiscoveryMapScreen(
            pins = RentalRepository.discoveryPins,
            onPinChatClick = onContactOwnerFromMap,
            onPinItemClick = onItemClick,
            onSwitchToList = { isMapView = false },
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CanvasGround)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp)
            ) {
                // Header Bar: Catalogue Title & Map Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Equipment Catalogue",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = "${filteredItems.size} verified units listed in White Town",
                            fontSize = 12.sp,
                            color = InkSecondary
                        )
                    }

                    // Map Discovery View Toggle Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CanvasDark)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White),
                                onClick = { isMapView = true }
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("explore_map_toggle_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map View",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RADAR MAP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CanvasWhite)
                        .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = InkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                fontSize = 13.5.sp,
                                color = InkPrimary,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(CanvasDark),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Filter catalogue by gear, host, or specs...",
                                        fontSize = 13.sp,
                                        color = InkMuted
                                    )
                                }
                                inner()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("explore_search_field")
                        )
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = InkMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips Strip
                CategoryAvatarRow(
                    selectedCategory = selectedCategory,
                    onSelectCategory = onSelectCategory,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Inventory Grid or Empty State
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "NO HARDWARE MATCHES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                color = InkMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No listed gear in White Town matches '$searchQuery'",
                                fontSize = 13.sp,
                                color = InkSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CanvasDark)
                                    .clickable { searchQuery = "" }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "RESET SEARCH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 76.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            PopularItemCard(
                                item = item,
                                onClick = { onItemClick(item) },
                                onToggleFavorite = onToggleFavorite,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
