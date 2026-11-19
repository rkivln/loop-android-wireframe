package com.example.ui.components.chat

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.ChatMessageType
import com.example.data.models.MessageStatus
import com.example.data.models.RentalMessage
import com.example.ui.components.ListingPhoto
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentYellowDark
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite
import java.util.Locale

@Composable
fun ChatAttachmentSheet(
    hasListingLocation: Boolean,
    listingLocationLabel: String,
    onPickPhoto: () -> Unit,
    onShareCurrentGpsPin: () -> Unit,
    onShareListingPin: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("chat_attachment_sheet"),
            shape = RoundedCornerShape(26.dp),
            color = PaperPureWhite,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Encrypted Attachment",
                        style = LoopType.HeadlineMedium.copy(fontSize = 18.sp),
                        color = InkCharcoal
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close attachment sheet",
                            tint = InkCharcoal
                        )
                    }
                }

                AttachmentOptionRow(
                    icon = Icons.Default.PhotoLibrary,
                    title = "Share Encrypted Photo",
                    subtitle = "Select via Android Photo Picker; encrypted inside payload",
                    onClick = {
                        onDismiss()
                        onPickPhoto()
                    },
                    testTag = "attach_photo_option"
                )

                AttachmentOptionRow(
                    icon = Icons.Default.MyLocation,
                    title = "Share Current Pickup Pin",
                    subtitle = "Use foreground GPS coordinates to coordinate handover",
                    onClick = {
                        onDismiss()
                        onShareCurrentGpsPin()
                    },
                    testTag = "attach_gps_pin_option"
                )

                if (hasListingLocation) {
                    AttachmentOptionRow(
                        icon = Icons.Default.LocationOn,
                        title = "Share Listing Pickup Spot",
                        subtitle = listingLocationLabel.ifBlank { "Use listing neighborhood coordinates" },
                        onClick = {
                            onDismiss()
                            onShareListingPin()
                        },
                        testTag = "attach_listing_pin_option"
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PaperIvory)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AccentMint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentForestGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                color = InkCharcoal
            )
            Text(
                text = subtitle,
                style = LoopType.BodySmall,
                color = InkSecondary
            )
        }
    }
}

