package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType
import com.example.data.models.MapCategoryFilter
import com.example.data.models.RentalCategory
import com.example.data.models.UserCommunityPost
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
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

/**
 * Reusable UI component that appears when a user clicks a map marker.
 *
 * Prominently presents:
 * - Event Name ([eventName])
 * - Scheduled Time ([eventTime])
 * - Category badge with icon / emoji ([category], [categoryEmoji])
 * - Location, distance, and walking/cycling time estimates
 * - Attendee metrics and RSVP / Join direct actions
 * - Navigation / directions intent launcher
 */
@Composable
fun EventMapMarkerCallout(
    eventName: String,
    eventTime: String,
    category: String,
    modifier: Modifier = Modifier,
    categoryEmoji: String? = null,
    categoryIcon: ImageVector? = null,
    categoryBgColor: Color = AccentMint,
    locationName: String? = null,
    address: String? = null,
    distance: String? = null,
    walkingTime: String? = null,
    cyclingTime: String? = null,
    attendeesInfo: String? = null,
    description: String? = null,
    isAttending: Boolean = false,
    rating: Float? = null,
    @DrawableRes imageRes: Int? = null,
    onDirectionsClick: (() -> Unit)? = null,
    onRsvpClick: (() -> Unit)? = null,
    onViewDetailsClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onDismiss: () -> Unit = {}
) {
    var isRsvpdLocally by remember(isAttending) { mutableStateOf(isAttending) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x33000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(PaperPureWhite)
            .border(1.5.dp, PaperBorderSubtle, RoundedCornerShape(24.dp))
            .testTag("event_marker_callout")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Meta Bar: Category Pill, Live Status & Close Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Pill Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(categoryBgColor.copy(alpha = 0.85f))
                        .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                        .semantics(mergeDescendants = true) {}
                        .testTag("event_marker_category")
                ) {
                    if (categoryEmoji != null) {
                        Text(
                            text = categoryEmoji,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    } else if (categoryIcon != null) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = InkCharcoal,
                            modifier = Modifier
                                .size(13.dp)
                                .padding(end = 3.dp)
                        )
                    }
                    Text(
                        text = category.uppercase(),
                        style = LoopType.EditorialTag.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = InkCharcoal
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rating Badge (if available)
                    if (rating != null && rating > 0f) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(PaperIvory)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccentWarmYellow,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format("%.1f", rating),
                                style = LoopType.Metadata.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = InkCharcoal
                            )
                        }
                    }

                    // Share Action (optional)
                    if (onShareClick != null) {
                        IconButton(
                            onClick = onShareClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("event_marker_share_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share event",
                                tint = InkSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PaperIvory)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onDismiss
                            )
                            .testTag("event_marker_close_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close details",
                            tint = InkCharcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content Row: Image Thumbnail (if any) + Title & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (imageRes != null) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                            .testTag("event_marker_thumbnail")
                    ) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = eventName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Event Name
                    Text(
                        text = eventName,
                        style = LoopType.HeadlineMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        ),
                        color = InkCharcoal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("event_marker_name")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Time Row (Primary Requirement)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .semantics(mergeDescendants = true) {}
                            .testTag("event_marker_time")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(AccentWarmYellow.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Event time",
                                tint = InkCharcoal,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = eventTime,
                            style = LoopType.Metadata.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = InkCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Location Row
                    if (!locationName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("event_marker_location")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Event location",
                                tint = InkSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = buildString {
                                    append(locationName)
                                    if (!distance.isNullOrBlank()) {
                                        append(" · ")
                                        append(distance)
                                    }
                                },
                                style = LoopType.Metadata.copy(fontSize = 12.sp),
                                color = InkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Description Snippet (if available)
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = description,
                    style = LoopType.BodyMedium.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    ),
                    color = InkSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("event_marker_description")
                )
            }

            // Quick Info Badges: Walking/Cycling times & Attendees
            if (!walkingTime.isNullOrBlank() || !attendeesInfo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaperIvory)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Transit walk & cycle estimates
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!walkingTime.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                    contentDescription = null,
                                    tint = InkSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = walkingTime,
                                    style = LoopType.Metadata.copy(fontSize = 11.sp),
                                    color = InkSecondary
                                )
                            }
                        }

                        if (!cyclingTime.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = null,
                                    tint = InkSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = cyclingTime,
                                    style = LoopType.Metadata.copy(fontSize = 11.sp),
                                    color = InkSecondary
                                )
                            }
                        }
                    }

                    // Attendees count / registration status
                    if (!attendeesInfo.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = AccentForestGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = attendeesInfo,
                                style = LoopType.Metadata.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = AccentForestGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Directions & Primary CTA (RSVP / Details)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Directions Action Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorder, RoundedCornerShape(22.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = { onDirectionsClick?.invoke() }
                        )
                        .testTag("event_marker_directions_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = "Get directions",
                            tint = InkCharcoal,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Directions",
                            style = LoopType.ButtonSecondaryLabel.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = InkCharcoal
                        )
                    }
                }

                // Primary CTA (RSVP / Attending / Join / Details)
                val isAttendingState = isRsvpdLocally
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isAttendingState) AccentForestGreen else InkCharcoal)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = {
                                isRsvpdLocally = !isAttendingState
                                onRsvpClick?.invoke() ?: onViewDetailsClick?.invoke()
                            }
                        )
                        .testTag("event_marker_rsvp_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isAttendingState) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = InkWhite,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (isAttendingState) "RSVP'd ✓" else "RSVP / Join",
                            style = LoopType.ButtonLabel.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = InkWhite
                        )
                    }
                }
            }
        }
    }
}

