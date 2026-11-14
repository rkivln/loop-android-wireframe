package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.UserCommunityPost

@Entity(tableName = "community_events")
data class CommunityEventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: String = "EVENT",
    val description: String,
    val location: String,
    val dateTime: String,
    val attendeesOrResponses: String,
    val status: String = "ACTIVE",
    val createdAt: String = "Today",
    val categoryName: String = "CREATIVE_PHOTO",
    val latitude: Double = 11.9340,
    val longitude: Double = 79.8330,
    val authorId: String = "user_me",
    val authorName: String = "Organizer",
    val isSynced: Boolean = true
)

fun CommunityEventEntity.toUserCommunityPost(): UserCommunityPost {
    val postType = try {
        CommunityPostType.valueOf(type)
    } catch (e: Exception) {
        CommunityPostType.EVENT
    }
    val postStatus = try {
        CommunityPostStatus.valueOf(status)
    } catch (e: Exception) {
        CommunityPostStatus.ACTIVE
    }
    return UserCommunityPost(
        id = id,
        title = title,
        type = postType,
        description = description,
        location = location,
        dateTime = dateTime,
        attendeesOrResponses = attendeesOrResponses,
        status = postStatus,
        createdAt = createdAt,
        categoryName = categoryName,
        latitude = latitude,
        longitude = longitude,
        authorId = authorId,
        authorName = authorName
    )
}

fun UserCommunityPost.toEntity(isSynced: Boolean = true): CommunityEventEntity {
    return CommunityEventEntity(
        id = id,
        title = title,
        type = type.name,
        description = description,
        location = location,
        dateTime = dateTime,
        attendeesOrResponses = attendeesOrResponses,
        status = status.name,
        createdAt = createdAt,
        categoryName = categoryName,
        latitude = latitude,
        longitude = longitude,
        authorId = authorId,
        authorName = authorName,
        isSynced = isSynced
    )
}