@Composable
fun ChatPickupPinDialog(
    initialTitle: String,
    initialAddress: String,
    latitude: Double?,
    longitude: Double?,
    onConfirm: (title: String, address: String, lat: Double?, lng: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var pinTitle by remember(initialTitle) { mutableStateOf(initialTitle.ifBlank { "Gear Pickup Spot" }) }
    var pinAddress by remember(initialAddress) { mutableStateOf(initialAddress) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("chat_pickup_pin_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = PaperPureWhite,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AccentForestGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Confirm Pickup Pin",
                            style = LoopType.HeadlineMedium.copy(fontSize = 18.sp),
                            color = InkCharcoal
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel pickup pin",
                            tint = InkCharcoal
                        )
                    }
                }

                if (latitude != null && longitude != null) {
                    Text(
                        text = String.format(
                            Locale.US,
                            "Coordinates: %.5f, %.5f (encrypted in message payload)",
                            latitude,
                            longitude
                        ),
                        style = LoopType.Metadata.copy(fontSize = 11.sp),
                        color = AccentForestGreen
                    )
                }

                OutlinedTextField(
                    value = pinTitle,
                    onValueChange = { pinTitle = it },
                    label = { Text("Landmark or Meetup Point") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentForestGreen,
                        unfocusedBorderColor = PaperBorder,
                        focusedTextColor = InkCharcoal,
                        unfocusedTextColor = InkCharcoal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pickup_pin_title_input")
                )

                OutlinedTextField(
                    value = pinAddress,
                    onValueChange = { pinAddress = it },
                    label = { Text("Neighborhood / Notes") },
                    maxLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentForestGreen,
                        unfocusedBorderColor = PaperBorder,
                        focusedTextColor = InkCharcoal,
                        unfocusedTextColor = InkCharcoal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pickup_pin_address_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Cancel", color = InkSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                pinTitle.trim().ifBlank { "Gear Pickup Spot" },
                                pinAddress.trim(),
                                latitude,
                                longitude
                            )
                        },
                        enabled = pinTitle.isNotBlank() || pinAddress.isNotBlank() || (latitude != null && longitude != null),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InkCharcoal,
                            contentColor = InkWhite
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("confirm_send_pin_btn")
                    ) {
                        Text(
                            text = "Send Encrypted Pin",
                            style = LoopType.Metadata.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatReplyBanner(
    replyTo: RentalMessage,
    onClear: () -> Unit
) {
    val snippet = when {
        replyTo.messageType == ChatMessageType.IMAGE_MEDIA ->
            replyTo.text.takeIf { it.isNotBlank() && it != "Photo" }?.let { "Photo: $it" } ?: "Photo attachment"
        replyTo.messageType == ChatMessageType.LOCATION_PIN ->
            "Pickup Pin: ${replyTo.locationTitle ?: replyTo.text}"
        else -> replyTo.text
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperPureWhite)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PaperIvory)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(12.dp))
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
            .testTag("chat_reply_banner"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AccentForestGreen)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Replying to ${if (replyTo.isFromMe) "You" else replyTo.senderName}",
                style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = AccentForestGreen,
                maxLines = 1
            )
            Text(
                text = snippet,
                style = LoopType.BodySmall,
                color = InkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = onClear,
            modifier = Modifier
                .size(48.dp)
                .testTag("clear_reply_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel reply",
                tint = InkMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ChatPendingPhotoStrip(
    bitmap: Bitmap,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperPureWhite)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PaperIvory)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
            .padding(8.dp)
            .testTag("chat_pending_photo_strip"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Selected photo preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Photo ready to encrypt & send",
                style = LoopType.TitleMedium.copy(fontSize = 13.5.sp),
                color = InkCharcoal
            )
            Text(
                text = "Add an optional caption below or tap Send",
                style = LoopType.Metadata.copy(fontSize = 11.sp),
                color = InkSecondary
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(48.dp)
                .testTag("remove_pending_photo_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove selected photo",
                tint = AlertCrimson,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(
    msg: RentalMessage,
    ownUid: String,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onOpenLocation: (Double?, Double?, String) -> Unit
) {
    val isMe = msg.isFromMe
    val isSenderOwner = ownUid.isNotBlank() && msg.senderId == ownUid
    var showMenu by remember { mutableStateOf(false) }

    val bubbleBg = when {
        msg.decryptionFailed -> AccentPeach
        isMe -> InkCharcoal
        else -> PaperPureWhite
    }
    val bubbleBorder = when {
        msg.decryptionFailed -> AlertCrimson.copy(alpha = 0.3f)
        isMe -> InkCharcoal
        else -> PaperBorderSubtle
    }
    val primaryTextColor = when {
        msg.decryptionFailed -> InkCharcoal
        isMe -> InkWhite
        else -> InkCharcoal
    }
    val secondaryTextColor = when {
        msg.decryptionFailed -> AlertCrimson
        isMe -> InkWhite.copy(alpha = 0.72f)
        else -> InkMuted
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box {
            Column(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(bubbleBg)
                    .border(1.dp, bubbleBorder, RoundedCornerShape(18.dp))
                    .combinedClickable(
                        onClick = {
                            if (msg.messageType == ChatMessageType.IMAGE_MEDIA && !msg.imageUrl.isNullOrBlank()) {
                                onOpenPhoto(msg.imageUrl)
                            }
                        },
                        onLongClick = { showMenu = true }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("chat_bubble_${msg.id}")
            ) {
                if (!msg.replyToSenderName.isNullOrBlank() || !msg.replyToSnippet.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isMe) Color.White.copy(alpha = 0.12f) else PaperIvory
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isMe) AccentMint else AccentForestGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = msg.replyToSenderName.orEmpty().ifBlank { "Reply" },
                                style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                                color = if (isMe) AccentMint else AccentForestGreen,
                                maxLines = 1
                            )
                            if (!msg.replyToSnippet.isNullOrBlank()) {
                                Text(
                                    text = msg.replyToSnippet,
                                    style = LoopType.BodySmall.copy(fontSize = 11.5.sp),
                                    color = primaryTextColor.copy(alpha = 0.85f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (msg.decryptionFailed) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Decryption failed",
                            tint = AlertCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = msg.text,
                            style = LoopType.BodyMedium,
                            color = primaryTextColor
                        )
                    }
                } else {
                    when (msg.messageType) {
                        ChatMessageType.IMAGE_MEDIA -> {
                            if (!msg.imageUrl.isNullOrBlank()) {
                                ListingPhoto(
                                    imageUrl = msg.imageUrl,
                                    fallbackRes = 0,
                                    contentDescription = msg.text.ifBlank { "Shared encrypted photo" },
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(176.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onOpenPhoto(msg.imageUrl) }
                                )
                            }
                            if (msg.text.isNotBlank() && msg.text != "Photo") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = msg.text,
                                    style = LoopType.BodyMedium,
                                    color = primaryTextColor
                                )
                            }
                        }

                        ChatMessageType.LOCATION_PIN -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isMe) Color.White.copy(alpha = 0.12f) else AccentMint.copy(alpha = 0.45f)
                                    )
                                    .clickable {
                                        onOpenLocation(
                                            msg.latitude,
                                            msg.longitude,
                                            msg.locationTitle ?: msg.locationAddress ?: "Pickup Spot"
                                        )
                                    }
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Pickup location pin",
                                        tint = if (isMe) AccentMint else AccentForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = msg.locationTitle ?: "Pickup Location Pin",
                                        style = LoopType.TitleMedium.copy(fontSize = 14.sp),
                                        color = primaryTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                val addrLine = msg.locationAddress?.takeIf { it.isNotBlank() }
                                    ?: if (msg.latitude != null && msg.longitude != null) {
                                        String.format(Locale.US, "%.4f, %.4f", msg.latitude, msg.longitude)
                                    } else {
                                        msg.text
                                    }
                                if (addrLine.isNotBlank()) {
                                    Text(
                                        text = addrLine,
                                        style = LoopType.BodySmall,
                                        color = primaryTextColor.copy(alpha = 0.85f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Directions,
                                        contentDescription = null,
                                        tint = if (isMe) AccentMint else AccentForestGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Open in Maps",
                                        style = LoopType.Metadata.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isMe) AccentMint else AccentForestGreen
                                    )
                                }
                            }
                        }

                        else -> {
                            Text(
                                text = msg.text,
                                style = LoopType.BodyMedium,
                                color = primaryTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (msg.isEncrypted && !msg.decryptionFailed) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "End-to-end encrypted",
                            tint = secondaryTextColor,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                    if (msg.expiresAtEpoch > 0L) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Disappearing message",
                            tint = secondaryTextColor,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                    Text(
                        text = if (isMe) "You · ${msg.timestamp}" else "${msg.senderName} · ${msg.timestamp}",
                        style = LoopType.Metadata.copy(fontSize = 10.sp),
                        color = secondaryTextColor
                    )
                    if (isMe) {
                        Icon(
                            imageVector = if (msg.status == MessageStatus.READ) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = msg.status.name,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(PaperPureWhite)
            ) {
                DropdownMenuItem(
                    text = { Text("Reply", style = LoopType.BodySmall, color = InkCharcoal) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null,
                            tint = InkCharcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        showMenu = false
                        onReply()
                    }
                )
                if (!msg.decryptionFailed && msg.text.isNotBlank()) {
                    DropdownMenuItem(
                        text = { Text("Copy text", style = LoopType.BodySmall, color = InkCharcoal) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = InkCharcoal,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onCopy()
                        }
                    )
                }
                if (isSenderOwner) {
                    HorizontalDivider(color = PaperBorderSubtle)
                    DropdownMenuItem(
                        text = { Text("Delete for everyone", style = LoopType.BodySmall, color = AlertCrimson) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = AlertCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatPhotoZoomModal(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .clickable(onClick = onDismiss)
                .testTag("chat_photo_zoom_modal")
        ) {
            ListingPhoto(
                imageUrl = imageUrl,
                fallbackRes = 0,
                contentDescription = "Full screen encrypted photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close photo viewer",
                    tint = Color.White
                )
            }
        }
    }
}
