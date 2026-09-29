package com.example.data.firebase

import com.example.data.models.ChatConversation
import com.example.data.models.ChatMessageType
import com.example.data.models.DeliveryOptionType
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType
import com.example.data.models.MessageStatus
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.data.models.RentalOwner
import com.example.data.models.SpecFeature

/**
 * Firestore DTOs for Loop Cloud Backend Synchronization
 */

data class FirestoreUser(
    val uid: String = "",
    val name: String = "",
    val badge: String = "Verified User",
    val initials: String = "U",
    val rating: Double = 5.0,
    val phone: String = "",
    val location: String = "Puducherry",
    val isOnline: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

data class FirestoreSpecFeature(
    val iconType: String = "",
    val label: String = ""
)

data class FirestoreRentalItem(
    val id: String = "",
    val title: String = "",
    val categoryName: String = "ELECTRONICS",
    val pricePerDay: Int = 0,
    val location: String = "Puducherry",
    val rating: Double = 4.9,
    val reviewCount: Int = 0,
    val description: String = "",
    val primaryImageName: String = "canon",
    val imageCount: Int = 3,
    val features: List<FirestoreSpecFeature> = emptyList(),
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerInitials: String = "",
    val isPopular: Boolean = false,
    val isFavorite: Boolean = false,
    val availableToday: Boolean = true,
    val securityDeposit: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class FirestoreMessage(
    val id: String = "",
    val senderName: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestampStr: String = "",
    val timestampEpoch: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false,
    val messageTypeStr: String = "TEXT",
    val statusStr: String = "READ",
    val audioDuration: String? = null,
    val locationTitle: String? = null,
    val locationAddress: String? = null,
    val itemTitle: String? = null,
    val itemPricePerDay: Int? = null
)

data class FirestoreConversation(
    val id: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val hostInitials: String = "",
    val hostBadge: String = "Top Owner",
    val hostPhone: String = "",
    val itemId: String? = null,
    val itemTitle: String? = null,
    val itemPricePerDay: Int? = null,
    val isOnline: Boolean = true,
    val isTyping: Boolean = false,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val lastMessageText: String = "",
    val lastMessageTimestamp: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class FirestoreDiscoveryPin(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val typeStr: String = "USER_HOST",
    val latitude: Double = 11.9340,
    val longitude: Double = 79.8330,
    val rating: Double = 4.9,
    val distance: String = "0.4 km away",
    val tag: String = "",
    val dateOrAvailability: String = "Available today",
    val locationName: String = "Puducherry",
    val priceOrAttendees: String = "",
    val description: String = "",
    val ownerId: String? = null,
    val itemId: String? = null,
    val isAttending: Boolean = false
)

data class FirestoreBooking(
    val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val renterName: String = "Gokulan R",
    val hostName: String = "Rakesh Kumar",
    val startDate: String = "10 Oct 2026",
    val endDate: String = "12 Oct 2026",
    val daysCount: Int = 3,
    val pricePerDay: Int = 700,
    val totalAmount: Int = 2150,
    val status: String = "Confirmed",
    val deliveryOption: String = "Self Pickup",
    val handoverOtp: String = "8492",
    val createdAt: Long = System.currentTimeMillis()
)
