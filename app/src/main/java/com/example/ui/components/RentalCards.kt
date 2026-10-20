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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.AccentYellowDark
import com.example.ui.theme.HeartRed
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentSurface
import com.example.ui.theme.ParchmentWhite

data class CategoryUiItem(
    val category: RentalCategory,
    val title: String,
    val icon: ImageVector
)

@Composable
fun CategoryAvatarRow(
    selectedCategory: RentalCategory? = null,
    onSelectCategory: (RentalCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryList = listOf(
        CategoryUiItem(RentalCategory.ELECTRONICS, "Cameras", Icons.Default.PhotoCamera),
        CategoryUiItem(RentalCategory.STUDY_OFFICE, "Laptops", Icons.Default.Laptop),
        CategoryUiItem(RentalCategory.VEHICLES, "Vehicles", Icons.Default.TwoWheeler),
        CategoryUiItem(RentalCategory.FURNITURE, "Furniture", Icons.Default.Chair),
        CategoryUiItem(RentalCategory.TOOLS, "Tools", Icons.Default.Build)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categoryList.forEach { item ->
            val isSelected = selectedCategory == item.category || (selectedCategory == null && item.category == RentalCategory.ELECTRONICS)
            val shape = RoundedCornerShape(14.dp)

            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 72.dp)
                    .clip(shape)
                    .background(if (isSelected) AccentYellow else ParchmentWhite, shape)
                    .border(
                        1.dp,
                        if (isSelected) AccentYellow else ParchmentBorder,
                        shape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.Black.copy(alpha = 0.1f)),
                        onClick = { onSelectCategory(item.category) }
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = InkBlack,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.title,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = InkBlack
                    )
                }
            }
        }
    }
}

// Popular Gear Item Card (Matches Image 2)
@Composable
fun PopularItemCard(
    item: RentalItem,
    onClick: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .width(168.dp)
            .clip(cardShape)
            .background(ParchmentWhite, cardShape)
            .border(1.dp, ParchmentBorder, cardShape)
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
                    .height(130.dp)
                    .background(ParchmentSurface)
            ) {
                Image(
                    painter = painterResource(id = item.primaryImageRes),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // White Squircle Favorite Heart Button (Top-Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, ParchmentBorder.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = HeartRed.copy(alpha = 0.2f)),
                            onClick = { onToggleFavorite(item.id) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) HeartRed else InkBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Card Text Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹${item.pricePerDay}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
                    Text(
                        text = " / day",
                        fontSize = 11.5.sp,
                        color = InkMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Rating Line with Orange Star: ★ 4.9 (28)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AccentYellowDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${item.rating} (${item.reviewCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkBlack
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBannerCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .height(126.dp)
            .clip(shape)
            .background(ParchmentWhite, shape)
            .border(1.dp, ParchmentBorder, shape)
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
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = InkSecondary,
                        maxLines = 2
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "EXPLORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = InkBlack,
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
                    .size(80.dp)
            )
        }
    }
}
