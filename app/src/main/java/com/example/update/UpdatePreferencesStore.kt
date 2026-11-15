package com.example.update

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.loopUpdateDataStore: DataStore<Preferences> by preferencesDataStore(name = "loop_update_prefs")

data class StoredUpdatePreferences(
    val customGithubRepo: String = "",
    val skippedVersionTag: String = "",
    val lastCheckedEpochMs: Long = 0L,
    val lastLatestTag: String = ""
)

object UpdatePreferencesStore {
    private val KEY_CUSTOM_GITHUB_REPO = stringPreferencesKey("custom_github_repo")
    private val KEY_SKIPPED_VERSION_TAG = stringPreferencesKey("skipped_version_tag")
    private val KEY_LAST_CHECKED_EPOCH_MS = longPreferencesKey("last_checked_epoch_ms")
    private val KEY_LAST_LATEST_TAG = stringPreferencesKey("last_latest_tag")

    val defaultBuildConfigRepo: String
        get() = BuildConfig.GITHUB_UPDATE_REPO.trim()

    fun observePreferences(context: Context): Flow<StoredUpdatePreferences> {
        return context.applicationContext.loopUpdateDataStore.data.map { prefs ->
            StoredUpdatePreferences(
                customGithubRepo = prefs[KEY_CUSTOM_GITHUB_REPO]?.trim() ?: "",
                skippedVersionTag = prefs[KEY_SKIPPED_VERSION_TAG]?.trim() ?: "",
                lastCheckedEpochMs = prefs[KEY_LAST_CHECKED_EPOCH_MS] ?: 0L,
                lastLatestTag = prefs[KEY_LAST_LATEST_TAG]?.trim() ?: ""
            )
        }
    }

    suspend fun readPreferences(context: Context): StoredUpdatePreferences {
        return observePreferences(context).first()
    }

    fun resolveEffectiveRepo(customRepo: String): String {
        return customRepo.trim().ifBlank { defaultBuildConfigRepo }
    }

    suspend fun saveCustomRepo(context: Context, ownerRepo: String) {
        context.applicationContext.loopUpdateDataStore.edit { prefs ->
            prefs[KEY_CUSTOM_GITHUB_REPO] = ownerRepo.trim()
            prefs[KEY_SKIPPED_VERSION_TAG] = ""
        }
    }

    suspend fun clearCustomRepo(context: Context) {
        context.applicationContext.loopUpdateDataStore.edit { prefs ->
            prefs.remove(KEY_CUSTOM_GITHUB_REPO)
            prefs[KEY_SKIPPED_VERSION_TAG] = ""
        }
    }

    suspend fun saveSkippedVersionTag(context: Context, tagName: String) {
        context.applicationContext.loopUpdateDataStore.edit { prefs ->
            prefs[KEY_SKIPPED_VERSION_TAG] = tagName.trim()
        }
    }

    suspend fun clearSkippedVersionTag(context: Context) {
        context.applicationContext.loopUpdateDataStore.edit { prefs ->
            prefs.remove(KEY_SKIPPED_VERSION_TAG)
        }
    }

    suspend fun recordCheckResult(context: Context, epochMs: Long, latestTag: String?) {
        context.applicationContext.loopUpdateDataStore.edit { prefs ->
            prefs[KEY_LAST_CHECKED_EPOCH_MS] = epochMs
            if (!latestTag.isNullOrBlank()) {
                prefs[KEY_LAST_LATEST_TAG] = latestTag.trim()
            }
        }
    }

    fun formatCheckedTimestamp(epochMs: Long): String {
        if (epochMs <= 0L) return "Not checked yet"
        val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        return "Checked ${formatter.format(Date(epochMs))}"
    }
}
