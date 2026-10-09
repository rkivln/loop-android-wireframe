package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.RentalRepository
import com.example.data.firebase.FirestoreService
import com.example.data.local.AppDatabase
import com.example.data.local.CommunityEventDao
import com.example.data.local.CommunityEventEntity
import com.example.data.local.toEntity
import com.example.data.local.toUserCommunityPost
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.RentalCategory
import com.example.data.models.UserCommunityPost
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Clean Data Repository Interface for Community Events.
 * Coordinates local Room persistence with remote Firestore synchronization.
 */
interface ICommunityEventRepository {
    val allEvents: Flow<List<UserCommunityPost>>
    val activeEvents: Flow<List<UserCommunityPost>>
    fun getEventById(id: String): Flow<UserCommunityPost?>
    suspend fun getEventByIdOnce(id: String): UserCommunityPost?
    suspend fun createEvent(post: UserCommunityPost): Result<UserCommunityPost>
    suspend fun updateEventStatus(id: String, status: CommunityPostStatus): Result<Unit>
    suspend fun rsvpToEvent(id: String): Result<String>
    suspend fun deleteEvent(id: String): Result<Unit>
    suspend fun syncWithFirestore(): Result<Unit>
}

/**
 * Data repository implementation combining Room Database (local offline-first source of truth)
 * with Firebase Firestore (real-time cloud database).
 */
