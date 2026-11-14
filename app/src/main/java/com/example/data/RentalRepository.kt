package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.firebase.FirestoreBooking
import com.example.data.firebase.FirestoreService
import com.example.data.models.ChatConversation
import com.example.data.models.ChatMessageType
import com.example.data.models.CommunityEvent
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.CuratedWalkingRoute
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType
import com.example.data.models.MapCategoryFilter
import com.example.data.models.MessageStatus
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.data.models.RentalOwner
import com.example.data.models.SpecFeature
import com.example.data.models.UserActivityItem
import com.example.data.models.UserActivityType
import com.example.data.models.UserCommunityPost
import com.example.data.models.UserProfile
import com.example.data.models.UserRentalBooking
import com.example.data.repository.CommunityEventRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object RentalRepository {
    private const val TAG = "RentalRepository"

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var firestoreService: FirestoreService? = null
    private var communityRepo: CommunityEventRepository? = null
    private var activeSessionUid: String? = null
    private var syncJobs = mutableListOf<Job>()
    private var messageListenerJob: Job? = null

    val defaultOwner = RentalOwner(
        id = "owner_1",
        name = "Rakesh Kumar",
        badge = "Top Maker",
        initials = "RK",
        memberSince = "Host since 2024",
        rating = 4.9f,
        totalRentals = 48,
        responseTime = "< 10 mins",
        phone = "+91 98401 22345"
    )

    val ownerPriya = RentalOwner(
        id = "owner_2",
        name = "Priya Sharma",
        badge = "Verified Creator",
        initials = "PS",
        memberSince = "Host since 2024",
        rating = 4.8f,
        totalRentals = 22,
        responseTime = "< 5 mins",
        phone = "+91 97890 54321"
    )

    val ownerVikram = RentalOwner(
        id = "owner_3",
        name = "Vikram David",
        badge = "Studio Superhost",
        initials = "VD",
        memberSince = "Host since 2023",
        rating = 4.9f,
        totalRentals = 64,
        responseTime = "< 15 mins",
        phone = "+91 94432 19876"
    )

    val ownerAnanya = RentalOwner(
        id = "owner_4",
        name = "Ananya Nair",
        badge = "Curator",
        initials = "AN",
        memberSince = "Host since 2024",
        rating = 4.9f,
        totalRentals = 35,
        responseTime = "< 10 mins",
        phone = "+91 99520 87654"
    )

    val items: List<RentalItem> = emptyList()

    val initialConversations: List<ChatConversation> = emptyList()
    val sampleMessages: List<RentalMessage> = emptyList()

    val discoveryPins: List<DiscoveryPinItem> = emptyList()

    val curatedRoutes: List<CuratedWalkingRoute> = emptyList()

    val initialUserProfile = UserProfile(
        uid = "",
        displayName = "Loop Member",
        email = "",
        phone = "",
        bio = "Creator and photographer in Puducherry.",
        location = "White Town, Puducherry",
        avatarPresetIndex = 0,
        rating = 5.0f,
        reviewCount = 0,
        rentalsCompleted = 0,
        listingsCount = 0,
        earnedAmount = 0,
        isVerified = true,
        memberSince = "Joined 2026",
        badges = listOf("✓ Google Verified", "★ Loop Member")
    )

    val initialUserActivities: List<UserActivityItem> = emptyList()

    val initialCommunityPosts: List<UserCommunityPost> = emptyList()

    private val _itemsFlow = MutableStateFlow(items)
    val itemsFlow: StateFlow<List<RentalItem>> = _itemsFlow.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<RentalCategory?>(null)
    val selectedCategory: StateFlow<RentalCategory?> = _selectedCategory.asStateFlow()

    val filteredItems: StateFlow<List<RentalItem>> = combine(
        _itemsFlow,
        _searchQuery,
        _selectedCategory
    ) { allItems, query, cat ->
        allItems.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true) ||
                item.location.contains(query, ignoreCase = true)
            val matchesCategory = cat == null || item.category == cat
            matchesQuery && matchesCategory
        }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, items)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: RentalCategory?) {
        _selectedCategory.value = cat
    }

    private val _conversationsFlow = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversationsFlow: StateFlow<List<ChatConversation>> = _conversationsFlow.asStateFlow()

    private val _activeConversationMessagesFlow = MutableStateFlow<Map<String, List<RentalMessage>>>(emptyMap())
    val activeConversationMessagesFlow: StateFlow<Map<String, List<RentalMessage>>> =
        _activeConversationMessagesFlow.asStateFlow()

    private val _bookingsFlow = MutableStateFlow<List<UserRentalBooking>>(emptyList())
    val bookingsFlow: StateFlow<List<UserRentalBooking>> = _bookingsFlow.asStateFlow()

    private val _discoveryPinsFlow = MutableStateFlow(discoveryPins)
    val discoveryPinsFlow: StateFlow<List<DiscoveryPinItem>> = _discoveryPinsFlow.asStateFlow()

    private val _userProfileFlow = MutableStateFlow(initialUserProfile)
    val userProfileFlow: StateFlow<UserProfile> = _userProfileFlow.asStateFlow()

    private val _userActivityFlow = MutableStateFlow<List<UserActivityItem>>(emptyList())
    val userActivityFlow: StateFlow<List<UserActivityItem>> = _userActivityFlow.asStateFlow()

    private val _userCommunityPostsFlow = MutableStateFlow(initialCommunityPosts)
    val userCommunityPostsFlow: StateFlow<List<UserCommunityPost>> = _userCommunityPostsFlow.asStateFlow()

    private val _syncErrorFlow = MutableStateFlow<String?>(null)
    val syncErrorFlow: StateFlow<String?> = _syncErrorFlow.asStateFlow()

    val communityEventsFlow: StateFlow<List<CommunityEvent>> = _userCommunityPostsFlow.map { posts ->
        posts.map { post ->
            CommunityEvent(
                id = post.id,
                title = post.title,
                description = post.description,
                category = post.categoryName,
                locationName = post.location,
                latitude = post.latitude,
                longitude = post.longitude,
                timeString = post.dateTime,
                organizerName = post.authorName,
                participantCount = Regex("(\\d+)").find(post.attendeesOrResponses)?.value?.toIntOrNull() ?: 1
            )
        }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    fun startSession(context: Context, uid: String) {
        val currentUser = Firebase.auth.currentUser ?: return
        if (activeSessionUid == uid && syncJobs.any { it.isActive }) return

        clearSession()
        activeSessionUid = uid
        _syncErrorFlow.value = null

        val service = FirestoreService.getInstance(context)
        firestoreService = service
        val cRepo = CommunityEventRepository.getInstance(context)
        communityRepo = cRepo
        cRepo.startAuthenticatedSync()

        // Ensure user's profile doc exists in Firestore
        repositoryScope.launch {
            val starterProfile = UserProfile(
                uid = uid,
                displayName = currentUser.displayName ?: "Loop Member",
                email = currentUser.email ?: "member@loop.in",
                location = "White Town, Puducherry",
                memberSince = "Joined 2026"
            )
            val existingDoc = try {
                service.firestore.collection("users").document(uid).get().await()
            } catch (_: Exception) {
                null
            }
            if (existingDoc == null || !existingDoc.exists()) {
                service.saveUserProfile(starterProfile)
            }
        }

        // 1. User Profile real-time sync
        syncJobs += repositoryScope.launch {
            try {
                service.listenToUserProfile(uid).collect { cloudProfile ->
                    _userProfileFlow.value = cloudProfile
                    val favIds = cloudProfile.favoriteItemIds.toSet()
                    _itemsFlow.value = _itemsFlow.value.map { item ->
                        item.copy(isFavorite = item.id in favIds)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Profile sync error: ${e.message}", e)
                _syncErrorFlow.value = "Profile sync error: ${e.localizedMessage}"
            }
        }

        // 2. Gear Listings real-time sync
        syncJobs += repositoryScope.launch {
            try {
                service.listenToItems().collect { cloudItems ->
                    val favIds = _userProfileFlow.value.favoriteItemIds.toSet()
                    val merged = cloudItems.map { item ->
                        item.copy(isFavorite = item.id in favIds)
                    }
                    _itemsFlow.value = merged
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Items sync error: ${e.message}", e)
                _syncErrorFlow.value = "Gear catalog sync error: ${e.localizedMessage}"
            }
        }

        // 3. Community Posts / Events sync (Room + Firestore)
        syncJobs += repositoryScope.launch {
            try {
                cRepo.allEvents.collect { events ->
                    if (events.isNotEmpty()) {
                        _userCommunityPostsFlow.value = events
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Community sync error: ${e.message}", e)
            }
        }

        // 4. Participant-scoped Conversations sync
        syncJobs += repositoryScope.launch {
            try {
                service.listenToConversations(uid, _itemsFlow.value).collect { cloudConvs ->
                    val currentMap = _activeConversationMessagesFlow.value
                    _conversationsFlow.value = cloudConvs.map { conv ->
                        val cachedMsgs = currentMap[conv.id]
                        if (!cachedMsgs.isNullOrEmpty()) conv.copy(messages = cachedMsgs) else conv
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Conversations sync error: ${e.message}", e)
                _syncErrorFlow.value = "Chat sync error: ${e.localizedMessage}"
            }
        }

        // 5. Bookings real-time sync (renter or host)
        syncJobs += repositoryScope.launch {
            try {
                service.listenToBookings(uid, _itemsFlow.value).collect { cloudBookings ->
                    _bookingsFlow.value = cloudBookings
                    val bookingActivities = cloudBookings.map { booking ->
                        UserActivityItem(
                            id = "act_${booking.id}",
                            title = "${booking.item.title} (${booking.status})",
                            subtitle = "Host: ${booking.hostName} · ₹${booking.totalPaid} · OTP #${booking.handoverOtp}",
                            type = UserActivityType.RESERVATION,
                            timestamp = "${booking.startDate} – ${booking.endDate}",
                            statusBadge = booking.status,
                            iconEmoji = "📦",
                            relatedId = booking.id
                        )
                    }
                    _userActivityFlow.value = bookingActivities
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Bookings sync error: ${e.message}", e)
                _syncErrorFlow.value = "Bookings sync error: ${e.localizedMessage}"
            }
        }
    }

    fun observeConversationMessages(conversationId: String) {
        val service = firestoreService ?: return
        if (Firebase.auth.currentUser == null) return
        messageListenerJob?.cancel()
        messageListenerJob = repositoryScope.launch {
            try {
                service.listenToMessages(conversationId).collect { msgs ->
                    _activeConversationMessagesFlow.value =
                        _activeConversationMessagesFlow.value + (conversationId to msgs)
                    _conversationsFlow.value = _conversationsFlow.value.map { conv ->
                        if (conv.id == conversationId) conv.copy(messages = msgs, unreadCount = 0) else conv
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Messages listener error: ${e.message}", e)
                _syncErrorFlow.value = "Unable to load messages: ${e.localizedMessage}"
            }
        }
    }

    fun clearSession() {
        syncJobs.forEach { it.cancel() }
        syncJobs.clear()
        messageListenerJob?.cancel()
        messageListenerJob = null
        communityRepo?.stopSync()
        activeSessionUid = null
        _conversationsFlow.value = emptyList()
        _activeConversationMessagesFlow.value = emptyMap()
        _bookingsFlow.value = emptyList()
        _userActivityFlow.value = emptyList()
        _userProfileFlow.value = initialUserProfile
        _itemsFlow.value = items
        _syncErrorFlow.value = null
    }

    fun clearSyncError() {
        _syncErrorFlow.value = null
    }

    suspend fun openOrCreateConversation(
        host: RentalOwner,
        item: RentalItem? = null
    ): Result<ChatConversation> {
        val service = firestoreService
            ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        return service.createOrGetConversation(host = host, item = item)
    }

    fun updateUserProfile(profile: UserProfile) {
        _userProfileFlow.value = profile
        repositoryScope.launch {
            firestoreService?.saveUserProfile(profile)?.onFailure {
                _syncErrorFlow.value = "Failed to save profile: ${it.localizedMessage}"
            }
        }
    }

    fun sendMessage(
        conversationId: String,
        text: String,
        messageType: ChatMessageType = ChatMessageType.TEXT,
        locationTitle: String? = null,
        locationAddress: String? = null
    ) {
        val currentUid = Firebase.auth.currentUser?.uid ?: return
        val senderDisplay = _userProfileFlow.value.displayName.ifBlank {
            Firebase.auth.currentUser?.displayName ?: "Me"
        }
        val newMsg = RentalMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = senderDisplay,
            text = text,
            timestamp = "Just now",
            isFromMe = true,
            senderId = currentUid,
            messageType = messageType,
            status = MessageStatus.SENT,
            locationTitle = locationTitle,
            locationAddress = locationAddress
        )

        repositoryScope.launch {
            firestoreService?.sendMessage(conversationId, newMsg)?.onFailure {
                _syncErrorFlow.value = "Message failed to send: ${it.localizedMessage}"
            }
        }
    }

    suspend fun createListing(
        title: String,
        price: Int,
        deposit: Int = 0,
        location: String = "White Town, Puducherry",
        category: RentalCategory,
        desc: String,
        imageUrl: String? = null
    ): Result<RentalItem> {
        val service = firestoreService
            ?: return Result.failure(IllegalStateException("Sign in required to publish gear."))
        val uid = Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))

        val profile = _userProfileFlow.value
        val newItem = RentalItem(
            id = "item_${System.currentTimeMillis()}",
            title = title.trim(),
            category = category,
            pricePerDay = price.coerceAtLeast(1),
            location = location.trim().ifBlank { "White Town, Puducherry" },
            rating = 5.0f,
            reviewCount = 1,
            description = desc.trim().ifBlank { "Curated creator equipment available in Puducherry." },
            primaryImageRes = when (category) {
                RentalCategory.ELECTRONICS -> R.drawable.img_cafe
                RentalCategory.STUDY_OFFICE -> R.drawable.img_synth
                RentalCategory.FURNITURE, RentalCategory.HOME_LIVING -> R.drawable.img_street
                RentalCategory.VEHICLES -> R.drawable.img_ebike
                RentalCategory.EVENTS -> R.drawable.img_street
                RentalCategory.TOOLS -> R.drawable.img_synth
            },
            imageUrl = imageUrl,
            imageCount = 3,
            features = listOf(
                SpecFeature("CAMERA", "Verified Object"),
                SpecFeature("BAG", "Accessories Included")
            ),
            owner = RentalOwner(
                id = uid,
                name = profile.displayName.ifBlank { "Loop Host" },
                badge = "Loop Host",
                initials = profile.initials
            ),
            isPopular = true,
            isFavorite = false,
            availableToday = true,
            securityDeposit = deposit.coerceAtLeast(0)
        )

        return service.createListing(newItem).onSuccess { savedItem ->
            _itemsFlow.value = listOf(savedItem) + _itemsFlow.value.filterNot { it.id == savedItem.id }
        }.onFailure {
            _syncErrorFlow.value = "Failed to publish listing: ${it.localizedMessage}"
        }
    }

    suspend fun createBooking(
        item: RentalItem,
        startDate: String,
        endDate: String,
        daysCount: Int = 3,
        deliveryOption: String = "Self Pickup"
    ): Result<FirestoreBooking> {
        val service = firestoreService
            ?: return Result.failure(IllegalStateException("Sign in required to create a booking."))
        val uid = Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))

        val profile = _userProfileFlow.value
        val deliveryFee = if (deliveryOption.contains("Delivery", ignoreCase = true)) 100 else 0
        val total = (item.pricePerDay * daysCount.coerceAtLeast(1)) + 50 + deliveryFee
        val otp = (1000..9999).random().toString()

        val booking = FirestoreBooking(
            id = "book_${System.currentTimeMillis()}",
            renterId = uid,
            renterName = profile.displayName.ifBlank { "Loop Member" },
            hostId = item.owner.id.ifBlank { uid },
            hostName = item.owner.name.ifBlank { "Loop Host" },
            itemId = item.id,
            itemTitle = item.title,
            startDate = startDate.ifBlank { "Oct 20, 2026" },
            endDate = endDate.ifBlank { "Oct 23, 2026" },
            daysCount = daysCount.coerceAtLeast(1),
            pricePerDay = item.pricePerDay,
            totalAmount = total,
            status = "Confirmed",
            deliveryOption = deliveryOption,
            handoverOtp = otp
        )

        return service.createBooking(booking).onFailure {
            _syncErrorFlow.value = "Booking failed: ${it.localizedMessage}"
        }
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        repositoryScope.launch {
            firestoreService?.updateBookingStatus(bookingId, status)?.onFailure {
                _syncErrorFlow.value = "Could not update booking: ${it.localizedMessage}"
            }
        }
    }

    fun createCommunityPost(
        title: String,
        type: CommunityPostType,
        description: String,
        location: String,
        dateTime: String
    ): UserCommunityPost {
        val uid = Firebase.auth.currentUser?.uid ?: "member"
        val profile = _userProfileFlow.value
        val newPost = UserCommunityPost(
            id = "post_${System.currentTimeMillis()}",
            title = title,
            type = type,
            description = description,
            location = location.ifBlank { "White Town, Puducherry" },
            dateTime = dateTime.ifBlank { "Upcoming · Scheduled" },
            attendeesOrResponses = when (type) {
                CommunityPostType.EVENT -> "1 creator registered"
                CommunityPostType.HELP_REQUEST -> "Seeking assistance"
                CommunityPostType.STUDY_GROUP -> "1 desk claimed"
            },
            status = CommunityPostStatus.ACTIVE,
            createdAt = "Just now",
            authorId = uid,
            authorName = profile.displayName.ifBlank { "Community Member" }
        )

        _userCommunityPostsFlow.value = listOf(newPost) + _userCommunityPostsFlow.value

        repositoryScope.launch {
            communityRepo?.createEvent(newPost) ?: firestoreService?.createCommunityPost(newPost)
        }
        return newPost
    }

    fun toggleCommunityPostStatus(postId: String) {
        val current = _userCommunityPostsFlow.value.find { it.id == postId } ?: return
        val newStatus = if (current.status == CommunityPostStatus.ACTIVE) {
            CommunityPostStatus.COMPLETED
        } else {
            CommunityPostStatus.ACTIVE
        }
        _userCommunityPostsFlow.value = _userCommunityPostsFlow.value.map { post ->
            if (post.id == postId) post.copy(status = newStatus) else post
        }
        repositoryScope.launch {
            communityRepo?.updateEventStatus(postId, newStatus)
                ?: firestoreService?.updateCommunityPostStatus(postId, newStatus)
        }
    }

    fun deleteCommunityPost(postId: String) {
        _userCommunityPostsFlow.value = _userCommunityPostsFlow.value.filterNot { it.id == postId }
        repositoryScope.launch {
            communityRepo?.deleteEvent(postId)
                ?: firestoreService?.deleteCommunityPost(postId)
        }
    }

    fun toggleFavorite(itemId: String) {
        val currentFavs = _userProfileFlow.value.favoriteItemIds.toMutableList()
        if (itemId in currentFavs) {
            currentFavs.remove(itemId)
        } else {
            currentFavs.add(itemId)
        }
        val updatedProfile = _userProfileFlow.value.copy(favoriteItemIds = currentFavs)
        _userProfileFlow.value = updatedProfile
        _itemsFlow.value = _itemsFlow.value.map { item ->
            if (item.id == itemId) item.copy(isFavorite = itemId in currentFavs) else item
        }
        repositoryScope.launch {
            firestoreService?.updateFavoriteItems(currentFavs, updatedProfile)
        }
    }

    fun rsvpToCommunityPost(postId: String) {
        _discoveryPinsFlow.value = _discoveryPinsFlow.value.map { pin ->
            if (pin.id == postId) pin.copy(isAttending = !pin.isAttending) else pin
        }
        repositoryScope.launch {
            val matchingPost = _userCommunityPostsFlow.value.find { it.id == postId }
            if (matchingPost != null) {
                communityRepo?.rsvpToEvent(postId)
            }
        }
    }
}
