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
    val attendeeIdsCsv: String = "",
    val status: String = "ACTIVE",
    val createdAt: String = "",
    val categoryName: String = "CREATIVE_PHOTO",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val authorId: String = "",
    val authorName: String = "",
    val imageUrl: String? = null,
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
    val parsedAttendees = attendeeIdsCsv.split("|").map { it.trim() }.filter { it.isNotBlank() }
    return UserCommunityPost(
        id = id,
        title = title,
        type = postType,
        description = description,
        location = location,
        dateTime = dateTime,
        attendeesOrResponses = attendeesOrResponses,
        attendeeIds = parsedAttendees,
        status = postStatus,
        createdAt = createdAt,
        categoryName = categoryName,
        latitude = latitude,
        longitude = longitude,
        authorId = authorId,
        authorName = authorName,
        imageUrl = imageUrl
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
        attendeeIdsCsv = attendeeIds.filter { it.isNotBlank() }.distinct().joinToString("|"),
        status = status.name,
        createdAt = createdAt,
        categoryName = categoryName,
        latitude = latitude,
        longitude = longitude,
        authorId = authorId,
        authorName = authorName,
        imageUrl = imageUrl,
        isSynced = isSynced
    )
}
