package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.LocationHelper
import com.example.data.RentalRepository
import com.example.data.models.CuratedWalkingRoute
import com.example.data.models.DiscoveryPinItem
import com.example.data.models.DiscoveryPinType
import com.example.data.models.RentalCategory
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.ui.components.ProfileMarker
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite
import com.example.ui.theme.PaperWarm
import kotlinx.coroutines.launch

// Light Themed Editorial Map Palette
private val MapGroundPaper = Color(0xFFF6F4ED)        // Warm ivory / parchment terrain
private val MapWaterSeaside = Color(0xFFD6E4EB)       // Serene coastal watercolor wash
private val MapWaterCoastline = Color(0xFFBFD2DC)     // Coastline shore border
private val MapRoadPrimary = Color(0xFFE5DFD3)        // Warm stone avenue lines
private val MapRoadSecondary = Color(0xFFEFECE3)      // Subtle cross street casing
private val MapRouteTrack = Color(0xFFE5A93C)         // Warm ochre trail highlight
private val MapHubRadiusFill = Color(0xFFE2EFE7)      // Soft mint pickup zone fill
private val MapHubRadiusStroke = Color(0xFF2D5A43)    // Forest green dashed boundary

@Composable
fun DiscoveryMapScreen(
    onBackToCatalog: () -> Unit,
    onItemClick: (RentalItem) -> Unit,
    onContactOwner: (RentalOwner) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allPins = RentalRepository.discoveryPins
    val allRoutes = RentalRepository.curatedRoutes

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedRoute by remember { mutableStateOf<CuratedWalkingRoute?>(null) }
    var selectedPin by remember { mutableStateOf<DiscoveryPinItem?>(allPins.firstOrNull()) }

    var userLat by remember { mutableDoubleStateOf(LocationHelper.DEFAULT_LAT) }
    var userLng by remember { mutableDoubleStateOf(LocationHelper.DEFAULT_LNG) }
    var isGpsActive by remember { mutableStateOf(false) }

    // Interactive Pan & Zoom for Light Architectural Canvas Map
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1f) }

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
            isGpsActive = true
            LocationHelper.fetchCurrentLocation(
                context = context,
                onLocationReceived = { lat, lng ->
                    userLat = lat
                    userLng = lng
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            isGpsActive = true
            LocationHelper.fetchCurrentLocation(
                context = context,
                onLocationReceived = { lat, lng ->
                    userLat = lat
                    userLng = lng
                }
            )
        }
    }

    // Filter pins based on category & active route
    val filteredPins = remember(selectedCategoryFilter, selectedRoute, allPins) {
        if (selectedRoute != null) {
            allPins.filter { it.id in selectedRoute!!.pinIds }
        } else {
            when (selectedCategoryFilter) {
                "Cameras" -> allPins.filter { it.category == RentalCategory.ELECTRONICS }
                "E-Bikes" -> allPins.filter { it.category == RentalCategory.VEHICLES }
                "Audio/Synths" -> allPins.filter { it.category == RentalCategory.STUDY_OFFICE }
                "Hubs" -> allPins.filter { it.type == DiscoveryPinType.PICKUP_HUB }
                "Meetups" -> allPins.filter { it.type == DiscoveryPinType.COMMUNITY_EVENT }
                else -> allPins
            }
        }
    }

    val carouselState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MapGroundPaper)
    ) {
        // Light Themed Architectural Map Canvas
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

                // East Bay of Bengal watercolor coastal wash
                val coastX = centerX + (width * 0.28f * zoomScale)
                drawRect(
                    color = MapWaterSeaside,
                    topLeft = Offset(coastX, 0f),
                    size = androidx.compose.ui.geometry.Size(width - coastX, height)
                )

                // Coastline separation shore contour
                drawLine(
                    color = MapWaterCoastline,
                    start = Offset(coastX, 0f),
                    end = Offset(coastX, height),
                    strokeWidth = 3.dp.toPx()
                )

                // French Quarter Avenues (Dumas, Romain Rolland, Suffren, Goubert)
                val roadColor = MapRoadPrimary
                val roadWidth = 3.2.dp.toPx() * zoomScale

                val ave1 = centerX - (width * 0.32f * zoomScale)
                val ave2 = centerX - (width * 0.12f * zoomScale)
                val ave3 = centerX + (width * 0.08f * zoomScale)
                val ave4 = centerX + (width * 0.24f * zoomScale)

                drawLine(roadColor, Offset(ave1, 0f), Offset(ave1, height), roadWidth)
                drawLine(roadColor, Offset(ave2, 0f), Offset(ave2, height), roadWidth)
                drawLine(roadColor, Offset(ave3, 0f), Offset(ave3, height), roadWidth)
                drawLine(roadColor, Offset(ave4, 0f), Offset(ave4, height), roadWidth)

                // French Quarter Cross Streets (Rue Suffren, Rue De La Marine, Rue François Martin)
                val st1 = centerY - (height * 0.28f * zoomScale)
                val st2 = centerY - (height * 0.10f * zoomScale)
                val st3 = centerY + (height * 0.08f * zoomScale)
                val st4 = centerY + (height * 0.26f * zoomScale)

                drawLine(roadColor, Offset(0f, st1), Offset(coastX, st1), roadWidth)
                drawLine(roadColor, Offset(0f, st2), Offset(coastX, st2), roadWidth)
                drawLine(roadColor, Offset(0f, st3), Offset(coastX, st3), roadWidth)
                drawLine(roadColor, Offset(0f, st4), Offset(coastX, st4), roadWidth)

                // Active Curated Walking Route Track (Warm ochre dash)
                val routeTrackColor = if (selectedRoute != null) MapRouteTrack else MapRouteTrack.copy(alpha = 0.6f)
                val routePoints = listOf(
                    Offset(ave2, st1),
                    Offset(ave3, st1),
                    Offset(ave3, st3),
                    Offset(ave4, st3),
                    Offset(ave4, st4)
                )

                for (i in 0 until routePoints.size - 1) {
                    drawLine(
                        color = routeTrackColor,
                        start = routePoints[i],
                        end = routePoints[i + 1],
                        strokeWidth = (if (selectedRoute != null) 4.5.dp else 2.8.dp).toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                    )
                }

                // Free Contactless Hub Delivery Radius Circle (Suffren Courtyard Hub)
                val hubCenter = Offset(ave3, st2)
                drawCircle(
                    color = MapHubRadiusFill.copy(alpha = 0.5f),
                    radius = 90.dp.toPx() * zoomScale,
                    center = hubCenter
                )
                drawCircle(
                    color = MapHubRadiusStroke,
                    radius = 90.dp.toPx() * zoomScale,
                    center = hubCenter,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f))
                )

                // Live User GPS Location Indicator with Azure Blue Dot & Translucent Ripple
                val userMarkerX = centerX - (width * 0.04f * zoomScale)
                val userMarkerY = centerY - (height * 0.02f * zoomScale)
                val userPos = Offset(userMarkerX, userMarkerY)

                // Expanding translucent radar ring
                drawCircle(
                    color = Color(0xFF2563EB).copy(alpha = pulseAlpha),
                    radius = pulseRadius * zoomScale,
                    center = userPos
                )
                // Center solid blue dot with crisp white border
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

        // Custom Photographic Pins Layer
        filteredPins.forEachIndexed { index, pin ->
            val isSelected = selectedPin?.id == pin.id
            val (baseX, baseY) = when (pin.id) {
                "pin_hub_cafe" -> 160.dp to 220.dp
                "pin_rakesh" -> 220.dp to 180.dp
                "pin_vikram" -> 270.dp to 320.dp
                "pin_priya" -> 80.dp to 300.dp
                "pin_event_photowalk" -> 280.dp to 410.dp
                else -> 140.dp to 450.dp
            }

            val renderedX = baseX + (panOffsetX * 0.4f).dp
            val renderedY = baseY + (panOffsetY * 0.4f).dp

            Box(
                modifier = Modifier.offset(x = renderedX, y = renderedY)
            ) {
                if (pin.type == DiscoveryPinType.PICKUP_HUB) {
                    // Smart Hub Badge Marker (Solid Forest Green)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AccentForestGreen)
                            .border(2.5.dp, PaperPureWhite, CircleShape)
                            .clickable {
                                selectedPin = pin
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
                } else {
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

        // Top Navigation & Filter Bar (Light Themed)
        Column(
            modifier = Modifier
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

                // Neighborhood Tag & GPS Status Indicator
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
                                .background(if (isGpsActive) Color(0xFF2563EB) else AccentForestGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGpsActive) "GPS LIVE · WHITE TOWN" else "WHITE TOWN QUARTER",
                            style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                            color = InkCharcoal
                        )
                    }
                }

                // Locate Me GPS Action Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isGpsActive) AccentMint else PaperPureWhite)
                        .border(1.dp, if (isGpsActive) AccentForestGreen else PaperBorder, CircleShape)
                        .clickable {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            panOffsetX = 0f
                            panOffsetY = 0f
                            zoomScale = 1.1f
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

            // Department / Type Filter Pills Row (Light Theme)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All" to "All",
                    "📸 Cameras" to "Cameras",
                    "🛵 E-Bikes" to "E-Bikes",
                    "🎛️ Audio" to "Audio/Synths",
                    "🔒 Hubs" to "Hubs",
                    "✨ Meetups" to "Meetups"
                ).forEach { (label, key) ->
                    val isSelected = selectedCategoryFilter == key && selectedRoute == null
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
                                selectedRoute = null
                                selectedCategoryFilter = key
                            }
                            .padding(horizontal = 13.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = LoopType.Metadata.copy(
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) InkWhite else InkCharcoal
                        )
                    }
                }
            }

            // Curated Walking Routes Quick Pills (Light Theme)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allRoutes.forEach { route ->
                    val isRouteActive = selectedRoute?.id == route.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isRouteActive) AccentForestGreen else PaperPureWhite)
                            .border(
                                1.dp,
                                if (isRouteActive) AccentForestGreen else PaperBorder,
                                RoundedCornerShape(100.dp)
                            )
                            .clickable {
                                selectedRoute = if (isRouteActive) null else route
                            }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = if (isRouteActive) InkWhite else AccentWarmYellow,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${route.title} (${route.durationMin})",
                                style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                color = if (isRouteActive) InkWhite else InkCharcoal
                            )
                        }
                    }
                }
            }
        }

        // Bottom Discovery Carousel & Direct Actions Drawer (Light Theme)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 86.dp)
        ) {
            LazyRow(
                state = carouselState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPins) { pin ->
                    val isSelected = selectedPin?.id == pin.id

                    Box(
                        modifier = Modifier
                            .width(320.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(PaperPureWhite)
                            .border(
                                1.5.dp,
                                if (isSelected) InkCharcoal else PaperBorderSubtle,
                                RoundedCornerShape(22.dp)
                            )
                            .clickable { selectedPin = pin }
                            .padding(14.dp)
                            .testTag("map_carousel_card_${pin.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Photo / Hub Icon
                                if (pin.imageRes != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    ) {
                                        Image(
                                            painter = painterResource(id = pin.imageRes),
                                            contentDescription = pin.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(AccentForestGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = InkWhite,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = pin.title,
                                            style = LoopType.HeadlineMedium.copy(fontSize = 16.sp),
                                            color = InkCharcoal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = pin.priceOrAttendees,
                                            style = LoopType.PriceSmall.copy(fontSize = 13.5.sp),
                                            color = InkCharcoal
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = pin.locationName,
                                        style = LoopType.Metadata.copy(fontSize = 11.5.sp),
                                        color = InkSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Walking & Cycling Estimates
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                                contentDescription = null,
                                                tint = InkSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = pin.walkingTime,
                                                style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                                                color = InkSecondary
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBike,
                                                contentDescription = null,
                                                tint = InkSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = pin.cyclingTime,
                                                style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                                                color = InkSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Row: Directions & Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Directions Action
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(19.dp))
                                        .background(PaperIvory)
                                        .border(1.dp, PaperBorder, RoundedCornerShape(19.dp))
                                        .clickable {
                                            LocationHelper.openNavigationIntent(
                                                context = context,
                                                latitude = pin.latitude,
                                                longitude = pin.longitude,
                                                label = pin.title
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.NearMe,
                                            contentDescription = null,
                                            tint = InkCharcoal,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Directions",
                                            style = LoopType.ButtonSecondaryLabel.copy(fontSize = 12.sp),
                                            color = InkCharcoal
                                        )
                                    }
                                }

                                // View Piece / Connect Action
                                Box(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(19.dp))
                                        .background(InkCharcoal)
                                        .clickable {
                                            if (pin.rentalItem != null) {
                                                onItemClick(pin.rentalItem)
                                            } else if (pin.owner != null) {
                                                onContactOwner(pin.owner)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (pin.rentalItem != null) "Reserve Piece" else if (pin.type == DiscoveryPinType.PICKUP_HUB) "Hub Info" else "Connect",
                                        style = LoopType.ButtonLabel.copy(fontSize = 12.5.sp),
                                        color = InkWhite
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
