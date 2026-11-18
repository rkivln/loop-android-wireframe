package com.example.notifications

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.notificationCheckpointDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "loop_notification_checkpoints"
)

object NotificationCheckpointStore {

    private fun baselineKey(uid: String) = booleanPreferencesKey("baseline_init_$uid")
    private fun promptShownKey(uid: String) = booleanPreferencesKey("perm_prompt_shown_$uid")
    private fun convSigKey(uid: String) = stringSetPreferencesKey("conv_sigs_$uid")
    private fun bookingSigKey(uid: String) = stringSetPreferencesKey("booking_sigs_$uid")
    private fun postIdsKey(uid: String) = stringSetPreferencesKey("post_ids_$uid")
    private fun postRsvpKey(uid: String) = stringSetPreferencesKey("post_rsvp_$uid")
    private fun itemReviewKey(uid: String) = stringSetPreferencesKey("item_reviews_$uid")
    private fun notifiedKeysKey(uid: String) = stringSetPreferencesKey("notified_keys_$uid")
    private val KEY_FCM_TOKEN = stringPreferencesKey("last_fcm_token")

    private const val MAX_NOTIFIED_HISTORY = 300

    suspend fun hasPromptedPermission(context: Context, uid: String): Boolean {
        if (uid.isBlank()) return true
        val prefs = context.applicationContext.notificationCheckpointDataStore.data.first()
        return prefs[promptShownKey(uid)] ?: false
    }

    suspend fun markPermissionPromptShown(context: Context, uid: String) {
        if (uid.isBlank()) return
        context.applicationContext.notificationCheckpointDataStore.edit { prefs ->
            prefs[promptShownKey(uid)] = true
        }
    }

    suspend fun saveFcmToken(context: Context, token: String) {
        if (token.isBlank()) return
        context.applicationContext.notificationCheckpointDataStore.edit { prefs ->
            prefs[KEY_FCM_TOKEN] = token
        }
    }

    suspend fun getSavedFcmToken(context: Context): String {
        val prefs = context.applicationContext.notificationCheckpointDataStore.data.first()
        return prefs[KEY_FCM_TOKEN].orEmpty()
    }

    suspend fun loadBaselineAndNotifiedKeys(
        context: Context,
        uid: String
    ): Pair<NotificationBaseline?, Set<String>> {
        if (uid.isBlank()) return null to emptySet()
        val prefs = context.applicationContext.notificationCheckpointDataStore.data.first()
        val initialized = prefs[baselineKey(uid)] ?: false
        val notifiedKeys = prefs[notifiedKeysKey(uid)] ?: emptySet()
        if (!initialized) return null to notifiedKeys

        val convMap = (prefs[convSigKey(uid)] ?: emptySet()).associate { entry ->
            val idx = entry.indexOf('|')
            if (idx > 0) entry.substring(0, idx) to entry.substring(idx + 1) else entry to ""
        }
        val bookingMap = (prefs[bookingSigKey(uid)] ?: emptySet()).associate { entry ->
            val parts = entry.split('|')
            val id = parts.getOrNull(0).orEmpty()
            id to BookingFingerprint(
                status = parts.getOrNull(1).orEmpty(),
                handoverOtp = parts.getOrNull(2).orEmpty(),
                isReviewed = parts.getOrNull(3) == "true",
                ratingGiven = parts.getOrNull(4)?.toIntOrNull() ?: 0
            )
        }.filterKeys { it.isNotBlank() }

        val postRsvpMap = (prefs[postRsvpKey(uid)] ?: emptySet()).associate { entry ->
            val idx = entry.indexOf('|')
            if (idx > 0) {
                entry.substring(0, idx) to (entry.substring(idx + 1).toIntOrNull() ?: 0)
            } else {
                entry to 0
            }
        }
        val itemReviewMap = (prefs[itemReviewKey(uid)] ?: emptySet()).associate { entry ->
            val idx = entry.indexOf('|')
            if (idx > 0) {
                entry.substring(0, idx) to (entry.substring(idx + 1).toIntOrNull() ?: 0)
            } else {
                entry to 0
            }
        }

        return NotificationBaseline(
            conversationSignatures = convMap,
            bookingFingerprints = bookingMap,
            seenPostIds = prefs[postIdsKey(uid)] ?: emptySet(),
            postAttendeeCounts = postRsvpMap,
            ownedItemReviewCounts = itemReviewMap
        ) to notifiedKeys
    }

    suspend fun saveBaselineAndNotifiedKeys(
        context: Context,
        uid: String,
        baseline: NotificationBaseline,
        newlyNotifiedKeys: Set<String>
    ) {
        if (uid.isBlank()) return
        context.applicationContext.notificationCheckpointDataStore.edit { prefs ->
            prefs[baselineKey(uid)] = true
            prefs[convSigKey(uid)] = baseline.conversationSignatures.entries.map { "${it.key}|${it.value}" }.toSet()
            prefs[bookingSigKey(uid)] = baseline.bookingFingerprints.entries.map {
                "${it.key}|${it.value.status}|${it.value.handoverOtp}|${it.value.isReviewed}|${it.value.ratingGiven}"
            }.toSet()
            prefs[postIdsKey(uid)] = baseline.seenPostIds.take(500).toSet()
            prefs[postRsvpKey(uid)] = baseline.postAttendeeCounts.entries.map { "${it.key}|${it.value}" }.toSet()
            prefs[itemReviewKey(uid)] = baseline.ownedItemReviewCounts.entries.map { "${it.key}|${it.value}" }.toSet()

            if (newlyNotifiedKeys.isNotEmpty()) {
                val merged = (prefs[notifiedKeysKey(uid)].orEmpty().toList() + newlyNotifiedKeys)
                    .distinct()
                    .takeLast(MAX_NOTIFIED_HISTORY)
                    .toSet()
                prefs[notifiedKeysKey(uid)] = merged
            }
        }
    }

    suspend fun recordNotifiedKeyIfUnseen(context: Context, uid: String, key: String): Boolean {
        if (uid.isBlank() || key.isBlank()) return true
        var isNew = false
        context.applicationContext.notificationCheckpointDataStore.edit { prefs ->
            val existing = prefs[notifiedKeysKey(uid)] ?: emptySet()
            if (key !in existing) {
                isNew = true
                prefs[notifiedKeysKey(uid)] = (existing.toList() + key)
                    .takeLast(MAX_NOTIFIED_HISTORY)
                    .toSet()
            }
        }
        return isNew
    }
}
