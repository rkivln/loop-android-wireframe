package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocationHelper
import com.example.data.RentalRepository
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType
import com.example.data.models.MapCategoryFilter
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.ui.components.CommunityActivityMap
import com.example.ui.components.EventMapMarkerCallout
import com.example.ui.components.MapLibreGLMapView
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.ProfileMarker
import com.google.android.gms.maps.model.LatLng
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopTheme
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite
import com.example.ui.theme.PaperWarm
import kotlinx.coroutines.launch

// Light & Dark Themed Architectural Map Palette
private val MapGroundPaperLight = Color(0xFFF6F4ED)
private val MapRoadPrimaryLight = Color(0xFFE5DFD3)
private val MapHubRadiusFillLight = Color(0xFFE2EFE7)
private val MapHubRadiusStrokeLight = Color(0xFF2D5A43)

private val MapGroundPaperDark = Color(0xFF141918)
private val MapRoadPrimaryDark = Color(0xFF232B28)
private val MapHubRadiusFillDark = Color(0xFF1A332B)
private val MapHubRadiusStrokeDark = Color(0xFF5EEAD4)

enum class MapEngineMode(val displayName: String, val emoji: String) {
    MAPLIBRE("MapLibre GL", "🗺️"),
    GOOGLE_MAP("Google Map", "🌐"),
    CANVAS("Canvas", "🏛️")
}

