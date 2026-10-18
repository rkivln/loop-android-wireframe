package com.example.data.models

enum class RentalCategory {
    ALL, CAMERAS, LENSES, AUDIO, LIGHTING, DRONES, ACCESSORIES
}

data class RentalOwner(
    val name: String,
    val avatarUrl: String,
    val rating: Float,
    val reviewCount: Int,
    val isVerified: Boolean
)

data class RentalItem(
    val id: String,
    val title: String,
    val category: RentalCategory,
    val dailyRate: Double,
    val deposit: Double,
    val rating: Float,
    val reviewCount: Int,
    val location: String,
    val imageUrl: String,
    val description: String,
    val owner: RentalOwner,
    val isFavorite: Boolean = false,
    val specs: Map<String, String> = emptyMap()
)
