package com.example.data.models

import java.util.Locale

data class UserProfile(
    val uid: String = "user_me",
    val displayName: String = "Jaya Nandhini G",
    val email: String = "gokulan.rkivln@gmail.com",
    val phone: String = "+91 98401 22345",
    val bio: String = "Designer and photographer based in White Town, Puducherry. Passionate about tactile objects, analogue processes, and quiet travels.",
    val location: String = "White Town, Puducherry",
    val avatarPresetIndex: Int = 0,
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
    fun getInitials(): String {
        val parts = displayName.trim().split("\\s+".toRegex())
        if (parts.isEmpty() || displayName.isBlank()) return "U"
        return if (parts.size == 1) {
            parts[0].take(2).uppercase(Locale.ROOT)
        } else {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase(Locale.ROOT)
        }
    }
}
