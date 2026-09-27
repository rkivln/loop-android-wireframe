package com.example.data.models

enum class LoopCategory(val label: String) {
    ALL("All"),
    PEOPLE("People"),
    EVENTS("Events"),
    HELP("Help"),
    STUDY("Study")
}

data class UserProfile(
    val id: String,
    val name: String,
    val handle: String,
    val bio: String,
    val postsCount: Int,
    val connectionsCount: Int,
    val eventsCount: Int,
    val isOnline: Boolean = true
)

data class NearbyPerson(
    val id: String,
    val name: String,
    val role: String,
    val distance: String,
    val initials: String,
    val colorIndex: Int,
    val latRatio: Float,
    val lngRatio: Float
)

data class EventItem(
    val id: String,
    val title: String,
    val category: LoopCategory,
    val time: String,
    val distance: String,
    val location: String,
    val attendeeCount: Int,
    val description: String,
    val isGoing: Boolean = false,
    val gradientType: Int = 0
)

data class StudyGroup(
    val id: String,
    val title: String,
    val topic: String,
    val membersCount: Int,
    val distance: String,
    val schedule: String,
    val description: String,
    val isJoined: Boolean = false
)

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val timeAgo: String,
    val distance: String,
    val content: String,
    val category: LoopCategory,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val visualType: Int = 0 // 0: None, 1: Sunset/Ocean, 2: Code/Dark, 3: Abstract Mesh
)

enum class MessageType {
    TEXT,
    AUDIO,
    FILE
}

data class ChatMessage(
    val id: String,
    val senderName: String,
    val text: String = "",
    val timestamp: String,
    val isFromMe: Boolean,
    val type: MessageType = MessageType.TEXT,
    val fileName: String? = null,
    val fileSize: String? = null,
    val audioDuration: String? = null,
    val isPlaying: Boolean = false
)

data class MapPinItem(
    val id: String,
    val title: String,
    val category: LoopCategory,
    val distance: String,
    val xRatio: Float,
    val yRatio: Float,
    val snippet: String,
    val memberCount: Int,
    val initials: String = "",
    val latitude: Double = 11.9338,
    val longitude: Double = 79.8297
)
