package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.models.AppearanceMode
import com.example.data.models.BlockedUserEntry
import com.example.data.models.ChatConversation
import com.example.data.models.ChatMessageType
import com.example.data.models.ChatPrivacySettings
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.HouseListing
import com.example.data.models.LocationSharingPreference
import com.example.data.models.MessageStatus
import com.example.data.models.NotificationPreferences
import com.example.data.models.ProfileVisibility
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.data.models.RentalOwner
import com.example.data.models.SpecFeature
import com.example.data.models.UserCommunityPost
import com.example.data.models.UserProfile
import com.example.data.models.UserRentalBooking
import com.example.data.models.UserSettings
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreService(
    val firestore: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    val housesDataSource by lazy { HouseFirestoreDataSource(firestore) }

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun parseUserProfileSnapshot(
        snapshot: DocumentSnapshot,
        fallbackUid: String,
        allowAuthFallback: Boolean
    ): UserProfile {
        if (!snapshot.exists()) {
            return UserProfile(
                uid = fallbackUid,
                displayName = if (allowAuthFallback) auth.currentUser?.displayName ?: "" else "",
                email = if (allowAuthFallback) auth.currentUser?.email ?: "" else "",
                customAvatarUri = if (allowAuthFallback) {
                    auth.currentUser?.photoUrl?.toString()?.trim()?.takeIf { it.isNotBlank() }?.take(500)
                } else {
                    null
                }
            )
        }
        val rawDisplayName = snapshot.getString("displayName")
            ?: if (allowAuthFallback) auth.currentUser?.displayName ?: "" else ""
        val badgesList = (snapshot.get("badges") as? List<*>)?.mapNotNull { it?.toString() }
            ?: emptyList()
        val favoritesList = (snapshot.get("favoriteItemIds") as? List<*>)?.mapNotNull { it?.toString() }
            ?: emptyList()
        val favoriteHousesList = (snapshot.get("favoriteHouseIds") as? List<*>)?.mapNotNull { it?.toString() }
            ?: emptyList()

        @Suppress("UNCHECKED_CAST")
        val rawBlocked = snapshot.get("blockedUsers") as? List<Map<String, Any?>>
        val blockedList = rawBlocked?.mapNotNull { entry ->
            val bUid = (entry["userId"] as? String).orEmpty()
            if (bUid.isBlank()) null
            else BlockedUserEntry(
                userId = bUid,
                displayName = (entry["displayName"] as? String).orEmpty().ifBlank { "Community Member" },
                blockedAtLabel = (entry["blockedAtLabel"] as? String).orEmpty()
            )
        } ?: (snapshot.get("blockedUserIds") as? List<*>)
            ?.mapNotNull { it?.toString()?.takeIf { id -> id.isNotBlank() } }
            ?.map { id -> BlockedUserEntry(userId = id, displayName = "Blocked Member") }
            ?: emptyList()

        val parsedSettings = UserSettings(
            appearanceMode = AppearanceMode.fromNameOrDefault(snapshot.getString("appearanceMode")),
            profileVisibility = ProfileVisibility.fromNameOrDefault(snapshot.getString("profileVisibility")),
            locationSharing = LocationSharingPreference.fromNameOrDefault(snapshot.getString("locationSharing")),
            notifications = NotificationPreferences(
                pushEnabled = snapshot.getBoolean("notifyPush") ?: true,
                newNeighbourhoodPosts = snapshot.getBoolean("notifyNewPosts") ?: true,
                repliesToComments = snapshot.getBoolean("notifyCommentReplies") ?: true,
                likesAndInteractions = snapshot.getBoolean("notifyLikes") ?: true,
                announcements = snapshot.getBoolean("notifyAnnouncements") ?: true
            ),
            chatPrivacy = ChatPrivacySettings(
                sendReadReceipts = snapshot.getBoolean("sendReadReceipts") ?: true,
                showTypingStatus = snapshot.getBoolean("showTypingStatus") ?: true,
                defaultDisappearingSeconds = snapshot.getLong("defaultDisappearingSeconds") ?: 0L,
                hideNotificationPreviews = snapshot.getBoolean("hideNotificationPreviews") ?: false
            ),
            blockedUsers = blockedList
        )

        return UserProfile(
            uid = snapshot.id,
            displayName = rawDisplayName,
            email = snapshot.getString("email") ?: if (allowAuthFallback) auth.currentUser?.email ?: "" else "",
            phone = snapshot.getString("phone") ?: "",
            bio = snapshot.getString("bio") ?: "",
            location = snapshot.getString("location") ?: "",
            avatarPresetIndex = snapshot.getLong("avatarPresetIndex")?.toInt() ?: 0,
            customAvatarUri = snapshot.getString("customAvatarUri") ?: if (allowAuthFallback) {
                auth.currentUser?.photoUrl?.toString()?.trim()?.takeIf { it.isNotBlank() }?.take(500)
            } else {
                null
            },
            rating = (snapshot.getDouble("rating") ?: 0.0).toFloat(),
            reviewCount = snapshot.getLong("reviewCount")?.toInt() ?: 0,
            rentalsCompleted = snapshot.getLong("rentalsCompleted")?.toInt() ?: 0,
            listingsCount = snapshot.getLong("listingsCount")?.toInt() ?: 0,
            earnedAmount = snapshot.getLong("earnedAmount")?.toInt() ?: 0,
            isVerified = snapshot.getBoolean("isVerified") ?: false,
            memberSince = snapshot.getString("memberSince") ?: "",
            badges = badgesList,
            favoriteItemIds = favoritesList,
            favoriteHouseIds = favoriteHousesList,
            legacyResetVersion = snapshot.getLong("legacyResetVersion")?.toInt() ?: 0,
            fcmToken = snapshot.getString("fcmToken") ?: "",
            e2eePublicKey = snapshot.getString("e2eePublicKey") ?: "",
            e2eeKeyFingerprint = snapshot.getString("e2eeKeyFingerprint") ?: "",
            settings = parsedSettings
        )
    }

    /**
     * Real-time listener for User Profile in Firestore (/users/{uid})
     */
    fun listenToUserProfile(uid: String): Flow<UserProfile> {
        val docRef = firestore.collection("users").document(uid)
        val isOwnUser = uid == auth.currentUser?.uid
        return docRef.snapshots()
            .map { snapshot ->
                parseUserProfileSnapshot(
                    snapshot = snapshot,
                    fallbackUid = uid,
                    allowAuthFallback = isOwnUser
                )
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                throw error
            }
    }

    /**
     * One-shot fetch for any member's public profile document (/users/{uid})
     */
    suspend fun getUserProfileOnce(uid: String): Result<UserProfile?> {
        if (uid.isBlank()) return Result.success(null)
        val docRef = firestore.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                Result.success(null)
            } else {
                val isOwnUser = uid == auth.currentUser?.uid
                Result.success(
                    parseUserProfileSnapshot(
                        snapshot = snapshot,
                        fallbackUid = uid,
                        allowAuthFallback = isOwnUser
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Create or update user profile in Firestore (/users/{uid})
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            val isNew = !snapshot.exists()
            val resolvedEmail = profile.email.ifBlank {
                auth.currentUser?.email?.takeIf { it.isNotBlank() } ?: "${uid}@loop.local"
            }
            val resolvedDisplayName = profile.displayName.ifBlank {
                auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: resolvedEmail.substringBefore("@").ifBlank { "Loop Member" }
            }

            val data = mutableMapOf<String, Any?>(
                "userId" to uid,
                "displayName" to resolvedDisplayName,
                "email" to resolvedEmail,
                "phone" to profile.phone,
                "bio" to profile.bio,
                "location" to profile.location,
                "avatarPresetIndex" to profile.avatarPresetIndex,
                "customAvatarUri" to profile.customAvatarUri,
                "rating" to profile.rating.toDouble(),
                "reviewCount" to profile.reviewCount,
                "rentalsCompleted" to profile.rentalsCompleted,
                "listingsCount" to profile.listingsCount,
                "earnedAmount" to profile.earnedAmount,
                "isVerified" to profile.isVerified,
                "memberSince" to profile.memberSince,
                "badges" to profile.badges,
                "favoriteItemIds" to profile.favoriteItemIds,
                "favoriteHouseIds" to profile.favoriteHouseIds,
                "legacyResetVersion" to profile.legacyResetVersion,
                "fcmToken" to profile.fcmToken.takeIf { it.isNotBlank() },
                "e2eePublicKey" to profile.e2eePublicKey.takeIf { it.isNotBlank() },
                "e2eeKeyFingerprint" to profile.e2eeKeyFingerprint.takeIf { it.isNotBlank() },
                "appearanceMode" to profile.settings.appearanceMode.name,
                "profileVisibility" to profile.settings.profileVisibility.name,
                "locationSharing" to profile.settings.locationSharing.name,
                "notifyPush" to profile.settings.notifications.pushEnabled,
                "notifyNewPosts" to profile.settings.notifications.newNeighbourhoodPosts,
                "notifyCommentReplies" to profile.settings.notifications.repliesToComments,
                "notifyLikes" to profile.settings.notifications.likesAndInteractions,
                "notifyAnnouncements" to profile.settings.notifications.announcements,
                "sendReadReceipts" to profile.settings.chatPrivacy.sendReadReceipts,
                "showTypingStatus" to profile.settings.chatPrivacy.showTypingStatus,
                "defaultDisappearingSeconds" to profile.settings.chatPrivacy.defaultDisappearingSeconds,
                "hideNotificationPreviews" to profile.settings.chatPrivacy.hideNotificationPreviews,
                "blockedUserIds" to profile.settings.blockedUsers.map { it.userId }.take(200),
                "blockedUsers" to profile.settings.blockedUsers.take(200).map {
                    mapOf(
                        "userId" to it.userId,
                        "displayName" to it.displayName,
                        "blockedAtLabel" to it.blockedAtLabel
                    )
                },
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (isNew || !snapshot.contains("createdAt")) {
                data["createdAt"] = FieldValue.serverTimestamp()
            }

            @Suppress("UNCHECKED_CAST")
            val filteredData = data.filterValues { it != null } as Map<String, Any>
            docRef.set(filteredData, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(token: String): Result<Unit> {
        val cleanToken = token.trim().take(512)
        if (cleanToken.isBlank()) return Result.success(Unit)
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("users").document(uid)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                return Result.success(Unit)
            }
            docRef.update(
                mapOf(
                    "fcmToken" to cleanToken,
                    "fcmTokenUpdatedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateE2eePublicKey(publicKeyBase64: String, fingerprint: String): Result<Unit> {
        val cleanKey = publicKeyBase64.trim()
        val cleanFp = fingerprint.trim()
        if (cleanKey.isBlank()) return Result.success(Unit)
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("users").document(uid)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) return Result.success(Unit)
            docRef.update(
                mapOf(
                    "e2eePublicKey" to cleanKey,
                    "e2eeKeyFingerprint" to cleanFp,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Persist Settings (Appearance, Notifications, Chat E2EE Privacy, Privacy & Security, Blocked Users) on /users/{uid}.
     */
    suspend fun updateUserSettings(settings: UserSettings, fallbackProfile: UserProfile): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                return saveUserProfile(fallbackProfile.copy(uid = uid, settings = settings))
            }
            docRef.update(
                mapOf(
                    "appearanceMode" to settings.appearanceMode.name,
                    "profileVisibility" to settings.profileVisibility.name,
                    "locationSharing" to settings.locationSharing.name,
                    "notifyPush" to settings.notifications.pushEnabled,
                    "notifyNewPosts" to settings.notifications.newNeighbourhoodPosts,
                    "notifyCommentReplies" to settings.notifications.repliesToComments,
                    "notifyLikes" to settings.notifications.likesAndInteractions,
                    "notifyAnnouncements" to settings.notifications.announcements,
                    "sendReadReceipts" to settings.chatPrivacy.sendReadReceipts,
                    "showTypingStatus" to settings.chatPrivacy.showTypingStatus,
                    "defaultDisappearingSeconds" to settings.chatPrivacy.defaultDisappearingSeconds,
                    "hideNotificationPreviews" to settings.chatPrivacy.hideNotificationPreviews,
                    "blockedUserIds" to settings.blockedUsers.map { it.userId }.take(200),
                    "blockedUsers" to settings.blockedUsers.take(200).map {
                        mapOf(
                            "userId" to it.userId,
                            "displayName" to it.displayName,
                            "blockedAtLabel" to it.blockedAtLabel
                        )
                    },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Persist favorite item IDs on the user's profile document.
     */
    suspend fun updateFavoriteItems(favoriteItemIds: List<String>, fallbackProfile: UserProfile): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                return saveUserProfile(fallbackProfile.copy(uid = uid, favoriteItemIds = favoriteItemIds))
            }
            docRef.update(
                mapOf(
                    "favoriteItemIds" to favoriteItemIds.take(200),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Real-time listener for All Equipment Listings in Firestore (/items)
     */
    fun listenToItems(): Flow<List<RentalItem>> {
        val collectionRef = firestore.collection("items")
        return collectionRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val categoryStr = doc.getString("categoryName") ?: "ELECTRONICS"
                        val category = try {
                            RentalCategory.valueOf(categoryStr)
                        } catch (_: Exception) {
                            RentalCategory.ELECTRONICS
                        }

                        val imageRes = imageResForCategory(category)

                        @Suppress("UNCHECKED_CAST")
                        val rawFeatures = doc.get("features") as? List<Map<String, Any>>
                        val featuresList = rawFeatures?.mapNotNull { f ->
                            val label = (f["label"] as? String).orEmpty()
                            if (label.isBlank()) null
                            else SpecFeature(
                                iconType = f["iconType"] as? String ?: "CAMERA",
                                label = label
                            )
                        } ?: emptyList()

                        val ownerName = doc.getString("ownerName") ?: "Community Member"
                        val ownerInitials = doc.getString("ownerInitials")
                            ?: ownerName.trim().split(" ").filter { it.isNotBlank() }
                                .take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "U" }
                        val rawImageUrls = (doc.get("imageUrls") as? List<*>)
                            ?.mapNotNull { it?.toString()?.takeIf { url -> url.isNotBlank() } }
                            ?.take(4)
                            ?: emptyList()
                        val coverUrl = doc.getString("imageUrl")?.takeIf { it.isNotBlank() }
                            ?: rawImageUrls.firstOrNull()
                        val resolvedUrls = if (rawImageUrls.isNotEmpty()) rawImageUrls else listOfNotNull(coverUrl)

                        RentalItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            category = category,
                            pricePerDay = doc.getLong("pricePerDay")?.toInt() ?: 0,
                            location = doc.getString("location") ?: "",
                            rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                            reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
                            description = doc.getString("description") ?: "",
                            primaryImageRes = 0,
                            imageUrl = coverUrl,
                            imageUrls = resolvedUrls,
                            imageCount = doc.getLong("imageCount")?.toInt() ?: resolvedUrls.size,
                            features = featuresList,
                            owner = RentalOwner(
                                id = doc.getString("ownerId") ?: "",
                                name = ownerName,
                                badge = doc.getString("ownerBadge") ?: "Community Member",
                                initials = ownerInitials
                            ),
                            isPopular = doc.getBoolean("isPopular") ?: false,
                            isFavorite = doc.getBoolean("isFavorite") ?: false,
                            availableToday = doc.getBoolean("availableToday") ?: true,
                            activeBookingId = doc.getString("activeBookingId")?.takeIf { it.isNotBlank() },
                            securityDeposit = doc.getLong("securityDeposit")?.toInt() ?: 0,
                            latitude = doc.getDouble("latitude"),
                            longitude = doc.getDouble("longitude")
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, collectionRef.path)
                throw error
            }
    }

    /**
     * Create a new equipment listing in Firestore (/items/{itemId})
     */
    suspend fun createListing(item: RentalItem): Result<RentalItem> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("items").document(item.id)
        return try {
            val ownerName = item.owner.name.ifBlank { auth.currentUser?.displayName ?: "Community Member" }
            val initials = item.owner.initials.ifBlank {
                ownerName.trim().split(" ").filter { it.isNotBlank() }
                    .take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "U" }
            }
            val normalizedGallery = item.imageUrls
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .take(4)
                .ifEmpty { listOfNotNull(item.imageUrl?.takeIf { it.isNotBlank() }) }
            val coverUrl = normalizedGallery.firstOrNull() ?: item.imageUrl?.takeIf { it.isNotBlank() }

            val itemData = mutableMapOf<String, Any?>(
                "id" to item.id,
                "title" to item.title,
                "categoryName" to item.category.name,
                "pricePerDay" to item.pricePerDay,
                "securityDeposit" to item.securityDeposit,
                "location" to item.location,
                "rating" to item.rating.toDouble(),
                "reviewCount" to item.reviewCount,
                "description" to item.description,
                "imageUrl" to coverUrl,
                "imageUrls" to normalizedGallery,
                "imageCount" to normalizedGallery.size,
                "ownerId" to uid,
                "ownerName" to ownerName,
                "ownerInitials" to initials,
                "ownerBadge" to item.owner.badge,
                "isPopular" to item.isPopular,
                "isFavorite" to false,
                "availableToday" to item.availableToday,
                "activeBookingId" to item.activeBookingId,
                "latitude" to item.latitude,
                "longitude" to item.longitude,
                "features" to item.features.map {
                    mapOf("iconType" to it.iconType, "label" to it.label)
                },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            @Suppress("UNCHECKED_CAST")
            docRef.set(itemData.filterValues { it != null } as Map<String, Any>).await()
            Result.success(
                item.copy(
                    imageUrl = coverUrl,
                    imageUrls = normalizedGallery,
                    imageCount = normalizedGallery.size,
                    owner = item.owner.copy(id = uid, name = ownerName, initials = initials)
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateListing(item: RentalItem): Result<RentalItem> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("items").document(item.id)
        return try {
            val ownerName = item.owner.name.ifBlank { auth.currentUser?.displayName ?: "Community Member" }
            val initials = item.owner.initials.ifBlank {
                ownerName.trim().split(" ").filter { it.isNotBlank() }
                    .take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "U" }
            }
            val normalizedGallery = item.imageUrls
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .take(4)
                .ifEmpty { listOfNotNull(item.imageUrl?.takeIf { it.isNotBlank() }) }
            val coverUrl = normalizedGallery.firstOrNull() ?: item.imageUrl?.takeIf { it.isNotBlank() }

            val updates = mutableMapOf<String, Any?>(
                "title" to item.title,
                "categoryName" to item.category.name,
                "pricePerDay" to item.pricePerDay,
                "securityDeposit" to item.securityDeposit,
                "location" to item.location,
                "description" to item.description,
                "imageUrl" to coverUrl,
                "imageUrls" to normalizedGallery,
                "imageCount" to normalizedGallery.size,
                "ownerName" to ownerName,
                "ownerInitials" to initials,
                "availableToday" to item.availableToday,
                "activeBookingId" to item.activeBookingId,
                "latitude" to item.latitude,
                "longitude" to item.longitude,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(
                item.copy(
                    imageUrl = coverUrl,
                    imageUrls = normalizedGallery,
                    imageCount = normalizedGallery.size,
                    owner = item.owner.copy(id = uid, name = ownerName, initials = initials)
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateListingAvailability(itemId: String, availableToday: Boolean): Result<Unit> {
        val docRef = firestore.collection("items").document(itemId)
        return try {
            docRef.update(
                mapOf(
                    "availableToday" to availableToday,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun setItemBookingLock(
        itemId: String,
        availableToday: Boolean,
        activeBookingId: String?
    ): Result<Unit> {
        val docRef = firestore.collection("items").document(itemId)
        return try {
            docRef.update(
                mapOf<String, Any?>(
                    "availableToday" to availableToday,
                    "activeBookingId" to activeBookingId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun applyItemRatingReview(itemId: String, ratingToAdd: Int): Result<Pair<Float, Int>> {
        val docRef = firestore.collection("items").document(itemId)
        return try {
            val clamped = ratingToAdd.coerceIn(1, 5)
            val snap = docRef.get().await()
            val oldCount = snap.getLong("reviewCount")?.toInt() ?: 0
            val oldRating = snap.getDouble("rating") ?: 0.0
            val newCount = oldCount + 1
            val newAvg = ((oldRating * oldCount) + clamped) / newCount.toDouble()
            val roundedAvg = kotlin.math.round(newAvg * 10.0) / 10.0

            docRef.update(
                mapOf(
                    "rating" to roundedAvg,
                    "reviewCount" to newCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(roundedAvg.toFloat() to newCount)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteListing(itemId: String): Result<Unit> {
        val docRef = firestore.collection("items").document(itemId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Real-time listener for Community Posts in Firestore (/community_posts)
     */
    fun listenToCommunityPosts(): Flow<List<UserCommunityPost>> {
        val collectionRef = firestore.collection("community_posts")
        return collectionRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val typeStr = doc.getString("typeStr") ?: "EVENT"
                        val type = try {
                            CommunityPostType.valueOf(typeStr)
                        } catch (_: Exception) {
                            CommunityPostType.EVENT
                        }

                        val statusStr = doc.getString("statusStr") ?: "ACTIVE"
                        val status = try {
                            CommunityPostStatus.valueOf(statusStr)
                        } catch (_: Exception) {
                            CommunityPostStatus.ACTIVE
                        }

                        val attendeeIdsList = (doc.get("attendeeIds") as? List<*>)
                            ?.mapNotNull { it?.toString()?.takeIf { id -> id.isNotBlank() } }
                            ?: emptyList()

                        UserCommunityPost(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            type = type,
                            categoryName = doc.getString("categoryName") ?: "CREATIVE_PHOTO",
                            description = doc.getString("description") ?: "",
                            location = doc.getString("location") ?: "",
                            dateTime = doc.getString("dateTime") ?: "",
                            attendeesOrResponses = doc.getString("attendeesOrResponses") ?: "0 participating",
                            attendeeIds = attendeeIdsList,
                            status = status,
                            createdAt = "",
                            createdAtEpoch = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                            latitude = doc.getDouble("latitude"),
                            longitude = doc.getDouble("longitude"),
                            authorId = doc.getString("authorId") ?: "",
                            authorName = doc.getString("authorName") ?: "Community Member",
                            imageUrl = doc.getString("imageUrl")?.takeIf { it.isNotBlank() }
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, collectionRef.path)
                throw error
            }
    }

    /**
     * Create community event post in Firestore (/community_posts/{postId})
     */
    suspend fun createCommunityPost(post: UserCommunityPost): Result<UserCommunityPost> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("community_posts").document(post.id)
        return try {
            val authorName = post.authorName.ifBlank { auth.currentUser?.displayName ?: "Community Member" }
            val data = mutableMapOf<String, Any?>(
                "id" to post.id,
                "title" to post.title,
                "typeStr" to post.type.name,
                "categoryName" to post.categoryName,
                "description" to post.description,
                "location" to post.location,
                "dateTime" to post.dateTime,
                "attendeesOrResponses" to post.attendeesOrResponses,
                "attendeeIds" to post.attendeeIds,
                "statusStr" to post.status.name,
                "authorId" to uid,
                "authorName" to authorName,
                "latitude" to post.latitude,
                "longitude" to post.longitude,
                "imageUrl" to post.imageUrl?.takeIf { it.isNotBlank() },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            @Suppress("UNCHECKED_CAST")
            docRef.set(data.filterValues { it != null } as Map<String, Any>).await()
            Result.success(post.copy(authorId = uid, authorName = authorName))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCommunityPost(post: UserCommunityPost): Result<UserCommunityPost> {
        val docRef = firestore.collection("community_posts").document(post.id)
        return try {
            val updates = mutableMapOf<String, Any?>(
                "title" to post.title,
                "typeStr" to post.type.name,
                "categoryName" to post.categoryName,
                "description" to post.description,
                "location" to post.location,
                "dateTime" to post.dateTime,
                "statusStr" to post.status.name,
                "latitude" to post.latitude,
                "longitude" to post.longitude,
                "imageUrl" to post.imageUrl?.takeIf { it.isNotBlank() },
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(post)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCommunityPostStatus(id: String, status: CommunityPostStatus): Result<Unit> {
        val docRef = firestore.collection("community_posts").document(id)
        return try {
            docRef.update(
                mapOf(
                    "statusStr" to status.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCommunityPostAttendees(id: String, attendeesText: String): Result<Unit> {
        val docRef = firestore.collection("community_posts").document(id)
        return try {
            docRef.update(
                mapOf(
                    "attendeesOrResponses" to attendeesText,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCommunityPostRsvp(
        id: String,
        attendeesText: String,
        attendeeIds: List<String>
    ): Result<Unit> {
        val docRef = firestore.collection("community_posts").document(id)
        return try {
            docRef.update(
                mapOf(
                    "attendeesOrResponses" to attendeesText,
                    "attendeeIds" to attendeeIds.distinct().take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteCommunityPost(id: String): Result<Unit> {
        val docRef = firestore.collection("community_posts").document(id)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Participant-scoped real-time listener for Conversations (/conversations)
     */
    fun listenToConversations(uid: String, knownItems: List<RentalItem> = emptyList()): Flow<List<ChatConversation>> {
        val collectionRef = firestore.collection("conversations")
        val query = collectionRef.whereArrayContains("participantIds", uid)
        return query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val hostId = doc.getString("hostId") ?: ""
                        val hostName = doc.getString("hostName") ?: "Host"
                        val renterId = doc.getString("renterId") ?: ""
                        val renterName = doc.getString("renterName") ?: "Member"
                        val isHost = uid == hostId && renterId.isNotBlank() && hostId != renterId
                        val displayOtherName = if (isHost && renterName.isNotBlank()) {
                            renterName
                        } else {
                            hostName
                        }
                        val otherInitials = if (isHost) {
                            computeInitials(displayOtherName)
                        } else {
                            doc.getString("hostInitials")?.takeIf { it.isNotBlank() }
                                ?: computeInitials(displayOtherName)
                        }
                        val counterparty = RentalOwner(
                            id = if (isHost) renterId else hostId,
                            name = displayOtherName,
                            badge = if (isHost) "Community Member" else (doc.getString("hostBadge") ?: "Community Member"),
                            initials = otherInitials,
                            phone = if (isHost) "" else (doc.getString("hostPhone") ?: "")
                        )

                        val lastSenderId = doc.getString("lastSenderId").orEmpty()
                        val lastMessageText = doc.getString("lastMessageText").orEmpty()
                        val lastMessages = if (lastMessageText.isNotBlank()) {
                            listOf(
                                RentalMessage(
                                    id = "last_${doc.id}",
                                    senderName = if (lastSenderId == uid) "You" else displayOtherName,
                                    text = lastMessageText,
                                    timestamp = doc.getString("lastMessageTimestamp") ?: "Just now",
                                    isFromMe = lastSenderId == uid,
                                    senderId = lastSenderId,
                                    status = MessageStatus.READ
                                )
                            )
                        } else {
                            emptyList()
                        }

                        val itemId = doc.getString("itemId")?.takeIf { it.isNotBlank() }
                        val itemContext = itemId?.let { id ->
                            knownItems.find { it.id == id } ?: doc.getString("itemTitle")?.let { title ->
                                RentalItem(
                                    id = id,
                                    title = title,
                                    category = RentalCategory.ELECTRONICS,
                                    pricePerDay = doc.getLong("itemPricePerDay")?.toInt() ?: 0,
                                    location = "",
                                    rating = 0f,
                                    reviewCount = 0,
                                    description = "",
                                    primaryImageRes = 0,
                                    features = emptyList(),
                                    owner = counterparty
                                )
                            }
                        }

                        val storedUnread = doc.getLong("unreadCount")?.toInt() ?: 0
                        val effectiveUnread = if (lastSenderId.isNotBlank() && lastSenderId == uid) 0 else storedUnread
                        val mutedBy = (doc.get("mutedByUserIds") as? List<*>)
                            ?.mapNotNull { it?.toString()?.takeIf { id -> id.isNotBlank() } }
                            ?: emptyList()
                        val disappearingSecs = doc.getLong("disappearingTimerSeconds") ?: 0L
                        val e2eeEnabled = doc.getBoolean("e2eeEnabled") ?: true

                        val houseId = doc.getString("houseId")?.takeIf { it.isNotBlank() }
                        val houseTitle = doc.getString("houseTitle")?.takeIf { it.isNotBlank() }
                        val houseMonthlyRent = doc.getLong("houseMonthlyRent")?.toInt()

                        ChatConversation(
                            id = doc.id,
                            owner = counterparty,
                            itemContext = itemContext,
                            houseId = houseId,
                            houseTitle = houseTitle,
                            houseMonthlyRent = houseMonthlyRent,
                            communityPostId = doc.getString("communityPostId")?.takeIf { it.isNotBlank() },
                            communityPostTitle = doc.getString("communityPostTitle")?.takeIf { it.isNotBlank() },
                            lastSenderId = lastSenderId,
                            isOnline = doc.getBoolean("isOnline") ?: true,
                            isTyping = doc.getBoolean("isTyping") ?: false,
                            unreadCount = effectiveUnread,
                            isPinned = doc.getBoolean("isPinned") ?: false,
                            mutedByUserIds = mutedBy,
                            disappearingTimerSeconds = disappearingSecs,
                            e2eeEnabled = e2eeEnabled,
                            messages = lastMessages
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, collectionRef.path)
                throw error
            }
    }

    /**
     * Create or reuse a participant-scoped conversation in Firestore.
     */
    suspend fun createOrGetConversation(
        host: RentalOwner,
        item: RentalItem? = null,
        houseListing: HouseListing? = null,
        communityPost: UserCommunityPost? = null,
        customConversationId: String? = null,
        defaultDisappearingSeconds: Long = 0L,
        e2eeEnabled: Boolean = true
    ): Result<ChatConversation> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val cleanHostId = host.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        if (cleanHostId.isBlank()) {
            return Result.failure(IllegalArgumentException("Recipient user ID is missing."))
        }
        if (cleanHostId == uid) {
            return Result.failure(IllegalStateException("You cannot start a conversation with yourself."))
        }
        val contextSuffix = when {
            item != null -> item.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            houseListing != null -> houseListing.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            communityPost != null -> communityPost.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            else -> "general"
        }
        val convId = customConversationId
            ?: "conv_${uid.take(24)}_${cleanHostId.take(24)}_${contextSuffix.take(24)}"
        val docRef = firestore.collection("conversations").document(convId)

        return try {
            val existingSnap = docRef.get().await()
            val participantIds = listOf(uid, cleanHostId).distinct()
            val renterName = auth.currentUser?.displayName ?: "Loop Member"

            if (!existingSnap.exists()) {
                if (customConversationId == null) {
                    val userConvs = firestore.collection("conversations")
                        .whereArrayContains("participantIds", uid)
                        .get()
                        .await()
                    val matchingExisting = userConvs.documents.firstOrNull { d ->
                        val participants = (d.get("participantIds") as? List<*>)
                            ?.mapNotNull { it?.toString() }
                            ?: emptyList()
                        val sameParticipants = cleanHostId in participants && uid in participants
                        val sameItem = item?.id != null && d.getString("itemId") == item.id
                        val sameHouse = houseListing?.id != null && d.getString("houseId") == houseListing.id
                        val samePost = communityPost?.id != null && d.getString("communityPostId") == communityPost.id
                        val sameGeneral = item == null && houseListing == null && communityPost == null &&
                            d.getString("itemId").isNullOrBlank() &&
                            d.getString("houseId").isNullOrBlank() &&
                            d.getString("communityPostId").isNullOrBlank()
                        sameParticipants && (sameItem || sameHouse || samePost || sameGeneral)
                    }
                    if (matchingExisting != null) {
                        val existingHostId = matchingExisting.getString("hostId") ?: cleanHostId
                        val existingHostName = matchingExisting.getString("hostName") ?: host.name
                        val existingRenterId = matchingExisting.getString("renterId") ?: uid
                        val existingRenterName = matchingExisting.getString("renterName") ?: renterName
                        val isHostInDoc = uid == existingHostId
                        val otherName = if (isHostInDoc) existingRenterName else existingHostName
                        val otherOwner = RentalOwner(
                            id = if (isHostInDoc) existingRenterId else existingHostId,
                            name = otherName,
                            badge = if (isHostInDoc) "Community Member" else (matchingExisting.getString("hostBadge") ?: host.badge),
                            initials = computeInitials(otherName),
                            phone = if (isHostInDoc) "" else (matchingExisting.getString("hostPhone") ?: host.phone)
                        )
                        val mutedBy = (matchingExisting.get("mutedByUserIds") as? List<*>)
                            ?.mapNotNull { it?.toString()?.takeIf { id -> id.isNotBlank() } }
                            ?: emptyList()
                        return Result.success(
                            ChatConversation(
                                id = matchingExisting.id,
                                owner = otherOwner,
                                itemContext = item,
                                houseId = houseListing?.id ?: matchingExisting.getString("houseId"),
                                houseTitle = houseListing?.title ?: matchingExisting.getString("houseTitle"),
                                houseMonthlyRent = houseListing?.monthlyRent ?: matchingExisting.getLong("houseMonthlyRent")?.toInt(),
                                communityPostId = communityPost?.id ?: matchingExisting.getString("communityPostId"),
                                communityPostTitle = communityPost?.title ?: matchingExisting.getString("communityPostTitle"),
                                lastSenderId = matchingExisting.getString("lastSenderId").orEmpty(),
                                isOnline = matchingExisting.getBoolean("isOnline") ?: true,
                                isTyping = matchingExisting.getBoolean("isTyping") ?: false,
                                mutedByUserIds = mutedBy,
                                disappearingTimerSeconds = matchingExisting.getLong("disappearingTimerSeconds") ?: 0L,
                                e2eeEnabled = matchingExisting.getBoolean("e2eeEnabled") ?: true,
                                messages = emptyList()
                            )
                        )
                    }
                }

                val initialText = when {
                    item != null -> "Inquiry about ${item.title}"
                    houseListing != null -> "House inquiry: ${houseListing.title}"
                    communityPost != null -> "Message about ${communityPost.title}"
                    else -> "Conversation started"
                }
                val safeTimer = defaultDisappearingSeconds.coerceIn(0L, 2_592_000L)
                val convData = mutableMapOf<String, Any?>(
                    "id" to convId,
                    "participantIds" to participantIds,
                    "hostId" to cleanHostId,
                    "hostName" to host.name,
                    "hostInitials" to host.initials.ifBlank { computeInitials(host.name) },
                    "hostBadge" to host.badge,
                    "hostPhone" to host.phone,
                    "renterId" to uid,
                    "renterName" to renterName,
                    "itemId" to item?.id,
                    "itemTitle" to item?.title,
                    "itemPricePerDay" to item?.pricePerDay,
                    "houseId" to houseListing?.id,
                    "houseTitle" to houseListing?.title,
                    "houseMonthlyRent" to houseListing?.monthlyRent,
                    "communityPostId" to communityPost?.id,
                    "communityPostTitle" to communityPost?.title,
                    "lastSenderId" to "",
                    "isOnline" to true,
                    "isTyping" to false,
                    "unreadCount" to 0,
                    "isPinned" to false,
                    "mutedByUserIds" to emptyList<String>(),
                    "disappearingTimerSeconds" to safeTimer,
                    "e2eeEnabled" to e2eeEnabled,
                    "lastMessageText" to initialText,
                    "lastMessageTimestamp" to "Just now",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                @Suppress("UNCHECKED_CAST")
                docRef.set(convData.filterValues { it != null } as Map<String, Any>).await()

                return Result.success(
                    ChatConversation(
                        id = convId,
                        owner = host.copy(id = cleanHostId),
                        itemContext = item,
                        houseId = houseListing?.id,
                        houseTitle = houseListing?.title,
                        houseMonthlyRent = houseListing?.monthlyRent,
                        communityPostId = communityPost?.id,
                        communityPostTitle = communityPost?.title,
                        isOnline = true,
                        disappearingTimerSeconds = safeTimer,
                        e2eeEnabled = e2eeEnabled,
                        messages = emptyList()
                    )
                )
            }

            val mutedBy = (existingSnap.get("mutedByUserIds") as? List<*>)
                ?.mapNotNull { it?.toString()?.takeIf { id -> id.isNotBlank() } }
                ?: emptyList()
            Result.success(
                ChatConversation(
                    id = convId,
                    owner = host.copy(id = cleanHostId),
                    itemContext = item,
                    houseId = houseListing?.id ?: existingSnap.getString("houseId"),
                    houseTitle = houseListing?.title ?: existingSnap.getString("houseTitle"),
                    houseMonthlyRent = houseListing?.monthlyRent ?: existingSnap.getLong("houseMonthlyRent")?.toInt(),
                    communityPostId = communityPost?.id,
                    communityPostTitle = communityPost?.title,
                    lastSenderId = existingSnap.getString("lastSenderId").orEmpty(),
                    isOnline = existingSnap.getBoolean("isOnline") ?: true,
                    isTyping = existingSnap.getBoolean("isTyping") ?: false,
                    mutedByUserIds = mutedBy,
                    disappearingTimerSeconds = existingSnap.getLong("disappearingTimerSeconds") ?: 0L,
                    e2eeEnabled = existingSnap.getBoolean("e2eeEnabled") ?: true,
                    messages = emptyList()
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Real-time listener for immutable messages in a conversation
     */
    fun listenToMessages(conversationId: String): Flow<List<RentalMessage>> {
        val messagesRef = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")

        return messagesRef.orderBy("createdAt", Query.Direction.ASCENDING).snapshots()
            .map { snapshot ->
                val currentUid = auth.currentUser?.uid
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val msgTypeStr = doc.getString("messageTypeStr") ?: "TEXT"
                        val msgType = try {
                            ChatMessageType.valueOf(msgTypeStr)
                        } catch (_: Exception) {
                            ChatMessageType.TEXT
                        }

                        val statusStr = doc.getString("statusStr") ?: "READ"
                        val status = try {
                            MessageStatus.valueOf(statusStr)
                        } catch (_: Exception) {
                            MessageStatus.READ
                        }

                        val senderId = doc.getString("senderId") ?: ""
                        val isFromMe = if (currentUid != null) senderId == currentUid else false
                        val epoch = doc.getLong("timestampEpoch")
                            ?: doc.getTimestamp("createdAt")?.toDate()?.time
                            ?: 0L

                        RentalMessage(
                            id = doc.id,
                            senderName = doc.getString("senderName") ?: "Member",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getString("timestampStr") ?: "Just now",
                            isFromMe = isFromMe,
                            senderId = senderId,
                            messageType = msgType,
                            status = status,
                            audioDuration = doc.getString("audioDuration"),
                            imageUrl = doc.getString("imageUrl"),
                            locationTitle = doc.getString("locationTitle"),
                            locationAddress = doc.getString("locationAddress"),
                            latitude = doc.getDouble("latitude"),
                            longitude = doc.getDouble("longitude"),
                            itemTitle = doc.getString("itemTitle"),
                            itemPricePerDay = doc.getLong("itemPricePerDay")?.toInt(),
                            isEncrypted = doc.getBoolean("isEncrypted") ?: false,
                            keyVersion = doc.getLong("keyVersion")?.toInt() ?: 1,
                            ciphertext = doc.getString("ciphertext"),
                            iv = doc.getString("iv"),
                            senderPublicKey = doc.getString("senderPublicKey"),
                            recipientPublicKey = doc.getString("recipientPublicKey"),
                            replyToMessageId = doc.getString("replyToMessageId"),
                            replyToSenderName = doc.getString("replyToSenderName"),
                            timestampEpoch = epoch,
                            expiresAtEpoch = doc.getLong("expiresAtEpoch") ?: 0L
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, messagesRef.path)
                throw error
            }
    }

    /**
     * Send an immutable message (encrypted envelope or legacy plaintext) into an existing conversation.
     */
    suspend fun sendMessage(conversationId: String, message: RentalMessage): Result<RentalMessage> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val convRef = firestore.collection("conversations").document(conversationId)
        val msgRef = convRef.collection("messages").document(message.id)

        return try {
            val senderName = message.senderName.ifBlank { auth.currentUser?.displayName ?: "Me" }
            val nowEpoch = if (message.timestampEpoch > 0L) message.timestampEpoch else System.currentTimeMillis()
            val storedText = if (message.isEncrypted) "Encrypted message" else message.text
            @Suppress("UNCHECKED_CAST")
            val msgData = mapOf<String, Any?>(
                "id" to message.id,
                "senderId" to uid,
                "senderName" to senderName,
                "text" to storedText,
                "timestampStr" to message.timestamp,
                "timestampEpoch" to nowEpoch,
                "isFromMe" to true,
                "messageTypeStr" to message.messageType.name,
                "statusStr" to message.status.name,
                "audioDuration" to message.audioDuration,
                "imageUrl" to if (message.isEncrypted) null else message.imageUrl,
                "locationTitle" to if (message.isEncrypted) null else message.locationTitle,
                "locationAddress" to if (message.isEncrypted) null else message.locationAddress,
                "latitude" to if (message.isEncrypted) null else message.latitude,
                "longitude" to if (message.isEncrypted) null else message.longitude,
                "itemTitle" to message.itemTitle,
                "itemPricePerDay" to message.itemPricePerDay,
                "isEncrypted" to message.isEncrypted,
                "keyVersion" to message.keyVersion,
                "ciphertext" to message.ciphertext,
                "iv" to message.iv,
                "senderPublicKey" to message.senderPublicKey,
                "recipientPublicKey" to message.recipientPublicKey,
                "replyToMessageId" to message.replyToMessageId,
                "replyToSenderName" to message.replyToSenderName,
                "expiresAtEpoch" to message.expiresAtEpoch.coerceAtLeast(0L),
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            msgRef.set(msgData).await()

            convRef.update(
                mapOf(
                    "lastMessageText" to storedText,
                    "lastMessageTimestamp" to message.timestamp,
                    "lastSenderId" to uid,
                    "unreadCount" to FieldValue.increment(1),
                    "isTyping" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(
                message.copy(
                    senderId = uid,
                    senderName = senderName,
                    isFromMe = true,
                    timestampEpoch = nowEpoch
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(
        conversationId: String,
        messageId: String,
        fallbackLastMessageText: String? = null,
        fallbackLastSenderId: String? = null
    ): Result<Unit> {
        try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val convRef = firestore.collection("conversations").document(conversationId)
        val msgRef = convRef.collection("messages").document(messageId)
        return try {
            msgRef.delete().await()
            if (fallbackLastMessageText != null) {
                val updates = mutableMapOf<String, Any>(
                    "lastMessageText" to fallbackLastMessageText,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (fallbackLastSenderId != null) {
                    updates["lastSenderId"] = fallbackLastSenderId
                }
                convRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateConversationMute(
        conversationId: String,
        uid: String,
        muted: Boolean
    ): Result<Unit> {
        try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val convRef = firestore.collection("conversations").document(conversationId)
        return try {
            val op = if (muted) FieldValue.arrayUnion(uid) else FieldValue.arrayRemove(uid)
            convRef.update(
                mapOf(
                    "mutedByUserIds" to op,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateConversationDisappearingTimer(
        conversationId: String,
        seconds: Long
    ): Result<Unit> {
        try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val convRef = firestore.collection("conversations").document(conversationId)
        return try {
            convRef.update(
                mapOf(
                    "disappearingTimerSeconds" to seconds.coerceIn(0L, 2_592_000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateConversationTyping(
        conversationId: String,
        isTyping: Boolean
    ): Result<Unit> {
        try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val convRef = firestore.collection("conversations").document(conversationId)
        return try {
            convRef.update(
                mapOf(
                    "isTyping" to isTyping,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path)
            Result.failure(e)
        }
    }

    suspend fun markConversationRead(conversationId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val convRef = firestore.collection("conversations").document(conversationId)
        return try {
            val snap = convRef.get().await()
            if (snap.exists()) {
                val lastSenderId = snap.getString("lastSenderId").orEmpty()
                val unread = snap.getLong("unreadCount")?.toInt() ?: 0
                if (unread > 0 && lastSenderId.isNotBlank() && lastSenderId != uid) {
                    convRef.update(
                        mapOf(
                            "unreadCount" to 0,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path)
            Result.failure(e)
        }
    }

    /**
     * Real-time listener for Bookings where current user is renter OR host.
     */
    fun listenToBookings(uid: String, knownItems: List<RentalItem> = emptyList()): Flow<List<UserRentalBooking>> {
        val collectionRef = firestore.collection("bookings")
        val asRenterFlow = collectionRef.whereEqualTo("renterId", uid).snapshots()
        val asHostFlow = collectionRef.whereEqualTo("hostId", uid).snapshots()

        return combine(asRenterFlow, asHostFlow) { renterSnap, hostSnap ->
            val mergedDocs = linkedMapOf<String, com.google.firebase.firestore.DocumentSnapshot>()
            renterSnap.documents.forEach { mergedDocs[it.id] = it }
            hostSnap.documents.forEach { mergedDocs[it.id] = it }

            mergedDocs.values.mapNotNull { doc ->
                try {
                    val itemId = doc.getString("itemId") ?: ""
                    val itemTitle = doc.getString("itemTitle") ?: "Gear Rental"
                    val pricePerDay = doc.getLong("pricePerDay")?.toInt() ?: 0
                    val hostId = doc.getString("hostId") ?: ""
                    val hostName = doc.getString("hostName") ?: "Host"
                    val status = doc.getString("status") ?: "Confirmed"

                    val resolvedItem = knownItems.find { it.id == itemId } ?: RentalItem(
                        id = itemId,
                        title = itemTitle,
                        category = RentalCategory.ELECTRONICS,
                        pricePerDay = pricePerDay,
                        location = "",
                        rating = 0f,
                        reviewCount = 0,
                        description = "",
                        primaryImageRes = 0,
                        features = emptyList(),
                        owner = RentalOwner(id = hostId, name = hostName)
                    )

                    UserRentalBooking(
                        id = doc.id,
                        item = resolvedItem,
                        hostId = hostId,
                        hostName = hostName,
                        renterId = doc.getString("renterId") ?: "",
                        renterName = doc.getString("renterName") ?: "",
                        startDate = doc.getString("startDate") ?: "",
                        endDate = doc.getString("endDate") ?: "",
                        daysRemaining = doc.getLong("daysCount")?.toInt() ?: 1,
                        totalPaid = doc.getLong("totalAmount")?.toInt() ?: 0,
                        status = status,
                        handoverOtp = doc.getString("handoverOtp") ?: "",
                        deliveryOption = doc.getString("deliveryOption") ?: "Self Pickup",
                        isCompleted = status.equals("Completed", ignoreCase = true) ||
                            status.equals("Cancelled", ignoreCase = true),
                        isReviewed = doc.getBoolean("isReviewed") ?: false,
                        ratingGiven = doc.getLong("ratingGiven")?.toInt() ?: 0,
                        reviewComment = doc.getString("reviewComment") ?: ""
                    )
                } catch (_: Exception) {
                    null
                }
            }
        }.catch { error ->
            if (error is Exception) handleFirestoreError(error, OperationType.LIST, collectionRef.path)
            throw error
        }
    }

    /**
     * Atomically verify item availability and create reservation + lock item in a single transaction.
     */
    suspend fun createBookingTransaction(booking: FirestoreBooking): Result<FirestoreBooking> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        if (booking.hostId.isBlank() || booking.hostId == uid) {
            return Result.failure(IllegalStateException("You cannot book your own listing."))
        }
        val bookingRef = firestore.collection("bookings").document(booking.id)
        val itemRef = firestore.collection("items").document(booking.itemId)
        return try {
            val renterName = booking.renterName.ifBlank { auth.currentUser?.displayName ?: "Renter" }
            val saved = firestore.runTransaction { tx ->
                val itemSnap = tx.get(itemRef)
                if (!itemSnap.exists()) {
                    throw IllegalStateException("This listing is no longer available.")
                }
                val ownerId = itemSnap.getString("ownerId").orEmpty()
                if (ownerId.isBlank() || ownerId == uid) {
                    throw IllegalStateException("You cannot book your own listing.")
                }
                val isAvailable = itemSnap.getBoolean("availableToday") ?: true
                val activeId = itemSnap.getString("activeBookingId")?.takeIf { it.isNotBlank() }
                if (!isAvailable || activeId != null) {
                    throw IllegalStateException("This piece was just booked or paused by the host.")
                }

                val liveTitle = itemSnap.getString("title")?.takeIf { it.isNotBlank() } ?: booking.itemTitle
                val livePrice = itemSnap.getLong("pricePerDay")?.toInt() ?: booking.pricePerDay
                val liveHostName = itemSnap.getString("ownerName")?.takeIf { it.isNotBlank() } ?: booking.hostName

                val bookingData = mapOf<String, Any>(
                    "id" to booking.id,
                    "renterId" to uid,
                    "renterName" to renterName,
                    "hostId" to ownerId,
                    "hostName" to liveHostName,
                    "itemId" to booking.itemId,
                    "itemTitle" to liveTitle,
                    "startDate" to booking.startDate,
                    "endDate" to booking.endDate,
                    "daysCount" to booking.daysCount,
                    "pricePerDay" to livePrice,
                    "totalAmount" to booking.totalAmount,
                    "status" to "Confirmed",
                    "deliveryOption" to booking.deliveryOption,
                    "handoverOtp" to booking.handoverOtp,
                    "isReviewed" to false,
                    "ratingGiven" to 0,
                    "reviewComment" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                tx.set(bookingRef, bookingData)
                tx.update(
                    itemRef,
                    mapOf<String, Any?>(
                        "availableToday" to false,
                        "activeBookingId" to booking.id,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                booking.copy(
                    renterId = uid,
                    renterName = renterName,
                    hostId = ownerId,
                    hostName = liveHostName,
                    itemTitle = liveTitle,
                    pricePerDay = livePrice,
                    status = "Confirmed"
                )
            }.await()
            Result.success(saved)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e !is IllegalStateException && e !is IllegalArgumentException) {
                handleFirestoreError(e, OperationType.WRITE, "${bookingRef.path}|${itemRef.path}")
            }
            Result.failure(e)
        }
    }

    /**
     * Atomically validate handover OTP on the server document and transition booking Confirmed -> Active.
     */
    suspend fun verifyHandoverPinTransaction(bookingId: String, enteredPin: String): Result<FirestoreBooking> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val bookingRef = firestore.collection("bookings").document(bookingId)
        return try {
            val updated = firestore.runTransaction { tx ->
                val snap = tx.get(bookingRef)
                if (!snap.exists()) {
                    throw IllegalArgumentException("Reservation not found.")
                }
                val hostId = snap.getString("hostId").orEmpty()
                if (hostId != uid) {
                    throw IllegalStateException("Only the host can verify handover PIN.")
                }
                val status = snap.getString("status").orEmpty()
                if (!status.equals("Confirmed", ignoreCase = true)) {
                    throw IllegalStateException("Reservation must be Confirmed before pickup.")
                }
                val expectedOtp = snap.getString("handoverOtp").orEmpty().trim()
                if (enteredPin.trim() != expectedOtp) {
                    throw IllegalArgumentException("Incorrect 4-digit handover PIN.")
                }
                tx.update(
                    bookingRef,
                    mapOf(
                        "status" to "Active",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                parseFirestoreBookingSnapshot(snap).copy(status = "Active")
            }.await()
            Result.success(updated)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e !is IllegalStateException && e !is IllegalArgumentException) {
                handleFirestoreError(e, OperationType.UPDATE, bookingRef.path)
            }
            Result.failure(e)
        }
    }

    /**
     * Atomically transition booking -> Completed and unlock the associated item if locked by this booking.
     */
    suspend fun completeBookingTransaction(bookingId: String): Result<FirestoreBooking> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val bookingRef = firestore.collection("bookings").document(bookingId)
        return try {
            val updated = firestore.runTransaction { tx ->
                val bookingSnap = tx.get(bookingRef)
                if (!bookingSnap.exists()) {
                    throw IllegalArgumentException("Reservation not found.")
                }
                val hostId = bookingSnap.getString("hostId").orEmpty()
                if (hostId != uid) {
                    throw IllegalStateException("Only the host can mark gear returned.")
                }
                val status = bookingSnap.getString("status").orEmpty()
                if (status.equals("Cancelled", ignoreCase = true)) {
                    throw IllegalStateException("Cancelled bookings cannot be completed.")
                }
                if (status.equals("Completed", ignoreCase = true)) {
                    return@runTransaction parseFirestoreBookingSnapshot(bookingSnap)
                }
                val itemId = bookingSnap.getString("itemId").orEmpty()
                val itemSnap = if (itemId.isNotBlank()) tx.get(firestore.collection("items").document(itemId)) else null

                tx.update(
                    bookingRef,
                    mapOf(
                        "status" to "Completed",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                if (itemSnap != null && itemSnap.exists() && itemSnap.getString("activeBookingId") == bookingId) {
                    tx.update(
                        itemSnap.reference,
                        mapOf<String, Any?>(
                            "availableToday" to true,
                            "activeBookingId" to null,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                parseFirestoreBookingSnapshot(bookingSnap).copy(status = "Completed")
            }.await()
            Result.success(updated)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e !is IllegalStateException && e !is IllegalArgumentException) {
                handleFirestoreError(e, OperationType.UPDATE, bookingRef.path)
            }
            Result.failure(e)
        }
    }

    /**
     * Atomically cancel a Confirmed booking and unlock the item if locked by this booking.
     */
    suspend fun cancelBookingTransaction(bookingId: String): Result<FirestoreBooking> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val bookingRef = firestore.collection("bookings").document(bookingId)
        return try {
            val updated = firestore.runTransaction { tx ->
                val bookingSnap = tx.get(bookingRef)
                if (!bookingSnap.exists()) {
                    throw IllegalArgumentException("Reservation not found.")
                }
                val hostId = bookingSnap.getString("hostId").orEmpty()
                val renterId = bookingSnap.getString("renterId").orEmpty()
                if (uid != hostId && uid != renterId) {
                    throw IllegalStateException("Only reservation participants can cancel.")
                }
                val status = bookingSnap.getString("status").orEmpty()
                if (!status.equals("Confirmed", ignoreCase = true)) {
                    throw IllegalStateException("Only Confirmed bookings can be cancelled.")
                }
                val itemId = bookingSnap.getString("itemId").orEmpty()
                val itemSnap = if (itemId.isNotBlank()) tx.get(firestore.collection("items").document(itemId)) else null

                tx.update(
                    bookingRef,
                    mapOf(
                        "status" to "Cancelled",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                if (itemSnap != null && itemSnap.exists() && itemSnap.getString("activeBookingId") == bookingId) {
                    tx.update(
                        itemSnap.reference,
                        mapOf<String, Any?>(
                            "availableToday" to true,
                            "activeBookingId" to null,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                parseFirestoreBookingSnapshot(bookingSnap).copy(status = "Cancelled")
            }.await()
            Result.success(updated)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e !is IllegalStateException && e !is IllegalArgumentException) {
                handleFirestoreError(e, OperationType.UPDATE, bookingRef.path)
            }
            Result.failure(e)
        }
    }

    /**
     * Atomically submit a renter review on a Completed booking and update the item's aggregate rating.
     */
    suspend fun submitBookingReviewTransaction(
        bookingId: String,
        rating: Int,
        comment: String
    ): Result<Pair<Float, Int>> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val bookingRef = firestore.collection("bookings").document(bookingId)
        val clamped = rating.coerceIn(1, 5)
        val cleanedComment = comment.trim().take(1000)
        return try {
            val resultPair = firestore.runTransaction { tx ->
                val bookingSnap = tx.get(bookingRef)
                if (!bookingSnap.exists()) {
                    throw IllegalArgumentException("Reservation not found.")
                }
                val renterId = bookingSnap.getString("renterId").orEmpty()
                if (renterId != uid) {
                    throw IllegalStateException("Only the borrower can review this rental.")
                }
                val status = bookingSnap.getString("status").orEmpty()
                if (!status.equals("Completed", ignoreCase = true)) {
                    throw IllegalStateException("Complete the rental before leaving a review.")
                }
                if (bookingSnap.getBoolean("isReviewed") == true) {
                    throw IllegalStateException("You have already reviewed this rental.")
                }
                val itemId = bookingSnap.getString("itemId").orEmpty()
                val itemSnap = if (itemId.isNotBlank()) tx.get(firestore.collection("items").document(itemId)) else null

                tx.update(
                    bookingRef,
                    mapOf(
                        "isReviewed" to true,
                        "ratingGiven" to clamped,
                        "reviewComment" to cleanedComment,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                if (itemSnap != null && itemSnap.exists()) {
                    val oldCount = itemSnap.getLong("reviewCount")?.toInt() ?: 0
                    val oldRating = itemSnap.getDouble("rating") ?: 0.0
                    val newCount = oldCount + 1
                    val newAvg = ((oldRating * oldCount) + clamped) / newCount.toDouble()
                    val roundedAvg = kotlin.math.round(newAvg * 10.0) / 10.0

                    tx.update(
                        itemSnap.reference,
                        mapOf(
                            "rating" to roundedAvg,
                            "reviewCount" to newCount,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    roundedAvg.toFloat() to newCount
                } else {
                    clamped.toFloat() to 1
                }
            }.await()
            Result.success(resultPair)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e !is IllegalStateException && e !is IllegalArgumentException) {
                handleFirestoreError(e, OperationType.UPDATE, bookingRef.path)
            }
            Result.failure(e)
        }
    }

    private fun parseFirestoreBookingSnapshot(doc: DocumentSnapshot): FirestoreBooking {
        return FirestoreBooking(
            id = doc.id,
            renterId = doc.getString("renterId") ?: "",
            renterName = doc.getString("renterName") ?: "",
            hostId = doc.getString("hostId") ?: "",
            hostName = doc.getString("hostName") ?: "",
            itemId = doc.getString("itemId") ?: "",
            itemTitle = doc.getString("itemTitle") ?: "Gear Rental",
            startDate = doc.getString("startDate") ?: "",
            endDate = doc.getString("endDate") ?: "",
            daysCount = doc.getLong("daysCount")?.toInt() ?: 1,
            pricePerDay = doc.getLong("pricePerDay")?.toInt() ?: 0,
            totalAmount = doc.getLong("totalAmount")?.toInt() ?: 0,
            status = doc.getString("status") ?: "Confirmed",
            deliveryOption = doc.getString("deliveryOption") ?: "Self Pickup",
            handoverOtp = doc.getString("handoverOtp") ?: "",
            isReviewed = doc.getBoolean("isReviewed") ?: false,
            ratingGiven = doc.getLong("ratingGiven")?.toInt() ?: 0,
            reviewComment = doc.getString("reviewComment") ?: ""
        )
    }

    /**
     * Create a rental booking (/bookings/{bookingId})
     */
    suspend fun createBooking(booking: FirestoreBooking): Result<FirestoreBooking> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        if (booking.hostId.isBlank() || booking.hostId == uid) {
            return Result.failure(IllegalStateException("You cannot book your own listing."))
        }
        val docRef = firestore.collection("bookings").document(booking.id)
        return try {
            val renterName = booking.renterName.ifBlank { auth.currentUser?.displayName ?: "Renter" }
            val data = mapOf<String, Any>(
                "id" to booking.id,
                "renterId" to uid,
                "renterName" to renterName,
                "hostId" to booking.hostId,
                "hostName" to booking.hostName,
                "itemId" to booking.itemId,
                "itemTitle" to booking.itemTitle,
                "startDate" to booking.startDate,
                "endDate" to booking.endDate,
                "daysCount" to booking.daysCount,
                "pricePerDay" to booking.pricePerDay,
                "totalAmount" to booking.totalAmount,
                "status" to booking.status,
                "deliveryOption" to booking.deliveryOption,
                "handoverOtp" to booking.handoverOtp,
                "isReviewed" to booking.isReviewed,
                "ratingGiven" to booking.ratingGiven,
                "reviewComment" to booking.reviewComment,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(data).await()
            Result.success(booking.copy(renterId = uid, renterName = renterName))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(bookingId: String, newStatus: String): Result<Unit> {
        val docRef = firestore.collection("bookings").document(bookingId)
        return try {
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun submitBookingReview(
        bookingId: String,
        rating: Int,
        comment: String
    ): Result<Unit> {
        val docRef = firestore.collection("bookings").document(bookingId)
        return try {
            docRef.update(
                mapOf(
                    "isReviewed" to true,
                    "ratingGiven" to rating.coerceIn(1, 5),
                    "reviewComment" to comment.trim().take(1000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun purgeAllItemsAndCommunityPosts(): Result<Pair<Int, Int>> {
        try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        var deletedItems = 0
        var deletedPosts = 0
        val itemsCol = firestore.collection("items")
        val postsCol = firestore.collection("community_posts")
        return try {
            val itemDocs = itemsCol.get().await().documents
            for (doc in itemDocs) {
                try {
                    doc.reference.delete().await()
                    deletedItems++
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.DELETE, doc.reference.path)
                }
            }

            val postDocs = postsCol.get().await().documents
            for (doc in postDocs) {
                try {
                    doc.reference.delete().await()
                    deletedPosts++
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.DELETE, doc.reference.path)
                }
            }
            Result.success(deletedItems to deletedPosts)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "items|community_posts")
            Result.failure(e)
        }
    }

    private fun computeInitials(name: String): String {
        return name.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
            .ifBlank { "U" }
    }

    private fun imageResForCategory(category: RentalCategory): Int {
        return 0
    }

    suspend fun fetchConversationsOnce(uid: String, knownItems: List<RentalItem> = emptyList()): Result<List<ChatConversation>> {
        if (uid.isBlank()) return Result.success(emptyList())
        val collectionRef = firestore.collection("conversations")
        return try {
            val snapshot = collectionRef.whereArrayContains("participantIds", uid).get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    val hostId = doc.getString("hostId") ?: ""
                    val hostName = doc.getString("hostName") ?: "Host"
                    val renterId = doc.getString("renterId") ?: ""
                    val renterName = doc.getString("renterName") ?: "Member"
                    val isHost = uid == hostId && renterId.isNotBlank() && hostId != renterId
                    val displayOtherName = if (isHost && renterName.isNotBlank()) renterName else hostName
                    val otherInitials = if (isHost) computeInitials(displayOtherName)
                    else doc.getString("hostInitials")?.takeIf { it.isNotBlank() } ?: computeInitials(displayOtherName)
                    val counterparty = RentalOwner(
                        id = if (isHost) renterId else hostId,
                        name = displayOtherName,
                        badge = if (isHost) "Community Member" else (doc.getString("hostBadge") ?: "Community Member"),
                        initials = otherInitials,
                        phone = if (isHost) "" else (doc.getString("hostPhone") ?: "")
                    )
                    val lastSenderId = doc.getString("lastSenderId").orEmpty()
                    val lastMessageText = doc.getString("lastMessageText").orEmpty()
                    val lastMessages = if (lastMessageText.isNotBlank()) {
                        listOf(
                            RentalMessage(
                                id = "last_${doc.id}",
                                senderName = if (lastSenderId == uid) "You" else displayOtherName,
                                text = lastMessageText,
                                timestamp = doc.getString("lastMessageTimestamp") ?: "Just now",
                                isFromMe = lastSenderId == uid,
                                senderId = lastSenderId,
                                status = MessageStatus.READ
                            )
                        )
                    } else emptyList()
                    val itemId = doc.getString("itemId")?.takeIf { it.isNotBlank() }
                    val itemContext = itemId?.let { id ->
                        knownItems.find { it.id == id } ?: doc.getString("itemTitle")?.let { title ->
                            RentalItem(
                                id = id,
                                title = title,
                                category = RentalCategory.ELECTRONICS,
                                pricePerDay = doc.getLong("itemPricePerDay")?.toInt() ?: 0,
                                location = "",
                                rating = 0f,
                                reviewCount = 0,
                                description = "",
                                primaryImageRes = 0,
                                features = emptyList(),
                                owner = counterparty
                            )
                        }
                    }
                    val storedUnread = doc.getLong("unreadCount")?.toInt() ?: 0
                    val effectiveUnread = if (lastSenderId.isNotBlank() && lastSenderId == uid) 0 else storedUnread
                    val mutedBy = (doc.get("mutedByUserIds") as? List<*>)
                        ?.mapNotNull { it?.toString()?.takeIf { id -> id.isNotBlank() } }
                        ?: emptyList()
                    ChatConversation(
                        id = doc.id,
                        owner = counterparty,
                        itemContext = itemContext,
                        communityPostId = doc.getString("communityPostId")?.takeIf { it.isNotBlank() },
                        communityPostTitle = doc.getString("communityPostTitle")?.takeIf { it.isNotBlank() },
                        lastSenderId = lastSenderId,
                        isOnline = doc.getBoolean("isOnline") ?: true,
                        isTyping = doc.getBoolean("isTyping") ?: false,
                        unreadCount = effectiveUnread,
                        isPinned = doc.getBoolean("isPinned") ?: false,
                        mutedByUserIds = mutedBy,
                        disappearingTimerSeconds = doc.getLong("disappearingTimerSeconds") ?: 0L,
                        e2eeEnabled = doc.getBoolean("e2eeEnabled") ?: true,
                        messages = lastMessages
                    )
                } catch (_: Exception) {
                    null
                }
            }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, collectionRef.path)
            Result.failure(e)
        }
    }

    suspend fun fetchBookingsOnce(uid: String, knownItems: List<RentalItem> = emptyList()): Result<List<UserRentalBooking>> {
        if (uid.isBlank()) return Result.success(emptyList())
        val collectionRef = firestore.collection("bookings")
        return try {
            val renterDocs = collectionRef.whereEqualTo("renterId", uid).get().await().documents
            val hostDocs = collectionRef.whereEqualTo("hostId", uid).get().await().documents
            val mergedDocs = linkedMapOf<String, DocumentSnapshot>()
            renterDocs.forEach { mergedDocs[it.id] = it }
            hostDocs.forEach { mergedDocs[it.id] = it }

            val list = mergedDocs.values.mapNotNull { doc ->
                try {
                    val itemId = doc.getString("itemId") ?: ""
                    val itemTitle = doc.getString("itemTitle") ?: "Gear Rental"
                    val pricePerDay = doc.getLong("pricePerDay")?.toInt() ?: 0
                    val hostId = doc.getString("hostId") ?: ""
                    val hostName = doc.getString("hostName") ?: "Host"
                    val status = doc.getString("status") ?: "Confirmed"
                    val resolvedItem = knownItems.find { it.id == itemId } ?: RentalItem(
                        id = itemId,
                        title = itemTitle,
                        category = RentalCategory.ELECTRONICS,
                        pricePerDay = pricePerDay,
                        location = "",
                        rating = 0f,
                        reviewCount = 0,
                        description = "",
                        primaryImageRes = 0,
                        features = emptyList(),
                        owner = RentalOwner(id = hostId, name = hostName)
                    )
                    UserRentalBooking(
                        id = doc.id,
                        item = resolvedItem,
                        hostId = hostId,
                        hostName = hostName,
                        renterId = doc.getString("renterId") ?: "",
                        renterName = doc.getString("renterName") ?: "",
                        startDate = doc.getString("startDate") ?: "",
                        endDate = doc.getString("endDate") ?: "",
                        daysRemaining = doc.getLong("daysCount")?.toInt() ?: 1,
                        totalPaid = doc.getLong("totalAmount")?.toInt() ?: 0,
                        status = status,
                        handoverOtp = doc.getString("handoverOtp") ?: "",
                        deliveryOption = doc.getString("deliveryOption") ?: "Self Pickup",
                        isCompleted = status.equals("Completed", ignoreCase = true) ||
                            status.equals("Cancelled", ignoreCase = true),
                        isReviewed = doc.getBoolean("isReviewed") ?: false,
                        ratingGiven = doc.getLong("ratingGiven")?.toInt() ?: 0,
                        reviewComment = doc.getString("reviewComment") ?: ""
                    )
                } catch (_: Exception) {
                    null
                }
            }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, collectionRef.path)
            Result.failure(e)
        }
    }

    suspend fun fetchCommunityPostsOnce(): Result<List<UserCommunityPost>> {
        val collectionRef = firestore.collection("community_posts")
        return try {
            val snapshot = collectionRef.get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    val type = try {
                        CommunityPostType.valueOf(doc.getString("typeStr") ?: "EVENT")
                    } catch (_: Exception) {
                        CommunityPostType.EVENT
                    }
                    val status = try {
                        CommunityPostStatus.valueOf(doc.getString("statusStr") ?: "ACTIVE")
                    } catch (_: Exception) {
                        CommunityPostStatus.ACTIVE
                    }
                    val attendeeIdsList = (doc.get("attendeeIds") as? List<*>)
                        ?.mapNotNull { it?.toString()?.takeIf { id -> id.isNotBlank() } }
                        ?: emptyList()
                    UserCommunityPost(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        type = type,
                        categoryName = doc.getString("categoryName") ?: "CREATIVE_PHOTO",
                        description = doc.getString("description") ?: "",
                        location = doc.getString("location") ?: "",
                        dateTime = doc.getString("dateTime") ?: "",
                        attendeesOrResponses = doc.getString("attendeesOrResponses") ?: "0 participating",
                        attendeeIds = attendeeIdsList,
                        status = status,
                        createdAt = "",
                        createdAtEpoch = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                        latitude = doc.getDouble("latitude"),
                        longitude = doc.getDouble("longitude"),
                        authorId = doc.getString("authorId") ?: "",
                        authorName = doc.getString("authorName") ?: "Community Member",
                        imageUrl = doc.getString("imageUrl")?.takeIf { it.isNotBlank() }
                    )
                } catch (_: Exception) {
                    null
                }
            }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, collectionRef.path)
            Result.failure(e)
        }
    }

    suspend fun fetchOwnedItemsOnce(uid: String): Result<List<RentalItem>> {
        if (uid.isBlank()) return Result.success(emptyList())
        val collectionRef = firestore.collection("items")
        return try {
            val snapshot = collectionRef.whereEqualTo("ownerId", uid).get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    val category = try {
                        RentalCategory.valueOf(doc.getString("categoryName") ?: "ELECTRONICS")
                    } catch (_: Exception) {
                        RentalCategory.ELECTRONICS
                    }
                    val ownerName = doc.getString("ownerName") ?: "Community Member"
                    RentalItem(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        category = category,
                        pricePerDay = doc.getLong("pricePerDay")?.toInt() ?: 0,
                        location = doc.getString("location") ?: "",
                        rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                        reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
                        description = doc.getString("description") ?: "",
                        primaryImageRes = 0,
                        features = emptyList(),
                        owner = RentalOwner(
                            id = doc.getString("ownerId") ?: "",
                            name = ownerName,
                            badge = doc.getString("ownerBadge") ?: "Community Member",
                            initials = doc.getString("ownerInitials") ?: computeInitials(ownerName)
                        ),
                        availableToday = doc.getBoolean("availableToday") ?: true,
                        activeBookingId = doc.getString("activeBookingId")?.takeIf { it.isNotBlank() }
                    )
                } catch (_: Exception) {
                    null
                }
            }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, collectionRef.path)
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FirestoreService? = null

        fun getInstance(context: Context): FirestoreService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreService(context).also { INSTANCE = it }
            }
        }
    }
}
