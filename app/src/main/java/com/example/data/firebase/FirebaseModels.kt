package com.example.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore Canonical Typed Data Models for Loop App
 * Uses @DocumentId, @ServerTimestamp, and full default parameters
 */

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String? = null,
    val bio: String? = null,
    val location: String = "",
    val avatarPresetIndex: Int = 0,
    val customAvatarUri: String? = null,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val rentalsCompleted: Int = 0,
    val listingsCount: Int = 0,
    val earnedAmount: Int = 0,
    val isVerified: Boolean = false,
    val memberSince: String = "",
    val badges: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreSpecFeature(
    val iconType: String = "",
    val label: String = ""
)

@IgnoreExtraProperties
data class FirestoreRentalItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val categoryName: String = "ELECTRONICS",
    val pricePerDay: Int = 0,
    val location: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val description: String = "",
    val imageUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val imageCount: Int = 0,
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerInitials: String = "",
    val isPopular: Boolean = false,
    val isFavorite: Boolean = false,
    val availableToday: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val features: List<Map<String, String>> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreCommunityEvent(
    @DocumentId val id: String = "",
    val title: String = "",
    val typeStr: String = "EVENT",
    val categoryName: String = "COMMUNITY",
    val description: String = "",
    val location: String = "",
    val dateTime: String = "",
    val attendeesOrResponses: String = "0 participating",
    val statusStr: String = "ACTIVE",
    val authorId: String = "",
    val authorName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreConversation(
    @DocumentId val id: String = "",
    val participantIds: List<String> = emptyList(),
    val hostId: String = "",
    val hostName: String = "",
    val hostInitials: String = "",
    val hostBadge: String = "Community Member",
    val hostPhone: String = "",
    val renterId: String = "",
    val renterName: String = "",
    val itemId: String? = null,
    val itemTitle: String? = null,
    val itemPricePerDay: Int? = null,
    val isOnline: Boolean = true,
    val isTyping: Boolean = false,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val lastMessageText: String = "",
    val lastMessageTimestamp: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestampStr: String = "",
    val timestampEpoch: Long = 0L,
    val isFromMe: Boolean = false,
    val messageTypeStr: String = "TEXT",
    val statusStr: String = "READ",
    val audioDuration: String? = null,
    val locationTitle: String? = null,
    val locationAddress: String? = null,
    val itemTitle: String? = null,
    val itemPricePerDay: Int? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreBooking(
    @DocumentId val id: String = "",
    val renterId: String = "",
    val renterName: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val daysCount: Int = 1,
    val pricePerDay: Int = 0,
    val totalAmount: Int = 0,
    val status: String = "Confirmed",
    val deliveryOption: String = "Self Pickup",
    val handoverOtp: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
