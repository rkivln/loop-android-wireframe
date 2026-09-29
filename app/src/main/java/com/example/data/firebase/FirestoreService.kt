package com.example.data.firebase

import android.util.Log
import com.example.R
import com.example.data.RentalRepository
import com.example.data.models.ChatConversation
import com.example.data.models.ChatMessageType
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType
import com.example.data.models.MessageStatus
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.data.models.RentalOwner
import com.example.data.models.SpecFeature
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService private constructor() {

    private val TAG = "FirestoreService"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(com.google.firebase.FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                // Enable modern persistent cache settings for fast offline-first responsiveness
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        PersistentCacheSettings.newBuilder()
                            .setSizeBytes(100 * 1024 * 1024) // 100 MB cache
                            .build()
                    )
                    .build()
                db.firestoreSettings = settings
                db
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not initialized, defaulting to in-memory reactive layer: ${e.message}")
            null
        }
    }

    val isFirebaseAvailable: Boolean
        get() = firestore != null

    /**
     * Real-time listener for All Equipment Listings in Firestore
     */
    fun listenToItems(): Flow<List<RentalItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(RentalRepository.items)
            awaitClose { }
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to items: ${error.message}")
                    trySend(RentalRepository.items)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        try {
                            val categoryStr = doc.getString("categoryName") ?: "ELECTRONICS"
                            val category = try {
                                RentalCategory.valueOf(categoryStr)
                            } catch (e: Exception) {
                                RentalCategory.ELECTRONICS
                            }

                            val imageRes = when (category) {
                                RentalCategory.ELECTRONICS -> R.drawable.canon_eos_camera_1790477967758
                                RentalCategory.STUDY_OFFICE -> R.drawable.modern_laptop_1790477981432
                                RentalCategory.VEHICLES -> R.drawable.electric_scooter_1790477992190
                                RentalCategory.FURNITURE -> R.drawable.modern_armchair_1790478015816
                                else -> R.drawable.canon_eos_camera_1790477967758
                            }

                            val featuresList = (doc.get("features") as? List<Map<String, Any>>)?.map { f ->
                                SpecFeature(
                                    iconType = f["iconType"] as? String ?: "CAMERA",
                                    label = f["label"] as? String ?: ""
                                )
                            } ?: listOf(SpecFeature("CAMERA", "Verified Item"))

                            RentalItem(
                                id = doc.id,
                                title = doc.getString("title") ?: "Item",
                                category = category,
                                pricePerDay = doc.getLong("pricePerDay")?.toInt() ?: 500,
                                location = doc.getString("location") ?: "Puducherry",
                                rating = (doc.getDouble("rating") ?: 4.9).toFloat(),
                                reviewCount = doc.getLong("reviewCount")?.toInt() ?: 12,
                                description = doc.getString("description") ?: "",
                                primaryImageRes = imageRes,
                                features = featuresList,
                                owner = RentalOwner(
                                    id = doc.getString("ownerId") ?: "owner_1",
                                    name = doc.getString("ownerName") ?: "Host",
                                    badge = "Top Owner",
                                    initials = doc.getString("ownerInitials") ?: "H"
                                ),
                                isPopular = doc.getBoolean("isPopular") ?: true,
                                isFavorite = doc.getBoolean("isFavorite") ?: false,
                                availableToday = doc.getBoolean("availableToday") ?: true
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing item doc: ${e.message}")
                            null
                        }
                    }
                    trySend(items)
                } else {
                    // Seed initial data if collection is empty
                    seedInitialItems(db)
                    trySend(RentalRepository.items)
                }
            }

        awaitClose { registration.remove() }
    }

    /**
     * Real-time listener for Conversations in Firestore
     */
    fun listenToConversations(): Flow<List<ChatConversation>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(RentalRepository.initialConversations)
            awaitClose { }
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection("conversations")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to conversations: ${error.message}")
                    trySend(RentalRepository.initialConversations)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val conversations = snapshot.documents.mapNotNull { doc ->
                        try {
                            val host = RentalOwner(
                                id = doc.getString("hostId") ?: "owner_1",
                                name = doc.getString("hostName") ?: "Host",
                                badge = doc.getString("hostBadge") ?: "Top Owner",
                                initials = doc.getString("hostInitials") ?: "H",
                                phone = doc.getString("hostPhone") ?: "+91 98765 43210"
                            )

                            val lastMessage = RentalMessage(
                                id = "last_${doc.id}",
                                senderName = host.name,
                                text = doc.getString("lastMessageText") ?: "Hello!",
                                timestamp = doc.getString("lastMessageTimestamp") ?: "10:30 AM",
                                isFromMe = false,
                                status = MessageStatus.READ
                            )

                            val itemContext = doc.getString("itemId")?.let { itemId ->
                                RentalRepository.items.find { it.id == itemId }
                            } ?: RentalRepository.items.firstOrNull()

                            ChatConversation(
                                id = doc.id,
                                owner = host,
                                itemContext = itemContext,
                                isOnline = doc.getBoolean("isOnline") ?: true,
                                isTyping = doc.getBoolean("isTyping") ?: false,
                                unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0,
                                isPinned = doc.getBoolean("isPinned") ?: false,
                                messages = listOf(lastMessage)
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing conv doc: ${e.message}")
                            null
                        }
                    }
                    trySend(conversations)
                } else {
                    seedInitialConversations(db)
                    trySend(RentalRepository.initialConversations)
                }
            }

        awaitClose { registration.remove() }
    }

    /**
     * Real-time listener for Live Messages inside a Specific Conversation
     */
    fun listenToMessages(conversationId: String): Flow<List<RentalMessage>> = callbackFlow {
        val db = firestore
        if (db == null) {
            val fallback = RentalRepository.initialConversations.find { it.id == conversationId }?.messages
                ?: RentalRepository.sampleMessages
            trySend(fallback)
            awaitClose { }
            return@callbackFlow
        }

        val registration = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestampEpoch", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to messages for $conversationId: ${error.message}")
                    val fallback = RentalRepository.initialConversations.find { it.id == conversationId }?.messages
                        ?: RentalRepository.sampleMessages
                    trySend(fallback)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            val msgTypeStr = doc.getString("messageTypeStr") ?: "TEXT"
                            val msgType = try {
                                ChatMessageType.valueOf(msgTypeStr)
                            } catch (e: Exception) {
                                ChatMessageType.TEXT
                            }

                            val statusStr = doc.getString("statusStr") ?: "READ"
                            val status = try {
                                MessageStatus.valueOf(statusStr)
                            } catch (e: Exception) {
                                MessageStatus.READ
                            }

                            RentalMessage(
                                id = doc.id,
                                senderName = doc.getString("senderName") ?: "Me",
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getString("timestampStr") ?: "Just now",
                                isFromMe = doc.getBoolean("isFromMe") ?: true,
                                messageType = msgType,
                                status = status,
                                audioDuration = doc.getString("audioDuration"),
                                locationTitle = doc.getString("locationTitle"),
                                locationAddress = doc.getString("locationAddress"),
                                itemTitle = doc.getString("itemTitle"),
                                itemPricePerDay = doc.getLong("itemPricePerDay")?.toInt()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing message doc: ${e.message}")
                            null
                        }
                    }
                    trySend(messages)
                } else {
                    // Seed initial messages for this conversation
                    val initial = RentalRepository.initialConversations.find { it.id == conversationId }?.messages
                        ?: RentalRepository.sampleMessages
                    seedInitialMessages(db, conversationId, initial)
                    trySend(initial)
                }
            }

        awaitClose { registration.remove() }
    }

    /**
     * Send message to Firestore with automatic conversation lastMessage update
     */
    suspend fun sendMessage(
        conversationId: String,
        message: RentalMessage
    ) {
        val db = firestore ?: return
        try {
            val msgData = hashMapOf(
                "id" to message.id,
                "senderName" to message.senderName,
                "text" to message.text,
                "timestampStr" to message.timestamp,
                "timestampEpoch" to System.currentTimeMillis(),
                "isFromMe" to message.isFromMe,
                "messageTypeStr" to message.messageType.name,
                "statusStr" to message.status.name,
                "audioDuration" to message.audioDuration,
                "locationTitle" to message.locationTitle,
                "locationAddress" to message.locationAddress,
                "itemTitle" to message.itemTitle,
                "itemPricePerDay" to message.itemPricePerDay
            )

            // Add to messages sub-collection
            db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document(message.id)
                .set(msgData)
                .await()

            // Update parent conversation summary
            val updateData = hashMapOf(
                "lastMessageText" to message.text,
                "lastMessageTimestamp" to message.timestamp,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("conversations")
                .document(conversationId)
                .set(updateData, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message to Firestore: ${e.message}")
        }
    }

    /**
     * Create a new equipment listing in Firestore
     */
    suspend fun createListing(item: RentalItem) {
        val db = firestore ?: return
        try {
            val itemData = hashMapOf(
                "id" to item.id,
                "title" to item.title,
                "categoryName" to item.category.name,
                "pricePerDay" to item.pricePerDay,
                "location" to item.location,
                "rating" to item.rating.toDouble(),
                "reviewCount" to item.reviewCount,
                "description" to item.description,
                "ownerId" to item.owner.id,
                "ownerName" to item.owner.name,
                "ownerInitials" to item.owner.initials,
                "isPopular" to item.isPopular,
                "isFavorite" to item.isFavorite,
                "availableToday" to item.availableToday,
                "createdAt" to System.currentTimeMillis(),
                "features" to item.features.map {
                    hashMapOf("iconType" to it.iconType, "label" to it.label)
                }
            )

            db.collection("items")
                .document(item.id)
                .set(itemData)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create listing in Firestore: ${e.message}")
        }
    }

    /**
     * Seed initial equipment listings to Firestore
     */
    private fun seedInitialItems(db: FirebaseFirestore) {
        try {
            val batch = db.batch()
            RentalRepository.items.forEach { item ->
                val docRef = db.collection("items").document(item.id)
                val data = hashMapOf(
                    "id" to item.id,
                    "title" to item.title,
                    "categoryName" to item.category.name,
                    "pricePerDay" to item.pricePerDay,
                    "location" to item.location,
                    "rating" to item.rating.toDouble(),
                    "reviewCount" to item.reviewCount,
                    "description" to item.description,
                    "ownerId" to item.owner.id,
                    "ownerName" to item.owner.name,
                    "ownerInitials" to item.owner.initials,
                    "isPopular" to item.isPopular,
                    "isFavorite" to item.isFavorite,
                    "availableToday" to item.availableToday,
                    "createdAt" to System.currentTimeMillis(),
                    "features" to item.features.map {
                        hashMapOf("iconType" to it.iconType, "label" to it.label)
                    }
                )
                batch.set(docRef, data)
            }
            batch.commit()
        } catch (e: Exception) {
            Log.w(TAG, "Initial item seed skipped: ${e.message}")
        }
    }

    /**
     * Seed initial conversations to Firestore
     */
    private fun seedInitialConversations(db: FirebaseFirestore) {
        try {
            val batch = db.batch()
            RentalRepository.initialConversations.forEach { conv ->
                val docRef = db.collection("conversations").document(conv.id)
                val data = hashMapOf(
                    "id" to conv.id,
                    "hostId" to conv.owner.id,
                    "hostName" to conv.owner.name,
                    "hostInitials" to conv.owner.initials,
                    "hostBadge" to conv.owner.badge,
                    "hostPhone" to conv.owner.phone,
                    "itemId" to conv.itemContext?.id,
                    "itemTitle" to conv.itemContext?.title,
                    "itemPricePerDay" to conv.itemContext?.pricePerDay,
                    "isOnline" to conv.isOnline,
                    "isTyping" to conv.isTyping,
                    "unreadCount" to conv.unreadCount,
                    "isPinned" to conv.isPinned,
                    "lastMessageText" to (conv.lastMessage?.text ?: "Hi there!"),
                    "lastMessageTimestamp" to (conv.lastMessage?.timestamp ?: "10:30 AM"),
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, data)
            }
            batch.commit()
        } catch (e: Exception) {
            Log.w(TAG, "Initial conv seed skipped: ${e.message}")
        }
    }

    private fun seedInitialMessages(
        db: FirebaseFirestore,
        conversationId: String,
        messages: List<RentalMessage>
    ) {
        try {
            val batch = db.batch()
            messages.forEach { msg ->
                val docRef = db.collection("conversations")
                    .document(conversationId)
                    .collection("messages")
                    .document(msg.id)
                val data = hashMapOf(
                    "id" to msg.id,
                    "senderName" to msg.senderName,
                    "text" to msg.text,
                    "timestampStr" to msg.timestamp,
                    "timestampEpoch" to System.currentTimeMillis(),
                    "isFromMe" to msg.isFromMe,
                    "messageTypeStr" to msg.messageType.name,
                    "statusStr" to msg.status.name,
                    "audioDuration" to msg.audioDuration,
                    "locationTitle" to msg.locationTitle,
                    "locationAddress" to msg.locationAddress,
                    "itemTitle" to msg.itemTitle,
                    "itemPricePerDay" to msg.itemPricePerDay
                )
                batch.set(docRef, data)
            }
            batch.commit()
        } catch (e: Exception) {
            Log.w(TAG, "Initial message seed skipped: ${e.message}")
        }
    }

    companion object {
        val instance: FirestoreService by lazy { FirestoreService() }
    }
}
