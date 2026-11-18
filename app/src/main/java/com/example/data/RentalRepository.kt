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
import com.example.notifications.FcmTokenSyncHelper
import com.example.notifications.LoopNotificationManager
import com.example.notifications.NotificationCheckpointStore
import com.example.notifications.NotificationDeltaEvaluator
import com.example.notifications.NotificationSyncWorker
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SyncAlert(
    val id: Long = System.nanoTime(),
    val message: String,
    val retryLabel: String? = null,
    val onRetry: (suspend () -> Unit)? = null
)

object RentalRepository {
    private const val TAG = "RentalRepository"

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sessionAppContext: Context? = null
    private var firestoreService: FirestoreService? = null
    private var communityRepo: CommunityEventRepository? = null
    private var activeSessionUid: String? = null
    private var activeOpenConversationId: String? = null
    private var isAppInForeground: Boolean = true
    private var syncJobs = mutableListOf<Job>()
    private var messageListenerJob: Job? = null
    private val memberProfileCache = java.util.concurrent.ConcurrentHashMap<String, UserProfile>()

    fun setActiveOpenConversationId(conversationId: String?) {
        activeOpenConversationId = conversationId?.takeIf { it.isNotBlank() }
    }

    fun setAppForegroundState(inForeground: Boolean) {
        isAppInForeground = inForeground
    }

