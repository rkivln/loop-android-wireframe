package com.example.data.models

enum class HousePropertyType(val label: String, val shortLabel: String) {
    APARTMENT("Apartment", "Flat"),
    INDEPENDENT_HOUSE("Independent House", "House"),
    VILLA("Villa", "Villa"),
    STUDIO("Studio", "Studio"),
    SHARED_ROOM("Shared Room", "Shared");

    companion object {
        fun fromNameOrDefault(raw: String?, fallback: HousePropertyType = APARTMENT): HousePropertyType {
            return entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: fallback
        }
    }
}

enum class FurnishingStatus(val label: String) {
    UNFURNISHED("Unfurnished"),
    SEMI_FURNISHED("Semi-Furnished"),
    FULLY_FURNISHED("Fully Furnished");

    companion object {
        fun fromNameOrDefault(raw: String?, fallback: FurnishingStatus = SEMI_FURNISHED): FurnishingStatus {
            return entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: fallback
        }
    }
}

enum class TenantPreference(val label: String) {
    ANY("Everyone Welcome"),
    FAMILY("Families"),
    WORKING_PROFESSIONALS("Working Professionals"),
    STUDENTS("Students");

    companion object {
        fun fromNameOrDefault(raw: String?, fallback: TenantPreference = ANY): TenantPreference {
            return entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: fallback
        }
    }
}

enum class HouseAmenity(val label: String) {
    WIFI("Wi-Fi Ready"),
    AC("Air Conditioning"),
    PARKING_CAR("Car Parking"),
    PARKING_BIKE("Two-Wheeler Parking"),
    POWER_BACKUP("Power Backup"),
    BALCONY("Private Balcony"),
    MODULAR_KITCHEN("Modular Kitchen"),
    WASHING_MACHINE("Washing Machine"),
    PET_FRIENDLY("Pet-Friendly"),
    SECURITY_24X7("24/7 Security"),
    WATER_24X7("24/7 Water");

    companion object {
        fun fromNameOrNull(raw: String?): HouseAmenity? {
            return entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) }
        }
    }
}

enum class VisitTimeSlot(val label: String, val shortWindow: String) {
    MORNING("Morning (10 AM – 12 PM)", "10 AM – 12 PM"),
    AFTERNOON("Afternoon (2 PM – 5 PM)", "2 PM – 5 PM"),
    EVENING("Evening (5 PM – 7 PM)", "5 PM – 7 PM");

    companion object {
        fun fromNameOrDefault(raw: String?, fallback: VisitTimeSlot = MORNING): VisitTimeSlot {
            return entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: fallback
        }
    }
}

enum class VisitRequestStatus(val label: String) {
    REQUESTED("Visit Requested"),
    CONFIRMED("Visit Confirmed"),
    RESCHEDULE_SUGGESTED("New Time Suggested"),
    COMPLETED("Visit Completed"),
    DECLINED("Declined"),
    CANCELLED("Cancelled");

    val isTerminal: Boolean
        get() = this == COMPLETED || this == DECLINED || this == CANCELLED

    companion object {
        fun fromNameOrDefault(raw: String?, fallback: VisitRequestStatus = REQUESTED): VisitRequestStatus {
            return entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: fallback
        }
    }
}

val DEFAULT_ROOM_LABELS = listOf(
    "Living Room",
    "Bedroom",
    "Kitchen",
    "Exterior",
    "Balcony",
    "Bathroom"
)

data class HouseListing(
    val id: String,
    val title: String,
    val propertyType: HousePropertyType = HousePropertyType.APARTMENT,
    val bhkCount: Int = 2,
    val bathrooms: Int = 1,
    val areaSqFt: Int = 950,
    val floorNumber: Int = 1,
    val totalFloors: Int = 2,
    val furnishing: FurnishingStatus = FurnishingStatus.SEMI_FURNISHED,
    val tenantPreference: TenantPreference = TenantPreference.ANY,
    val monthlyRent: Int,
    val securityDeposit: Int = 0,
    val maintenanceMonthly: Int = 0,
    val minLeaseMonths: Int = 11,
    val availableFromLabel: String = "Immediate",
    val isAvailableForRent: Boolean = true,
    val location: String,
    val fullAddressNote: String = "",
    val description: String,
    val amenities: List<HouseAmenity> = emptyList(),
    val imageUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val photoLabels: List<String> = emptyList(),
    val imageCount: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val owner: RentalOwner,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isFavorite: Boolean = false,
    val createdAtEpoch: Long = 0L
) {
    val configurationLabel: String
        get() = when {
            propertyType == HousePropertyType.STUDIO || bhkCount <= 0 -> "Studio"
            else -> "$bhkCount BHK"
        }

    val resolvedGalleryUrls: List<String>
        get() = imageUrls.filter { it.isNotBlank() }
            .ifEmpty { listOfNotNull(imageUrl?.takeIf { it.isNotBlank() }) }
            .take(4)

    fun roomLabelAt(index: Int): String {
        return photoLabels.getOrNull(index)?.takeIf { it.isNotBlank() }
            ?: if (index == 0) "Cover Photo" else "Room ${index + 1}"
    }
}

data class HouseVisitRequest(
    val id: String,
    val houseId: String,
    val houseTitle: String,
    val houseLocation: String = "",
    val monthlyRent: Int = 0,
    val tenantId: String,
    val tenantName: String,
    val landlordId: String,
    val landlordName: String,
    val preferredDate: String,
    val preferredTimeSlot: VisitTimeSlot = VisitTimeSlot.MORNING,
    val occupantsCount: Int = 1,
    val householdType: TenantPreference = TenantPreference.ANY,
    val targetMoveInMonth: String = "Immediate",
    val tenantNote: String = "",
    val landlordResponseNote: String = "",
    val confirmedAddressNote: String = "",
    val status: VisitRequestStatus = VisitRequestStatus.REQUESTED,
    val conversationId: String? = null,
    val createdAtEpoch: Long = 0L,
    val updatedAtEpoch: Long = 0L
)

enum class HouseSortOption(val label: String) {
    FEATURED("Featured"),
    RENT_LOW_TO_HIGH("Rent: Low to High"),
    RENT_HIGH_TO_LOW("Rent: High to Low"),
    AREA_HIGH_TO_LOW("Largest Area"),
    TOP_RATED("Top Rated")
}

data class HouseFilterState(
    val sortOption: HouseSortOption = HouseSortOption.FEATURED,
    val propertyType: HousePropertyType? = null,
    val minBhk: Int? = null,
    val furnishing: FurnishingStatus? = null,
    val tenantPreference: TenantPreference? = null,
    val maxMonthlyRent: Int? = null,
    val petFriendlyOnly: Boolean = false,
    val availableOnly: Boolean = true,
    val savedOnly: Boolean = false
) {
    val activeFilterCount: Int
        get() = listOfNotNull(
            if (sortOption != HouseSortOption.FEATURED) 1 else null,
            if (propertyType != null) 1 else null,
            if (minBhk != null) 1 else null,
            if (furnishing != null) 1 else null,
            if (tenantPreference != null) 1 else null,
            if (maxMonthlyRent != null) 1 else null,
            if (petFriendlyOnly) 1 else null,
            if (!availableOnly) 1 else null,
            if (savedOnly) 1 else null
        ).size
}
