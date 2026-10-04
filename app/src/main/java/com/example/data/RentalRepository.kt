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
import com.example.data.models.UserProfile
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

    val items: List<RentalItem> = listOf(
        RentalItem(
            id = "item_leica_m",
            title = "Canon EOS & 35mm Glass",
            category = RentalCategory.ELECTRONICS,
            pricePerDay = 750,
            location = "White Town, Puducherry",
            rating = 4.9f,
            reviewCount = 38,
            description = "Impeccably maintained 35mm photographic setup with prime portrait glass. Natural film rendering, leather neck strap, extra high-capacity battery, and fast 64GB card included.",
            primaryImageRes = R.drawable.img_editorial_lifestyle_1_1791087263359,
            imageCount = 5,
            features = listOf(
                SpecFeature("CAMERA", "24.2 MP APS-C"),
                SpecFeature("LENS", "35mm f/1.8 Prime"),
                SpecFeature("STORAGE", "64GB Extreme SD"),
                SpecFeature("BAG", "Horween Leather Strap")
            ),
            owner = defaultOwner,
            isPopular = true,
            isFavorite = true,
            availableToday = true,
            securityDeposit = 1500
        ),
        RentalItem(
            id = "item_synth_station",
            title = "Analogue Synth & Studio Desk",
            category = RentalCategory.STUDY_OFFICE,
            pricePerDay = 850,
            location = "Heritage Quarter",
            rating = 4.9f,
            reviewCount = 24,
            description = "Complete creative desktop audio setup featuring compact analogue synthesizer, closed-back studio monitors, and oak workspace accessories for music production and podcast sessions.",
            primaryImageRes = R.drawable.img_editorial_studio_2_1791087277042,
            imageCount = 4,
            features = listOf(
                SpecFeature("CHIP", "Analogue Engine"),
                SpecFeature("STORAGE", "USB-C Audio Interface"),
                SpecFeature("BAG", "Hard Shell Flight Case"),
                SpecFeature("BATTERY", "Power Supply & Cables")
            ),
            owner = ownerPriya,
            isPopular = true,
            isFavorite = false,
            availableToday = true,
            securityDeposit = 2000
        ),
        RentalItem(
            id = "item_coastal_ebike",
            title = "Coastal Cruiser Electric E-Bike",
            category = RentalCategory.VEHICLES,
            pricePerDay = 450,
            location = "Goubert Avenue",
            rating = 4.8f,
            reviewCount = 52,
            description = "Minimalist matte scrambler e-bike with 75km range, integrated LED headlamp, woven front basket, and dual disk brakes. Ideal for scenic coastal rides along the Promenade.",
            primaryImageRes = R.drawable.img_editorial_outdoor_3_1791087293034,
            imageCount = 4,
            features = listOf(
                SpecFeature("RANGE", "75 km Range"),
                SpecFeature("BATTERY", "Quick-Swap Battery"),
                SpecFeature("BAG", "Kevlar Helmet Included"),
                SpecFeature("CAMERA", "Integrated U-Lock")
            ),
            owner = ownerVikram,
            isPopular = true,
            isFavorite = true,
            availableToday = true,
            securityDeposit = 1000
        ),
        RentalItem(
            id = "item_armchair",
            title = "Scandinavian Linen Armchair",
            category = RentalCategory.FURNITURE,
            pricePerDay = 300,
            location = "Suffren Street",
            rating = 4.9f,
            reviewCount = 19,
            description = "Artisanal neutral linen armchair with solid oak joinery. Sourced for architectural photoshoots, pop-up gallery lounges, and relaxed reading corners.",
            primaryImageRes = R.drawable.modern_armchair_1790478015816,
            imageCount = 3,
            features = listOf(
                SpecFeature("BAG", "Natural Linen Cover"),
                SpecFeature("LENS", "Solid White Oak"),
                SpecFeature("CHIP", "Lightweight Modular")
            ),
            owner = defaultOwner,
            isPopular = false,
            isFavorite = false,
            availableToday = true,
            securityDeposit = 800
        ),
        RentalItem(
            id = "item_projector",
            title = "Portable Cinema Projector",
            category = RentalCategory.EVENTS,
            pricePerDay = 500,
            location = "Lawspet Quarter",
            rating = 4.8f,
            reviewCount = 31,
            description = "Compact 1080p full HD cinematic projector with built-in Harman Kardon acoustics, lightweight tripod, and wireless AirPlay / Chromecast support for outdoor courtyard screenings.",
            primaryImageRes = R.drawable.modern_projector_1790478027952,
            imageCount = 4,
            features = listOf(
                SpecFeature("CAMERA", "1080p Cine Lens"),
                SpecFeature("STORAGE", "HDMI & AirPlay"),
                SpecFeature("BAG", "Carbon Tripod Bag"),
                SpecFeature("CHIP", "360° Audio Speaker")
            ),
            owner = ownerAnanya,
            isPopular = false,
            isFavorite = false,
            availableToday = true,
            securityDeposit = 1200
        ),
        RentalItem(
            id = "item_pro_laptop",
            title = "M2 Pro Workstation Laptop",
            category = RentalCategory.ELECTRONICS,
            pricePerDay = 600,
            location = "Mission Street",
            rating = 4.9f,
            reviewCount = 27,
            description = "High-performance laptop calibrated for Lightroom, DaVinci Resolve, and mobile development. Sourced with padded felt sleeve and 96W USB-C charger.",
            primaryImageRes = R.drawable.modern_laptop_1790477981432,
            imageCount = 4,
            features = listOf(
                SpecFeature("CHIP", "M2 Pro 12-Core"),
                SpecFeature("STORAGE", "1TB Fast NVMe"),
                SpecFeature("BATTERY", "16h Battery Life"),
                SpecFeature("BAG", "Felt Wool Sleeve")
            ),
            owner = ownerPriya,
            isPopular = false,
            isFavorite = false,
            availableToday = true,
            securityDeposit = 2500
        )
    )

    val initialConversations: List<ChatConversation> = listOf(
        ChatConversation(
            id = "conv_rakesh",
            owner = defaultOwner,
            itemContext = items[0],
            isOnline = true,
            isTyping = false,
            unreadCount = 1,
            isPinned = true,
            messages = listOf(
                RentalMessage(
                    id = "m_rakesh_0",
                    senderName = "System",
                    text = "Inquiry started for Canon EOS & 35mm Glass (₹750/day)",
                    timestamp = "10:28 AM",
                    isFromMe = false,
                    messageType = ChatMessageType.RENTAL_OFFER,
                    itemTitle = "Canon EOS & 35mm Glass",
                    itemPricePerDay = 750,
                    itemImageRes = R.drawable.img_editorial_lifestyle_1_1791087263359
                ),
                RentalMessage(
                    id = "m_rakesh_1",
                    senderName = "Rakesh Kumar",
                    text = "Hello! Looking forward to passing over the 35mm kit. The glass is freshly cleaned.",
                    timestamp = "10:30 AM",
                    isFromMe = false,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_rakesh_2",
                    senderName = "Me",
                    text = "Hi Rakesh! Could we meet around 11 AM near Café des Arts in White Town?",
                    timestamp = "10:32 AM",
                    isFromMe = true,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_rakesh_4",
                    senderName = "Rakesh Kumar",
                    text = "Pickup Location",
                    timestamp = "10:35 AM",
                    isFromMe = false,
                    messageType = ChatMessageType.LOCATION_PIN,
                    locationTitle = "Café des Arts Courtyard",
                    locationAddress = "10, Suffren St, White Town, Puducherry",
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_rakesh_5",
                    senderName = "Rakesh Kumar",
                    text = "Perfect! I'll have the camera, leather strap, and extra battery ready for you. See you there.",
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
            itemContext = items[1],
            isOnline = true,
            isTyping = false,
            unreadCount = 0,
            isPinned = false,
            messages = listOf(
                RentalMessage(
                    id = "m_priya_1",
                    senderName = "Me",
                    text = "Hi Priya! Is the synth station available for recording this Saturday?",
                    timestamp = "Yesterday",
                    isFromMe = true,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                ),
                RentalMessage(
                    id = "m_priya_2",
                    senderName = "Priya Sharma",
                    text = "Yes, it is! Comes packed in the flight case with all required cables.",
                    timestamp = "Yesterday",
                    isFromMe = false,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                )
            )
        ),
        ChatConversation(
            id = "conv_vikram",
            owner = ownerVikram,
            itemContext = items[2],
            isOnline = false,
            isTyping = false,
            unreadCount = 0,
            isPinned = false,
            messages = listOf(
                RentalMessage(
                    id = "m_vikram_1",
                    senderName = "Vikram David",
                    text = "Coastal E-bike is fully charged and tire pressure is dialed in.",
                    timestamp = "Tuesday",
                    isFromMe = false,
                    messageType = ChatMessageType.TEXT,
                    status = MessageStatus.READ
                )
            )
        )
    )

    val sampleMessages: List<RentalMessage> = initialConversations.first().messages

    val discoveryPins: List<DiscoveryPinItem> = listOf(
        DiscoveryPinItem(
            id = "pin_rakesh",
            title = "Rakesh Kumar",
            subtitle = "35mm Optics & Cameras",
            type = DiscoveryPinType.USER_HOST,
            latitude = 11.9338,
            longitude = 79.8350,
            rating = 4.9f,
            distance = "0.2 km away",
            tag = "★ Master Host",
            dateOrAvailability = "Available today",
            locationName = "Suffren St, White Town",
            priceOrAttendees = "₹750 / day",
            description = "Dedicated photographer renting vintage 35mm bodies, portrait lenses, and hard shell flight cases.",
            owner = defaultOwner,
            rentalItem = items[0],
            imageRes = R.drawable.img_editorial_lifestyle_1_1791087263359
        ),
        DiscoveryPinItem(
            id = "pin_event_photowalk",
            title = "Promenade Golden Hour Walk",
            subtitle = "Rock Beach Promenade",
            type = DiscoveryPinType.COMMUNITY_EVENT,
            latitude = 11.9295,
            longitude = 79.8370,
            rating = 4.9f,
            distance = "0.4 km away",
            tag = "📸 Photo Walk",
            dateOrAvailability = "Today · 5:00 PM – 7:00 PM",
            locationName = "Promenade Rock Beach",
            priceOrAttendees = "18 creators",
            description = "Casual twilight gathering of local photographers exploring the French Quarter seafront with shared lenses and analogue cameras.",
            isAttending = true
        ),
        DiscoveryPinItem(
            id = "pin_priya",
            title = "Priya Sharma",
            subtitle = "Audio & Studio Gear",
            type = DiscoveryPinType.USER_HOST,
            latitude = 11.9372,
            longitude = 79.8270,
            rating = 4.8f,
            distance = "0.6 km away",
            tag = "Verified Host",
            dateOrAvailability = "Available today",
            locationName = "Heritage Quarter",
            priceOrAttendees = "₹850 / day",
            description = "Producer sharing synthesizers, studio audio interfaces, and high-spec creative laptops.",
            owner = ownerPriya,
            rentalItem = items[1],
            imageRes = R.drawable.img_editorial_studio_2_1791087277042
        ),
        DiscoveryPinItem(
            id = "pin_vikram",
            title = "Vikram David",
            subtitle = "Coastal E-Bikes",
            type = DiscoveryPinType.USER_HOST,
            latitude = 11.9310,
            longitude = 79.8365,
            rating = 4.8f,
            distance = "0.5 km away",
            tag = "⚡ Superhost",
            dateOrAvailability = "Available today",
            locationName = "Goubert Avenue",
            priceOrAttendees = "₹450 / day",
            description = "Curator of electric mobility and scrambler cruisers for seamless coastal wandering.",
            owner = ownerVikram,
            rentalItem = items[2],
            imageRes = R.drawable.img_editorial_outdoor_3_1791087293034
        )
    )

    val initialUserProfile = UserProfile(
        uid = "user_me",
        displayName = "Gokulan R",
        email = "gokulan.rkivln@gmail.com",
        phone = "+91 98401 22345",
        bio = "Designer and photographer based in White Town, Puducherry. Passionate about tactile objects, analogue processes, and quiet travels.",
        location = "White Town, Puducherry",
        avatarPresetIndex = 0,
        rating = 5.0f,
        reviewCount = 18,
        rentalsCompleted = 14,
        listingsCount = 2,
        earnedAmount = 2450,
        isVerified = true,
        memberSince = "Member since 2024",
        badges = listOf("✓ ID Verified", "★ Curated Host", "📸 35mm Club", "⚡ Quick Responder")
    )

    private val _itemsFlow = MutableStateFlow(items)
    val itemsFlow: StateFlow<List<RentalItem>> = _itemsFlow.asStateFlow()

    private val _conversationsFlow = MutableStateFlow(initialConversations)
    val conversationsFlow: StateFlow<List<ChatConversation>> = _conversationsFlow.asStateFlow()

    private val _discoveryPinsFlow = MutableStateFlow(discoveryPins)
    val discoveryPinsFlow: StateFlow<List<DiscoveryPinItem>> = _discoveryPinsFlow.asStateFlow()

    private val _userProfileFlow = MutableStateFlow(initialUserProfile)
    val userProfileFlow: StateFlow<UserProfile> = _userProfileFlow.asStateFlow()

    init {
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

        repositoryScope.launch {
            firestoreService.listenToUserProfile("user_me").collectLatest { cloudProfile ->
                _userProfileFlow.value = cloudProfile
            }
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        _userProfileFlow.value = profile
        repositoryScope.launch {
            firestoreService.updateUserProfile(profile)
        }
    }

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

        _conversationsFlow.value = _conversationsFlow.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(
                    messages = conv.messages + newMsg,
                    unreadCount = 0
                )
            } else conv
        }

        repositoryScope.launch {
            firestoreService.sendMessage(conversationId, newMsg)
        }
    }

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
            location = "White Town, Puducherry",
            rating = 5.0f,
            reviewCount = 1,
            description = desc.ifBlank { "Newly listed object curated by Gokulan R." },
            primaryImageRes = when (category) {
                RentalCategory.ELECTRONICS, RentalCategory.STUDY_OFFICE -> R.drawable.img_editorial_studio_2_1791087277042
                RentalCategory.FURNITURE, RentalCategory.HOME_LIVING -> R.drawable.modern_armchair_1790478015816
                RentalCategory.VEHICLES -> R.drawable.img_editorial_outdoor_3_1791087293034
                else -> R.drawable.img_editorial_lifestyle_1_1791087263359
            },
            imageCount = 3,
            features = listOf(
                SpecFeature("CAMERA", "Verified Object"),
                SpecFeature("BAG", "Accessories Included")
            ),
            owner = RentalOwner("owner_me", _userProfileFlow.value.displayName, "New Maker", _userProfileFlow.value.initials, "Joined today", 5.0f),
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
