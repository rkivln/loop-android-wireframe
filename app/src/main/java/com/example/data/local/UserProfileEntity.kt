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
    val avatarPresetIndex: Int,
    val customAvatarUrl: String,
    val itemsLentCount: Int,
    val itemsRentedCount: Int,
    val rating: Double
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
        customAvatarUrl = customAvatarUrl,
        itemsLentCount = itemsLentCount,
        itemsRentedCount = itemsRentedCount,
        rating = rating
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
        customAvatarUrl = customAvatarUrl,
        itemsLentCount = itemsLentCount,
        itemsRentedCount = itemsRentedCount,
        rating = rating
    )
}