/**
 * Convenient overload accepting domain model [DiscoveryPinItem].
 */
@Composable
fun EventMapMarkerCallout(
    pin: DiscoveryPinItem,
    modifier: Modifier = Modifier,
    isAttending: Boolean = pin.isAttending,
    onDirectionsClick: (() -> Unit)? = null,
    onRsvpClick: (() -> Unit)? = null,
    onViewDetailsClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onDismiss: () -> Unit = {}
) {
    val (categoryTitle, categoryEmoji, categoryColor, categoryIcon) = when (pin.type) {
        DiscoveryPinType.COMMUNITY_EVENT -> Quadruple(
            "Local Event",
            "📸",
            AccentMint,
            Icons.Outlined.PhotoCamera
        )
        DiscoveryPinType.STUDY_GROUP -> Quadruple(
            "Study Group",
            "📚",
            AccentPowderBlue,
            Icons.Outlined.MenuBook
        )
        DiscoveryPinType.HELP_REQUEST -> Quadruple(
            "Help Needed",
            "🤝",
            AccentPeach,
            Icons.Outlined.Handshake
        )
        DiscoveryPinType.PICKUP_HUB -> Quadruple(
            "Smart Hub",
            "🔒",
            AccentMint,
            Icons.Outlined.Event
        )
        else -> Quadruple(
            pin.filterCategory.title,
            pin.filterCategory.emoji,
            AccentBeigeOat,
            Icons.Outlined.Event
        )
    }

    EventMapMarkerCallout(
        eventName = pin.title,
        eventTime = pin.dateOrAvailability,
        category = categoryTitle,
        categoryEmoji = categoryEmoji,
        categoryIcon = categoryIcon,
        categoryBgColor = categoryColor,
        locationName = pin.locationName,
        address = pin.address,
        distance = pin.distance,
        walkingTime = pin.walkingTime,
        cyclingTime = pin.cyclingTime,
        attendeesInfo = pin.priceOrAttendees,
        description = pin.description,
        isAttending = isAttending,
        rating = pin.rating,
        imageRes = pin.imageRes,
        onDirectionsClick = onDirectionsClick,
        onRsvpClick = onRsvpClick,
        onViewDetailsClick = onViewDetailsClick,
        onShareClick = onShareClick,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/**
 * Convenient overload accepting domain model [UserCommunityPost].
 */
@Composable
fun EventMapMarkerCallout(
    event: UserCommunityPost,
    modifier: Modifier = Modifier,
    isAttending: Boolean = false,
    distance: String? = "Nearby",
    walkingTime: String? = "5 min walk",
    onDirectionsClick: (() -> Unit)? = null,
    onRsvpClick: (() -> Unit)? = null,
    onViewDetailsClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onDismiss: () -> Unit = {}
) {
    val (categoryTitle, categoryEmoji, categoryColor, categoryIcon) = when (event.type.name) {
        "EVENT" -> Quadruple("Local Event", "📸", AccentMint, Icons.Outlined.PhotoCamera)
        "STUDY_GROUP" -> Quadruple("Study Group", "📚", AccentPowderBlue, Icons.Outlined.MenuBook)
        "HELP_REQUEST" -> Quadruple("Help Needed", "🤝", AccentPeach, Icons.Outlined.Handshake)
        else -> Quadruple(event.category.title, event.category.iconEmoji, AccentBeigeOat, Icons.Outlined.Event)
    }

    EventMapMarkerCallout(
        eventName = event.title,
        eventTime = event.dateTime,
        category = categoryTitle,
        categoryEmoji = categoryEmoji,
        categoryIcon = categoryIcon,
        categoryBgColor = categoryColor,
        locationName = event.location,
        distance = distance,
        walkingTime = walkingTime,
        attendeesInfo = event.attendeesOrResponses,
        description = event.description,
        isAttending = isAttending,
        onDirectionsClick = onDirectionsClick,
        onRsvpClick = onRsvpClick,
        onViewDetailsClick = onViewDetailsClick,
        onShareClick = onShareClick,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
