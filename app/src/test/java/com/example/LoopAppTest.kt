package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.local.AppDatabase
import com.example.data.local.UserProfileDao
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.models.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class LoopAppTest {

    private lateinit var database: AppDatabase
    private lateinit var userProfileDao: UserProfileDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userProfileDao = database.userProfileDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveUserProfileInRoom() = runBlocking {
        val profile = UserProfile(
            uid = "test_user_1",
            displayName = "Jaya Nandhini G",
            email = "jayanandhini@loop.in",
            phone = "+91 9988776655",
            bio = "Crafting ceramics & sharing wheel tools.",
            location = "Auroville, Puducherry",
            avatarPresetIndex = 0
        )

        userProfileDao.insertOrUpdateProfile(profile.toEntity())

        val retrievedEntity = userProfileDao.getProfile("test_user_1")
        assertNotNull(retrievedEntity)
        assertEquals("Jaya Nandhini G", retrievedEntity?.displayName)
        assertEquals("jayanandhini@loop.in", retrievedEntity?.email)
        assertEquals("Auroville, Puducherry", retrievedEntity?.location)

        val domain = retrievedEntity?.toDomain()
        assertEquals(profile.displayName, domain?.displayName)
        assertEquals(profile.bio, domain?.bio)
    }

    @Test
    fun testUpdateUserProfileInRoom() = runBlocking {
        val initial = UserProfile(
            uid = "test_user_update",
            displayName = "Jaya Nandhini G",
            bio = "Original Bio"
        )
        userProfileDao.insertOrUpdateProfile(initial.toEntity())

        val updated = initial.copy(
            displayName = "Jaya Nandhini G (Updated)",
            bio = "Updated Bio"
        )
        userProfileDao.insertOrUpdateProfile(updated.toEntity())

        val result = userProfileDao.getProfile("test_user_update")
        assertEquals("Jaya Nandhini G (Updated)", result?.displayName)
        assertEquals("Updated Bio", result?.bio)
    }

    @Test
    fun testUserProfileFlowEmitsUpdates() = runBlocking {
        val profile = UserProfile(
            uid = "flow_user",
            displayName = "Jaya Nandhini G",
            bio = "Testing Reactive Flow"
        )
        userProfileDao.insertOrUpdateProfile(profile.toEntity())

        val flowResult = userProfileDao.getProfileFlow("flow_user").first()
        assertNotNull(flowResult)
        assertEquals("Jaya Nandhini G", flowResult?.displayName)
    }
}
