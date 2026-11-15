package com.example.data.models

enum class AppearanceMode(val label: String, val subtitle: String) {
    LIGHT("Light mode", "Warm ivory paper & deep ink surfaces"),
    DARK("Dark mode", "Nocturnal obsidian studio palette"),
    SYSTEM("System default", "Follows your Android device appearance");

    companion object {
        fun fromNameOrDefault(name: String?): AppearanceMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SYSTEM
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
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val profileVisibility: ProfileVisibility = ProfileVisibility.PUBLIC,
    val locationSharing: LocationSharingPreference = LocationSharingPreference.PRECISE,
    val notifications: NotificationPreferences = NotificationPreferences(),
    val blockedUsers: List<BlockedUserEntry> = emptyList()
) {
    val blockedUserIds: Set<String>
        get() = blockedUsers.map { it.userId }.filter { it.isNotBlank() }.toSet()
}
