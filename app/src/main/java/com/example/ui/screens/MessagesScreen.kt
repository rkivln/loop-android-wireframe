package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocationHelper
import com.example.data.RentalRepository
import com.example.data.models.ChatMessageType
import com.example.data.models.DisappearingMessageTimer
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.ui.components.MemberProfileModalSheet
import com.example.ui.components.MessagesSkeletonList
import com.example.ui.components.PhotoBitmapProcessor
import com.example.ui.components.UserAvatar
import com.example.ui.components.chat.ChatAttachmentSheet
import com.example.ui.components.chat.ChatLockStatusBadge
import com.example.ui.components.chat.ChatMessageBubble
import com.example.ui.components.chat.ChatPendingPhotoStrip
import com.example.ui.components.chat.ChatPhotoZoomModal
import com.example.ui.components.chat.ChatPickupPinDialog
import com.example.ui.components.chat.ChatReplyBanner
import com.example.ui.components.chat.DisappearingTimerSheet
import com.example.ui.components.chat.SafetyNumberVerificationModal
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.AccentYellowDark
import com.example.ui.theme.AccentYellowSoft
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class DispatchFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    UNREAD("Unread")
}

private data class PickupPinDraft(
    val title: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?
)

@Composable
fun MessagesScreen(
    initialConversationId: String? = null,
    onConversationSelected: (String?) -> Unit = {},
    onOpenItem: (RentalItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val conversations by RentalRepository.conversationsFlow.collectAsStateWithLifecycle()
    val messagesMap by RentalRepository.activeConversationMessagesFlow.collectAsStateWithLifecycle()
    val allItems by RentalRepository.itemsFlow.collectAsStateWithLifecycle()
    val currentProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val isConversationsLoading by RentalRepository.isConversationsLoadingFlow.collectAsStateWithLifecycle()
    val peerEncryptionStates by RentalRepository.peerEncryptionStatesFlow.collectAsStateWithLifecycle()
    val ownFingerprint by RentalRepository.ownE2eeFingerprintFlow.collectAsStateWithLifecycle()

    var selectedConversationId by remember(initialConversationId) {
        mutableStateOf(initialConversationId)
    }
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(DispatchFilter.ALL) }

    LaunchedEffect(selectedConversationId) {
        val convId = selectedConversationId
        if (!convId.isNullOrBlank()) {
            RentalRepository.observeConversationMessages(convId)
        }
    }

    DisposableEffect(selectedConversationId) {
        RentalRepository.setActiveOpenConversationId(selectedConversationId)
        onDispose {
            RentalRepository.setActiveOpenConversationId(null)
        }
    }

    val activeConversation = conversations.find { it.id == selectedConversationId }

    if (selectedConversationId != null && activeConversation != null) {
        val convId = activeConversation.id
        BackHandler {
            RentalRepository.setTypingStatus(convId, false)
            selectedConversationId = null
            onConversationSelected(null)
        }

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        val messages = messagesMap[convId] ?: activeConversation.messages
        val peerEncState = peerEncryptionStates[activeConversation.owner.id]
        val isMuted = activeConversation.isMutedFor(currentProfile.uid)
        val timerOption = DisappearingMessageTimer.fromSecondsOrDefault(activeConversation.disappearingTimerSeconds)

        var textInput by remember(convId) { mutableStateOf("") }
        var lastTypingSent by remember(convId) { mutableStateOf(false) }
        var showOptionsMenu by remember { mutableStateOf(false) }
        var showBlockConfirmDialog by remember { mutableStateOf(false) }
        var showMemberProfile by remember { mutableStateOf(false) }
        var showSafetyNumberModal by remember { mutableStateOf(false) }
        var showDisappearingTimerSheet by remember { mutableStateOf(false) }
        var showAttachmentSheet by remember { mutableStateOf(false) }
        var replyingToMessage by remember(convId) { mutableStateOf<RentalMessage?>(null) }
        var pendingPhotoBitmap by remember(convId) { mutableStateOf<Bitmap?>(null) }
        var zoomPhotoUrl by remember { mutableStateOf<String?>(null) }
        var pendingPinDraft by remember { mutableStateOf<PickupPinDraft?>(null) }
        var isSendingPhoto by remember(convId) { mutableStateOf(false) }

        DisposableEffect(convId) {
            onDispose {
                RentalRepository.setTypingStatus(convId, false)
            }
        }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    val bmp = withContext(Dispatchers.IO) {
                        PhotoBitmapProcessor.loadOrientedSourceBitmap(context, uri, maxEdge = 960)
                    }
                    if (bmp != null) {
                        pendingPhotoBitmap = bmp
                    } else {
                        Toast.makeText(context, "Unable to read selected image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val openListingFallbackPin: () -> Unit = {
            val ctxItem = activeConversation.itemContext
            pendingPinDraft = PickupPinDraft(
                title = ctxItem?.title?.let { "$it Pickup" } ?: "Gear Pickup Spot",
                address = ctxItem?.location?.ifBlank { currentProfile.location } ?: currentProfile.location,
                latitude = ctxItem?.latitude,
                longitude = ctxItem?.longitude
            )
        }

        val fetchGpsAndOpenPin: () -> Unit = {
            LocationHelper.fetchCurrentLocation(
                context = context,
                onLocationReceived = { lat, lng ->
                    scope.launch {
                        val area = LocationHelper.reverseGeocodeLocation(context, lat, lng)
                        pendingPinDraft = PickupPinDraft(
                            title = "Current Meetup Point",
                            address = area ?: activeConversation.itemContext?.location.orEmpty(),
                            latitude = lat,
                            longitude = lng
                        )
                    }
                },
                onLocationUnavailable = {
                    Toast.makeText(
                        context,
                        "GPS unavailable; enter pickup landmark manually",
                        Toast.LENGTH_SHORT
                    ).show()
                    openListingFallbackPin()
                }
            )
        }

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                fetchGpsAndOpenPin()
            } else {
                Toast.makeText(
                    context,
                    "Location permission denied; enter pickup details manually",
                    Toast.LENGTH_SHORT
                ).show()
                openListingFallbackPin()
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PaperIvory)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            RentalRepository.setTypingStatus(convId, false)
                            selectedConversationId = null
                            onConversationSelected(null)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, CircleShape)
                            .testTag("chat_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to conversations",
                            tint = InkCharcoal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { showMemberProfile = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("chat_header_profile_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserAvatar(
                                avatarUrl = activeConversation.owner.avatarUrl,
                                initials = activeConversation.owner.initials,
                                size = 34.dp,
                                avatarPresetIndex = activeConversation.owner.avatarPresetIndex,
                                fallbackContainerColor = InkCharcoal,
                                borderColor = PaperBorderSubtle,
                                borderWidth = 1.dp,
                                contentDescription = "${activeConversation.owner.name} avatar"
                            )
                            Column {
                                Text(
                                    text = activeConversation.owner.name,
                                    style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                                    color = InkCharcoal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "View profile ›",
                                    style = LoopType.Metadata.copy(fontSize = 10.sp, color = AccentForestGreen),
                                    maxLines = 1
                                )
                            }
                        }

                        ChatLockStatusBadge(
                            encryptionState = peerEncState,
                            e2eeEnabled = activeConversation.e2eeEnabled,
                            onClick = { showSafetyNumberModal = true }
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showOptionsMenu = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("chat_options_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Conversation options",
                                tint = InkCharcoal
                            )
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier.background(PaperPureWhite)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Verify encryption & Safety Number",
                                        style = LoopType.BodySmall,
                                        color = InkCharcoal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = AccentForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    showSafetyNumberModal = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isMuted) "Unmute notifications" else "Mute notifications",
                                        style = LoopType.BodySmall,
                                        color = InkCharcoal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isMuted) {
                                            Icons.Default.NotificationsActive
                                        } else {
                                            Icons.Default.NotificationsOff
                                        },
                                        contentDescription = null,
                                        tint = InkCharcoal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    RentalRepository.toggleConversationMute(convId)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Disappearing messages · ${timerOption.shortLabel}",
                                        style = LoopType.BodySmall,
                                        color = InkCharcoal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = AccentForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    showDisappearingTimerSheet = true
                                }
                            )
                            HorizontalDivider(color = PaperBorderSubtle)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Block ${activeConversation.owner.name}",
                                        style = LoopType.BodySmall,
                                        color = AlertCrimson
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = AlertCrimson,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    showBlockConfirmDialog = true
                                }
                            )
                        }
                    }
                }

                // Thread privacy indicators (Disappearing timer / Muted / Key Change warning)
                if (activeConversation.disappearingTimerSeconds > 0L || isMuted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (activeConversation.disappearingTimerSeconds > 0L) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(AccentMint)
                                    .clickable { showDisappearingTimerSheet = true }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("thread_disappearing_pill"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Disappearing timer",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Disappearing · ${timerOption.label}",
                                    style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                    color = AccentForestGreen
                                )
                            }
                        }
                        if (isMuted) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(PaperPureWhite)
                                    .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                                    .clickable { RentalRepository.toggleConversationMute(convId) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("thread_muted_pill"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = "Muted",
                                    tint = InkSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Muted",
                                    style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                    color = InkSecondary
                                )
                            }
                        }
                    }
                }

                if (peerEncState?.keyChangedWarning == true) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentYellowSoft)
                            .border(1.dp, AccentYellowDark.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .clickable { showSafetyNumberModal = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("key_changed_warning_banner"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Encryption key changed",
                            tint = AccentYellowDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${activeConversation.owner.name}'s encryption key changed. Tap to verify Safety Number.",
                            style = LoopType.Metadata.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                            color = InkCharcoal
                        )
                    }
                }

                activeConversation.itemContext?.let { ctx ->
                    val liveItem = allItems.find { it.id == ctx.id } ?: ctx
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                            .clickable { onOpenItem(liveItem) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("chat_item_context_card"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = liveItem.title,
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                color = InkCharcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val statusText = when {
                                !liveItem.availableToday && liveItem.activeBookingId != null -> "Currently Booked"
                                !liveItem.availableToday -> "Availability Paused"
                                else -> "Available · ₹${liveItem.pricePerDay}/day"
                            }
                            Text(
                                text = statusText,
                                style = LoopType.Metadata.copy(fontSize = 11.sp),
                                color = InkSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "View ›",
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = AccentForestGreen
                        )
                    }
                }

                if (activeConversation.itemContext == null && !activeConversation.communityPostTitle.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AccentMint.copy(alpha = 0.7f))
                            .border(1.dp, AccentForestGreen.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("chat_post_context_pill"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMMUNITY POST · ${activeConversation.communityPostTitle}",
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                            color = AccentForestGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Messages Feed
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AccentMint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "End-to-end encrypted",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "Messages are end-to-end encrypted",
                                style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                                color = InkCharcoal
                            )
                            Text(
                                text = "Say hello to ${activeConversation.owner.name} to coordinate pickup times, share encrypted photos, or send a meetup pin.",
                                style = LoopType.BodySmall,
                                color = InkSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageBubble(
                                msg = msg,
                                ownUid = currentProfile.uid,
                                onReply = { replyingToMessage = msg },
                                onCopy = {
                                    copyToClipboard(context, "Loop message", msg.text)
                                    Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    RentalRepository.deleteMessage(convId, msg)
                                },
                                onOpenPhoto = { url -> zoomPhotoUrl = url },
                                onOpenLocation = { lat, lng, label ->
                                    if (lat != null && lng != null) {
                                        LocationHelper.openNavigationIntent(context, lat, lng, label)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Pickup note: $label",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }

                if (activeConversation.isTyping) {
                    Text(
                        text = "${activeConversation.owner.name} is typing...",
                        style = LoopType.Metadata.copy(
                            fontSize = 11.5.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        color = AccentForestGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 4.dp)
                            .testTag("chat_typing_indicator")
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = PaperBorderSubtle)

                replyingToMessage?.let { replyTarget ->
                    ChatReplyBanner(
                        replyTo = replyTarget,
                        onClear = { replyingToMessage = null }
                    )
                }

                pendingPhotoBitmap?.let { bmp ->
                    ChatPendingPhotoStrip(
                        bitmap = bmp,
                        onRemove = { pendingPhotoBitmap = null }
                    )
                }

                // Composer Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PaperPureWhite)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAttachmentSheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PaperIvory)
                            .border(1.dp, PaperBorderSubtle, CircleShape)
                            .testTag("chat_attach_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach photo or pickup pin",
                            tint = InkCharcoal,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperIvory)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(100.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (textInput.isEmpty()) {
                            Text(
                                text = if (pendingPhotoBitmap != null) {
                                    "Add a caption (optional)..."
                                } else {
                                    "Message ${activeConversation.owner.name}..."
                                },
                                style = LoopType.BodyMedium,
                                color = InkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = textInput,
                            onValueChange = { updated ->
                                textInput = updated
                                val nowTyping = updated.isNotBlank()
                                if (nowTyping != lastTypingSent) {
                                    lastTypingSent = nowTyping
                                    RentalRepository.setTypingStatus(convId, nowTyping)
                                }
                            },
                            singleLine = true,
                            textStyle = LoopType.BodyMedium.copy(color = InkCharcoal),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_message_input")
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSend = (textInput.isNotBlank() || pendingPhotoBitmap != null) && !isSendingPhoto
                    IconButton(
                        onClick = {
                            val bmp = pendingPhotoBitmap
                            val trimmed = textInput.trim()
                            val replyTarget = replyingToMessage
                            if (bmp != null) {
                                isSendingPhoto = true
                                scope.launch {
                                    val dataUri = withContext(Dispatchers.IO) {
                                        PhotoBitmapProcessor.bitmapToBase64DataUri(bmp, targetMaxBytes = 120_000)
                                    }
                                    RentalRepository.sendMessage(
                                        conversationId = convId,
                                        text = trimmed.ifBlank { "Photo" },
                                        messageType = ChatMessageType.IMAGE_MEDIA,
                                        imageUrl = dataUri,
                                        replyToMessage = replyTarget
                                    )
                                    pendingPhotoBitmap = null
                                    textInput = ""
                                    replyingToMessage = null
                                    if (lastTypingSent) {
                                        lastTypingSent = false
                                        RentalRepository.setTypingStatus(convId, false)
                                    }
                                    isSendingPhoto = false
                                }
                            } else if (trimmed.isNotEmpty()) {
                                RentalRepository.sendMessage(
                                    conversationId = convId,
                                    text = trimmed,
                                    messageType = ChatMessageType.TEXT,
                                    replyToMessage = replyTarget
                                )
                                textInput = ""
                                replyingToMessage = null
                                if (lastTypingSent) {
                                    lastTypingSent = false
                                    RentalRepository.setTypingStatus(convId, false)
                                }
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (canSend) InkCharcoal else PaperBorder)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send encrypted message",
                            tint = if (canSend) InkWhite else InkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (showBlockConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showBlockConfirmDialog = false },
                    containerColor = PaperPureWhite,
                    titleContentColor = InkCharcoal,
                    textContentColor = InkSecondary,
                    title = {
                        Text(
                            text = "Block ${activeConversation.owner.name}?",
                            style = LoopType.HeadlineMedium.copy(fontSize = 18.sp)
                        )
                    },
                    text = {
                        Text(
                            text = "This conversation and all of their pieces and community posts will be hidden. You can unblock them in Settings.",
                            style = LoopType.BodyMedium
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showBlockConfirmDialog = false
                                RentalRepository.blockUser(
                                    context,
                                    activeConversation.owner.id,
                                    activeConversation.owner.name
                                )
                                selectedConversationId = null
                                onConversationSelected(null)
                            },
                            modifier = Modifier.testTag("chat_confirm_block_btn")
                        ) {
                            Text("Block User", color = AlertCrimson, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBlockConfirmDialog = false }) {
                            Text("Cancel", color = InkCharcoal)
                        }
                    }
                )
            }

            if (showMemberProfile) {
                MemberProfileModalSheet(
                    owner = activeConversation.owner,
                    fallbackLocation = activeConversation.itemContext?.location.orEmpty(),
                    showMessageButton = false,
                    hasActiveRelationship = true,
                    onDismiss = { showMemberProfile = false }
                )
            }

            if (showSafetyNumberModal) {
                SafetyNumberVerificationModal(
                    peerName = activeConversation.owner.name,
                    encryptionState = peerEncState,
                    ownFingerprint = ownFingerprint,
                    onVerifyToggle = { verified ->
                        val peerFp = peerEncState?.peerFingerprint.orEmpty()
                        if (peerFp.isNotBlank()) {
                            RentalRepository.verifyPeerSafetyNumber(
                                peerUid = activeConversation.owner.id,
                                peerFingerprint = peerFp,
                                verified = verified
                            )
                        }
                    },
                    onCopySafetyNumber = { number ->
                        copyToClipboard(context, "Loop Safety Number", number)
                        Toast.makeText(context, "Safety Number copied", Toast.LENGTH_SHORT).show()
                    },
                    onDismiss = { showSafetyNumberModal = false }
                )
            }

            if (showDisappearingTimerSheet) {
                DisappearingTimerSheet(
                    selectedSeconds = activeConversation.disappearingTimerSeconds,
                    onSelectTimer = { timer ->
                        RentalRepository.setConversationDisappearingTimer(convId, timer.seconds)
                    },
                    onDismiss = { showDisappearingTimerSheet = false }
                )
            }

            if (showAttachmentSheet) {
                ChatAttachmentSheet(
                    hasListingLocation = activeConversation.itemContext != null,
                    listingLocationLabel = activeConversation.itemContext?.location.orEmpty(),
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onShareCurrentGpsPin = {
                        if (LocationHelper.hasLocationPermission(context)) {
                            fetchGpsAndOpenPin()
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    onShareListingPin = openListingFallbackPin,
                    onDismiss = { showAttachmentSheet = false }
                )
            }

            pendingPinDraft?.let { draft ->
                ChatPickupPinDialog(
                    initialTitle = draft.title,
                    initialAddress = draft.address,
                    latitude = draft.latitude,
                    longitude = draft.longitude,
                    onConfirm = { title, address, lat, lng ->
                        RentalRepository.sendMessage(
                            conversationId = convId,
                            text = if (address.isNotBlank()) "$title — $address" else title,
                            messageType = ChatMessageType.LOCATION_PIN,
                            locationTitle = title,
                            locationAddress = address,
                            latitude = lat,
                            longitude = lng,
                            replyToMessage = replyingToMessage
                        )
                        pendingPinDraft = null
                        replyingToMessage = null
                    },
                    onDismiss = { pendingPinDraft = null }
                )
            }

            zoomPhotoUrl?.let { photoUrl ->
                ChatPhotoZoomModal(
                    imageUrl = photoUrl,
                    onDismiss = { zoomPhotoUrl = null }
                )
            }
        }
    } else {
        val filteredConversations = conversations.filter { conv ->
            val matchesQuery = searchQuery.isBlank() ||
                conv.owner.name.contains(searchQuery, ignoreCase = true) ||
                (conv.itemContext?.title?.contains(searchQuery, ignoreCase = true) == true) ||
                (conv.communityPostTitle?.contains(searchQuery, ignoreCase = true) == true) ||
                (conv.lastMessage?.text?.contains(searchQuery, ignoreCase = true) == true)

            val hasUnread = conv.unreadCount > 0 && conv.lastSenderId != currentProfile.uid
            val matchesFilter = when (activeFilter) {
                DispatchFilter.ALL -> true
                DispatchFilter.ACTIVE -> conv.lastMessage != null
                DispatchFilter.UNREAD -> hasUnread
            }
            matchesQuery && matchesFilter
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(PaperIvory)
                .statusBarsPadding()
        ) {
            // Editorial Header: "Studio Dispatch" + subtitle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Studio Dispatch",
                    style = LoopType.HeroDisplayLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Normal),
                    color = InkCharcoal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "End-to-end encrypted",
                        tint = AccentForestGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "End-to-end encrypted dialogue with verified hosts",
                        style = LoopType.BodySmall,
                        color = InkSecondary
                    )
                }
            }

            // White Pill Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search conversations",
                    tint = InkMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search makers and equipment...",
                            style = LoopType.BodyMedium,
                            color = InkMuted,
                            maxLines = 1
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = LoopType.BodyMedium.copy(color = InkCharcoal),
                        cursorBrush = SolidColor(InkCharcoal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dispatch_search_input")
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = InkMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips: All, Active, Unread
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DispatchFilter.entries.forEach { filter ->
                    val isSelected = activeFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) InkCharcoal else PaperPureWhite)
                            .border(
                                1.dp,
                                if (isSelected) InkCharcoal else PaperBorder,
                                RoundedCornerShape(100.dp)
                            )
                            .clickable { activeFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = filter.label,
                            style = LoopType.Metadata.copy(
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) InkWhite else InkCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isConversationsLoading && conversations.isEmpty()) {
                MessagesSkeletonList(
                    count = 4,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 22.dp, vertical = 4.dp)
                )
            } else if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp)
                        .testTag("empty_messages_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = InkMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No conversations yet",
                            style = LoopType.HeadlineMedium,
                            color = InkCharcoal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Open any camera, studio desk, or e-bike and tap Message to coordinate with the host.",
                            style = LoopType.BodySmall,
                            color = InkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        val isConvMuted = conv.isMutedFor(currentProfile.uid)
                        val peerState = peerEncryptionStates[conv.owner.id]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                                .clickable {
                                    selectedConversationId = conv.id
                                    onConversationSelected(conv.id)
                                }
                                .padding(16.dp)
                                .testTag("conversation_card_${conv.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                avatarUrl = conv.owner.avatarUrl,
                                initials = conv.owner.initials,
                                size = 48.dp,
                                avatarPresetIndex = conv.owner.avatarPresetIndex,
                                fallbackContainerColor = InkCharcoal,
                                borderColor = PaperBorderSubtle,
                                borderWidth = 1.dp,
                                contentDescription = "${conv.owner.name} avatar"
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = conv.owner.name,
                                        style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                                        color = InkCharcoal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (conv.disappearingTimerSeconds > 0L) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = "Disappearing messages enabled",
                                            tint = AccentForestGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    if (isConvMuted) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsOff,
                                            contentDescription = "Muted conversation",
                                            tint = InkMuted,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = conv.lastMessage?.timestamp ?: "New",
                                        style = LoopType.Metadata.copy(fontSize = 11.sp),
                                        color = InkMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (conv.e2eeEnabled) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "End-to-end encrypted",
                                            tint = if (peerState?.isVerified == true) AccentForestGreen else InkMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                    }
                                    val previewText = when {
                                        conv.isTyping -> "${conv.owner.name} is typing..."
                                        conv.lastMessage != null -> conv.lastMessage!!.text
                                        !conv.itemContext?.title.isNullOrBlank() -> conv.itemContext!!.title
                                        !conv.communityPostTitle.isNullOrBlank() -> conv.communityPostTitle!!
                                        else -> "Tap to start chatting"
                                    }
                                    Text(
                                        text = previewText,
                                        style = if (conv.isTyping) {
                                            LoopType.BodySmall.copy(fontStyle = FontStyle.Italic)
                                        } else {
                                            LoopType.BodySmall
                                        },
                                        color = if (conv.isTyping) AccentForestGreen else InkSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val hasUnread = conv.unreadCount > 0 && conv.lastSenderId != currentProfile.uid
                                    if (hasUnread) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(AccentWarmYellow)
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

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}
