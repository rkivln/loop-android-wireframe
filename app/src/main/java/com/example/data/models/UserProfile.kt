package com.example.data.models

data class UserProfile(
    val uid: String = "user_me",
    val displayName: String = "Jaya Nandhini G",
    val email: String = "creator@loop.community",
    val phone: String = "+91 98765 43210",
    val bio: String = "Passionate photographer, ocean diver & DIY maker based in White Town, Puducherry. Happy to share gear & co-create!",
    val location: String = "White Town, Puducherry",
    val avatarPresetIndex: Int = 0,
    val customAvatarUrl: String = "",
    val itemsLentCount: Int = 14,
    val itemsRentedCount: Int = 8,
    val rating: Double = 4.95
)