class CommunityEventRepository(
    private val eventDao: CommunityEventDao,
    private val firestoreService: FirestoreService = FirestoreService.instance,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ICommunityEventRepository {

    private val TAG = "CommunityEventRepo"
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    /**
     * All community events observed reactively from Room SQLite database.
     */
    override val allEvents: Flow<List<UserCommunityPost>> = eventDao.getAllEvents()
        .map { entities -> entities.map { it.toUserCommunityPost() } }
        .distinctUntilChanged()

    /**
     * Active events observed reactively from Room SQLite database.
     */
    override val activeEvents: Flow<List<UserCommunityPost>> = eventDao.getActiveEvents()
        .map { entities -> entities.map { it.toUserCommunityPost() } }
        .distinctUntilChanged()

    init {
        // 1. Seed initial events to Room if local database is empty
        repositoryScope.launch {
            try {
                val count = eventDao.getCount()
                if (count == 0) {
                    val initialEntities = RentalRepository.initialCommunityPosts.map { post ->
                        post.toEntity(isSynced = true)
                    }
                    eventDao.insertEvents(initialEntities)
                    Log.d(TAG, "Seeded ${initialEntities.size} initial community events into Room")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking/seeding Room database: ${e.message}")
            }
        }

        // 2. Start real-time Firestore listener to keep local Room database updated
        repositoryScope.launch {
            try {
                firestoreService.listenToCommunityPosts().collectLatest { cloudPosts ->
                    if (cloudPosts.isNotEmpty()) {
                        val entities = cloudPosts.map { it.toEntity(isSynced = true) }
                        eventDao.insertEvents(entities)
                        Log.d(TAG, "Synchronized ${entities.size} community posts from Firestore into Room")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Realtime Firestore sync exception: ${e.message}")
            }
        }
    }

    /**
     * Reactive stream for a specific event by ID from Room.
     */
    override fun getEventById(id: String): Flow<UserCommunityPost?> =
        eventDao.getEventById(id).map { it?.toUserCommunityPost() }

    /**
     * One-shot fetch for a single event from Room.
     */
    override suspend fun getEventByIdOnce(id: String): UserCommunityPost? = withContext(ioDispatcher) {
        eventDao.getEventByIdOnce(id)?.toUserCommunityPost()
    }

    /**
     * Stores a new community event.
     * Persists immediately to Room for instantaneous offline UI updates,
     * then synchronizes with Firestore.
     */
    override suspend fun createEvent(post: UserCommunityPost): Result<UserCommunityPost> = withContext(ioDispatcher) {
        try {
            // Write to Room first (optimistic local durability)
            val entity = post.toEntity(isSynced = false)
            eventDao.insertEvent(entity)

            // Sync with Firestore
            try {
                firestoreService.createCommunityPost(post)
                eventDao.updateSyncStatus(post.id, isSynced = true)
            } catch (fe: Exception) {
                Log.w(TAG, "Event saved locally to Room, pending Firestore cloud sync: ${fe.message}")
            }

            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create community event: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates an event's active/completed/archived status in both Room and Firestore.
     */
    override suspend fun updateEventStatus(id: String, status: CommunityPostStatus): Result<Unit> = withContext(ioDispatcher) {
        try {
            // Update Room locally
            eventDao.updateStatus(id, status.name)

            // Update Firestore
            try {
                firestoreService.updateCommunityPostStatus(id, status)
            } catch (fe: Exception) {
                Log.w(TAG, "Status updated in Room; Firestore pending: ${fe.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update event status: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Records an RSVP / response to a community event.
     * Updates attendee counts locally in Room and syncs to Firestore.
     */
    override suspend fun rsvpToEvent(id: String): Result<String> = withContext(ioDispatcher) {
        try {
            val current = eventDao.getEventByIdOnce(id)
            val currentText = current?.attendeesOrResponses ?: "0 registered"

            val numRegex = Regex("(\\d+)")
            val match = numRegex.find(currentText)
            val currentCount = match?.value?.toIntOrNull() ?: 1

            val newAttendeesText = when {
                currentText.contains("attending", ignoreCase = true) || currentText.contains("creators", ignoreCase = true) ->
                    "${currentCount + 1} creators attending"
                currentText.contains("responses", ignoreCase = true) ->
                    "${currentCount + 1} responses received"
                currentText.contains("booked", ignoreCase = true) || currentText.contains("desks", ignoreCase = true) ->
                    "${currentCount + 1} desks booked"
                else ->
                    "${currentCount + 1} members participating"
            }

            // Update in Room
            eventDao.updateAttendees(id, newAttendeesText)

            // Update in Firestore
            try {
                firestoreService.updateCommunityPostAttendees(id, newAttendeesText)
            } catch (fe: Exception) {
                Log.w(TAG, "Attendees updated in Room; Firestore sync pending: ${fe.message}")
            }

            val feedback = when (current?.type) {
                "HELP_REQUEST" -> "Response sent to organizer!"
                "STUDY_GROUP" -> "Desk reserved at study table!"
                else -> "RSVP confirmed! Spot saved for event."
            }

            Result.success(feedback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to RSVP to event: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a community event from both Room and Firestore.
     */
    override suspend fun deleteEvent(id: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            eventDao.deleteEventById(id)

            try {
                firestoreService.deleteCommunityPost(id)
            } catch (fe: Exception) {
                Log.w(TAG, "Event deleted locally from Room; Firestore pending: ${fe.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete event: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Manually triggers a one-shot fetch and synchronization from Firestore into Room.
     */
    override suspend fun syncWithFirestore(): Result<Unit> = withContext(ioDispatcher) {
        try {
            firestoreService.listenToCommunityPosts().collectLatest { cloudPosts ->
                if (cloudPosts.isNotEmpty()) {
                    val entities = cloudPosts.map { it.toEntity(isSynced = true) }
                    eventDao.insertEvents(entities)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Manual sync error: ${e.message}", e)
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CommunityEventRepository? = null

        fun getInstance(context: Context): CommunityEventRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val db = AppDatabase.getInstance(context)
                    CommunityEventRepository(
                        eventDao = db.communityEventDao(),
                        firestoreService = FirestoreService.instance
                    ).also { INSTANCE = it }
                }
            }
        }

        /**
         * Returns existing instance or creates a default one if ApplicationContext is accessible.
         */
        fun get(): CommunityEventRepository? = INSTANCE
    }
}
