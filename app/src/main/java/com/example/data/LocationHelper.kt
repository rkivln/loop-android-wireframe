package com.example.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {
    private const val TAG = "LocationHelper"
    const val DEFAULT_LAT = 11.9338
    const val DEFAULT_LNG = 79.8350

    fun fetchCurrentLocation(
        context: Context,
        onLocationReceived: (lat: Double, lng: Double) -> Unit
    ) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            onLocationReceived(DEFAULT_LAT, DEFAULT_LNG)
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
                                onLocationReceived(DEFAULT_LAT, DEFAULT_LNG)
                            }
                        }.addOnFailureListener {
                            onLocationReceived(DEFAULT_LAT, DEFAULT_LNG)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Unable to fetch GPS location: ${e.message}")
                    onLocationReceived(DEFAULT_LAT, DEFAULT_LNG)
                }
        } catch (se: SecurityException) {
            Log.w(TAG, "Location permission missing: ${se.message}")
            onLocationReceived(DEFAULT_LAT, DEFAULT_LNG)
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
