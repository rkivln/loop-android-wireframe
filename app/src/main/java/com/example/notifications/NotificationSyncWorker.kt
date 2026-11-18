package com.example.notifications

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.firebase.FirestoreService
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

class NotificationSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val uid = Firebase.auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            return Result.success()
        }
        val appContext = applicationContext
        if (!LoopNotificationManager.canPostNotifications(appContext)) {
            return Result.success()
        }

        return try {
            val service = FirestoreService.getInstance(appContext)
            val profile = service.getUserProfileOnce(uid).getOrNull()
                ?: return Result.success()

            if (!profile.settings.notifications.pushEnabled) {
                return Result.success()
            }

            val conversations = service.fetchConversationsOnce(uid).getOrNull().orEmpty()
            val bookings = service.fetchBookingsOnce(uid).getOrNull().orEmpty()
            val communityPosts = service.fetchCommunityPostsOnce().getOrNull().orEmpty()
            val ownedItems = service.fetchOwnedItemsOnce(uid).getOrNull().orEmpty()

            val (previousBaseline, alreadyNotified) =
                NotificationCheckpointStore.loadBaselineAndNotifiedKeys(appContext, uid)

            val evaluation = NotificationDeltaEvaluator.evaluateDeltas(
                currentUid = uid,
                profile = profile,
                previousBaseline = previousBaseline,
                alreadyNotifiedKeys = alreadyNotified,
                conversations = conversations,
                bookings = bookings,
                communityPosts = communityPosts,
                ownedItems = ownedItems,
                activeOpenConversationId = null,
                isAppInForeground = false
            )

            val postedKeys = mutableSetOf<String>()
            evaluation.events.forEach { event ->
                if (LoopNotificationManager.postAlert(appContext, event)) {
                    postedKeys += event.eventKey
                }
            }

            NotificationCheckpointStore.saveBaselineAndNotifiedKeys(
                context = appContext,
                uid = uid,
                baseline = evaluation.updatedBaseline,
                newlyNotifiedKeys = postedKeys
            )
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Background notification sync failed; retrying", e)
            if (runAttemptCount < 3) Result.retry() else Result.success()
        }
    }

    companion object {
        private const val TAG = "NotifSyncWorker"
        private const val UNIQUE_PERIODIC_WORK = "loop_notification_periodic_sync"
        private const val UNIQUE_IMMEDIATE_WORK = "loop_notification_immediate_sync"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<NotificationSyncWorker>(
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val oneTime = OneTimeWorkRequestBuilder<NotificationSyncWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_IMMEDIATE_WORK,
                ExistingWorkPolicy.REPLACE,
                oneTime
            )
        }

        fun cancel(context: Context) {
            val wm = WorkManager.getInstance(context.applicationContext)
            wm.cancelUniqueWork(UNIQUE_PERIODIC_WORK)
            wm.cancelUniqueWork(UNIQUE_IMMEDIATE_WORK)
        }
    }
}
