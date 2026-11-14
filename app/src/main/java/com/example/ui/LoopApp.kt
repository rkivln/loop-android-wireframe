package com.example.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.location.LocationAccessState
import com.example.location.LocationPermissionManager
import com.example.ui.auth.AuthManager
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.WelcomeScreen
import com.example.ui.components.BottomNavBar
import com.example.ui.components.LocationPermissionPromptDialog
import com.example.ui.components.LoopTab
import com.example.ui.screens.BookingScreen
import com.example.ui.screens.CreateListingScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ItemDetailsScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.LoopTheme
import com.example.ui.theme.PaperIvory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

sealed class ScreenState {
    object Main : ScreenState()
    data class ItemDetails(val item: RentalItem) : ScreenState()
    data class Booking(val item: RentalItem) : ScreenState()
    object CreateListing : ScreenState()
    object EditProfile : ScreenState()
}

@Composable
fun LoopApp() {
    LoopTheme {
        val auth = Firebase.auth
        var currentUser by remember { mutableStateOf(auth.currentUser) }
        var showWelcome by remember { mutableStateOf(true) }

        DisposableEffect(auth) {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                currentUser = user
                if (user == null) {
                    RentalRepository.clearSession()
                }
            }
            auth.addAuthStateListener(listener)
            onDispose {
                auth.removeAuthStateListener(listener)
            }
        }

        if (currentUser == null) {
            if (showWelcome) {
                WelcomeScreen(
                    onGetStarted = { showWelcome = false }
                )
            } else {
                BackHandler { showWelcome = true }
                AuthScreen(
                    onAuthSuccess = {
                        currentUser = auth.currentUser
                    }
                )
            }
        } else {
            key(currentUser!!.uid) {
                LoopMainContent(
                    currentUserId = currentUser!!.uid,
                    onSignedOut = {
                        showWelcome = true
                    }
                )
            }
        }
    }
}

@Composable
fun LoopMainContent(
    currentUserId: String,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val locationPermissionManager = remember { LocationPermissionManager(context) }
    val locationAccessState by locationPermissionManager.accessState.collectAsStateWithLifecycle()
    val conversations by RentalRepository.conversationsFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        RentalRepository.startSession(context, currentUserId)
    }

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
    var currentTab by remember { mutableStateOf(LoopTab.CATALOG) }
    var openExploreInMapMode by remember { mutableStateOf(false) }
    var activeConversationId by remember { mutableStateOf<String?>(null) }

    val unreadCount = conversations.sumOf { it.unreadCount }

    val openConversationWithHost: (RentalOwner, RentalItem?) -> Unit = { owner, item ->
        coroutineScope.launch {
            val result = RentalRepository.openOrCreateConversation(owner, item)
            result.fold(
                onSuccess = { conv ->
                    activeConversationId = conv.id
                    currentTab = LoopTab.STUDIO
                    currentScreen = ScreenState.Main
                },
                onFailure = { error ->
                    snackbarHostState.showSnackbar(
                        error.localizedMessage ?: "Unable to start conversation."
                    )
                }
            )
        }
    }

    val isFineLocationGranted = locationAccessState == LocationAccessState.GrantedFine

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is ScreenState.ItemDetails -> {
                ItemDetailsScreen(
                    item = screen.item,
                    onNavigateBack = { currentScreen = ScreenState.Main },
                    onBookNow = { bookingItem ->
                        currentScreen = ScreenState.Booking(bookingItem)
                    },
                    onContactOwner = { owner, rentalItem ->
                        openConversationWithHost(owner, rentalItem)
                    }
                )
            }
            is ScreenState.Booking -> {
                BookingScreen(
                    item = screen.item,
                    onBack = { currentScreen = ScreenState.ItemDetails(screen.item) },
                    onBookingConfirmed = { currentScreen = ScreenState.Main }
                )
            }
            is ScreenState.CreateListing -> {
                CreateListingScreen(
                    onBack = { currentScreen = ScreenState.Main },
                    onListingCreated = {
                        currentScreen = ScreenState.Main
                        currentTab = LoopTab.OBJECTS
                        openExploreInMapMode = false
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
                    containerColor = PaperIvory,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        BottomNavBar(
                            currentTab = currentTab,
                            unreadCount = unreadCount,
                            onTabSelected = { tab ->
                                if (tab == LoopTab.PUBLISH) {
                                    currentScreen = ScreenState.CreateListing
                                } else {
                                    if (tab == LoopTab.OBJECTS) {
                                        openExploreInMapMode = false
                                    }
                                    currentTab = tab
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when (currentTab) {
                        LoopTab.CATALOG -> {
                            HomeScreen(
                                onItemClick = { item ->
                                    currentScreen = ScreenState.ItemDetails(item)
                                },
                                onExploreMapClick = {
                                    openExploreInMapMode = true
                                    currentTab = LoopTab.OBJECTS
                                },
                                onNavigateToObjects = { category ->
                                    RentalRepository.setSelectedCategory(category)
                                    openExploreInMapMode = false
                                    currentTab = LoopTab.OBJECTS
                                },
                                onPromptLocation = {
                                    showPermissionDialog = true
                                },
                                isLocationGranted = isFineLocationGranted,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        LoopTab.OBJECTS, LoopTab.PUBLISH -> {
                            ExploreScreen(
                                onItemClick = { item ->
                                    currentScreen = ScreenState.ItemDetails(item)
                                },
                                onContactOwner = { owner ->
                                    openConversationWithHost(owner, null)
                                },
                                onBackToCatalog = {
                                    openExploreInMapMode = false
                                    currentTab = LoopTab.CATALOG
                                },
                                initialShowMap = openExploreInMapMode,
                                onMapModeChange = { openExploreInMapMode = it },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        LoopTab.STUDIO -> {
                            MessagesScreen(
                                initialConversationId = activeConversationId,
                                onConversationSelected = { activeConversationId = it },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        LoopTab.PROFILE -> {
                            ProfileScreen(
                                onEditProfile = {
                                    currentScreen = ScreenState.EditProfile
                                },
                                onSignOut = {
                                    AuthManager.signOut(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onSignOutComplete = {
                                            RentalRepository.clearSession()
                                            onSignedOut()
                                        },
                                        scope = coroutineScope
                                    )
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
