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
    val e2eePublicKey: String = "",
    val e2eeKeyFingerprint: String = "",
    val sendReadReceipts: Boolean = true,
    val showTypingStatus: Boolean = true,
    val defaultDisappearingSeconds: Long = 0L,
    val hideNotificationPreviews: Boolean = false,
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
    val activeBookingId: String? = null,
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
    val attendeeIds: List<String> = emptyList(),
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
    val houseId: String? = null,
    val houseTitle: String? = null,
    val houseMonthlyRent: Int? = null,
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
    val imageUrl: String? = null,
    val locationTitle: String? = null,
    val locationAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val itemTitle: String? = null,
    val itemPricePerDay: Int? = null,
    val isEncrypted: Boolean = false,
    val keyVersion: Int = 1,
    val ciphertext: String? = null,
    val iv: String? = null,
    val senderPublicKey: String? = null,
    val recipientPublicKey: String? = null,
    val replyToMessageId: String? = null,
    val replyToSenderName: String? = null,
    val expiresAtEpoch: Long = 0L,
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
    val isReviewed: Boolean = false,
    val ratingGiven: Int = 0,
    val reviewComment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreHouseListing(
    @DocumentId val id: String = "",
    val title: String = "",
    val propertyType: String = "APARTMENT",
    val bhkCount: Int = 2,
    val bathrooms: Int = 1,
    val areaSqFt: Int = 950,
    val floorNumber: Int = 1,
    val totalFloors: Int = 2,
    val furnishing: String = "SEMI_FURNISHED",
    val tenantPreference: String = "ANY",
    val monthlyRent: Int = 0,
    val securityDeposit: Int = 0,
    val maintenanceMonthly: Int = 0,
    val minLeaseMonths: Int = 11,
    val availableFromLabel: String = "Immediate",
    val availableForRent: Boolean = true,
    val location: String = "",
    val fullAddressNote: String? = null,
    val description: String = "",
    val amenities: List<String> = emptyList(),
    val imageUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val photoLabels: List<String> = emptyList(),
    val imageCount: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerInitials: String = "",
    val ownerBadge: String = "Verified Host",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreHouseVisitRequest(
    @DocumentId val id: String = "",
    val houseId: String = "",
    val houseTitle: String = "",
    val houseLocation: String = "",
    val monthlyRent: Int = 0,
    val tenantId: String = "",
    val tenantName: String = "",
    val landlordId: String = "",
    val landlordName: String = "",
    val preferredDate: String = "",
    val preferredTimeSlot: String = "MORNING",
    val occupantsCount: Int = 1,
    val householdType: String = "ANY",
    val targetMoveInMonth: String = "Immediate",
    val tenantNote: String = "",
    val landlordResponseNote: String = "",
    val confirmedAddressNote: String = "",
    val status: String = "REQUESTED",
    val conversationId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

