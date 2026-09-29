package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.R
import com.example.data.RentalRepository
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.ui.components.DarkPillButton
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.BrandStarAmber
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Puducherry City Center reference coordinates
private val PUDUCHERRY_CENTER = LatLng(11.9340, 79.8330)

@Composable
fun DiscoveryMapScreen(
    pins: List<DiscoveryPinItem> = RentalRepository.discoveryPins,
    onPinChatClick: (RentalOwner) -> Unit,
    onPinItemClick: (RentalItem) -> Unit,
    onSwitchToList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Detect if valid Maps API key is present
    val hasValidGoogleMapsKey = remember {
        val key = try { BuildConfig.MAPS_API_KEY } catch (e: Exception) { "" }
        key.isNotBlank() && !key.contains("YOUR_") && key != "YOUR_MAPS_API_KEY"
    }

    var activeFilter by remember { mutableStateOf("All") }
    var selectedPin by remember { mutableStateOf<DiscoveryPinItem?>(pins.firstOrNull()) }
    var searchQuery by remember { mutableStateOf("") }
    var isVectorViewMode by remember { mutableStateOf(!hasValidGoogleMapsKey) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(PUDUCHERRY_CENTER, 14.2f)
    }

    // Filter pins
    val filteredPins = remember(pins, activeFilter, searchQuery) {
        pins.filter { pin ->
            val matchesFilter = when (activeFilter) {
                "Users & Hosts" -> pin.type == DiscoveryPinType.USER_HOST
                "Community Events" -> pin.type == DiscoveryPinType.COMMUNITY_EVENT
                "Available Today" -> pin.dateOrAvailability.contains("today", ignoreCase = true)
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    pin.title.contains(searchQuery, ignoreCase = true) ||
                    pin.subtitle.contains(searchQuery, ignoreCase = true) ||
                    pin.locationName.contains(searchQuery, ignoreCase = true) ||
                    pin.tag.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    // Vector Map pan and zoom state
    var mapOffsetX by remember { mutableFloatStateOf(0f) }
    var mapOffsetY by remember { mutableFloatStateOf(0f) }
    var mapZoom by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF))
    ) {
        if (!isVectorViewMode && hasValidGoogleMapsKey) {
            // Google Maps SDK when valid API key exists
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                    compassEnabled = true
                ),
                onMapClick = { selectedPin = null }
            ) {
                filteredPins.forEach { pin ->
                    val isSelected = selectedPin?.id == pin.id
                    val markerState = rememberMarkerState(
                        key = pin.id,
                        position = LatLng(pin.latitude, pin.longitude)
                    )

                    MarkerComposable(
                        state = markerState,
                        onClick = {
                            selectedPin = pin
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(pin.latitude, pin.longitude),
                                        15.2f
                                    ),
                                    600
                                )
                            }
                            true
                        }
                    ) {
                        CustomDiscoveryMarker(
                            pin = pin,
                            isSelected = isSelected
                        )
                    }
                }
            }
        } else {
            // High-Fidelity Interactive Vector Discovery Map of Puducherry
            InteractiveVectorDiscoveryMap(
                pins = filteredPins,
                selectedPin = selectedPin,
                onSelectPin = { pin -> selectedPin = pin },
                offsetX = mapOffsetX,
                offsetY = mapOffsetY,
                zoom = mapZoom,
                onTransform = { dZoom, pan ->
                    mapZoom = (mapZoom * dZoom).coerceIn(0.7f, 2.5f)
                    mapOffsetX += pan.x
                    mapOffsetY += pan.y
                }
            )
        }

        // Top Floating Control Bar: Search, List View Switcher, Area Quick Jumps, Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Search & Switch to List Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search map",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(BrandDark),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search users, photowalks, items...",
                                        fontSize = 13.5.sp,
                                        color = TextMuted
                                    )
                                }
                                inner()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("map_search_input")
                        )
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Switch to List View Button
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(BrandDark)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White),
                            onClick = onSwitchToList
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ViewList,
                            contentDescription = "List View",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "List",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Category Chips (All, Users & Hosts, Community Events, Available Today)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Users & Hosts", "Community Events", "Available Today").forEach { filter ->
                    val isSelected = activeFilter == filter
                    val chipBg = if (isSelected) BrandDark else Color.White
                    val chipTextColor = if (isSelected) Color.White else TextPrimary

                    Box(
                        modifier = Modifier
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(chipBg)
                            .clickable { activeFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = chipTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Area Quick Jump Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val areaDestinations = listOf(
                    "📍 White Town" to (0f to 0f),
                    "🏖️ Promenade Beach" to (-40f to -20f),
                    "🌱 Auroville" to (30f to -80f),
                    "🏢 MG Road" to (40f to 30f),
                    "🌿 Lawspet" to (-20f to -60f)
                )

                areaDestinations.forEach { (areaLabel, targetPan) ->
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.94f))
                            .clickable {
                                mapOffsetX = targetPan.first
                                mapOffsetY = targetPan.second
                                mapZoom = 1.25f
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = areaLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Floating Map Controls (Right Side: Layers, My Location, Zoom + / -)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Map Type Toggle (Vector Map / Satellite)
            FloatingMapButton(
                icon = Icons.Default.Layers,
                contentDescription = "Toggle Map Type",
                onClick = {
                    isVectorViewMode = !isVectorViewMode
                }
            )

            // My Location Button
            FloatingMapButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "My Location",
                tint = if (hasLocationPermission) BrandBlue else TextPrimary,
                onClick = {
                    mapOffsetX = 0f
                    mapOffsetY = 0f
                    mapZoom = 1.0f
                    if (!hasLocationPermission) {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            )

            // Zoom In Button
            FloatingMapButton(
                icon = Icons.Default.Add,
                contentDescription = "Zoom In",
                onClick = {
                    mapZoom = (mapZoom + 0.25f).coerceAtMost(2.5f)
                }
            )

            // Zoom Out Button
            FloatingMapButton(
                icon = Icons.Default.Remove,
                contentDescription = "Zoom Out",
                onClick = {
                    mapZoom = (mapZoom - 0.25f).coerceAtLeast(0.7f)
                }
            )
        }

        // Selected Pin Interactive Bottom Card
        AnimatedVisibility(
            visible = selectedPin != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 76.dp, start = 14.dp, end = 14.dp)
        ) {
            selectedPin?.let { pin ->
                DiscoveryPinDetailCard(
                    pin = pin,
                    onChatClick = {
                        pin.owner?.let { onPinChatClick(it) } ?: run {
                            onPinChatClick(RentalRepository.defaultOwner)
                        }
                    },
                    onItemClick = {
                        pin.rentalItem?.let { onPinItemClick(it) }
                    },
                    onClose = { selectedPin = null }
                )
            }
        }
    }
}

