package com.example.notifications

import com.example.data.models.ChatConversation
import com.example.data.models.UserCommunityPost
import com.example.data.models.UserRentalBooking
import com.example.data.models.RentalItem
import com.example.data.models.UserProfile

data class BookingFingerprint(
    val status: String,
    val handoverOtp: String,
    val isReviewed: Boolean,
    val ratingGiven: Int
)

data class NotificationBaseline(
    val conversationSignatures: Map<String, String> = emptyMap(),
    val bookingFingerprints: Map<String, BookingFingerprint> = emptyMap(),
    val seenPostIds: Set<String> = emptySet(),
    val postAttendeeCounts: Map<String, Int> = emptyMap(),
    val ownedItemReviewCounts: Map<String, Int> = emptyMap()
)

enum class NotificationCategory {
    MESSAGE,
    BOOKING_PIN,
    REVIEW_PAYOUT,
    COMMUNITY_POST,
    COMMUNITY_RSVP,
    ANNOUNCEMENT
}

data class NotificationAlertEvent(
    val eventKey: String,
    val category: NotificationCategory,
    val title: String,
    val body: String,
    val senderName: String? = null,
    val hidePreview: Boolean = false,
    val deepLink: NotificationDeepLink
)

data class DeltaEvaluationResult(
    val events: List<NotificationAlertEvent>,
    val updatedBaseline: NotificationBaseline
)

object NotificationDeltaEvaluator {

    fun buildBaseline(
        currentUid: String,
        conversations: List<ChatConversation>,
        bookings: List<UserRentalBooking>,
        communityPosts: List<UserCommunityPost>,
        ownedItems: List<RentalItem>
    ): NotificationBaseline {
        val convSigs = conversations.associate { conv ->
            conv.id to conversationSignature(conv)
        }
        val bookingFps = bookings.associate { b ->
            b.id to BookingFingerprint(
                status = b.status,
                handoverOtp = b.handoverOtp,
                isReviewed = b.isReviewed,
                ratingGiven = b.ratingGiven
            )
        }
        val postIds = communityPosts.map { it.id }.toSet()
        val rsvpCounts = communityPosts
            .filter { it.authorId == currentUid }
            .associate { it.id to it.attendeeIds.size }
        val itemReviewCounts = ownedItems
            .filter { it.owner.id == currentUid || it.owner.id.isBlank() }
            .associate { it.id to it.reviewCount }

        return NotificationBaseline(
            conversationSignatures = convSigs,
            bookingFingerprints = bookingFps,
            seenPostIds = postIds,
            postAttendeeCounts = rsvpCounts,
            ownedItemReviewCounts = itemReviewCounts
        )
    }

