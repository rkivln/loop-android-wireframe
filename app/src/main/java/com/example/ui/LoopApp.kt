package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.data.ThemePreferencesStore
import com.example.data.models.AppearanceMode
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.data.models.UserCommunityPost
import com.example.location.LocationAccessState
import com.example.location.LocationPermissionManager
import com.example.notifications.NotificationCheckpointStore
import com.example.notifications.NotificationDeepLink
import com.example.notifications.NotificationSyncWorker
import com.example.ui.auth.AuthManager
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.WelcomeScreen
import com.example.ui.components.BottomNavBar
import com.example.ui.components.LocationPermissionPromptDialog
import com.example.ui.components.LoopTab
import com.example.ui.components.NotificationPermissionPromptDialog
import com.example.ui.components.ReleaseNotesDialog
import com.example.update.AppUpdateCoordinator
import com.example.update.AppUpdateUiState
import com.example.ui.screens.BookingScreen
import com.example.ui.screens.CreateListingScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ItemDetailsScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
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
    data class EditListing(val item: RentalItem, val returnToSettings: Boolean = false) : ScreenState()
    object EditProfile : ScreenState()
    data class Settings(val openMyPosts: Boolean = false) : ScreenState()
}

@Composable
fun LoopApp(
    pendingDeepLink: NotificationDeepLink? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val appearanceMode by ThemePreferencesStore.observeAppearanceMode(context)
        .collectAsStateWithLifecycle(initialValue = AppearanceMode.LIGHT)

    LoopTheme(appearanceMode = appearanceMode) {
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
                    pendingDeepLink = pendingDeepLink,
                    onDeepLinkConsumed = onDeepLinkConsumed,
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
    pendingDeepLink: NotificationDeepLink? = null,
    onDeepLinkConsumed: () -> Unit = {},
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val locationPermissionManager = remember { LocationPermissionManager(context) }
    val locationAccessState by locationPermissionManager.accessState.collectAsStateWithLifecycle()
    val conversations by RentalRepository.conversationsFlow.collectAsStateWithLifecycle()
    val syncAlert by RentalRepository.syncAlertFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> RentalRepository.setAppForegroundState(true)
                Lifecycle.Event.ON_STOP -> RentalRepository.setAppForegroundState(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            RentalRepository.setAppForegroundState(false)
        }
    }

    LaunchedEffect(currentUserId) {
        RentalRepository.startSession(context, currentUserId)
    }

    LaunchedEffect(syncAlert?.id) {
        val alert = syncAlert ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = alert.message,
            actionLabel = alert.retryLabel,
            withDismissAction = true,
            duration = if (alert.onRetry != null) SnackbarDuration.Long else SnackbarDuration.Short
        )
        RentalRepository.clearSyncError()
        if (result == SnackbarResult.ActionPerformed) {
            alert.onRetry?.invoke()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        locationPermissionManager.onPermissionsResult(result)
        showPermissionDialog = false
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        showNotificationPermissionDialog = false
        if (granted) {
            ThemePreferencesStore.ensureNotificationChannel(context)
            RentalRepository.syncNotificationsForCurrentUser(context)
            NotificationSyncWorker.triggerImmediateSync(context)
        }
    }

    LaunchedEffect(Unit) {
        val status = locationPermissionManager.checkCurrentPermissionStatus()
        if (status == LocationAccessState.PromptNeeded) {
            showPermissionDialog = true
        }
        AppUpdateCoordinator.initializeAndCheckOnStartup(context)
    }

    LaunchedEffect(currentUserId, showPermissionDialog) {
        if (!showPermissionDialog &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !ThemePreferencesStore.isNotificationPermissionGranted(context) &&
            !NotificationCheckpointStore.hasPromptedPermission(context, currentUserId)
        ) {
            showNotificationPermissionDialog = true
        }
    }

    val updateState by AppUpdateCoordinator.updateState.collectAsStateWithLifecycle()
    val showReleaseNotesDialog by AppUpdateCoordinator.showReleaseNotesDialog.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<ScreenState>(ScreenState.Main) }
    var currentTab by remember { mutableStateOf(LoopTab.CATALOG) }
    var openExploreInMapMode by remember { mutableStateOf(false) }
    var activeConversationId by remember { mutableStateOf<String?>(null) }
    var openBookingsOnProfile by remember { mutableStateOf(false) }
    var highlightedBookingId by remember { mutableStateOf<String?>(null) }
    var openCommunityPostId by remember { mutableStateOf<String?>(null) }
    var openCreatePostOnProfile by remember { mutableStateOf(false) }

    LaunchedEffect(pendingDeepLink, currentUserId) {
        val link = pendingDeepLink ?: return@LaunchedEffect
        when (link) {
            is NotificationDeepLink.Conversation -> {
                activeConversationId = link.conversationId
                currentTab = LoopTab.STUDIO
                currentScreen = ScreenState.Main
            }
            is NotificationDeepLink.Booking -> {
                highlightedBookingId = link.bookingId
                openBookingsOnProfile = true
                currentTab = LoopTab.PROFILE
                currentScreen = ScreenState.Main
            }
            is NotificationDeepLink.CommunityPost -> {
                openCommunityPostId = link.postId
                currentTab = LoopTab.CATALOG
                currentScreen = ScreenState.Main
            }
        }
        onDeepLinkConsumed()
    }

    val unreadCount = conversations
        .filter { it.lastSenderId != currentUserId }
        .sumOf { it.unreadCount }

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

    val openConversationWithOrganizer: (UserCommunityPost) -> Unit = { post ->
        coroutineScope.launch {
            val result = RentalRepository.openConversationWithPostAuthor(post)
            result.fold(
                onSuccess = { conv ->
                    activeConversationId = conv.id
                    currentTab = LoopTab.STUDIO
                    currentScreen = ScreenState.Main
                },
                onFailure = { error ->
                    snackbarHostState.showSnackbar(
                        error.localizedMessage ?: "Unable to message organizer."
                    )
                }
            )
        }
    }

    val isFineLocationGranted = locationAccessState == LocationAccessState.GrantedFine

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            contentKey = { state ->
                when (state) {
                    is ScreenState.Main -> "main"
                    is ScreenState.ItemDetails -> "details_${state.item.id}"
                    is ScreenState.Booking -> "booking_${state.item.id}"
                    is ScreenState.CreateListing -> "create_listing"
                    is ScreenState.EditListing -> "edit_listing_${state.item.id}"
                    is ScreenState.EditProfile -> "edit_profile"
                    is ScreenState.Settings -> "settings"
                }
            },
            transitionSpec = {
                val isReturningToMain = targetState is ScreenState.Main && initialState !is ScreenState.Main
                val isBackFromBooking = targetState is ScreenState.ItemDetails && initialState is ScreenState.Booking
                val isBackFromEdit = (targetState is ScreenState.ItemDetails || targetState is ScreenState.Settings) &&
                    initialState is ScreenState.EditListing
                val isBackTransition = isReturningToMain || isBackFromBooking || isBackFromEdit

                val duration = 240
                if (isBackTransition) {
                    (slideInHorizontally(
                        animationSpec = tween(duration, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> -fullWidth / 5 }
                    ) + fadeIn(tween(duration, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(duration, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> fullWidth / 4 }
                            ) + fadeOut(tween(180))
                        )
                } else {
                    (slideInHorizontally(
                        animationSpec = tween(duration, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> fullWidth / 4 }
                    ) + fadeIn(tween(duration, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(duration, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> -fullWidth / 5 }
                            ) + fadeOut(tween(180))
                        )
                }
            },
            label = "loop_screen_transition"
        ) { screen ->
            when (screen) {
            is ScreenState.ItemDetails -> {
                ItemDetailsScreen(
                    item = screen.item,
                    onNavigateBack = { currentScreen = ScreenState.Main },
                    onBookNow = { bookingItem ->
                        currentScreen = ScreenState.Booking(bookingItem)
                    },
                    onContactOwner = { owner, rentalItem ->
                        openConversationWithHost(owner, rentalItem)
                    },
                    onEditItem = { editItem ->
                        currentScreen = ScreenState.EditListing(editItem, returnToSettings = false)
                    }
                )
            }
            is ScreenState.Booking -> {
                BookingScreen(
                    item = screen.item,
                    onBack = { currentScreen = ScreenState.ItemDetails(screen.item) },
                    onBookingConfirmed = { currentScreen = ScreenState.Main },
                    onViewBookings = {
                        openBookingsOnProfile = true
                        currentTab = LoopTab.PROFILE
                        currentScreen = ScreenState.Main
                    },
                    onMessageHost = { owner, bookingItem ->
                        openConversationWithHost(owner, bookingItem)
                    }
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
            is ScreenState.EditListing -> {
                CreateListingScreen(
                    editingItem = screen.item,
                    onBack = {
                        currentScreen = if (screen.returnToSettings) {
                            ScreenState.Settings(openMyPosts = true)
                        } else {
                            ScreenState.ItemDetails(screen.item)
                        }
                    },
                    onListingUpdated = {
                        currentScreen = if (screen.returnToSettings) {
                            ScreenState.Settings(openMyPosts = true)
                        } else {
                            ScreenState.ItemDetails(screen.item)
                        }
                    },
                    onListingDeleted = {
                        currentScreen = if (screen.returnToSettings) {
                            ScreenState.Settings(openMyPosts = true)
                        } else {
                            ScreenState.Main
                        }
                    }
                )
            }
            is ScreenState.EditProfile -> {
                EditProfileScreen(
                    onNavigateBack = { currentScreen = ScreenState.Main }
                )
            }
            is ScreenState.Settings -> {
                SettingsScreen(
                    initialOpenMyPosts = screen.openMyPosts,
                    onBack = { currentScreen = ScreenState.Main },
                    onOpenItem = { selectedItem ->
                        currentScreen = ScreenState.ItemDetails(selectedItem)
                    },
                    onEditItem = { selectedItem ->
                        currentScreen = ScreenState.EditListing(selectedItem, returnToSettings = true)
                    },
                    onCreateListing = {
                        currentScreen = ScreenState.CreateListing
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
                    }
                )
            }
            is ScreenState.Main -> {
                Scaffold(
                    containerColor = PaperIvory,
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
                                onPublishClick = {
                                    currentScreen = ScreenState.CreateListing
                                },
                                onCreateCommunityPostClick = {
                                    openCreatePostOnProfile = true
                                    currentTab = LoopTab.PROFILE
                                },
                                onMessageOrganizer = openConversationWithOrganizer,
                                initialCommunityPostId = openCommunityPostId,
                                onCommunityPostOpened = { openCommunityPostId = null },
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
                                onContactHostWithItem = { owner, rentalItem ->
                                    openConversationWithHost(owner, rentalItem)
                                },
                                onMessageOrganizer = openConversationWithOrganizer,
                                onBackToCatalog = {
                                    openExploreInMapMode = false
                                    currentTab = LoopTab.CATALOG
                                },
                                onPublishClick = {
                                    currentScreen = ScreenState.CreateListing
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
                                onOpenItem = { item ->
                                    currentScreen = ScreenState.ItemDetails(item)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        LoopTab.PROFILE -> {
                            ProfileScreen(
                                onEditProfile = {
                                    currentScreen = ScreenState.EditProfile
                                },
                                onOpenSettings = {
                                    currentScreen = ScreenState.Settings()
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
                                initialOpenBookings = openBookingsOnProfile,
                                highlightedBookingId = highlightedBookingId,
                                onBookingsSheetOpened = { openBookingsOnProfile = false },
                                onBookingsSheetDismissed = { highlightedBookingId = null },
                                initialOpenCreatePost = openCreatePostOnProfile,
                                onCreatePostOpened = { openCreatePostOnProfile = false },
                                onOpenItem = { item ->
                                    currentScreen = ScreenState.ItemDetails(item)
                                },
                                onMessageUser = { owner, rentalItem ->
                                    openConversationWithHost(owner, rentalItem)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
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
        } else if (showNotificationPermissionDialog) {
            NotificationPermissionPromptDialog(
                onEnableNotifications = {
                    coroutineScope.launch {
                        NotificationCheckpointStore.markPermissionPromptShown(context, currentUserId)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        showNotificationPermissionDialog = false
                        ThemePreferencesStore.ensureNotificationChannel(context)
                        RentalRepository.syncNotificationsForCurrentUser(context)
                    }
                },
                onDismiss = {
                    coroutineScope.launch {
                        NotificationCheckpointStore.markPermissionPromptShown(context, currentUserId)
                    }
                    showNotificationPermissionDialog = false
                }
            )
        }

        val currentAvailableUpdate = updateState as? AppUpdateUiState.UpdateAvailable
        if (showReleaseNotesDialog && currentAvailableUpdate != null) {
            ReleaseNotesDialog(
                state = currentAvailableUpdate,
                onOpenGitHubRelease = {
                    val opened = AppUpdateCoordinator.openReleaseInBrowser(
                        context = context,
                        url = currentAvailableUpdate.release.htmlUrl
                    )
                    AppUpdateCoordinator.closeReleaseNotesDialog()
                    if (!opened) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("No browser app found to open the GitHub Release page.")
                        }
                    }
                },
                onSkipVersion = {
                    AppUpdateCoordinator.skipVersion(context, currentAvailableUpdate.release.tagName)
                },
                onDismiss = {
                    AppUpdateCoordinator.closeReleaseNotesDialog()
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = if (currentScreen is ScreenState.Main) 80.dp else 16.dp
                )
        )
    }
}
