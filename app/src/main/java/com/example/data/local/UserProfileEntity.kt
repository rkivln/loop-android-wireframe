package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.UserProfile

/**
 * Room Entity representing a persistent local user profile in SQLite.
 * Provides instant local offline access and local-first editing for avatar, display name, bio, etc.
 */
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val uid: String = "user_me",
    val displayName: String = "Gokulan R",
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
    val lastUpdatedEpoch: Long = System.currentTimeMillis()
)

/**
 * Converts a Room [UserProfileEntity] to domain [UserProfile].
 */
fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        uid = this.uid,
        displayName = this.displayName,
        email = this.email,
        phone = this.phone,
        bio = this.bio,
        location = this.location,
        avatarPresetIndex = this.avatarPresetIndex,
        customAvatarUri = this.customAvatarUri,
        rating = this.rating,
        reviewCount = this.reviewCount,
        rentalsCompleted = this.rentalsCompleted,
        listingsCount = this.listingsCount,
        earnedAmount = this.earnedAmount,
        isVerified = this.isVerified,
        memberSince = this.memberSince
    )
}

/**
 * Converts a domain [UserProfile] to Room [UserProfileEntity].
 */
fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        uid = this.uid,
        displayName = this.displayName,
        email = this.email,
        phone = this.phone,
        bio = this.bio,
        location = this.location,
        avatarPresetIndex = this.avatarPresetIndex,
        customAvatarUri = this.customAvatarUri,
        rating = this.rating,
        reviewCount = this.reviewCount,
        rentalsCompleted = this.rentalsCompleted,
        listingsCount = this.listingsCount,
        earnedAmount = this.earnedAmount,
        isVerified = this.isVerified,
        memberSince = this.memberSince,
        lastUpdatedEpoch = System.currentTimeMillis()
    )
}