    fun evaluateDeltas(
        currentUid: String,
        profile: UserProfile,
        previousBaseline: NotificationBaseline?,
        alreadyNotifiedKeys: Set<String>,
        conversations: List<ChatConversation>,
        bookings: List<UserRentalBooking>,
        communityPosts: List<UserCommunityPost>,
        ownedItems: List<RentalItem>,
        activeOpenConversationId: String? = null,
        isAppInForeground: Boolean = false
    ): DeltaEvaluationResult {
        val nextBaseline = buildBaseline(
            currentUid = currentUid,
            conversations = conversations,
            bookings = bookings,
            communityPosts = communityPosts,
            ownedItems = ownedItems
        )

        if (previousBaseline == null || currentUid.isBlank()) {
            // Seed initial baseline silently so sign-in or cold restore does not flood old notifications.
            return DeltaEvaluationResult(events = emptyList(), updatedBaseline = nextBaseline)
        }

        val prefs = profile.settings.notifications
        if (!prefs.pushEnabled) {
            return DeltaEvaluationResult(events = emptyList(), updatedBaseline = nextBaseline)
        }

        val blockedIds = profile.settings.blockedUserIds
        val hideMessagePreviews = profile.settings.chatPrivacy.hideNotificationPreviews
        val events = mutableListOf<NotificationAlertEvent>()

        // 1. Direct Chat Messages
        conversations.forEach { conv ->
            val lastSenderId = conv.lastSenderId
            val lastText = conv.lastMessage?.text?.trim().orEmpty()
            val sig = conversationSignature(conv)
            val prevSig = previousBaseline.conversationSignatures[conv.id]
            val isViewingThisChat = isAppInForeground && activeOpenConversationId == conv.id

            if (sig != prevSig &&
                lastText.isNotBlank() &&
                lastSenderId.isNotBlank() &&
                lastSenderId != currentUid &&
                lastSenderId !in blockedIds &&
                conv.owner.id !in blockedIds &&
                !conv.isMutedFor(currentUid) &&
                conv.unreadCount > 0 &&
                !isViewingThisChat
            ) {
                val eventKey = "msg:${conv.id}:$sig"
                if (eventKey !in alreadyNotifiedKeys) {
                    val contextPrefix = conv.itemContext?.title ?: conv.communityPostTitle
                    val normalTitle = if (!contextPrefix.isNullOrBlank()) {
                        "${conv.owner.name} · $contextPrefix"
                    } else {
                        conv.owner.name
                    }
                    events += if (hideMessagePreviews) {
                        NotificationAlertEvent(
                            eventKey = eventKey,
                            category = NotificationCategory.MESSAGE,
                            title = "Studio Dispatch",
                            body = "New encrypted message · Tap to open",
                            senderName = null,
                            hidePreview = true,
                            deepLink = NotificationDeepLink.Conversation(conv.id)
                        )
                    } else {
                        NotificationAlertEvent(
                            eventKey = eventKey,
                            category = NotificationCategory.MESSAGE,
                            title = normalTitle,
                            body = lastText,
                            senderName = conv.owner.name,
                            hidePreview = false,
                            deepLink = NotificationDeepLink.Conversation(conv.id)
                        )
                    }
                }
            }
        }

        // 2. Booking Status, Handover PINs, Reviews & Host Payouts
        bookings.forEach { booking ->
            val prevFp = previousBaseline.bookingFingerprints[booking.id]
            val isHost = booking.hostId == currentUid
            val otherPartyId = if (isHost) booking.renterId else booking.hostId
            if (otherPartyId in blockedIds) return@forEach

            if (prevFp == null) {
                // Brand new reservation created while session or background sync is watching
                val eventKey = "booking_new:${booking.id}:${booking.status}"
                if (eventKey !in alreadyNotifiedKeys) {
                    if (isHost) {
                        val renterLabel = booking.renterName.ifBlank { "A neighbor" }
                        events += NotificationAlertEvent(
                            eventKey = eventKey,
                            category = NotificationCategory.BOOKING_PIN,
                            title = "New Booking: ${booking.item.title}",
                            body = "$renterLabel reserved for ${booking.daysRemaining}d (₹${booking.totalPaid}). Pickup PIN #${booking.handoverOtp}.",
                            deepLink = NotificationDeepLink.Booking(booking.id)
                        )
                    } else if (booking.renterId == currentUid) {
                        events += NotificationAlertEvent(
                            eventKey = eventKey,
                            category = NotificationCategory.BOOKING_PIN,
                            title = "Booking Confirmed: ${booking.item.title}",
                            body = "Share handover PIN #${booking.handoverOtp} with ${booking.hostName.ifBlank { "the host" }} at pickup.",
                            deepLink = NotificationDeepLink.Booking(booking.id)
                        )
                    }
                }
            } else {
                // Existing booking status transition
                if (!prevFp.status.equals(booking.status, ignoreCase = true)) {
                    val statusKey = "booking_status:${booking.id}:${booking.status.lowercase()}"
                    if (statusKey !in alreadyNotifiedKeys) {
                        when {
                            booking.status.equals("Active", ignoreCase = true) -> {
                                events += NotificationAlertEvent(
                                    eventKey = statusKey,
                                    category = NotificationCategory.BOOKING_PIN,
                                    title = "Pickup Verified · ${booking.item.title}",
                                    body = "Handover PIN #${booking.handoverOtp} confirmed. Rental is now active.",
                                    deepLink = NotificationDeepLink.Booking(booking.id)
                                )
                            }
                            booking.status.equals("Completed", ignoreCase = true) -> {
                                if (isHost) {
                                    events += NotificationAlertEvent(
                                        eventKey = statusKey,
                                        category = NotificationCategory.REVIEW_PAYOUT,
                                        title = "Return Complete · +₹${booking.totalPaid} Payout",
                                        body = "${booking.item.title} returned by ${booking.renterName.ifBlank { "renter" }}. Earnings added to your Studio Shelf.",
                                        deepLink = NotificationDeepLink.Booking(booking.id)
                                    )
                                } else {
                                    events += NotificationAlertEvent(
                                        eventKey = statusKey,
                                        category = NotificationCategory.BOOKING_PIN,
                                        title = "Rental Completed · ${booking.item.title}",
                                        body = "Gear return confirmed by ${booking.hostName.ifBlank { "host" }}. Tap to rate your experience.",
                                        deepLink = NotificationDeepLink.Booking(booking.id)
                                    )
                                }
                            }
                            booking.status.equals("Cancelled", ignoreCase = true) -> {
                                events += NotificationAlertEvent(
                                    eventKey = statusKey,
                                    category = NotificationCategory.BOOKING_PIN,
                                    title = "Booking Cancelled · ${booking.item.title}",
                                    body = "Reservation for ${booking.startDate.ifBlank { "scheduled dates" }} was cancelled and the item is unlocked.",
                                    deepLink = NotificationDeepLink.Booking(booking.id)
                                )
                            }
                        }
                    }
                }

                // New Review received by Host on a Completed booking
                if (isHost && !prevFp.isReviewed && booking.isReviewed) {
                    val reviewKey = "booking_review:${booking.id}:${booking.ratingGiven}"
                    if (reviewKey !in alreadyNotifiedKeys) {
                        val snippet = booking.reviewComment.trim().takeIf { it.isNotBlank() }?.let { " — \"$it\"" }.orEmpty()
                        events += NotificationAlertEvent(
                            eventKey = reviewKey,
                            category = NotificationCategory.REVIEW_PAYOUT,
                            title = "New ${booking.ratingGiven}-Star Review · ${booking.item.title}",
                            body = "${booking.renterName.ifBlank { "Borrower" }} left a ${booking.ratingGiven}-star rating$snippet",
                            deepLink = NotificationDeepLink.Booking(booking.id)
                        )
                    }
                }
            }
        }

        // Track item review increments as a secondary review signal if booking wasn't matched
        ownedItems.forEach { item ->
            val prevReviews = previousBaseline.ownedItemReviewCounts[item.id]
            if (prevReviews != null && item.reviewCount > prevReviews) {
                val matchingBooking = bookings.find {
                    it.hostId == currentUid && it.item.id == item.id && it.isReviewed
                }
                if (matchingBooking == null) {
                    val itemReviewKey = "item_review:${item.id}:${item.reviewCount}"
                    if (itemReviewKey !in alreadyNotifiedKeys) {
                        events += NotificationAlertEvent(
                            eventKey = itemReviewKey,
                            category = NotificationCategory.REVIEW_PAYOUT,
                            title = "New Review on ${item.title}",
                            body = "Rating updated to ${String.format("%.1f", item.rating)}/5 (${item.reviewCount} reviews).",
                            deepLink = NotificationDeepLink.Booking(item.activeBookingId ?: item.id)
                        )
                    }
                }
            }
        }

        // 3. Community Posts & RSVPs
        communityPosts.forEach { post ->
            if (post.authorId in blockedIds) return@forEach

            if (prefs.newNeighbourhoodPosts &&
                post.id !in previousBaseline.seenPostIds &&
                post.authorId != currentUid
            ) {
                val postKey = "post_new:${post.id}"
                if (postKey !in alreadyNotifiedKeys) {
                    events += NotificationAlertEvent(
                        eventKey = postKey,
                        category = NotificationCategory.COMMUNITY_POST,
                        title = "New ${post.type.title}: ${post.title}",
                        body = "${post.authorName.ifBlank { "A neighbor" }} posted in ${post.location.ifBlank { "your neighborhood" }}.",
                        deepLink = NotificationDeepLink.CommunityPost(post.id)
                    )
                }
            }

            if (prefs.likesAndInteractions && post.authorId == currentUid) {
                val prevCount = previousBaseline.postAttendeeCounts[post.id] ?: 0
                val newCount = post.attendeeIds.size
                if (newCount > prevCount) {
                    val rsvpKey = "post_rsvp:${post.id}:$newCount"
                    if (rsvpKey !in alreadyNotifiedKeys) {
                        events += NotificationAlertEvent(
                            eventKey = rsvpKey,
                            category = NotificationCategory.COMMUNITY_RSVP,
                            title = "New RSVP · ${post.title}",
                            body = "${post.attendeesOrResponses} · ${post.dateTime.ifBlank { "Upcoming" }}",
                            deepLink = NotificationDeepLink.CommunityPost(post.id)
                        )
                    }
                }
            }
        }

        return DeltaEvaluationResult(events = events, updatedBaseline = nextBaseline)
    }

    private fun conversationSignature(conv: ChatConversation): String {
        val lastTextHash = conv.lastMessage?.text?.trim().orEmpty().hashCode()
        return "${lastTextHash}_${conv.unreadCount}_${conv.lastSenderId}"
    }
}
