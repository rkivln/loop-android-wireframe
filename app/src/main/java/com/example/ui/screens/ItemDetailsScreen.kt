package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RentalItem
import com.example.data.models.SpecFeature
import com.example.ui.components.LoopPrimaryButton
import com.example.ui.components.LoopSecondaryButton
import com.example.ui.theme.AccentCobalt
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CanvasDark
import com.example.ui.theme.CanvasGround
import com.example.ui.theme.CanvasSubtle
import com.example.ui.theme.CanvasWhite
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LineHairline
import com.example.ui.theme.RatingAmber
import com.example.ui.theme.StatusLive

@Composable
fun ItemDetailsScreen(
    item: RentalItem,
    onRentNow: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onContactOwner: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var isFavorite by remember(item.isFavorite) { mutableStateOf(item.isFavorite) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasGround)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
        ) {
            // Hardware Photo Gallery Showcase
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                        .background(CanvasSubtle)
                ) {
                    Image(
                        painter = painterResource(id = item.primaryImageRes),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Bar overlay: Back button, Heart, Share
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CanvasWhite.copy(alpha = 0.92f))
                                .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                                .clickable(onClick = onBack)
                                .testTag("details_back_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = InkPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Right icons (Heart & Share)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CanvasWhite.copy(alpha = 0.92f))
                                    .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                                    .clickable {
                                        isFavorite = !isFavorite
                                        onToggleFavorite(item.id)
                                    }
                                    .testTag("details_fav_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) AlertCrimson else InkPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CanvasWhite.copy(alpha = 0.92f))
                                    .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                                    .clickable { /* share */ }
                                    .testTag("details_share_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = InkPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Photo Counter & Inspection Status Pill (Bottom)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CanvasDark.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "1 / ${item.imageCount} PHOTOS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Hardware Dossier: Title, Price, Specs, Description, Host
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Block: Category, Title, Rating, Price
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "// ${item.category.title.uppercase()} HARDWARE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp,
                                color = InkMuted
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = RatingAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${item.rating} (${item.reviewCount} reviews)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = InkPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            color = InkPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${item.pricePerDay}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = InkPrimary
                            )
                            Text(
                                text = " / day",
                                fontSize = 13.sp,
                                color = InkMuted,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CanvasSubtle)
                                    .border(1.dp, LineHairline, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ZERO DEPOSIT WITH ID",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = StatusLive
                                )
                            }
                        }
                    }

                    // Technical Specifications Matrix (Hairline Grid)
                    Column {
                        Text(
                            text = "HARDWARE SPECIFICATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                            color = InkMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CanvasWhite)
                                .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                        ) {
                            Column {
                                val features = item.features
                                for (i in features.indices step 2) {
                                    if (i > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(LineHairline)
                                        )
                                    }
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        SpecMatrixCell(
                                            feature = features[i],
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(54.dp)
                                                .background(LineHairline)
                                        )
                                        if (i + 1 < features.size) {
                                            SpecMatrixCell(
                                                feature = features[i + 1],
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Equipment Description
                    Column {
                        Text(
                            text = "DESCRIPTION & INCLUDED KIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                            color = InkMuted
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CanvasWhite)
                                .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = item.description,
                                fontSize = 13.5.sp,
                                lineHeight = 20.sp,
                                color = InkPrimary
                            )
                        }
                    }

                    // Verified Host Dossier Card
                    Column {
                        Text(
                            text = "EQUIPMENT HOST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                            color = InkMuted
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CanvasWhite)
                                .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(CanvasDark),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = item.owner.initials,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.owner.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = InkPrimary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.VerifiedUser,
                                                    contentDescription = "Verified",
                                                    tint = AccentCobalt,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(
                                                text = "${item.owner.badge} · ${item.owner.memberSince}",
                                                fontSize = 11.5.sp,
                                                color = InkSecondary
                                            )
                                        }
                                    }

                                    // Direct Chat Action
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CanvasSubtle)
                                            .border(1.dp, LineHairline, RoundedCornerShape(4.dp))
                                            .clickable(onClick = onContactOwner)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag("details_chat_host_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "CHAT",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = InkPrimary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(LineHairline)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "AVG RESPONSE",
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = InkMuted
                                        )
                                        Text(
                                            text = item.owner.responseTime,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkPrimary
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "TOTAL RENTALS",
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = InkMuted
                                        )
                                        Text(
                                            text = "${item.owner.totalRentals} completed",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkPrimary
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "PICKUP AREA",
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = InkMuted
                                        )
                                        Text(
                                            text = "White Town",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Persistent Architectural Rental Action Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CanvasWhite)
                .border(1.dp, LineHairline, RoundedCornerShape(0.dp))
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${item.pricePerDay}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = InkPrimary
                        )
                        Text(
                            text = "/day",
                            fontSize = 11.5.sp,
                            color = InkMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Zero Deposit · White Town Handover",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = StatusLive
                    )
                }

                LoopPrimaryButton(
                    text = "RENT NOW",
                    onClick = onRentNow,
                    height = 44.dp,
                    showArrow = true,
                    modifier = Modifier.width(160.dp),
                    testTag = "details_rent_now_btn"
                )
            }
        }
    }
}

@Composable
private fun SpecMatrixCell(
    feature: SpecFeature,
    modifier: Modifier = Modifier
) {
    val icon = when (feature.iconType) {
        "CAMERA" -> Icons.Default.PhotoCamera
        "LENS" -> Icons.Default.ZoomIn
        "STORAGE" -> Icons.Default.SdCard
        "BAG" -> Icons.Default.ShoppingBag
        "CHIP" -> Icons.Default.Memory
        else -> Icons.Default.PhotoCamera
    }

    Row(
        modifier = modifier.padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InkMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = feature.iconType,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = InkMuted
            )
            Text(
                text = feature.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary,
                maxLines = 1
            )
        }
    }
}
