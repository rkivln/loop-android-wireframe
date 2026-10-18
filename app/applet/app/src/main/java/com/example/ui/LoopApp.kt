package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.RentalRepository
import com.example.data.models.RentalItem
import com.example.ui.components.BottomNavBar
import com.example.ui.components.LoopTab
import com.example.ui.screens.*
import com.example.ui.theme.LoopTheme

sealed class Screen {
    object MainTabs : Screen()
    data class ItemDetails(val item: RentalItem) : Screen()
    data class Booking(val item: RentalItem) : Screen()
}

@Composable
fun LoopApp() {
    val repository = remember { RentalRepository() }
    val userProfile by repository.userProfile.collectAsState()
    val items by repository.items.collectAsState()
    val conversations by repository.conversations.collectAsState()
    val messagesMap by repository.messages.collectAsState()

    var currentTab by remember { mutableStateOf(LoopTab.HOME) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainTabs) }

    val unreadCount = conversations.sumOf { it.unreadCount }

    LoopTheme {
        Scaffold(
            bottomBar = {
                if (currentScreen is Screen.MainTabs) {
                    BottomNavBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        unreadCount = unreadCount
                    )
                }
            }
        ) { padding ->
            androidx.compose.foundation.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (val screen = currentScreen) {
                    is Screen.MainTabs -> {
                        when (currentTab) {
                            LoopTab.HOME -> HomeScreen(
                                items = items,
                                onItemClick = { currentScreen = Screen.ItemDetails(it) },
                                onToggleFavorite = { repository.toggleFavorite(it) },
                                onSearchClick = { currentTab = LoopTab.EXPLORE }
                            )
                            LoopTab.EXPLORE -> ExploreScreen(
                                items = items,
                                onItemClick = { currentScreen = Screen.ItemDetails(it) },
                                onToggleFavorite = { repository.toggleFavorite(it) }
                            )
                            LoopTab.CREATE -> CreateListingScreen(
                                onListingCreated = { title, category, rate, deposit, location, description, imageUrl ->
                                    repository.createListing(title, category, rate, deposit, location, description, imageUrl)
                                    currentTab = LoopTab.HOME
                                }
                            )
                            LoopTab.MESSAGES -> MessagesScreen(
                                conversations = conversations,
                                messagesMap = messagesMap,
                                onSendMessage = { convId, text ->
                                    repository.sendMessage(convId, text)
                                }
                            )
                            LoopTab.PROFILE -> ProfileScreen(userProfile = userProfile)
                        }
                    }
                    is Screen.ItemDetails -> {
                        ItemDetailsScreen(
                            item = screen.item,
                            onBack = { currentScreen = Screen.MainTabs },
                            onBookNow = { currentScreen = Screen.Booking(it) }
                        )
                    }
                    is Screen.Booking -> {
                        BookingScreen(
                            item = screen.item,
                            onBack = { currentScreen = Screen.ItemDetails(screen.item) },
                            onConfirmBooking = { currentScreen = Screen.MainTabs }
                        )
                    }
                }
            }
        }
    }
}
