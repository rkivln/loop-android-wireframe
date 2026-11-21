package com.example.data.models

import androidx.annotation.DrawableRes
import com.example.R

enum class RentalCategory(
    val title: String,
    val bgHex: Long,
    val iconHex: Long
) {
    ELECTRONICS("Electronics", 0xFFEFF6FF, 0xFF2563EB),
    FURNITURE("Furniture", 0xFFFFF7ED, 0xFFEA580C),
    VEHICLES("Vehicles", 0xFFF0FDFA, 0xFF0D9488),
    TOOLS("Tools", 0xFFFEF3C7, 0xFFD97706),
    EVENTS("Events", 0xFFFDF2F8, 0xFFDB2777),
    STUDY_OFFICE("Study & Office", 0xFFEFF6FF, 0xFF2563EB),
    HOME_LIVING("Home & Living", 0xFFFFF7ED, 0xFFEA580C)
}

data class SpecFeature(
    val iconType: String, // "CAMERA", "LENS", "STORAGE", "BAG", "CHIP", "BATTERY", "RANGE"
    val label: String
)

data class RentalOwner(
    val id: String,
    val name: String,
    val badge: String = "Community Member",
    val initials: String = "U",
    val memberSince: String = "",
    val rating: Float = 0f,
    val totalRentals: Int = 0,
    val responseTime: String = "",
    val phone: String = "",
    val avatarUrl: String? = null,
    val avatarPresetIndex: Int? = null
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
    @DrawableRes val primaryImageRes: Int = 0,
    val imageUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val imageCount: Int = 1,
    val features: List<SpecFeature>,
    val owner: RentalOwner,
    val isPopular: Boolean = false,
    val isFavorite: Boolean = false,
    val availableToday: Boolean = true,
    val activeBookingId: String? = null,
    val securityDeposit: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val dailyRate: Int get() = pricePerDay
    val deposit: Int get() = securityDeposit
    val isAvailable: Boolean get() = availableToday
}

enum class DeliveryOptionType(val title: String, val subtitle: String?, val fee: Int) {
    SELF_PICKUP("Self Pickup", null, 0),
    OWNER_DELIVERY("Owner Delivery", "Local delivery arranged with host", 100)
}

data class BookingRequest(
    val id: String,
    val item: RentalItem,
    val startDate: String = "",
    val endDate: String = "",
    val daysCount: Int = 1,
    val deliveryOption: DeliveryOptionType = DeliveryOptionType.SELF_PICKUP,
    val serviceFee: Int = 50,
    val status: String = "Confirmed"
) {
    val rentalTotal: Int get() = item.pricePerDay * daysCount
    val deliveryFee: Int get() = deliveryOption.fee
}

data class UserRentalBooking(
    val id: String,
    val item: RentalItem,
    val hostId: String = "",
    val hostName: String,
    val renterId: String = "",
    val renterName: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val daysRemaining: Int,
    val totalPaid: Int,
    val status: String = "Confirmed",
    val handoverOtp: String = "",
    val deliveryOption: String = "Self Pickup",
    val isCompleted: Boolean = false,
    val isReviewed: Boolean = false,
    val ratingGiven: Int = 0,
    val reviewComment: String = ""
)

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
    val senderId: String = "",
    val messageType: ChatMessageType = ChatMessageType.TEXT,
    val status: MessageStatus = MessageStatus.READ,
    val audioDuration: String? = null,
    val isAudioPlaying: Boolean = false,
    @DrawableRes val imageRes: Int? = null,
    val imageUrl: String? = null,
    val locationTitle: String? = null,
    val locationAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val itemTitle: String? = null,
    val itemPricePerDay: Int? = null,
    @DrawableRes val itemImageRes: Int? = null,
    val isEncrypted: Boolean = false,
    val decryptionFailed: Boolean = false,
    val keyVersion: Int = 1,
    val ciphertext: String? = null,
    val iv: String? = null,
    val senderPublicKey: String? = null,
    val recipientPublicKey: String? = null,
    val replyToMessageId: String? = null,
    val replyToSenderName: String? = null,
    val replyToSnippet: String? = null,
    val timestampEpoch: Long = 0L,
    val expiresAtEpoch: Long = 0L
)

data class ChatConversation(
    val id: String,
    val owner: RentalOwner,
    val itemContext: RentalItem?,
    val communityPostId: String? = null,
    val communityPostTitle: String? = null,
    val lastSenderId: String = "",
    val isOnline: Boolean = true,
    val isTyping: Boolean = false,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val mutedByUserIds: List<String> = emptyList(),
    val disappearingTimerSeconds: Long = 0L,
    val e2eeEnabled: Boolean = true,
    val peerPublicKey: String = "",
    val peerKeyFingerprint: String = "",
    val messages: List<RentalMessage>
) {
    val lastMessage: RentalMessage? get() = messages.lastOrNull()

    fun isMutedFor(uid: String): Boolean {
        return uid.isNotBlank() && uid in mutedByUserIds
    }
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
    val subtitle: String
) {
    ALL("All", "All neighborhood discoveries"),
    STUDY_GROUPS("Study Groups", "Co-working & study tables"),
    HELP_NEEDED("Help Needed", "Gear setup & skill sharing"),
    LOCAL_EVENTS("Local Events", "Photowalks & gatherings"),
    GEAR_HUBS("Gear Hubs", "24/7 Smart pickup hubs"),
    MAKERS("Makers", "Community creators & hosts")
}

data class DiscoveryPinItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: DiscoveryPinType,
    val latitude: Double,
    val longitude: Double,
    val rating: Float = 0f,
    val distance: String = "",
    val tag: String,
    val dateOrAvailability: String,
    val locationName: String,
    val priceOrAttendees: String,
    val description: String,
    val owner: RentalOwner? = null,
    val rentalItem: RentalItem? = null,
    val communityPost: UserCommunityPost? = null,
    @DrawableRes val imageRes: Int? = null,
    val imageUrl: String? = null,
    val isAttending: Boolean = false,
    val category: RentalCategory = RentalCategory.ELECTRONICS,
    val filterCategory: MapCategoryFilter = MapCategoryFilter.MAKERS,
    val neighborhood: String = "",
    val walkingTime: String = "",
    val cyclingTime: String = "",
    val address: String = ""
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
