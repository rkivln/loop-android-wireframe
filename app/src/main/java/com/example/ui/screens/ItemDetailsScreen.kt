package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.GearInspectionModal
import com.example.ui.components.SmartKitBundler
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.AccentYellowDark
import com.example.ui.theme.HeartRed
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoopType
import com.example.ui.theme.ParchmentBg
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentSurface
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg

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
    var isFollowing by remember { mutableStateOf(false) }
    var bundledAccessoryTotal by remember { mutableIntStateOf(200) }
    var showInspectionModal by remember { mutableStateOf(false) }

    val effectiveDailyPrice = item.pricePerDay + bundledAccessoryTotal

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ParchmentBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ) {
            // Hero Photo Showcase with Vintage Decal Stickers (Image 3)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    Image(
                        painter = painterResource(id = item.primaryImageRes),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Distressed Yellow Barcode & Tape Decals Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 20.dp, end = 16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentYellow)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "||| | |||| | ||||||",
                            style = LoopType.CaptionTechnical,
                            color = InkBlack
                        )
                    }

                    // Top Bar overlay: Back button (<), Share and Favorite Heart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button (Rounded squircle)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, ParchmentBorder, RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = Color.LightGray),
                                    onClick = onBack
                                )
                                .testTag("details_back_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = InkBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Right action buttons: Share + Heart
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Share Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, ParchmentBorder, RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = Color.LightGray),
                                        onClick = { /* Share */ }
                                    )
                                    .testTag("details_share_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = InkBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Favorite Heart Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, ParchmentBorder, RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = HeartRed.copy(alpha = 0.2f)),
                                        onClick = {
                                            isFavorite = !isFavorite
                                            onToggleFavorite(item.id)
                                        }
                                    )
                                    .testTag("details_fav_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) HeartRed else InkBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Details Content (Matches Image 3)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title and "✦ Available" Status Badge Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            style = LoopType.H1,
                            modifier = Modifier.weight(1f)
                        )

                        // "✦ Available" Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusGreenBg)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "✦ Available",
                                style = LoopType.CaptionTechnical,
                                color = StatusGreen
                            )
                        }
                    }

                    // Price & Rating Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${item.pricePerDay}",
                                style = LoopType.PriceLarge,
                                color = InkBlack
                            )
                            Text(
                                text = " / day",
                                style = LoopType.BodySmall,
                                color = InkMuted
                            )
                        }

                        // Star Rating: ★ 4.9 (28)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccentYellowDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${item.rating} (${item.reviewCount})",
                                style = LoopType.BodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = InkBlack
                            )
                        }
                    }

                    // Description text
                    Text(
                        text = item.description,
                        style = LoopType.BodyEditorial
                    )

                    // 4-Card Hardware Specifications Strip (Matches Image 3)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailSpecBox(
                            icon = Icons.Default.PhotoCamera,
                            label = "24.2 MP",
                            sublabel = "APS-C"
                        )
                        DetailSpecBox(
                            icon = Icons.Default.Videocam,
                            label = "Full HD",
                            sublabel = "1080p"
                        )
                        DetailSpecBox(
                            icon = Icons.Default.ZoomIn,
                            label = "18-55mm",
                            sublabel = "Kit Lens"
                        )
                        DetailSpecBox(
                            icon = Icons.Default.ShoppingBag,
                            label = "Bag",
                            sublabel = "Included"
                        )
                    }

                    // Smart Kit Bundler Add-on Feature
                    SmartKitBundler(
                        baseItemTitle = item.title,
                        onBundlePriceChanged = { added ->
                            bundledAccessoryTotal = added
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Owner / Host Dossier Row (Arjun S. · Verified Owner · Follow)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ParchmentWhite)
                            .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Circular Avatar
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(InkBlack),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.owner.initials,
                                        style = LoopType.H3,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.owner.name,
                                            style = LoopType.H3,
                                            color = InkBlack
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        // Green verified check badge
                                        Box(
                                            modifier = Modifier
                                                .size(15.dp)
                                                .clip(CircleShape)
                                                .background(StatusGreen),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "✓",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "Verified Owner  ·  White Town",
                                        style = LoopType.BodySmall,
                                        color = InkSecondary
                                    )
                                }
                            }

                            // Follow / Followed Outlined Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, ParchmentBorder, RoundedCornerShape(8.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = Color.LightGray),
                                        onClick = { isFollowing = !isFollowing }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isFollowing) "Following" else "Follow",
                                    style = LoopType.BodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                            }
                        }
                    }

                    // Handover Inspection & Condition Protocol Card (Impressive Feature)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ParchmentWhite)
                            .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
                            .clickable { showInspectionModal = true }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentYellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentTurnedIn,
                                        contentDescription = null,
                                        tint = InkBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Handover Inspection Protocol",
                                        style = LoopType.H3,
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "5-point sensor, lens & battery condition scan",
                                        style = LoopType.BodySmall,
                                        color = InkSecondary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = InkMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Location Map Preview Snippet Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ParchmentWhite)
                            .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
                            .clickable(onClick = onContactOwner)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StatusGreenBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = HeartRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "White Town, Puducherry",
                                        style = LoopType.H3,
                                        color = InkBlack
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "📍 300 m away",
                                        style = LoopType.BodySmall,
                                        color = InkSecondary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = InkMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Bottom Action Bar: "💬 Chat" + "📅 Rent Now" (Matches Image 3)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(ParchmentBg)
                .border(1.dp, ParchmentBorder, RoundedCornerShape(0.dp))
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "💬 Chat" Button (Outlined White)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ParchmentWhite)
                        .border(1.dp, ParchmentBorder, RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.LightGray),
                            onClick = onContactOwner
                        )
                        .testTag("details_chat_host_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Chat",
                            tint = InkBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chat",
                            style = LoopType.BodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                    }
                }

                // "📅 Rent Now" Button (Solid Black)
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(InkBlack)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White),
                            onClick = onRentNow
                        )
                        .testTag("details_rent_now_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Rent Now",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rent Now · ₹$effectiveDailyPrice/d",
                            style = LoopType.ButtonLabel,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Inspection Checklist Modal Sheet
        AnimatedVisibility(
            visible = showInspectionModal,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            GearInspectionModal(
                item = item,
                onCompleteInspection = {
                    showInspectionModal = false
                },
                onDismiss = { showInspectionModal = false }
            )
        }
    }
}

@Composable
private fun DetailSpecBox(
    icon: ImageVector,
    label: String,
    sublabel: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .size(width = 82.dp, height = 70.dp)
            .clip(shape)
            .background(ParchmentWhite, shape)
            .border(1.dp, ParchmentBorder, shape)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = InkBlack,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = LoopType.CaptionTechnical,
                fontWeight = FontWeight.Bold,
                color = InkBlack,
                maxLines = 1
            )
            Text(
                text = sublabel,
                style = LoopType.BodySmall,
                fontSize = 9.sp,
                color = InkSecondary,
                maxLines = 1
            )
        }
    }
}
