package com.example.data.models

data class RentalItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val pricePerDay: Double,
    val ownerName: String,
    val ownerRating: Double = 4.9,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean = true,
    val tags: List<String> = emptyList(),
    val imageUrl: String = ""
)
