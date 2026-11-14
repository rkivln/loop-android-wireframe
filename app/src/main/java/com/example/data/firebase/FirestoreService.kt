package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.models.ChatConversation
import com.example.data.models.ChatMessageType
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.MessageStatus
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.data.models.RentalOwner
import com.example.data.models.SpecFeature
import com.example.data.models.UserCommunityPost
import com.example.data.models.UserProfile
import com.example.data.models.UserRentalBooking
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    /**
     * Real-time listener for User Profile in Firestore (/users/{uid})
     */
    fun listenToUserProfile(uid: String): Flow<UserProfile> {
        val docRef = firestore.collection("users").document(uid)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    val rawDisplayName = snapshot.getString("displayName")
                        ?: auth.currentUser?.displayName
                        ?: "Loop Member"
                    val badgesList = (snapshot.get("badges") as? List<*>)?.mapNotNull { it?.toString() }
                        ?: listOf("✓ ID Verified", "★ Community Member")
                    val favoritesList = (snapshot.get("favoriteItemIds") as? List<*>)?.mapNotNull { it?.toString() }
                        ?: emptyList()

                    UserProfile(
                        uid = snapshot.id,
                        displayName = rawDisplayName,
                        email = snapshot.getString("email") ?: (auth.currentUser?.email ?: ""),
                        phone = snapshot.getString("phone") ?: "",
                        bio = snapshot.getString("bio") ?: "Creator & gear enthusiast in Puducherry.",
                        location = snapshot.getString("location") ?: "White Town, Puducherry",
                        avatarPresetIndex = snapshot.getLong("avatarPresetIndex")?.toInt() ?: 0,
                        customAvatarUri = snapshot.getString("customAvatarUri"),
                        rating = (snapshot.getDouble("rating") ?: 5.0).toFloat(),
                        reviewCount = snapshot.getLong("reviewCount")?.toInt() ?: 0,
                        rentalsCompleted = snapshot.getLong("rentalsCompleted")?.toInt() ?: 0,
                        listingsCount = snapshot.getLong("listingsCount")?.toInt() ?: 0,
                        earnedAmount = snapshot.getLong("earnedAmount")?.toInt() ?: 0,
                        isVerified = snapshot.getBoolean("isVerified") ?: true,
                        memberSince = snapshot.getString("memberSince") ?: "Joined 2026",
                        badges = badgesList,
                        favoriteItemIds = favoritesList
                    )
                } else {
                    UserProfile(
                        uid = uid,
                        displayName = auth.currentUser?.displayName ?: "Loop Member",
                        email = auth.currentUser?.email ?: ""
                    )
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                throw error
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

            val data = mutableMapOf<String, Any?>(
                "userId" to uid,
                "displayName" to profile.displayName.ifBlank { auth.currentUser?.displayName ?: "Loop Member" },
                "email" to profile.email.ifBlank { auth.currentUser?.email ?: "member@loop.in" },
                "phone" to profile.phone.ifBlank { null },
                "bio" to profile.bio.ifBlank { null },
                "location" to profile.location.ifBlank { "White Town, Puducherry" },
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
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (isNew) {
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
                        val featuresList = rawFeatures?.map { f ->
                            SpecFeature(
                                iconType = f["iconType"] as? String ?: "CAMERA",
                                label = f["label"] as? String ?: ""
                            )
                        } ?: listOf(SpecFeature("CAMERA", "Verified Gear"))

                        RentalItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "Equipment",
                            category = category,
                            pricePerDay = doc.getLong("pricePerDay")?.toInt() ?: 500,
                            location = doc.getString("location") ?: "Puducherry",
                            rating = (doc.getDouble("rating") ?: 4.9).toFloat(),
                            reviewCount = doc.getLong("reviewCount")?.toInt() ?: 1,
                            description = doc.getString("description") ?: "",
                            primaryImageRes = imageRes,
                            imageUrl = doc.getString("imageUrl"),
                            features = featuresList,
                            owner = RentalOwner(
                                id = doc.getString("ownerId") ?: "",
                                name = doc.getString("ownerName") ?: "Host",
                                badge = "Top Owner",
                                initials = doc.getString("ownerInitials") ?: "H"
                            ),
                            isPopular = doc.getBoolean("isPopular") ?: false,
                            isFavorite = doc.getBoolean("isFavorite") ?: false,
                            availableToday = doc.getBoolean("availableToday") ?: true,
                            securityDeposit = doc.getLong("securityDeposit")?.toInt() ?: 0
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
            val ownerName = item.owner.name.ifBlank { auth.currentUser?.displayName ?: "Loop Host" }
            val initials = item.owner.initials.ifBlank {
                ownerName.trim().split(" ").filter { it.isNotBlank() }
                    .take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "LH" }
            }
            val itemData = mapOf<String, Any>(
                "id" to item.id,
                "title" to item.title,
                "categoryName" to item.category.name,
                "pricePerDay" to item.pricePerDay,
                "securityDeposit" to item.securityDeposit,
                "location" to item.location,
                "rating" to item.rating.toDouble(),
                "reviewCount" to item.reviewCount,
                "description" to item.description,
                "imageUrl" to (item.imageUrl ?: ""),
                "ownerId" to uid,
                "ownerName" to ownerName,
                "ownerInitials" to initials,
                "isPopular" to item.isPopular,
                "isFavorite" to false,
                "availableToday" to item.availableToday,
                "features" to item.features.map {
                    mapOf("iconType" to it.iconType, "label" to it.label)
                },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(itemData).await()
            Result.success(
                item.copy(
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

                        UserCommunityPost(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            type = type,
                            categoryName = doc.getString("categoryName") ?: "CREATIVE_PHOTO",
                            description = doc.getString("description") ?: "",
                            location = doc.getString("location") ?: "Puducherry",
                            dateTime = doc.getString("dateTime") ?: "Upcoming",
                            attendeesOrResponses = doc.getString("attendeesOrResponses") ?: "0 participating",
                            status = status,
                            createdAt = "Recently",
                            createdAtEpoch = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                            latitude = doc.getDouble("latitude") ?: 11.9340,
                            longitude = doc.getDouble("longitude") ?: 79.8330,
                            authorId = doc.getString("authorId") ?: "",
                            authorName = doc.getString("authorName") ?: "Organizer"
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
            val data = mapOf<String, Any>(
                "id" to post.id,
                "title" to post.title,
                "typeStr" to post.type.name,
                "categoryName" to post.categoryName,
                "description" to post.description,
                "location" to post.location,
                "dateTime" to post.dateTime,
                "attendeesOrResponses" to post.attendeesOrResponses,
                "statusStr" to post.status.name,
                "authorId" to uid,
                "authorName" to authorName,
                "latitude" to post.latitude,
                "longitude" to post.longitude,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(data).await()
            Result.success(post.copy(authorId = uid, authorName = authorName))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
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
                        val displayOtherName = if (uid == hostId && renterName.isNotBlank() && hostId != renterId) {
                            renterName
                        } else {
                            hostName
                        }
                        val hostInitials = doc.getString("hostInitials") ?: displayOtherName.take(2).uppercase()
                        val host = RentalOwner(
                            id = if (uid == hostId && renterId.isNotBlank()) renterId else hostId,
                            name = displayOtherName,
                            badge = doc.getString("hostBadge") ?: "Top Owner",
                            initials = hostInitials,
                            phone = doc.getString("hostPhone") ?: ""
                        )

                        val lastMessageText = doc.getString("lastMessageText").orEmpty()
                        val lastMessages = if (lastMessageText.isNotBlank()) {
                            listOf(
                                RentalMessage(
                                    id = "last_${doc.id}",
                                    senderName = displayOtherName,
                                    text = lastMessageText,
                                    timestamp = doc.getString("lastMessageTimestamp") ?: "Just now",
                                    isFromMe = false,
                                    status = MessageStatus.READ
                                )
                            )
                        } else {
                            emptyList()
                        }

                        val itemId = doc.getString("itemId")
                        val itemContext = itemId?.let { id ->
                            knownItems.find { it.id == id } ?: doc.getString("itemTitle")?.let { title ->
                                RentalItem(
                                    id = id,
                                    title = title,
                                    category = RentalCategory.ELECTRONICS,
                                    pricePerDay = doc.getLong("itemPricePerDay")?.toInt() ?: 0,
                                    location = "Puducherry",
                                    rating = 5.0f,
                                    reviewCount = 0,
                                    description = "",
                                    primaryImageRes = R.drawable.img_cafe,
                                    features = emptyList(),
                                    owner = host
                                )
                            }
                        }

                        ChatConversation(
                            id = doc.id,
                            owner = host,
                            itemContext = itemContext,
                            isOnline = doc.getBoolean("isOnline") ?: true,
                            isTyping = doc.getBoolean("isTyping") ?: false,
                            unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0,
                            isPinned = doc.getBoolean("isPinned") ?: false,
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
        customConversationId: String? = null
    ): Result<ChatConversation> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val cleanHostId = host.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "host_member" }
        val cleanItemSuffix = item?.id?.replace(Regex("[^a-zA-Z0-9_\\-]"), "_") ?: "general"
        val convId = customConversationId
            ?: "conv_${uid.take(24)}_${cleanHostId.take(24)}_${cleanItemSuffix.take(24)}"
        val docRef = firestore.collection("conversations").document(convId)

        return try {
            val existingSnap = docRef.get().await()
            val participantIds = listOf(uid, cleanHostId).distinct()
            val renterName = auth.currentUser?.displayName ?: "Loop Member"

            if (!existingSnap.exists()) {
                val convData = mutableMapOf<String, Any?>(
                    "id" to convId,
                    "participantIds" to participantIds,
                    "hostId" to cleanHostId,
                    "hostName" to host.name,
                    "hostInitials" to host.initials,
                    "hostBadge" to host.badge,
                    "hostPhone" to host.phone,
                    "renterId" to uid,
                    "renterName" to renterName,
                    "itemId" to item?.id,
                    "itemTitle" to item?.title,
                    "itemPricePerDay" to item?.pricePerDay,
                    "isOnline" to true,
                    "isTyping" to false,
                    "unreadCount" to 0,
                    "isPinned" to false,
                    "lastMessageText" to (item?.let { "Inquiry about ${it.title}" } ?: "Conversation started"),
                    "lastMessageTimestamp" to "Just now",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                @Suppress("UNCHECKED_CAST")
                docRef.set(convData.filterValues { it != null } as Map<String, Any>).await()
            }

            Result.success(
                ChatConversation(
                    id = convId,
                    owner = host.copy(id = cleanHostId),
                    itemContext = item,
                    isOnline = true,
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
                            locationTitle = doc.getString("locationTitle"),
                            locationAddress = doc.getString("locationAddress"),
                            itemTitle = doc.getString("itemTitle"),
                            itemPricePerDay = doc.getLong("itemPricePerDay")?.toInt()
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
     * Send an immutable message into an existing conversation
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
            @Suppress("UNCHECKED_CAST")
            val msgData = mapOf<String, Any?>(
                "id" to message.id,
                "senderId" to uid,
                "senderName" to senderName,
                "text" to message.text,
                "timestampStr" to message.timestamp,
                "timestampEpoch" to System.currentTimeMillis(),
                "isFromMe" to true,
                "messageTypeStr" to message.messageType.name,
                "statusStr" to message.status.name,
                "audioDuration" to message.audioDuration,
                "locationTitle" to message.locationTitle,
                "locationAddress" to message.locationAddress,
                "itemTitle" to message.itemTitle,
                "itemPricePerDay" to message.itemPricePerDay,
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            msgRef.set(msgData).await()

            convRef.update(
                mapOf(
                    "lastMessageText" to message.text,
                    "lastMessageTimestamp" to message.timestamp,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(message.copy(senderId = uid, senderName = senderName, isFromMe = true))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
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
                        location = "Puducherry",
                        rating = 5.0f,
                        reviewCount = 0,
                        description = "",
                        primaryImageRes = R.drawable.img_cafe,
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
                        handoverOtp = doc.getString("handoverOtp") ?: "8492",
                        deliveryOption = doc.getString("deliveryOption") ?: "Self Pickup",
                        isCompleted = status.equals("Completed", ignoreCase = true) ||
                            status.equals("Cancelled", ignoreCase = true)
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
     * Create a rental booking (/bookings/{bookingId})
     */
    suspend fun createBooking(booking: FirestoreBooking): Result<FirestoreBooking> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("bookings").document(booking.id)
        return try {
            val renterName = booking.renterName.ifBlank { auth.currentUser?.displayName ?: "Renter" }
            val data = mapOf<String, Any>(
                "id" to booking.id,
                "renterId" to uid,
                "renterName" to renterName,
                "hostId" to booking.hostId.ifBlank { uid },
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

    private fun imageResForCategory(category: RentalCategory): Int {
        return when (category) {
            RentalCategory.ELECTRONICS -> R.drawable.img_cafe
            RentalCategory.STUDY_OFFICE -> R.drawable.img_synth
            RentalCategory.VEHICLES -> R.drawable.img_ebike
            RentalCategory.FURNITURE, RentalCategory.HOME_LIVING -> R.drawable.img_street
            RentalCategory.EVENTS -> R.drawable.img_street
            RentalCategory.TOOLS -> R.drawable.img_synth
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
