package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType
import com.example.data.models.MapCategoryFilter
import com.example.data.models.RentalCategory
import com.example.data.models.UserCommunityPost
import com.example.ui.components.EventMapMarkerCallout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EventMapMarkerCalloutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testEventMapMarkerCallout_displaysRequiredEventDetails() {
        var directionsClicked = false
        var rsvpClicked = false
        var dismissed = false

        composeTestRule.setContent {
            EventMapMarkerCallout(
                eventName = "Promenade Golden Hour Photowalk",
                eventTime = "Today · 5:30 PM – 7:30 PM",
                category = "Local Events",
                categoryEmoji = "📸",
                locationName = "Rock Beach Promenade",
                distance = "300 m away",
                walkingTime = "4 min walk",
                attendeesInfo = "14 creators attending",
                description = "Sunset photowalk exploring colonial streets.",
                isAttending = false,
                onDirectionsClick = { directionsClicked = true },
                onRsvpClick = { rsvpClicked = true },
                onDismiss = { dismissed = true }
            )
        }

        // Verify Event Name
        composeTestRule.onNodeWithTag("event_marker_name")
            .assertIsDisplayed()
            .assertTextContains("Promenade Golden Hour Photowalk")

        // Verify Event Time
        composeTestRule.onNodeWithTag("event_marker_time")
            .assertIsDisplayed()
            .assertTextContains("Today · 5:30 PM – 7:30 PM")

        // Verify Category
        composeTestRule.onNodeWithTag("event_marker_category")
            .assertIsDisplayed()
            .assertTextContains("LOCAL EVENTS")

        // Test Directions Action
        composeTestRule.onNodeWithTag("event_marker_directions_btn")
            .assertIsDisplayed()
            .performClick()
        assertTrue("Directions click callback should be triggered", directionsClicked)

        // Test RSVP Action
        composeTestRule.onNodeWithTag("event_marker_rsvp_btn")
            .assertIsDisplayed()
            .performClick()
        assertTrue("RSVP click callback should be triggered", rsvpClicked)

        // Test Dismiss Action
        composeTestRule.onNodeWithTag("event_marker_close_btn")
            .assertIsDisplayed()
            .performClick()
        assertTrue("Dismiss callback should be triggered", dismissed)
    }

    @Test
    fun testEventMapMarkerCallout_pinOverload() {
        val pin = DiscoveryPinItem(
            id = "pin_photowalk",
            title = "Sunset Seafront Photowalk",
            subtitle = "Pondy Street Collective",
            type = DiscoveryPinType.COMMUNITY_EVENT,
            latitude = 11.9338,
            longitude = 79.8350,
            rating = 4.9f,
            distance = "250 m away",
            tag = "📸 Photo Walk",
            dateOrAvailability = "Saturday · 5:00 PM",
            locationName = "Goubert Ave",
            priceOrAttendees = "18 creators attending",
            description = "Community photo session with analog and digital cameras.",
            isAttending = true,
            category = RentalCategory.EVENTS,
            filterCategory = MapCategoryFilter.LOCAL_EVENTS,
            neighborhood = "White Town",
            walkingTime = "3 min walk",
            cyclingTime = "1 min ride"
        )

        var rsvpCount = 0

        composeTestRule.setContent {
            EventMapMarkerCallout(
                pin = pin,
                onRsvpClick = { rsvpCount++ }
            )
        }

        composeTestRule.onNodeWithTag("event_marker_name")
            .assertIsDisplayed()
            .assertTextContains("Sunset Seafront Photowalk")

        composeTestRule.onNodeWithTag("event_marker_time")
            .assertIsDisplayed()
            .assertTextContains("Saturday · 5:00 PM")

        composeTestRule.onNodeWithTag("event_marker_category")
            .assertIsDisplayed()
            .assertTextContains("LOCAL EVENT")

        composeTestRule.onNodeWithTag("event_marker_rsvp_btn")
            .performClick()
        assertEquals(1, rsvpCount)
    }

    @Test
    fun testEventMapMarkerCallout_communityPostOverload() {
        val post = UserCommunityPost(
            id = "post_darkroom",
            title = "Darkroom & Film Developing Help",
            type = com.example.data.models.CommunityPostType.HELP_REQUEST,
            description = "Learn how to mix chemistry and load reels.",
            location = "Romain Rolland Studio",
            dateTime = "Sunday · 10:00 AM",
            attendeesOrResponses = "4 responses",
            status = com.example.data.models.CommunityPostStatus.ACTIVE,
            createdAt = "Yesterday",
            category = RentalCategory.ELECTRONICS
        )

        composeTestRule.setContent {
            EventMapMarkerCallout(
                event = post,
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithTag("event_marker_name")
            .assertIsDisplayed()
            .assertTextContains("Darkroom & Film Developing Help")

        composeTestRule.onNodeWithTag("event_marker_time")
            .assertIsDisplayed()
            .assertTextContains("Sunday · 10:00 AM")

        composeTestRule.onNodeWithTag("event_marker_category")
            .assertIsDisplayed()
            .assertTextContains("HELP NEEDED")
    }
}
