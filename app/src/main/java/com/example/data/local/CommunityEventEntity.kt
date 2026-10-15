package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.CommunityEvent

@Entity(tableName = "community_events")
data class CommunityEventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timeString: String,
    val organizerName: String,
    val participantCount: Int
)

fun CommunityEventEntity.toDomain(): CommunityEvent {
    return CommunityEvent(
        id = id,
        title = title,
        description = description,
        category = category,
        locationName = locationName,
        latitude = latitude,
        longitude = longitude,
        timeString = timeString,
        organizerName = organizerName,
        participantCount = participantCount
    )
}

fun CommunityEvent.toEntity(): CommunityEventEntity {
    return CommunityEventEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        locationName = locationName,
        latitude = latitude,
        longitude = longitude,
        timeString = timeString,
        organizerName = organizerName,
        participantCount = participantCount
    )
}
