package com.example.data.repository

import com.example.R
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firebase.FirestoreBooking
import com.example.data.firebase.FirestoreService
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.data.models.RentalOwner
import com.example.data.models.SpecFeature
import com.example.data.models.UserCommunityPost
import com.example.data.models.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class FirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun userProfile_saveAndFavoritesUpdate_succeedsAndBlocksCrossUserWrite() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val service = FirestoreService(firestore)

        val profile = UserProfile(
            uid = aliceUid,
            displayName = "Alice Puducherry",
            email = ALICE_EMAIL,
            location = "White Town",
            favoriteItemIds = listOf("item_leica_m")
        )
        val saveRes = withTimeout(DEFAULT_TIMEOUT_MS) { service.saveUserProfile(profile) }
        assertTrue("Profile save failed: ${saveRes.exceptionOrNull()}", saveRes.isSuccess)

        val favRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            service.updateFavoriteItems(listOf("item_leica_m", "item_coastal_ebike"), profile)
        }
        assertTrue("Favorites update failed: ${favRes.exceptionOrNull()}", favRes.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            service.listenToUserProfile(aliceUid).first { it.favoriteItemIds.contains("item_coastal_ebike") }
        }
        assertEquals("Alice Puducherry", observed.displayName)

        // Cross-user read by Bob must succeed for member profile cards (displayName, bio, location, badges, rating)
        signInTestUser(BOB_EMAIL)
        val fetchedByBobRes = withTimeout(DEFAULT_TIMEOUT_MS) { service.getUserProfileOnce(aliceUid) }
        assertTrue("Bob reading Alice's profile card failed: ${fetchedByBobRes.exceptionOrNull()}", fetchedByBobRes.isSuccess)
        val fetchedByBob = fetchedByBobRes.getOrNull()
        assertEquals("Alice Puducherry", fetchedByBob?.displayName)
        assertEquals("White Town", fetchedByBob?.location)

        // Cross-user write by Bob must fail with PERMISSION_DENIED
        var denied = false
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .update("displayName", "Hacked Name").await()
            }
        } catch (e: Exception) {
            if (e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                denied = true
            }
        }
        assertTrue("Bob updating Alice's profile must be rejected with PERMISSION_DENIED", denied)
    }

    @Test
    fun userSettings_updateAndEnumValidation_enforced() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val service = FirestoreService(firestore)

        val starter = UserProfile(
            uid = aliceUid,
            displayName = "Alice Settings Tester",
            email = ALICE_EMAIL
        )
        val saveRes = withTimeout(DEFAULT_TIMEOUT_MS) { service.saveUserProfile(starter) }
        assertTrue("Starter profile save failed: ${saveRes.exceptionOrNull()}", saveRes.isSuccess)

        val updatedSettings = com.example.data.models.UserSettings(
            appearanceMode = com.example.data.models.AppearanceMode.DARK,
            profileVisibility = com.example.data.models.ProfileVisibility.PRIVATE,
            locationSharing = com.example.data.models.LocationSharingPreference.NEIGHBORHOOD_ONLY,
            notifications = com.example.data.models.NotificationPreferences(
                pushEnabled = true,
                newNeighbourhoodPosts = false,
                repliesToComments = true,
                likesAndInteractions = false,
                announcements = true
            ),
            blockedUsers = listOf(
                com.example.data.models.BlockedUserEntry(
                    userId = "blocked_user_99",
                    displayName = "Blocked Member",
                    blockedAtLabel = "Today"
                )
            )
        )
        val settingsRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            service.updateUserSettings(updatedSettings, starter)
        }
        assertTrue("Settings update failed: ${settingsRes.exceptionOrNull()}", settingsRes.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            service.listenToUserProfile(aliceUid).first {
                it.settings.appearanceMode == com.example.data.models.AppearanceMode.DARK &&
                    it.settings.profileVisibility == com.example.data.models.ProfileVisibility.PRIVATE
            }
        }
        assertEquals(
            com.example.data.models.LocationSharingPreference.NEIGHBORHOOD_ONLY,
            observed.settings.locationSharing
        )
        assertTrue(observed.settings.blockedUserIds.contains("blocked_user_99"))
        assertEquals(false, observed.settings.notifications.newNeighbourhoodPosts)

        // Invalid enum string must be rejected by Firestore security rules
        var invalidEnumDenied = false
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .update("appearanceMode", "INVALID_MODE").await()
            }
        } catch (e: Exception) {
            if (e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                invalidEnumDenied = true
            }
        }
        assertTrue("Invalid appearanceMode enum must be rejected with PERMISSION_DENIED", invalidEnumDenied)
    }

    @Test
    fun createAndListenItems_authenticatedSucceeds_crossUserAndUnauthDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val service = FirestoreService(firestore)

        val uniqueItemId = "item_${UUID.randomUUID()}"
        val item = RentalItem(
            id = uniqueItemId,
            title = "Canon EOS Camera & 50mm Lens",
            category = RentalCategory.ELECTRONICS,
            pricePerDay = 650,
            location = "White Town, Puducherry",
            rating = 4.9f,
            reviewCount = 12,
            description = "Mint condition 50mm photography kit.",
            primaryImageRes = 0,
            imageUrl = "data:image/jpeg;base64,COVER",
            imageUrls = listOf("data:image/jpeg;base64,COVER", "data:image/jpeg;base64,DETAIL"),
            imageCount = 2,
            owner = RentalOwner(id = aliceUid, name = "Alice Tester", badge = "Top Host", initials = "AT"),
            features = listOf(SpecFeature("CAMERA", "50mm Prime")),
            availableToday = true
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { service.createListing(item) }
        assertTrue("Item creation should succeed: ${result.exceptionOrNull()}", result.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            service.listenToItems().first { list -> list.any { it.id == uniqueItemId } }
        }
        val storedItem = emitted.first { it.id == uniqueItemId }
        assertEquals(2, storedItem.imageUrls.size)
        assertEquals("data:image/jpeg;base64,COVER", storedItem.imageUrl)

        // Cross-user update denied
        signInTestUser(BOB_EMAIL)
        var denied = false
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("items").document(uniqueItemId).update("title", "Stolen Gear").await()
            }
        } catch (e: Exception) {
            if (e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                denied = true
            }
        }
        assertTrue("Bob updating Alice's item must fail with PERMISSION_DENIED", denied)

        // Unauthenticated Flow listener denied
        auth.signOut()
        var unauthDenied = false
        try {
            withTimeout(FLOW_TIMEOUT_MS) { service.listenToItems().first() }
        } catch (e: Exception) {
            if (e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                unauthDenied = true
            }
        }
        assertTrue("Unauthenticated items listener must fail with PERMISSION_DENIED", unauthDenied)
    }

    @Test
    fun communityPosts_createAndListen_authenticatedSucceeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val service = FirestoreService(firestore)

        val uniquePostId = "post_${UUID.randomUUID()}"
        val post = UserCommunityPost(
            id = uniquePostId,
            title = "Auroville Forest Photowalk",
            type = CommunityPostType.EVENT,
            categoryName = "CREATIVE_PHOTO",
            description = "Morning photowalk capturing botanical diversity.",
            location = "Auroville Visitor Centre",
            dateTime = "Saturday · 6:30 AM",
            attendeesOrResponses = "5 creators attending",
            authorId = aliceUid,
            authorName = "Alice Tester",
            status = CommunityPostStatus.ACTIVE
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { service.createCommunityPost(post) }
        assertTrue(result.isSuccess)

        val posts = withTimeout(FLOW_TIMEOUT_MS) {
            service.listenToCommunityPosts().first { list -> list.any { it.id == uniquePostId } }
        }
        assertTrue(posts.any { it.title == "Auroville Forest Photowalk" })

        val purgeRes = withTimeout(DEFAULT_TIMEOUT_MS) { service.purgeAllItemsAndCommunityPosts() }
        assertTrue("Purge failed: ${purgeRes.exceptionOrNull()}", purgeRes.isSuccess)
        val remainingPosts = withTimeout(FLOW_TIMEOUT_MS) {
            service.listenToCommunityPosts().first { list -> list.none { it.id == uniquePostId } }
        }
        assertTrue(remainingPosts.none { it.id == uniquePostId })
    }

    @Test
    fun conversationsAndMessages_participantScopedAndImmutable_outsiderDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)

        // Sign back in as Alice to start conversation with Bob
        signInTestUser(ALICE_EMAIL)
        val aliceService = FirestoreService(firestore)
        val uniqueConvId = "conv_${UUID.randomUUID()}"
        val bobOwner = RentalOwner(id = bobUid, name = "Bob Host", initials = "BH")

        val convResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceService.createOrGetConversation(host = bobOwner, customConversationId = uniqueConvId)
        }
        assertTrue("Conversation creation failed: ${convResult.exceptionOrNull()}", convResult.isSuccess)

        // Alice can list participant conversations and send message
        val aliceConvs = withTimeout(FLOW_TIMEOUT_MS) {
            aliceService.listenToConversations(aliceUid).first { list -> list.any { it.id == uniqueConvId } }
        }
        assertTrue(aliceConvs.any { it.id == uniqueConvId })

        val msgId = "msg_${UUID.randomUUID()}"
        val msgResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceService.sendMessage(
                uniqueConvId,
                RentalMessage(
                    id = msgId,
                    senderName = "Alice",
                    text = "Is this lens free this weekend?",
                    timestamp = "Just now",
                    isFromMe = true,
                    senderId = aliceUid
                )
            )
        }
        assertTrue("Message send failed: ${msgResult.exceptionOrNull()}", msgResult.isSuccess)

        val msgs = withTimeout(FLOW_TIMEOUT_MS) {
            aliceService.listenToMessages(uniqueConvId).first { list -> list.any { it.id == msgId } }
        }
        assertEquals("Is this lens free this weekend?", msgs.first { it.id == msgId }.text)

        // Messages are append-only: update must fail
        var updateDenied = false
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("conversations").document(uniqueConvId)
                    .collection("messages").document(msgId)
                    .update("text", "Edited message").await()
            }
        } catch (e: Exception) {
            if (e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                updateDenied = true
            }
        }
        assertTrue("Updating immutable message must fail with PERMISSION_DENIED", updateDenied)

        // Outsider Charlie cannot read conversation or send message
        signInTestUser(CHARLIE_EMAIL)
        var outsiderReadDenied = false
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("conversations").document(uniqueConvId).get().await()
            }
        } catch (e: Exception) {
            if (e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                outsiderReadDenied = true
            }
        }
        assertTrue("Non-participant reading conversation must fail with PERMISSION_DENIED", outsiderReadDenied)
    }

    @Test
    fun bookings_renterCreateHostUpdate_outsiderDenied() = runBlocking {
        val bobUid = signInTestUser(BOB_EMAIL)
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceService = FirestoreService(firestore)

        val uniqueBookingId = "book_${UUID.randomUUID()}"
        val booking = FirestoreBooking(
            id = uniqueBookingId,
            renterId = aliceUid,
            renterName = "Alice Renter",
            hostId = bobUid,
            hostName = "Bob Host",
            itemId = "item_leica_m",
            itemTitle = "Canon EOS & 35mm Glass",
            startDate = "Nov 18, 2026",
            endDate = "Nov 21, 2026",
            daysCount = 3,
            pricePerDay = 750,
            totalAmount = 2300,
            status = "Confirmed",
            handoverOtp = "4291"
        )

        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) { aliceService.createBooking(booking) }
        assertTrue("Booking creation failed: ${createRes.exceptionOrNull()}", createRes.isSuccess)

        val aliceBookings = withTimeout(FLOW_TIMEOUT_MS) {
            aliceService.listenToBookings(aliceUid).first { list -> list.any { it.id == uniqueBookingId } }
        }
        assertTrue(aliceBookings.any { it.id == uniqueBookingId })

        // Host Bob can see and update status to Completed
        signInTestUser(BOB_EMAIL)
        val bobService = FirestoreService(firestore)
        val updateRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobService.updateBookingStatus(uniqueBookingId, "Completed")
        }
        assertTrue("Host updating booking status failed: ${updateRes.exceptionOrNull()}", updateRes.isSuccess)

        // Outsider Charlie cannot read booking
        signInTestUser(CHARLIE_EMAIL)
        var charlieDenied = false
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("bookings").document(uniqueBookingId).get().await()
            }
        } catch (e: Exception) {
            if (e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                charlieDenied = true
            }
        }
        assertTrue("Third-party reading booking must fail with PERMISSION_DENIED", charlieDenied)
    }

    @Test
    fun interconnectedFlows_itemBookingLockReviewAndRsvp_enforced() = runBlocking {
        val bobUid = signInTestUser(BOB_EMAIL)
        val bobService = FirestoreService(firestore)
        val uniqueItemId = "item_${UUID.randomUUID()}"
        val bobItem = RentalItem(
            id = uniqueItemId,
            title = "Bob Studio Mic",
            category = RentalCategory.ELECTRONICS,
            pricePerDay = 400,
            location = "White Town",
            rating = 0f,
            reviewCount = 0,
            description = "Studio microphone",
            features = emptyList(),
            owner = RentalOwner(id = bobUid, name = "Bob Host", initials = "BH"),
            availableToday = true
        )
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { bobService.createListing(bobItem) }.isSuccess)

        val uniquePostId = "post_${UUID.randomUUID()}"
        val bobPost = UserCommunityPost(
            id = uniquePostId,
            title = "Audio Meetup",
            type = CommunityPostType.EVENT,
            description = "Bring field recorders.",
            location = "Rock Beach",
            dateTime = "Sunday 7 AM",
            attendeesOrResponses = "1 attending",
            authorId = bobUid,
            authorName = "Bob Host",
            status = CommunityPostStatus.ACTIVE
        )
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { bobService.createCommunityPost(bobPost) }.isSuccess)

        // Sign in as Alice (borrower + attendee)
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceService = FirestoreService(firestore)

        // Self-conversation & self-booking rejected by rules
        val selfConvRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceService.createOrGetConversation(
                host = RentalOwner(id = aliceUid, name = "Alice", initials = "A"),
                customConversationId = "conv_self_${UUID.randomUUID()}"
            )
        }
        assertTrue("Self-conversation must be rejected", selfConvRes.isFailure)

        val selfBookingRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceService.createBooking(
                FirestoreBooking(
                    id = "book_self_${UUID.randomUUID()}",
                    renterId = aliceUid,
                    hostId = aliceUid,
                    itemId = uniqueItemId,
                    totalAmount = 450,
                    status = "Confirmed",
                    handoverOtp = "1111"
                )
            )
        }
        assertTrue("Self-booking must be rejected", selfBookingRes.isFailure)

        // Alice creates valid booking and locks Bob's item
        val bookingId = "book_${UUID.randomUUID()}"
        val bookRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceService.createBooking(
                FirestoreBooking(
                    id = bookingId,
                    renterId = aliceUid,
                    renterName = "Alice",
                    hostId = bobUid,
                    hostName = "Bob Host",
                    itemId = uniqueItemId,
                    itemTitle = bobItem.title,
                    totalAmount = 450,
                    status = "Confirmed",
                    handoverOtp = "7392"
                )
            )
        }
        assertTrue("Valid booking must succeed", bookRes.isSuccess)
        val lockRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceService.setItemBookingLock(uniqueItemId, availableToday = false, activeBookingId = bookingId)
        }
        assertTrue("Renter locking item failed: ${lockRes.exceptionOrNull()}", lockRes.isSuccess)

        // Alice RSVPs to Bob's post
        val rsvpRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceService.updateCommunityPostRsvp(uniquePostId, "1 creator attending", listOf(aliceUid))
        }
        assertTrue("Alice RSVP failed: ${rsvpRes.exceptionOrNull()}", rsvpRes.isSuccess)

        // Host verifies handover (Active), completes (Completed), unlocks item
        signInTestUser(BOB_EMAIL)
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { bobService.updateBookingStatus(bookingId, "Active") }.isSuccess)
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { bobService.updateBookingStatus(bookingId, "Completed") }.isSuccess)
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { bobService.setItemBookingLock(uniqueItemId, availableToday = true, activeBookingId = null) }.isSuccess)

        // Borrower submits review & updates item rating
        signInTestUser(ALICE_EMAIL)
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { aliceService.submitBookingReview(bookingId, 5, "Great mic!") }.isSuccess)
        val ratingRes = withTimeout(DEFAULT_TIMEOUT_MS) { aliceService.applyItemRatingReview(uniqueItemId, 5) }
        assertTrue("Rating update failed: ${ratingRes.exceptionOrNull()}", ratingRes.isSuccess)
        assertEquals(5.0f, ratingRes.getOrThrow().first)
        assertEquals(1, ratingRes.getOrThrow().second)
    }

    @Test
    fun atomicBookingLifecycleTransactions_createVerifyCompleteCancelAndReview_enforced() = runBlocking {
        val bobUid = signInTestUser(BOB_EMAIL)
        val bobService = FirestoreService(firestore)
        val itemId = "item_tx_${UUID.randomUUID()}"
        val item = RentalItem(
            id = itemId,
            title = "Transactional FX3 Kit",
            category = RentalCategory.ELECTRONICS,
            pricePerDay = 900,
            location = "Auroville",
            rating = 0f,
            reviewCount = 0,
            description = "Cinema camera body",
            features = emptyList(),
            owner = RentalOwner(id = bobUid, name = "Bob Host", initials = "BH"),
            availableToday = true
        )
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { bobService.createListing(item) }.isSuccess)

        // 1. Alice atomically creates booking + locks item
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceService = FirestoreService(firestore)
        val bookingId = "book_tx_${UUID.randomUUID()}"
        val createTx = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceService.createBookingTransaction(
                FirestoreBooking(
                    id = bookingId,
                    renterId = aliceUid,
                    renterName = "Alice",
                    hostId = bobUid,
                    hostName = "Bob Host",
                    itemId = itemId,
                    itemTitle = item.title,
                    startDate = "Nov 18, 2026",
                    endDate = "Nov 20, 2026",
                    daysCount = 2,
                    pricePerDay = 900,
                    totalAmount = 1850,
                    status = "Confirmed",
                    handoverOtp = "8426"
                )
            )
        }
        assertTrue("Atomic booking tx failed: ${createTx.exceptionOrNull()}", createTx.isSuccess)

        // Item must be locked to bookingId
        val lockedSnap = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("items").document(itemId).get().await()
        }
        assertEquals(false, lockedSnap.getBoolean("availableToday"))
        assertEquals(bookingId, lockedSnap.getString("activeBookingId"))

        // 2. Concurrent borrower Charlie must fail double-booking transaction
        val charlieUid = signInTestUser(CHARLIE_EMAIL)
        val charlieService = FirestoreService(firestore)
        val concurrentTx = withTimeout(DEFAULT_TIMEOUT_MS) {
            charlieService.createBookingTransaction(
                FirestoreBooking(
                    id = "book_race_${UUID.randomUUID()}",
                    renterId = charlieUid,
                    renterName = "Charlie",
                    hostId = bobUid,
                    hostName = "Bob Host",
                    itemId = itemId,
                    itemTitle = item.title,
                    totalAmount = 950,
                    status = "Confirmed",
                    handoverOtp = "1234"
                )
            )
        }
        assertTrue("Concurrent booking on locked item must fail", concurrentTx.isFailure)

        // 3. Host Bob verifies wrong PIN (fails), then right PIN (succeeds -> Active)
        signInTestUser(BOB_EMAIL)
        val wrongPinTx = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobService.verifyHandoverPinTransaction(bookingId, "0000")
        }
        assertTrue("Wrong PIN must fail", wrongPinTx.isFailure)

        val rightPinTx = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobService.verifyHandoverPinTransaction(bookingId, "8426")
        }
        assertTrue("Valid PIN verification failed: ${rightPinTx.exceptionOrNull()}", rightPinTx.isSuccess)
        assertEquals("Active", rightPinTx.getOrThrow().status)

        // 4. Host Bob completes booking -> status Completed + item unlocked
        val completeTx = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobService.completeBookingTransaction(bookingId)
        }
        assertTrue("Complete booking tx failed: ${completeTx.exceptionOrNull()}", completeTx.isSuccess)
        val unlockedSnap = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("items").document(itemId).get().await()
        }
        assertEquals(true, unlockedSnap.getBoolean("availableToday"))
        assertEquals(null, unlockedSnap.getString("activeBookingId"))

        // 5. Alice reviews transactionally (sets isReviewed + updates item rating) and cannot review twice
        signInTestUser(ALICE_EMAIL)
        val reviewTx = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceService.submitBookingReviewTransaction(bookingId, 5, "Spotless kit")
        }
        assertTrue("Atomic review failed: ${reviewTx.exceptionOrNull()}", reviewTx.isSuccess)
        assertEquals(5.0f, reviewTx.getOrThrow().first)
        assertEquals(1, reviewTx.getOrThrow().second)

        val duplicateReviewTx = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceService.submitBookingReviewTransaction(bookingId, 4, "Second try")
        }
        assertTrue("Duplicate review must be rejected", duplicateReviewTx.isFailure)

        // 6. Charlie books unlocked item and cancels -> unlocks item cleanly
        signInTestUser(CHARLIE_EMAIL)
        val charlieBookId = "book_cancel_${UUID.randomUUID()}"
        assertTrue(
            withTimeout(DEFAULT_TIMEOUT_MS) {
                charlieService.createBookingTransaction(
                    FirestoreBooking(
                        id = charlieBookId,
                        renterId = charlieUid,
                        renterName = "Charlie",
                        hostId = bobUid,
                        hostName = "Bob Host",
                        itemId = itemId,
                        itemTitle = item.title,
                        totalAmount = 950,
                        status = "Confirmed",
                        handoverOtp = "5555"
                    )
                )
            }.isSuccess
        )
        val cancelTx = withTimeout(DEFAULT_TIMEOUT_MS) {
            charlieService.cancelBookingTransaction(charlieBookId)
        }
        assertTrue("Cancel tx failed: ${cancelTx.exceptionOrNull()}", cancelTx.isSuccess)
        assertEquals("Cancelled", cancelTx.getOrThrow().status)
        val postCancelSnap = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("items").document(itemId).get().await()
        }
        assertEquals(true, postCancelSnap.getBoolean("availableToday"))
        assertEquals(null, postCancelSnap.getString("activeBookingId"))
    }

    private companion object {
        const val ALICE_EMAIL = "alice@looptest.com"
        const val BOB_EMAIL = "bob@looptest.com"
        const val CHARLIE_EMAIL = "charlie@looptest.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
