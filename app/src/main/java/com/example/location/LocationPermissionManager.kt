package com.example.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LocationAccessState {
    GrantedFine,
    GrantedCoarse,
    Denied,
    PromptNeeded
}

class LocationPermissionManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("loop_location_prefs", Context.MODE_PRIVATE)
    private val _accessState = MutableStateFlow(resolveInitialState())
    val accessState: StateFlow<LocationAccessState> = _accessState.asStateFlow()

    private fun resolveInitialState(): LocationAccessState {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fine) return LocationAccessState.GrantedFine

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (coarse) return LocationAccessState.GrantedCoarse

        val dismissed = prefs.getBoolean("prompt_dismissed", false)
        return if (dismissed) LocationAccessState.Denied else LocationAccessState.PromptNeeded
    }

    fun checkCurrentPermissionStatus(): LocationAccessState {
        val state = resolveInitialState()
        _accessState.value = state
        return state
    }

    fun onPermissionsResult(result: Map<String, Boolean>) {
        val fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        _accessState.value = when {
            fineGranted -> LocationAccessState.GrantedFine
            coarseGranted -> LocationAccessState.GrantedCoarse
            else -> {
                prefs.edit().putBoolean("prompt_dismissed", true).apply()
                LocationAccessState.Denied
            }
        }
    }

    fun dismissPrompt() {
        prefs.edit().putBoolean("prompt_dismissed", true).apply()
        _accessState.value = LocationAccessState.Denied
    }
}
