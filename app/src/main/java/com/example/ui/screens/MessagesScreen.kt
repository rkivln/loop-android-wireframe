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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RentalRepository
import com.example.data.models.ChatConversation
import com.example.data.models.ChatMessageType
import com.example.data.models.MessageStatus
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.data.models.RentalOwner
import com.example.ui.components.LoopAttachmentSheet
import com.example.ui.components.LoopChatBackground
import com.example.ui.components.LoopChatDateDivider
import com.example.ui.components.LoopMessageBubble
import com.example.ui.components.LoopRentalContextBanner
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandStarAmber
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessagesScreen(
    conversations: List<ChatConversation> = RentalRepository.initialConversations,
    selectedConversationId: String? = null,
    onViewItemDetails: ((RentalItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var conversationList by remember { mutableStateOf(conversations) }
    var activeConversationId by remember { mutableStateOf(selectedConversationId) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("All") }

    val activeConversation = conversationList.find { it.id == activeConversationId }

    if (activeConversation != null) {
        LoopConversationScreen(
            conversation = activeConversation,
            onBack = { activeConversationId = null },
            onSendMessage = { newText, messageType, locTitle, locAddr ->
                RentalRepository.sendMessage(
                    conversationId = activeConversation.id,
                    text = newText,
                    messageType = messageType,
                    locationTitle = locTitle,
                    locationAddress = locAddr
                )
                val newMsg = RentalMessage(
                    id = "msg_${System.currentTimeMillis()}",
                    senderName = "Me",
                    text = newText,
                    timestamp = "Just now",
                    isFromMe = true,
                    messageType = messageType,
                    status = MessageStatus.SENT,
                    locationTitle = locTitle,
                    locationAddress = locAddr
                )
                conversationList = conversationList.map { conv ->
                    if (conv.id == activeConversation.id) {
                        conv.copy(
                            messages = conv.messages + newMsg,
                            unreadCount = 0
                        )
                    } else conv
                }
            },
            onViewItem = { itemTitle ->
                val matchingItem = RentalRepository.items.find { it.title == itemTitle }
                    ?: activeConversation.itemContext
                if (matchingItem != null) {
                    onViewItemDetails?.invoke(matchingItem)
                }
            },
            modifier = modifier
        )
    } else {
        LoopInboxScreen(
            conversations = conversationList,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedFilterTab = selectedFilterTab,
            onSelectFilterTab = { selectedFilterTab = it },
            onSelectConversation = { convId ->
                conversationList = conversationList.map { conv ->
                    if (conv.id == convId) conv.copy(unreadCount = 0) else conv
                }
                activeConversationId = convId
            },
            modifier = modifier
        )
    }
}

@Composable
fun LoopInboxScreen(
    conversations: List<ChatConversation>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilterTab: String,
    onSelectFilterTab: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewChatDialog by remember { mutableStateOf(false) }

    val filteredList = remember(conversations, searchQuery, selectedFilterTab) {
        conversations.filter { conv ->
            val matchesFilter = when (selectedFilterTab) {
                "Active Rentals" -> conv.itemContext != null
                "Hosts" -> conv.owner.badge.isNotBlank()
                "Unread" -> conv.unreadCount > 0
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    conv.owner.name.contains(searchQuery, ignoreCase = true) ||
                    (conv.itemContext?.title?.contains(searchQuery, ignoreCase = true) == true) ||
                    (conv.lastMessage?.text?.contains(searchQuery, ignoreCase = true) == true)
            matchesFilter && matchesSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Messages",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Connect with equipment hosts & renters",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // New inquiry action icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, CircleShape)
                        .clickable { showNewChatDialog = true }
                        .testTag("inbox_new_chat_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Message",
                        tint = BrandDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = TextPrimary
                        ),
                        cursorBrush = SolidColor(BrandDark),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search conversations, hosts, gear...",
                                    fontSize = 13.5.sp,
                                    color = TextMuted
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inbox_search_input")
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onSearchQueryChange("") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Active Rentals", "Hosts", "Unread").forEach { tab ->
                    val isSelected = selectedFilterTab == tab
                    val chipBg = if (isSelected) BrandDark else SurfaceCard
                    val chipTextColor = if (isSelected) Color.White else TextSecondary

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(chipBg)
                            .border(1.dp, if (isSelected) BrandDark else BorderSubtle, RoundedCornerShape(16.dp))
                            .clickable { onSelectFilterTab(tab) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = chipTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Conversation Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 76.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { conv ->
                    LoopConversationCard(
                        conversation = conv,
                        onClick = { onSelectConversation(conv.id) }
                    )
                }
            }
        }

        if (showNewChatDialog) {
            AlertDialog(
                onDismissRequest = { showNewChatDialog = false },
                title = { Text("Start a Conversation") },
                text = {
                    Column {
                        Text(
                            text = "Select a verified equipment owner nearby in Puducherry:",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        conversations.forEach { conv ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        showNewChatDialog = false
                                        onSelectConversation(conv.id)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(BrandDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = conv.owner.initials,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = conv.owner.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = conv.itemContext?.title ?: "Equipment Owner",
                                        fontSize = 11.5.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNewChatDialog = false }) {
                        Text("Cancel", color = BrandDark)
                    }
                }
            )
        }
    }
}

@Composable
fun LoopConversationCard(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    val lastMsg = conversation.lastMessage

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("loop_chat_card_${conversation.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online status
            Box(modifier = Modifier.size(48.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.owner.initials,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (conversation.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(13.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(BrandGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = conversation.owner.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (conversation.itemContext != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = conversation.itemContext.title.take(16),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandBlue
                                )
                            }
                        }
                    }

                    Text(
                        text = lastMsg?.timestamp ?: "10:30 AM",
                        fontSize = 11.5.sp,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (conversation.unreadCount > 0) BrandBlue else TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (lastMsg?.isFromMe == true) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = BrandBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        val snippet = when (lastMsg?.messageType) {
                            ChatMessageType.AUDIO_NOTE -> "🎤 Voice note (${lastMsg.audioDuration ?: "0:24"})"
                            ChatMessageType.LOCATION_PIN -> "📍 Pickup: ${lastMsg.locationTitle ?: "Location pin"}"
                            ChatMessageType.RENTAL_OFFER -> "🛍️ Booking Request"
                            ChatMessageType.IMAGE_MEDIA -> "📷 Photo"
                            else -> lastMsg?.text ?: "Start conversation"
                        }

                        Text(
                            text = snippet,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (conversation.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        if (conversation.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(BrandDark)
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${conversation.unreadCount}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoopConversationScreen(
    conversation: ChatConversation,
    onBack: () -> Unit,
    onSendMessage: (String, ChatMessageType, String?, String?) -> Unit,
    onViewItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var inputMessage by remember { mutableStateOf("") }
    var isTypingSimulation by remember { mutableStateOf(false) }
    var isAudioRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showCallDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.size - 1)
        }
    }

    LaunchedEffect(isAudioRecording) {
        if (isAudioRecording) {
            recordingSeconds = 0
            while (isAudioRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Clean Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(0.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onBack)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BrandDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.owner.initials,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = conversation.owner.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = BrandBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = if (isTypingSimulation) "typing..." else "Online · Puducherry",
                                fontSize = 11.sp,
                                color = if (isTypingSimulation) BrandBlue else BrandGreen
                            )
                        }
                    }

                    // Action Icons: Call + View Details
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceSecondary)
                                .clickable { showCallDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call Host",
                                tint = TextPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceSecondary)
                                .clickable {
                                    conversation.itemContext?.let { onViewItem(it.title) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View Item",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Sticky Active Rental Context Banner
            conversation.itemContext?.let { item ->
                LoopRentalContextBanner(
                    itemTitle = item.title,
                    pricePerDay = item.pricePerDay,
                    dates = "10 Oct – 12 Oct (3 Days)",
                    location = item.location,
                    onViewDetails = { onViewItem(item.title) }
                )
            }

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    LoopChatDateDivider(text = "TODAY")
                }

                items(conversation.messages, key = { it.id }) { msg ->
                    LoopMessageBubble(
                        message = msg,
                        onViewItem = onViewItem
                    )
                }

                if (isTypingSimulation) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${conversation.owner.name} is typing",
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "✍️", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Quick Suggestion Reply Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val suggestions = listOf(
                    "📍 Share Pickup Location" to {
                        onSendMessage(
                            "Pickup Location Pin",
                            ChatMessageType.LOCATION_PIN,
                            "Café des Arts, White Town",
                            "10, Suffren St, White Town, Puducherry"
                        )
                    },
                    "⏰ 11:00 AM works!" to {
                        onSendMessage("11:00 AM works perfectly for pickup! 👍", ChatMessageType.TEXT, null, null)
                    },
                    "🔋 Battery & Accessories?" to {
                        onSendMessage("Are all accessories and batteries fully charged?", ChatMessageType.TEXT, null, null)
                        coroutineScope.launch {
                            delay(1200)
                            isTypingSimulation = true
                            delay(1600)
                            isTypingSimulation = false
                            onSendMessage("Yes! 100% charged with an extra spare battery included.", ChatMessageType.TEXT, null, null)
                        }
                    },
                    "📄 Deposit Terms" to {
                        onSendMessage("Could you confirm the pickup ID requirement?", ChatMessageType.TEXT, null, null)
                        coroutineScope.launch {
                            delay(1200)
                            isTypingSimulation = true
                            delay(1600)
                            isTypingSimulation = false
                            onSendMessage("Just bring any Government ID upon pickup. Zero security deposit required.", ChatMessageType.TEXT, null, null)
                        }
                    }
                )

                suggestions.forEach { (label, action) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .clickable { action() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Attachment Drawer
            AnimatedVisibility(
                visible = showAttachmentSheet,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                LoopAttachmentSheet(
                    onOptionSelected = { option ->
                        showAttachmentSheet = false
                        when (option) {
                            "LOCATION" -> {
                                onSendMessage(
                                    "Location Shared",
                                    ChatMessageType.LOCATION_PIN,
                                    "White Town Promenade Beach",
                                    "Goubert Ave, White Town, Puducherry"
                                )
                            }
                            "AUDIO" -> {
                                onSendMessage(
                                    "Voice note",
                                    ChatMessageType.AUDIO_NOTE,
                                    null,
                                    null
                                )
                            }
                            else -> {
                                onSendMessage(
                                    "Sent $option attachment",
                                    ChatMessageType.TEXT,
                                    null,
                                    null
                                )
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(0.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAudioRecording) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceSecondary)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "0:0${recordingSeconds} Recording...",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Red
                                )
                            }

                            Text(
                                text = "Cancel",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.clickable { isAudioRecording = false }
                            )
                        }
                    }
                } else {
                    // Attachment Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceSecondary)
                            .clickable { showAttachmentSheet = !showAttachmentSheet },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach",
                            tint = BrandDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceSecondary)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(BrandDark),
                            decorationBox = { inner ->
                                if (inputMessage.isEmpty()) {
                                    Text(
                                        text = "Type a message...",
                                        fontSize = 13.5.sp,
                                        color = TextMuted
                                    )
                                }
                                inner()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("loop_chat_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Send / Mic Action Button
                val hasText = inputMessage.isNotBlank()
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (hasText || isAudioRecording) BrandDark else SurfaceSecondary, CircleShape)
                        .clickable {
                            if (isAudioRecording) {
                                isAudioRecording = false
                                onSendMessage("Voice note", ChatMessageType.AUDIO_NOTE, null, null)
                            } else if (hasText) {
                                val textToSend = inputMessage
                                inputMessage = ""
                                onSendMessage(textToSend, ChatMessageType.TEXT, null, null)

                                // Intelligent host response simulation
                                coroutineScope.launch {
                                    delay(1200)
                                    isTypingSimulation = true
                                    delay(1700)
                                    isTypingSimulation = false
                                    val replyText = when {
                                        textToSend.contains("hi", ignoreCase = true) || textToSend.contains("hello", ignoreCase = true) ->
                                            "Hi Gokulan! Everything is ready for pickup."
                                        textToSend.contains("time", ignoreCase = true) || textToSend.contains("reach", ignoreCase = true) ->
                                            "11:00 AM works great. I'll meet you near White Town."
                                        else -> "Got it! Thanks for confirming. See you soon! 👍"
                                    }
                                    onSendMessage(replyText, ChatMessageType.TEXT, null, null)
                                }
                            } else {
                                isAudioRecording = true
                            }
                        }
                        .testTag("loop_chat_send_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasText || isAudioRecording) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = if (hasText) "Send" else "Mic",
                        tint = if (hasText || isAudioRecording) Color.White else TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Host Call Dialog
        if (showCallDialog) {
            AlertDialog(
                onDismissRequest = { showCallDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(BrandDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Call Host",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Calling ${conversation.owner.name}...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${conversation.owner.phone} · Loop Verified Host",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCallDialog = false }) {
                        Text("End Call", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
