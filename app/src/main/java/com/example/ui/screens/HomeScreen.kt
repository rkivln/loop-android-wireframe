package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.ui.components.CategoryAvatarRow
import com.example.ui.components.CommunityPhotowalksSheet
import com.example.ui.components.PopularItemCard
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.HeartRed
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoopType
import com.example.ui.theme.ParchmentBg
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg

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
    var locationMenuExpanded by remember { mutableStateOf(false) }
    var activeCategory by remember { mutableStateOf<RentalCategory?>(RentalCategory.ELECTRONICS) }
    var showPhotowalksModal by remember { mutableStateOf(false) }

    val locations = listOf("White Town, Puducherry", "Heritage Town", "Auroville", "Lawspet", "Mission Street")
    val popularItems = remember(items) { items.filter { it.isPopular } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ParchmentBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar: LOOP Logo + Location (Left) | Notification + User Avatar (Right)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: LOOP Logo & Location Dropdown
                    Column {
                        Text(
                            text = "LOOP",
                            style = LoopType.H1,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Location selector: 📍 White Town, Puducherry ⌵
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { locationMenuExpanded = true }
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = InkBlack,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = selectedLocation,
                                    style = LoopType.BodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Location",
                                    tint = InkBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = locationMenuExpanded,
                                onDismissRequest = { locationMenuExpanded = false },
                                modifier = Modifier.background(ParchmentWhite)
                            ) {
                                locations.forEach { loc ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = loc,
                                                style = LoopType.BodyMedium,
                                                fontWeight = if (loc == selectedLocation) FontWeight.Bold else FontWeight.Normal,
                                                color = InkBlack
                                            )
                                        },
                                        onClick = {
                                            selectedLocation = loc
                                            locationMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Right Actions: Notification Bell (with red dot) + User Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Notification Bell with Red Badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = Color.LightGray),
                                    onClick = onNotificationsClick
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = InkBlack,
                                modifier = Modifier.size(24.dp)
                            )
                            // Red notification dot
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(HeartRed)
                            )
                        }

                        // Circular User Avatar (Arjun S.)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(InkBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AS",
                                style = LoopType.BodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Postcard Hero Banner ("Local Gear, Real People", Puducherry Promenade)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = { showPhotowalksModal = true })
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.whitetown_promenade_banner_1790872139983),
                        contentDescription = "White Town Promenade",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Atmospheric gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )

                    // Postcard Overlaid Text (Script & Sans)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Right Script Tagline & Meetup Chip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentYellow)
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "3 WALKS THIS WEEKEND ✦",
                                    style = LoopType.CaptionTechnical,
                                    color = InkBlack
                                )
                            }

                            Text(
                                text = "Local Gear,\nReal People",
                                style = LoopType.ScriptEditorial,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                        }

                        // Bottom Subtext
                        Column {
                            Text(
                                text = "Capture. Create. Meet.",
                                style = LoopType.H3,
                                color = Color.White
                            )
                            Text(
                                text = "Do more together in White Town. Tap to view Photowalks →",
                                style = LoopType.BodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Search Bar & Filter Sliders Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Pill Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ParchmentWhite)
                            .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
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
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(
                                    fontSize = 13.5.sp,
                                    color = InkBlack,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(InkBlack),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search cameras, laptops, vehicles...",
                                            style = LoopType.BodySmall,
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

                    Spacer(modifier = Modifier.width(10.dp))

                    // Filter Sliders Button (Squircle)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ParchmentWhite)
                            .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
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
                            tint = InkBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Categories Row (Cameras, Laptops, Vehicles, Furniture, Tools)
            item {
                CategoryAvatarRow(
                    selectedCategory = activeCategory,
                    onSelectCategory = { cat ->
                        activeCategory = cat
                        onSelectCategory(cat)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // "Popular Near You" Header & Item Cards Row
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popular Near You",
                            style = LoopType.H2
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(onClick = onSeeAllPopular)
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "See all",
                                style = LoopType.BodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = InkBlack
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
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

            // Community Creator Jam CTA Feature Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ParchmentWhite)
                        .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
                        .clickable { showPhotowalksModal = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = InkBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Promenade Street Photowalk",
                                    style = LoopType.H3,
                                    color = InkBlack
                                )
                                Text(
                                    text = "Sat 5:30 PM · 14 Creators Attending · Test Gear",
                                    style = LoopType.BodySmall,
                                    color = InkSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(InkBlack)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "RSVP",
                                style = LoopType.CaptionTechnical,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }

        // Community Photowalks Modal Sheet
        AnimatedVisibility(
            visible = showPhotowalksModal,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            CommunityPhotowalksSheet(
                onDismiss = { showPhotowalksModal = false }
            )
        }
    }
}
