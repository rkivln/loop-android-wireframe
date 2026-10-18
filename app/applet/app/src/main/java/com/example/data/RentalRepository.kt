package com.example.data

import com.example.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RentalRepository {
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _items = MutableStateFlow(
        listOf(
            RentalItem(
                id = "item_1",
                title = "Canon EOS R5 Mirrorless Camera",
                category = RentalCategory.CAMERAS,
                dailyRate = 85.0,
                deposit = 500.0,
                rating = 4.9f,
                reviewCount = 28,
                location = "White Town, Puducherry",
                imageUrl = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=800&q=80",
                description = "Flagship full-frame mirrorless camera with 45MP sensor, 8K video recording, and lightning-fast autofocus. Perfect for editorial shoots and weddings.",
                owner = RentalOwner("Arjun Mehta", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80", 4.95f, 42, true),
                specs = mapOf("Sensor" to "45MP Full-Frame CMOS", "Video" to "8K RAW", "Mount" to "Canon RF")
            ),
            RentalItem(
                id = "item_2",
                title = "DJI Mavic 3 Pro Cine Drone",
                category = RentalCategory.DRONES,
                dailyRate = 110.0,
                deposit = 600.0,
                rating = 4.85f,
                reviewCount = 19,
                location = "Heritage Town, Puducherry",
                imageUrl = "https://images.unsplash.com/photo-1527977966376-1c8408f9f108?auto=format&fit=crop&w=800&q=80",
                description = "Tri-camera system flagship drone with Hasselblad optics and Apple ProRes 422 HQ encoding for cinematic aerial cinematography.",
                owner = RentalOwner("Kavitha Nair", "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=200&q=80", 4.88f, 31, true),
                specs = mapOf("Flight Time" to "43 mins", "Camera" to "Hasselblad 4/3 CMOS", "Format" to "Apple ProRes")
            ),
            RentalItem(
                id = "item_3",
                title = "Leica M6 35mm Film Camera",
                category = RentalCategory.CAMERAS,
                dailyRate = 65.0,
                deposit = 400.0,
                rating = 5.0f,
                reviewCount = 34,
                location = "French Quarter, Puducherry",
                imageUrl = "https://images.unsplash.com/photo-1500634245200-e524fc675315?auto=format&fit=crop&w=800&q=80",
                description = "Legendary mechanical rangefinder camera with mechanical shutter and brightline viewfinder. Pure analog analogue photography experience.",
                owner = RentalOwner("Jaya Nandhini G", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80", 5.0f, 18, true),
                specs = mapOf("Type" to "Mechanical Rangefinder", "Film" to "35mm", "Shutter" to "Mechanical cloth focal plane")
            ),
            RentalItem(
                id = "item_4",
                title = "Aputure 120d Mark II LED Light",
                category = RentalCategory.LIGHTING,
                dailyRate = 45.0,
                deposit = 250.0f.toDouble(),
                rating = 4.9f,
                reviewCount = 15,
                location = "Mission Street, Puducherry",
                imageUrl = "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80",
                description = "Powerful continuous daylight COB LED light fixture with DMX support, quiet cooling fan, and V-mount battery plate.",
                owner = RentalOwner("Ramesh Kumar", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=200&q=80", 4.75f, 22, false),
                specs = mapOf("CRI" to "96+", "TLCI" to "97+", "Color Temp" to "5500K")
            )
        )
    )
    val items: StateFlow<List<RentalItem>> = _items.asStateFlow()

    private val _conversations = MutableStateFlow(
        listOf(
            ChatConversation(
                conversationId = "conv_1",
                itemId = "item_1",
                itemTitle = "Canon EOS R5 Mirrorless Camera",
                itemImageUrl = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=800&q=80",
                otherUserName = "Arjun Mehta",
                otherUserAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
                lastMessage = "Hi Jaya! Is the camera available this weekend?",
                lastMessageTimestamp = System.currentTimeMillis() - 3600000,
                unreadCount = 1
            ),
            ChatConversation(
                conversationId = "conv_2",
                itemId = "item_2",
                itemTitle = "DJI Mavic 3 Pro Cine Drone",
                itemImageUrl = "https://images.unsplash.com/photo-1527977966376-1c8408f9f108?auto=format&fit=crop&w=800&q=80",
                otherUserName = "Kavitha Nair",
                otherUserAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=200&q=80",
                lastMessage = "Thanks for returning the drone in pristine condition!",
                lastMessageTimestamp = System.currentTimeMillis() - 86400000,
                unreadCount = 0
            )
        )
    )
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow(
        mapOf(
            "conv_1" to mutableListOf(
                RentalMessage("m1", "user_other", "Arjun Mehta", "Hi Jaya! Is the camera available this weekend?", System.currentTimeMillis() - 3600000, false, false)
            ),
            "conv_2" to mutableListOf(
                RentalMessage("m2", "user_other", "Kavitha Nair", "Thanks for returning the drone in pristine condition!", System.currentTimeMillis() - 86400000, true, false)
            )
        )
    )
    val messages: StateFlow<Map<String, List<RentalMessage>>> = _messages.asStateFlow()

    fun toggleFavorite(itemId: String) {
        _items.value = _items.value.map {
            if (it.id == itemId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun updateProfile(newProfile: UserProfile) {
        _userProfile.value = newProfile
    }

    fun sendMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        val currentMap = _messages.value.toMutableMap()
        val list = currentMap[conversationId]?.toMutableList() ?: mutableListOf()
        val newMessage = RentalMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderId = "user_me",
            senderName = _userProfile.value.displayName,
            text = text,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isTyping = false
        )
        list.add(newMessage)
        currentMap[conversationId] = list
        _messages.value = currentMap

        // Update conversation last message
        _conversations.value = _conversations.value.map { conv ->
            if (conv.conversationId == conversationId) {
                conv.copy(lastMessage = text, lastMessageTimestamp = System.currentTimeMillis())
            } else conv
        }
    }

    fun createListing(title: String, category: RentalCategory, dailyRate: Double, deposit: Double, location: String, description: String, imageUrl: String) {
        val newItem = RentalItem(
            id = "item_${System.currentTimeMillis()}",
            title = title,
            category = category,
            dailyRate = dailyRate,
            deposit = deposit,
            rating = 5.0f,
            reviewCount = 0,
            location = location,
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=800&q=80" },
            description = description,
            owner = RentalOwner(_userProfile.value.displayName, "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80", 5.0f, 18, true)
        )
        _items.value = listOf(newItem) + _items.value
        val p = _userProfile.value
        _userProfile.value = p.copy(listingsCount = p.listingsCount + 1)
    }
}
