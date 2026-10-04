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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.PrimaryCTA
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
        EditorialConversationScreen(
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
        EditorialInboxScreen(
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
fun EditorialInboxScreen(
    conversations: List<ChatConversation>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilterTab: String,
    onSelectFilterTab: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredList = remember(conversations, searchQuery, selectedFilterTab) {
        conversations.filter { conv ->
            val matchesFilter = when (selectedFilterTab) {
                "Active" -> conv.itemContext != null
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperWarm)
            .statusBarsPadding()
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Studio Dispatch",
                    style = LoopType.HeroDisplayLarge.copy(fontSize = 28.sp),
                    color = InkCharcoal
                )
                Text(
                    text = "Direct dialogue with verified hosts",
                    style = LoopType.Metadata,
                    color = InkSecondary
                )
            }
        }

        // Search Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorder, RoundedCornerShape(24.dp))
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
                        tint = InkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                        cursorBrush = SolidColor(InkCharcoal),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search makers and equipment...",
                                    style = LoopType.BodyEditorial.copy(fontSize = 13.5.sp),
                                    color = InkMuted
                                )
                            }
                            inner()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inbox_search_input")
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = InkMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onSearchQueryChange("") }
                        )
                    }
                }
            }
        }

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Active", "Unread").forEach { tab ->
                val isSelected = selectedFilterTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) InkCharcoal else PaperPureWhite)
                        .border(
                            1.dp,
                            if (isSelected) InkCharcoal else PaperBorder,
                            RoundedCornerShape(100.dp)
                        )
                        .clickable { onSelectFilterTab(tab) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tab,
                        style = LoopType.Metadata.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (isSelected) InkWhite else InkCharcoal
                    )
                }
            }
        }

        // Conversation List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList, key = { it.id }) { conv ->
                EditorialConversationCard(
                    conversation = conv,
                    onClick = { onSelectConversation(conv.id) }
                )
            }
        }
    }
}

@Composable
fun EditorialConversationCard(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    val lastMsg = conversation.lastMessage

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(14.dp)
            .testTag("editorial_chat_card_${conversation.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Monogram Avatar
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(InkCharcoal),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversation.owner.initials,
                style = LoopType.HeadlineMedium.copy(fontSize = 16.sp),
                color = InkWhite
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.owner.name,
                    style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = InkCharcoal
                )

                Text(
                    text = lastMsg?.timestamp ?: "10:30 AM",
                    style = LoopType.Metadata.copy(fontSize = 11.sp),
                    color = if (conversation.unreadCount > 0) AccentForestGreen else InkMuted
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lastMsg?.text ?: "Start conversation",
                    style = LoopType.BodySmall,
                    color = InkSecondary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                if (conversation.unreadCount > 0) {
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

@Composable
fun EditorialConversationScreen(
    conversation: ChatConversation,
    onBack: () -> Unit,
    onSendMessage: (String, ChatMessageType, String?, String?) -> Unit,
    onViewItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperWarm)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Editorial Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onBack)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = InkCharcoal,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(InkCharcoal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation.owner.initials,
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                            color = InkWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = conversation.owner.name,
                            style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = InkCharcoal
                        )
                        Text(
                            text = "Verified Host · Puducherry",
                            style = LoopType.Metadata.copy(fontSize = 11.sp),
                            color = AccentForestGreen
                        )
                    }
                }
            }

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversation.messages, key = { it.id }) { msg ->
                    EditorialMessageRow(msg)
                }
            }

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PaperIvory)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                        cursorBrush = SolidColor(InkCharcoal),
                        decorationBox = { inner ->
                            if (inputMessage.isEmpty()) {
                                Text(
                                    text = "Send message to ${conversation.owner.name.split(" ").first()}...",
                                    style = LoopType.BodyEditorial.copy(fontSize = 13.5.sp),
                                    color = InkMuted
                                )
                            }
                            inner()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editorial_chat_input")
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputMessage.isNotBlank()) InkCharcoal else PaperIvory)
                        .clickable(
                            enabled = inputMessage.isNotBlank(),
                            onClick = {
                                if (inputMessage.isNotBlank()) {
                                    onSendMessage(inputMessage.trim(), ChatMessageType.TEXT, null, null)
                                    inputMessage = ""
                                }
                            }
                        )
                        .testTag("editorial_chat_send_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputMessage.isNotBlank()) InkWhite else InkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorialMessageRow(message: RentalMessage) {
    val isMe = message.isFromMe

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMe) 18.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 18.dp
                    )
                )
                .background(if (isMe) InkCharcoal else PaperPureWhite)
                .border(1.dp, if (isMe) InkCharcoal else PaperBorderSubtle, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                style = LoopType.BodyEditorial.copy(fontSize = 14.sp),
                color = if (isMe) InkWhite else InkCharcoal
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = message.timestamp,
            style = LoopType.Metadata.copy(fontSize = 10.sp),
            color = InkMuted,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
