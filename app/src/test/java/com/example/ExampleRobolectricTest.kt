package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RentalRepository
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.RentalCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertNotNull(appName)
    }

    @Test
    fun `user profile operations work as expected`() {
        val initialProfile = RentalRepository.userProfileFlow.value
        assertEquals("Gokulan R", initialProfile.displayName)
        assertTrue(initialProfile.isVerified)

        val updatedBio = "Updated bio for community testing"
        RentalRepository.updateUserProfile(initialProfile.copy(bio = updatedBio))
        assertEquals(updatedBio, RentalRepository.userProfileFlow.value.bio)
    }

    @Test
    fun `community posts creation and status toggling work as expected`() {
        val initialCount = RentalRepository.userCommunityPostsFlow.value.size

        RentalRepository.createCommunityPost(
            title = "Test Cleanup Event",
            type = CommunityPostType.EVENT,
            description = "Community beach cleaning drive",
            location = "Rock Beach, Pondicherry",
            dateTime = "Saturday, 7:00 AM",
            latitude = 11.9338,
            longitude = 79.8350,
            category = RentalCategory.EVENTS
        )

        val currentPosts = RentalRepository.userCommunityPostsFlow.value
        assertEquals(initialCount + 1, currentPosts.size)
        val createdPost = currentPosts.first()
        assertEquals("Test Cleanup Event", createdPost.title)
        assertEquals(CommunityPostStatus.ACTIVE, createdPost.status)

        // Toggle post status
        RentalRepository.toggleCommunityPostStatus(createdPost.id)
        val updatedPost = RentalRepository.userCommunityPostsFlow.value.first { it.id == createdPost.id }
        assertEquals(CommunityPostStatus.COMPLETED, updatedPost.status)
    }

    @Test
    fun `user current rentals operations work as expected`() {
        val currentRentals = RentalRepository.userCurrentRentalsFlow.value
        assertTrue("Rentals should not be empty", currentRentals.isNotEmpty())

        val targetRental = currentRentals.first()
        val initialDays = targetRental.daysRemaining

        // Extend rental by 2 days
        RentalRepository.extendRental(targetRental.id, 2)
        val extendedRental = RentalRepository.userCurrentRentalsFlow.value.first { it.id == targetRental.id }
        assertEquals(initialDays + 2, extendedRental.daysRemaining)

        // Complete rental return
        RentalRepository.completeRentalReturn(targetRental.id)
        val returnedRental = RentalRepository.userCurrentRentalsFlow.value.first { it.id == targetRental.id }
        assertTrue(returnedRental.isCompleted)
    }
}
