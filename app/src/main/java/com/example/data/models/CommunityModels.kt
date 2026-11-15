package com.example.data.models

data class CommunityEvent(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "COMMUNITY",
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timeString: String = "",
    val organizerName: String = "",
    val participantCount: Int = 0
)
