package com.example.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.models.AppearanceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.loopSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "loop_theme_prefs")

object ThemePreferencesStore {
    private val KEY_APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
    const val NOTIFICATION_CHANNEL_ID = "loop_community_alerts"
    const val CHANNEL_MESSAGES = "loop_messages"
    const val CHANNEL_BOOKINGS = "loop_bookings"
    const val CHANNEL_COMMUNITY = "loop_community_alerts"
    const val CHANNEL_ANNOUNCEMENTS = "loop_announcements"

    fun observeAppearanceMode(context: Context): Flow<AppearanceMode> {
        return context.applicationContext.loopSettingsDataStore.data.map { prefs ->
            AppearanceMode.fromNameOrDefault(prefs[KEY_APPEARANCE_MODE])
        }
    }

    suspend fun saveAppearanceMode(context: Context, mode: AppearanceMode) {
        context.applicationContext.loopSettingsDataStore.edit { prefs ->
            prefs[KEY_APPEARANCE_MODE] = mode.name
        }
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_MESSAGES,
                    "Messages & Inquiries",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Real-time chat messages and rental inquiries from neighbors."
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_BOOKINGS,
                    "Bookings & Handover PINs",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reservation confirmations, pickup handover PINs, returns, reviews, and payouts."
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_COMMUNITY,
                    "Neighborhood & Activity Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "New neighborhood posts, comment replies, RSVPs, and local events."
                },
                NotificationChannel(
                    CHANNEL_ANNOUNCEMENTS,
                    "Loop Announcements",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Platform tips, verification alerts, and Loop community news."
                }
            )
            channels.forEach { channel ->
                if (manager.getNotificationChannel(channel.id) == null) {
                    manager.createNotificationChannel(channel)
                }
            }
        }
    }
}
