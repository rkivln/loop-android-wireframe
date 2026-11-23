package com.example.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.MainActivity
import com.example.R
import com.example.data.models.AppearanceMode
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.data.models.SpecFeature
import com.example.data.models.UserProfile
import com.example.ui.auth.WelcomeScreen
import com.example.ui.components.BottomNavBar
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.LoopTab
import com.example.ui.components.UserAvatar
import com.example.ui.screens.BookingScreen
import com.example.ui.screens.CreateListingScreen
import com.example.ui.screens.DiscoveryMapScreen
import com.example.ui.screens.EditProfileModalSheet
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.ItemDetailsScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LoopTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoopUiCrashAndRegressionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun ensureFirebaseInitialized() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("1:123456789012:android:abcdef1234567890")
                    .setProjectId("loop-test-project")
                    .setApiKey("AIzaSyTestKeyForRobolectricUiTests000000")
                    .build()
            )
        }
    }

    private fun sampleRentalItem(): RentalItem {
        return RentalItem(
            id = "test_item_1",
            title = "Fujifilm X-T5 Mirrorless Kit",
            category = RentalCategory.ELECTRONICS,
            pricePerDay = 950,
            location = "White Town, Puducherry",
            rating = 4.9f,
            reviewCount = 14,
            description = "Compact 40MP APS-C camera kit with 23mm f/2 lens and dual batteries.",
            primaryImageRes = R.drawable.img_welcome_hero,
            imageCount = 1,
            features = listOf(
                SpecFeature("CAMERA", "40MP Sensor"),
                SpecFeature("LENS", "23mm f/2")
            ),
            owner = RentalOwner(
                id = "owner_1",
                name = "Ananya Rao",
                badge = "Verified Host",
                initials = "AR",
                memberSince = "2024",
                rating = 4.9f,
                totalRentals = 22,
                responseTime = "15 mins",
                avatarPresetIndex = 1
            ),
            isPopular = true,
            availableToday = true,
            securityDeposit = 2500,
            latitude = 11.9341,
            longitude = 79.8306
        )
    }

    @Test
    fun mainActivity_coldLaunch_rendersWelcomeWithoutCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
                assertFalse(activity.isDestroyed)
            }
        }
    }

    @Test
    @Config(qualifiers = "night")
    fun loopTheme_defaultsToLightEvenUnderNightSystemConfiguration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val nightFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        assertTrue(nightFlags == Configuration.UI_MODE_NIGHT_YES)

        var resolvedIsDark: Boolean? = null
        var activeMode by mutableStateOf(AppearanceMode.fromNameOrDefault(null))

        composeRule.setContent {
            LoopTheme(appearanceMode = activeMode) {
                resolvedIsDark = LoopTheme.colors.isDark
            }
        }
        composeRule.waitForIdle()
        assertFalse("Default theme must remain light even when system is in night mode", resolvedIsDark == true)

        activeMode = AppearanceMode.DARK
        composeRule.waitForIdle()
        assertTrue("Switching explicitly to DARK must enable dark palette", resolvedIsDark == true)
    }

    @Test
    fun welcomeScreen_advancesThroughAllSlidesAndCompletesCleanly() {
        var started = false
        composeRule.setContent {
            LoopTheme {
                WelcomeScreen(onGetStarted = { started = true })
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("ISSUE 01 — OBJECTS OF INTENT").assertIsDisplayed()
        composeRule.onNodeWithTag("welcome_skip_button").assertIsDisplayed()

        // Advance to slide 2
        composeRule.onNodeWithTag("welcome_get_started_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ISSUE 02 — URBAN MOBILITY").assertIsDisplayed()

        // Advance to slide 3
        composeRule.onNodeWithTag("welcome_get_started_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ISSUE 03 — TRUSTED MAKERS").assertIsDisplayed()
        composeRule.onNodeWithTag("welcome_sign_in_link").assertIsDisplayed()

        // Complete welcome flow
        composeRule.onNodeWithTag("welcome_get_started_button").performClick()
        composeRule.waitForIdle()
        assertTrue(started)
    }

    @Test
    fun discoveryMapScreen_layerSwitcher_showsOnlyWorkingLayersAndSwitchesWithoutCrashing() {
        var navigatedBack = false
        composeRule.setContent {
            LoopTheme {
                DiscoveryMapScreen(
                    onBackToCatalog = { navigatedBack = true },
                    onItemClick = {},
                    onContactOwner = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("map_layer_switcher_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        // Working layers must exist; broken Google Maps layers must be absent
        composeRule.onNodeWithTag("map_layer_menu").assertIsDisplayed()
        composeRule.onNodeWithTag("map_layer_option_STREET_VECTOR").assertIsDisplayed()
        composeRule.onNodeWithTag("map_layer_option_OSM_CLASSIC").assertIsDisplayed()
        composeRule.onNodeWithTag("map_layer_option_ARCHITECTURAL_GRID").assertIsDisplayed()
        composeRule.onNodeWithTag("map_layer_option_GOOGLE_STANDARD").assertDoesNotExist()
        composeRule.onNodeWithTag("map_layer_option_GOOGLE_SATELLITE").assertDoesNotExist()

        // Switch to Grid Blueprint
        composeRule.onNodeWithTag("map_layer_option_ARCHITECTURAL_GRID").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("map_layer_menu").assertDoesNotExist()

        // Re-open switcher and switch to OSM Classic
        composeRule.onNodeWithTag("map_layer_switcher_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("map_layer_menu").assertIsDisplayed()
        composeRule.onNodeWithTag("map_layer_option_OSM_CLASSIC").performClick()
        composeRule.waitForIdle()

        // Re-open switcher and switch back to Street Vector
        composeRule.onNodeWithTag("map_layer_switcher_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("map_layer_menu").assertIsDisplayed()
        composeRule.onNodeWithTag("map_layer_option_STREET_VECTOR").performClick()
        composeRule.waitForIdle()

        // Exercise category filter & back button
        composeRule.onNodeWithTag("map_filter_chip_MAKERS").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("map_back_to_catalog_btn").performClick()
        composeRule.waitForIdle()
        assertTrue(navigatedBack)
    }

    @Test
    fun editorialTopBar_renders48dpBackIconButtonAndInvokesCallback() {
        var backPressed = false
        composeRule.setContent {
            LoopTheme {
                EditorialTopBar(
                    title = "Settings",
                    onBack = { backPressed = true }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithText("←").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeRule.onNodeWithTag("top_bar_back_btn")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeRule.waitForIdle()
        assertTrue(backPressed)
    }

    @Test
    fun userAvatar_rendersEmailPhotoAndInitialsFallbackCleanly() {
        composeRule.setContent {
            LoopTheme {
                Column {
                    UserAvatar(
                        avatarUrl = "https://lh3.googleusercontent.com/a/test-avatar",
                        initials = "AM",
                        contentDescription = "Email avatar"
                    )
                    UserAvatar(
                        avatarUrl = null,
                        initials = "AM",
                        avatarPresetIndex = 2,
                        contentDescription = "Preset avatar"
                    )
                }
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("AM").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Email avatar").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Preset avatar").assertIsDisplayed()
    }

    @Test
    fun coreScreens_smokeRenderWithoutComposeRuntimeExceptions() {
        val sampleItem = sampleRentalItem()
        var currentScreenIndex by mutableStateOf(0)

        composeRule.setContent {
            LoopTheme {
                when (currentScreenIndex) {
                    0 -> ExploreScreen()
                    1 -> ItemDetailsScreen(item = sampleItem, onNavigateBack = {})
                    2 -> BookingScreen(item = sampleItem, onBack = {}, onBookingConfirmed = {})
                    3 -> MessagesScreen()
                    4 -> ProfileScreen()
                    5 -> EditProfileScreen(onNavigateBack = {})
                    6 -> SettingsScreen(
                        onBack = {},
                        onOpenItem = {},
                        onCreateListing = {},
                        onSignOut = {}
                    )
                    7 -> CreateListingScreen(onBack = {})
                }
            }
        }

        for (screenIndex in 0..7) {
            currentScreenIndex = screenIndex
            composeRule.waitForIdle()
        }
    }

    @Test
    fun liquidGlassBottomNavBar_rendersTabsAndSwitchesSmoothlyInLightAndDarkTheme() {
        var activeTab by mutableStateOf(LoopTab.CATALOG)
        var appearance by mutableStateOf(AppearanceMode.LIGHT)
        var publishTriggered = false

        composeRule.setContent {
            LoopTheme(appearanceMode = appearance) {
                BottomNavBar(
                    currentTab = activeTab,
                    unreadCount = 3,
                    onTabSelected = { tab ->
                        if (tab == LoopTab.PUBLISH) {
                            publishTriggered = true
                        } else {
                            activeTab = tab
                        }
                    }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("bottom_nav_bar").assertIsDisplayed()
        LoopTab.entries.forEach { tab ->
            composeRule.onNodeWithTag(tab.testTag)
                .assertIsDisplayed()
                .assertHeightIsAtLeast(48.dp)
        }

        // Switch tabs via tap (triggers spring jelly capsule transition)
        composeRule.onNodeWithTag(LoopTab.PROFILE.testTag).performClick()
        composeRule.waitForIdle()
        assertTrue(activeTab == LoopTab.PROFILE)

        composeRule.onNodeWithTag(LoopTab.OBJECTS.testTag).performClick()
        composeRule.waitForIdle()
        assertTrue(activeTab == LoopTab.OBJECTS)

        composeRule.onNodeWithTag(LoopTab.PUBLISH.testTag).performClick()
        composeRule.waitForIdle()
        assertTrue(publishTriggered)

        // Verify Dark mode rendering
        appearance = AppearanceMode.DARK
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(LoopTab.STUDIO.testTag).performClick()
        composeRule.waitForIdle()
        assertTrue(activeTab == LoopTab.STUDIO)
    }

    @Test
    fun editProfileModalSheet_saveButtonClearsFloatingBottomNavBarAndTriggersSave() {
        var savedProfile: UserProfile? = null
        val initialProfile = UserProfile(
            uid = "user_1",
            displayName = "GOKULAN",
            bio = "Creator & Developer - LOOP",
            location = "Pangur"
        )

        composeRule.setContent {
            LoopTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    EditProfileModalSheet(
                        currentProfile = initialProfile,
                        bottomBarPadding = 86.dp,
                        onSave = { savedProfile = it },
                        onDismiss = {}
                    )
                    BottomNavBar(
                        currentTab = LoopTab.PROFILE,
                        onTabSelected = {},
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        composeRule.waitForIdle()
        val saveBounds = composeRule.onNodeWithTag("save_profile_button")
            .assertIsDisplayed()
            .getBoundsInRoot()
        val navBounds = composeRule.onNodeWithTag("bottom_nav_bar")
            .assertIsDisplayed()
            .getBoundsInRoot()

        assertTrue(
            "Save button bottom (${saveBounds.bottom}) must sit above bottom nav top (${navBounds.top})",
            saveBounds.bottom <= navBounds.top
        )

        composeRule.onNodeWithTag("save_profile_button").performClick()
        composeRule.waitForIdle()
        assertTrue(savedProfile?.displayName == "GOKULAN")
    }
}