@Composable
fun InteractiveVectorDiscoveryMap(
    pins: List<DiscoveryPinItem>,
    selectedPin: DiscoveryPinItem?,
    onSelectPin: (DiscoveryPinItem) -> Unit,
    offsetX: Float,
    offsetY: Float,
    zoom: Float,
    onTransform: (zoomChange: Float, pan: Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    onTransform(gestureZoom, pan)
                }
            }
    ) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        val centerX = canvasWidth / 2 + offsetX
        val centerY = canvasHeight / 2 + offsetY

        // Map Base Canvas (Bay of Bengal coastline, streets, parks, grid)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background Landmass
            drawRect(Color(0xFFF1F5F9))

            // Coastline & Bay of Bengal Sea (East Side)
            val coastPath = Path().apply {
                val coastStartX = centerX + (120f * zoom)
                moveTo(coastStartX, 0f)
                cubicTo(
                    coastStartX - (20f * zoom), centerY - (100f * zoom),
                    coastStartX + (30f * zoom), centerY + (120f * zoom),
                    coastStartX + (10f * zoom), canvasHeight
                )
                lineTo(canvasWidth, canvasHeight)
                lineTo(canvasWidth, 0f)
                close()
            }
            drawPath(coastPath, Color(0xFFBAE6FD))

            // Beach Sand Strip
            val beachPath = Path().apply {
                val coastStartX = centerX + (115f * zoom)
                moveTo(coastStartX, 0f)
                cubicTo(
                    coastStartX - (20f * zoom), centerY - (100f * zoom),
                    coastStartX + (30f * zoom), centerY + (120f * zoom),
                    coastStartX + (10f * zoom), canvasHeight
                )
                lineTo(coastStartX + (14f * zoom), canvasHeight)
                cubicTo(
                    coastStartX + (34f * zoom), centerY + (120f * zoom),
                    coastStartX - (6f * zoom), centerY - (100f * zoom),
                    coastStartX + (14f * zoom), 0f
                )
                close()
            }
            drawPath(beachPath, Color(0xFFFEF08A))

            // Parks & Green Spaces (Bharathi Park, Botanical Garden)
            drawCircle(
                color = Color(0xFFBBF7D0),
                radius = 35f * zoom,
                center = Offset(centerX + (30f * zoom), centerY + (10f * zoom))
            )
            drawRoundRect(
                color = Color(0xFFBBF7D0),
                topLeft = Offset(centerX - (110f * zoom), centerY + (80f * zoom)),
                size = Size(60f * zoom, 50f * zoom),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * zoom)
            )

            // Street Grid (Goubert Ave, Suffren St, Dumas St, MG Road, Mission St)
            val streetColor = Color(0xFFFFFFFF)
            val highwayColor = Color(0xFFFED7AA)

            // Goubert Ave (Beach Promenade road)
            drawLine(
                color = highwayColor,
                start = Offset(centerX + (105f * zoom), 0f),
                end = Offset(centerX + (95f * zoom), canvasHeight),
                strokeWidth = 6f * zoom
            )

            // East Coast Road (ECR towards Auroville)
            drawLine(
                color = highwayColor,
                start = Offset(centerX - (40f * zoom), 0f),
                end = Offset(centerX + (60f * zoom), centerY - (120f * zoom)),
                strokeWidth = 7f * zoom
            )

            // Horizontal Streets
            for (i in -4..5) {
                val y = centerY + (i * 42f * zoom)
                drawLine(
                    color = streetColor,
                    start = Offset(centerX - (200f * zoom), y),
                    end = Offset(centerX + (100f * zoom), y),
                    strokeWidth = 4f * zoom
                )
            }

            // Vertical Streets (French & Heritage quarters)
            for (i in -3..2) {
                val x = centerX + (i * 44f * zoom)
                drawLine(
                    color = streetColor,
                    start = Offset(x, centerY - (180f * zoom)),
                    end = Offset(x, centerY + (180f * zoom)),
                    strokeWidth = 4f * zoom
                )
            }
        }

        // Relative Coordinate Map Projection for Pins
        val pinOffsets = mapOf(
            "pin_rakesh" to (20f to 0f), // White Town
            "pin_event_photowalk" to (95f to 40f), // Rock Beach Promenade
            "pin_priya" to (-60f to -30f), // MG Road
            "pin_event_jam" to (40f to 15f), // Café des Arts
            "pin_vikram" to (90f to 90f), // Goubert Ave
            "pin_ananya" to (-50f to -120f), // Lawspet
            "pin_event_beach_clean" to (70f to -170f), // Auroville Beach
            "pin_event_tech_coffee" to (-30f to -10f), // Mission St
            "pin_sneha" to (-45f to 25f) // Mission Street
        )

        // Render Pin Elements
        pins.forEach { pin ->
            val isSelected = selectedPin?.id == pin.id
            val (relX, relY) = pinOffsets[pin.id] ?: (0f to 0f)
            val posX = centerX + (relX * zoom)
            val posY = centerY + (relY * zoom)

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (posX - 40.dp.toPx()).roundToInt(),
                            (posY - 35.dp.toPx()).roundToInt()
                        )
                    }
                    .clickable { onSelectPin(pin) }
            ) {
                CustomDiscoveryMarker(
                    pin = pin,
                    isSelected = isSelected
                )
            }
        }
    }
}

