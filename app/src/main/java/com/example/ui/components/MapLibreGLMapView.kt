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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
    centerLat: Double = 11.9338, // White Town Promenade, Puducherry
    centerLng: Double = 79.8350,
    zoom: Double = 14.8
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    var currentStyleKey by remember { mutableStateOf("vector") } // "vector" or "osm"

    val bridge = remember(activityPoints) {
        MapLibreBridge(
            onPinClicked = { pinId ->
                activityPoints.find { it.id == pinId }?.let { pin ->
                    onPointSelected(pin)
                }
            },
            onEmptyMapClicked = {
                onMapClick()
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
                val emoji = when (point.type.name) {
                    "COMMUNITY_EVENT" -> "📸"
                    "HELP_REQUEST" -> "🤝"
                    "STUDY_GROUP" -> "📚"
                    "PICKUP_HUB" -> "🔒"
                    "USER_HOST" -> "🛠️"
                    else -> "📍"
                }
                put("emoji", emoji)
            }
            array.put(obj)
        }
        array.toString()
    }

    // Push updated pins to MapLibre when ready or when points change
    LaunchedEffect(pinsJson, isMapReady) {
        if (isMapReady && webViewRef != null) {
            val escapedJson = pinsJson.replace("\\", "\\\\").replace("'", "\\'")
            webViewRef?.evaluateJavascript("window.setPinsJson('$escapedJson');", null)
        }
    }

    // Fly to selected pin when it changes
    LaunchedEffect(selectedPin?.id, isMapReady) {
        if (isMapReady && selectedPin != null && webViewRef != null) {
            webViewRef?.evaluateJavascript("window.selectPin('${selectedPin.id}');", null)
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
                webViewRef?.destroy()
                webViewRef = null
            }
        }

        // MapLibre GL Badge and Attribution Pill
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(PaperPureWhite.copy(alpha = 0.94f))
                .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                .shadow(2.dp, RoundedCornerShape(100.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .testTag("maplibre_badge"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentForestGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MapLibre GL • OpenStreetMap",
                    style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = InkCharcoal
                )
            }
        }

        // Floating Control Buttons: Recenter & Style Switcher
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Style toggle button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorder, CircleShape)
                    .shadow(4.dp, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) {
                        currentStyleKey = if (currentStyleKey == "vector") "osm" else "vector"
                        webViewRef?.evaluateJavascript("window.setMapStyle('$currentStyleKey');", null)
                    }
                    .testTag("maplibre_style_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Toggle Map Layer Style",
                    tint = if (currentStyleKey == "osm") AccentForestGreen else InkCharcoal,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Recenter to Puducherry White Town Promenade
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorder, CircleShape)
                    .shadow(4.dp, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) {
                        webViewRef?.evaluateJavascript(
                            "window.flyToCoords($centerLat, $centerLng, 15.2);",
                            null
                        )
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
