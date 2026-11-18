package com.example.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationDeepLinkTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun roundTrips_conversationBookingAndCommunityPostDeepLinks() {
        val convIntent = NotificationDeepLink.toIntent(
            context,
            NotificationDeepLink.Conversation("conv_abc")
        )
        assertEquals(
            NotificationDeepLink.Conversation("conv_abc"),
            NotificationDeepLink.fromIntent(convIntent)
        )

        val bookingIntent = NotificationDeepLink.toIntent(
            context,
            NotificationDeepLink.Booking("bk_xyz")
        )
        assertEquals(
            NotificationDeepLink.Booking("bk_xyz"),
            NotificationDeepLink.fromIntent(bookingIntent)
        )

        val postIntent = NotificationDeepLink.toIntent(
            context,
            NotificationDeepLink.CommunityPost("post_123")
        )
        assertEquals(
            NotificationDeepLink.CommunityPost("post_123"),
            NotificationDeepLink.fromIntent(postIntent)
        )

        NotificationDeepLink.clearFromIntent(convIntent)
        assertNull(NotificationDeepLink.fromIntent(convIntent))
    }

    @Test
    fun parsesFcmStringAliases_andRejectsBlankTargets() {
        val intent = android.content.Intent().apply {
            putExtra(NotificationDeepLink.EXTRA_TYPE, "payout")
            putExtra(NotificationDeepLink.EXTRA_TARGET_ID, "bk_99")
        }
        assertEquals(NotificationDeepLink.Booking("bk_99"), NotificationDeepLink.fromIntent(intent))

        val blankIntent = android.content.Intent().apply {
            putExtra(NotificationDeepLink.EXTRA_TYPE, "conversation")
            putExtra(NotificationDeepLink.EXTRA_TARGET_ID, "   ")
        }
        assertNull(NotificationDeepLink.fromIntent(blankIntent))
    }
}
