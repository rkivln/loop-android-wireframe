package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.AccentYellowSoft
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
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.theme.PlusJakartaFontFamily

/**
 * Editorial Top Navigation Bar
 */
@Composable
fun EditorialTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    issueTag: String? = null,
    onBack: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PaperIvory)
                    .border(1.dp, PaperBorderSubtle, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onBack
                    )
                    .testTag("top_bar_back_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkCharcoal
                )
            }
        } else if (issueTag != null) {
            Text(
                text = issueTag.uppercase(),
                style = LoopType.EditorialTag,
                color = InkSecondary
            )
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }

        if (title != null) {
            Text(
                text = title,
                style = LoopType.HeadlineMedium,
                color = InkCharcoal,
                textAlign = TextAlign.Center
            )
        }

        if (trailingContent != null) {
            trailingContent()
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}

/**
 * Editorial Headline
 */
@Composable
fun EditorialHeadline(
    text: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false,
    color: Color = InkCharcoal,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = if (isLarge) LoopType.HeroDisplayLarge else LoopType.HeroDisplay,
        color = color,
        textAlign = textAlign,
        modifier = modifier
    )
}

/**
 * Refined Body Copy
 */
@Composable
fun BodyCopy(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = InkSecondary,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        style = LoopType.BodyEditorial,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/**
 * Primary CTA Button
 * Minimalist, solid pill, 52dp height, tactile press feedback
 */
@Composable
fun PrimaryCTA(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = InkCharcoal,
    contentColor: Color = InkWhite,
    trailingIcon: ImageVector? = null,
    testTag: String = "primary_cta_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "btn_press_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = text,
                style = LoopType.ButtonLabel,
                color = contentColor
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Secondary CTA Button
 * Calm, subtle border, matching height
 */
@Composable
fun SecondaryCTA(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PaperIvory,
    contentColor: Color = InkCharcoal,
    testTag: String = "secondary_cta_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "btn_secondary_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(backgroundColor)
            .border(1.dp, PaperBorder, RoundedCornerShape(26.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = InkCharcoal.copy(alpha = 0.1f)),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = LoopType.ButtonSecondaryLabel,
            color = contentColor
        )
    }
}

/**
 * Pagination Dots
 * Tiny, refined, elongated active indicator
 */
@Composable
fun PaginationDots(
    totalPages: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = InkCharcoal,
    inactiveColor: Color = PaperBorder
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalPages) {
            val isSelected = i == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 18.dp else 6.dp,
                animationSpec = tween(durationMillis = 260),
                label = "dot_width"
            )

            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else inactiveColor)
            )
        }
    }
}

/**
 * Section Label
 */
@Composable
fun SectionLabel(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = LoopType.EditorialTag,
            color = InkCharcoal
        )

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                color = InkSecondary,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onActionClick
                    )
                    .padding(vertical = 4.dp)
            )
        }
    }
}

/**
 * PhotoCollage Component
 * Physical moodboard feel with hero photo, overlapping rotated secondary photo, and badge
 */
@Composable
fun PhotoCollage(
    @DrawableRes heroImageRes: Int,
    @DrawableRes secondaryImageRes: Int,
    badgeText: String? = "VOL. 01 — CURATED",
    modifier: Modifier = Modifier,
    secondaryRotation: Float = -4.5f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        // Main Portrait Hero Photo
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.82f)
                .height(310.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = heroImageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Secondary Overlapping Mini Photo (Offset & Rotated)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (10).dp)
                .size(width = 130.dp, height = 150.dp)
                .rotate(secondaryRotation)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.15f))
                .clip(RoundedCornerShape(16.dp))
                .background(PaperPureWhite)
                .border(3.dp, PaperPureWhite, RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = secondaryImageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Small Issue Tag / Badge
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 16.dp, y = 14.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(PaperPureWhite.copy(alpha = 0.94f))
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(100.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badgeText,
                    style = LoopType.EditorialTag.copy(fontSize = 9.5.sp),
                    color = InkCharcoal
                )
            }
        }
    }
}

/**
 * Editorial Photo Card for Feeds and Discovery (Section 19)
 * Dominant photography, restrained metadata, tactile feel
 */
