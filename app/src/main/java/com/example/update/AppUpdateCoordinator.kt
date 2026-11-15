package com.example.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AppUpdateCoordinator {
    private const val TAG = "AppUpdateCoordinator"
    private val coordinatorScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val installedVersion: String
        get() = BuildConfig.VERSION_NAME

    private val _updateState = MutableStateFlow<AppUpdateUiState>(
        if (UpdatePreferencesStore.defaultBuildConfigRepo.isBlank()) {
            AppUpdateUiState.NotConfigured(installedVersion)
        } else {
            AppUpdateUiState.UpToDate(
                installedVersion = installedVersion,
                configuredRepo = UpdatePreferencesStore.defaultBuildConfigRepo,
                latestTag = null,
                lastCheckedLabel = "Not checked yet"
            )
        }
    )
    val updateState: StateFlow<AppUpdateUiState> = _updateState.asStateFlow()

    private val _effectiveRepoFlow = MutableStateFlow(UpdatePreferencesStore.defaultBuildConfigRepo)
    val effectiveRepoFlow: StateFlow<String> = _effectiveRepoFlow.asStateFlow()

    private val _hasCustomRepoOverrideFlow = MutableStateFlow(false)
    val hasCustomRepoOverrideFlow: StateFlow<Boolean> = _hasCustomRepoOverrideFlow.asStateFlow()

    private val _showReleaseNotesDialog = MutableStateFlow(false)
    val showReleaseNotesDialog: StateFlow<Boolean> = _showReleaseNotesDialog.asStateFlow()

    private var hasRunStartupCheck = false

    fun initializeAndCheckOnStartup(context: Context) {
        val appContext = context.applicationContext
        if (hasRunStartupCheck) return
        hasRunStartupCheck = true

        coordinatorScope.launch {
            val prefs = UpdatePreferencesStore.readPreferences(appContext)
            val effectiveRepo = UpdatePreferencesStore.resolveEffectiveRepo(prefs.customGithubRepo)
            _effectiveRepoFlow.value = effectiveRepo
            _hasCustomRepoOverrideFlow.value = prefs.customGithubRepo.isNotBlank()

            if (effectiveRepo.isBlank()) {
                _updateState.value = AppUpdateUiState.NotConfigured(installedVersion)
                return@launch
            }

            checkForUpdatesInternal(appContext, effectiveRepo, prefs.skippedVersionTag, isManual = false)
        }
    }

    fun checkForUpdates(context: Context, isManual: Boolean = true) {
        val appContext = context.applicationContext
        coordinatorScope.launch {
            val prefs = UpdatePreferencesStore.readPreferences(appContext)
            val effectiveRepo = UpdatePreferencesStore.resolveEffectiveRepo(prefs.customGithubRepo)
            _effectiveRepoFlow.value = effectiveRepo
            _hasCustomRepoOverrideFlow.value = prefs.customGithubRepo.isNotBlank()

            if (effectiveRepo.isBlank()) {
                _updateState.value = AppUpdateUiState.NotConfigured(installedVersion)
                return@launch
            }

            checkForUpdatesInternal(appContext, effectiveRepo, prefs.skippedVersionTag, isManual = isManual)
        }
    }

    private suspend fun checkForUpdatesInternal(
        appContext: Context,
        effectiveRepo: String,
        skippedVersionTag: String,
        isManual: Boolean
    ) {
        val previousState = _updateState.value
        _updateState.value = AppUpdateUiState.Checking(
            installedVersion = installedVersion,
            configuredRepo = effectiveRepo,
            isManual = isManual
        )

        val result = GitHubReleasesService.fetchLatestRelease(effectiveRepo)
        result.fold(
            onSuccess = { release ->
                val now = System.currentTimeMillis()
                UpdatePreferencesStore.recordCheckResult(appContext, now, release.tagName)

                val isNewer = SemanticVersionComparator.isRemoteNewer(
                    installedVersion = installedVersion,
                    remoteTag = release.tagName
                )
                if (isNewer) {
                    val skippedNormalized = SemanticVersionComparator.normalizeTag(skippedVersionTag)
                    val isSkipped = skippedNormalized.isNotBlank() &&
                        skippedNormalized.equals(release.normalizedVersion, ignoreCase = true)

                    val previousBannerDismissed = (previousState as? AppUpdateUiState.UpdateAvailable)
                        ?.takeIf { it.release.tagName == release.tagName }
                        ?.isBannerDismissedForSession ?: false

                    _updateState.value = AppUpdateUiState.UpdateAvailable(
                        installedVersion = installedVersion,
                        configuredRepo = effectiveRepo,
                        release = release,
                        isBannerDismissedForSession = previousBannerDismissed,
                        isVersionSkipped = if (isManual && isSkipped) true else isSkipped
                    )
                } else {
                    _updateState.value = AppUpdateUiState.UpToDate(
                        installedVersion = installedVersion,
                        configuredRepo = effectiveRepo,
                        latestTag = release.tagName,
                        lastCheckedLabel = UpdatePreferencesStore.formatCheckedTimestamp(now)
                    )
                }
            },
            onFailure = { error ->
                Log.w(TAG, "GitHub update check failed for $effectiveRepo: ${error.message}")
                if (!isManual && previousState is AppUpdateUiState.UpdateAvailable) {
                    _updateState.value = previousState
                } else {
                    _updateState.value = AppUpdateUiState.Error(
                        installedVersion = installedVersion,
                        configuredRepo = effectiveRepo,
                        message = error.localizedMessage ?: "Failed to check GitHub releases.",
                        isManual = isManual
                    )
                }
            }
        )
    }

    fun openReleaseNotesDialog() {
        if (_updateState.value is AppUpdateUiState.UpdateAvailable) {
            _showReleaseNotesDialog.value = true
        }
    }

    fun closeReleaseNotesDialog() {
        _showReleaseNotesDialog.value = false
    }

    fun dismissBannerForSession() {
        val current = _updateState.value
        if (current is AppUpdateUiState.UpdateAvailable) {
            _updateState.value = current.copy(isBannerDismissedForSession = true)
        }
    }

    fun skipVersion(context: Context, tagName: String) {
        val appContext = context.applicationContext
        _showReleaseNotesDialog.value = false
        val current = _updateState.value
        if (current is AppUpdateUiState.UpdateAvailable) {
            _updateState.value = current.copy(
                isVersionSkipped = true,
                isBannerDismissedForSession = true
            )
        }
        coordinatorScope.launch {
            UpdatePreferencesStore.saveSkippedVersionTag(appContext, tagName)
        }
    }

    fun clearSkippedVersionAndReopen(context: Context) {
        val appContext = context.applicationContext
        val current = _updateState.value
        if (current is AppUpdateUiState.UpdateAvailable) {
            _updateState.value = current.copy(
                isVersionSkipped = false,
                isBannerDismissedForSession = false
            )
            _showReleaseNotesDialog.value = true
        }
        coordinatorScope.launch {
            UpdatePreferencesStore.clearSkippedVersionTag(appContext)
        }
    }

    fun saveCustomRepository(context: Context, rawInput: String): Result<String> {
        val normalized = SemanticVersionComparator.normalizeOwnerRepoInput(rawInput)
            ?: return Result.failure(
                IllegalArgumentException("Enter a valid repository as owner/repo or a GitHub URL.")
            )
        val appContext = context.applicationContext
        _effectiveRepoFlow.value = normalized
        _hasCustomRepoOverrideFlow.value = true
        coordinatorScope.launch {
            UpdatePreferencesStore.saveCustomRepo(appContext, normalized)
            checkForUpdatesInternal(appContext, normalized, skippedVersionTag = "", isManual = true)
        }
        return Result.success(normalized)
    }

    fun resetRepositoryToDefault(context: Context) {
        val appContext = context.applicationContext
        val defaultRepo = UpdatePreferencesStore.defaultBuildConfigRepo
        _effectiveRepoFlow.value = defaultRepo
        _hasCustomRepoOverrideFlow.value = false
        coordinatorScope.launch {
            UpdatePreferencesStore.clearCustomRepo(appContext)
            if (defaultRepo.isBlank()) {
                _updateState.value = AppUpdateUiState.NotConfigured(installedVersion)
            } else {
                checkForUpdatesInternal(appContext, defaultRepo, skippedVersionTag = "", isManual = true)
            }
        }
    }

    fun openReleaseInBrowser(context: Context, url: String): Boolean {
        val safeUrl = url.trim()
        if (!safeUrl.startsWith("https://", ignoreCase = true)) {
            return false
        }
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}
