package com.example.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.example.R
import com.example.data.ThemePreferencesStore

object LoopNotificationManager {

    fun canPostNotifications(context: Context): Boolean {
        if (!ThemePreferencesStore.isNotificationPermissionGranted(context)) return false
        return NotificationManagerCompat.from(context.applicationContext).areNotificationsEnabled()
    }

    fun channelIdFor(category: NotificationCategory): String {
        return when (category) {
            NotificationCategory.MESSAGE -> ThemePreferencesStore.CHANNEL_MESSAGES
            NotificationCategory.BOOKING_PIN,
            NotificationCategory.REVIEW_PAYOUT -> ThemePreferencesStore.CHANNEL_BOOKINGS
            NotificationCategory.COMMUNITY_POST,
            NotificationCategory.COMMUNITY_RSVP -> ThemePreferencesStore.CHANNEL_COMMUNITY
            NotificationCategory.ANNOUNCEMENT -> ThemePreferencesStore.CHANNEL_ANNOUNCEMENTS
        }
    }

    @SuppressLint("MissingPermission")
    fun postAlert(context: Context, event: NotificationAlertEvent): Boolean {
        val appContext = context.applicationContext
        if (!canPostNotifications(appContext)) return false

        ThemePreferencesStore.ensureNotificationChannel(appContext)
        val channelId = channelIdFor(event.category)
        val notificationId = event.eventKey.hashCode()

        val intent = NotificationDeepLink.toIntent(appContext, event.deepLink)
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val contentIntent = PendingIntent.getActivity(
            appContext,
            notificationId,
            intent,
            pendingIntentFlags
        )

        val priority = when (event.category) {
            NotificationCategory.MESSAGE,
            NotificationCategory.BOOKING_PIN -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_notification_loop)
            .setContentTitle(event.title)
            .setContentText(event.body)
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        when (event.category) {
            NotificationCategory.MESSAGE -> {
                val senderPerson = Person.Builder()
                    .setName(event.senderName?.takeIf { it.isNotBlank() } ?: event.title)
                    .build()
                val mePerson = Person.Builder().setName("You").build()
                builder
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setStyle(
                        NotificationCompat.MessagingStyle(mePerson)
                            .setConversationTitle(event.title)
                            .addMessage(event.body, System.currentTimeMillis(), senderPerson)
                    )
            }
            NotificationCategory.BOOKING_PIN,
            NotificationCategory.REVIEW_PAYOUT -> {
                builder
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(event.body))
            }
            NotificationCategory.COMMUNITY_POST,
            NotificationCategory.COMMUNITY_RSVP,
            NotificationCategory.ANNOUNCEMENT -> {
                builder
                    .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(event.body))
            }
        }

        return try {
            NotificationManagerCompat.from(appContext).notify(notificationId, builder.build())
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun sendTestNotification(context: Context): Result<Unit> {
        val appContext = context.applicationContext
        if (!ThemePreferencesStore.isNotificationPermissionGranted(appContext)) {
            return Result.failure(IllegalStateException("Allow Android notification permission first."))
        }
        if (!NotificationManagerCompat.from(appContext).areNotificationsEnabled()) {
            return Result.failure(IllegalStateException("Notifications are disabled in Android system settings."))
        }
        val posted = postAlert(
            appContext,
            NotificationAlertEvent(
                eventKey = "loop_test_${System.currentTimeMillis()}",
                category = NotificationCategory.BOOKING_PIN,
                title = "Loop Push Alerts Active",
                body = "Real-time chats, booking PINs, community drops, reviews, and payouts will alert on this phone.",
                deepLink = NotificationDeepLink.Booking("test_verification")
            )
        )
        return if (posted) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Unable to post system notification."))
        }
    }
}
