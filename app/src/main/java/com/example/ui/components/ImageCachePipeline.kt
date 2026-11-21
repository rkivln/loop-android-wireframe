package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.models.RentalItem
import com.example.ui.theme.PaperPureWhite
import com.example.ui.theme.PaperWarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * High-speed image decoding, LRU memory caching, preloading, and shimmer placeholder pipeline.
 */
object ImageCachePipeline {
    private const val MAX_CACHE_BYTES = 24 * 1024 * 1024 // 24 MB decoded Base64 cache
    private val decodeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val decodeSemaphore = Semaphore(permits = 3)

    private val decodedBitmapCache = object : LruCache<String, ImageBitmap>(MAX_CACHE_BYTES) {
        override fun sizeOf(key: String, value: ImageBitmap): Int {
            // Estimate RGBA/RGB565 memory footprint in bytes (at least 1KB)
            return (value.width * value.height * 2).coerceAtLeast(1024)
        }
    }

    private fun buildCacheKey(dataUri: String, maxEdgePx: Int): String {
        val bucket = when {
            maxEdgePx <= 320 -> 320
            maxEdgePx <= 720 -> 720
            else -> 1280
        }
        return "${dataUri.hashCode()}_${dataUri.length}_$bucket"
    }

    fun getCachedDataUriBitmap(dataUri: String?, maxEdgePx: Int = 720): ImageBitmap? {
        if (dataUri.isNullOrBlank() || !dataUri.startsWith("data:image", ignoreCase = true)) {
            return null
        }
        return decodedBitmapCache.get(buildCacheKey(dataUri, maxEdgePx))
    }

    suspend fun decodeDataUriBitmapAsync(
        dataUri: String,
        maxEdgePx: Int = 720
    ): ImageBitmap? {
        val cacheKey = buildCacheKey(dataUri, maxEdgePx)
        decodedBitmapCache.get(cacheKey)?.let { return it }

        return withContext(Dispatchers.Default) {
            decodedBitmapCache.get(cacheKey)?.let { return@withContext it }

            decodeSemaphore.withPermit {
                decodedBitmapCache.get(cacheKey)?.let { return@withPermit it }
                try {
                    val base64Part = dataUri.substringAfter("base64,", missingDelimiterValue = "")
                    if (base64Part.isBlank()) return@withPermit null
                    val bytes = Base64.decode(base64Part, Base64.DEFAULT)

                    val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOpts)
                    if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) {
                        return@withPermit null
                    }

                    val targetBucket = when {
                        maxEdgePx <= 320 -> 320
                        maxEdgePx <= 720 -> 720
                        else -> 1280
                    }
                    var sampleSize = 1
                    while (
                        (boundsOpts.outWidth / sampleSize) > targetBucket * 2 ||
                        (boundsOpts.outHeight / sampleSize) > targetBucket * 2
                    ) {
                        sampleSize *= 2
                    }

                    val isJpeg = dataUri.startsWith("data:image/jpeg", ignoreCase = true) ||
                        dataUri.startsWith("data:image/jpg", ignoreCase = true)

                    val decodeOpts = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = if (isJpeg) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
                    }
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
                        ?: return@withPermit null
                    val imageBitmap = bitmap.asImageBitmap()
                    decodedBitmapCache.put(cacheKey, imageBitmap)
                    imageBitmap
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    /**
     * Warm Coil's memory/disk cache and our Base64 LRU cache for top catalog listings
     * as soon as cloud items arrive on startup.
     */
    fun prefetchCatalogImages(
        context: Context,
        items: List<RentalItem>,
        maxItems: Int = 10
    ) {
        if (items.isEmpty()) return
        val appContext = context.applicationContext
        val imageLoader = appContext.imageLoader
        val urlsToWarm = items
            .take(maxItems)
            .flatMap { item ->
                listOfNotNull(item.imageUrl) + item.imageUrls.take(1)
            }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        urlsToWarm.forEach { url ->
            when {
                url.startsWith("data:image", ignoreCase = true) -> {
                    if (getCachedDataUriBitmap(url, 720) == null) {
                        decodeScope.launch {
                            decodeDataUriBitmapAsync(url, 720)
                        }
                    }
                }
                url.startsWith("http://", ignoreCase = true) ||
                    url.startsWith("https://", ignoreCase = true) -> {
                    val req = ImageRequest.Builder(appContext)
                        .data(url)
                        .size(720, 540)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                    imageLoader.enqueue(req)
                }
            }
        }
    }
}

@Composable
fun ShimmerImageBox(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "photo_shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = -320f,
        targetValue = 980f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "photo_shimmer_offset"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            PaperWarm,
            PaperPureWhite.copy(alpha = 0.85f),
            PaperWarm
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 320f, 320f)
    )

    Box(modifier = modifier.background(shimmerBrush))
}
