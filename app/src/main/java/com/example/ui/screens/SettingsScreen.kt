package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.data.RentalRepository
import com.example.data.ThemePreferencesStore
import com.example.data.models.AVATAR_PRESETS
import com.example.data.models.AppearanceMode
import com.example.data.models.DisappearingMessageTimer
import com.example.data.models.RentalItem
import com.example.data.models.UserCommunityPost
import com.example.notifications.LoopNotificationManager
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.GitHubRepoConfigDialog
import com.example.ui.components.UserAvatar
import com.example.ui.components.chat.DisappearingTimerSheet
import com.example.ui.components.chat.MyEncryptionKeyDialog
import com.example.update.AppUpdateCoordinator
import com.example.update.AppUpdateUiState
import com.example.update.UpdatePreferencesStore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite

private enum class SettingsSubScreen {
    HOME,
    MY_POSTS,
    SAVED_POSTS,
    MY_ACTIVITY,
    PROFILE_VISIBILITY,
    BLOCKED_USERS,
    LOCATION_SHARING,
    PRIVACY_POLICY,
    MANAGE_PERMISSIONS
}

@Composable
fun SettingsScreen(
    initialOpenMyPosts: Boolean = false,
    onBack: () -> Unit,
    onOpenItem: (RentalItem) -> Unit,
    onEditItem: (RentalItem) -> Unit = {},
    onCreateListing: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val userProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val settings by RentalRepository.userSettingsFlow.collectAsStateWithLifecycle()
    val myPublishedItems by RentalRepository.myPublishedItemsFlow.collectAsStateWithLifecycle()
    val myCommunityPosts by RentalRepository.myCommunityPostsFlow.collectAsStateWithLifecycle()
    val savedItems by RentalRepository.savedItemsFlow.collectAsStateWithLifecycle()
    val userActivities by RentalRepository.userActivityFlow.collectAsStateWithLifecycle()
    val ownFingerprint by RentalRepository.ownE2eeFingerprintFlow.collectAsStateWithLifecycle()

    var activeSubScreen by remember(initialOpenMyPosts) {
        mutableStateOf(if (initialOpenMyPosts) SettingsSubScreen.MY_POSTS else SettingsSubScreen.HOME)
    }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRepoConfigDialog by remember { mutableStateOf(false) }
    var showOwnKeyDialog by remember { mutableStateOf(false) }
    var showDefaultDisappearingDialog by remember { mutableStateOf(false) }
    var pendingDeleteItemId by remember { mutableStateOf<String?>(null) }
    var pendingDeletePostId by remember { mutableStateOf<String?>(null) }
    var pendingEditPost by remember { mutableStateOf<UserCommunityPost?>(null) }
    var notificationStatusHint by remember { mutableStateOf<String?>(null) }
    var isOsNotificationGranted by remember {
        mutableStateOf(ThemePreferencesStore.isNotificationPermissionGranted(context))
    }

    val updateState by AppUpdateCoordinator.updateState.collectAsStateWithLifecycle()
    val effectiveRepo by AppUpdateCoordinator.effectiveRepoFlow.collectAsStateWithLifecycle()
    val hasCustomRepoOverride by AppUpdateCoordinator.hasCustomRepoOverrideFlow.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isOsNotificationGranted = ThemePreferencesStore.isNotificationPermissionGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isOsNotificationGranted = granted
        if (granted) {
            ThemePreferencesStore.ensureNotificationChannel(context)
            notificationStatusHint = null
            RentalRepository.updateUserSettings(
                context,
                settings.copy(
                    notifications = settings.notifications.copy(pushEnabled = true)
                )
            )
        } else {
            notificationStatusHint = "Notification permission denied in Android settings. Open Manage Permissions to enable."
            RentalRepository.updateUserSettings(
                context,
                settings.copy(
                    notifications = settings.notifications.copy(pushEnabled = false)
                )
            )
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // ManagePermissionsSubScreen refreshes via lifecycle or composition
    }

    val requestNotificationsFlow: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !ThemePreferencesStore.isNotificationPermissionGranted(context)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            ThemePreferencesStore.ensureNotificationChannel(context)
            isOsNotificationGranted = true
            notificationStatusHint = null
            RentalRepository.updateUserSettings(
                context,
                settings.copy(notifications = settings.notifications.copy(pushEnabled = true))
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (activeSubScreen) {
        SettingsSubScreen.MY_POSTS -> {
            MyPostsSubScreen(
                myItems = myPublishedItems,
                myCommunityPosts = myCommunityPosts,
                onBack = { activeSubScreen = SettingsSubScreen.HOME },
                onOpenItem = onOpenItem,
                onEditItem = onEditItem,
                onToggleItemAvailability = { RentalRepository.toggleListingAvailability(it) },
                onDeleteItem = { pendingDeleteItemId = it },
                onEditPost = { pendingEditPost = it },
                onTogglePostStatus = { RentalRepository.toggleCommunityPostStatus(it) },
                onDeletePost = { pendingDeletePostId = it },
                onCreateListing = onCreateListing
            )
        }
        SettingsSubScreen.SAVED_POSTS -> {
            SavedPostsSubScreen(
                savedItems = savedItems,
                onBack = { activeSubScreen = SettingsSubScreen.HOME },
                onOpenItem = onOpenItem,
                onUnsaveItem = { RentalRepository.toggleFavorite(it) }
            )
        }
        SettingsSubScreen.MY_ACTIVITY -> {
            MyActivitySubScreen(
                activities = userActivities,
                onBack = { activeSubScreen = SettingsSubScreen.HOME }
            )
        }
        SettingsSubScreen.PROFILE_VISIBILITY -> {
            ProfileVisibilitySubScreen(
                selectedVisibility = settings.profileVisibility,
                onSelect = { visibility ->
                    RentalRepository.updateUserSettings(
                        context,
                        settings.copy(profileVisibility = visibility)
                    )
                },
                onBack = { activeSubScreen = SettingsSubScreen.HOME }
            )
        }
        SettingsSubScreen.BLOCKED_USERS -> {
            BlockedUsersSubScreen(
                blockedUsers = settings.blockedUsers,
                onUnblock = { userId ->
                    RentalRepository.unblockUser(context, userId)
                },
                onBack = { activeSubScreen = SettingsSubScreen.HOME }
            )
        }
        SettingsSubScreen.LOCATION_SHARING -> {
            LocationSharingSubScreen(
                selectedPreference = settings.locationSharing,
                onSelect = { pref ->
                    RentalRepository.updateUserSettings(
                        context,
                        settings.copy(locationSharing = pref)
                    )
                },
                onBack = { activeSubScreen = SettingsSubScreen.HOME }
            )
        }
        SettingsSubScreen.PRIVACY_POLICY -> {
            PrivacyPolicySubScreen(
                onBack = { activeSubScreen = SettingsSubScreen.HOME }
            )
        }
        SettingsSubScreen.MANAGE_PERMISSIONS -> {
            ManagePermissionsSubScreen(
                onRequestLocation = {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onRequestNotifications = requestNotificationsFlow,
                onBack = { activeSubScreen = SettingsSubScreen.HOME }
            )
        }
        SettingsSubScreen.HOME -> {
            BackHandler(onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PaperIvory)
                    .navigationBarsPadding()
                    .testTag("settings_screen")
            ) {
                EditorialTopBar(
                    title = "Settings",
                    onBack = onBack
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // User Identity Summary Header
                    item {
                        val authPhotoUrl = remember { Firebase.auth.currentUser?.photoUrl?.toString() }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                avatarUrl = userProfile.resolvedAvatarUrl(authPhotoUrl),
                                initials = userProfile.initials,
                                size = 52.dp,
                                avatarPresetIndex = userProfile.avatarPresetIndex,
                                borderColor = PaperBorderSubtle,
                                borderWidth = 1.dp,
                                contentDescription = "Your profile avatar"
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userProfile.displayName.ifBlank { "Community Member" },
                                    style = LoopType.TitleMedium.copy(fontSize = 16.sp),
                                    color = InkCharcoal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = userProfile.email.ifBlank {
                                        userProfile.location.ifBlank { "Signed in with Google" }
                                    },
                                    style = LoopType.BodySmall,
                                    color = InkSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(AccentMint)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = settings.profileVisibility.label,
                                    style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                                    color = AccentForestGreen
                                )
                            }
                        }
                    }

                    // 1. PROFILE MANAGEMENT
                    item {
                        SettingsGroupHeader("PROFILE MANAGEMENT")
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                        ) {
                            SettingsNavRow(
                                icon = Icons.Default.Inventory2,
                                title = "My Posts",
                                subtitle = "Manage published gear & community requests",
                                badgeText = "${myPublishedItems.size + myCommunityPosts.size}",
                                onClick = { activeSubScreen = SettingsSubScreen.MY_POSTS },
                                testTag = "settings_my_posts_row"
                            )
                            SettingsDivider()
                            SettingsNavRow(
                                icon = Icons.Default.Bookmark,
                                title = "Saved Posts",
                                subtitle = "Bookmarked items ready to reserve",
                                badgeText = "${savedItems.size}",
                                onClick = { activeSubScreen = SettingsSubScreen.SAVED_POSTS },
                                testTag = "settings_saved_posts_row"
                            )
                            SettingsDivider()
                            SettingsNavRow(
                                icon = Icons.Default.Schedule,
                                title = "My Activity",
                                subtitle = "Bookings, listings, and community timeline",
                                badgeText = "${userActivities.size}",
                                onClick = { activeSubScreen = SettingsSubScreen.MY_ACTIVITY },
                                testTag = "settings_my_activity_row"
                            )
                            SettingsDivider()
                            SettingsNavRow(
                                icon = Icons.Default.Visibility,
                                title = "Profile Visibility",
                                subtitle = settings.profileVisibility.label,
                                onClick = { activeSubScreen = SettingsSubScreen.PROFILE_VISIBILITY },
                                testTag = "settings_profile_visibility_row"
                            )
                            SettingsDivider()
                            SettingsNavRow(
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                title = "Logout",
                                subtitle = "Sign out of your Google session",
                                isDestructive = true,
                                showChevron = false,
                                onClick = { showLogoutDialog = true },
                                testTag = "settings_logout_row"
                            )
                        }
                    }

                    // 2. NOTIFICATIONS
                    item {
                        SettingsGroupHeader("NOTIFICATIONS")
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                        ) {
                            val pushEffective = settings.notifications.pushEnabled && isOsNotificationGranted
                            SettingsSwitchRow(
                                icon = Icons.Default.NotificationsActive,
                                title = "Push Notifications",
                                subtitle = when {
                                    !isOsNotificationGranted -> "Android permission required"
                                    settings.notifications.pushEnabled -> "FCM & background sync active"
                                    else -> "Paused for all categories"
                                },
                                checked = pushEffective,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        requestNotificationsFlow()
                                    } else {
                                        notificationStatusHint = null
                                        RentalRepository.updateUserSettings(
                                            context,
                                            settings.copy(
                                                notifications = settings.notifications.copy(pushEnabled = false)
                                            )
                                        )
                                    }
                                },
                                testTag = "settings_push_toggle"
                            )

                            if (notificationStatusHint != null || !isOsNotificationGranted) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AccentPeach)
                                        .clickable { activeSubScreen = SettingsSubScreen.MANAGE_PERMISSIONS }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = notificationStatusHint
                                            ?: "Notifications are turned off in Android system settings.",
                                        style = LoopType.Metadata.copy(fontSize = 11.5.sp),
                                        color = InkCharcoal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Permissions →",
                                        style = LoopType.Metadata.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                                        color = AccentForestGreen
                                    )
                                }
                            }

                            SettingsDivider()
                            SettingsSwitchRow(
                                icon = Icons.Default.NotificationsNone,
                                title = "New neighbourhood posts",
                                subtitle = "Gear drops, photowalks, and help requests nearby",
                                checked = settings.notifications.newNeighbourhoodPosts,
                                enabled = pushEffective,
                                onCheckedChange = { checked ->
                                    RentalRepository.updateUserSettings(
                                        context,
                                        settings.copy(
                                            notifications = settings.notifications.copy(
                                                newNeighbourhoodPosts = checked
                                            )
                                        )
                                    )
                                },
                                testTag = "settings_notify_posts_toggle"
                            )
                            SettingsDivider()
                            SettingsSwitchRow(
                                icon = Icons.Default.ChatBubbleOutline,
                                title = "Replies to your comments",
                                subtitle = "Questions about your listings and event threads",
                                checked = settings.notifications.repliesToComments,
                                enabled = pushEffective,
                                onCheckedChange = { checked ->
                                    RentalRepository.updateUserSettings(
                                        context,
                                        settings.copy(
                                            notifications = settings.notifications.copy(
                                                repliesToComments = checked
                                            )
                                        )
                                    )
                                },
                                testTag = "settings_notify_replies_toggle"
                            )
                            SettingsDivider()
                            SettingsSwitchRow(
                                icon = Icons.Default.FavoriteBorder,
                                title = "Likes and interactions",
                                subtitle = "Saves on your pieces and community RSVPs",
                                checked = settings.notifications.likesAndInteractions,
                                enabled = pushEffective,
                                onCheckedChange = { checked ->
                                    RentalRepository.updateUserSettings(
                                        context,
                                        settings.copy(
                                            notifications = settings.notifications.copy(
                                                likesAndInteractions = checked
                                            )
                                        )
                                    )
                                },
                                testTag = "settings_notify_likes_toggle"
                            )
                            SettingsDivider()
                            SettingsSwitchRow(
                                icon = Icons.Default.Campaign,
                                title = "Announcements",
                                subtitle = "Platform tips, handover updates, and community news",
                                checked = settings.notifications.announcements,
                                enabled = pushEffective,
                                onCheckedChange = { checked ->
                                    RentalRepository.updateUserSettings(
                                        context,
                                        settings.copy(
                                            notifications = settings.notifications.copy(
                                                announcements = checked
                                            )
                                        )
                                    )
                                },
                                testTag = "settings_notify_announcements_toggle"
                            )
                            SettingsDivider()
                            SettingsNavRow(
                                icon = Icons.Default.NotificationsActive,
                                title = "Send test notification",
                                subtitle = notificationStatusHint
                                    ?: "Verify push alert delivery & tap routing on this phone",
                                badgeText = if (pushEffective) "Ready" else "Off",
                                onClick = {
                                    if (!ThemePreferencesStore.isNotificationPermissionGranted(context)) {
                                        requestNotificationsFlow()
                                    } else {
                                        LoopNotificationManager.sendTestNotification(context).fold(
                                            onSuccess = {
                                                notificationStatusHint = "Test notification sent to your status bar."
                                                RentalRepository.syncNotificationsForCurrentUser(context)
                                            },
                                            onFailure = { err ->
                                                notificationStatusHint = err.localizedMessage
                                                    ?: "Unable to post test notification."
                                            }
                                        )
                                    }
                                },
                                testTag = "settings_send_test_notification_btn"
                            )
                        }
                    }

                    // 2B. CHAT & END-TO-END PRIVACY
                    item {
                        SettingsGroupHeader("CHAT & END-TO-END PRIVACY")
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                        ) {
                            SettingsNavRow(
                                icon = Icons.Default.Key,
                                title = "My encryption key",
                                subtitle = if (ownFingerprint.isNotBlank()) {
                                    "Fingerprint: $ownFingerprint"
                                } else {
                                    "Syncing P-256 ECDH device key..."
                                },
                                badgeText = "E2EE",
                                onClick = { showOwnKeyDialog = true },
                                testTag = "settings_chat_e2ee_key_row"
                            )
                            SettingsDivider()
                            SettingsSwitchRow(
                                icon = Icons.Default.DoneAll,
                                title = "Read receipts",
                                subtitle = "Let neighbors see when you've opened their messages",
                                checked = settings.chatPrivacy.sendReadReceipts,
                                onCheckedChange = { checked ->
                                    RentalRepository.updateUserSettings(
                                        context,
                                        settings.copy(
                                            chatPrivacy = settings.chatPrivacy.copy(sendReadReceipts = checked)
                                        )
                                    )
                                },
                                testTag = "settings_chat_read_receipts_toggle"
                            )
                            SettingsDivider()
                            SettingsSwitchRow(
                                icon = Icons.Default.Keyboard,
                                title = "Typing indicators",
                                subtitle = "Show when you are composing a reply",
                                checked = settings.chatPrivacy.showTypingStatus,
                                onCheckedChange = { checked ->
                                    RentalRepository.updateUserSettings(
                                        context,
                                        settings.copy(
                                            chatPrivacy = settings.chatPrivacy.copy(showTypingStatus = checked)
                                        )
                                    )
                                },
                                testTag = "settings_chat_typing_toggle"
                            )
                            SettingsDivider()
                            val defaultTimer = DisappearingMessageTimer.fromSecondsOrDefault(
                                settings.chatPrivacy.defaultDisappearingSeconds
                            )
                            SettingsNavRow(
                                icon = Icons.Default.Timer,
                                title = "Default disappearing timer",
                                subtitle = defaultTimer.label,
                                badgeText = if (defaultTimer != DisappearingMessageTimer.OFF) defaultTimer.shortLabel else null,
                                onClick = { showDefaultDisappearingDialog = true },
                                testTag = "settings_chat_disappearing_row"
                            )
                            SettingsDivider()
                            SettingsSwitchRow(
                                icon = Icons.Default.VisibilityOff,
                                title = "Hide notification previews",
                                subtitle = "Mask sender names and message text in alerts",
                                checked = settings.chatPrivacy.hideNotificationPreviews,
                                onCheckedChange = { checked ->
                                    RentalRepository.updateUserSettings(
                                        context,
                                        settings.copy(
                                            chatPrivacy = settings.chatPrivacy.copy(hideNotificationPreviews = checked)
                                        )
                                    )
                                },
                                testTag = "settings_chat_hide_previews_toggle"
                            )
                        }
                    }

                    // 3. PRIVACY & SECURITY
                    item {
                        SettingsGroupHeader("PRIVACY & SECURITY")
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                        ) {
                            SettingsNavRow(
                                icon = Icons.Default.Visibility,
                                title = "Profile visibility",
                                subtitle = settings.profileVisibility.label,
                                onClick = { activeSubScreen = SettingsSubScreen.PROFILE_VISIBILITY },
                                testTag = "settings_privacy_visibility_row"
                            )
                            SettingsDivider()
                            SettingsNavRow(
                                icon = Icons.Default.Block,
                                title = "Blocked users",
                                subtitle = if (settings.blockedUsers.isEmpty()) {
                                    "No blocked accounts"
                                } else {
                                    "${settings.blockedUsers.size} account(s) hidden from feeds"
                                },
                                badgeText = if (settings.blockedUsers.isNotEmpty()) "${settings.blockedUsers.size}" else null,
                                onClick = { activeSubScreen = SettingsSubScreen.BLOCKED_USERS },
                                testTag = "settings_blocked_users_row"
                            )
                            SettingsDivider()
                            SettingsNavRow(
                                icon = Icons.Default.LocationOn,
                                title = "Location sharing preferences",
                                subtitle = settings.locationSharing.label,
                                onClick = { activeSubScreen = SettingsSubScreen.LOCATION_SHARING },
                                testTag = "settings_location_sharing_row"
                            )
                            SettingsDivider()
                            SettingsNavRow(
                                icon = Icons.Default.Policy,
                                title = "Data and privacy policy",
                                subtitle = "How Loop handles identity, photos & GPS",
                                onClick = { activeSubScreen = SettingsSubScreen.PRIVACY_POLICY },
                                testTag = "settings_privacy_policy_row"
                            )
                            SettingsDivider()
                            SettingsNavRow(
                                icon = Icons.Default.Security,
                                title = "Manage permissions",
                                subtitle = "Location, notifications & Photo Picker",
                                onClick = { activeSubScreen = SettingsSubScreen.MANAGE_PERMISSIONS },
                                testTag = "settings_manage_permissions_row"
                            )
                        }
                    }

                    // 4. APPEARANCE
                    item {
                        SettingsGroupHeader("APPEARANCE")
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                        ) {
                            AppearanceMode.entries.forEachIndexed { idx, mode ->
                                if (idx > 0) SettingsDivider()
                                val isSelected = settings.appearanceMode == mode
                                val icon = when (mode) {
                                    AppearanceMode.LIGHT -> Icons.Default.LightMode
                                    AppearanceMode.DARK -> Icons.Default.DarkMode
                                    AppearanceMode.SYSTEM -> Icons.Default.PhoneAndroid
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 60.dp)
                                        .clickable {
                                            RentalRepository.updateUserSettings(
                                                context,
                                                settings.copy(appearanceMode = mode)
                                            )
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .testTag("appearance_option_${mode.name}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) AccentMint else PaperIvory),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) AccentForestGreen else InkSecondary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.label,
                                            style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                                            color = InkCharcoal
                                        )
                                        Text(
                                            text = mode.subtitle,
                                            style = LoopType.BodySmall,
                                            color = InkSecondary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) AccentForestGreen else Color.Transparent)
                                            .border(
                                                width = if (isSelected) 0.dp else 1.5.dp,
                                                color = if (isSelected) AccentForestGreen else PaperBorder,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = InkWhite,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. APP UPDATES
                    item {
                        SettingsGroupHeader("APP UPDATES")
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                        ) {
                            val currentUpdateState = updateState
                            val statusSubtitle = when (currentUpdateState) {
                                is AppUpdateUiState.NotConfigured -> "Set a GitHub owner/repo below to enable release checks"
                                is AppUpdateUiState.Checking -> "Checking ${currentUpdateState.configuredRepo}..."
                                is AppUpdateUiState.UpToDate -> {
                                    val latestNote = currentUpdateState.latestTag?.let { " · Latest $it" } ?: ""
                                    "Up to date$latestNote · ${currentUpdateState.lastCheckedLabel}"
                                }
                                is AppUpdateUiState.UpdateAvailable -> {
                                    "${currentUpdateState.release.tagName} available (${currentUpdateState.release.publishedAtLabel})"
                                }
                                is AppUpdateUiState.Error -> currentUpdateState.message
                            }
                            val statusBadge = when (currentUpdateState) {
                                is AppUpdateUiState.UpdateAvailable -> "Update ${currentUpdateState.release.tagName}"
                                is AppUpdateUiState.Checking -> "Checking"
                                is AppUpdateUiState.UpToDate -> "Current"
                                is AppUpdateUiState.NotConfigured -> "Setup"
                                is AppUpdateUiState.Error -> "Retry"
                            }

                            SettingsNavRow(
                                icon = Icons.Default.SystemUpdate,
                                title = "Loop v${BuildConfig.VERSION_NAME}",
                                subtitle = statusSubtitle,
                                badgeText = statusBadge,
                                showChevron = currentUpdateState is AppUpdateUiState.UpdateAvailable ||
                                    currentUpdateState is AppUpdateUiState.NotConfigured,
                                onClick = {
                                    when (currentUpdateState) {
                                        is AppUpdateUiState.UpdateAvailable -> AppUpdateCoordinator.openReleaseNotesDialog()
                                        is AppUpdateUiState.NotConfigured -> showRepoConfigDialog = true
                                        else -> AppUpdateCoordinator.checkForUpdates(context, isManual = true)
                                    }
                                },
                                testTag = "settings_update_status_row"
                            )
                            SettingsDivider()
                            SettingsNavRow(
                                icon = Icons.Default.Source,
                                title = "GitHub repository",
                                subtitle = effectiveRepo.ifBlank { "Tap to enter owner/repo" },
                                badgeText = when {
                                    hasCustomRepoOverride -> "Custom"
                                    effectiveRepo.isNotBlank() -> "Default"
                                    else -> null
                                },
                                onClick = { showRepoConfigDialog = true },
                                testTag = "settings_github_repo_row"
                            )
                            SettingsDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 58.dp)
                                    .clickable(enabled = currentUpdateState !is AppUpdateUiState.Checking) {
                                        when {
                                            effectiveRepo.isBlank() -> showRepoConfigDialog = true
                                            currentUpdateState is AppUpdateUiState.UpdateAvailable &&
                                                currentUpdateState.isVersionSkipped -> {
                                                AppUpdateCoordinator.clearSkippedVersionAndReopen(context)
                                            }
                                            else -> AppUpdateCoordinator.checkForUpdates(context, isManual = true)
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .testTag("settings_check_updates_btn"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(PaperIvory),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (currentUpdateState is AppUpdateUiState.Checking) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            color = AccentForestGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = InkCharcoal,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when {
                                            currentUpdateState is AppUpdateUiState.Checking -> "Checking GitHub Releases..."
                                            currentUpdateState is AppUpdateUiState.UpdateAvailable &&
                                                currentUpdateState.isVersionSkipped -> "Review skipped release ${currentUpdateState.release.tagName}"
                                            else -> "Check for updates"
                                        },
                                        style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                                        color = InkCharcoal
                                    )
                                    Text(
                                        text = if (effectiveRepo.isBlank()) {
                                            "Configure a repository first"
                                        } else {
                                            "Inspect latest stable release on GitHub"
                                        },
                                        style = LoopType.BodySmall,
                                        color = InkSecondary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = PaperPureWhite,
            titleContentColor = InkCharcoal,
            textContentColor = InkSecondary,
            title = {
                Text(
                    text = "Sign out of Loop?",
                    style = LoopType.HeadlineMedium.copy(fontSize = 19.sp)
                )
            },
            text = {
                Text(
                    text = "Your published listings, saved pieces, and preferences remain synced safely to your account.",
                    style = LoopType.BodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                    },
                    modifier = Modifier.testTag("settings_logout_confirm_btn")
                ) {
                    Text(
                        text = "Logout",
                        color = AlertCrimson,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "Cancel", color = InkCharcoal)
                }
            }
        )
    }

    pendingDeleteItemId?.let { itemId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteItemId = null },
            containerColor = PaperPureWhite,
            titleContentColor = InkCharcoal,
            textContentColor = InkSecondary,
            title = { Text("Delete this listing?", style = LoopType.HeadlineMedium.copy(fontSize = 18.sp)) },
            text = { Text("This piece will be removed from the marketplace catalog.", style = LoopType.BodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = {
                        RentalRepository.deleteListing(itemId)
                        pendingDeleteItemId = null
                    }
                ) {
                    Text("Delete", color = AlertCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteItemId = null }) {
                    Text("Cancel", color = InkCharcoal)
                }
            }
        )
    }

    pendingDeletePostId?.let { postId ->
        AlertDialog(
            onDismissRequest = { pendingDeletePostId = null },
            containerColor = PaperPureWhite,
            titleContentColor = InkCharcoal,
            textContentColor = InkSecondary,
            title = { Text("Delete community post?", style = LoopType.HeadlineMedium.copy(fontSize = 18.sp)) },
            text = { Text("Neighbors will no longer see this community initiative.", style = LoopType.BodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = {
                        RentalRepository.deleteCommunityPost(postId)
                        pendingDeletePostId = null
                    }
                ) {
                    Text("Delete", color = AlertCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePostId = null }) {
                    Text("Cancel", color = InkCharcoal)
                }
            }
        )
    }

        AnimatedVisibility(
            visible = pendingEditPost != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            pendingEditPost?.let { postToEdit ->
                BackHandler { pendingEditPost = null }
                CreateCommunityPostModalSheet(
                    editingPost = postToEdit,
                    onPostUpdated = { updated ->
                        RentalRepository.updateCommunityPost(updated)
                        pendingEditPost = null
                    },
                    onPostDeleted = { toDelete ->
                        pendingEditPost = null
                        pendingDeletePostId = toDelete.id
                    },
                    onDismiss = { pendingEditPost = null }
                )
            }
        }

        if (showRepoConfigDialog) {
            GitHubRepoConfigDialog(
                currentRepo = effectiveRepo,
                defaultBuildConfigRepo = UpdatePreferencesStore.defaultBuildConfigRepo,
                hasCustomOverride = hasCustomRepoOverride,
                onSave = { rawInput ->
                    AppUpdateCoordinator.saveCustomRepository(context, rawInput)
                },
                onResetToDefault = {
                    AppUpdateCoordinator.resetRepositoryToDefault(context)
                },
                onDismiss = { showRepoConfigDialog = false }
            )
        }

        if (showOwnKeyDialog) {
            MyEncryptionKeyDialog(
                ownFingerprint = ownFingerprint,
                onCopyFingerprint = { fp ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    clipboard?.setPrimaryClip(ClipData.newPlainText("Loop E2EE Key Fingerprint", fp))
                    Toast.makeText(context, "Encryption fingerprint copied", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showOwnKeyDialog = false }
            )
        }

        if (showDefaultDisappearingDialog) {
            DisappearingTimerSheet(
                title = "Default Disappearing Timer",
                subtitle = "Applied automatically when starting new Studio Dispatch conversations.",
                selectedSeconds = settings.chatPrivacy.defaultDisappearingSeconds,
                onSelectTimer = { timer ->
                    RentalRepository.updateUserSettings(
                        context,
                        settings.copy(
                            chatPrivacy = settings.chatPrivacy.copy(
                                defaultDisappearingSeconds = timer.seconds
                            )
                        )
                    )
                },
                onDismiss = { showDefaultDisappearingDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = LoopType.EditorialTag.copy(fontSize = 11.sp),
        color = InkSecondary,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = PaperBorderSubtle,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    isDestructive: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit,
    testTag: String
) {
    val iconTint = if (isDestructive) AlertCrimson else InkCharcoal
    val titleColor = if (isDestructive) AlertCrimson else InkCharcoal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isDestructive) AlertCrimson.copy(alpha = 0.12f) else PaperIvory),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                color = titleColor
            )
            Text(
                text = subtitle,
                style = LoopType.BodySmall,
                color = InkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!badgeText.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(AccentMint)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = AccentForestGreen
                )
            }
        }
        if (showChevron) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = InkMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (checked && enabled) AccentMint else PaperIvory),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked && enabled) AccentForestGreen else InkMuted,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                color = if (enabled) InkCharcoal else InkMuted
            )
            Text(
                text = subtitle,
                style = LoopType.BodySmall,
                color = if (enabled) InkSecondary else InkMuted
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = InkWhite,
                checkedTrackColor = AccentForestGreen,
                uncheckedThumbColor = InkSecondary,
                uncheckedTrackColor = PaperIvory,
                uncheckedBorderColor = PaperBorder
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
