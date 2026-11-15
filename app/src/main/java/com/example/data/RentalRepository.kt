package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.firebase.FirestoreBooking
import com.example.data.firebase.FirestoreService
import com.example.data.models.AppearanceMode
import com.example.data.models.BlockedUserEntry
import com.example.data.models.ChatConversation
import com.example.data.models.ChatMessageType
import com.example.data.models.CommunityEvent
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.CuratedWalkingRoute
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType
import com.example.data.models.LocationSharingPreference
import com.example.data.models.MapCategoryFilter
import com.example.data.models.MessageStatus
import com.example.data.models.ProfileVisibility
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
import com.example.data.models.UserSettings
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

    val items: List<RentalItem> = emptyList()

    val initialConversations: List<ChatConversation> = emptyList()
    val sampleMessages: List<RentalMessage> = emptyList()

    val discoveryPins: List<DiscoveryPinItem> = emptyList()

    val curatedRoutes: List<CuratedWalkingRoute> = emptyList()

    val initialUserProfile = UserProfile()

    val initialUserActivities: List<UserActivityItem> = emptyList()

    val initialCommunityPosts: List<UserCommunityPost> = emptyList()

    private val _rawItemsFlow = MutableStateFlow(items)
    private val _userProfileFlow = MutableStateFlow(initialUserProfile)
    val userProfileFlow: StateFlow<UserProfile> = _userProfileFlow.asStateFlow()

    val userSettingsFlow: StateFlow<UserSettings> = _userProfileFlow
        .map { it.settings }
        .stateIn(repositoryScope, SharingStarted.Eagerly, UserSettings())

    val itemsFlow: StateFlow<List<RentalItem>> = combine(
        _rawItemsFlow,
        _userProfileFlow
    ) { rawItems, profile ->
        val favIds = profile.favoriteItemIds.toSet()
        val blockedIds = profile.settings.blockedUserIds
        rawItems
            .filter { it.owner.id !in blockedIds }
            .map { it.copy(isFavorite = it.id in favIds) }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, items)

    val savedItemsFlow: StateFlow<List<RentalItem>> = itemsFlow
        .map { all -> all.filter { it.isFavorite } }
        .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    val myPublishedItemsFlow: StateFlow<List<RentalItem>> = combine(
        itemsFlow,
        _userProfileFlow
    ) { all, profile ->
        if (profile.uid.isBlank()) emptyList() else all.filter { it.owner.id == profile.uid }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<RentalCategory?>(null)
    val selectedCategory: StateFlow<RentalCategory?> = _selectedCategory.asStateFlow()

    val filteredItems: StateFlow<List<RentalItem>> = combine(
        itemsFlow,
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

    private val _rawConversationsFlow = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversationsFlow: StateFlow<List<ChatConversation>> = combine(
        _rawConversationsFlow,
        _userProfileFlow
    ) { rawConvs, profile ->
        val blockedIds = profile.settings.blockedUserIds
        rawConvs.filter { it.owner.id !in blockedIds }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    private val _activeConversationMessagesFlow = MutableStateFlow<Map<String, List<RentalMessage>>>(emptyMap())
    val activeConversationMessagesFlow: StateFlow<Map<String, List<RentalMessage>>> =
        _activeConversationMessagesFlow.asStateFlow()

    private val _bookingsFlow = MutableStateFlow<List<UserRentalBooking>>(emptyList())
    val bookingsFlow: StateFlow<List<UserRentalBooking>> = _bookingsFlow.asStateFlow()

    private val _rsvpIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    val rsvpIdsFlow: StateFlow<Set<String>> = _rsvpIdsFlow.asStateFlow()

    private val _rawCommunityPostsFlow = MutableStateFlow(initialCommunityPosts)
    val userCommunityPostsFlow: StateFlow<List<UserCommunityPost>> = combine(
        _rawCommunityPostsFlow,
        _userProfileFlow
    ) { rawPosts, profile ->
        val blockedIds = profile.settings.blockedUserIds
        rawPosts.filter { it.authorId !in blockedIds }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, initialCommunityPosts)

    val myCommunityPostsFlow: StateFlow<List<UserCommunityPost>> = combine(
        userCommunityPostsFlow,
        _userProfileFlow
    ) { posts, profile ->
        if (profile.uid.isBlank()) emptyList() else posts.filter { it.authorId == profile.uid }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    val userActivityFlow: StateFlow<List<UserActivityItem>> = combine(
        _bookingsFlow,
        myPublishedItemsFlow,
        myCommunityPostsFlow
    ) { bookings, myItems, myPosts ->
        val bookingActivities = bookings.map { booking ->
            UserActivityItem(
                id = "act_book_${booking.id}",
                title = "${booking.item.title} (${booking.status})",
                subtitle = "Host: ${booking.hostName} · ₹${booking.totalPaid} · OTP #${booking.handoverOtp}",
                type = UserActivityType.RESERVATION,
                timestamp = "${booking.startDate} – ${booking.endDate}",
                statusBadge = booking.status,
                iconEmoji = "📦",
                relatedId = booking.id
            )
        }
        val itemActivities = myItems.map { item ->
            UserActivityItem(
                id = "act_item_${item.id}",
                title = "Published: ${item.title}",
                subtitle = "${item.category.title} · ₹${item.pricePerDay}/day · ${item.location.ifBlank { "Marketplace" }}",
                type = UserActivityType.LISTING_PUBLISHED,
                timestamp = if (item.availableToday) "Active listing" else "Paused",
                statusBadge = if (item.availableToday) "Live" else "Paused",
                iconEmoji = item.category.iconEmoji,
                relatedId = item.id
            )
        }
        val postActivities = myPosts.map { post ->
            UserActivityItem(
                id = "act_post_${post.id}",
                title = post.title,
                subtitle = "${post.type.title} · ${post.location.ifBlank { "Neighborhood" }}",
                type = when (post.type) {
                    CommunityPostType.EVENT -> UserActivityType.COMMUNITY_EVENT
                    CommunityPostType.HELP_REQUEST -> UserActivityType.HELP_REQUEST
                    CommunityPostType.STUDY_GROUP -> UserActivityType.STUDY_GROUP
                },
                timestamp = post.dateTime.ifBlank { "Community post" },
                statusBadge = post.status.label,
                iconEmoji = post.type.emoji,
                relatedId = post.id
            )
        }
        bookingActivities + itemActivities + postActivities
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    val discoveryPinsFlow: StateFlow<List<DiscoveryPinItem>> = combine(
        itemsFlow,
        userCommunityPostsFlow,
        _rsvpIdsFlow
    ) { itemsList, postsList, rsvpIds ->
        val itemPins = itemsList.mapNotNull { item ->
            val lat = item.latitude
            val lng = item.longitude
            if (lat != null && lng != null) {
                DiscoveryPinItem(
                    id = item.id,
                    title = item.title,
                    subtitle = "Hosted by ${item.owner.name}",
                    type = DiscoveryPinType.RENTAL_ITEM,
                    latitude = lat,
                    longitude = lng,
                    rating = item.rating,
                    distance = item.location.ifBlank { "Nearby" },
                    tag = item.category.title,
                    dateOrAvailability = if (item.availableToday) "Available" else "Unavailable",
                    locationName = item.location,
                    priceOrAttendees = "₹${item.pricePerDay}/day",
                    description = item.description,
                    owner = item.owner,
                    rentalItem = item,
                    imageUrl = item.imageUrl,
                    category = item.category,
                    filterCategory = MapCategoryFilter.MAKERS,
                    neighborhood = item.location
                )
            } else {
                null
            }
        }
        val postPins = postsList.mapNotNull { post ->
            val lat = post.latitude
            val lng = post.longitude
            if (lat != null && lng != null) {
                val pinType = when (post.type) {
                    CommunityPostType.EVENT -> DiscoveryPinType.COMMUNITY_EVENT
                    CommunityPostType.HELP_REQUEST -> DiscoveryPinType.HELP_REQUEST
                    CommunityPostType.STUDY_GROUP -> DiscoveryPinType.STUDY_GROUP
                }
                val filterCat = when (post.type) {
                    CommunityPostType.EVENT -> MapCategoryFilter.LOCAL_EVENTS
                    CommunityPostType.HELP_REQUEST -> MapCategoryFilter.HELP_NEEDED
                    CommunityPostType.STUDY_GROUP -> MapCategoryFilter.STUDY_GROUPS
                }
                DiscoveryPinItem(
                    id = post.id,
                    title = post.title,
                    subtitle = post.authorName.ifBlank { "Community Member" },
                    type = pinType,
                    latitude = lat,
                    longitude = lng,
                    tag = post.type.title,
                    dateOrAvailability = post.dateTime,
                    locationName = post.location,
                    priceOrAttendees = post.attendeesOrResponses,
                    description = post.description,
                    isAttending = post.id in rsvpIds,
                    filterCategory = filterCat,
                    neighborhood = post.location
                )
            } else {
                null
            }
        }
        itemPins + postPins
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    private val _syncErrorFlow = MutableStateFlow<String?>(null)
    val syncErrorFlow: StateFlow<String?> = _syncErrorFlow.asStateFlow()

    val communityEventsFlow: StateFlow<List<CommunityEvent>> = userCommunityPostsFlow.map { posts ->
        posts.mapNotNull { post ->
            val lat = post.latitude
            val lng = post.longitude
            if (lat != null && lng != null) {
                CommunityEvent(
                    id = post.id,
                    title = post.title,
                    description = post.description,
                    category = post.categoryName,
                    locationName = post.location,
                    latitude = lat,
                    longitude = lng,
                    timeString = post.dateTime,
                    organizerName = post.authorName,
                    participantCount = Regex("(\\d+)").find(post.attendeesOrResponses)?.value?.toIntOrNull() ?: 1
                )
            } else {
                null
            }
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

        // Ensure user's profile doc exists in Firestore with zeroed starter stats and legacy reset version 2
        repositoryScope.launch {
            val starterProfile = UserProfile(
                uid = uid,
                displayName = currentUser.displayName ?: "",
                email = currentUser.email ?: "",
                legacyResetVersion = 2
            )
            val existingDoc = try {
                service.firestore.collection("users").document(uid).get().await()
            } catch (_: Exception) {
                null
            }
            if (existingDoc == null || !existingDoc.exists()) {
                service.saveUserProfile(starterProfile)
            } else {
                val currentVersion = existingDoc.getLong("legacyResetVersion")?.toInt() ?: 0
                val storedEarned = existingDoc.getLong("earnedAmount")?.toInt() ?: 0
                val storedLocation = existingDoc.getString("location").orEmpty()
                if (currentVersion < 2 || storedEarned > 0 || storedLocation.contains("White Town", ignoreCase = true)) {
                    service.saveUserProfile(starterProfile)
                }
            }

            // Also purge any legacy seeded items from the global /items collection
            try {
                val legacySeeds = service.firestore.collection("items")
                    .whereIn("title", listOf(
                        "Coastal Cruiser Electric E-Bike",
                        "Canon EOS & 35mm Glass",
                        "Scandinavian Linen Armchair",
                        "Leica M6 & 35mm Summicron",
                        "Marantz Studio Reference Rig",
                        "Eames Lounge Chair & Ottoman"
                    ))
                    .get()
                    .await()
                for (doc in legacySeeds.documents) {
                    doc.reference.delete().await()
                }
            } catch (_: Exception) {}
        }

        // 1. User Profile real-time sync
        syncJobs += repositoryScope.launch {
            try {
                service.listenToUserProfile(uid).collect { cloudProfile ->
                    _userProfileFlow.value = cloudProfile
                    ThemePreferencesStore.saveAppearanceMode(context, cloudProfile.settings.appearanceMode)
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
                    _rawItemsFlow.value = cloudItems
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Items sync error: ${e.message}", e)
                _syncErrorFlow.value = "Catalog sync error: ${e.localizedMessage}"
            }
        }

        // 3. Community Posts / Events sync (Room + Firestore)
        syncJobs += repositoryScope.launch {
            try {
                cRepo.allEvents.collect { events ->
                    _rawCommunityPostsFlow.value = events
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
                service.listenToConversations(uid, _rawItemsFlow.value).collect { cloudConvs ->
                    val currentMap = _activeConversationMessagesFlow.value
                    _rawConversationsFlow.value = cloudConvs.map { conv ->
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
                service.listenToBookings(uid, _rawItemsFlow.value).collect { cloudBookings ->
                    _bookingsFlow.value = cloudBookings
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
                    _rawConversationsFlow.value = _rawConversationsFlow.value.map { conv ->
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
        _rawConversationsFlow.value = emptyList()
        _activeConversationMessagesFlow.value = emptyMap()
        _bookingsFlow.value = emptyList()
        _rawCommunityPostsFlow.value = initialCommunityPosts
        _userProfileFlow.value = initialUserProfile
        _rawItemsFlow.value = items
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

    fun updateUserSettings(context: Context, updatedSettings: UserSettings) {
        val currentProfile = _userProfileFlow.value
        val updatedProfile = currentProfile.copy(settings = updatedSettings)
        _userProfileFlow.value = updatedProfile
        repositoryScope.launch {
            ThemePreferencesStore.saveAppearanceMode(context, updatedSettings.appearanceMode)
            firestoreService?.updateUserSettings(updatedSettings, updatedProfile)?.onFailure {
                _syncErrorFlow.value = "Failed to save settings: ${it.localizedMessage}"
            }
        }
    }

    fun blockUser(context: Context, userId: String, displayName: String) {
        if (userId.isBlank() || userId == _userProfileFlow.value.uid) return
        val current = _userProfileFlow.value.settings
        if (current.blockedUsers.any { it.userId == userId }) return
        val newEntry = BlockedUserEntry(
            userId = userId,
            displayName = displayName.ifBlank { "Community Member" },
            blockedAtLabel = "Blocked recently"
        )
        updateUserSettings(context, current.copy(blockedUsers = current.blockedUsers + newEntry))
    }

    fun unblockUser(context: Context, userId: String) {
        val current = _userProfileFlow.value.settings
        val remaining = current.blockedUsers.filterNot { it.userId == userId }
        updateUserSettings(context, current.copy(blockedUsers = remaining))
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
        location: String = "",
        category: RentalCategory,
        desc: String,
        imageUrl: String? = null,
        imageUrls: List<String> = emptyList(),
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<RentalItem> {
        val service = firestoreService
            ?: return Result.failure(IllegalStateException("Sign in required to publish listing."))
        val uid = Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))

        val profile = _userProfileFlow.value
        val ownerName = profile.displayName.ifBlank { Firebase.auth.currentUser?.displayName ?: "Community Member" }
        val galleryUrls = imageUrls.filter { it.isNotBlank() }.take(4).ifEmpty {
            listOfNotNull(imageUrl?.takeIf { it.isNotBlank() })
        }
        val coverUrl = galleryUrls.firstOrNull()
        val locShare = profile.settings.locationSharing
        val effectiveLocation = if (locShare == LocationSharingPreference.HIDDEN && location.isBlank()) {
            ""
        } else {
            location.trim()
        }
        val effectiveLat = if (locShare == LocationSharingPreference.PRECISE) latitude else null
        val effectiveLng = if (locShare == LocationSharingPreference.PRECISE) longitude else null

        val newItem = RentalItem(
            id = "item_${System.currentTimeMillis()}",
            title = title.trim(),
            category = category,
            pricePerDay = price.coerceAtLeast(1),
            location = effectiveLocation,
            rating = 0f,
            reviewCount = 0,
            description = desc.trim(),
            primaryImageRes = 0,
            imageUrl = coverUrl,
            imageUrls = galleryUrls,
            imageCount = galleryUrls.size,
            features = emptyList(),
            owner = RentalOwner(
                id = uid,
                name = ownerName,
                badge = "Community Member",
                initials = profile.initials
            ),
            isPopular = false,
            isFavorite = false,
            availableToday = true,
            securityDeposit = deposit.coerceAtLeast(0),
            latitude = effectiveLat,
            longitude = effectiveLng
        )

        return service.createListing(newItem).onSuccess { savedItem ->
            _rawItemsFlow.value = listOf(savedItem) + _rawItemsFlow.value.filterNot { it.id == savedItem.id }
        }.onFailure {
            _syncErrorFlow.value = "Failed to publish listing: ${it.localizedMessage}"
        }
    }

    fun deleteListing(itemId: String) {
        _rawItemsFlow.value = _rawItemsFlow.value.filterNot { it.id == itemId }
        repositoryScope.launch {
            firestoreService?.deleteListing(itemId)?.onFailure {
                _syncErrorFlow.value = "Failed to delete listing: ${it.localizedMessage}"
            }
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
            renterName = profile.displayName.ifBlank { Firebase.auth.currentUser?.displayName ?: "Member" },
            hostId = item.owner.id.ifBlank { uid },
            hostName = item.owner.name.ifBlank { "Host" },
            itemId = item.id,
            itemTitle = item.title,
            startDate = startDate,
            endDate = endDate,
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
        dateTime: String,
        latitude: Double? = null,
        longitude: Double? = null
    ): UserCommunityPost {
        val uid = Firebase.auth.currentUser?.uid ?: ""
        val profile = _userProfileFlow.value
        val locShare = profile.settings.locationSharing
        val effectiveLat = if (locShare == LocationSharingPreference.PRECISE) latitude else null
        val effectiveLng = if (locShare == LocationSharingPreference.PRECISE) longitude else null

        val newPost = UserCommunityPost(
            id = "post_${System.currentTimeMillis()}",
            title = title.trim(),
            type = type,
            description = description.trim(),
            location = location.trim(),
            dateTime = dateTime.trim(),
            attendeesOrResponses = when (type) {
                CommunityPostType.EVENT -> "1 attending"
                CommunityPostType.HELP_REQUEST -> "Seeking assistance"
                CommunityPostType.STUDY_GROUP -> "1 desk claimed"
            },
            status = CommunityPostStatus.ACTIVE,
            createdAt = "Just now",
            latitude = effectiveLat,
            longitude = effectiveLng,
            authorId = uid,
            authorName = profile.displayName.ifBlank { Firebase.auth.currentUser?.displayName ?: "Community Member" }
        )

        _rawCommunityPostsFlow.value = listOf(newPost) + _rawCommunityPostsFlow.value

        repositoryScope.launch {
            communityRepo?.createEvent(newPost) ?: firestoreService?.createCommunityPost(newPost)
        }
        return newPost
    }

    fun toggleCommunityPostStatus(postId: String) {
        val current = _rawCommunityPostsFlow.value.find { it.id == postId } ?: return
        val newStatus = if (current.status == CommunityPostStatus.ACTIVE) {
            CommunityPostStatus.COMPLETED
        } else {
            CommunityPostStatus.ACTIVE
        }
        _rawCommunityPostsFlow.value = _rawCommunityPostsFlow.value.map { post ->
            if (post.id == postId) post.copy(status = newStatus) else post
        }
        repositoryScope.launch {
            communityRepo?.updateEventStatus(postId, newStatus)
                ?: firestoreService?.updateCommunityPostStatus(postId, newStatus)
        }
    }

    fun deleteCommunityPost(postId: String) {
        _rawCommunityPostsFlow.value = _rawCommunityPostsFlow.value.filterNot { it.id == postId }
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
        repositoryScope.launch {
            firestoreService?.updateFavoriteItems(currentFavs, updatedProfile)
        }
    }

    fun rsvpToCommunityPost(postId: String) {
        val currentSet = _rsvpIdsFlow.value
        val nowAttending = postId !in currentSet
        _rsvpIdsFlow.value = if (nowAttending) currentSet + postId else currentSet - postId
        repositoryScope.launch {
            val matchingPost = _rawCommunityPostsFlow.value.find { it.id == postId }
            if (matchingPost != null && nowAttending) {
                communityRepo?.rsvpToEvent(postId)
            }
        }
    }
}
