package com.example.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.models.AvatarPreset
import com.example.data.models.CommunityPostType
import com.example.data.models.DiscoveryPinType
import com.example.data.models.MapCategoryFilter
import com.example.data.models.RentalCategory
import com.example.data.models.UserActivityItem
import com.example.data.models.UserActivityType
import com.example.ui.screens.ProfileTab

fun RentalCategory.getIconVector(): ImageVector {
    return when (this) {
        RentalCategory.ELECTRONICS -> Icons.Default.Laptop
        RentalCategory.FURNITURE -> Icons.Default.Weekend
        RentalCategory.VEHICLES -> Icons.AutoMirrored.Filled.DirectionsBike
        RentalCategory.TOOLS -> Icons.Default.Build
        RentalCategory.EVENTS -> Icons.Default.CardGiftcard
        RentalCategory.STUDY_OFFICE -> Icons.Default.Computer
        RentalCategory.HOME_LIVING -> Icons.Default.Home
    }
}

fun MapCategoryFilter.getIconVector(): ImageVector {
    return when (this) {
        MapCategoryFilter.ALL -> Icons.Default.Star
        MapCategoryFilter.STUDY_GROUPS -> Icons.Default.MenuBook
        MapCategoryFilter.HELP_NEEDED -> Icons.Default.Handshake
        MapCategoryFilter.LOCAL_EVENTS -> Icons.Default.CameraAlt
        MapCategoryFilter.GEAR_HUBS -> Icons.Default.Lock
        MapCategoryFilter.MAKERS -> Icons.Default.Build
    }
}

fun CommunityPostType.getIconVector(): ImageVector {
    return when (this) {
        CommunityPostType.EVENT -> Icons.Default.CameraAlt
        CommunityPostType.HELP_REQUEST -> Icons.Default.Handshake
        CommunityPostType.STUDY_GROUP -> Icons.Default.MenuBook
    }
}

fun AvatarPreset.getIconVector(): ImageVector {
    return when (id) {
        0 -> Icons.Default.Person
        1 -> Icons.Default.CameraAlt
        2 -> Icons.Default.Eco
        3 -> Icons.Default.FlashOn
        4 -> Icons.Default.Headphones
        else -> Icons.Default.AutoAwesome
    }
}

fun ProfileTab.getIconVector(): ImageVector {
    return when (this) {
        ProfileTab.OVERVIEW -> Icons.Default.Person
        ProfileTab.MY_POSTS -> Icons.Default.Handshake
        ProfileTab.ACTIVITY -> Icons.Default.Schedule
    }
}

fun UserActivityType.getIconVector(): ImageVector {
    return when (this) {
        UserActivityType.RESERVATION -> Icons.Default.Handshake
        UserActivityType.LISTING_PUBLISHED -> Icons.Default.Inventory2
        UserActivityType.COMMUNITY_EVENT -> Icons.Default.CameraAlt
        UserActivityType.HELP_REQUEST -> Icons.Default.Handshake
        UserActivityType.STUDY_GROUP -> Icons.Default.MenuBook
        UserActivityType.HUB_CHECKIN -> Icons.Default.Lock
    }
}

fun UserActivityItem.getIconVector(): ImageVector = type.getIconVector()

fun DiscoveryPinType.getIconVector(): ImageVector {
    return when (this) {
        DiscoveryPinType.COMMUNITY_EVENT -> Icons.Default.CameraAlt
        DiscoveryPinType.HELP_REQUEST -> Icons.Default.Handshake
        DiscoveryPinType.STUDY_GROUP -> Icons.Default.MenuBook
        DiscoveryPinType.PICKUP_HUB -> Icons.Default.Lock
        DiscoveryPinType.USER_HOST -> Icons.Default.Build
        DiscoveryPinType.RENTAL_ITEM -> Icons.Default.Inventory2
    }
}

