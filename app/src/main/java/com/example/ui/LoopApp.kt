package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.data.RentalRepository
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalMessage
import com.example.data.models.RentalOwner
import com.example.data.models.SpecFeature
import com.example.ui.components.LoopBottomNavigation
import com.example.ui.components.LoopTab
import com.example.ui.screens.BookingScreen
import com.example.ui.screens.CreateListingScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ItemDetailsScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.LoopTheme
import kotlinx.coroutines.launch

sealed class ScreenState {
    object Welcome : ScreenState()
    object Main : ScreenState()
    data class ItemDetails(val item: RentalItem) : ScreenState()
    data class Booking(val item: RentalItem) : ScreenState()
    object CreateListing : ScreenState()
}

@Composable
fun LoopApp() {
    LoopTheme {
        var currentScreenState by remember { mutableStateOf<ScreenState>(ScreenState.Welcome) }
        var currentTab by remember { mutableStateOf(LoopTab.HOME) }
        var selectedCategory by remember { mutableStateOf<RentalCategory?>(null) }
        var itemsList by remember { mutableStateOf(RentalRepository.items) }
        var messagesList by remember { mutableStateOf(RentalRepository.sampleMessages) }

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        val onToggleFavorite: (String) -> Unit = { itemId ->
            itemsList = itemsList.map { item ->
                if (item.id == itemId) item.copy(isFavorite = !item.isFavorite) else item
            }
        }

        val onSendMessage: (String) -> Unit = { text ->
            val newMsg = RentalMessage(
                id = "m_${System.currentTimeMillis()}",
                senderName = "Me",
                text = text,
                timestamp = "Just now",
                isFromMe = true
            )
            messagesList = messagesList + newMsg
        }

        val onListingCreated: (String, Int, RentalCategory, String) -> Unit = { title, price, category, desc ->
            val newItem = RentalItem(
                id = "item_${System.currentTimeMillis()}",
                title = title,
                category = category,
                pricePerDay = price,
                location = "Puducherry",
                rating = 5.0f,
                reviewCount = 1,
                description = desc.ifBlank { "Newly listed item by Gokulan R." },
                primaryImageRes = when (category) {
                    RentalCategory.ELECTRONICS, RentalCategory.STUDY_OFFICE -> R.drawable.modern_laptop_1790477981432
                    RentalCategory.FURNITURE, RentalCategory.HOME_LIVING -> R.drawable.modern_armchair_1790478015816
                    RentalCategory.VEHICLES -> R.drawable.electric_scooter_1790477992190
                    else -> R.drawable.canon_eos_camera_1790477967758
                },
                imageCount = 3,
                features = listOf(
                    SpecFeature("CAMERA", "Verified Item"),
                    SpecFeature("BAG", "Accessories Included")
                ),
                owner = RentalOwner("owner_me", "Gokulan R", "New Host", "G", "Joined today", 5.0f),
                isPopular = true,
                isFavorite = false,
                availableToday = true
            )
            itemsList = listOf(newItem) + itemsList
            currentScreenState = ScreenState.Main
            currentTab = LoopTab.HOME
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Your listing '$title' is now live!")
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (currentScreenState is ScreenState.Main) {
                    LoopBottomNavigation(
                        selectedTab = currentTab,
                        onTabSelected = { tab ->
                            if (tab == LoopTab.LIST) {
                                currentScreenState = ScreenState.CreateListing
                            } else {
                                currentTab = tab
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                AnimatedContent(
                    targetState = currentScreenState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { state ->
                    when (state) {
                        is ScreenState.Welcome -> {
                            WelcomeScreen(
                                onGetStarted = { currentScreenState = ScreenState.Main },
                                onLogin = { currentScreenState = ScreenState.Main }
                            )
                        }

                        is ScreenState.Main -> {
                            when (currentTab) {
                                LoopTab.HOME -> {
                                    HomeScreen(
                                        items = itemsList,
                                        onItemClick = { item ->
                                            currentScreenState = ScreenState.ItemDetails(item)
                                        },
                                        onToggleFavorite = onToggleFavorite,
                                        onSelectCategory = { cat ->
                                            selectedCategory = cat
                                            currentTab = LoopTab.EXPLORE
                                        },
                                        onSearchClick = { currentTab = LoopTab.EXPLORE },
                                        onNotificationsClick = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("No new notifications")
                                            }
                                        },
                                        onSeeAllPopular = {
                                            selectedCategory = null
                                            currentTab = LoopTab.EXPLORE
                                        },
                                        onSeeAllCategories = {
                                            selectedCategory = null
                                            currentTab = LoopTab.EXPLORE
                                        }
                                    )
                                }

                                LoopTab.EXPLORE -> {
                                    ExploreScreen(
                                        items = itemsList,
                                        selectedCategory = selectedCategory,
                                        onSelectCategory = { cat ->
                                            selectedCategory = if (selectedCategory == cat) null else cat
                                        },
                                        onItemClick = { item ->
                                            currentScreenState = ScreenState.ItemDetails(item)
                                        },
                                        onToggleFavorite = onToggleFavorite
                                    )
                                }

                                LoopTab.LIST -> {
                                    // Managed via ScreenState.CreateListing
                                }

                                LoopTab.MESSAGES -> {
                                    MessagesScreen(
                                        messages = messagesList,
                                        onSendMessage = onSendMessage
                                    )
                                }

                                LoopTab.PROFILE -> {
                                    ProfileScreen(
                                        onNavigateBookings = {
                                            currentTab = LoopTab.MESSAGES
                                        }
                                    )
                                }
                            }
                        }

                        is ScreenState.ItemDetails -> {
                            ItemDetailsScreen(
                                item = state.item,
                                onRentNow = {
                                    currentScreenState = ScreenState.Booking(state.item)
                                },
                                onToggleFavorite = onToggleFavorite,
                                onContactOwner = {
                                    currentScreenState = ScreenState.Main
                                    currentTab = LoopTab.MESSAGES
                                },
                                onBack = {
                                    currentScreenState = ScreenState.Main
                                }
                            )
                        }

                        is ScreenState.Booking -> {
                            BookingScreen(
                                item = state.item,
                                onBookingConfirmed = {
                                    currentScreenState = ScreenState.Main
                                    currentTab = LoopTab.MESSAGES
                                },
                                onBack = {
                                    currentScreenState = ScreenState.ItemDetails(state.item)
                                }
                            )
                        }

                        is ScreenState.CreateListing -> {
                            CreateListingScreen(
                                onListingCreated = onListingCreated,
                                onClose = {
                                    currentScreenState = ScreenState.Main
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
