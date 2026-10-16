package com.example.data

import android.content.Context
import com.example.data.firebase.FirestoreService
import com.example.data.local.AppDatabase
import com.example.data.local.UserProfileDao
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.models.CommunityEvent
import com.example.data.models.RentalItem
import com.example.data.models.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

object RentalRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val firestoreService = FirestoreService()
    private var userProfileDao: UserProfileDao? = null

    private val initialUserProfile = UserProfile(
        uid = "user_me",
        displayName = "Auroville Creator",
        email = "creator@loop.community",
        phone = "+91 98765 43210",
        bio = "Passionate photographer, ocean diver & DIY maker based in White Town, Puducherry. Happy to share gear & co-create!",
        location = "White Town, Puducherry",
        avatarPresetIndex = 0,
        customAvatarUrl = "",
        itemsLentCount = 14,
        itemsRentedCount = 8,
        rating = 4.95
    )

    private val _userProfileFlow = MutableStateFlow(initialUserProfile)
    val userProfileFlow: StateFlow<UserProfile> = _userProfileFlow.asStateFlow()

    private val sampleRentalItems = listOf(
        RentalItem(
            id = "item_1",
            title = "Sony Alpha A7 IV Mirrorless Camera",
            category = "Photography",
            description = "33MP Full-Frame camera with 28-70mm lens, 2 spare batteries and 128GB high-speed SD card. Perfect for sunrise shoots at Rock Beach.",
            pricePerDay = 1200.0,
            ownerName = "Siddharth",
            ownerRating = 4.9,
            location = "White Town, Puducherry",
            latitude = 11.9338,
            longitude = 79.8354,
            isAvailable = true,
            tags = listOf("Camera", "Sony", "Photography", "Video")
        ),
        RentalItem(
            id = "item_2",
            title = "Torq 7'2 Surfboard & Leash",
            category = "Surfing",
            description = "Great all-rounder surfboard suitable for beginner to intermediate waves at Serenity Beach or Bodhi Beach. Wax included.",
            pricePerDay = 450.0,
            ownerName = "Mathilde",
            ownerRating = 4.8,
            location = "Serenity Beach, Puducherry",
            latitude = 11.9675,
            longitude = 79.8450,
            isAvailable = true,
            tags = listOf("Surf", "Ocean", "Beach", "Water Sports")
        ),
        RentalItem(
            id = "item_3",
            title = "Auroville Handmade Electric Pottery Wheel",
            category = "Crafts",
            description = "Compact Shimpo-style wheel with variable speed foot pedal, bat pins and starter clay sculpting tool set.",
            pricePerDay = 600.0,
            ownerName = "Ananya",
            ownerRating = 5.0,
            location = "Auroville, Puducherry",
            latitude = 12.0070,
            longitude = 79.8107,
            isAvailable = true,
            tags = listOf("Pottery", "Ceramics", "Art", "DIY")
        ),
        RentalItem(
            id = "item_4",
            title = "Vintage French City Cruiser Bicycle",
            category = "Mobility",
            description = "7-speed heritage bicycle with front basket, helmet and phone mount. Perfect for exploring the French and Tamil quarters.",
            pricePerDay = 200.0,
            ownerName = "Jean-Luc",
            ownerRating = 4.9,
            location = "Heritage Town, Puducherry",
            latitude = 11.9360,
            longitude = 79.8310,
            isAvailable = true,
            tags = listOf("Bike", "Cycle", "Transport", "Heritage")
        ),
        RentalItem(
            id = "item_5",
            title = "Scubapro Diving Mask, Snorkel & Fins",
            category = "Diving",
            description = "Tempered glass panoramic scuba mask, purge snorkel and adjustable open-heel fins (size 8-10). Great for Temple Adventures reef dives.",
            pricePerDay = 350.0,
            ownerName = "Vikram",
            ownerRating = 4.7,
            location = "Goubert Avenue, Puducherry",
            latitude = 11.9312,
            longitude = 79.8360,
            isAvailable = true,
            tags = listOf("Diving", "Snorkel", "Ocean", "Reef")
        ),
        RentalItem(
            id = "item_6",
            title = "Anker Nebula 1080p Outdoor Cinema Projector",
            category = "Electronics",
            description = "Battery-powered projector with 90-inch portable folding screen, HDMI & Bluetooth audio for rooftop community movie nights.",
            pricePerDay = 850.0,
            ownerName = "Deepak",
            ownerRating = 5.0,
            location = "Muthialpet, Puducherry",
            latitude = 11.9480,
            longitude = 79.8290,
            isAvailable = true,
            tags = listOf("Projector", "Cinema", "Movies", "Screen")
        ),
        RentalItem(
            id = "item_7",
            title = "Quechua 3-Person Waterproof Camping Tent",
            category = "Outdoors",
            description = "Fresh & Black blackout technology tent, wind resistant up to 50 km/h, includes sleeping mats and solar camping lamp.",
            pricePerDay = 400.0,
            ownerName = "Priya",
            ownerRating = 4.85,
            location = "Lawspet, Puducherry",
            latitude = 11.9590,
            longitude = 79.8150,
            isAvailable = true,
            tags = listOf("Camping", "Tent", "Outdoors", "Trek")
        )
    )

    private val _rentalItemsFlow = MutableStateFlow(sampleRentalItems)
    val rentalItemsFlow: StateFlow<List<RentalItem>> = _rentalItemsFlow.asStateFlow()

    private val sampleCommunityEvents = listOf(
        CommunityEvent(
            id = "event_1",
            title = "Rock Beach Morning Eco Cleanup",
            description = "Join community volunteers to keep the promenade pristine. Gloves, recycling bags and morning tea provided.",
            category = "CLEANUP",
            locationName = "Promenade Beach, Puducherry",
            latitude = 11.9330,
            longitude = 79.8365,
            timeString = "Sunday, 6:30 AM",
            organizerName = "Clean Puducherry Org",
            participantCount = 28
        ),
        CommunityEvent(
            id = "event_2",
            title = "Sunset Watercolor & Clay Workshop",
            description = "Co-learning session on terracotta clay shaping and coastal watercolor sketching. Beginners warmly welcomed.",
            category = "WORKSHOP",
            locationName = "Auroville Visitors Centre",
            latitude = 12.0068,
            longitude = 79.8112,
            timeString = "Saturday, 4:00 PM",
            organizerName = "Auroville Art Collective",
            participantCount = 19
        ),
        CommunityEvent(
            id = "event_3",
            title = "Golden Hour French Quarter Photowalk",
            description = "Explore colonial yellow facades, bougainvillea gates, and street portrait techniques. Share lens gear & tips.",
            category = "MEETUP",
            locationName = "Bharathi Park, White Town",
            latitude = 11.9325,
            longitude = 79.8335,
            timeString = "Friday, 5:00 PM",
            organizerName = "Puducherry Shutterbugs",
            participantCount = 15
        ),
        CommunityEvent(
            id = "event_4",
            title = "Dawn Longboard & Surf Community Circle",
            description = "Open paddle out and wave riding at the northern break. Board sharing available for newcomers.",
            category = "SPORTS",
            locationName = "Serenity Beach Breakwater",
            latitude = 11.9680,
            longitude = 79.8455,
            timeString = "Tomorrow, 6:00 AM",
            organizerName = "East Coast Surfers",
            participantCount = 22
        ),
        CommunityEvent(
            id = "event_5",
            title = "Community Garden Compost Shift - Volunteer",
            description = "Help turn organic waste compost piles and plant native shade saplings at the heritage garden nursery.",
            category = "HELP NEEDED",
            locationName = "Botanical Garden, Puducherry",
            latitude = 11.9280,
            longitude = 79.8210,
            timeString = "Sunday, 8:00 AM",
            organizerName = "Green Pondy Trust",
            participantCount = 12
        )
    )

    private val _communityEventsFlow = MutableStateFlow(sampleCommunityEvents)
    val communityEventsFlow: StateFlow<List<CommunityEvent>> = _communityEventsFlow.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val filteredItems: StateFlow<List<RentalItem>> = combine(
        _rentalItemsFlow,
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                item.title.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true) ||
                        item.location.contains(query, ignoreCase = true) ||
                        item.tags.any { it.contains(query, ignoreCase = true) }
            }

            val matchesCategory = if (category.isNullOrBlank() || category == "All") {
                true
            } else {
                item.category.equals(category, ignoreCase = true)
            }

            matchesQuery && matchesCategory
        }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, sampleRentalItems)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = if (category == "All") null else category
    }

    fun initLocalDatabase(context: Context) {
        val database = AppDatabase.getInstance(context)
        val dao = database.userProfileDao()
        userProfileDao = dao

        repositoryScope.launch {
            val existing = dao.getProfile("user_me")
            if (existing == null) {
                dao.insertOrUpdateProfile(initialUserProfile.toEntity())
            } else {
                _userProfileFlow.value = existing.toDomain()
            }

            dao.getProfileFlow("user_me").collectLatest { entity ->
                if (entity != null) {
                    _userProfileFlow.value = entity.toDomain()
                }
            }
        }

        repositoryScope.launch {
            firestoreService.listenToUserProfile("user_me").collectLatest { cloudProfile ->
                _userProfileFlow.value = cloudProfile
                userProfileDao?.insertOrUpdateProfile(cloudProfile.toEntity())
            }
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        _userProfileFlow.value = profile
        repositoryScope.launch {
            userProfileDao?.insertOrUpdateProfile(profile.toEntity())
            firestoreService.saveUserProfile(profile)
        }
    }
}
