package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.ui.components.EditorialTopBar
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

private enum class DispatchFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    UNREAD("Unread")
}

@Composable
fun MessagesScreen(
    initialConversationId: String? = null,
    onConversationSelected: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val conversations by RentalRepository.conversationsFlow.collectAsStateWithLifecycle()
    val messagesMap by RentalRepository.activeConversationMessagesFlow.collectAsStateWithLifecycle()

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

    val activeConversation = conversations.find { it.id == selectedConversationId }

    if (selectedConversationId != null && activeConversation != null) {
        BackHandler {
            selectedConversationId = null
            onConversationSelected(null)
        }

        val messages = messagesMap[activeConversation.id] ?: activeConversation.messages
        var textInput by remember { mutableStateOf("") }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(PaperIvory)
        ) {
            EditorialTopBar(
                title = activeConversation.owner.name,
                onBack = {
                    selectedConversationId = null
                    onConversationSelected(null)
                },
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(InkCharcoal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeConversation.owner.initials,
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                            color = InkWhite
                        )
                    }
                }
            )

            activeConversation.itemContext?.let { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                        color = InkCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "₹${item.pricePerDay}/day",
                        style = LoopType.PriceSmall.copy(fontSize = 12.sp),
                        color = InkSecondary
                    )
                }
            }

            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Say hello to ${activeConversation.owner.name} to coordinate pickup times or inspect accessories.",
                        style = LoopType.BodyEditorial,
                        color = InkSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isMe = msg.isFromMe
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isMe) InkCharcoal else PaperPureWhite)
                                    .border(
                                        1.dp,
                                        if (isMe) InkCharcoal else PaperBorderSubtle,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    style = LoopType.BodyMedium,
                                    color = if (isMe) InkWhite else InkCharcoal
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isMe) "You · ${msg.timestamp}" else "${msg.senderName} · ${msg.timestamp}",
                                    style = LoopType.Metadata.copy(fontSize = 10.sp),
                                    color = if (isMe) InkWhite.copy(alpha = 0.7f) else InkMuted
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = PaperBorderSubtle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperPureWhite)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(100.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (textInput.isEmpty()) {
                        Text(
                            text = "Message ${activeConversation.owner.name}...",
                            style = LoopType.BodyMedium,
                            color = InkMuted,
                            maxLines = 1
                        )
                    }
                    BasicTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        singleLine = true,
                        textStyle = LoopType.BodyMedium.copy(color = InkCharcoal),
                        cursorBrush = SolidColor(InkCharcoal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_message_input")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(InkCharcoal)
                        .clickable {
                            val trimmed = textInput.trim()
                            if (trimmed.isNotEmpty()) {
                                RentalRepository.sendMessage(activeConversation.id, trimmed)
                                textInput = ""
                            }
                        }
                        .testTag("chat_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = InkWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    } else {
        val filteredConversations = conversations.filter { conv ->
            val matchesQuery = searchQuery.isBlank() ||
                conv.owner.name.contains(searchQuery, ignoreCase = true) ||
                (conv.itemContext?.title?.contains(searchQuery, ignoreCase = true) == true) ||
                (conv.lastMessage?.text?.contains(searchQuery, ignoreCase = true) == true)

            val matchesFilter = when (activeFilter) {
                DispatchFilter.ALL -> true
                DispatchFilter.ACTIVE -> conv.lastMessage != null
                DispatchFilter.UNREAD -> conv.unreadCount > 0
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
                Text(
                    text = "Direct dialogue with verified hosts",
                    style = LoopType.BodySmall,
                    color = InkSecondary
                )
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

            if (filteredConversations.isEmpty()) {
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
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(InkCharcoal),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conv.owner.initials,
                                    style = LoopType.HeadlineMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = InkWhite
                                )
                            }

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
                                    Spacer(modifier = Modifier.width(8.dp))
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
                                    Text(
                                        text = conv.lastMessage?.text
                                            ?: conv.itemContext?.title
                                            ?: "Tap to start chatting",
                                        style = LoopType.BodySmall,
                                        color = InkSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (conv.unreadCount > 0) {
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
