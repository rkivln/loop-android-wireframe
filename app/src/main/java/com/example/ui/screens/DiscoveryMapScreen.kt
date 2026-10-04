package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.RentalRepository
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.ProfileMarker
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.DarkCanvasNearBlack
import com.example.ui.theme.DarkMapRoad
import com.example.ui.theme.DarkMapWater
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperPureWhite

@Composable
fun DiscoveryMapScreen(
    onBackToCatalog: () -> Unit,
    onItemClick: (RentalItem) -> Unit,
    onContactOwner: (RentalOwner) -> Unit,
    modifier: Modifier = Modifier
) {
    val pins = RentalRepository.discoveryPins
    var selectedPin by remember { mutableStateOf<DiscoveryPinItem?>(pins.firstOrNull()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvasNearBlack)
    ) {
        // Visually Quiet Minimal Dark Charcoal Map Graphic (Section 18)
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // Coastline water mass (East)
            drawRect(
                color = DarkMapWater,
                topLeft = Offset(width * 0.78f, 0f),
                size = androidx.compose.ui.geometry.Size(width * 0.22f, height)
            )

            // Coastline separation line
            drawLine(
                color = Color(0xFF2A2D30),
                start = Offset(width * 0.78f, 0f),
                end = Offset(width * 0.78f, height),
                strokeWidth = 2.dp.toPx()
            )

            // Minimalist grid roads (White Town Heritage Grid)
            val roadColor = DarkMapRoad
            val roadWidth = 2.5.dp.toPx()

            // Vertical Avenues
            drawLine(roadColor, Offset(width * 0.20f, 0f), Offset(width * 0.20f, height), roadWidth)
            drawLine(roadColor, Offset(width * 0.40f, 0f), Offset(width * 0.40f, height), roadWidth)
            drawLine(roadColor, Offset(width * 0.60f, 0f), Offset(width * 0.60f, height), roadWidth)
            drawLine(roadColor, Offset(width * 0.75f, 0f), Offset(width * 0.75f, height), roadWidth)

            // Horizontal Cross Streets
            drawLine(roadColor, Offset(0f, height * 0.22f), Offset(width * 0.78f, height * 0.22f), roadWidth)
            drawLine(roadColor, Offset(0f, height * 0.38f), Offset(width * 0.78f, height * 0.38f), roadWidth)
            drawLine(roadColor, Offset(0f, height * 0.54f), Offset(width * 0.78f, height * 0.54f), roadWidth)
            drawLine(roadColor, Offset(0f, height * 0.70f), Offset(width * 0.78f, height * 0.70f), roadWidth)

            // One highlighted scenic route (Promenade along coast)
            drawLine(
                color = AccentWarmYellow.copy(alpha = 0.6f),
                start = Offset(width * 0.75f, height * 0.20f),
                end = Offset(width * 0.75f, height * 0.65f),
                strokeWidth = 3.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
        }

        // Circular Photographic Markers placed on grid
        pins.forEachIndexed { index, pin ->
            val isSelected = selectedPin?.id == pin.id
            val (xOffset, yOffset) = when (index) {
                0 -> 160.dp to 220.dp
                1 -> 280.dp to 340.dp
                2 -> 90.dp to 380.dp
                3 -> 240.dp to 180.dp
                else -> 180.dp to 460.dp
            }

            Box(
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
            ) {
                ProfileMarker(
                    initials = pin.title.take(2).uppercase(),
                    imageRes = pin.imageRes,
                    isSelected = isSelected,
                    onClick = { selectedPin = pin }
                )
            }
        }

        // Top Navigation Bar (Dark Contrast)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceCard)
                    .border(1.dp, Color(0xFF33332D), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onBackToCatalog
                    )
                    .testTag("map_back_to_catalog_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkTextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, Color(0xFF33332D), RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "WHITE TOWN NEIGHBORHOOD",
                    style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                    color = DarkTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(42.dp))
        }

        // Bottom Floating Photo Card for Selected Maker / Pin (Section 18)
        AnimatedVisibility(
            visible = selectedPin != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val pin = selectedPin
            if (pin != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, Color(0xFF33332D), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                        .testTag("map_selected_pin_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail photo
                        if (pin.imageRes != null) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = pin.imageRes),
                                    contentDescription = pin.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pin.title,
                                    style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                                    color = DarkTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = pin.priceOrAttendees,
                                    style = LoopType.PriceSmall,
                                    color = AccentWarmYellow
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${pin.subtitle} · ${pin.distance}",
                                style = LoopType.Metadata,
                                color = DarkTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(AccentWarmYellow)
                                        .clickable {
                                            if (pin.rentalItem != null) {
                                                onItemClick(pin.rentalItem)
                                            } else if (pin.owner != null) {
                                                onContactOwner(pin.owner)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (pin.rentalItem != null) "View Object" else "Connect",
                                        style = LoopType.ButtonLabel.copy(fontSize = 13.sp),
                                        color = InkCharcoal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
