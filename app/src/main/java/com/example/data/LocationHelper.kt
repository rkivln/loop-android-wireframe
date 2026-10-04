package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.roundToInt

object LocationHelper {

    // Default reference coordinates: Café des Arts, White Town, Puducherry
    const val DEFAULT_LAT = 11.9338
    const val DEFAULT_LNG = 79.8350

    /**
     * Compute distance in meters between two geographical points
     */
    fun calculateDistanceMeters(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, results)
        return results[0]
    }

    /**
     * Format distance human-readably (e.g. "350 m", "1.2 km")
     */
    fun formatDistance(distanceMeters: Float): String {
        return if (distanceMeters < 1000) {
            "${distanceMeters.roundToInt()} m away"
        } else {
            "%.1f km away".format(distanceMeters / 1000f)
        }
    }

    /**
     * Format walking estimate (average 4.8 km/h = 80 m/min)
     */
    fun formatWalkingTime(distanceMeters: Float): String {
        val minutes = (distanceMeters / 80f).roundToInt().coerceAtLeast(1)
        return "$minutes min walk"
    }

    /**
     * Format cycling estimate (average 16 km/h = 266 m/min)
     */
    fun formatCyclingTime(distanceMeters: Float): String {
        val minutes = (distanceMeters / 266f).roundToInt().coerceAtLeast(1)
        return "$minutes min cycle"
    }

    /**
     * Open native maps turn-by-turn navigation
     */
    fun openNavigationIntent(context: Context, latitude: Double, longitude: Double, label: String) {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    /**
     * Fetch current device GPS location safely
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onLocationReceived: (latitude: Double, longitude: Double) -> Unit,
        onError: () -> Unit = {}
    ) {
        try {
            val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        onLocationReceived(loc.latitude, loc.longitude)
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
                .addOnFailureListener {
                    onLocationReceived(DEFAULT_LAT, DEFAULT_LNG)
                }
        } catch (e: Exception) {
            onLocationReceived(DEFAULT_LAT, DEFAULT_LNG)
        }
    }
}
