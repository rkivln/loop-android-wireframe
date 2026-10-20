package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.models.CommunityEvent
import com.example.ui.components.EventMapMarkerCallout
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class EventMapMarkerCalloutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysRequiredEventDetails() {
        val testEvent = CommunityEvent(
            id = "test_event_1",
            title = "Promenade Beach Eco Cleanup",
            description = "Community volunteer morning cleanup with supplies provided.",
            category = "CLEANUP",
            locationName = "Rock Beach, Puducherry",
            latitude = 11.9330,
            longitude = 79.8365,
            timeString = "Sunday, 6:30 AM",
            organizerName = "Clean Pondy",
            participantCount = 35
        )

        composeTestRule.setContent {
            EventMapMarkerCallout(event = testEvent)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("event_marker_callout").assertIsDisplayed()
        composeTestRule.onNodeWithTag("event_marker_title").assertIsDisplayed().assertTextContains("Promenade Beach Eco Cleanup")
        composeTestRule.onNodeWithTag("event_marker_category").assertIsDisplayed().assertTextContains("CLEANUP")
        composeTestRule.onNodeWithTag("event_marker_time").assertIsDisplayed().assertTextContains("Sunday, 6:30 AM")
        composeTestRule.onNodeWithTag("event_marker_location").assertIsDisplayed().assertTextContains("Rock Beach, Puducherry")
        composeTestRule.onNodeWithTag("event_marker_join_button").assertIsDisplayed()
    }

    @Test
    fun displaysHelpNeededCategoryCorrectly() {
        val helpEvent = CommunityEvent(
            id = "help_event",
            title = "Urgent Sapling Planting",
            description = "Need hands to plant 50 mangrove saplings before the rain.",
            category = "HELP NEEDED",
            locationName = "Botanical Garden",
            latitude = 11.9280,
            longitude = 79.8210,
            timeString = "Saturday, 7:00 AM",
            organizerName = "Green Trust",
            participantCount = 8
        )

        composeTestRule.setContent {
            EventMapMarkerCallout(event = helpEvent)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("event_marker_category").assertIsDisplayed().assertTextContains("HELP NEEDED")
    }
}
