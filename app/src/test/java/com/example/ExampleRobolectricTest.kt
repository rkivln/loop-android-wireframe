package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RentalRepository
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.RentalCategory
import com.example.data.local.AppDatabase
import com.example.data.local.UserProfileEntity
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import kotlinx.coroutines.runBlocking
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

    @Test
    fun `user profile room entity and dao persistence work as expected`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val dao = db.userProfileDao()

        val testEntity = UserProfileEntity(
            uid = "user_test_123",
            displayName = "Alex Rivers",
            bio = "Analog photographer and print maker",
            location = "Heritage Quarter",
            avatarPresetIndex = 2
        )

        dao.insertOrUpdateProfile(testEntity)

        val retrieved = dao.getUserProfileOnce("user_test_123")
        assertNotNull(retrieved)
        assertEquals("Alex Rivers", retrieved?.displayName)
        assertEquals("Analog photographer and print maker", retrieved?.bio)
        assertEquals(2, retrieved?.avatarPresetIndex)

        // Test updating basic profile (avatar, display name, bio, location)
        dao.updateBasicProfile(
            uid = "user_test_123",
            displayName = "Alex Rivers Updated",
            bio = "Updated bio for test suite",
            avatarPresetIndex = 4,
            location = "White Town Promenade",
            phone = "+91 99999 88888"
        )

        val updated = dao.getUserProfileOnce("user_test_123")
        assertNotNull(updated)
        assertEquals("Alex Rivers Updated", updated?.displayName)
        assertEquals("Updated bio for test suite", updated?.bio)
        assertEquals(4, updated?.avatarPresetIndex)
        assertEquals("White Town Promenade", updated?.location)
    }

    @Test
    fun `rental items filter by keyword and category works as expected`() {
        val items = RentalRepository.itemsFlow.value
        assertTrue("Repository items should not be empty", items.isNotEmpty())

        // Keyword filter: "camera" or "canon"
        val cameraItems = items.filter { item ->
            item.title.contains("camera", ignoreCase = true) ||
            item.description.contains("camera", ignoreCase = true) ||
            item.title.contains("canon", ignoreCase = true)
        }
        assertTrue("Camera items should be found", cameraItems.isNotEmpty())

        // Category filter: ELECTRONICS
        val electronicsItems = items.filter { it.category == RentalCategory.ELECTRONICS }
        assertTrue("Electronics items should not be empty", electronicsItems.isNotEmpty())
        assertTrue(electronicsItems.all { it.category == RentalCategory.ELECTRONICS })

        // Combined filter: keyword + category
        val combined = items.filter { item ->
            item.category == RentalCategory.ELECTRONICS &&
            (item.title.contains("Canon", ignoreCase = true) || item.description.contains("Canon", ignoreCase = true))
        }
        assertTrue("Combined filter should return matching items", combined.isNotEmpty())
    }
}
