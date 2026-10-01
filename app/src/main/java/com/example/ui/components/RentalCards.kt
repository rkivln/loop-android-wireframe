package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CanvasDark
import com.example.ui.theme.CanvasSubtle
import com.example.ui.theme.CanvasWhite
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LineHairline
import com.example.ui.theme.RatingAmber
import com.example.ui.theme.StatusLive

@Composable
fun CategoryAvatarRow(
    selectedCategory: RentalCategory? = null,
    onSelectCategory: (RentalCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        RentalCategory.ELECTRONICS,
        RentalCategory.VEHICLES,
        RentalCategory.STUDY_OFFICE,
        RentalCategory.FURNITURE,
        RentalCategory.TOOLS,
        RentalCategory.EVENTS
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSelected = selectedCategory == cat
            val shape = RoundedCornerShape(6.dp)

            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(if (isSelected) CanvasDark else CanvasWhite, shape)
                    .border(1.dp, if (isSelected) CanvasDark else LineHairline, shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.LightGray),
                        onClick = { onSelectCategory(cat) }
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("category_chip_${cat.name}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cat.iconEmoji,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cat.title,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else InkPrimary
                    )
                }
            }
        }
    }
}

// Gear Listing Card: Editorial, high-clarity hardware frame
@Composable
fun PopularItemCard(
    item: RentalItem,
    onClick: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .width(172.dp)
            .clip(cardShape)
            .background(CanvasWhite, cardShape)
            .border(1.dp, LineHairline, cardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray),
                onClick = onClick
            )
            .testTag("popular_card_${item.id}")
    ) {
        Column {
            // Hardware Photo Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(134.dp)
                    .background(CanvasSubtle)
            ) {
                Image(
                    painter = painterResource(id = item.primaryImageRes),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Live Availability Tag Top-Left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CanvasDark.copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(StatusLive)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AVAILABLE",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                }

                // Tactile Favorite Toggle Top-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CanvasWhite.copy(alpha = 0.95f))
                        .border(0.5.dp, LineHairline, RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AlertCrimson.copy(alpha = 0.2f)),
                            onClick = { onToggleFavorite(item.id) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) AlertCrimson else InkPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Technical Gear Specifications & Pricing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                // Category Meta Tag
                Text(
                    text = item.category.title.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    color = InkMuted
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${item.pricePerDay}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = InkPrimary
                        )
                        Text(
                            text = "/d",
                            fontSize = 11.sp,
                            color = InkMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = RatingAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${item.rating}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = InkPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Location / Distance
                Text(
                    text = "White Town · ${item.location}",
                    fontSize = 11.sp,
                    color = InkSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

// Curated Collection Banner: High-contrast architectural editorial split
@Composable
fun CategoryBannerCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .height(132.dp)
            .clip(shape)
            .background(CanvasWhite, shape)
            .border(1.dp, LineHairline, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray),
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "COLLECTION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp,
                        color = InkMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = InkSecondary,
                        maxLines = 2
                    )
                }

                // Clean forward arrow indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "EXPLORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = InkPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = InkPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .weight(0.9f)
                    .size(86.dp)
            )
        }
    }
}
