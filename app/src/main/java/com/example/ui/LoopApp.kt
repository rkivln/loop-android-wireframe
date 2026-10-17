package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.RentalItem
import com.example.location.LocationAccessState
import com.example.location.LocationPermissionManager
import com.example.ui.components.LocationPermissionPromptDialog
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ItemDetailsScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.LoopTheme

sealed class ScreenState {
    object Main : ScreenState()
    data class ItemDetails(val item: RentalItem) : ScreenState()
    object EditProfile : ScreenState()
}

enum class LoopTab(val title: String, val icon: ImageVector, val testTag: String) {
    HOME("Home", Icons.Default.Home, "nav_home"),
    EXPLORE("Activity Map", Icons.Default.Map, "nav_explore"),
    MESSAGES("Messages", Icons.AutoMirrored.Filled.Chat, "nav_messages"),
    PROFILE("Profile", Icons.Default.Person, "nav_profile")
}

@Composable
fun LoopApp() {
    LoopTheme {
        val context = LocalContext.current
        val locationPermissionManager = remember { LocationPermissionManager(context) }
        val locationAccessState by locationPermissionManager.accessState.collectAsStateWithLifecycle()

        var showPermissionDialog by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            locationPermissionManager.onPermissionsResult(result)
            showPermissionDialog = false
        }

        LaunchedEffect(Unit) {
            val status = locationPermissionManager.checkCurrentPermissionStatus()
            if (status == LocationAccessState.PromptNeeded) {
                showPermissionDialog = true
            }
        }

        var currentScreen by remember { mutableStateOf<ScreenState>(ScreenState.Main) }
        var currentTab by remember { mutableStateOf(LoopTab.HOME) }

        val isFineLocationGranted = locationAccessState == LocationAccessState.GrantedFine

        Box(modifier = Modifier.fillMaxSize()) {
            when (val screen = currentScreen) {
                is ScreenState.ItemDetails -> {
                    ItemDetailsScreen(
                        item = screen.item,
                        onNavigateBack = { currentScreen = ScreenState.Main },
                        onContactOwner = {
                            currentTab = LoopTab.MESSAGES
                            currentScreen = ScreenState.Main
                        }
                    )
                }
                is ScreenState.EditProfile -> {
                    EditProfileScreen(
                        onNavigateBack = { currentScreen = ScreenState.Main }
                    )
                }
                is ScreenState.Main -> {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                LoopTab.values().forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentTab == tab,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.title
                                            )
                                        },
                                        label = { Text(tab.title) },
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        when (currentTab) {
                            LoopTab.HOME -> {
                                HomeScreen(
                                    onItemClick = { item ->
                                        currentScreen = ScreenState.ItemDetails(item)
                                    },
                                    onExploreMapClick = {
                                        currentTab = LoopTab.EXPLORE
                                    },
                                    onPromptLocation = {
                                        showPermissionDialog = true
                                    },
                                    isLocationGranted = isFineLocationGranted,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            LoopTab.EXPLORE -> {
                                ExploreScreen(
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            LoopTab.MESSAGES -> {
                                MessagesScreen(
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            LoopTab.PROFILE -> {
                                ProfileScreen(
                                    onEditProfile = {
                                        currentScreen = ScreenState.EditProfile
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }

            if (showPermissionDialog) {
                LocationPermissionPromptDialog(
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onDismiss = {
                        locationPermissionManager.dismissPrompt()
                        showPermissionDialog = false
                    }
                )
            }
        }
    }
}
