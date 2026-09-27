package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.LoopCategory
import com.example.data.models.MapPinItem
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

val DarkMapStyleJson = """
[
  {"elementType": "geometry", "stylers": [{"color": "#0d1017"}]},
  {"elementType": "labels.icon", "stylers": [{"visibility": "off"}]},
  {"elementType": "labels.text.fill", "stylers": [{"color": "#7c8ba1"}]},
  {"elementType": "labels.text.stroke", "stylers": [{"color": "#0d1017"}]},
  {"featureType": "administrative", "elementType": "geometry", "stylers": [{"color": "#28334a"}]},
  {"featureType": "administrative.country", "elementType": "geometry.stroke", "stylers": [{"color": "#3b4866"}]},
  {"featureType": "poi", "elementType": "geometry", "stylers": [{"color": "#141926"}]},
  {"featureType": "road", "elementType": "geometry", "stylers": [{"color": "#1a2030"}]},
  {"featureType": "road", "elementType": "geometry.stroke", "stylers": [{"color": "#10141f"}]},
  {"featureType": "road.arterial", "elementType": "geometry", "stylers": [{"color": "#222a3d"}]},
  {"featureType": "road.highway", "elementType": "geometry", "stylers": [{"color": "#2c374f"}]},
  {"featureType": "transit", "elementType": "geometry", "stylers": [{"color": "#1a2030"}]},
  {"featureType": "water", "elementType": "geometry", "stylers": [{"color": "#080e1a"}]},
  {"featureType": "water", "elementType": "geometry.fill", "stylers": [{"color": "#070c17"}]}
]
""".trimIndent()

@Composable
fun GoogleMapView(
    pins: List<MapPinItem>,
    selectedPin: MapPinItem?,
    onPinSelected: (MapPinItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val pondicherry = remember { LatLng(11.9338, 79.8297) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pondicherry, 14.5f)
    }

    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NORMAL,
            mapStyleOptions = MapStyleOptions(DarkMapStyleJson),
            isMyLocationEnabled = false
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            pins.forEach { pin ->
                val markerState = rememberMarkerState(
                    key = pin.id,
                    position = LatLng(pin.latitude, pin.longitude)
                )
                val isSelected = pin.id == selectedPin?.id

                MarkerComposable(
                    state = markerState,
                    onClick = {
                        onPinSelected(pin)
                        true
                    }
                ) {
                    MapMarkerItem(
                        pin = pin,
                        isSelected = isSelected,
                        onClick = { onPinSelected(pin) }
                    )
                }
            }
        }
    }
}

@Composable
fun DarkVectorMapView(
    pins: List<MapPinItem>,
    selectedPin: MapPinItem?,
    onPinSelected: (MapPinItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Clean dark vector terrain
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0xFF0A0C13))

            // Coastline contour on right side
            val waterPath = Path().apply {
                moveTo(size.width * 0.72f, 0f)
                cubicTo(
                    size.width * 0.68f, size.height * 0.3f,
                    size.width * 0.85f, size.height * 0.7f,
                    size.width * 0.82f, size.height
                )
                lineTo(size.width, size.height)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(
                path = waterPath,
                color = Color(0xFF070B14)
            )

            // Coastline divider stroke
            drawPath(
                path = waterPath,
                color = Color(0x1F3B82F6),
                style = Stroke(width = 2f)
            )

            // Systematic road grid
            val roadColor = Color(0x14334155)
            val majorRoadColor = Color(0x22475569)

            drawLine(
                color = majorRoadColor,
                start = Offset(0f, size.height * 0.25f),
                end = Offset(size.width * 0.75f, size.height * 0.65f),
                strokeWidth = 3f
            )
            drawLine(
                color = majorRoadColor,
                start = Offset(size.width * 0.2f, 0f),
                end = Offset(size.width * 0.6f, size.height),
                strokeWidth = 3f
            )

            for (i in 1..7) {
                val y = size.height * (i / 8f)
                drawLine(
                    color = roadColor,
                    start = Offset(0f, y),
                    end = Offset(size.width * 0.75f, y),
                    strokeWidth = 1.5f
                )
            }

            // User center location pin
            val userCenter = Offset(size.width * 0.5f, size.height * 0.45f)
            drawCircle(
                color = BrandPrimary,
                radius = 7f,
                center = userCenter
            )
            drawCircle(
                color = Color(0x334F46E5),
                radius = 16f,
                center = userCenter
            )
        }

        // Render interactive map markers
        pins.forEach { pin ->
            val isSelected = pin.id == selectedPin?.id
            val pinX = (widthPx * pin.xRatio).dp / 2.75f
            val pinY = (heightPx * pin.yRatio).dp / 2.75f

            MapMarkerItem(
                pin = pin,
                isSelected = isSelected,
                onClick = { onPinSelected(pin) },
                modifier = Modifier.offset(x = pinX, y = pinY)
            )
        }
    }
}

@Composable
fun MapMarkerItem(
    pin: MapPinItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val markerColor = when (pin.category) {
        LoopCategory.STUDY -> Color(0xFF4F46E5)
        LoopCategory.EVENTS -> Color(0xFFE11D48)
        LoopCategory.HELP -> Color(0xFF059669)
        LoopCategory.PEOPLE -> Color(0xFF2563EB)
        LoopCategory.ALL -> Color(0xFF7C3AED)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderDefault, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Column {
                    Text(
                        text = pin.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${pin.distance} away",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.padding(top = 3.dp))
        }

        val pinSize = if (isSelected) 36.dp else 30.dp
        Box(
            modifier = Modifier
                .size(pinSize)
                .clip(CircleShape)
                .background(markerColor, CircleShape)
                .border(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) Color.White else BorderDefault,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when (pin.category) {
                LoopCategory.STUDY -> Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                LoopCategory.EVENTS -> Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                LoopCategory.HELP -> Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                LoopCategory.PEOPLE -> Text(
                    text = pin.initials,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                else -> Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