@Composable
fun EditorialPhotoCard(
    item: RentalItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "card_scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .testTag("photo_card_${item.id}")
    ) {
        // Large Image Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isLarge) 240.dp else 190.dp)
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
        ) {
            Image(
                painter = painterResource(id = item.primaryImageRes),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Category pill on top left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(PaperPureWhite.copy(alpha = 0.92f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.category.title.uppercase(),
                    style = LoopType.Metadata.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = InkCharcoal
                )
            }

            // Favorite Icon button top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PaperPureWhite.copy(alpha = 0.92f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onToggleFavorite
                    )
                    .testTag("fav_btn_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (item.isFavorite) AccentPeach else InkCharcoal,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Location tag bottom left
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(InkCharcoal.copy(alpha = 0.75f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = InkWhite,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = item.location,
                    style = LoopType.Metadata.copy(fontSize = 10.sp, color = InkWhite)
                )
            }
        }

        // Minimal Editorial Metadata below photo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = LoopType.HeadlineMedium.copy(fontSize = 18.sp),
                    color = InkCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AccentWarmYellow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "%.1f".format(item.rating),
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, color = InkCharcoal)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.description,
                style = LoopType.BodySmall,
                color = InkSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hosted by ${item.owner.name}",
                    style = LoopType.Metadata,
                    color = InkMuted
                )

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹${item.pricePerDay}",
                        style = LoopType.PriceSmall,
                        color = InkCharcoal
                    )
                    Text(
                        text = " / day",
                        style = LoopType.Metadata,
                        color = InkSecondary
                    )
                }
            }
        }
    }
}

/**
 * Tactile Category Tile (Section 20)
 * Soft pastel backgrounds (Mint, Soft yellow, Muted Peach, Powder blue, Warm beige)
 */
@Composable
fun CategoryTile(
    category: RentalCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pastelBg = when (category) {
        RentalCategory.ELECTRONICS -> AccentPowderBlue
        RentalCategory.FURNITURE -> AccentBeigeOat
        RentalCategory.VEHICLES -> AccentMint
        RentalCategory.TOOLS -> AccentYellowSoft
        RentalCategory.EVENTS -> AccentPeach.copy(alpha = 0.4f)
        RentalCategory.STUDY_OFFICE -> AccentPowderBlue.copy(alpha = 0.7f)
        RentalCategory.HOME_LIVING -> AccentBeigeOat
    }

    Column(
        modifier = modifier
            .width(82.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = onClick
            )
            .testTag("cat_tile_${category.name}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isSelected) InkCharcoal else pastelBg)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) InkCharcoal else PaperBorder,
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.iconEmoji,
                fontSize = 26.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = category.title,
            style = LoopType.Metadata.copy(
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) InkCharcoal else InkSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Circular Photographic Profile Marker (Section 18)
 */
@Composable
fun ProfileMarker(
    initials: String,
    @DrawableRes imageRes: Int? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(if (isSelected) 46.dp else 40.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(PaperPureWhite)
            .border(2.5.dp, if (isSelected) AccentWarmYellow else PaperPureWhite, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials,
                style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                color = InkCharcoal
            )
        }
    }
}

/**
 * Sticky Floating Bottom Action Bar
 */
@Composable
fun StickyBottomAction(
    pricePerDay: Int,
    actionButtonText: String = "Reserve Piece",
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    noteText: String? = "Instant confirmation · Free cancellation"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PaperPureWhite.copy(alpha = 0.98f))
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹$pricePerDay",
                        style = LoopType.PriceHeadline,
                        color = InkCharcoal
                    )
                    Text(
                        text = " / day",
                        style = LoopType.BodySmall,
                        color = InkSecondary
                    )
                }
                if (noteText != null) {
                    Text(
                        text = noteText,
                        style = LoopType.Metadata.copy(fontSize = 10.sp),
                        color = InkMuted
                    )
                }
            }

            Box(modifier = Modifier.width(180.dp)) {
                PrimaryCTA(
                    text = actionButtonText,
                    onClick = onActionClick,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    testTag = "sticky_action_reserve_btn"
                )
            }
        }
    }
}
