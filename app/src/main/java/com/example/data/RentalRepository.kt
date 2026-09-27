package com.example.data

import com.example.R
import com.example.data.models.DeliveryOptionType
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.data.models.RentalOwner
import com.example.data.models.SpecFeature

object RentalRepository {

    val defaultOwner = RentalOwner(
        id = "owner_1",
        name = "Rakesh Kumar",
        badge = "Top Owner",
        initials = "RK",
        memberSince = "Joined 1 year ago",
        rating = 4.9f,
        totalRentals = 48,
        responseTime = "< 15 mins"
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
            owner = RentalOwner(
                id = "owner_2",
                name = "Priya Sharma",
                badge = "Verified Owner",
                initials = "PS",
                memberSince = "Joined 6 months ago",
                rating = 4.8f
            ),
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
            owner = RentalOwner(
                id = "owner_3",
                name = "Vikram David",
                badge = "Superhost",
                initials = "VD",
                memberSince = "Joined 2 years ago",
                rating = 4.7f
            ),
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
            owner = RentalOwner(
                id = "owner_4",
                name = "Ananya Nair",
                badge = "Top Owner",
                initials = "AN",
                memberSince = "Joined 8 months ago",
                rating = 4.9f
            ),
            isPopular = false,
            isFavorite = false,
            availableToday = true
        )
    )

    val sampleMessages: List<RentalMessage> = listOf(
        RentalMessage(
            id = "m1",
            senderName = "Rakesh Kumar",
            text = "Hi Gokulan! Thanks for booking the Canon EOS 200D.",
            timestamp = "10:30 AM",
            isFromMe = false
        ),
        RentalMessage(
            id = "m2",
            senderName = "Me",
            text = "Hi Rakesh! Is it possible to pick it up around 11 AM near White Town?",
            timestamp = "10:32 AM",
            isFromMe = true
        ),
        RentalMessage(
            id = "m3",
            senderName = "Rakesh Kumar",
            text = "Yes, absolutely! The camera is fully charged with the extra SD card packed.",
            timestamp = "10:35 AM",
            isFromMe = false
        )
    )
}
