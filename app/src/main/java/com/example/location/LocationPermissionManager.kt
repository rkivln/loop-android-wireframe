package com.example.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class LocationAccessState {
    object Checking : LocationAccessState()
    object PromptNeeded : LocationAccessState()
    object GrantedFine : LocationAccessState()
    object GrantedCoarseOnly : LocationAccessState()
    object Denied : LocationAccessState()
}

/**
 * Manages location permission flow and device location settings using
 * Google Play Services Location library.
 */
class LocationPermissionManager(private val context: Context) {

    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)

    private val _accessState = MutableStateFlow<LocationAccessState>(LocationAccessState.Checking)
    val accessState: StateFlow<LocationAccessState> = _accessState.asStateFlow()

    private val _isGpsHardwareEnabled = MutableStateFlow(true)
    val isGpsHardwareEnabled: StateFlow<Boolean> = _isGpsHardwareEnabled.asStateFlow()

    fun checkCurrentPermissionStatus(): LocationAccessState {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val state = when {
            fineGranted -> LocationAccessState.GrantedFine
            coarseGranted -> LocationAccessState.GrantedCoarseOnly
            else -> LocationAccessState.PromptNeeded
        }
        _accessState.value = state
        return state
    }

    fun onPermissionsResult(permissions: Map<String, Boolean>) {
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        _accessState.value = when {
            fineGranted -> LocationAccessState.GrantedFine
            coarseGranted -> LocationAccessState.GrantedCoarseOnly
            else -> LocationAccessState.Denied
        }

        if (fineGranted || coarseGranted) {
            checkGooglePlayServicesLocationSettings()
        }
    }

    fun checkGooglePlayServicesLocationSettings(
        onResolutionRequired: ((ResolvableApiException) -> Unit)? = null
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).setMinUpdateIntervalMillis(5000L).build()

        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(settingsBuilder.build())

        task.addOnSuccessListener {
            _isGpsHardwareEnabled.value = true
        }

        task.addOnFailureListener { exception ->
            _isGpsHardwareEnabled.value = false
            if (exception is ResolvableApiException && onResolutionRequired != null) {
                onResolutionRequired(exception)
            }
        }
    }

    fun dismissPrompt() {
        if (_accessState.value == LocationAccessState.PromptNeeded) {
            _accessState.value = LocationAccessState.Denied
        }
    }
}
