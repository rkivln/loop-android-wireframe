package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.models.ChatConversation
import com.example.data.models.RentalMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    conversations: List<ChatConversation>,
    messagesMap: Map<String, List<RentalMessage>>,
    onSendMessage: (conversationId: String, text: String) -> Unit
) {
    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    val activeConversation = conversations.find { it.conversationId == selectedConversationId }

    if (activeConversation != null) {
        val messages = messagesMap[activeConversation.conversationId] ?: emptyList()
        var textInput by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(activeConversation.otherUserName) },
                    navigationIcon = {
                        IconButton(onClick = { selectedConversationId = null }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            bottomBar = {
                Surface(tonalElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    onSendMessage(activeConversation.conversationId, textInput)
                                    textInput = ""
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderId == "user_me"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = msg.text, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isMe) "Sent ✓" else "Received",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Messages & Realtime Chat") }) }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(conversations) { conv ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedConversationId = conv.conversationId }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = conv.otherUserName, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = conv.lastMessage, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            }
                            if (conv.unreadCount > 0) {
                                Badge { Text("${conv.unreadCount}") }
                            }
                        }
                    }
                }
            }
        }
    }
}
