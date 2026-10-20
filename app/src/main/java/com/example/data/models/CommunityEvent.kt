package com.example.data.models

data class CommunityEvent(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timeString: String,
    val organizerName: String,
    val participantCount: Int = 0
)
