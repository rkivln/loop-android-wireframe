package com.example.notifications

import android.content.Context
import android.util.Log
import com.example.data.firebase.FirestoreService
import com.example.data.models.NotificationPreferences
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoopFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM registration token")
        val appContext = applicationContext
        serviceScope.launch {
            NotificationCheckpointStore.saveFcmToken(appContext, token)
            if (Firebase.auth.currentUser != null) {
                FirestoreService.getInstance(appContext).updateFcmToken(token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val appContext = applicationContext
        val uid = Firebase.auth.currentUser?.uid ?: return

        val data = message.data
        val rawType = (data["type"] ?: data["category"] ?: "announcement").trim().lowercase()
        val targetId = (data["targetId"]
            ?: data["conversationId"]
            ?: data["bookingId"]
            ?: data["postId"]
            ?: message.messageId
            ?: System.currentTimeMillis().toString()).trim()

        val title = (data["title"] ?: message.notification?.title ?: "Loop Update").trim()
        val body = (data["body"] ?: message.notification?.body ?: "Tap to view details.").trim()
        val senderName = data["senderName"]?.trim()?.takeIf { it.isNotBlank() }
        val senderId = data["senderId"]?.trim().orEmpty()
        val messageId = (data["messageId"] ?: message.messageId ?: "${rawType}_${targetId}_${body.hashCode()}").trim()

        if (senderId.isNotBlank() && senderId == uid) {
            return
        }

        val (category, deepLink) = when (rawType) {
            "message", "chat", "conversation" ->
                NotificationCategory.MESSAGE to NotificationDeepLink.Conversation(targetId)
            "booking", "reservation", "pin", "handover" ->
                NotificationCategory.BOOKING_PIN to NotificationDeepLink.Booking(targetId)
            "review", "payout", "earning" ->
                NotificationCategory.REVIEW_PAYOUT to NotificationDeepLink.Booking(targetId)
            "community", "post", "community_post" ->
                NotificationCategory.COMMUNITY_POST to NotificationDeepLink.CommunityPost(targetId)
            "rsvp", "interaction" ->
                NotificationCategory.COMMUNITY_RSVP to NotificationDeepLink.CommunityPost(targetId)
            else ->
                NotificationCategory.ANNOUNCEMENT to NotificationDeepLink.CommunityPost(targetId)
        }

        serviceScope.launch {
            val profile = FirestoreService.getInstance(appContext).getUserProfileOnce(uid).getOrNull()
            val settings = profile?.settings
            if (settings != null) {
                if (!settings.notifications.pushEnabled) return@launch
                if (senderId.isNotBlank() && senderId in settings.blockedUserIds) return@launch
                val allowedByCategory = when (category) {
                    NotificationCategory.COMMUNITY_POST -> settings.notifications.newNeighbourhoodPosts
                    NotificationCategory.COMMUNITY_RSVP -> settings.notifications.likesAndInteractions
                    NotificationCategory.ANNOUNCEMENT -> settings.notifications.announcements
                    NotificationCategory.MESSAGE,
                    NotificationCategory.BOOKING_PIN,
                    NotificationCategory.REVIEW_PAYOUT -> true
                }
                if (!allowedByCategory) return@launch
            }

            val isUnseen = NotificationCheckpointStore.recordNotifiedKeyIfUnseen(
                context = appContext,
                uid = uid,
                key = "fcm:$messageId"
            )
            if (!isUnseen) return@launch

            val hideMessagePreview = category == NotificationCategory.MESSAGE &&
                settings?.chatPrivacy?.hideNotificationPreviews == true

            LoopNotificationManager.postAlert(
                context = appContext,
                event = NotificationAlertEvent(
                    eventKey = "fcm:$messageId",
                    category = category,
                    title = if (hideMessagePreview) "Studio Dispatch" else title,
                    body = if (hideMessagePreview) "New encrypted message · Tap to open" else body,
                    senderName = if (hideMessagePreview) null else senderName,
                    hidePreview = hideMessagePreview,
                    deepLink = deepLink
                )
            )
        }
    }

    companion object {
        private const val TAG = "LoopFcmService"
    }
}

object FcmTokenSyncHelper {
    private const val TAG = "FcmTokenSyncHelper"
    private const val TOPIC_COMMUNITY = "loop_community"
    private const val TOPIC_ANNOUNCEMENTS = "loop_announcements"

    suspend fun syncTokenAndTopics(
        context: Context,
        uid: String,
        preferences: NotificationPreferences? = null
    ) {
        if (uid.isBlank()) return
        val appContext = context.applicationContext
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            if (token.isNotBlank()) {
                NotificationCheckpointStore.saveFcmToken(appContext, token)
                FirestoreService.getInstance(appContext).updateFcmToken(token)
            }
        } catch (e: Exception) {
            Log.w(TAG, "FCM token retrieval skipped: ${e.message}")
        }

        if (preferences != null) {
            syncTopics(preferences)
        }
    }

    suspend fun syncTopics(preferences: NotificationPreferences) {
        try {
            val messaging = FirebaseMessaging.getInstance()
            if (preferences.pushEnabled && preferences.newNeighbourhoodPosts) {
                messaging.subscribeToTopic(TOPIC_COMMUNITY).await()
            } else {
                messaging.unsubscribeFromTopic(TOPIC_COMMUNITY).await()
            }
            if (preferences.pushEnabled && preferences.announcements) {
                messaging.subscribeToTopic(TOPIC_ANNOUNCEMENTS).await()
            } else {
                messaging.unsubscribeFromTopic(TOPIC_ANNOUNCEMENTS).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "FCM topic sync skipped: ${e.message}")
        }
    }
}
