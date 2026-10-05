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
    val responseTime: String = "< 15 mins",
    val phone: String = "+91 98765 43210"
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

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class ChatMessageType {
    TEXT,
    AUDIO_NOTE,
    IMAGE_MEDIA,
    LOCATION_PIN,
    RENTAL_OFFER
}

data class RentalMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean,
    val messageType: ChatMessageType = ChatMessageType.TEXT,
    val status: MessageStatus = MessageStatus.READ,
    val audioDuration: String? = null,
    val isAudioPlaying: Boolean = false,
    @DrawableRes val imageRes: Int? = null,
    val locationTitle: String? = null,
    val locationAddress: String? = null,
    val itemTitle: String? = null,
    val itemPricePerDay: Int? = null,
    @DrawableRes val itemImageRes: Int? = null
)

data class ChatConversation(
    val id: String,
    val owner: RentalOwner,
    val itemContext: RentalItem?,
    val isOnline: Boolean = true,
    val isTyping: Boolean = false,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val messages: List<RentalMessage>
) {
    val lastMessage: RentalMessage? get() = messages.lastOrNull()
}

enum class DiscoveryPinType {
    USER_HOST,
    COMMUNITY_EVENT,
    RENTAL_ITEM,
    PICKUP_HUB,
    STUDY_GROUP,
    HELP_REQUEST
}

enum class MapCategoryFilter(
    val title: String,
    val emoji: String,
    val subtitle: String
) {
    ALL("All", "✨", "All neighborhood discoveries"),
    STUDY_GROUPS("Study Groups", "📚", "Co-working & study tables"),
    HELP_NEEDED("Help Needed", "🤝", "Gear setup & skill sharing"),
    LOCAL_EVENTS("Local Events", "📸", "Photowalks & gatherings"),
    GEAR_HUBS("Gear Hubs", "🔒", "24/7 Smart pickup hubs"),
    MAKERS("Makers", "🛠️", "Verified creators & gear hosts")
}

data class DiscoveryPinItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: DiscoveryPinType,
    val latitude: Double,
    val longitude: Double,
    val rating: Float = 4.9f,
    val distance: String = "0.4 km away",
    val tag: String,
    val dateOrAvailability: String,
    val locationName: String,
    val priceOrAttendees: String,
    val description: String,
    val owner: RentalOwner? = null,
    val rentalItem: RentalItem? = null,
    @DrawableRes val imageRes: Int? = null,
    val isAttending: Boolean = false,
    val category: RentalCategory = RentalCategory.ELECTRONICS,
    val filterCategory: MapCategoryFilter = MapCategoryFilter.MAKERS,
    val neighborhood: String = "White Town",
    val walkingTime: String = "4 min walk",
    val cyclingTime: String = "1 min ride",
    val address: String = "Suffren St, White Town, Puducherry"
)

data class CuratedWalkingRoute(
    val id: String,
    val title: String,
    val subtitle: String,
    val distanceKm: String,
    val durationMin: String,
    val stopsCount: Int,
    val pinIds: List<String>,
    val description: String
)
