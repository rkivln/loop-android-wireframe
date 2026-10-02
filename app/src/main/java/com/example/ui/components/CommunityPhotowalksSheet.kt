package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoopType
import com.example.ui.theme.ParchmentBg
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg

data class PhotowalkEvent(
    val id: String,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val host: String,
    val attendeesCount: Int,
    val gearHighlights: List<String>,
    val description: String
)

@Composable
fun CommunityPhotowalksSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val events = remember {
        listOf(
            PhotowalkEvent(
                id = "walk_1",
                title = "Promenade Golden Hour Photowalk",
                date = "This Saturday",
                time = "5:30 PM – 7:30 PM",
                location = "Goubert Ave (Near Old Lighthouse)",
                host = "Arjun S. & Pondy Street Collective",
                attendeesCount = 14,
                gearHighlights = listOf("Canon 50mm f/1.8", "Sony 85mm GM", "Fuji X100V", "DJI RS3 Gimbal"),
                description = "Golden hour ocean portraits & street photography. Bring your camera or test lenses borrowed from neighbors."
            ),
            PhotowalkEvent(
                id = "walk_2",
                title = "French Quarter Heritage Architecture Walk",
                date = "Sunday Morning",
                time = "6:45 AM – 8:45 AM",
                location = "Café des Arts, Rue Suffren",
                host = "Priya M. (Architect & Photographer)",
                attendeesCount = 9,
                gearHighlights = listOf("16-35mm Ultra Wide", "Tilt-Shift 24mm", "Ricoh GR III"),
                description = "Colonial yellow facades, bougainvillea gates, and morning light. Wide-angle lens testing session."
            ),
            PhotowalkEvent(
                id = "walk_3",
                title = "Auroville Forest Wildlife & Telephoto Jam",
                date = "Next Tuesday",
                time = "4:00 PM – 6:30 PM",
                location = "Auroville Botanical Gardens",
                host = "Vikram K. (Wildlife Biologist)",
                attendeesCount = 7,
                gearHighlights = listOf("Sigma 150-600mm", "Sony 200-600mm", "Monopod Kit"),
                description = "Birding and macro testing in lush greenery. Super-telephoto lenses available to try on-site."
            )
        )
    }

    val rsvpStates = remember { mutableStateMapOf("walk_1" to true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(ParchmentBg)
                .clickable(enabled = false) {}
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentYellow)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "CREATOR COMMUNITY",
                                        style = LoopType.CaptionTechnical,
                                        color = InkBlack
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Local Photowalks & Jams",
                                style = LoopType.H1
                            )
                            Text(
                                text = "Meet local creators, shoot together, and test community gear",
                                style = LoopType.BodySmall
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = InkBlack
                            )
                        }
                    }
                }

                // Event Cards
                items(events, key = { it.id }) { event ->
                    val isRsvped = rsvpStates[event.id] == true

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ParchmentWhite)
                            .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Date / Time Badge & RSVP Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ParchmentBg)
                                            .border(1.dp, ParchmentBorder, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${event.date} · ${event.time}",
                                            style = LoopType.CaptionTechnical,
                                            color = InkBlack
                                        )
                                    }
                                }

                                // RSVP Toggle Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isRsvped) StatusGreenBg else InkBlack)
                                        .border(1.dp, if (isRsvped) StatusGreen else InkBlack, RoundedCornerShape(8.dp))
                                        .clickable {
                                            rsvpStates[event.id] = !isRsvped
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("rsvp_btn_${event.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isRsvped) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = StatusGreen,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = if (isRsvped) "Attending ✓" else "Join Walk",
                                            style = LoopType.BodySmall,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            color = if (isRsvped) StatusGreen else Color.White
                                        )
                                    }
                                }
                            }

                            // Title & Description
                            Text(
                                text = event.title,
                                style = LoopType.H2
                            )

                            Text(
                                text = event.description,
                                style = LoopType.BodyEditorial
                            )

                            // Location & Host
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = InkMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${event.location}  ·  Hosted by ${event.host}",
                                    style = LoopType.BodySmall,
                                    color = InkSecondary
                                )
                            }

                            // Gear Highlight Badges (What creators are bringing to try)
                            Column {
                                Text(
                                    text = "GEAR AVAILABLE TO TRY ON THIS WALK:",
                                    style = LoopType.CaptionTechnical,
                                    color = InkMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    event.gearHighlights.forEach { gear ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ParchmentBg)
                                                .border(1.dp, ParchmentBorder, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = gear,
                                                style = LoopType.CaptionTechnical,
                                                color = InkBlack
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
    }
}
