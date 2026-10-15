package com.example.data.firebase

import com.example.data.models.CommunityEvent
import com.example.data.models.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreService {
    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    fun listenToUserProfile(uid: String): Flow<UserProfile> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            trySend(UserProfile(uid = uid))
            channel.close()
            return@callbackFlow
        }

        val listener = fs.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(UserProfile(uid = uid))
                    return@addSnapshotListener
                }

                val profile = UserProfile(
                    uid = snapshot.getString("uid") ?: uid,
                    displayName = snapshot.getString("displayName") ?: "Auroville Creator",
                    email = snapshot.getString("email") ?: "creator@loop.community",
                    phone = snapshot.getString("phone") ?: "+91 98765 43210",
                    bio = snapshot.getString("bio") ?: "Passionate photographer, ocean diver & DIY maker based in White Town, Puducherry.",
                    location = snapshot.getString("location") ?: "White Town, Puducherry",
                    avatarPresetIndex = snapshot.getLong("avatarPresetIndex")?.toInt() ?: 0,
                    customAvatarUrl = snapshot.getString("customAvatarUrl") ?: "",
                    itemsLentCount = snapshot.getLong("itemsLentCount")?.toInt() ?: 14,
                    itemsRentedCount = snapshot.getLong("itemsRentedCount")?.toInt() ?: 8,
                    rating = snapshot.getDouble("rating") ?: 4.95
                )
                trySend(profile)
            }

        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val fs = firestore ?: return
        try {
            val data = mapOf(
                "uid" to profile.uid,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "phone" to profile.phone,
                "bio" to profile.bio,
                "location" to profile.location,
                "avatarPresetIndex" to profile.avatarPresetIndex,
                "customAvatarUrl" to profile.customAvatarUrl,
                "itemsLentCount" to profile.itemsLentCount,
                "itemsRentedCount" to profile.itemsRentedCount,
                "rating" to profile.rating
            )
            fs.collection("users").document(profile.uid).set(data)
        } catch (_: Exception) {
            // Gracefully handled for offline/local-first
        }
    }
}
