package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.RentalRepository
import com.example.data.firebase.FirestoreService
import com.example.data.local.AppDatabase
import com.example.data.local.CommunityEventDao
import com.example.data.local.toEntity
import com.example.data.local.toUserCommunityPost
import com.example.data.models.CommunityPostStatus
import com.example.data.models.UserCommunityPost
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

class CommunityEventRepository(
    private val eventDao: CommunityEventDao,
    private val firestoreService: FirestoreService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ICommunityEventRepository {

    private val tag = "CommunityEventRepo"
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private var syncJob: Job? = null

    override val allEvents: Flow<List<UserCommunityPost>> = eventDao.getAllEvents()
        .map { entities -> entities.map { it.toUserCommunityPost() } }
        .distinctUntilChanged()

    override val activeEvents: Flow<List<UserCommunityPost>> = eventDao.getActiveEvents()
        .map { entities -> entities.map { it.toUserCommunityPost() } }
        .distinctUntilChanged()

    fun startAuthenticatedSync() {
        if (Firebase.auth.currentUser == null) return
        if (syncJob?.isActive == true) return
        syncJob = repositoryScope.launch {
            try {
                firestoreService.listenToCommunityPosts().collect { cloudPosts ->
                    eventDao.clearEvents()
                    if (cloudPosts.isNotEmpty()) {
                        val entities = cloudPosts.map { it.toEntity(isSynced = true) }
                        eventDao.insertEvents(entities)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(tag, "Realtime Firestore community sync failed: ${e.message}")
            }
        }
    }

    fun stopSync() {
        syncJob?.cancel()
        syncJob = null
    }

    override fun getEventById(id: String): Flow<UserCommunityPost?> =
        eventDao.getEventById(id).map { it?.toUserCommunityPost() }

    override suspend fun getEventByIdOnce(id: String): UserCommunityPost? = withContext(ioDispatcher) {
        eventDao.getEventByIdOnce(id)?.toUserCommunityPost()
    }

    override suspend fun createEvent(post: UserCommunityPost): Result<UserCommunityPost> = withContext(ioDispatcher) {
        try {
            val entity = post.toEntity(isSynced = false)
            eventDao.insertEvent(entity)

            val cloudResult = firestoreService.createCommunityPost(post)
            cloudResult.onSuccess { syncedPost ->
                eventDao.insertEvent(syncedPost.toEntity(isSynced = true))
            }.onFailure { error ->
                Log.e(tag, "Failed to sync created event to Firestore: ${error.message}")
            }
            Result.success(cloudResult.getOrDefault(post))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(tag, "Failed to create community event: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateEventStatus(id: String, status: CommunityPostStatus): Result<Unit> = withContext(ioDispatcher) {
        try {
            eventDao.updateStatus(id, status.name)
            val result = firestoreService.updateCommunityPostStatus(id, status)
            result.onFailure { Log.e(tag, "Status update failed in Firestore: ${it.message}") }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(tag, "Failed to update event status: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun rsvpToEvent(id: String): Result<String> = withContext(ioDispatcher) {
        try {
            val current = eventDao.getEventByIdOnce(id)
            val currentText = current?.attendeesOrResponses ?: "0 participating"

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

            eventDao.updateAttendees(id, newAttendeesText)
            firestoreService.updateCommunityPostAttendees(id, newAttendeesText)

            val feedback = when (current?.type) {
                "HELP_REQUEST" -> "Response sent to organizer!"
                "STUDY_GROUP" -> "Desk reserved at study table!"
                else -> "RSVP confirmed! Spot saved for event."
            }

            Result.success(feedback)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(tag, "Failed to RSVP to event: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteEvent(id: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            eventDao.deleteEventById(id)
            firestoreService.deleteCommunityPost(id)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete event: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun syncWithFirestore(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (Firebase.auth.currentUser == null) {
                return@withContext Result.failure(IllegalStateException("User not signed in"))
            }
            val cloudPosts = firestoreService.listenToCommunityPosts().first()
            if (cloudPosts.isNotEmpty()) {
                val entities = cloudPosts.map { it.toEntity(isSynced = true) }
                eventDao.insertEvents(entities)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(tag, "Manual sync error: ${e.message}", e)
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
                        firestoreService = FirestoreService.getInstance(context)
                    ).also { INSTANCE = it }
                }
            }
        }

        fun get(): CommunityEventRepository? = INSTANCE
    }
}