@Composable
fun DiscoveryMapScreen(
    onBackToCatalog: () -> Unit,
    onItemClick: (RentalItem) -> Unit,
    onContactOwner: (RentalOwner) -> Unit,
    onPublishPiece: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allPins by RentalRepository.discoveryPinsFlow.collectAsStateWithLifecycle()

    var selectedCategoryFilter by remember { mutableStateOf(MapCategoryFilter.ALL) }
    var selectedPin by remember { mutableStateOf<DiscoveryPinItem?>(null) }

    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }
    var resolvedAreaLabel by remember { mutableStateOf<String?>(null) }
    var isGpsActive by remember { mutableStateOf(false) }

    // Interactive Pan & Zoom for Light Architectural Canvas Map
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1f) }

    val refreshLiveGps: () -> Unit = {
        LocationHelper.fetchCurrentLocation(
            context = context,
            onLocationReceived = { lat, lng ->
                userLat = lat
                userLng = lng
                isGpsActive = true
                panOffsetX = 0f
                panOffsetY = 0f
                zoomScale = 1.2f
                coroutineScope.launch {
                    resolvedAreaLabel = LocationHelper.reverseGeocodeLocation(context, lat, lng)
                }
            },
            onLocationUnavailable = {
                isGpsActive = false
            }
        )
    }

    // Pulse Animation for GPS Location Dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Permission Launcher for GPS Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            refreshLiveGps()
        } else {
            isGpsActive = false
        }
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            refreshLiveGps()
        }
    }

    // Filter pins based on category chip group
    val filteredPins = remember(selectedCategoryFilter, allPins) {
        when (selectedCategoryFilter) {
            MapCategoryFilter.ALL -> allPins
            MapCategoryFilter.STUDY_GROUPS -> allPins.filter {
                it.filterCategory == MapCategoryFilter.STUDY_GROUPS || it.type == DiscoveryPinType.STUDY_GROUP
            }
            MapCategoryFilter.HELP_NEEDED -> allPins.filter {
                it.filterCategory == MapCategoryFilter.HELP_NEEDED || it.type == DiscoveryPinType.HELP_REQUEST
            }
            MapCategoryFilter.LOCAL_EVENTS -> allPins.filter {
                it.filterCategory == MapCategoryFilter.LOCAL_EVENTS || it.type == DiscoveryPinType.COMMUNITY_EVENT
            }
            MapCategoryFilter.GEAR_HUBS -> allPins.filter {
                it.filterCategory == MapCategoryFilter.GEAR_HUBS || it.type == DiscoveryPinType.PICKUP_HUB
            }
            MapCategoryFilter.MAKERS -> allPins.filter {
                it.filterCategory == MapCategoryFilter.MAKERS ||
                    it.type == DiscoveryPinType.USER_HOST ||
                    it.type == DiscoveryPinType.RENTAL_ITEM
            }
        }
    }

    // Auto-update selectedPin if filtered out
    LaunchedEffect(filteredPins) {
        if (selectedPin != null && selectedPin !in filteredPins) {
            selectedPin = null
        }
    }

    val carouselState = rememberLazyListState()
    var mapMode by remember { mutableStateOf(MapEngineMode.MAPLIBRE) }
    val isDarkTheme = LoopTheme.colors.isDark
    val mapGroundColor = if (isDarkTheme) MapGroundPaperDark else MapGroundPaperLight
    val mapRoadColor = if (isDarkTheme) MapRoadPrimaryDark else MapRoadPrimaryLight
    val mapHubFillColor = if (isDarkTheme) MapHubRadiusFillDark else MapHubRadiusFillLight
    val mapHubStrokeColor = if (isDarkTheme) MapHubRadiusStrokeDark else MapHubRadiusStrokeLight

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mapGroundColor)
    ) {
        when (mapMode) {
            MapEngineMode.MAPLIBRE -> {
                MapLibreGLMapView(
                    modifier = Modifier.fillMaxSize(),
                    activityPoints = filteredPins,
                    selectedPin = selectedPin,
                    centerLat = userLat,
                    centerLng = userLng,
                    onPointSelected = { pin ->
                        selectedPin = pin
                        val pinIndex = filteredPins.indexOf(pin)
                        if (pinIndex >= 0) {
                            coroutineScope.launch {
                                carouselState.animateScrollToItem(pinIndex)
                            }
                        }
                    },
                    onMapClick = {
                        selectedPin = null
                    },
                    onRequestGpsRecenter = {
                        if (LocationHelper.hasLocationPermission(context)) {
                            refreshLiveGps()
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                )
            }
            MapEngineMode.GOOGLE_MAP -> {
                val liveLatLng = if (userLat != null && userLng != null) {
                    LatLng(userLat!!, userLng!!)
                } else {
                    null
                }
                CommunityActivityMap(
                    modifier = Modifier.fillMaxSize(),
                    activityPoints = filteredPins,
                    initialCenter = liveLatLng,
                    onPointSelected = { pin ->
                        selectedPin = pin
                        val pinIndex = filteredPins.indexOf(pin)
                        if (pinIndex >= 0) {
                            coroutineScope.launch {
                                carouselState.animateScrollToItem(pinIndex)
                            }
                        }
                    },
                    onDirectionsClick = { pin ->
                        selectedPin = pin
                    },
                    onRsvpClick = { pin ->
                        selectedPin = pin
                    },
                    onRecenterMissingGps = {
                        if (LocationHelper.hasLocationPermission(context)) {
                            refreshLiveGps()
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    showFilterRow = false
                )
            }
            MapEngineMode.CANVAS -> {
                // Light Architectural Grid Map Canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(0.8f, 2.2f)
                                panOffsetX = (panOffsetX + pan.x).coerceIn(-400f, 400f)
                                panOffsetY = (panOffsetY + pan.y).coerceIn(-400f, 400f)
                            }
                        }
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val width = size.width
                        val height = size.height

                        val centerX = width / 2f + panOffsetX
                        val centerY = height / 2f + panOffsetY

                        val roadColor = mapRoadColor
                        val roadWidth = 2.6.dp.toPx() * zoomScale

                        for (i in -2..2) {
                            val vx = centerX + (width * 0.22f * i * zoomScale)
                            drawLine(roadColor, Offset(vx, 0f), Offset(vx, height), roadWidth)
                            val hy = centerY + (height * 0.18f * i * zoomScale)
                            drawLine(roadColor, Offset(0f, hy), Offset(width, hy), roadWidth)
                        }

                        if (isGpsActive && userLat != null && userLng != null) {
                            val userPos = Offset(centerX, centerY)

                            drawCircle(
                                color = mapHubFillColor.copy(alpha = 0.45f),
                                radius = 90.dp.toPx() * zoomScale,
                                center = userPos
                            )
                            drawCircle(
                                color = mapHubStrokeColor,
                                radius = 90.dp.toPx() * zoomScale,
                                center = userPos,
                                style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f))
                            )
                            drawCircle(
                                color = Color(0xFF2563EB).copy(alpha = pulseAlpha),
                                radius = pulseRadius * zoomScale,
                                center = userPos
                            )
                            drawCircle(
                                color = Color(0xFF2563EB),
                                radius = 6.dp.toPx() * zoomScale,
                                center = userPos
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 6.dp.toPx() * zoomScale,
                                center = userPos,
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }
                    }

                    // Dynamic Community & Listing Pins Layer
                    val refLat = userLat ?: filteredPins.firstOrNull()?.latitude ?: 0.0
                    val refLng = userLng ?: filteredPins.firstOrNull()?.longitude ?: 0.0
                    filteredPins.forEachIndexed { index, pin ->
                        val isSelected = selectedPin?.id == pin.id
                        val deltaLng = ((pin.longitude - refLng) * 14000.0).toFloat().coerceIn(-120f, 120f)
                        val deltaLat = ((refLat - pin.latitude) * 14000.0).toFloat().coerceIn(-140f, 140f)
                        val baseX = (160f + deltaLng + ((index % 3) - 1) * 18f).dp
                        val baseY = (290f + deltaLat + ((index / 3) * 18f)).dp

                        val renderedX = baseX + (panOffsetX * 0.4f).dp
                        val renderedY = baseY + (panOffsetY * 0.4f).dp

                        Box(
                            modifier = Modifier.offset(x = renderedX, y = renderedY)
                        ) {
                when (pin.type) {
                    DiscoveryPinType.PICKUP_HUB -> {
                        // Smart Hub Badge Marker (Solid Forest Green)
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 46.dp else 40.dp)
                                .clip(CircleShape)
                                .background(AccentForestGreen)
                                .border(
                                    2.5.dp,
                                    if (isSelected) AccentWarmYellow else PaperPureWhite,
                                    CircleShape
                                )
                                .clickable {
                                    selectedPin = pin
                                    val pinIndex = filteredPins.indexOf(pin)
                                    if (pinIndex >= 0) {
                                        coroutineScope.launch {
                                            carouselState.animateScrollToItem(pinIndex)
                                        }
                                    }
                                }
                                .testTag("map_marker_${pin.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Smart Hub",
                                tint = InkWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    DiscoveryPinType.STUDY_GROUP -> {
                        // Study Group Badge Marker (Soft Powder Blue)
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 46.dp else 40.dp)
                                .clip(CircleShape)
                                .background(AccentPowderBlue)
                                .border(
                                    2.5.dp,
                                    if (isSelected) AccentWarmYellow else PaperPureWhite,
                                    CircleShape
                                )
                                .clickable {
                                    selectedPin = pin
                                    val pinIndex = filteredPins.indexOf(pin)
                                    if (pinIndex >= 0) {
                                        coroutineScope.launch {
                                            carouselState.animateScrollToItem(pinIndex)
                                        }
                                    }
                                }
                                .testTag("map_marker_${pin.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Study Group",
                                tint = InkCharcoal,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    DiscoveryPinType.HELP_REQUEST -> {
                        // Help Needed Badge Marker (Warm Coral Peach)
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 46.dp else 40.dp)
                                .clip(CircleShape)
                                .background(AccentPeach)
                                .border(
                                    2.5.dp,
                                    if (isSelected) AccentWarmYellow else PaperPureWhite,
                                    CircleShape
                                )
                                .clickable {
                                    selectedPin = pin
                                    val pinIndex = filteredPins.indexOf(pin)
                                    if (pinIndex >= 0) {
                                        coroutineScope.launch {
                                            carouselState.animateScrollToItem(pinIndex)
                                        }
                                    }
                                }
                                .testTag("map_marker_${pin.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = "Help Needed",
                                tint = InkCharcoal,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    DiscoveryPinType.COMMUNITY_EVENT -> {
                        // Local Event Badge Marker (Warm Mint / Photo)
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 46.dp else 40.dp)
                                .clip(CircleShape)
                                .background(AccentMint)
                                .border(
                                    2.5.dp,
                                    if (isSelected) AccentWarmYellow else PaperPureWhite,
                                    CircleShape
                                )
                                .clickable {
                                    selectedPin = pin
                                    val pinIndex = filteredPins.indexOf(pin)
                                    if (pinIndex >= 0) {
                                        coroutineScope.launch {
                                            carouselState.animateScrollToItem(pinIndex)
                                        }
                                    }
                                }
                                .testTag("map_marker_${pin.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Local Event",
                                tint = InkCharcoal,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    else -> {
                        ProfileMarker(
                            initials = pin.title.take(2).uppercase(),
                            imageRes = pin.imageRes,
                            isSelected = isSelected,
                            onClick = {
                                selectedPin = pin
                                val pinIndex = filteredPins.indexOf(pin)
                                if (pinIndex >= 0) {
                                    coroutineScope.launch {
                                        carouselState.animateScrollToItem(pinIndex)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
            }
        }
        }

        // Top Navigation & Filter Bar (Light Themed)
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Pure White Pill)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorder, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = onBackToCatalog
                        )
                        .testTag("map_back_to_catalog_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = InkCharcoal,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Live GPS Location Indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isGpsActive && userLat != null) Color(0xFF2563EB) else InkMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val gpsHeaderText = when {
                            isGpsActive && userLat != null && userLng != null -> {
                                resolvedAreaLabel?.let { "GPS LIVE · ${it.uppercase()}" }
                                    ?: "GPS LIVE · %.3f, %.3f".format(userLat, userLng)
                            }
                            else -> "ENABLE GPS LOCATION"
                        }
                        Text(
                            text = gpsHeaderText,
                            style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                            color = InkCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Map Mode Toggle (MapLibre GL vs Google Map vs Canvas)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (mapMode != MapEngineMode.CANVAS) InkCharcoal else PaperPureWhite)
                            .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    mapMode = when (mapMode) {
                                        MapEngineMode.MAPLIBRE -> MapEngineMode.GOOGLE_MAP
                                        MapEngineMode.GOOGLE_MAP -> MapEngineMode.CANVAS
                                        MapEngineMode.CANVAS -> MapEngineMode.MAPLIBRE
                                    }
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("map_mode_toggle_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${mapMode.emoji} ${mapMode.displayName}",
                            style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = if (mapMode != MapEngineMode.CANVAS) InkWhite else InkCharcoal
                        )
                    }

                    // Locate Me GPS Action Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isGpsActive) AccentMint else PaperPureWhite)
                            .border(1.dp, if (isGpsActive) AccentForestGreen else PaperBorder, CircleShape)
                            .clickable {
                                if (LocationHelper.hasLocationPermission(context)) {
                                    refreshLiveGps()
                                } else {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                            .testTag("map_locate_me_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Locate Me",
                            tint = if (isGpsActive) AccentForestGreen else InkCharcoal,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Category Filter Chip Group for Discovery Map
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapCategoryFilter.values().forEach { filter ->
                    val isSelected = selectedCategoryFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) InkCharcoal else PaperPureWhite)
                            .border(
                                1.dp,
                                if (isSelected) InkCharcoal else PaperBorder,
                                RoundedCornerShape(100.dp)
                            )
                            .clickable {
                                selectedCategoryFilter = filter
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("map_filter_chip_${filter.name}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = filter.emoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = filter.title,
                                style = LoopType.Metadata.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) InkWhite else InkCharcoal
                            )
                        }
                    }
                }
            }
        }

        // Clean Empty State Overlay when no listings/posts exist on the map yet
        if (filteredPins.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(PaperPureWhite.copy(alpha = 0.96f))
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                    .padding(20.dp)
                    .testTag("map_empty_state"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (allPins.isEmpty()) "No pieces mapped yet" else "No pins in this category",
                    style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                    color = InkCharcoal,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (allPins.isEmpty()) {
                        "Publish your first item or community post with GPS enabled to place it on the live map."
                    } else {
                        "Switch back to All to view available pins nearby."
                    },
                    style = LoopType.BodySmall,
                    color = InkSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                PrimaryCTA(
                    text = if (allPins.isEmpty()) "Publish Piece" else "Show All Pins",
                    onClick = {
                        if (allPins.isEmpty()) {
                            onPublishPiece()
                        } else {
                            selectedCategoryFilter = MapCategoryFilter.ALL
                        }
                    },
                    testTag = if (allPins.isEmpty()) "map_empty_publish_button" else "map_empty_reset_filter_button"
                )
            }
        }

        // Bottom Discovery Marker Callout & Carousel Drawer for MapLibre & Canvas modes
        if (mapMode != MapEngineMode.GOOGLE_MAP && filteredPins.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp)
            ) {
                // Reusable UI Component that appears when a user clicks a map marker
                AnimatedVisibility(
                    visible = selectedPin != null,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    selectedPin?.let { pin ->
                        EventMapMarkerCallout(
                            pin = pin,
                            onDirectionsClick = {
                                LocationHelper.openNavigationIntent(
                                    context = context,
                                    latitude = pin.latitude,
                                    longitude = pin.longitude,
                                    label = pin.title
                                )
                            },
                            onRsvpClick = {
                                RentalRepository.rsvpToCommunityPost(pin.id)
                            },
                            onViewDetailsClick = {
                                if (pin.rentalItem != null) {
                                    onItemClick(pin.rentalItem)
                                } else if (pin.owner != null) {
                                    onContactOwner(pin.owner)
                                }
                            },
                            onDismiss = {
                                selectedPin = null
                            }
                        )
                    }
                }

                // Compact Marker Carousel when callout is dismissed or to quickly switch markers
                AnimatedVisibility(visible = selectedPin == null) {
                    LazyRow(
                        state = carouselState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredPins) { pin ->
                            Box(
                                modifier = Modifier
                                    .width(300.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(PaperPureWhite)
                                    .border(1.5.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                                    .clickable { selectedPin = pin }
                                    .padding(12.dp)
                                    .testTag("map_carousel_card_${pin.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val iconBg = when (pin.type) {
                                        DiscoveryPinType.PICKUP_HUB -> AccentForestGreen
                                        DiscoveryPinType.STUDY_GROUP -> AccentPowderBlue
                                        DiscoveryPinType.HELP_REQUEST -> AccentPeach
                                        DiscoveryPinType.COMMUNITY_EVENT -> AccentMint
                                        else -> PaperIvory
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(iconBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (pin.type) {
                                                DiscoveryPinType.PICKUP_HUB -> Icons.Outlined.Lock
                                                DiscoveryPinType.STUDY_GROUP -> Icons.Default.MenuBook
                                                DiscoveryPinType.HELP_REQUEST -> Icons.Default.Handshake
                                                DiscoveryPinType.COMMUNITY_EVENT -> Icons.Default.PhotoCamera
                                                else -> Icons.Default.NearMe
                                            },
                                            contentDescription = null,
                                            tint = if (pin.type == DiscoveryPinType.PICKUP_HUB) InkWhite else InkCharcoal,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pin.title,
                                            style = LoopType.HeadlineMedium.copy(fontSize = 14.sp),
                                            color = InkCharcoal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${pin.tag} · ${pin.distance}",
                                            style = LoopType.Metadata.copy(fontSize = 11.sp),
                                            color = InkSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
