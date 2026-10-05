package com.example.data.models

data class UserProfile(
    val uid: String = "user_me",
    val displayName: String = "Gokulan R",
    val email: String = "gokulan.rkivln@gmail.com",
    val phone: String = "+91 98401 22345",
    val bio: String = "Designer and photographer based in White Town, Puducherry. Passionate about tactile objects, analogue processes, and quiet travels.",
    val location: String = "White Town, Puducherry",
    val avatarPresetIndex: Int = 0, // 0 to 5 preset avatars
    val customAvatarUri: String? = null,
    val rating: Float = 5.0f,
    val reviewCount: Int = 18,
    val rentalsCompleted: Int = 14,
    val listingsCount: Int = 3,
    val earnedAmount: Int = 2450,
    val isVerified: Boolean = true,
    val memberSince: String = "Member since Oct 2024",
    val badges: List<String> = listOf("✓ ID Verified", "★ Curated Host", "📸 35mm Club", "⚡ Quick Responder")
) {
    val initials: String
        get() {
            val parts = displayName.trim().split(" ")
            return when {
                parts.isEmpty() || displayName.isBlank() -> "U"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            }
        }
}

// Curated avatar styles for Loop users
data class AvatarPreset(
    val id: Int,
    val label: String,
    val bgHex1: Long,
    val bgHex2: Long,
    val emoji: String
)

val AVATAR_PRESETS = listOf(
    AvatarPreset(0, "Midnight Slate", 0xFF0F172A, 0xFF334155, "👤"),
    AvatarPreset(1, "Ocean Blue", 0xFF1D4ED8, 0xFF3B82F6, "📸"),
    AvatarPreset(2, "Emerald Forest", 0xFF065F46, 0xFF10B981, "🌱"),
    AvatarPreset(3, "Sunset Amber", 0xFFC2410C, 0xFFF97316, "⚡"),
    AvatarPreset(4, "Royal Purple", 0xFF5B21B6, 0xFF8B5CF6, "🎧"),
    AvatarPreset(5, "Rose Quartz", 0xFF9F1239, 0xFFF43F5E, "✨")
)

enum class UserActivityType {
    RESERVATION,
    COMMUNITY_EVENT,
    HELP_REQUEST,
    STUDY_GROUP,
    HUB_CHECKIN,
    LISTING_PUBLISHED
}

data class UserActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: UserActivityType,
    val timestamp: String,
    val statusBadge: String,
    val iconEmoji: String,
    val relatedId: String? = null
)

enum class CommunityPostType(val title: String, val emoji: String) {
    EVENT("Local Event", "📸"),
    HELP_REQUEST("Help Needed", "🤝"),
    STUDY_GROUP("Study Group", "📚")
}

enum class CommunityPostStatus(val label: String) {
    ACTIVE("Active / Open"),
    COMPLETED("Resolved / Completed"),
    ARCHIVED("Archived")
}

data class UserCommunityPost(
    val id: String,
    val title: String,
    val type: CommunityPostType,
    val description: String,
    val location: String,
    val dateTime: String,
    val attendeesOrResponses: String,
    val status: CommunityPostStatus = CommunityPostStatus.ACTIVE,
    val createdAt: String = "Today"
)
