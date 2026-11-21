package com.example.data

import com.example.data.models.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileAvatarTest {

    @Test
    fun resolvedAvatarUrl_whenCustomAvatarUriIsNull_fallsBackToAuthPhotoUrl() {
        val profile = UserProfile(uid = "u1", displayName = "Alex Mercer", customAvatarUri = null)

        assertEquals(
            "https://lh3.googleusercontent.com/a/photo",
            profile.resolvedAvatarUrl("  https://lh3.googleusercontent.com/a/photo  ")
        )
        assertNull(profile.resolvedAvatarUrl("   "))
        assertNull(profile.resolvedAvatarUrl(null))
        assertFalse(profile.hasExplicitPresetPreference)
    }

    @Test
    fun resolvedAvatarUrl_whenCustomAvatarUriIsEmptyString_respectsExplicitPresetPreference() {
        val profile = UserProfile(uid = "u1", displayName = "Alex Mercer", customAvatarUri = "")

        assertNull(profile.resolvedAvatarUrl("https://lh3.googleusercontent.com/a/photo"))
        assertTrue(profile.hasExplicitPresetPreference)
    }

    @Test
    fun resolvedAvatarUrl_whenCustomAvatarUriIsSet_prefersStoredUrlAndCapsLength() {
        val storedUrl = "  https://lh3.googleusercontent.com/a/stored-avatar  "
        val profile = UserProfile(uid = "u1", displayName = "Alex Mercer", customAvatarUri = storedUrl)

        assertEquals(
            "https://lh3.googleusercontent.com/a/stored-avatar",
            profile.resolvedAvatarUrl("https://lh3.googleusercontent.com/a/other")
        )
        assertFalse(profile.hasExplicitPresetPreference)

        val oversized = "https://example.com/" + "a".repeat(600)
        val oversizedProfile = UserProfile(uid = "u2", customAvatarUri = oversized)
        assertEquals(500, oversizedProfile.resolvedAvatarUrl()?.length)
    }
}
