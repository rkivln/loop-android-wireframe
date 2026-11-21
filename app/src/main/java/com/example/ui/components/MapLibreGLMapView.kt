package com.example.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.models.DiscoveryPinItem
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperPureWhite
import org.json.JSONArray
import org.json.JSONObject

/**
 * JavaScript interface bridge for MapLibre GL to communicate with Jetpack Compose.
 */
class MapLibreBridge(
    private val onPinClicked: (String) -> Unit,
    private val onEmptyMapClicked: () -> Unit,
    private val onReady: () -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onPinClick(pinId: String) {
        mainHandler.post {
            onPinClicked(pinId)
        }
    }

    @JavascriptInterface
    fun onMapClick() {
        mainHandler.post {
            onEmptyMapClicked()
        }
    }

    @JavascriptInterface
    fun onMapReady() {
        mainHandler.post {
            onReady()
        }
    }
}

/**
 * High-performance MapLibre GL Map View integrated into Jetpack Compose.
 * Uses MapLibre GL JS engine with OpenStreetMap & open vector tiles.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapLibreGLMapView(
    modifier: Modifier = Modifier,
    activityPoints: List<DiscoveryPinItem>,
    selectedPin: DiscoveryPinItem? = null,
    onPointSelected: (DiscoveryPinItem) -> Unit = {},
    onMapClick: () -> Unit = {},
    onRequestGpsRecenter: () -> Unit = {},
    centerLat: Double? = null,
    centerLng: Double? = null,
    zoom: Double = 14.8,
    styleKey: String = "vector",
    recenterTrigger: Int = 0,
    showOverlayControls: Boolean = true,
    showAttributionBadge: Boolean = true,
    attributionTopPadding: Dp = 114.dp
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    var internalStyleKey by remember { mutableStateOf(styleKey) }

    val latestPoints by rememberUpdatedState(activityPoints)
    val latestOnPointSelected by rememberUpdatedState(onPointSelected)
    val latestOnMapClick by rememberUpdatedState(onMapClick)

    fun evalJs(script: String) {
        val view = webViewRef ?: return
        runCatching {
            view.evaluateJavascript(script, null)
        }
    }

    LaunchedEffect(styleKey) {
        internalStyleKey = styleKey
    }

    val bridge = remember {
        MapLibreBridge(
            onPinClicked = { pinId ->
                latestPoints.find { it.id == pinId }?.let { pin ->
                    latestOnPointSelected(pin)
                }
            },
            onEmptyMapClicked = {
                latestOnMapClick()
            },
            onReady = {
                isMapReady = true
            }
        )
    }

    // Convert activity points to JSON for MapLibre
    val pinsJson = remember(activityPoints) {
        val array = JSONArray()
        activityPoints.forEach { point ->
            val obj = JSONObject().apply {
                put("id", point.id)
                put("title", point.title)
                put("subtitle", point.subtitle)
                put("type", point.type.name)
                put("latitude", point.latitude)
                put("longitude", point.longitude)
                put("locationName", point.locationName)
                put("tag", point.tag)
                put("priceOrAttendees", point.priceOrAttendees)
            }
            array.put(obj)
        }
        array.toString()
    }

    // Push updated pins to MapLibre when ready or when points change
    LaunchedEffect(pinsJson, isMapReady) {
        if (isMapReady) {
            val escapedJson = pinsJson.replace("\\", "\\\\").replace("'", "\\'")
            evalJs("window.setPinsJson('$escapedJson');")
        }
    }

    // Apply requested style when map becomes ready or styleKey changes
    LaunchedEffect(internalStyleKey, isMapReady) {
        if (isMapReady) {
            evalJs("window.setMapStyle('$internalStyleKey');")
        }
    }

    // Auto-center MapLibre on live GPS coordinates when active
    LaunchedEffect(centerLat, centerLng, isMapReady) {
        if (isMapReady && centerLat != null && centerLng != null) {
            evalJs("window.setUserLocation($centerLat, $centerLng, true);")
        }
    }

    // Trigger explicit recenter from external unified GPS button
    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0 && isMapReady && centerLat != null && centerLng != null) {
            evalJs("window.flyToCoords($centerLat, $centerLng, $zoom);")
        }
    }

    // Fly to selected pin when it changes
    LaunchedEffect(selectedPin?.id, isMapReady) {
        if (isMapReady && selectedPin != null) {
            evalJs("window.selectPin('${selectedPin.id}');")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("maplibre_gl_container")
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        allowFileAccess = true
                        allowContentAccess = true
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapReady = true
                            val escapedJson = pinsJson.replace("\\", "\\\\").replace("'", "\\'")
                            view?.evaluateJavascript("window.setPinsJson('$escapedJson');", null)
                        }
                    }
                    addJavascriptInterface(bridge, "AndroidBridge")
                    loadUrl("file:///android_asset/maplibre/map.html")
                    webViewRef = this
                }
            },
            update = { view ->
                webViewRef = view
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                val view = webViewRef
                webViewRef = null
                isMapReady = false
                if (view != null) {
                    runCatching {
                        view.stopLoading()
                        view.removeJavascriptInterface("AndroidBridge")
                        (view.parent as? ViewGroup)?.removeView(view)
                        view.destroy()
                    }
                }
            }
        }

        if (showAttributionBadge) {
            // Subtle OpenStreetMap & MapLibre attribution pill inset cleanly below top controls
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(top = attributionTopPadding, start = 16.dp)
                    .shadow(2.dp, RoundedCornerShape(100.dp))
                    .clip(RoundedCornerShape(100.dp))
                    .background(PaperPureWhite.copy(alpha = 0.92f))
                    .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("maplibre_badge"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AccentForestGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (internalStyleKey == "osm") "OpenStreetMap" else "OpenFreeMap • OSM",
                        style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                        color = InkCharcoal
                    )
                }
            }
        }

        if (showOverlayControls) {
            // Floating Control Buttons for standalone usage: Recenter & Style Switcher
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .minimumInteractiveComponentSize()
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorder, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) {
                            internalStyleKey = if (internalStyleKey == "vector") "osm" else "vector"
                        }
                        .testTag("maplibre_style_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Toggle Map Layer Style",
                        tint = if (internalStyleKey == "osm") AccentForestGreen else InkCharcoal,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .minimumInteractiveComponentSize()
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorder, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) {
                            if (centerLat != null && centerLng != null) {
                                webViewRef?.evaluateJavascript(
                                    "window.flyToCoords($centerLat, $centerLng, $zoom);",
                                    null
                                )
                            } else {
                                onRequestGpsRecenter()
                            }
                        }
                        .testTag("maplibre_recenter_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter Map",
                        tint = AccentForestGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
