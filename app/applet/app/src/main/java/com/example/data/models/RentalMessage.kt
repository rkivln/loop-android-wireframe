package com.example.data.models

data class RentalMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isTyping: Boolean = false
)

data class ChatConversation(
    val conversationId: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val itemImageUrl: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)
