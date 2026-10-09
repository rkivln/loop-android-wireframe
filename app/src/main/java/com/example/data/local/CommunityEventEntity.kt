package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.RentalCategory
import com.example.data.models.UserCommunityPost

/**
 * Room Entity representing a community event, help request, or study group.
 * Provides persistent local caching and offline-first durability.
 */
@Entity(tableName = "community_events")
data class CommunityEventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: String, // EVENT, HELP_REQUEST, STUDY_GROUP
    val category: String, // ELECTRONICS, EVENTS, FURNITURE, VEHICLES, STUDY_OFFICE
    val description: String,
    val location: String,
    val dateTime: String,
    val attendeesOrResponses: String,
    val status: String, // ACTIVE, COMPLETED, ARCHIVED
    val createdAt: String,
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val latitude: Double = 11.9338,
    val longitude: Double = 79.8350,
    val authorId: String = "user_me",
    val authorName: String = "Gokulan R",
    val isSyncedToFirestore: Boolean = true,
    val lastUpdatedEpoch: Long = System.currentTimeMillis()
)

/**
 * Converts a Room database entity to the UI / domain model [UserCommunityPost].
 */
fun CommunityEventEntity.toUserCommunityPost(): UserCommunityPost {
    val postType = try {
        CommunityPostType.valueOf(this.type)
    } catch (e: Exception) {
        CommunityPostType.EVENT
    }

    val postCategory = try {
        RentalCategory.valueOf(this.category)
    } catch (e: Exception) {
        RentalCategory.ELECTRONICS
    }

    val postStatus = try {
        CommunityPostStatus.valueOf(this.status)
    } catch (e: Exception) {
        CommunityPostStatus.ACTIVE
    }

    return UserCommunityPost(
        id = this.id,
        title = this.title,
        type = postType,
        description = this.description,
        location = this.location,
        dateTime = this.dateTime,
        attendeesOrResponses = this.attendeesOrResponses,
        status = postStatus,
        createdAt = this.createdAt,
        latitude = this.latitude,
        longitude = this.longitude,
        category = postCategory
    )
}

/**
 * Converts a domain [UserCommunityPost] to a Room [CommunityEventEntity].
 */
fun UserCommunityPost.toEntity(
    isSynced: Boolean = true,
    authorId: String = "user_me",
    authorName: String = "Gokulan R",
    createdAtEpoch: Long = System.currentTimeMillis()
): CommunityEventEntity {
    return CommunityEventEntity(
        id = this.id,
        title = this.title,
        type = this.type.name,
        category = this.category.name,
        description = this.description,
        location = this.location,
        dateTime = this.dateTime,
        attendeesOrResponses = this.attendeesOrResponses,
        status = this.status.name,
        createdAt = this.createdAt,
        createdAtEpoch = createdAtEpoch,
        latitude = this.latitude,
        longitude = this.longitude,
        authorId = authorId,
        authorName = authorName,
        isSyncedToFirestore = isSynced,
        lastUpdatedEpoch = System.currentTimeMillis()
    )
}
