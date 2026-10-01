package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
import com.example.R
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.ui.components.CategoryAvatarRow
import com.example.ui.components.CategoryBannerCard
import com.example.ui.components.LocationDropdownSelector
import com.example.ui.components.LoopLogoText
import com.example.ui.components.NotificationBellButton
import com.example.ui.components.PopularItemCard
import com.example.ui.theme.AccentCobalt
import com.example.ui.theme.CanvasDark
import com.example.ui.theme.CanvasGround
import com.example.ui.theme.CanvasSubtle
import com.example.ui.theme.CanvasWhite
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LineHairline
import com.example.ui.theme.StatusLive

@Composable
fun HomeScreen(
    items: List<RentalItem>,
    onItemClick: (RentalItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSelectCategory: (RentalCategory) -> Unit,
    onSearchClick: () -> Unit,
    onOpenMapDiscovery: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSeeAllPopular: () -> Unit,
    onSeeAllCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("White Town, Puducherry") }

    val popularItems = remember(items) { items.filter { it.isPopular } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasGround)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Masthead: Brand Mark, Location Selector, Alert Bell
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoopLogoText(fontSize = 20, showSubtext = false)

                    LocationDropdownSelector(
                        selectedLocation = selectedLocation,
                        onLocationSelected = { selectedLocation = it }
                    )

                    NotificationBellButton(
                        onClick = onNotificationsClick,
                        hasUnread = true
                    )
                }
            }

            // Editorial Headline & Descriptor
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text(
                        text = "Local Equipment Exchange",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                        color = InkPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rent camera systems, laptops, and mobility directly from verified creators in White Town.",
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = InkSecondary
                    )
                }
            }

            // Technical Search & Filter Input Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
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
                                            text = "Search gear by model, brand, or location...",
                                            fontSize = 13.sp,
                                            color = InkMuted
                                        )
                                    }
                                    inner()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_search_input")
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

                    Spacer(modifier = Modifier.width(8.dp))

                    // Filter Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CanvasWhite)
                            .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.LightGray),
                                onClick = onSearchClick
                            )
                            .testTag("home_filter_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = InkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Live Neighborhood Radar Strip (No flashy AI gradient, crisp architectural technical panel)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CanvasDark)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White),
                            onClick = onOpenMapDiscovery
                        )
                        .padding(14.dp)
                        .testTag("home_map_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(StatusLive)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RADAR DISCOVERY · 3.0 KM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.8.sp,
                                    color = Color(0xFF4ADE80)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "9 Hosts & 4 Creator Meetups Active",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Promenade Beach Photowalk · Café des Arts Jam",
                                fontSize = 11.5.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Technical arrow button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MAP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Hardware Categories Filter Strip
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "// GEAR CLASSIFICATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                            color = InkMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    CategoryAvatarRow(
                        onSelectCategory = onSelectCategory,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Inventory Listing Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Available Hardware",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            Text(
                                text = "Inspected and ready for instant local pickup",
                                fontSize = 12.sp,
                                color = InkSecondary
                            )
                        }

                        Text(
                            text = "VIEW ALL →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp,
                            color = AccentCobalt,
                            modifier = Modifier
                                .clickable(onClick = onSeeAllPopular)
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(popularItems, key = { it.id }) { item ->
                            PopularItemCard(
                                item = item,
                                onClick = { onItemClick(item) },
                                onToggleFavorite = onToggleFavorite
                            )
                        }
                    }
                }
            }

            // Technical Equipment Trust Protocol
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CanvasWhite)
                        .border(1.dp, LineHairline, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "// LOOP VERIFICATION PROTOCOL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                            color = InkMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProtocolItem(
                                code = "01",
                                title = "ID Verified",
                                desc = "Govt KYC verified hosts"
                            )
                            ProtocolItem(
                                code = "02",
                                title = "Zero Deposit",
                                desc = "Community trust scoring"
                            )
                            ProtocolItem(
                                code = "03",
                                title = "Handover",
                                desc = "Tested at pickup"
                            )
                        }
                    }
                }
            }

            // Curated Collections Grid
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Curated Kits",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            Text(
                                text = "Tested kits for projects, shoots, and remote workflows",
                                fontSize = 12.sp,
                                color = InkSecondary
                            )
                        }

                        Text(
                            text = "ALL →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp,
                            color = AccentCobalt,
                            modifier = Modifier
                                .clickable(onClick = onSeeAllCategories)
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryBannerCard(
                            title = "Study & Workstations",
                            subtitle = "M2 Laptops, keyboards, and 1080p projectors",
                            imageRes = R.drawable.modern_laptop_1790477981432,
                            onClick = { onSelectCategory(RentalCategory.STUDY_OFFICE) },
                            modifier = Modifier.weight(1f)
                        )

                        CategoryBannerCard(
                            title = "Studio & Living",
                            subtitle = "Scandinavian armchairs & staging kits",
                            imageRes = R.drawable.modern_armchair_1790478015816,
                            onClick = { onSelectCategory(RentalCategory.HOME_LIVING) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun ProtocolItem(
    code: String,
    title: String,
    desc: String
) {
    Column {
        Text(
            text = code,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = AccentCobalt
        )
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )
        Text(
            text = desc,
            fontSize = 10.5.sp,
            color = InkSecondary,
            lineHeight = 13.sp
        )
    }
}
