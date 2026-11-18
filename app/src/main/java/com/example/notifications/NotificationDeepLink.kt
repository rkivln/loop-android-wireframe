package com.example.notifications

import android.content.Context
import android.content.Intent
import com.example.MainActivity

sealed class NotificationDeepLink {
    data class Conversation(val conversationId: String) : NotificationDeepLink()
    data class Booking(val bookingId: String) : NotificationDeepLink()
    data class CommunityPost(val postId: String) : NotificationDeepLink()

    companion object {
        const val EXTRA_TYPE = "loop_notif_type"
        const val EXTRA_TARGET_ID = "loop_notif_target_id"

        const val TYPE_CONVERSATION = "conversation"
        const val TYPE_BOOKING = "booking"
        const val TYPE_COMMUNITY_POST = "community_post"

        fun toIntent(context: Context, deepLink: NotificationDeepLink): Intent {
            val (type, targetId) = when (deepLink) {
                is Conversation -> TYPE_CONVERSATION to deepLink.conversationId
                is Booking -> TYPE_BOOKING to deepLink.bookingId
                is CommunityPost -> TYPE_COMMUNITY_POST to deepLink.postId
            }
            return Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TYPE, type)
                putExtra(EXTRA_TARGET_ID, targetId)
            }
        }

        fun fromIntent(intent: Intent?): NotificationDeepLink? {
            if (intent == null) return null
            val type = intent.getStringExtra(EXTRA_TYPE)?.trim()?.lowercase().orEmpty()
            val targetId = intent.getStringExtra(EXTRA_TARGET_ID)?.trim().orEmpty()
            if (type.isBlank() || targetId.isBlank()) return null
            return when (type) {
                TYPE_CONVERSATION, "chat", "message" -> Conversation(targetId)
                TYPE_BOOKING, "reservation", "pin", "payout", "review" -> Booking(targetId)
                TYPE_COMMUNITY_POST, "community", "event", "rsvp" -> CommunityPost(targetId)
                else -> null
            }
        }

        fun clearFromIntent(intent: Intent?) {
            intent?.removeExtra(EXTRA_TYPE)
            intent?.removeExtra(EXTRA_TARGET_ID)
        }
    }
}
