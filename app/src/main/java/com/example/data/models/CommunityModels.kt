package com.example.data.models

data class CommunityEvent(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "COMMUNITY",
    val locationName: String = "White Town, Puducherry",
    val latitude: Double = 11.9340,
    val longitude: Double = 79.8330,
    val timeString: String = "Upcoming",
    val organizerName: String = "Loop Member",
    val participantCount: Int = 1
)
