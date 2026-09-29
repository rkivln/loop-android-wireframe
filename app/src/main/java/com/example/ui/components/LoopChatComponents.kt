package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ChatMessageType
import com.example.data.models.MessageStatus
import com.example.data.models.RentalMessage
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LoopChatBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    )
}

@Composable
fun LoopChatDateDivider(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE2E8F0).copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 0.4.sp
            )
        }
    }
}

@Composable
fun LoopRentalContextBanner(
    itemTitle: String,
    pricePerDay: Int,
    dates: String,
    location: String,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onViewDetails)
            .padding(12.dp)
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
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = itemTitle,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹$pricePerDay/day",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandBlue
                        )
                    }
                    Text(
                        text = "$dates · $location",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Details",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDark
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = BrandDark,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun LoopMessageBubble(
    message: RentalMessage,
    onViewItem: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isMe = message.isFromMe

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val bubbleBg = if (isMe) BrandDark else SurfaceCard
    val textColor = if (isMe) Color.White else TextPrimary
    val timeColor = if (isMe) Color.White.copy(alpha = 0.65f) else TextMuted

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 70.dp, max = 310.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .border(
                    width = if (isMe) 0.dp else 1.dp,
                    color = if (isMe) Color.Transparent else BorderSubtle,
                    shape = bubbleShape
                )
                .padding(
                    start = if (message.messageType == ChatMessageType.RENTAL_OFFER) 0.dp else 14.dp,
                    end = if (message.messageType == ChatMessageType.RENTAL_OFFER) 0.dp else 14.dp,
                    top = if (message.messageType == ChatMessageType.RENTAL_OFFER) 0.dp else 10.dp,
                    bottom = if (message.messageType == ChatMessageType.RENTAL_OFFER) 0.dp else 8.dp
                )
        ) {
            when (message.messageType) {
                ChatMessageType.TEXT -> {
                    Column {
                        Text(
                            text = message.text,
                            fontSize = 14.5.sp,
                            lineHeight = 20.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LoopMessageFooter(
                            timestamp = message.timestamp,
                            isMe = isMe,
                            status = message.status,
                            timeColor = timeColor
                        )
                    }
                }

                ChatMessageType.AUDIO_NOTE -> {
                    LoopVoiceNoteBubble(
                        message = message,
                        isMe = isMe,
                        timeColor = timeColor
                    )
                }

                ChatMessageType.LOCATION_PIN -> {
                    LoopLocationPinBubble(
                        message = message,
                        isMe = isMe,
                        timeColor = timeColor
                    )
                }

                ChatMessageType.RENTAL_OFFER -> {
                    LoopRentalOfferCard(
                        message = message,
                        onViewItem = { message.itemTitle?.let { onViewItem?.invoke(it) } }
                    )
                }

                ChatMessageType.IMAGE_MEDIA -> {
                    Column {
                        if (message.imageRes != null) {
                            Image(
                                painter = painterResource(id = message.imageRes),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        if (message.text.isNotBlank()) {
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                color = textColor
                            )
                        }
                        LoopMessageFooter(
                            timestamp = message.timestamp,
                            isMe = isMe,
                            status = message.status,
                            timeColor = timeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoopVoiceNoteBubble(
    message: RentalMessage,
    isMe: Boolean,
    timeColor: Color
) {
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0.4f) }

    Column(modifier = Modifier.width(240.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Play/Pause circular button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isMe) Color.White else BrandDark)
                    .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (isMe) BrandDark else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Waveform
            Column(modifier = Modifier.weight(1f)) {
                LoopAudioWaveform(
                    isPlaying = isPlaying,
                    progress = playbackProgress,
                    color = if (isMe) Color.White else BrandDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPlaying) "0:12" else (message.audioDuration ?: "0:24"),
                        fontSize = 11.sp,
                        color = timeColor
                    )

                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = timeColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        LoopMessageFooter(
            timestamp = message.timestamp,
            isMe = isMe,
            status = message.status,
            timeColor = timeColor
        )
    }
}

@Composable
fun LoopAudioWaveform(
    isPlaying: Boolean,
    progress: Float,
    color: Color,
    barCount: Int = 22,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val animatedHeight by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "height_anim"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val pattern = listOf(4, 10, 16, 12, 6, 14, 18, 8, 12, 16, 10, 6, 14, 8, 12, 4, 10, 16, 12, 6, 14, 8)

        for (i in 0 until barCount) {
            val baseHeight = (pattern.getOrElse(i) { 10 }).dp
            val dynamicHeight = if (isPlaying) {
                baseHeight * (0.6f + (if (i % 2 == 0) animatedHeight else (1f - animatedHeight)) * 0.4f)
            } else {
                baseHeight
            }

            val isPlayed = (i.toFloat() / barCount) <= progress
            val barColor = if (isPlayed) color else color.copy(alpha = 0.3f)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(dynamicHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun LoopLocationPinBubble(
    message: RentalMessage,
    isMe: Boolean,
    timeColor: Color
) {
    Column(modifier = Modifier.width(250.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(Color(0xFFE2E8F0))
                drawLine(Color(0xFFCBD5E1), Offset(0f, 30.dp.toPx()), Offset(size.width, 60.dp.toPx()), strokeWidth = 6.dp.toPx())
                drawLine(Color(0xFFCBD5E1), Offset(50.dp.toPx(), 0f), Offset(70.dp.toPx(), size.height), strokeWidth = 5.dp.toPx())
                drawCircle(Color(0xFF0F172A), radius = 10.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
                drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BrandDark.copy(alpha = 0.85f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Pickup Location Pin",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message.locationTitle ?: "Pickup Location",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isMe) Color.White else TextPrimary
        )

        if (!message.locationAddress.isNullOrBlank()) {
            Text(
                text = message.locationAddress,
                fontSize = 12.sp,
                color = if (isMe) Color.White.copy(alpha = 0.8f) else TextSecondary,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isMe) Color.White.copy(alpha = 0.15f) else Color(0xFFF1F5F9))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = null,
                tint = if (isMe) Color.White else BrandDark,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Directions in Maps",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isMe) Color.White else BrandDark
            )
        }

        LoopMessageFooter(
            timestamp = message.timestamp,
            isMe = isMe,
            status = message.status,
            timeColor = timeColor
        )
    }
}

@Composable
fun LoopRentalOfferCard(
    message: RentalMessage,
    onViewItem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandDark)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Rental Booking Summary",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (message.itemImageRes != null) {
                Image(
                    painter = painterResource(id = message.itemImageRes),
                    contentDescription = message.itemTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.itemTitle ?: "Rental Item",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "₹${message.itemPricePerDay ?: 700} / day · Puducherry",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandBlue
                )
                Text(
                    text = "10 Oct – 12 Oct (3 Days)",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceSecondary)
                .clickable(onClick = onViewItem)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "View Rental Details →",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDark
            )
        }
    }
}

@Composable
fun LoopMessageFooter(
    timestamp: String,
    isMe: Boolean,
    status: MessageStatus,
    timeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timestamp,
            fontSize = 10.sp,
            color = timeColor
        )

        if (isMe) {
            Spacer(modifier = Modifier.width(4.dp))
            when (status) {
                MessageStatus.SENDING -> {
                    Text(text = "🕒", fontSize = 9.sp)
                }
                MessageStatus.SENT -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sent",
                        tint = timeColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
                MessageStatus.DELIVERED, MessageStatus.READ -> {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Delivered",
                        tint = if (status == MessageStatus.READ) BrandBlue else timeColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LoopAttachmentSheet(
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            LoopAttachmentItem(
                icon = Icons.Default.LocationOn,
                label = "Location",
                onClick = { onOptionSelected("LOCATION") }
            )
            LoopAttachmentItem(
                icon = Icons.Default.CameraAlt,
                label = "Camera",
                onClick = { onOptionSelected("CAMERA") }
            )
            LoopAttachmentItem(
                icon = Icons.Outlined.Image,
                label = "Gallery",
                onClick = { onOptionSelected("GALLERY") }
            )
            LoopAttachmentItem(
                icon = Icons.Default.Audiotrack,
                label = "Voice Note",
                onClick = { onOptionSelected("AUDIO") }
            )
            LoopAttachmentItem(
                icon = Icons.Default.InsertDriveFile,
                label = "Agreement",
                onClick = { onOptionSelected("DOCUMENT") }
            )
        }
    }
}

@Composable
private fun LoopAttachmentItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(SurfaceSecondary)
                .border(1.dp, BorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = BrandDark,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}
