package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.models.CommunityEvent
import com.example.data.models.CommunityPostStatus
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType

@Composable
fun EventMapMarkerCallout(
    event: CommunityEvent,
    onJoinClick: (CommunityEvent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("event_marker_callout")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            when (event.category.uppercase()) {
                                "HELP NEEDED" -> MaterialTheme.colorScheme.errorContainer
                                "WORKSHOP" -> MaterialTheme.colorScheme.primaryContainer
                                "CLEANUP" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .semantics(mergeDescendants = true) {}
                        .testTag("event_marker_category")
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                when (event.category.uppercase()) {
                                    "HELP NEEDED" -> MaterialTheme.colorScheme.error
                                    "WORKSHOP" -> MaterialTheme.colorScheme.primary
                                    "CLEANUP" -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.secondary
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (event.category.uppercase()) {
                            "HELP NEEDED" -> MaterialTheme.colorScheme.onErrorContainer
                            "WORKSHOP" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "CLEANUP" -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Participants",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.participantCount} going",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("event_marker_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("event_marker_description")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .semantics(mergeDescendants = true) {}
                        .testTag("event_marker_time")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Event time",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.timeString,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .semantics(mergeDescendants = true) {}
                        .testTag("event_marker_location")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Event location",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onJoinClick(event) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_marker_join_button")
            ) {
                Text(
                    text = "Join Activity Point",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}


@Composable
fun EventMapMarkerCallout(
    pin: DiscoveryPinItem,
    currentUserId: String = "",
    onDirectionsClick: () -> Unit = {},
    onRsvpClick: () -> Unit = {},
    onViewDetailsClick: () -> Unit = {},
    onMessageOwnerOrOrganizerClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isRentalPin = pin.type == DiscoveryPinType.RENTAL_ITEM || pin.rentalItem != null
    val isCommunityPin = pin.type == DiscoveryPinType.COMMUNITY_EVENT ||
        pin.type == DiscoveryPinType.HELP_REQUEST ||
        pin.type == DiscoveryPinType.STUDY_GROUP
    val isOwnRental = pin.rentalItem?.owner?.id?.let { it.isNotBlank() && it == currentUserId } == true
    val isOwnCommunityPost = pin.communityPost?.authorId?.let { it.isNotBlank() && it == currentUserId } == true
    val isResolvedPost = pin.communityPost?.status == CommunityPostStatus.COMPLETED

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("event_marker_callout")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = pin.tag.ifBlank { pin.type.name }.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isOwnRental || isOwnCommunityPost) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isOwnRental) "YOUR LISTING" else "YOUR POST",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    } else if (pin.dateOrAvailability.isNotBlank()) {
                        val isUnavailable = pin.dateOrAvailability.equals("Booked", ignoreCase = true) ||
                            pin.dateOrAvailability.equals("Paused", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    if (isUnavailable) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.primaryContainer
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = pin.dateOrAvailability,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                color = if (isUnavailable) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("callout_dismiss_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close marker details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = pin.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            val metaLine = listOfNotNull(
                pin.priceOrAttendees.takeIf { it.isNotBlank() },
                pin.distance.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            if (metaLine.isNotBlank()) {
                Text(
                    text = metaLine,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (pin.description.isNotBlank() || pin.subtitle.isNotBlank()) {
                Text(
                    text = pin.description.ifBlank { pin.subtitle },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDirectionsClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("callout_directions_btn")
                ) {
                    Text("Directions", maxLines = 1)
                }

                when {
                    isRentalPin -> {
                        Button(
                            onClick = onViewDetailsClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("callout_view_piece_btn")
                        ) {
                            Text(if (isOwnRental) "Manage" else "View Piece", maxLines = 1)
                        }
                        if (!isOwnRental && pin.rentalItem?.owner?.id?.isNotBlank() == true) {
                            OutlinedButton(
                                onClick = onMessageOwnerOrOrganizerClick,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("callout_message_host_btn")
                            ) {
                                Text("Message", maxLines = 1)
                            }
                        }
                    }
                    isCommunityPin -> {
                        if (!isOwnCommunityPost && !isResolvedPost) {
                            Button(
                                onClick = onRsvpClick,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("callout_rsvp_btn")
                            ) {
                                if (pin.isAttending) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Attending", maxLines = 1)
                                } else {
                                    Text("RSVP", maxLines = 1)
                                }
                            }
                            if (pin.communityPost?.authorId?.isNotBlank() == true) {
                                OutlinedButton(
                                    onClick = onMessageOwnerOrOrganizerClick,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("callout_message_organizer_btn")
                                ) {
                                    Text("Message", maxLines = 1)
                                }
                            }
                        } else {
                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(if (isResolvedPost) "Resolved" else "Close", maxLines = 1)
                            }
                        }
                    }
                    else -> {
                        if (pin.owner != null && pin.owner.id.isNotBlank() && pin.owner.id != currentUserId) {
                            Button(
                                onClick = onMessageOwnerOrOrganizerClick,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text("Message Host", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