@Composable
fun CustomDiscoveryMarker(
    pin: DiscoveryPinItem,
    isSelected: Boolean
) {
    val isEvent = pin.type == DiscoveryPinType.COMMUNITY_EVENT
    val markerBgColor = if (isEvent) Color(0xFF6366F1) else Color(0xFF0F172A)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Marker Pill
        Box(
            modifier = Modifier
                .shadow(
                    elevation = if (isSelected) 8.dp else 3.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) Color(0xFF2563EB) else markerBgColor)
                .border(
                    1.5.dp,
                    if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEvent) {
                    Text(
                        text = when {
                            pin.title.contains("Photowalk", ignoreCase = true) -> "📸"
                            pin.title.contains("Acoustic", ignoreCase = true) -> "🎵"
                            pin.title.contains("Eco", ignoreCase = true) -> "🌱"
                            else -> "💻"
                        },
                        fontSize = 12.5.sp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pin.owner?.initials ?: "U",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = pin.title.split(" ").take(2).joinToString(" "),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(3.dp))

                if (isEvent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "EVENT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFCD34D),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "${pin.rating}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Marker Spike Triangle
        Box(
            modifier = Modifier
                .size(8.dp, 5.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (isSelected) Color(0xFF2563EB) else markerBgColor)
        )
    }
}

@Composable
fun DiscoveryPinDetailCard(
    pin: DiscoveryPinItem,
    onChatClick: () -> Unit,
    onItemClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAttending by remember(pin.isAttending) { mutableStateOf(pin.isAttending) }
    val isEvent = pin.type == DiscoveryPinType.COMMUNITY_EVENT

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            // Top Row: Tag, Distance, and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isEvent) Color(0xFFEEF2FF) else Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = pin.tag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEvent) Color(0xFF4F46E5) else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = pin.distance,
                        fontSize = 11.5.sp,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SurfaceSecondary)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Content Row: Image / Avatar + Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (pin.imageRes != null) {
                    Image(
                        painter = painterResource(id = pin.imageRes),
                        contentDescription = pin.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else if (pin.owner != null) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(BrandDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pin.owner.initials,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pin.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = pin.subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = pin.locationName,
                            fontSize = 11.5.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pin.dateOrAvailability,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isEvent) Color(0xFF4F46E5) else BrandGreen
                        )

                        Text(
                            text = pin.priceOrAttendees,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description Snippet
            Text(
                text = pin.description,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = TextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEvent) {
                    // RSVP Button for Events
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(if (isAttending) BrandGreen else Color(0xFF4F46E5))
                            .clickable { isAttending = !isAttending },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAttending) Icons.Default.Check else Icons.Default.Event,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAttending) "RSVP Confirmed ✓" else "Join Event (RSVP)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Chat / Contact Organizer
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(SurfaceSecondary)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(21.dp))
                            .clickable(onClick = onChatClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💬 Event Chat",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                } else {
                    // For Users & Hosts: Message Host + View Item Details
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(BrandDark)
                            .clickable(onClick = onChatClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💬 Message Host",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (pin.rentalItem != null) {
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(BrandDark)
                                .clickable(onClick = onItemClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "View Item",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingMapButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = TextPrimary
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .shadow(elevation = 3.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Color.White, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
