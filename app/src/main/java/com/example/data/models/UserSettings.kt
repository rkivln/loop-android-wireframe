package com.example.data.models

enum class AppearanceMode(val label: String, val subtitle: String) {
    LIGHT("Light mode", "Warm ivory paper & deep ink surfaces"),
    DARK("Dark mode", "Nocturnal obsidian studio palette"),
    SYSTEM("System default", "Follows your Android device appearance");

    companion object {
        fun fromNameOrDefault(name: String?): AppearanceMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: LIGHT
        }
    }
}

enum class ProfileVisibility(val label: String, val subtitle: String) {
    PUBLIC("Public", "Anyone in the marketplace can view your profile & pieces"),
    COMMUNITY_ONLY("Community Members", "Visible to signed-in neighborhood creators"),
    PRIVATE("Private", "Hide public profile details; only active chat or booking partners see you");

    companion object {
        fun fromNameOrDefault(name: String?): ProfileVisibility {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PUBLIC
        }
    }
}

enum class LocationSharingPreference(val label: String, val subtitle: String) {
    PRECISE("Precise GPS", "Attach live coordinates to new listings & neighborhood pins"),
    NEIGHBORHOOD_ONLY("Neighborhood Only", "Share neighborhood name without exact coordinates"),
    HIDDEN("Hidden", "Never auto-detect GPS or attach coordinates to posts");

    companion object {
        fun fromNameOrDefault(name: String?): LocationSharingPreference {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PRECISE
        }
    }
}

enum class DisappearingMessageTimer(
    val seconds: Long,
    val label: String,
    val shortLabel: String,
    val subtitle: String
) {
    OFF(0L, "Off", "Off", "Messages stay in thread until deleted"),
    DAY_1(86_400L, "24 Hours", "24h", "New messages disappear 24 hours after sending"),
    DAYS_7(604_800L, "7 Days", "7d", "New messages disappear 7 days after sending"),
    DAYS_30(2_592_000L, "30 Days", "30d", "New messages disappear 30 days after sending");

    companion object {
        fun fromSecondsOrDefault(seconds: Long?): DisappearingMessageTimer {
            if (seconds == null || seconds <= 0L) return OFF
            return entries.firstOrNull { it.seconds == seconds } ?: OFF
        }
    }
}

data class ChatPrivacySettings(
    val sendReadReceipts: Boolean = true,
    val showTypingStatus: Boolean = true,
    val defaultDisappearingSeconds: Long = 0L,
    val hideNotificationPreviews: Boolean = false
)

data class NotificationPreferences(
    val pushEnabled: Boolean = true,
    val newNeighbourhoodPosts: Boolean = true,
    val repliesToComments: Boolean = true,
    val likesAndInteractions: Boolean = true,
    val announcements: Boolean = true
)

data class BlockedUserEntry(
    val userId: String = "",
    val displayName: String = "",
    val blockedAtLabel: String = ""
)

data class UserSettings(
    val appearanceMode: AppearanceMode = AppearanceMode.LIGHT,
    val profileVisibility: ProfileVisibility = ProfileVisibility.PUBLIC,
    val locationSharing: LocationSharingPreference = LocationSharingPreference.PRECISE,
    val notifications: NotificationPreferences = NotificationPreferences(),
    val chatPrivacy: ChatPrivacySettings = ChatPrivacySettings(),
    val blockedUsers: List<BlockedUserEntry> = emptyList()
) {
    val blockedUserIds: Set<String>
        get() = blockedUsers.map { it.userId }.filter { it.isNotBlank() }.toSet()
}
