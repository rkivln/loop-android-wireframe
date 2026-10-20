package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.firebase.FirestoreService
import com.example.data.local.AppDatabase
import com.example.data.local.CommunityEventDao
import com.example.data.local.toEntity
import com.example.data.local.toUserCommunityPost
import com.example.data.models.CommunityPostStatus
import com.example.data.models.CommunityPostType
import com.example.data.models.RentalCategory
import com.example.data.models.UserCommunityPost
import com.example.data.repository.CommunityEventRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CommunityEventRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var eventDao: CommunityEventDao
    private lateinit var repository: CommunityEventRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        eventDao = database.communityEventDao()
        repository = CommunityEventRepository(
            eventDao = eventDao,
            firestoreService = FirestoreService.instance
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testRoomDaoInsertAndRetrieve() = runBlocking {
        val testEvent = UserCommunityPost(
            id = "test_event_1",
            title = "Auroville Organic Farming Workshop",
            type = CommunityPostType.EVENT,
            description = "Hands-on permaculture workshop in Auroville community farms.",
            location = "Auroville Green Belt",
            dateTime = "Sunday · 9:00 AM",
            attendeesOrResponses = "5 creators attending",
            status = CommunityPostStatus.ACTIVE,
            latitude = 12.0068,
            longitude = 79.8105,
            category = RentalCategory.EVENTS
        )

        eventDao.insertEvent(testEvent.toEntity(isSynced = true))

        val retrievedEntity = eventDao.getEventByIdOnce("test_event_1")
        assertNotNull(retrievedEntity)
        assertEquals("Auroville Organic Farming Workshop", retrievedEntity?.title)
        assertEquals("EVENT", retrievedEntity?.type)

        val domainModel = retrievedEntity?.toUserCommunityPost()
        assertNotNull(domainModel)
        assertEquals(testEvent.id, domainModel?.id)
        assertEquals(CommunityPostType.EVENT, domainModel?.type)
        assertEquals(CommunityPostStatus.ACTIVE, domainModel?.status)
    }

    @Test
    fun testRepositoryCreateAndGetAllEvents() = runBlocking {
        val newEvent = UserCommunityPost(
            id = "repo_event_42",
            title = "French Quarter Architecture Photowalk",
            type = CommunityPostType.EVENT,
            description = "Exploring colonial courtyards and mustard facades.",
            location = "Dumas Street, White Town",
            dateTime = "Tomorrow · 6:30 AM",
            attendeesOrResponses = "12 creators attending",
            status = CommunityPostStatus.ACTIVE,
            category = RentalCategory.EVENTS
        )

        val result = repository.createEvent(newEvent)
        assertTrue(result.isSuccess)

        val allEvents = repository.allEvents.first()
        assertTrue(allEvents.any { it.id == "repo_event_42" })
        val fetched = allEvents.first { it.id == "repo_event_42" }
        assertEquals("French Quarter Architecture Photowalk", fetched.title)
    }

    @Test
    fun testRepositoryUpdateEventStatus() = runBlocking {
        val event = UserCommunityPost(
            id = "status_event_1",
            title = "Camera Sensor Cleaning Help",
            type = CommunityPostType.HELP_REQUEST,
            description = "Need swab and sensor solution for full frame mirrorless.",
            location = "Romain Rolland St",
            dateTime = "Today",
            attendeesOrResponses = "2 responses received",
            status = CommunityPostStatus.ACTIVE,
            category = RentalCategory.ELECTRONICS
        )

        repository.createEvent(event)
        val updateResult = repository.updateEventStatus("status_event_1", CommunityPostStatus.COMPLETED)
        assertTrue(updateResult.isSuccess)

        val updated = repository.getEventByIdOnce("status_event_1")
        assertEquals(CommunityPostStatus.COMPLETED, updated?.status)
    }

    @Test
    fun testRepositoryRsvpToEvent() = runBlocking {
        val event = UserCommunityPost(
            id = "rsvp_event_1",
            title = "Alliance Francaise Reading Club",
            type = CommunityPostType.STUDY_GROUP,
            description = "Weekly French poetry and photography monograph review.",
            location = "Alliance Courtyard",
            dateTime = "Friday · 4:00 PM",
            attendeesOrResponses = "4 desks booked",
            status = CommunityPostStatus.ACTIVE,
            category = RentalCategory.STUDY_OFFICE
        )

        repository.createEvent(event)
        val rsvpResult = repository.rsvpToEvent("rsvp_event_1")
        assertTrue(rsvpResult.isSuccess)

        val updated = repository.getEventByIdOnce("rsvp_event_1")
        assertNotNull(updated)
        assertTrue(updated!!.attendeesOrResponses.contains("5 desks booked"))
    }

    @Test
    fun testRepositoryDeleteEvent() = runBlocking {
        val event = UserCommunityPost(
            id = "delete_me_1",
            title = "Temporary Meetup",
            type = CommunityPostType.EVENT,
            description = "Quick afternoon catchup",
            location = "Promenade Beach",
            dateTime = "Today",
            attendeesOrResponses = "1 creator registered",
            status = CommunityPostStatus.ACTIVE,
            category = RentalCategory.EVENTS
        )

        repository.createEvent(event)
        assertNotNull(repository.getEventByIdOnce("delete_me_1"))

        val deleteResult = repository.deleteEvent("delete_me_1")
        assertTrue(deleteResult.isSuccess)
        assertNull(repository.getEventByIdOnce("delete_me_1"))
    }
}