    fun syncNotificationsForCurrentUser(context: Context) {
        val uid = activeSessionUid ?: Firebase.auth.currentUser?.uid ?: return
        val appContext = context.applicationContext
        ThemePreferencesStore.ensureNotificationChannel(appContext)
        NotificationSyncWorker.schedulePeriodic(appContext)
        repositoryScope.launch {
            FcmTokenSyncHelper.syncTokenAndTopics(
                context = appContext,
                uid = uid,
                preferences = _userProfileFlow.value.settings.notifications
            )
        }
    }

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
        _rawItemsFlow,
        _userProfileFlow
    ) { rawConvs, itemsList, profile ->
        val blockedIds = profile.settings.blockedUserIds
        rawConvs
            .filter { it.owner.id !in blockedIds }
            .map { conv ->
                val liveItem = conv.itemContext?.id?.let { id -> itemsList.find { it.id == id } }
                if (liveItem != null) conv.copy(itemContext = liveItem) else conv
            }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    private val _activeConversationMessagesFlow = MutableStateFlow<Map<String, List<RentalMessage>>>(emptyMap())
    val activeConversationMessagesFlow: StateFlow<Map<String, List<RentalMessage>>> =
        _activeConversationMessagesFlow.asStateFlow()

    private val _rawBookingsFlow = MutableStateFlow<List<UserRentalBooking>>(emptyList())
    val bookingsFlow: StateFlow<List<UserRentalBooking>> = combine(
        _rawBookingsFlow,
        _rawItemsFlow
    ) { rawBookings, itemsList ->
        rawBookings.map { booking ->
            val liveItem = itemsList.find { it.id == booking.item.id }
            if (liveItem != null) booking.copy(item = liveItem) else booking
        }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    private val _rawCommunityPostsFlow = MutableStateFlow(initialCommunityPosts)
    val userCommunityPostsFlow: StateFlow<List<UserCommunityPost>> = combine(
        _rawCommunityPostsFlow,
        _userProfileFlow
    ) { rawPosts, profile ->
        val blockedIds = profile.settings.blockedUserIds
        rawPosts.filter { it.authorId !in blockedIds }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, initialCommunityPosts)

    val rsvpIdsFlow: StateFlow<Set<String>> = combine(
        _rawCommunityPostsFlow,
        _userProfileFlow
    ) { posts, profile ->
        if (profile.uid.isBlank()) {
            emptySet()
        } else {
            posts.filter { profile.uid in it.attendeeIds }.map { it.id }.toSet()
        }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptySet())

    val myCommunityPostsFlow: StateFlow<List<UserCommunityPost>> = combine(
        userCommunityPostsFlow,
        _userProfileFlow
    ) { posts, profile ->
        if (profile.uid.isBlank()) emptyList() else posts.filter { it.authorId == profile.uid }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    val userActivityFlow: StateFlow<List<UserActivityItem>> = combine(
        bookingsFlow,
        myPublishedItemsFlow,
        myCommunityPostsFlow,
        _userProfileFlow
    ) { bookings, myItems, myPosts, profile ->
        val bookingActivities = bookings.map { booking ->
            val isHost = booking.hostId == profile.uid && profile.uid.isNotBlank()
            val prefix = if (isHost) "Hosting" else "Reserved"
            val partyLabel = if (isHost) {
                "Renter: ${booking.renterName.ifBlank { "Member" }}"
            } else {
                "Host: ${booking.hostName.ifBlank { "Host" }}"
            }
            UserActivityItem(
                id = "act_book_${booking.id}",
                title = "$prefix: ${booking.item.title} (${booking.status})",
                subtitle = "$partyLabel · ₹${booking.totalPaid} · PIN #${booking.handoverOtp}",
                type = UserActivityType.RESERVATION,
                timestamp = "${booking.startDate} – ${booking.endDate}",
                statusBadge = booking.status,
                relatedId = booking.id
            )
        }
        val itemActivities = myItems.map { item ->
            val isBooked = item.activeBookingId != null && !item.availableToday
            val statusText = when {
                isBooked -> "Booked"
                item.availableToday -> "Live"
                else -> "Paused"
            }
            val timestampText = when {
                isBooked -> "Currently reserved"
                item.availableToday -> "Active listing"
                else -> "Paused"
            }
            UserActivityItem(
                id = "act_item_${item.id}",
                title = "Published: ${item.title}",
                subtitle = "${item.category.title} · ₹${item.pricePerDay}/day · ${item.location.ifBlank { "Marketplace" }}",
                type = UserActivityType.LISTING_PUBLISHED,
                timestamp = timestampText,
                statusBadge = statusText,
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
                relatedId = post.id
            )
        }
        bookingActivities + itemActivities + postActivities
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    val discoveryPinsFlow: StateFlow<List<DiscoveryPinItem>> = combine(
        itemsFlow,
        userCommunityPostsFlow,
        rsvpIdsFlow
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
                    dateOrAvailability = when {
                        item.activeBookingId != null && !item.availableToday -> "Booked"
                        item.availableToday -> "Available"
                        else -> "Paused"
                    },
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
                    communityPost = post,
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

    private val _syncAlertFlow = MutableStateFlow<SyncAlert?>(null)
    val syncAlertFlow: StateFlow<SyncAlert?> = _syncAlertFlow.asStateFlow()

    private val _isProfileLoadingFlow = MutableStateFlow(true)
    val isProfileLoadingFlow: StateFlow<Boolean> = _isProfileLoadingFlow.asStateFlow()

    private val _isCatalogLoadingFlow = MutableStateFlow(true)
    val isCatalogLoadingFlow: StateFlow<Boolean> = _isCatalogLoadingFlow.asStateFlow()

    private val _isBookingsLoadingFlow = MutableStateFlow(true)
    val isBookingsLoadingFlow: StateFlow<Boolean> = _isBookingsLoadingFlow.asStateFlow()

    private val _isConversationsLoadingFlow = MutableStateFlow(true)
    val isConversationsLoadingFlow: StateFlow<Boolean> = _isConversationsLoadingFlow.asStateFlow()

    private val _isRefreshingFlow = MutableStateFlow(false)
    val isRefreshingFlow: StateFlow<Boolean> = _isRefreshingFlow.asStateFlow()

    private fun emitSyncAlert(
        message: String,
        retryLabel: String? = null,
        onRetry: (suspend () -> Unit)? = null
    ) {
        _syncErrorFlow.value = message
        _syncAlertFlow.value = SyncAlert(
            id = System.nanoTime(),
            message = message,
            retryLabel = retryLabel,
            onRetry = onRetry
        )
    }

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

    private const val LEGACY_RESET_VERSION = 3

    fun startSession(context: Context, uid: String) {
        val currentUser = Firebase.auth.currentUser ?: return
        if (activeSessionUid == uid && syncJobs.any { it.isActive }) return

        clearSession()
        val appContext = context.applicationContext
        sessionAppContext = appContext
        activeSessionUid = uid
        _syncErrorFlow.value = null
        _syncAlertFlow.value = null
        _isProfileLoadingFlow.value = true
        _isCatalogLoadingFlow.value = true
        _isBookingsLoadingFlow.value = true
        _isConversationsLoadingFlow.value = true

        ThemePreferencesStore.ensureNotificationChannel(appContext)
        NotificationSyncWorker.schedulePeriodic(appContext)

        val service = FirestoreService.getInstance(appContext)
        firestoreService = service
        val cRepo = CommunityEventRepository.getInstance(appContext)
        communityRepo = cRepo

        syncJobs += repositoryScope.launch {
            cRepo.clearLocalCache()

            val existingDoc = try {
                service.firestore.collection("users").document(uid).get().await()
            } catch (_: Exception) {
                null
            }

            val currentVersion = existingDoc?.getLong("legacyResetVersion")?.toInt() ?: 0

            val needsFullReset = existingDoc == null ||
                !existingDoc.exists() ||
                currentVersion < LEGACY_RESET_VERSION

            if (needsFullReset) {
                service.purgeAllItemsAndCommunityPosts()
                cRepo.clearLocalCache()
                _rawItemsFlow.value = emptyList()
                _rawCommunityPostsFlow.value = emptyList()

                val preservedSettings = if (existingDoc != null && existingDoc.exists()) {
                    UserSettings(
                        appearanceMode = AppearanceMode.fromNameOrDefault(existingDoc.getString("appearanceMode")),
                        profileVisibility = ProfileVisibility.fromNameOrDefault(existingDoc.getString("profileVisibility")),
                        locationSharing = LocationSharingPreference.fromNameOrDefault(existingDoc.getString("locationSharing"))
                    )
                } else {
                    UserSettings()
                }

                val resetProfile = UserProfile(
                    uid = uid,
                    displayName = currentUser.displayName?.takeIf { it.isNotBlank() }
                        ?: existingDoc?.getString("displayName").orEmpty(),
                    email = currentUser.email?.takeIf { it.isNotBlank() }
                        ?: existingDoc?.getString("email").orEmpty(),
                    phone = "",
                    bio = "",
                    location = "",
                    avatarPresetIndex = existingDoc?.getLong("avatarPresetIndex")?.toInt() ?: 0,
                    customAvatarUri = existingDoc?.getString("customAvatarUri"),
                    rating = 0f,
                    reviewCount = 0,
                    rentalsCompleted = 0,
                    listingsCount = 0,
                    earnedAmount = 0,
                    isVerified = false,
                    memberSince = "",
                    badges = emptyList(),
                    favoriteItemIds = emptyList(),
                    legacyResetVersion = LEGACY_RESET_VERSION,
                    settings = preservedSettings
                )
                _userProfileFlow.value = resetProfile
                service.saveUserProfile(resetProfile)
            }

            cRepo.startAuthenticatedSync()

            var profileLoaded = false
            var itemsLoaded = false
            var bookingsLoaded = false
            var convsLoaded = false
            var communityLoaded = false

            // Ensure device FCM token & topic subscriptions are synced after profile initializes
            FcmTokenSyncHelper.syncTokenAndTopics(
                context = appContext,
                uid = uid,
                preferences = _userProfileFlow.value.settings.notifications
            )

            // 1. User Profile real-time sync
            syncJobs += repositoryScope.launch {
                try {
                    service.listenToUserProfile(uid).collect { cloudProfile ->
                        profileLoaded = true
                        _isProfileLoadingFlow.value = false
                        _userProfileFlow.value = cloudProfile
                        ThemePreferencesStore.saveAppearanceMode(context, cloudProfile.settings.appearanceMode)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _isProfileLoadingFlow.value = false
                    Log.e(TAG, "Profile sync error: ${e.message}", e)
                    emitSyncAlert("Profile sync error: ${e.localizedMessage}")
                }
            }

            // 2. Gear Listings real-time sync
            syncJobs += repositoryScope.launch {
                try {
                    service.listenToItems().collect { cloudItems ->
                        itemsLoaded = true
                        _isCatalogLoadingFlow.value = false
                        _rawItemsFlow.value = cloudItems
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _isCatalogLoadingFlow.value = false
                    Log.e(TAG, "Items sync error: ${e.message}", e)
                    emitSyncAlert("Catalog sync error: ${e.localizedMessage}")
                }
            }

            // 3. Community Posts / Events sync (Room + Firestore)
            syncJobs += repositoryScope.launch {
                try {
                    cRepo.allEvents.collect { events ->
                        communityLoaded = true
                        _rawCommunityPostsFlow.value = events
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    communityLoaded = true
                    Log.e(TAG, "Community sync error: ${e.message}", e)
                }
            }

            // 4. Participant-scoped Conversations sync
            syncJobs += repositoryScope.launch {
                try {
                    service.listenToConversations(uid, _rawItemsFlow.value).collect { cloudConvs ->
                        convsLoaded = true
                        _isConversationsLoadingFlow.value = false
                        val currentMap = _activeConversationMessagesFlow.value
                        _rawConversationsFlow.value = cloudConvs.map { conv ->
                            val cachedMsgs = currentMap[conv.id]
                            if (!cachedMsgs.isNullOrEmpty()) conv.copy(messages = cachedMsgs) else conv
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _isConversationsLoadingFlow.value = false
                    Log.e(TAG, "Conversations sync error: ${e.message}", e)
                    emitSyncAlert("Chat sync error: ${e.localizedMessage}")
                }
            }

            // 5. Bookings real-time sync (renter or host)
            syncJobs += repositoryScope.launch {
                try {
                    service.listenToBookings(uid, _rawItemsFlow.value).collect { cloudBookings ->
                        bookingsLoaded = true
                        _isBookingsLoadingFlow.value = false
                        _rawBookingsFlow.value = cloudBookings
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _isBookingsLoadingFlow.value = false
                    Log.e(TAG, "Bookings sync error: ${e.message}", e)
                    emitSyncAlert("Bookings sync error: ${e.localizedMessage}")
                }
            }

            // 6. Auto-sync real user stats & badges derived from listings, completed rentals, and reviews
            syncJobs += repositoryScope.launch {
                try {
                    combine(
                        _userProfileFlow,
                        myPublishedItemsFlow,
                        _rawBookingsFlow,
                        myCommunityPostsFlow
                    ) { profile, myItems, bookings, myPosts ->
                        StatsSnapshot(profile, myItems, bookings, myPosts)
                    }.collect { snap ->
                        if (!profileLoaded || !itemsLoaded || !bookingsLoaded) return@collect
                        if (snap.profile.uid != uid || uid.isBlank()) return@collect

                        val computedListings = snap.myItems.size
                        val completedAsHost = snap.bookings.filter {
                            it.hostId == uid && it.status.equals("Completed", ignoreCase = true)
                        }
                        val computedRentals = completedAsHost.size
                        val computedEarned = completedAsHost.sumOf { it.totalPaid }
                        val ratedItems = snap.myItems.filter { it.reviewCount > 0 && it.rating > 0f }
                        val computedReviewCount = ratedItems.sumOf { it.reviewCount }
                        val computedRating = if (computedReviewCount > 0) {
                            val weighted = ratedItems.sumOf { (it.rating * it.reviewCount).toDouble() }
                            (kotlin.math.round((weighted / computedReviewCount) * 10.0) / 10.0).toFloat()
                        } else {
                            0f
                        }

                        val computedBadges = buildList {
                            if (computedListings >= 1) add("Gear Host")
                            if (computedRentals >= 1) add("Completed Rental")
                            if (snap.myPosts.isNotEmpty()) add("Community Builder")
                            if (computedRating >= 4.5f && computedReviewCount >= 2) add("Top Rated")
                        }

                        val statsChanged =
                            snap.profile.listingsCount != computedListings ||
                                snap.profile.rentalsCompleted != computedRentals ||
                                snap.profile.earnedAmount != computedEarned ||
                                snap.profile.reviewCount != computedReviewCount ||
                                kotlin.math.abs(snap.profile.rating - computedRating) > 0.05f ||
                                snap.profile.badges != computedBadges

                        if (statsChanged) {
                            val reconciled = snap.profile.copy(
                                listingsCount = computedListings,
                                rentalsCompleted = computedRentals,
                                earnedAmount = computedEarned,
                                reviewCount = computedReviewCount,
                                rating = computedRating,
                                badges = computedBadges
                            )
                            _userProfileFlow.value = reconciled
                            service.saveUserProfile(reconciled)
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Profile stats reconciliation error: ${e.message}", e)
                }
            }

            // 7. Real-time mobile push delta monitor (messages, bookings/PINs, community posts/RSVPs, reviews & payouts)
            syncJobs += repositoryScope.launch {
                try {
                    combine(
                        _userProfileFlow,
                        conversationsFlow,
                        bookingsFlow,
                        userCommunityPostsFlow,
                        myPublishedItemsFlow
                    ) { profile, convs, bookings, posts, myItems ->
                        NotificationWatchSnapshot(profile, convs, bookings, posts, myItems)
                    }.collect { snap ->
                        if (!profileLoaded || !itemsLoaded || !bookingsLoaded || !convsLoaded || !communityLoaded) {
                            return@collect
                        }
                        if (snap.profile.uid != uid || uid.isBlank()) return@collect

                        val (previousBaseline, alreadyNotified) =
                            NotificationCheckpointStore.loadBaselineAndNotifiedKeys(appContext, uid)

                        val eval = NotificationDeltaEvaluator.evaluateDeltas(
                            currentUid = uid,
                            profile = snap.profile,
                            previousBaseline = previousBaseline,
                            alreadyNotifiedKeys = alreadyNotified,
                            conversations = snap.conversations,
                            bookings = snap.bookings,
                            communityPosts = snap.posts,
                            ownedItems = snap.myItems,
                            activeOpenConversationId = activeOpenConversationId,
                            isAppInForeground = isAppInForeground
                        )

                        val postedKeys = mutableSetOf<String>()
                        eval.events.forEach { event ->
                            if (LoopNotificationManager.postAlert(appContext, event)) {
                                postedKeys += event.eventKey
                            }
                        }

                        NotificationCheckpointStore.saveBaselineAndNotifiedKeys(
                            context = appContext,
                            uid = uid,
                            baseline = eval.updatedBaseline,
                            newlyNotifiedKeys = postedKeys
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Real-time notification monitor error: ${e.message}", e)
                }
            }
        }
    }

    private data class StatsSnapshot(
        val profile: UserProfile,
        val myItems: List<RentalItem>,
        val bookings: List<UserRentalBooking>,
        val myPosts: List<UserCommunityPost>
    )

    private data class NotificationWatchSnapshot(
        val profile: UserProfile,
        val conversations: List<ChatConversation>,
        val bookings: List<UserRentalBooking>,
        val posts: List<UserCommunityPost>,
        val myItems: List<RentalItem>
    )

    fun observeConversationMessages(conversationId: String) {
        val service = firestoreService ?: return
        if (Firebase.auth.currentUser == null) return
        messageListenerJob?.cancel()
        messageListenerJob = repositoryScope.launch {
            service.markConversationRead(conversationId)
            try {
                service.listenToMessages(conversationId).collect { msgs ->
                    _activeConversationMessagesFlow.value =
                        _activeConversationMessagesFlow.value + (conversationId to msgs)
                    _rawConversationsFlow.value = _rawConversationsFlow.value.map { conv ->
                        if (conv.id == conversationId) conv.copy(messages = msgs, unreadCount = 0) else conv
                    }
                    service.markConversationRead(conversationId)
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
        sessionAppContext?.let { NotificationSyncWorker.cancel(it) }
        activeSessionUid = null
        activeOpenConversationId = null
        memberProfileCache.clear()
        _rawConversationsFlow.value = emptyList()
        _activeConversationMessagesFlow.value = emptyMap()
        _rawBookingsFlow.value = emptyList()
        _rawCommunityPostsFlow.value = initialCommunityPosts
        _userProfileFlow.value = initialUserProfile
        _rawItemsFlow.value = items
        _syncErrorFlow.value = null
        _syncAlertFlow.value = null
        _isProfileLoadingFlow.value = false
        _isCatalogLoadingFlow.value = false
        _isBookingsLoadingFlow.value = false
        _isConversationsLoadingFlow.value = false
        _isRefreshingFlow.value = false
    }

    fun invalidateMemberProfile(uid: String) {
        if (uid.isNotBlank()) {
            memberProfileCache.remove(uid)
        }
    }

    fun refreshSessionData(context: Context) {
        val uid = activeSessionUid ?: Firebase.auth.currentUser?.uid ?: return
        if (_isRefreshingFlow.value) return
        _isRefreshingFlow.value = true
        memberProfileCache.clear()
        repositoryScope.launch {
            try {
                val service = firestoreService ?: FirestoreService.getInstance(context)
                service.getUserProfileOnce(uid).onSuccess { freshProfile ->
                    if (freshProfile != null) {
                        _userProfileFlow.value = freshProfile
                    }
                }
                communityRepo?.startAuthenticatedSync()
                clearSyncError()
                delay(420)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emitSyncAlert(
                    message = "Unable to refresh catalog: ${e.localizedMessage}",
                    retryLabel = "Retry"
                ) { refreshSessionData(context) }
            } finally {
                _isRefreshingFlow.value = false
            }
        }
    }

    suspend fun fetchMemberProfile(uid: String, forceRefresh: Boolean = false): UserProfile? {
        if (uid.isBlank()) return null
        val currentProfile = _userProfileFlow.value
        if (uid == activeSessionUid || uid == currentProfile.uid) {
            return currentProfile
        }
        if (!forceRefresh) {
            memberProfileCache[uid]?.let { return it }
        }
        return firestoreService?.getUserProfileOnce(uid)?.getOrNull()?.also { profile ->
            memberProfileCache[uid] = profile
        }
    }

    fun clearSyncError() {
        _syncErrorFlow.value = null
        _syncAlertFlow.value = null
    }

    suspend fun openOrCreateConversation(
        host: RentalOwner,
        item: RentalItem? = null,
        communityPost: UserCommunityPost? = null
    ): Result<ChatConversation> {
        val service = firestoreService
            ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        val uid = Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))

        if (host.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Member profile unavailable."))
        }
        if (host.id == uid) {
            val msg = "You cannot start a conversation with yourself."
            _syncErrorFlow.value = msg
            return Result.failure(IllegalStateException(msg))
        }
        if (host.id in _userProfileFlow.value.settings.blockedUserIds) {
            val msg = "Unblock ${host.name} in Settings to send messages."
            _syncErrorFlow.value = msg
            return Result.failure(IllegalStateException(msg))
        }

        val existing = _rawConversationsFlow.value.firstOrNull { conv ->
            val samePerson = conv.owner.id == host.id
            val sameItem = item?.id != null && conv.itemContext?.id == item.id
            val samePost = communityPost?.id != null && conv.communityPostId == communityPost.id
            val sameGeneral = item == null && communityPost == null &&
                conv.itemContext == null && conv.communityPostId == null
            samePerson && (sameItem || samePost || sameGeneral)
        }
        if (existing != null) {
            return Result.success(existing)
        }

        return service.createOrGetConversation(
            host = host,
            item = item,
            communityPost = communityPost
        ).onFailure { err ->
            _syncErrorFlow.value = err.localizedMessage ?: "Unable to open conversation."
        }
    }

    suspend fun openConversationWithPostAuthor(post: UserCommunityPost): Result<ChatConversation> {
        val uid = Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))
        if (post.authorId.isBlank() || post.authorId == uid) {
            val msg = "You cannot message yourself on your own community post."
            _syncErrorFlow.value = msg
            return Result.failure(IllegalStateException(msg))
        }
        val authorName = post.authorName.ifBlank { "Organizer" }
        val initials = authorName.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
            .ifBlank { "O" }
        val organizer = RentalOwner(
            id = post.authorId,
            name = authorName,
            badge = "Community Organizer",
            initials = initials
        )
        return openOrCreateConversation(host = organizer, item = null, communityPost = post)
    }

    fun updateUserProfile(
        profile: UserProfile,
        onResult: ((Result<Unit>) -> Unit)? = null
    ) {
        val previousProfile = _userProfileFlow.value
        _userProfileFlow.value = profile
        repositoryScope.launch {
            val res = firestoreService?.saveUserProfile(profile)
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            res.onSuccess {
                memberProfileCache[profile.uid] = profile
            }.onFailure { err ->
                _userProfileFlow.value = previousProfile
                emitSyncAlert(
                    message = "Failed to save profile: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    updateUserProfile(profile, onResult)
                }
            }
            onResult?.invoke(res)
        }
    }

    fun updateUserSettings(context: Context, updatedSettings: UserSettings) {
        val previousProfile = _userProfileFlow.value
        val updatedProfile = previousProfile.copy(settings = updatedSettings)
        _userProfileFlow.value = updatedProfile
        repositoryScope.launch {
            ThemePreferencesStore.saveAppearanceMode(context, updatedSettings.appearanceMode)
            val res = firestoreService?.updateUserSettings(updatedSettings, updatedProfile)
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            res.onSuccess {
                FcmTokenSyncHelper.syncTopics(updatedSettings.notifications)
            }.onFailure { err ->
                _userProfileFlow.value = previousProfile
                ThemePreferencesStore.saveAppearanceMode(context, previousProfile.settings.appearanceMode)
                emitSyncAlert(
                    message = "Failed to save settings: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    updateUserSettings(context, updatedSettings)
                }
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
            val res = firestoreService?.sendMessage(conversationId, newMsg)
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            res.onFailure { err ->
                emitSyncAlert(
                    message = "Message failed to send: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    firestoreService?.sendMessage(conversationId, newMsg)?.onFailure { retryErr ->
                        emitSyncAlert("Retry failed: ${retryErr.localizedMessage}")
                    }
                }
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

    suspend fun updateListing(
        existingItem: RentalItem,
        title: String,
        price: Int,
        deposit: Int = 0,
        location: String = "",
        category: RentalCategory,
        desc: String,
        imageUrl: String? = null,
        imageUrls: List<String> = emptyList(),
        availableToday: Boolean = existingItem.availableToday,
        latitude: Double? = existingItem.latitude,
        longitude: Double? = existingItem.longitude
    ): Result<RentalItem> {
        val service = firestoreService
            ?: return Result.failure(IllegalStateException("Sign in required to update listing."))
        val uid = Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))

        val profile = _userProfileFlow.value
        val ownerName = profile.displayName.ifBlank { existingItem.owner.name }
        val galleryUrls = imageUrls.filter { it.isNotBlank() }.take(4).ifEmpty {
            listOfNotNull(imageUrl?.takeIf { it.isNotBlank() })
        }
        val coverUrl = galleryUrls.firstOrNull()
        val locShare = profile.settings.locationSharing
        val effectiveLat = if (locShare == LocationSharingPreference.PRECISE) latitude else existingItem.latitude
        val effectiveLng = if (locShare == LocationSharingPreference.PRECISE) longitude else existingItem.longitude

        val updatedItem = existingItem.copy(
            title = title.trim(),
            category = category,
            pricePerDay = price.coerceAtLeast(1),
            location = location.trim(),
            description = desc.trim(),
            imageUrl = coverUrl,
            imageUrls = galleryUrls,
            imageCount = galleryUrls.size,
            availableToday = availableToday,
            securityDeposit = deposit.coerceAtLeast(0),
            latitude = effectiveLat,
            longitude = effectiveLng,
            owner = existingItem.owner.copy(
                id = existingItem.owner.id.ifBlank { uid },
                name = ownerName,
                initials = profile.initials.ifBlank { existingItem.owner.initials }
            )
        )

        return service.updateListing(updatedItem).onSuccess { savedItem ->
            _rawItemsFlow.value = _rawItemsFlow.value.map { if (it.id == savedItem.id) savedItem else it }
        }.onFailure {
            _syncErrorFlow.value = "Failed to update listing: ${it.localizedMessage}"
        }
    }

    fun toggleListingAvailability(itemId: String) {
        val previousItems = _rawItemsFlow.value
        val current = previousItems.find { it.id == itemId } ?: return
        val hasActiveBooking = !current.availableToday && (
            current.activeBookingId != null ||
                _rawBookingsFlow.value.any {
                    it.item.id == itemId && (
                        it.status.equals("Confirmed", ignoreCase = true) ||
                            it.status.equals("Active", ignoreCase = true)
                        )
                }
            )
        if (hasActiveBooking) {
            emitSyncAlert("This item is locked by an active reservation. Complete or cancel the booking first.")
            return
        }
        val nextAvailable = !current.availableToday
        _rawItemsFlow.value = previousItems.map { item ->
            if (item.id == itemId) item.copy(availableToday = nextAvailable) else item
        }
        repositoryScope.launch {
            val res = firestoreService?.updateListingAvailability(itemId, nextAvailable)
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            res.onFailure { err ->
                _rawItemsFlow.value = previousItems
                emitSyncAlert(
                    message = "Failed to update listing status: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    toggleListingAvailability(itemId)
                }
            }
        }
    }

    fun deleteListing(itemId: String) {
        val previousItems = _rawItemsFlow.value
        val current = previousItems.find { it.id == itemId }
        val hasActiveBooking = current?.activeBookingId != null || _rawBookingsFlow.value.any {
            it.item.id == itemId && (
                it.status.equals("Confirmed", ignoreCase = true) ||
                    it.status.equals("Active", ignoreCase = true)
                )
        }
        if (hasActiveBooking) {
            emitSyncAlert("Cannot delete a listing with an active reservation.")
            return
        }
        _rawItemsFlow.value = previousItems.filterNot { it.id == itemId }
        repositoryScope.launch {
            val res = firestoreService?.deleteListing(itemId)
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            res.onFailure { err ->
                _rawItemsFlow.value = previousItems
                emitSyncAlert(
                    message = "Failed to delete listing: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    deleteListing(itemId)
                }
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

        val liveItem = _rawItemsFlow.value.find { it.id == item.id } ?: item
        if (liveItem.owner.id.isBlank() || liveItem.owner.id == uid) {
            val msg = "You cannot reserve your own listing."
            emitSyncAlert(msg)
            return Result.failure(IllegalStateException(msg))
        }
        val profile = _userProfileFlow.value
        if (liveItem.owner.id in profile.settings.blockedUserIds) {
            val msg = "Unblock ${liveItem.owner.name} in Settings to reserve this item."
            emitSyncAlert(msg)
            return Result.failure(IllegalStateException(msg))
        }
        if (!liveItem.availableToday || liveItem.activeBookingId != null) {
            val msg = "This item is currently unavailable for booking."
            emitSyncAlert(msg)
            return Result.failure(IllegalStateException(msg))
        }

        val clampedDays = daysCount.coerceIn(1, 30)
        val deliveryFee = if (deliveryOption.contains("Delivery", ignoreCase = true)) 100 else 0
        val total = (liveItem.pricePerDay * clampedDays) + 50 + deliveryFee
        val otp = (1000..9999).random().toString()

        val booking = FirestoreBooking(
            id = "book_${System.currentTimeMillis()}",
            renterId = uid,
            renterName = profile.displayName.ifBlank { Firebase.auth.currentUser?.displayName ?: "Member" },
            hostId = liveItem.owner.id,
            hostName = liveItem.owner.name.ifBlank { "Host" },
            itemId = liveItem.id,
            itemTitle = liveItem.title,
            startDate = startDate,
            endDate = endDate,
            daysCount = clampedDays,
            pricePerDay = liveItem.pricePerDay,
            totalAmount = total,
            status = "Confirmed",
            deliveryOption = deliveryOption,
            handoverOtp = otp
        )

        val createdResult = service.createBookingTransaction(booking)
        createdResult.onSuccess { saved ->
            _rawItemsFlow.value = _rawItemsFlow.value.map {
                if (it.id == liveItem.id) it.copy(availableToday = false, activeBookingId = saved.id) else it
            }
            invalidateMemberProfile(saved.hostId)
            dispatchBookingChatUpdate(
                renterId = saved.renterId,
                renterName = saved.renterName,
                hostId = saved.hostId,
                hostName = saved.hostName,
                item = liveItem,
                messageText = "Reservation Confirmed: ${saved.itemTitle} (${saved.startDate} – ${saved.endDate}, ₹${saved.totalAmount}). Handover PIN will be exchanged at pickup."
            )
        }.onFailure { err ->
            emitSyncAlert("Booking failed: ${err.localizedMessage}")
        }
        return createdResult
    }

    suspend fun verifyHandoverPin(bookingId: String, enteredPin: String): Result<Unit> {
        val service = firestoreService
            ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        val uid = Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))
        val cached = _rawBookingsFlow.value.find { it.id == bookingId }
        if (cached != null) {
            if (cached.hostId != uid) {
                return Result.failure(IllegalStateException("Only the host can verify handover PIN."))
            }
            if (!cached.status.equals("Confirmed", ignoreCase = true)) {
                return Result.failure(IllegalStateException("Reservation must be Confirmed to hand over."))
            }
            if (enteredPin.trim() != cached.handoverOtp.trim()) {
                return Result.failure(IllegalArgumentException("Incorrect 4-digit handover PIN."))
            }
        }
        val txRes = service.verifyHandoverPinTransaction(bookingId, enteredPin)
        return txRes.map { updated ->
            _rawBookingsFlow.value = _rawBookingsFlow.value.map {
                if (it.id == bookingId) it.copy(status = "Active", isCompleted = false) else it
            }
            val liveItem = _rawItemsFlow.value.find { it.id == updated.itemId } ?: cached?.item
            dispatchBookingChatUpdate(
                renterId = updated.renterId,
                renterName = updated.renterName,
                hostId = updated.hostId,
                hostName = updated.hostName,
                item = liveItem,
                messageText = "Handover verified for ${updated.itemTitle}. Your rental is now Active!"
            )
            Unit
        }.onFailure { err ->
            if (err !is IllegalArgumentException) {
                emitSyncAlert(
                    message = "Could not confirm handover: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    verifyHandoverPin(bookingId, enteredPin)
                }
            }
        }
    }

    suspend fun completeBooking(bookingId: String): Result<Unit> {
        val service = firestoreService
            ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        val uid = Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))
        val cached = _rawBookingsFlow.value.find { it.id == bookingId }
        if (cached != null && cached.hostId != uid) {
            return Result.failure(IllegalStateException("Only the host can mark gear returned."))
        }
        val txRes = service.completeBookingTransaction(bookingId)
        return txRes.map { updated ->
            _rawBookingsFlow.value = _rawBookingsFlow.value.map {
                if (it.id == bookingId) it.copy(status = "Completed", isCompleted = true) else it
            }
            _rawItemsFlow.value = _rawItemsFlow.value.map {
                if (it.id == updated.itemId) it.copy(availableToday = true, activeBookingId = null) else it
            }
            invalidateMemberProfile(updated.hostId)
            invalidateMemberProfile(updated.renterId)
            val liveItem = _rawItemsFlow.value.find { it.id == updated.itemId } ?: cached?.item
            dispatchBookingChatUpdate(
                renterId = updated.renterId,
                renterName = updated.renterName,
                hostId = updated.hostId,
                hostName = updated.hostName,
                item = liveItem,
                messageText = "${updated.itemTitle} marked returned & completed. ${updated.renterName.ifBlank { "Renter" }}, you can now rate & review this piece in Reservations."
            )
            Unit
        }.onFailure { err ->
            emitSyncAlert(
                message = "Could not complete booking: ${err.localizedMessage}",
                retryLabel = "Retry"
            ) {
                completeBooking(bookingId)
            }
        }
    }

    suspend fun cancelBooking(bookingId: String): Result<Unit> {
        val service = firestoreService
            ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        val uid = Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))
        val cached = _rawBookingsFlow.value.find { it.id == bookingId }
        if (cached != null) {
            if (cached.renterId != uid && cached.hostId != uid) {
                return Result.failure(IllegalStateException("Only participants can cancel this reservation."))
            }
            if (!cached.status.equals("Confirmed", ignoreCase = true)) {
                return Result.failure(IllegalStateException("Active rentals must be returned rather than cancelled."))
            }
        }
        val txRes = service.cancelBookingTransaction(bookingId)
        return txRes.map { updated ->
            _rawBookingsFlow.value = _rawBookingsFlow.value.map {
                if (it.id == bookingId) it.copy(status = "Cancelled", isCompleted = true) else it
            }
            _rawItemsFlow.value = _rawItemsFlow.value.map {
                if (it.id == updated.itemId) it.copy(availableToday = true, activeBookingId = null) else it
            }
            invalidateMemberProfile(updated.hostId)
            invalidateMemberProfile(updated.renterId)
            val liveItem = _rawItemsFlow.value.find { it.id == updated.itemId } ?: cached?.item
            dispatchBookingChatUpdate(
                renterId = updated.renterId,
                renterName = updated.renterName,
                hostId = updated.hostId,
                hostName = updated.hostName,
                item = liveItem,
                messageText = "Reservation for ${updated.itemTitle} (${updated.startDate} – ${updated.endDate}) was cancelled. The listing is open again."
            )
            Unit
        }.onFailure { err ->
            emitSyncAlert(
                message = "Could not cancel booking: ${err.localizedMessage}",
                retryLabel = "Retry"
            ) {
                cancelBooking(bookingId)
            }
        }
    }

    suspend fun submitBookingReview(
        bookingId: String,
        rating: Int,
        comment: String
    ): Result<Unit> {
        val service = firestoreService
            ?: return Result.failure(IllegalStateException("Not connected to cloud session."))
        val uid = Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required."))
        val cached = _rawBookingsFlow.value.find { it.id == bookingId }
        if (cached != null) {
            if (cached.renterId != uid) {
                return Result.failure(IllegalStateException("Only the borrower can review this rental."))
            }
            if (!cached.status.equals("Completed", ignoreCase = true)) {
                return Result.failure(IllegalStateException("Complete the rental before leaving a review."))
            }
            if (cached.isReviewed) {
                return Result.failure(IllegalStateException("You have already reviewed this rental."))
            }
        }
        val clampedRating = rating.coerceIn(1, 5)
        val cleanedComment = comment.trim()
        val reviewRes = service.submitBookingReviewTransaction(bookingId, clampedRating, cleanedComment)
        return reviewRes.map { (avg, count) ->
            _rawBookingsFlow.value = _rawBookingsFlow.value.map {
                if (it.id == bookingId) {
                    it.copy(isReviewed = true, ratingGiven = clampedRating, reviewComment = cleanedComment)
                } else it
            }
            val targetItemId = cached?.item?.id
            if (!targetItemId.isNullOrBlank()) {
                _rawItemsFlow.value = _rawItemsFlow.value.map {
                    if (it.id == targetItemId) it.copy(rating = avg, reviewCount = count) else it
                }
            }
            cached?.hostId?.let { invalidateMemberProfile(it) }
            Unit
        }.onFailure { err ->
            emitSyncAlert(
                message = "Could not submit review: ${err.localizedMessage}",
                retryLabel = "Retry"
            ) {
                submitBookingReview(bookingId, rating, comment)
            }
        }
    }

    private fun dispatchBookingChatUpdate(
        renterId: String,
        renterName: String,
        hostId: String,
        hostName: String,
        item: RentalItem?,
        messageText: String
    ) {
        val service = firestoreService ?: return
        val currentUid = Firebase.auth.currentUser?.uid ?: activeSessionUid ?: return
        val isRenterActing = currentUid == renterId
        val targetId = if (isRenterActing) hostId else renterId
        val targetName = if (isRenterActing) hostName.ifBlank { "Host" } else renterName.ifBlank { "Member" }
        if (targetId.isBlank() || targetId == currentUid) return
        if (targetId in _userProfileFlow.value.settings.blockedUserIds) return

        val senderDisplay = _userProfileFlow.value.displayName.ifBlank {
            Firebase.auth.currentUser?.displayName ?: "Loop Member"
        }
        val targetInitials = targetName.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
            .ifBlank { "L" }
        val counterpartyOwner = RentalOwner(
            id = targetId,
            name = targetName,
            badge = if (isRenterActing) (item?.owner?.badge ?: "Host") else "Verified Borrower",
            initials = targetInitials
        )
        repositoryScope.launch {
            try {
                val conv = service.createOrGetConversation(host = counterpartyOwner, item = item).getOrNull()
                    ?: return@launch
                val msg = RentalMessage(
                    id = "msg_status_${System.currentTimeMillis()}",
                    senderName = senderDisplay,
                    text = messageText,
                    timestamp = "Just now",
                    isFromMe = true,
                    senderId = currentUid,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.SENT
                )
                service.sendMessage(conv.id, msg)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Automated booking chat update skipped: ${e.message}")
            }
        }
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        repositoryScope.launch {
            when {
                status.equals("Completed", ignoreCase = true) -> completeBooking(bookingId)
                status.equals("Cancelled", ignoreCase = true) -> cancelBooking(bookingId)
                else -> firestoreService?.updateBookingStatus(bookingId, status)?.onFailure {
                    emitSyncAlert("Could not update booking: ${it.localizedMessage}")
                }
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
        longitude: Double? = null,
        imageUrl: String? = null
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
            authorName = profile.displayName.ifBlank { Firebase.auth.currentUser?.displayName ?: "Community Member" },
            imageUrl = imageUrl?.takeIf { it.isNotBlank() }
        )

        val previousPosts = _rawCommunityPostsFlow.value
        _rawCommunityPostsFlow.value = listOf(newPost) + previousPosts

        repositoryScope.launch {
            val res = communityRepo?.createEvent(newPost)
                ?: firestoreService?.createCommunityPost(newPost)
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            res.onFailure { err ->
                _rawCommunityPostsFlow.value = previousPosts
                emitSyncAlert(
                    message = "Failed to publish post: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    _rawCommunityPostsFlow.value = listOf(newPost) + _rawCommunityPostsFlow.value.filterNot { it.id == newPost.id }
                    val retryRes = communityRepo?.createEvent(newPost)
                        ?: firestoreService?.createCommunityPost(newPost)
                        ?: Result.failure(IllegalStateException("Not connected"))
                    retryRes.onFailure { rErr ->
                        _rawCommunityPostsFlow.value = previousPosts
                        emitSyncAlert("Retry failed: ${rErr.localizedMessage}")
                    }
                }
            }
        }
        return newPost
    }

    fun updateCommunityPost(updatedPost: UserCommunityPost) {
        val previousPosts = _rawCommunityPostsFlow.value
        val cleaned = updatedPost.copy(
            title = updatedPost.title.trim(),
            description = updatedPost.description.trim(),
            location = updatedPost.location.trim(),
            dateTime = updatedPost.dateTime.trim(),
            imageUrl = updatedPost.imageUrl?.takeIf { it.isNotBlank() }
        )
        _rawCommunityPostsFlow.value = previousPosts.map { post ->
            if (post.id == cleaned.id) cleaned else post
        }
        repositoryScope.launch {
            val res = communityRepo?.updateEvent(cleaned)
                ?: firestoreService?.updateCommunityPost(cleaned)
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            res.onFailure { err ->
                _rawCommunityPostsFlow.value = previousPosts
                emitSyncAlert(
                    message = "Failed to update post: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    updateCommunityPost(cleaned)
                }
            }
        }
    }

    fun toggleCommunityPostStatus(postId: String) {
        val previousPosts = _rawCommunityPostsFlow.value
        val current = previousPosts.find { it.id == postId } ?: return
        val newStatus = if (current.status == CommunityPostStatus.ACTIVE) {
            CommunityPostStatus.COMPLETED
        } else {
            CommunityPostStatus.ACTIVE
        }
        _rawCommunityPostsFlow.value = previousPosts.map { post ->
            if (post.id == postId) post.copy(status = newStatus) else post
        }
        repositoryScope.launch {
            val res = communityRepo?.updateEventStatus(postId, newStatus)
                ?: firestoreService?.updateCommunityPostStatus(postId, newStatus)
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            res.onFailure { err ->
                _rawCommunityPostsFlow.value = previousPosts
                emitSyncAlert(
                    message = "Failed to update post status: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    toggleCommunityPostStatus(postId)
                }
            }
        }
    }

    fun deleteCommunityPost(postId: String) {
        val previousPosts = _rawCommunityPostsFlow.value
        _rawCommunityPostsFlow.value = previousPosts.filterNot { it.id == postId }
        repositoryScope.launch {
            val res = communityRepo?.deleteEvent(postId)
                ?: firestoreService?.deleteCommunityPost(postId)
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            res.onFailure { err ->
                _rawCommunityPostsFlow.value = previousPosts
                emitSyncAlert(
                    message = "Failed to delete post: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    deleteCommunityPost(postId)
                }
            }
        }
    }

    fun toggleFavorite(itemId: String) {
        val previousProfile = _userProfileFlow.value
        val currentFavs = previousProfile.favoriteItemIds.toMutableList()
        if (itemId in currentFavs) {
            currentFavs.remove(itemId)
        } else {
            currentFavs.add(itemId)
        }
        val updatedProfile = previousProfile.copy(favoriteItemIds = currentFavs)
        _userProfileFlow.value = updatedProfile
        repositoryScope.launch {
            val res = firestoreService?.updateFavoriteItems(currentFavs, updatedProfile)
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            res.onFailure { err ->
                _userProfileFlow.value = previousProfile
                emitSyncAlert(
                    message = "Could not update favorites: ${err.localizedMessage}",
                    retryLabel = "Retry"
                ) {
                    toggleFavorite(itemId)
                }
            }
        }
    }

    fun rsvpToCommunityPost(postId: String) {
        val uid = Firebase.auth.currentUser?.uid ?: _userProfileFlow.value.uid
        if (uid.isBlank()) {
            emitSyncAlert("Sign in required to RSVP.")
            return
        }
        val previousPosts = _rawCommunityPostsFlow.value
        val post = previousPosts.find { it.id == postId } ?: return
        if (post.authorId == uid) {
            emitSyncAlert("You are hosting this post.")
            return
        }
        if (post.status != CommunityPostStatus.ACTIVE) {
            emitSyncAlert("This community post is already resolved.")
            return
        }

        val nowAttending = uid !in post.attendeeIds
        val updatedIds = if (nowAttending) {
            (post.attendeeIds + uid).distinct()
        } else {
            post.attendeeIds.filterNot { it == uid }
        }
        val count = updatedIds.size
        val updatedText = when (post.type) {
            CommunityPostType.EVENT -> "$count ${if (count == 1) "creator" else "creators"} attending"
            CommunityPostType.HELP_REQUEST -> if (count == 0) "Seeking assistance" else "$count ${if (count == 1) "offer" else "offers"} to help"
            CommunityPostType.STUDY_GROUP -> "$count ${if (count == 1) "desk" else "desks"} booked"
        }

        _rawCommunityPostsFlow.value = previousPosts.map {
            if (it.id == postId) it.copy(attendeeIds = updatedIds, attendeesOrResponses = updatedText) else it
        }

        repositoryScope.launch {
            val result = communityRepo?.toggleRsvpForUser(postId, uid)?.map { it.first }
                ?: firestoreService?.updateCommunityPostRsvp(postId, updatedText, updatedIds)
                    ?.map { post.copy(attendeeIds = updatedIds, attendeesOrResponses = updatedText) }
                ?: Result.failure(IllegalStateException("Not connected to cloud session."))
            result.onFailure { err ->
                _rawCommunityPostsFlow.value = previousPosts
                emitSyncAlert(
                    message = err.localizedMessage ?: "Failed to update RSVP",
                    retryLabel = "Retry"
                ) {
                    rsvpToCommunityPost(postId)
                }
            }
        }
    }
}
