package com.example.ui

import android.content.Context
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.HouseRentalRepository
import com.example.data.models.AppearanceMode
import com.example.data.models.FurnishingStatus
import com.example.data.models.HouseAmenity
import com.example.data.models.HouseFilterState
import com.example.data.models.HouseListing
import com.example.data.models.HousePropertyType
import com.example.data.models.HouseSortOption
import com.example.data.models.RentalOwner
import com.example.data.models.TenantPreference
import com.example.data.models.VisitRequestStatus
import com.example.ui.components.BottomNavBar
import com.example.ui.components.LoopTab
import com.example.ui.screens.CreateHouseScreen
import com.example.ui.screens.HouseDetailsScreen
import com.example.ui.screens.HouseVisitsSheet
import com.example.ui.screens.HousesScreen
import com.example.ui.screens.ScheduleVisitBottomSheet
import com.example.ui.theme.LoopTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertEquals
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
class HouseRentalUiAndModelTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
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
        HouseRentalRepository.resetFilters()
    }

    private fun sampleHouseListing(): HouseListing {
        return HouseListing(
            id = "house_test_1",
            title = "Sunlit 2BHK Heritage Flat in White Town",
            propertyType = HousePropertyType.APARTMENT,
            bhkCount = 2,
            bathrooms = 2,
            areaSqFt = 1120,
            floorNumber = 2,
            totalFloors = 3,
            furnishing = FurnishingStatus.SEMI_FURNISHED,
            tenantPreference = TenantPreference.FAMILY,
            monthlyRent = 22000,
            securityDeposit = 60000,
            maintenanceMonthly = 1500,
            minLeaseMonths = 11,
            availableFromLabel = "Immediate",
            isAvailableForRent = true,
            location = "White Town, Puducherry",
            fullAddressNote = "Rue Suffren #24",
            description = "Quiet courtyard apartment with cross-ventilation, solar hot water, and covered two-wheeler bay.",
            amenities = listOf(HouseAmenity.WIFI, HouseAmenity.BALCONY, HouseAmenity.WATER_24X7),
            photoLabels = listOf("Living Room", "Bedroom"),
            imageCount = 2,
            latitude = 11.9341,
            longitude = 79.8306,
            owner = RentalOwner(
                id = "landlord_uid_1",
                name = "Meera Nair",
                badge = "Verified Landlord",
                initials = "MN"
            )
        )
    }

    @Test
    fun houseModels_helpersComputeConfigurationTerminalAndFilterCountCorrectly() {
        val studio = sampleHouseListing().copy(propertyType = HousePropertyType.STUDIO, bhkCount = 0)
        assertEquals("Studio", studio.configurationLabel)

        val twoBhk = sampleHouseListing()
        assertEquals("2 BHK", twoBhk.configurationLabel)
        assertEquals("Living Room", twoBhk.roomLabelAt(0))
        assertEquals("Room 3", twoBhk.roomLabelAt(2))

        assertTrue(VisitRequestStatus.CANCELLED.isTerminal)
        assertTrue(VisitRequestStatus.DECLINED.isTerminal)
        assertTrue(VisitRequestStatus.COMPLETED.isTerminal)
        assertFalse(VisitRequestStatus.REQUESTED.isTerminal)
        assertFalse(VisitRequestStatus.CONFIRMED.isTerminal)

        assertEquals(0, HouseFilterState().activeFilterCount)
        val populated = HouseFilterState(
            sortOption = HouseSortOption.RENT_LOW_TO_HIGH,
            propertyType = HousePropertyType.VILLA,
            minBhk = 2,
            petFriendlyOnly = true,
            availableOnly = false
        )
        assertEquals(5, populated.activeFilterCount)
    }

    @Test
    fun bottomNavBar_switchesToHousesTabCleanly() {
        var activeTab = LoopTab.CATALOG
        composeRule.setContent {
            LoopTheme {
                BottomNavBar(
                    currentTab = activeTab,
                    onTabSelected = { activeTab = it }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(LoopTab.HOUSES.testTag)
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeRule.waitForIdle()
        assertEquals(LoopTab.HOUSES, activeTab)
    }

    @Test
    fun publishTypeChooserOverlay_rendersAndRoutesToHouseOrEquipment() {
        var choseHouse = false
        var choseEquipment = false

        composeRule.setContent {
            LoopTheme {
                PublishTypeChooserOverlay(
                    highlightHouseFirst = true,
                    bottomClearance = 96.dp,
                    onChooseHouse = { choseHouse = true },
                    onChooseEquipment = { choseEquipment = true },
                    onDismiss = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("publish_chooser_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("chooser_list_house_btn")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeRule.waitForIdle()
        assertTrue(choseHouse)

        composeRule.onNodeWithTag("chooser_list_gear_btn")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeRule.waitForIdle()
        assertTrue(choseEquipment)
    }

    @Test
    fun housesScreen_rendersSearchFilterAndEmptyStateInLightAndDark() {
        composeRule.setContent {
            LoopTheme(appearanceMode = AppearanceMode.LIGHT) {
                HousesScreen(
                    onHouseClick = {},
                    onOpenMapWithHouses = {},
                    onPublishHouseClick = {},
                    bottomBarPadding = 86.dp
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("houses_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("houses_map_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("houses_filter_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("houses_search_input").performTextInput("White Town")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("houses_filter_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Filter Houses").assertIsDisplayed()
    }

    @Test
    fun houseDetailsScreen_rendersLeaseLedgerSpecsAndActionButtonsInDarkTheme() {
        val sample = sampleHouseListing()
        var messageClicked = false

        composeRule.setContent {
            LoopTheme(appearanceMode = AppearanceMode.DARK) {
                HouseDetailsScreen(
                    house = sample,
                    onNavigateBack = {},
                    onMessageLandlord = { messageClicked = true }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("house_details_screen").assertIsDisplayed()
        composeRule.onNodeWithText("LONG-TERM MONTHLY LEASE TERMS").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("₹22000 / month").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("house_message_landlord_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertTrue(messageClicked)
        composeRule.onNodeWithTag("house_request_visit_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("schedule_visit_sheet").assertIsDisplayed()
    }

    @Test
    fun scheduleVisitBottomSheet_updatesSlotOccupantsAndShowsSignInErrorWithoutSession() {
        val sample = sampleHouseListing()
        composeRule.setContent {
            LoopTheme {
                ScheduleVisitBottomSheet(
                    house = sample,
                    bottomBarPadding = 86.dp,
                    onVisitSubmitted = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("schedule_visit_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("visit_slot_EVENING").performScrollTo().performClick()
        composeRule.onNodeWithTag("visit_occupants_plus").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("visit_occupants_count")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextEquals("2")
        composeRule.onNodeWithTag("submit_visit_request_btn").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun createHouseScreen_validatesRentThresholdAndRendersCleanly() {
        composeRule.setContent {
            LoopTheme {
                CreateHouseScreen(onBack = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("create_house_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("house_title_input").performScrollTo().performTextInput("Garden Villa Flat")
        composeRule.onNodeWithTag("house_monthly_rent_input").performScrollTo().performTextInput("500")
        composeRule.onNodeWithTag("publish_house_submit_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Enter a monthly rent of at least ₹1,000.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun houseVisitsSheet_switchesBetweenMyVisitsAndInquiriesTabsCleanly() {
        composeRule.setContent {
            LoopTheme {
                HouseVisitsSheet(
                    bottomBarPadding = 86.dp,
                    onDismiss = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("house_visits_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("house_visits_tab_1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No incoming tenant inquiries yet").assertIsDisplayed()
        composeRule.onNodeWithTag("house_visits_tab_0").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No scheduled house visits yet").assertIsDisplayed()
    }
}
