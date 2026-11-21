package com.example.data

import com.example.data.models.AppearanceMode
import com.example.data.models.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class UserSettingsThemeTest {

    @Test
    fun userSettings_defaultsToLightAppearanceMode() {
        assertEquals(AppearanceMode.LIGHT, UserSettings().appearanceMode)
    }

    @Test
    fun fromNameOrDefault_defaultsToLightAndPreservesExplicitModes() {
        assertEquals(AppearanceMode.LIGHT, AppearanceMode.fromNameOrDefault(null))
        assertEquals(AppearanceMode.LIGHT, AppearanceMode.fromNameOrDefault(""))
        assertEquals(AppearanceMode.LIGHT, AppearanceMode.fromNameOrDefault("   "))
        assertEquals(AppearanceMode.LIGHT, AppearanceMode.fromNameOrDefault("UNKNOWN"))

        assertEquals(AppearanceMode.LIGHT, AppearanceMode.fromNameOrDefault("light"))
        assertEquals(AppearanceMode.DARK, AppearanceMode.fromNameOrDefault("DARK"))
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.fromNameOrDefault("System"))
    }
}
