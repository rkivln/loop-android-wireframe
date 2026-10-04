package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.RentalItem
import com.example.ui.components.BodyCopy
import com.example.ui.components.EditorialHeadline
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SecondaryCTA
import com.example.ui.components.StickyBottomAction
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
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
fun ItemDetailsScreen(
    item: RentalItem,
    onRentNow: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onContactOwner: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperWarm)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Hero Large Portrait Photography (Section 4)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    Image(
                        painter = painterResource(id = item.primaryImageRes),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Gradient for Status Bar / Back Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )

                    // Top Floating Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PaperPureWhite.copy(alpha = 0.94f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = onBack
                                )
                                .testTag("details_back_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = InkCharcoal,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PaperPureWhite.copy(alpha = 0.94f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { onToggleFavorite(item.id) }
                                )
                                .testTag("details_favorite_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (item.isFavorite) AccentPeach else InkCharcoal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Category Pill on Image Bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperPureWhite.copy(alpha = 0.96f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${item.category.title.uppercase()} · OBJECT NO. ${item.id.takeLast(4).uppercase()}",
                            style = LoopType.EditorialTag.copy(fontSize = 10.5.sp),
                            color = InkCharcoal
                        )
                    }
                }
            }

            // Object Title & Location
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = item.title,
                            style = LoopType.HeroDisplayLarge.copy(fontSize = 28.sp),
                            color = InkCharcoal,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccentWarmYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f".format(item.rating),
                                style = LoopType.HeadlineMedium.copy(fontSize = 16.sp),
                                color = InkCharcoal
                            )
                            Text(
                                text = " (${item.reviewCount})",
                                style = LoopType.Metadata,
                                color = InkSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = InkSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.location,
                            style = LoopType.BodySmall,
                            color = InkSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = item.description,
                        style = LoopType.BodyEditorial,
                        color = InkCharcoal
                    )
                }
            }

            // Included Specifications & Features (Object Anatomy)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                ) {
                    Text(
                        text = "OBJECT ANATOMY",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item.features.forEach { feature ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(AccentMint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentForestGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = feature.label,
                                    style = LoopType.BodyMedium,
                                    color = InkCharcoal
                                )
                            }
                        }
                    }
                }
            }

            // The Maker (Human profile section)
            item {
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                ) {
                    Text(
                        text = "THE MAKER & HOST",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(PaperIvory)
                                        .border(1.dp, PaperBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.owner.initials,
                                        style = LoopType.HeadlineMedium.copy(fontSize = 18.sp),
                                        color = InkCharcoal
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.owner.name,
                                            style = LoopType.HeadlineMedium.copy(fontSize = 18.sp),
                                            color = InkCharcoal
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(AccentMint)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = item.owner.badge,
                                                style = LoopType.Metadata.copy(fontSize = 10.sp, color = AccentForestGreen)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${item.owner.memberSince} · Responds ${item.owner.responseTime}",
                                        style = LoopType.Metadata,
                                        color = InkSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            SecondaryCTA(
                                text = "Direct Studio Message",
                                onClick = onContactOwner,
                                testTag = "details_contact_host_btn"
                            )
                        }
                    }
                }
            }

            // Guarantee & Safety Note
            item {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = AccentForestGreen,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Loop Protection & Inspection",
                            style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = InkCharcoal
                        )
                        Text(
                            text = "Damage protection covered up to ₹50,000. Verified contactless handoff.",
                            style = LoopType.BodySmall.copy(fontSize = 12.sp),
                            color = InkSecondary
                        )
                    }
                }
            }
        }

        // Sticky Floating Bottom Action Bar (Section 8)
        StickyBottomAction(
            pricePerDay = item.pricePerDay,
            actionButtonText = "Reserve Piece",
            onActionClick = onRentNow,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
