package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.data.models.CommunityEvent
import com.example.ui.components.EventMapMarkerCallout
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier
) {
    val events by RentalRepository.communityEventsFlow.collectAsStateWithLifecycle()
    var selectedEvent by remember { mutableStateOf<CommunityEvent?>(events.firstOrNull()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Puducherry town center coordinates: Lat 11.9338, Long 79.8354
    val pondyCenter = remember { LatLng(11.9380, 79.8300) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pondyCenter, 13f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("explore_map_screen")
    ) {
        // Google Maps integration using maps-compose
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .testTag("community_google_map"),
            cameraPositionState = cameraPositionState,
            properties = remember { MapProperties(isMyLocationEnabled = false) },
            uiSettings = remember {
                MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                )
            },
            onMapClick = {
                // Keep the selection or allow dismissing callout
            }
        ) {
            events.forEach { event ->
                val markerPosition = LatLng(event.latitude, event.longitude)
                Marker(
                    state = MarkerState(position = markerPosition),
                    title = event.title,
                    snippet = "${event.category} • ${event.timeString}",
                    onClick = {
                        selectedEvent = event
                        false
                    }
                )
            }
        }

        // Top Pill Overlay
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .testTag("map_overlay_header")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Map view",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nearby Activity Points (${events.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Bottom Callout for Selected Community Activity Point
        AnimatedVisibility(
            visible = selectedEvent != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        ) {
            selectedEvent?.let { event ->
                EventMapMarkerCallout(
                    event = event,
                    onJoinClick = { joinedEvent ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Joined ${joinedEvent.title}! See you there."
                            )
                        }
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        )
    }
}
