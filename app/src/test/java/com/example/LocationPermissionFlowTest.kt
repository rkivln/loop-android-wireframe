package com.example

import android.content.Context
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.location.LocationAccessState
import com.example.location.LocationPermissionManager
import com.example.ui.components.LocationPermissionPromptDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class LocationPermissionFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLocationPermissionManagerInitialState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = LocationPermissionManager(context)

        val status = manager.checkCurrentPermissionStatus()
        assertEquals(LocationAccessState.PromptNeeded, status)
    }

    @Test
    fun testLocationPermissionManagerHandlesGrantedResult() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = LocationPermissionManager(context)

        manager.onPermissionsResult(
            mapOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION to true,
                android.Manifest.permission.ACCESS_COARSE_LOCATION to true
            )
        )

        assertEquals(LocationAccessState.GrantedFine, manager.accessState.value)
    }

    @Test
    fun testLocationPermissionManagerHandlesDeniedResult() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = LocationPermissionManager(context)

        manager.onPermissionsResult(
            mapOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION to false,
                android.Manifest.permission.ACCESS_COARSE_LOCATION to false
            )
        )

        assertEquals(LocationAccessState.Denied, manager.accessState.value)
    }

    @Test
    fun testLocationPermissionPromptDialogDisplaysAndHandlesClicks() {
        var requested = false
        var dismissed = false

        composeTestRule.setContent {
            LocationPermissionPromptDialog(
                onRequestPermission = { requested = true },
                onDismiss = { dismissed = true }
            )
        }

        composeTestRule.onNodeWithTag("location_permission_dialog").assertExists()
        composeTestRule.onNodeWithTag("location_dialog_title").assertExists()
            .assertTextContains("Enable Fine Location Access")
        composeTestRule.onNodeWithTag("enable_fine_location_button").performScrollTo().performClick()
        assertTrue(requested)

        composeTestRule.onNodeWithTag("dismiss_location_permission_button").performScrollTo().performClick()
        assertTrue(dismissed)
    }
}
