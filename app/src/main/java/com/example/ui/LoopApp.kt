package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.RentalRepository
import com.example.data.models.ChatConversation
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
import com.example.ui.theme.ParchmentBg
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
        var selectedConversationId by remember { mutableStateOf<String?>(null) }
        var exploreMapViewEnabled by remember { mutableStateOf(false) }

        // Observe real-time flows from Firestore backend
        val itemsList by RentalRepository.itemsFlow.collectAsStateWithLifecycle()
        val conversationsList by RentalRepository.conversationsFlow.collectAsStateWithLifecycle()

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        val onToggleFavorite: (String) -> Unit = { itemId ->
            RentalRepository.toggleFavorite(itemId)
        }

        val onListingCreated: (String, Int, RentalCategory, String) -> Unit = { title, price, category, desc ->
            val newItem = RentalRepository.createListing(
                title = title,
                price = price,
                category = category,
                desc = desc
            )
            currentScreenState = ScreenState.Main
            currentTab = LoopTab.HOME
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Listing '${newItem.title}' synced with Firestore cloud backend!")
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
                                if (tab == LoopTab.MESSAGES) {
                                    selectedConversationId = null
                                }
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
                    .padding(paddingValues)
                    .background(ParchmentBg)
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
                                            exploreMapViewEnabled = false
                                            currentTab = LoopTab.EXPLORE
                                        },
                                        onSearchClick = {
                                            exploreMapViewEnabled = false
                                            currentTab = LoopTab.EXPLORE
                                        },
                                        onOpenMapDiscovery = {
                                            exploreMapViewEnabled = true
                                            currentTab = LoopTab.EXPLORE
                                        },
                                        onNotificationsClick = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Realtime sync active")
                                            }
                                        },
                                        onSeeAllPopular = {
                                            selectedCategory = null
                                            exploreMapViewEnabled = false
                                            currentTab = LoopTab.EXPLORE
                                        },
                                        onSeeAllCategories = {
                                            selectedCategory = null
                                            exploreMapViewEnabled = false
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
                                        onToggleFavorite = onToggleFavorite,
                                        onContactOwnerFromMap = { owner ->
                                            val matched = conversationsList.find { c ->
                                                c.owner.id == owner.id || c.owner.name.equals(owner.name, ignoreCase = true)
                                            }
                                            selectedConversationId = matched?.id ?: "conv_rakesh"
                                            currentTab = LoopTab.MESSAGES
                                        },
                                        initialMapView = exploreMapViewEnabled
                                    )
                                }

                                LoopTab.LIST -> {
                                    // Managed via ScreenState.CreateListing
                                }

                                LoopTab.MESSAGES -> {
                                    MessagesScreen(
                                        conversations = conversationsList,
                                        selectedConversationId = selectedConversationId,
                                        onViewItemDetails = { item ->
                                            currentScreenState = ScreenState.ItemDetails(item)
                                        }
                                    )
                                }

                                LoopTab.PROFILE -> {
                                    ProfileScreen(
                                        onNavigateBookings = {
                                            selectedConversationId = "conv_rakesh"
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
                                    val matchedConv = conversationsList.find { it.owner.id == state.item.owner.id || it.itemContext?.id == state.item.id }
                                    selectedConversationId = matchedConv?.id ?: "conv_rakesh"
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
                                    val matchedConv = conversationsList.find { it.owner.id == state.item.owner.id || it.itemContext?.id == state.item.id }
                                    selectedConversationId = matchedConv?.id ?: "conv_rakesh"
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
