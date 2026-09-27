package com.example.data.models

import androidx.annotation.DrawableRes
import com.example.R

enum class RentalCategory(
    val title: String,
    val iconEmoji: String,
    val bgHex: Long,
    val iconHex: Long
) {
    ELECTRONICS("Electronics", "💻", 0xFFEFF6FF, 0xFF2563EB),
    FURNITURE("Furniture", "🪑", 0xFFFFF7ED, 0xFFEA580C),
    VEHICLES("Vehicles", "🛵", 0xFFF0FDFA, 0xFF0D9488),
    TOOLS("Tools", "🔧", 0xFFFEF3C7, 0xFFD97706),
    EVENTS("Events", "🎁", 0xFFFDF2F8, 0xFFDB2777),
    STUDY_OFFICE("Study & Office", "🖥️", 0xFFEFF6FF, 0xFF2563EB),
    HOME_LIVING("Home & Living", "🛋️", 0xFFFFF7ED, 0xFFEA580C)
}

data class SpecFeature(
    val iconType: String, // "CAMERA", "LENS", "STORAGE", "BAG", "CHIP", "BATTERY", "RANGE"
    val label: String
)

data class RentalOwner(
    val id: String,
    val name: String,
    val badge: String = "Top Owner",
    val initials: String = "RK",
    val memberSince: String = "Joined 1 year ago",
    val rating: Float = 4.9f,
    val totalRentals: Int = 48,
    val responseTime: String = "< 15 mins"
)

data class RentalItem(
    val id: String,
    val title: String,
    val category: RentalCategory,
    val pricePerDay: Int,
    val location: String,
    val rating: Float,
    val reviewCount: Int,
    val description: String,
    @DrawableRes val primaryImageRes: Int,
    val imageCount: Int = 5,
    val features: List<SpecFeature>,
    val owner: RentalOwner,
    val isPopular: Boolean = false,
    val isFavorite: Boolean = false,
    val availableToday: Boolean = true,
    val securityDeposit: Int = 0
)

enum class DeliveryOptionType(val title: String, val subtitle: String?, val fee: Int) {
    SELF_PICKUP("Self Pickup", null, 0),
    OWNER_DELIVERY("Owner Delivery", "Delivery within Puducherry", 100)
}

data class BookingRequest(
    val id: String,
    val item: RentalItem,
    val startDate: String = "10 Oct 2026",
    val endDate: String = "12 Oct 2026",
    val daysCount: Int = 3,
    val deliveryOption: DeliveryOptionType = DeliveryOptionType.SELF_PICKUP,
    val serviceFee: Int = 50,
    val status: String = "Confirmed"
) {
    val rentalTotal: Int get() = item.pricePerDay * daysCount
    val deliveryFee: Int get() = deliveryOption.fee
    val grandTotal: Int get() = rentalTotal + deliveryFee + serviceFee
}

data class RentalMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean,
    val isItemCard: Boolean = false,
    val itemTitle: String? = null
)
