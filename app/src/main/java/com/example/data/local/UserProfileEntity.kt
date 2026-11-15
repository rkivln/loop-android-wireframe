package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.UserProfile

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val uid: String,
    val displayName: String,
    val email: String,
    val phone: String,
    val bio: String,
    val location: String,
    val avatarPresetIndex: Int = 0,
    val customAvatarUri: String? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val rentalsCompleted: Int = 0,
    val listingsCount: Int = 0,
    val earnedAmount: Int = 0,
    val isVerified: Boolean = false,
    val memberSince: String = ""
)

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        uid = uid,
        displayName = displayName,
        email = email,
        phone = phone,
        bio = bio,
        location = location,
        avatarPresetIndex = avatarPresetIndex,
        customAvatarUri = customAvatarUri,
        rating = rating,
        reviewCount = reviewCount,
        rentalsCompleted = rentalsCompleted,
        listingsCount = listingsCount,
        earnedAmount = earnedAmount,
        isVerified = isVerified,
        memberSince = memberSince
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        uid = uid,
        displayName = displayName,
        email = email,
        phone = phone,
        bio = bio,
        location = location,
        avatarPresetIndex = avatarPresetIndex,
        customAvatarUri = customAvatarUri,
        rating = rating,
        reviewCount = reviewCount,
        rentalsCompleted = rentalsCompleted,
        listingsCount = listingsCount,
        earnedAmount = earnedAmount,
        isVerified = isVerified,
        memberSince = memberSince
    )
}
