package com.example.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {
    private const val TAG = "LocationHelper"

    fun hasLocationPermission(context: Context): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return hasFine || hasCoarse
    }

    fun fetchCurrentLocation(
        context: Context,
        onLocationReceived: (lat: Double, lng: Double) -> Unit,
        onLocationUnavailable: () -> Unit = {}
    ) {
        if (!hasLocationPermission(context)) {
            onLocationUnavailable()
            return
        }

        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationReceived(location.latitude, location.longitude)
                    } else {
                        client.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                onLocationReceived(lastLoc.latitude, lastLoc.longitude)
                            } else {
                                onLocationUnavailable()
                            }
                        }.addOnFailureListener {
                            onLocationUnavailable()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Unable to fetch GPS location: ${e.message}")
                    onLocationUnavailable()
                }
        } catch (se: SecurityException) {
            Log.w(TAG, "Location permission missing: ${se.message}")
            onLocationUnavailable()
        }
    }

    suspend fun reverseGeocodeLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { list ->
                        if (cont.isActive) cont.resume(list)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
            }
            val addr = addresses.firstOrNull() ?: return@withContext null
            val parts = listOfNotNull(
                addr.subLocality?.takeIf { it.isNotBlank() },
                addr.locality?.takeIf { it.isNotBlank() } ?: addr.subAdminArea?.takeIf { it.isNotBlank() }
            ).distinct()
            when {
                parts.isNotEmpty() -> parts.joinToString(", ")
                !addr.adminArea.isNullOrBlank() -> addr.adminArea
                else -> null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Reverse geocode failed: ${e.message}")
            null
        }
    }

    fun openNavigationIntent(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String
    ) {
        val encodedLabel = Uri.encode(label)
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
        val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "No navigation app available: ${e.message}")
        }
    }
}
