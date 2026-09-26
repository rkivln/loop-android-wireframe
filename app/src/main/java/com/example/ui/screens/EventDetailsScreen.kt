package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.EventItem
import com.example.ui.components.AvatarBadge
import com.example.ui.components.AvatarPile
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.ButtonCtaGradient
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple
import com.example.ui.theme.WarmSunset

@Composable
fun EventDetailsScreen(
    event: EventItem,
    onToggleGoing: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var isGoing by remember(event.isGoing) { mutableStateOf(event.isGoing) }
    var attendeeCount by remember(event.attendeeCount) { mutableStateOf(event.attendeeCount) }

    val attendees = remember {
        listOf(
            "Arjun K" to "AK",
            "Sneha R" to "SR",
            "Rahul M" to "RM",
            "Priya S" to "PS",
            "Vikram D" to "VD",
            "Ananya N" to "AN"
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090B10))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Visual Horizon Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    // Abstract Planetary / Sunset Vector Art
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Void night sky background
                        drawRect(Color(0xFF07080F))

                        // Large celestial eclipse ring / sun glow
                        val sunCenter = Offset(w * 0.78f, h * 0.48f)
                        val sunRadius = w * 0.42f

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    WarmSunset.copy(alpha = 0.9f),
                                    Color(0xFFFF9966).copy(alpha = 0.6f),
                                    VividPurple.copy(alpha = 0.4f),
                                    Color.Transparent
                                ),
                                center = sunCenter,
                                radius = sunRadius * 1.4f
                            ),
                            radius = sunRadius * 1.4f,
                            center = sunCenter
                        )

                        // Inner dark planet sphere creating eclipse effect
                        drawCircle(
                            color = Color(0xFF0C0E18),
                            radius = sunRadius * 0.88f,
                            center = sunCenter
                        )

                        // Neon atmospheric edge
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(WarmSunset, VividPurple, ElectricCyan, WarmSunset),
                                center = sunCenter
                            ),
                            radius = sunRadius * 0.88f,
                            center = sunCenter,
                            style = Stroke(width = 4f)
                        )

                        // Terrain / Horizon wave at bottom
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFF090B10)),
                                startY = h * 0.6f,
                                endY = h
                            )
                        )
                    }

                    // Top navigation bar overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = onBack,
                            contentDescription = "Back",
                            testTag = "event_details_back_btn"
                        )

                        GlassIconButton(
                            icon = Icons.Default.MoreVert,
                            onClick = { /* menu */ },
                            contentDescription = "More",
                            testTag = "event_details_more_btn"
                        )
                    }
                }
            }

            // Event Main Content
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    // Category & Distance Badges
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2A1F45))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Event",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFC4B5FD)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x331F2438))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = event.distance,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    Text(
                        text = event.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Metadata rows
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Time
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = VividPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = event.time,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        // Location
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = event.location,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        // Attendees
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$attendeeCount people going",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            AvatarPile(
                                initialsList = listOf("AK", "SR", "RM"),
                                avatarSize = 24.dp,
                                overflowCount = if (attendeeCount > 3) attendeeCount - 3 else 0
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Description
                    Text(
                        text = event.description,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Action Buttons: Primary "I'm Going" & Secondary Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // I'm Going CTA
                        val ctaShape = RoundedCornerShape(27.dp)
                        val ctaModifier = if (isGoing) {
                            Modifier.background(Color(0xFF10B981), ctaShape)
                        } else {
                            Modifier.background(ButtonCtaGradient, ctaShape)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .clip(ctaShape)
                                .then(ctaModifier)
                                .border(1.dp, Color(0x44FFFFFF), ctaShape)
                                .clickable {
                                    isGoing = !isGoing
                                    attendeeCount = if (isGoing) attendeeCount + 1 else attendeeCount - 1
                                    onToggleGoing(event.id)
                                }
                                .testTag("event_going_cta_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isGoing) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (isGoing) "You're Going!" else "I’m Going",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Share Button
                        GlassIconButton(
                            icon = Icons.Default.Share,
                            onClick = { /* share event */ },
                            contentDescription = "Share",
                            size = 54.dp,
                            iconSize = 22.dp,
                            testTag = "event_share_btn"
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Attendees Grid / List
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attendees",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "View all",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeonBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Attendees Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(attendees) { (name, initials) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarBadge(
                                    initials = initials,
                                    size = 48.dp,
                                    colorIndex = initials.hashCode() % 5
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = name.split(" ").first(),
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
