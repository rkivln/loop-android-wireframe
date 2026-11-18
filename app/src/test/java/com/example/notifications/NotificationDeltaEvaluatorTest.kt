package com.example.notifications

import com.example.data.models.BlockedUserEntry
import com.example.data.models.ChatConversation
import com.example.data.models.CommunityPostType
import com.example.data.models.NotificationPreferences
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.data.models.RentalOwner
import com.example.data.models.UserCommunityPost
import com.example.data.models.UserProfile
import com.example.data.models.UserRentalBooking
import com.example.data.models.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDeltaEvaluatorTest {

    private val currentUid = "user_me"
    private val otherUid = "user_other"

    private fun makeProfile(
        pushEnabled: Boolean = true,
        newPosts: Boolean = true,
        rsvps: Boolean = true,
        blockedIds: List<String> = emptyList()
    ) = UserProfile(
        uid = currentUid,
        displayName = "Aarav",
        settings = UserSettings(
            notifications = NotificationPreferences(
                pushEnabled = pushEnabled,
                newNeighbourhoodPosts = newPosts,
                likesAndInteractions = rsvps
            ),
            blockedUsers = blockedIds.map { BlockedUserEntry(userId = it, displayName = "Blocked") }
        )
    )

    private fun makeItem(
        id: String = "item_1",
        ownerId: String = currentUid,
        reviewCount: Int = 0,
        rating: Float = 0f
    ) = RentalItem(
        id = id,
        title = "Fujifilm X100V",
        category = RentalCategory.ELECTRONICS,
        pricePerDay = 900,
        location = "White Town",
        rating = rating,
        reviewCount = reviewCount,
        description = "Clean rangefinder",
        features = emptyList(),
        owner = RentalOwner(id = ownerId, name = "Aarav")
    )

    private fun makeConv(
        id: String = "conv_1",
        senderId: String = otherUid,
        text: String = "Is it free tomorrow?",
        unreadCount: Int = 1
    ) = ChatConversation(
        id = id,
        owner = RentalOwner(id = otherUid, name = "Maya"),
        itemContext = makeItem(ownerId = currentUid),
        lastSenderId = senderId,
        unreadCount = unreadCount,
        messages = listOf(
            RentalMessage(
                id = "m1",
                senderName = "Maya",
                text = text,
                timestamp = "Now",
                isFromMe = senderId == currentUid,
                senderId = senderId
            )
        )
    )

    @Test
    fun initialBaseline_suppressesHistoricalNotifications() {
        val result = NotificationDeltaEvaluator.evaluateDeltas(
            currentUid = currentUid,
            profile = makeProfile(),
            previousBaseline = null,
            alreadyNotifiedKeys = emptySet(),
            conversations = listOf(makeConv()),
            bookings = listOf(
                UserRentalBooking(
                    id = "b1",
                    item = makeItem(),
                    hostId = currentUid,
                    hostName = "Aarav",
                    renterId = otherUid,
                    renterName = "Maya",
                    daysRemaining = 2,
                    totalPaid = 1800,
                    status = "Confirmed",
                    handoverOtp = "4821"
                )
            ),
            communityPosts = listOf(
                UserCommunityPost(
                    id = "p1",
                    title = "Morning Photowalk",
                    type = CommunityPostType.EVENT,
                    description = "Meet at Promenade",
                    location = "Rock Beach",
                    dateTime = "Sat 7 AM",
                    attendeesOrResponses = "3 going",
                    authorId = otherUid,
                    authorName = "Maya"
                )
            ),
            ownedItems = listOf(makeItem())
        )

        assertTrue(result.events.isEmpty())
        assertTrue(result.updatedBaseline.seenPostIds.contains("p1"))
        assertTrue(result.updatedBaseline.bookingFingerprints.containsKey("b1"))
    }

    @Test
    fun incomingChatDelta_emitsUnlessChatCurrentlyOpenOrBlocked() {
        val initial = NotificationDeltaEvaluator.buildBaseline(
            currentUid = currentUid,
            conversations = emptyList(),
            bookings = emptyList(),
            communityPosts = emptyList(),
            ownedItems = emptyList()
        )
        val incoming = makeConv()

        // Active foreground open conversation suppresses push banner
        val suppressedOpen = NotificationDeltaEvaluator.evaluateDeltas(
            currentUid = currentUid,
            profile = makeProfile(),
            previousBaseline = initial,
            alreadyNotifiedKeys = emptySet(),
            conversations = listOf(incoming),
            bookings = emptyList(),
            communityPosts = emptyList(),
            ownedItems = emptyList(),
            activeOpenConversationId = "conv_1",
            isAppInForeground = true
        )
        assertTrue(suppressedOpen.events.isEmpty())

        // Blocked sender suppresses push banner
        val suppressedBlocked = NotificationDeltaEvaluator.evaluateDeltas(
            currentUid = currentUid,
            profile = makeProfile(blockedIds = listOf(otherUid)),
            previousBaseline = initial,
            alreadyNotifiedKeys = emptySet(),
            conversations = listOf(incoming),
            bookings = emptyList(),
            communityPosts = emptyList(),
            ownedItems = emptyList()
        )
        assertTrue(suppressedBlocked.events.isEmpty())

        // Background or another tab emits rich chat event with Conversation deep link
        val emitted = NotificationDeltaEvaluator.evaluateDeltas(
            currentUid = currentUid,
            profile = makeProfile(),
            previousBaseline = initial,
            alreadyNotifiedKeys = emptySet(),
            conversations = listOf(incoming),
            bookings = emptyList(),
            communityPosts = emptyList(),
            ownedItems = emptyList(),
            activeOpenConversationId = null,
            isAppInForeground = false
        )
        assertEquals(1, emitted.events.size)
        val alert = emitted.events.first()
        assertEquals(NotificationCategory.MESSAGE, alert.category)
        assertEquals(NotificationDeepLink.Conversation("conv_1"), alert.deepLink)
    }

    @Test
    fun bookingLifecycle_confirmedToActiveToCompletedAndReview_emitsPinPayoutAndReview() {
        val baseBooking = UserRentalBooking(
            id = "bk_42",
            item = makeItem(),
            hostId = currentUid,
            hostName = "Aarav",
            renterId = otherUid,
            renterName = "Maya",
            daysRemaining = 2,
            totalPaid = 2200,
            status = "Confirmed",
            handoverOtp = "7390"
        )

        val baseline0 = NotificationDeltaEvaluator.buildBaseline(
            currentUid = currentUid,
            conversations = emptyList(),
            bookings = emptyList(),
            communityPosts = emptyList(),
            ownedItems = emptyList()
        )

        // 1. New booking arrives -> host sees handover PIN + amount
        val resNew = NotificationDeltaEvaluator.evaluateDeltas(
            currentUid = currentUid,
            profile = makeProfile(),
            previousBaseline = baseline0,
            alreadyNotifiedKeys = emptySet(),
            conversations = emptyList(),
            bookings = listOf(baseBooking),
            communityPosts = emptyList(),
            ownedItems = emptyList()
        )
        assertEquals(1, resNew.events.size)
        assertTrue(resNew.events.first().body.contains("7390"))
        assertEquals(NotificationDeepLink.Booking("bk_42"), resNew.events.first().deepLink)

        // 2. PIN verified -> Active status alert
        val activeBooking = baseBooking.copy(status = "Active")
        val resActive = NotificationDeltaEvaluator.evaluateDeltas(
            currentUid = currentUid,
            profile = makeProfile(),
            previousBaseline = resNew.updatedBaseline,
            alreadyNotifiedKeys = resNew.events.map { it.eventKey }.toSet(),
            conversations = emptyList(),
            bookings = listOf(activeBooking),
            communityPosts = emptyList(),
            ownedItems = emptyList()
        )
        assertEquals(1, resActive.events.size)
        assertTrue(resActive.events.first().title.contains("Pickup Verified"))

        // 3. Returned + Reviewed -> host sees both payout and review alerts
        val completedReviewed = activeBooking.copy(
            status = "Completed",
            isCompleted = true,
            isReviewed = true,
            ratingGiven = 5,
            reviewComment = "Crisp lens!"
        )
        val resDone = NotificationDeltaEvaluator.evaluateDeltas(
            currentUid = currentUid,
            profile = makeProfile(),
            previousBaseline = resActive.updatedBaseline,
            alreadyNotifiedKeys = emptySet(),
            conversations = emptyList(),
            bookings = listOf(completedReviewed),
            communityPosts = emptyList(),
            ownedItems = emptyList()
        )
        assertEquals(2, resDone.events.size)
        assertTrue(resDone.events.any { it.title.contains("₹2200 Payout") })
        assertTrue(resDone.events.any { it.title.contains("5-Star Review") && it.body.contains("Crisp lens!") })
    }

    @Test
    fun communityPostAndRsvpDeltas_respectPreferencesAndEmitCommunityDeepLinks() {
        val ownPostInitial = UserCommunityPost(
            id = "post_own",
            title = "Weekend Studio Jam",
            type = CommunityPostType.STUDY_GROUP,
            description = "Bring tripods",
            location = "Auroville",
            dateTime = "Sun 4 PM",
            attendeesOrResponses = "1 going",
            attendeeIds = listOf(currentUid),
            authorId = currentUid,
            authorName = "Aarav"
        )
        val neighborPost = UserCommunityPost(
            id = "post_neighbor",
            title = "Sunset Cycle Loop",
            type = CommunityPostType.EVENT,
            description = "Coastal ride",
            location = "Promenade",
            dateTime = "Fri 5 PM",
            attendeesOrResponses = "2 going",
            attendeeIds = listOf(otherUid),
            authorId = otherUid,
            authorName = "Maya"
        )

        val baseline = NotificationDeltaEvaluator.buildBaseline(
            currentUid = currentUid,
            conversations = emptyList(),
            bookings = emptyList(),
            communityPosts = listOf(ownPostInitial),
            ownedItems = emptyList()
        )

        val ownPostWithNewRsvp = ownPostInitial.copy(
            attendeeIds = listOf(currentUid, otherUid),
            attendeesOrResponses = "2 going"
        )

        // When pushEnabled = false -> no events emitted
        val muted = NotificationDeltaEvaluator.evaluateDeltas(
            currentUid = currentUid,
            profile = makeProfile(pushEnabled = false),
            previousBaseline = baseline,
            alreadyNotifiedKeys = emptySet(),
            conversations = emptyList(),
            bookings = emptyList(),
            communityPosts = listOf(ownPostWithNewRsvp, neighborPost),
            ownedItems = emptyList()
        )
        assertTrue(muted.events.isEmpty())

        // When enabled -> emits 1 new neighbor post alert + 1 RSVP alert
        val enabled = NotificationDeltaEvaluator.evaluateDeltas(
            currentUid = currentUid,
            profile = makeProfile(pushEnabled = true),
            previousBaseline = baseline,
            alreadyNotifiedKeys = emptySet(),
            conversations = emptyList(),
            bookings = emptyList(),
            communityPosts = listOf(ownPostWithNewRsvp, neighborPost),
            ownedItems = emptyList()
        )
        assertEquals(2, enabled.events.size)
        assertTrue(enabled.events.any {
            it.category == NotificationCategory.COMMUNITY_POST &&
                it.deepLink == NotificationDeepLink.CommunityPost("post_neighbor")
        })
        assertTrue(enabled.events.any {
            it.category == NotificationCategory.COMMUNITY_RSVP &&
                it.deepLink == NotificationDeepLink.CommunityPost("post_own")
        })
    }
}
