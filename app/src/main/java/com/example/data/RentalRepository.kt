package com.example.data

import com.example.R
import com.example.data.firebase.FirestoreService
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object RentalRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val firestoreService = FirestoreService.instance

    val defaultOwner = RentalOwner(
        id = "owner_1",
        name = "Rakesh Kumar",
        badge = "Top Owner",
        initials = "RK",
        memberSince = "Joined 1 year ago",
        rating = 4.9f,
        totalRentals = 48,
        responseTime = "< 15 mins",
        phone = "+91 98401 22345"
    )

    val ownerPriya = RentalOwner(
        id = "owner_2",
        name = "Priya Sharma",
        badge = "Verified Owner",
        initials = "PS",
        memberSince = "Joined 6 months ago",
        rating = 4.8f,
        totalRentals = 22,
        responseTime = "< 5 mins",
        phone = "+91 97890 54321"
    )

    val ownerVikram = RentalOwner(
        id = "owner_3",
        name = "Vikram David",
        badge = "Superhost",
        initials = "VD",
        memberSince = "Joined 2 years ago",
        rating = 4.7f,
        totalRentals = 64,
        responseTime = "< 30 mins",
        phone = "+91 94432 19876"
    )

    val ownerAnanya = RentalOwner(
        id = "owner_4",
        name = "Ananya Nair",
        badge = "Top Owner",
        initials = "AN",
        memberSince = "Joined 8 months ago",
        rating = 4.9f,
        totalRentals = 35,
        responseTime = "< 10 mins",
        phone = "+91 99520 87654"
    )

    val ownerArun = RentalOwner(
        id = "owner_5",
        name = "Arun Prakash",
        badge = "Sound Specialist",
        initials = "AP",
        memberSince = "Joined 1 year ago",
        rating = 4.9f,
        totalRentals = 30,
        responseTime = "< 15 mins",
        phone = "+91 98840 11223"
    )

    val ownerSneha = RentalOwner(
        id = "owner_6",
        name = "Sneha Patel",
        badge = "Outdoor Guide",
        initials = "SP",
        memberSince = "Joined 4 months ago",
        rating = 4.8f,
        totalRentals = 18,
        responseTime = "< 20 mins",
        phone = "+91 97911 33445"
    )

    val items: List<RentalItem> = listOf(
        RentalItem(
            id = "item_canon_200d",
            title = "Canon EOS 200D",
            category = RentalCategory.ELECTRONICS,
            pricePerDay = 700,
            location = "Puducherry",
            rating = 4.9f,
            reviewCount = 32,
            description = "Well maintained Canon EOS 200D with 18-55mm lens. Perfect for travel, events and casual shoots. Comes with battery, charger and 32GB SD card.",
            primaryImageRes = R.drawable.canon_eos_camera_1790477967758,
            imageCount = 5,
            features = listOf(
                SpecFeature("CAMERA", "24.2 MP"),
                SpecFeature("LENS", "18-55mm Lens"),
                SpecFeature("STORAGE", "32GB SD Card"),
                SpecFeature("BAG", "Carry Bag Included")
            ),
            owner = defaultOwner,
            isPopular = true,
            isFavorite = true,
            availableToday = true
        ),
        RentalItem(
            id = "item_laptop",
            title = "Laptop",
            category = RentalCategory.ELECTRONICS,
            pricePerDay = 500,
            location = "Puducherry",
            rating = 4.8f,
            reviewCount = 19,
            description = "Ultra-fast modern laptop with M2 chip, 16GB RAM and 512GB SSD. Great for coding, video editing and office presentations.",
            primaryImageRes = R.drawable.modern_laptop_1790477981432,
            imageCount = 4,
            features = listOf(
                SpecFeature("CHIP", "M2 Chip"),
                SpecFeature("STORAGE", "512GB SSD"),
                SpecFeature("BATTERY", "18h Battery"),
                SpecFeature("BAG", "Sleeve Included")
            ),
            owner = ownerPriya,
            isPopular = true,
            isFavorite = false,
            availableToday = true
        ),
        RentalItem(
            id = "item_scooter",
            title = "Scooter",
            category = RentalCategory.VEHICLES,
            pricePerDay = 350,
            location = "Puducherry",
            rating = 4.7f,
            reviewCount = 45,
            description = "Smooth electric scooter with 85km full range. Helmet and fast home charger included. Helmet provided.",
            primaryImageRes = R.drawable.electric_scooter_1790477992190,
            imageCount = 3,
            features = listOf(
                SpecFeature("RANGE", "85 km Range"),
                SpecFeature("BATTERY", "Fast Charging"),
                SpecFeature("BAG", "Helmet Included"),
                SpecFeature("CAMERA", "Digital Lock")
            ),
            owner = ownerVikram,
            isPopular = true,
            isFavorite = false,
            availableToday = true
        ),
        RentalItem(
            id = "item_armchair",
            title = "Modern Armchair",
            category = RentalCategory.FURNITURE,
            pricePerDay = 250,
            location = "Puducherry",
            rating = 4.9f,
            reviewCount = 14,
            description = "Cozy Scandinavian beige armchair for photoshoots, guest staging, or temporary living setups.",
            primaryImageRes = R.drawable.modern_armchair_1790478015816,
            imageCount = 3,
            features = listOf(
                SpecFeature("BAG", "Fabric Cushion"),
                SpecFeature("LENS", "Ergonomic"),
                SpecFeature("CAMERA", "Stain Guard"),
                SpecFeature("CHIP", "Lightweight")
            ),
            owner = defaultOwner,
            isPopular = false,
            isFavorite = false,
            availableToday = true
        ),
        RentalItem(
            id = "item_projector",
            title = "Mini Projector",
            category = RentalCategory.STUDY_OFFICE,
            pricePerDay = 450,
            location = "Puducherry",
            rating = 4.8f,
            reviewCount = 28,
            description = "1080p full HD portable cinema projector with HDMI and wireless screen mirroring.",
            primaryImageRes = R.drawable.modern_projector_1790478027952,
            imageCount = 4,
            features = listOf(
                SpecFeature("CAMERA", "1080p Full HD"),
                SpecFeature("STORAGE", "HDMI & Wi-Fi"),
                SpecFeature("BAG", "Tripod Included"),
                SpecFeature("CHIP", "Speaker Built-in")
            ),
            owner = ownerAnanya,
            isPopular = false,
            isFavorite = false,
            availableToday = true
        )
    )

    val canonItem = items[0]
    val laptopItem = items[1]
    val scooterItem = items[2]
    val projectorItem = items[4]

    val initialConversations: List<ChatConversation> = listOf(
        ChatConversation(
            id = "conv_rakesh",
            owner = defaultOwner,
            itemContext = canonItem,
            isOnline = true,
            isTyping = false,
            unreadCount = 2,
            isPinned = true,
            messages = listOf(
                RentalMessage(
                    id = "m_rakesh_0",
                    senderName = "System",
                    text = "Rental Inquiry Started for Canon EOS 200D (₹700/day)",
                    timestamp = "10:28 AM",
                    isFromMe = false,
                    messageType = ChatMessageType.RENTAL_OFFER,
                    itemTitle = "Canon EOS 200D",
                    itemPricePerDay = 700,
                    itemImageRes = R.drawable.canon_eos_camera_1790477967758
                ),
                RentalMessage(
                    id = "m_rakesh_1",
                    senderName = "Rakesh Kumar",
                    text = "Hi Gokulan! Thanks for booking the Canon EOS 200D.",
                    timestamp = "10:30 AM",
                    isFromMe = false,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_rakesh_2",
                    senderName = "Me",
                    text = "Hi Rakesh! Is it possible to pick it up around 11 AM near White Town?",
                    timestamp = "10:32 AM",
                    isFromMe = true,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_rakesh_3",
                    senderName = "Rakesh Kumar",
                    text = "Voice note from Rakesh",
                    timestamp = "10:34 AM",
                    isFromMe = false,
                    messageType = ChatMessageType.AUDIO_NOTE,
                    audioDuration = "0:24",
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_rakesh_4",
                    senderName = "Rakesh Kumar",
                    text = "Pickup Location Pin",
                    timestamp = "10:35 AM",
                    isFromMe = false,
                    messageType = ChatMessageType.LOCATION_PIN,
                    locationTitle = "Café des Arts, White Town",
                    locationAddress = "10, Suffren St, White Town, Puducherry",
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_rakesh_5",
                    senderName = "Rakesh Kumar",
                    text = "Yes, absolutely! The camera is fully charged with the extra 32GB SD card packed. See you at 11 AM! 👍",
                    timestamp = "10:36 AM",
                    isFromMe = false,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.DELIVERED
                )
            )
        ),
        ChatConversation(
            id = "conv_priya",
            owner = ownerPriya,
            itemContext = laptopItem,
            isOnline = true,
            isTyping = false,
            unreadCount = 0,
            isPinned = false,
            messages = listOf(
                RentalMessage(
                    id = "m_priya_1",
                    senderName = "Me",
                    text = "Hi Priya! Is the MacBook / M2 Laptop available for this weekend?",
                    timestamp = "Yesterday",
                    isFromMe = true,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_priya_2",
                    senderName = "Priya Sharma",
                    text = "Yes, it is! Comes with the original 67W fast charger and sleeve.",
                    timestamp = "Yesterday",
                    isFromMe = false,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_priya_3",
                    senderName = "Me",
                    text = "Perfect, I've sent the booking request! 💻",
                    timestamp = "Yesterday",
                    isFromMe = true,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                )
            )
        ),
        ChatConversation(
            id = "conv_vikram",
            owner = ownerVikram,
            itemContext = scooterItem,
            isOnline = false,
            isTyping = false,
            unreadCount = 0,
            isPinned = false,
            messages = listOf(
                RentalMessage(
                    id = "m_vikram_1",
                    senderName = "Vikram David",
                    text = "Hey! Scooter battery is at 100% and helmet is sanitized.",
                    timestamp = "Tuesday",
                    isFromMe = false,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_vikram_2",
                    senderName = "Me",
                    text = "Thanks Vikram, will return it by 7 PM.",
                    timestamp = "Tuesday",
                    isFromMe = true,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                )
            )
        ),
        ChatConversation(
            id = "conv_ananya",
            owner = ownerAnanya,
            itemContext = projectorItem,
            isOnline = true,
            isTyping = false,
            unreadCount = 1,
            isPinned = false,
            messages = listOf(
                RentalMessage(
                    id = "m_ananya_1",
                    senderName = "Ananya Nair",
                    text = "Hi! Did the HDMI cable and tripod work well for your movie night? 🎬",
                    timestamp = "Monday",
                    isFromMe = false,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                )
            )
        )
    )

    val discoveryPins: List<DiscoveryPinItem> = listOf(
        DiscoveryPinItem(
            id = "pin_rakesh",
            title = "Rakesh Kumar",
            subtitle = "Canon DSLR & Lens Host",
            type = DiscoveryPinType.USER_HOST,
            latitude = 11.9338,
            longitude = 79.8350,
            rating = 4.9f,
            distance = "0.2 km away",
            tag = "★ Top Host",
            dateOrAvailability = "Available today",
            locationName = "White Town, Puducherry",
            priceOrAttendees = "₹700 / day",
            description = "Top rated equipment owner in White Town. Rents Canon DSLR cameras, tripods, prime lenses and studio accessories.",
            owner = defaultOwner,
            rentalItem = canonItem,
            imageRes = R.drawable.canon_eos_camera_1790477967758
        ),
        DiscoveryPinItem(
            id = "pin_event_photowalk",
            title = "Puducherry Sunset Photowalk",
            subtitle = "Promenade Rock Beach",
            type = DiscoveryPinType.COMMUNITY_EVENT,
            latitude = 11.9295,
            longitude = 79.8370,
            rating = 4.9f,
            distance = "0.4 km away",
            tag = "📸 Community Event",
            dateOrAvailability = "Today · 5:00 PM – 7:30 PM",
            locationName = "Rock Beach Promenade, White Town",
            priceOrAttendees = "18 creators attending",
            description = "Join 18 local photographers and creators for a golden hour photowalk along the Promenade Beach. Camera gear tryouts and lens sharing welcome!",
            isAttending = true
        ),
        DiscoveryPinItem(
            id = "pin_priya",
            title = "Priya Sharma",
            subtitle = "M2 Laptop & Workstation Host",
            type = DiscoveryPinType.USER_HOST,
            latitude = 11.9372,
            longitude = 79.8270,
            rating = 4.8f,
            distance = "0.6 km away",
            tag = "Verified Host",
            dateOrAvailability = "Available today",
            locationName = "MG Road, Heritage Town",
            priceOrAttendees = "₹500 / day",
            description = "Software engineer sharing powerful M2 laptops, mechanical keyboards, portable monitors, and fast chargers.",
            owner = ownerPriya,
            rentalItem = laptopItem,
            imageRes = R.drawable.modern_laptop_1790477981432
        ),
        DiscoveryPinItem(
            id = "pin_event_jam",
            title = "Acoustic Jam & Open Mic",
            subtitle = "Café des Arts Courtyard",
            type = DiscoveryPinType.COMMUNITY_EVENT,
            latitude = 11.9345,
            longitude = 79.8340,
            rating = 5.0f,
            distance = "0.3 km away",
            tag = "🎵 Community Meetup",
            dateOrAvailability = "Tomorrow · 6:30 PM",
            locationName = "Café des Arts, Suffren St",
            priceOrAttendees = "24 members attending",
            description = "Chill community acoustic evening with local musicians, guitar jams, storytelling, and hot filter coffee. Free entry for Loop community.",
            isAttending = false
        ),
        DiscoveryPinItem(
            id = "pin_vikram",
            title = "Vikram David",
            subtitle = "Electric Mobility & Scooter Host",
            type = DiscoveryPinType.USER_HOST,
            latitude = 11.9310,
            longitude = 79.8365,
            rating = 4.7f,
            distance = "0.5 km away",
            tag = "⚡ Superhost",
            dateOrAvailability = "Available today",
            locationName = "Goubert Ave, Puducherry",
            priceOrAttendees = "₹350 / day",
            description = "Eco mobility enthusiast offering maintained electric scooters with 85km range, helmets, and fast home chargers.",
            owner = ownerVikram,
            rentalItem = scooterItem,
            imageRes = R.drawable.electric_scooter_1790477992190
        ),
        DiscoveryPinItem(
            id = "pin_ananya",
            title = "Ananya Nair",
            subtitle = "Cinema & Projector Host",
            type = DiscoveryPinType.USER_HOST,
            latitude = 11.9510,
            longitude = 79.8210,
            rating = 4.9f,
            distance = "1.8 km away",
            tag = "Top Host",
            dateOrAvailability = "Available today",
            locationName = "Lawspet, Puducherry",
            priceOrAttendees = "₹450 / day",
            description = "Film student renting 1080p portable projectors, portable projection screens, and HDMI wireless transmitters.",
            owner = ownerAnanya,
            rentalItem = projectorItem,
            imageRes = R.drawable.modern_projector_1790478027952
        ),
        DiscoveryPinItem(
            id = "pin_event_beach_clean",
            title = "Auroville Eco Beach Ride & Clean",
            subtitle = "Auroville Beach Meet",
            type = DiscoveryPinType.COMMUNITY_EVENT,
            latitude = 11.9840,
            longitude = 79.8290,
            rating = 4.8f,
            distance = "5.2 km away",
            tag = "🌱 Eco Community",
            dateOrAvailability = "Sunday · 6:30 AM",
            locationName = "Auroville Beach Main Entrance",
            priceOrAttendees = "32 attending",
            description = "Community sunrise cycling ride to Auroville Beach followed by a beach cleanup and breakfast coconut water social.",
            isAttending = false
        ),
        DiscoveryPinItem(
            id = "pin_event_tech_coffee",
            title = "Indie Creators & Tech Coffee",
            subtitle = "Mission Street Hub",
            type = DiscoveryPinType.COMMUNITY_EVENT,
            latitude = 11.9360,
            longitude = 79.8300,
            rating = 4.9f,
            distance = "0.7 km away",
            tag = "💻 Tech Meetup",
            dateOrAvailability = "Saturday · 11:00 AM",
            locationName = "Mission St, Heritage Quarter",
            priceOrAttendees = "15 makers attending",
            description = "Weekly casual coffee meetup for indie hackers, mobile app developers, UI designers, and creators in Puducherry.",
            isAttending = false
        ),
        DiscoveryPinItem(
            id = "pin_sneha",
            title = "Sneha Patel",
            subtitle = "Camping & Trekking Gear Host",
            type = DiscoveryPinType.USER_HOST,
            latitude = 11.9355,
            longitude = 79.8310,
            rating = 4.8f,
            distance = "0.5 km away",
            tag = "Verified Host",
            dateOrAvailability = "Available today",
            locationName = "Mission Street",
            priceOrAttendees = "₹300 / day",
            description = "Backpacker renting 2-person waterproof Quechua tents, sleeping bags, trekking poles, and portable camp stoves.",
            owner = ownerSneha,
            imageRes = R.drawable.modern_armchair_1790478015816
        )
    )

    val sampleMessages: List<RentalMessage> = initialConversations.first().messages

    // Reactive StateFlows connected to Firestore live listeners
    private val _itemsFlow = MutableStateFlow(items)
    val itemsFlow: StateFlow<List<RentalItem>> = _itemsFlow.asStateFlow()

    private val _conversationsFlow = MutableStateFlow(initialConversations)
    val conversationsFlow: StateFlow<List<ChatConversation>> = _conversationsFlow.asStateFlow()

    private val _discoveryPinsFlow = MutableStateFlow(discoveryPins)
    val discoveryPinsFlow: StateFlow<List<DiscoveryPinItem>> = _discoveryPinsFlow.asStateFlow()

    init {
        // Start listening to real-time Firestore collections
        repositoryScope.launch {
            firestoreService.listenToItems().collectLatest { cloudItems ->
                if (cloudItems.isNotEmpty()) {
                    _itemsFlow.value = cloudItems
                }
            }
        }

        repositoryScope.launch {
            firestoreService.listenToConversations().collectLatest { cloudConvs ->
                if (cloudConvs.isNotEmpty()) {
                    _conversationsFlow.value = cloudConvs
                }
            }
        }
    }

    /**
     * Send a new message and sync with Firestore in background
     */
    fun sendMessage(
        conversationId: String,
        text: String,
        messageType: ChatMessageType = ChatMessageType.TEXT,
        locationTitle: String? = null,
        locationAddress: String? = null
    ) {
        val newMsg = RentalMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = "Me",
            text = text,
            timestamp = "Just now",
            isFromMe = true,
            messageType = messageType,
            status = MessageStatus.SENT,
            locationTitle = locationTitle,
            locationAddress = locationAddress
        )

        // Update local StateFlow immediately for zero-lag UI
        _conversationsFlow.value = _conversationsFlow.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(
                    messages = conv.messages + newMsg,
                    unreadCount = 0
                )
            } else conv
        }

        // Sync with Firestore Cloud Backend
        repositoryScope.launch {
            firestoreService.sendMessage(conversationId, newMsg)
        }
    }

    /**
     * Create listing and sync with Firestore in background
     */
    fun createListing(
        title: String,
        price: Int,
        category: RentalCategory,
        desc: String
    ): RentalItem {
        val newItem = RentalItem(
            id = "item_${System.currentTimeMillis()}",
            title = title,
            category = category,
            pricePerDay = price,
            location = "Puducherry",
            rating = 5.0f,
            reviewCount = 1,
            description = desc.ifBlank { "Newly listed item by Gokulan R." },
            primaryImageRes = when (category) {
                RentalCategory.ELECTRONICS, RentalCategory.STUDY_OFFICE -> R.drawable.modern_laptop_1790477981432
                RentalCategory.FURNITURE, RentalCategory.HOME_LIVING -> R.drawable.modern_armchair_1790478015816
                RentalCategory.VEHICLES -> R.drawable.electric_scooter_1790477992190
                else -> R.drawable.canon_eos_camera_1790477967758
            },
            imageCount = 3,
            features = listOf(
                SpecFeature("CAMERA", "Verified Item"),
                SpecFeature("BAG", "Accessories Included")
            ),
            owner = RentalOwner("owner_me", "Gokulan R", "New Host", "G", "Joined today", 5.0f),
            isPopular = true,
            isFavorite = false,
            availableToday = true
        )

        _itemsFlow.value = listOf(newItem) + _itemsFlow.value

        repositoryScope.launch {
            firestoreService.createListing(newItem)
        }

        return newItem
    }

    fun toggleFavorite(itemId: String) {
        _itemsFlow.value = _itemsFlow.value.map { item ->
            if (item.id == itemId) item.copy(isFavorite = !item.isFavorite) else item
        }
    }
}
