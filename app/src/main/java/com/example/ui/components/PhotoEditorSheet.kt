package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LightAccentForestGreen
import com.example.ui.theme.LightAccentMint
import com.example.ui.theme.LightInkCharcoal
import com.example.ui.theme.LoopType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

enum class CropAspectPreset(val label: String, val ratio: Float?) {
    FREE("Free", null),
    SQUARE("1:1", 1f),
    EDITORIAL_4_3("4:3", 4f / 3f),
    WIDE_16_9("16:9", 16f / 9f)
}

data class NormalizedCropRect(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
)

data class PhotoEditState(
    val rotationDegrees: Int = 0,
    val flipHorizontal: Boolean = false,
    val aspectPreset: CropAspectPreset = CropAspectPreset.EDITORIAL_4_3,
    val cropRect: NormalizedCropRect = NormalizedCropRect()
)

data class DraftListingPhoto(
    val id: String,
    val sourceUri: Uri = Uri.EMPTY,
    val existingImageUrl: String? = null,
    val editState: PhotoEditState = PhotoEditState(),
    val previewBitmap: Bitmap? = null,
    val isNewlyAdded: Boolean = false
)

private enum class CropDragHandle {
    NONE,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    MOVE
}

object PhotoBitmapProcessor {

    fun loadOrientedSourceBitmap(
        context: Context,
        uri: Uri,
        maxEdge: Int = 1280
    ): Bitmap? {
        return try {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

            var sampleSize = 1
            while (
                (boundsOpts.outWidth / sampleSize) > maxEdge * 2 ||
                (boundsOpts.outHeight / sampleSize) > maxEdge * 2
            ) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return null

            val exifOrientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            val matrix = Matrix()
            when (exifOrientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(270f)
                    matrix.postScale(-1f, 1f)
                }
            }

            val oriented = if (!matrix.isIdentity) {
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            } else {
                decoded
            }

            scaleDownToMaxEdge(oriented, maxEdge)
        } catch (_: Exception) {
            null
        }
    }

    fun applyOrientation(
        sourceBitmap: Bitmap,
        rotationDegrees: Int,
        flipHorizontal: Boolean
    ): Bitmap {
        val normRot = ((rotationDegrees % 360) + 360) % 360
        if (normRot == 0 && !flipHorizontal) return sourceBitmap
        val matrix = Matrix().apply {
            if (normRot != 0) postRotate(normRot.toFloat())
            if (flipHorizontal) postScale(-1f, 1f)
        }
        return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true)
    }

    fun computeAspectFittedRect(
        bitmapWidth: Int,
        bitmapHeight: Int,
        targetRatio: Float?
    ): NormalizedCropRect {
        if (bitmapWidth <= 0 || bitmapHeight <= 0 || targetRatio == null) {
            return NormalizedCropRect(0f, 0f, 1f, 1f)
        }
        val imgAspect = bitmapWidth.toFloat() / bitmapHeight.toFloat()
        val normRatio = targetRatio / imgAspect
        return if (normRatio >= 1f) {
            val h = (1f / normRatio).coerceIn(0.15f, 1f)
            val top = (1f - h) / 2f
            NormalizedCropRect(0f, top, 1f, top + h)
        } else {
            val w = normRatio.coerceIn(0.15f, 1f)
            val left = (1f - w) / 2f
            NormalizedCropRect(left, 0f, left + w, 1f)
        }
    }

    fun renderTransformedBitmap(
        sourceBitmap: Bitmap,
        editState: PhotoEditState,
        maxOutputEdge: Int = 768
    ): Bitmap {
        val oriented = applyOrientation(
            sourceBitmap = sourceBitmap,
            rotationDegrees = editState.rotationDegrees,
            flipHorizontal = editState.flipHorizontal
        )
        val rect = editState.cropRect
        val x = (rect.left.coerceIn(0f, 0.98f) * oriented.width).roundToInt().coerceIn(0, oriented.width - 1)
        val y = (rect.top.coerceIn(0f, 0.98f) * oriented.height).roundToInt().coerceIn(0, oriented.height - 1)
        val w = ((rect.right - rect.left).coerceIn(0.02f, 1f) * oriented.width).roundToInt()
            .coerceIn(1, oriented.width - x)
        val h = ((rect.bottom - rect.top).coerceIn(0.02f, 1f) * oriented.height).roundToInt()
            .coerceIn(1, oriented.height - y)

        val cropped = if (x == 0 && y == 0 && w == oriented.width && h == oriented.height) {
            oriented
        } else {
            Bitmap.createBitmap(oriented, x, y, w, h)
        }
        return scaleDownToMaxEdge(cropped, maxOutputEdge)
    }

    fun bitmapToBase64DataUri(bitmap: Bitmap, targetMaxBytes: Int = 140_000): String {
        val qualities = intArrayOf(82, 72, 60, 48, 38)
        var bestBytes: ByteArray? = null
        for (q in qualities) {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, q, out)
            val bytes = out.toByteArray()
            bestBytes = bytes
            if (bytes.size <= targetMaxBytes) {
                break
            }
        }
        val finalBytes = bestBytes ?: ByteArray(0)
        val encoded = Base64.encodeToString(finalBytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$encoded"
    }

    fun loadExistingBitmap(imageUrl: String?, maxEdge: Int = 1280): Bitmap? {
        if (imageUrl.isNullOrBlank()) return null
        return try {
            val decoded = when {
                imageUrl.startsWith("data:image", ignoreCase = true) -> {
                    val base64Part = imageUrl.substringAfter("base64,", missingDelimiterValue = "")
                    if (base64Part.isNotBlank()) {
                        val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } else null
                }
                imageUrl.startsWith("http://", ignoreCase = true) ||
                    imageUrl.startsWith("https://", ignoreCase = true) -> {
                    java.net.URL(imageUrl).openStream().use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                else -> null
            } ?: return null
            scaleDownToMaxEdge(decoded, maxEdge)
        } catch (_: Exception) {
            null
        }
    }

    private fun scaleDownToMaxEdge(bitmap: Bitmap, maxEdge: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxEdge && h <= maxEdge) return bitmap
        val ratio = w.toFloat() / h.toFloat()
        val newW = if (ratio >= 1f) maxEdge else (maxEdge * ratio).roundToInt().coerceAtLeast(1)
        val newH = if (ratio >= 1f) (maxEdge / ratio).roundToInt().coerceAtLeast(1) else maxEdge
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}

@Composable
fun PhotoEditorModal(
    draftPhoto: DraftListingPhoto,
    queuePositionText: String? = null,
    hasNextInQueue: Boolean = false,
    onApply: (PhotoEditState, Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler(onBack = onCancel)

    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var sourceBitmap by remember(draftPhoto.id) { mutableStateOf<Bitmap?>(null) }
    var isDecoding by remember(draftPhoto.id) { mutableStateOf(true) }
    var isRendering by remember(draftPhoto.id) { mutableStateOf(false) }

    var rotationDegrees by remember(draftPhoto.id) { mutableIntStateOf(draftPhoto.editState.rotationDegrees) }
    var flipHorizontal by remember(draftPhoto.id) { mutableStateOf(draftPhoto.editState.flipHorizontal) }
    var aspectPreset by remember(draftPhoto.id) { mutableStateOf(draftPhoto.editState.aspectPreset) }
    var cropRect by remember(draftPhoto.id) { mutableStateOf(draftPhoto.editState.cropRect) }

    LaunchedEffect(draftPhoto.id, draftPhoto.sourceUri, draftPhoto.existingImageUrl) {
        isDecoding = true
        val decoded = withContext(Dispatchers.IO) {
            when {
                draftPhoto.sourceUri != Uri.EMPTY ->
                    PhotoBitmapProcessor.loadOrientedSourceBitmap(context, draftPhoto.sourceUri, maxEdge = 1280)
                !draftPhoto.existingImageUrl.isNullOrBlank() ->
                    PhotoBitmapProcessor.loadExistingBitmap(draftPhoto.existingImageUrl, maxEdge = 1280)
                else -> draftPhoto.previewBitmap
            }
        }
        sourceBitmap = decoded
        if (decoded != null && draftPhoto.isNewlyAdded) {
            cropRect = PhotoBitmapProcessor.computeAspectFittedRect(
                bitmapWidth = decoded.width,
                bitmapHeight = decoded.height,
                targetRatio = aspectPreset.ratio
            )
        }
        isDecoding = false
    }

    val orientedBitmap = remember(sourceBitmap, rotationDegrees, flipHorizontal) {
        sourceBitmap?.let {
            PhotoBitmapProcessor.applyOrientation(it, rotationDegrees, flipHorizontal)
        }
    }

    var workspaceSize by remember { mutableStateOf(IntSize.Zero) }
    val canvasPadPx = with(density) { 20.dp.toPx() }
    val handleHitRadiusPx = with(density) { 36.dp.toPx() }

    val imageBounds: Rect? = remember(orientedBitmap, workspaceSize, canvasPadPx) {
        val bmp = orientedBitmap
        if (bmp == null || workspaceSize.width <= 0 || workspaceSize.height <= 0) {
            null
        } else {
            val availW = (workspaceSize.width - canvasPadPx * 2f).coerceAtLeast(1f)
            val availH = (workspaceSize.height - canvasPadPx * 2f).coerceAtLeast(1f)
            val bmpAspect = bmp.width.toFloat() / bmp.height.toFloat()
            val boxAspect = availW / availH
            val drawW: Float
            val drawH: Float
            if (bmpAspect > boxAspect) {
                drawW = availW
                drawH = availW / bmpAspect
            } else {
                drawH = availH
                drawW = availH * bmpAspect
            }
            val left = (workspaceSize.width - drawW) / 2f
            val top = (workspaceSize.height - drawH) / 2f
            Rect(left = left, top = top, right = left + drawW, bottom = top + drawH)
        }
    }

    val liveCropRect by rememberUpdatedState(cropRect)
    val liveImageBounds by rememberUpdatedState(imageBounds)
    val liveAspectPreset by rememberUpdatedState(aspectPreset)
    val liveOrientedBitmap by rememberUpdatedState(orientedBitmap)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightInkCharcoal)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("photo_editor_modal")
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(enabled = !isRendering, onClick = onCancel)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("photo_editor_cancel_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel editing",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cancel",
                    style = LoopType.Metadata.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Edit Photo",
                    style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                    color = Color.White
                )
                if (!queuePositionText.isNullOrBlank()) {
                    Text(
                        text = queuePositionText,
                        style = LoopType.Metadata.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(LightAccentMint)
                    .clickable(enabled = orientedBitmap != null && !isRendering) {
                        val src = sourceBitmap ?: return@clickable
                        isRendering = true
                        val finalState = PhotoEditState(
                            rotationDegrees = rotationDegrees,
                            flipHorizontal = flipHorizontal,
                            aspectPreset = aspectPreset,
                            cropRect = cropRect
                        )
                        coroutineScope.launch {
                            val rendered = withContext(Dispatchers.Default) {
                                PhotoBitmapProcessor.renderTransformedBitmap(src, finalState, maxOutputEdge = 768)
                            }
                            isRendering = false
                            onApply(finalState, rendered)
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("photo_editor_apply_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRendering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        color = LightAccentForestGreen,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Apply photo edits",
                        tint = LightAccentForestGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasNextInQueue) "Apply & Next" else "Apply",
                    style = LoopType.Metadata.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                    color = LightAccentForestGreen
                )
            }
        }

        // Interactive Crop & Frame Canvas Workspace
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged { workspaceSize = it }
                .pointerInput(draftPhoto.id) {
                    var activeHandle = CropDragHandle.NONE
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val bounds = liveImageBounds ?: run {
                                activeHandle = CropDragHandle.NONE
                                return@detectDragGestures
                            }
                            val c = liveCropRect
                            val cLeft = bounds.left + c.left * bounds.width
                            val cTop = bounds.top + c.top * bounds.height
                            val cRight = bounds.left + c.right * bounds.width
                            val cBottom = bounds.top + c.bottom * bounds.height

                            val dTL = (startOffset - Offset(cLeft, cTop)).getDistance()
                            val dTR = (startOffset - Offset(cRight, cTop)).getDistance()
                            val dBL = (startOffset - Offset(cLeft, cBottom)).getDistance()
                            val dBR = (startOffset - Offset(cRight, cBottom)).getDistance()

                            val nearest = listOf(
                                CropDragHandle.TOP_LEFT to dTL,
                                CropDragHandle.TOP_RIGHT to dTR,
                                CropDragHandle.BOTTOM_LEFT to dBL,
                                CropDragHandle.BOTTOM_RIGHT to dBR
                            ).minByOrNull { it.second }

                            activeHandle = when {
                                nearest != null && nearest.second <= handleHitRadiusPx -> nearest.first
                                startOffset.x in cLeft..cRight && startOffset.y in cTop..cBottom ->
                                    CropDragHandle.MOVE
                                else -> CropDragHandle.NONE
                            }
                        },
                        onDragEnd = { activeHandle = CropDragHandle.NONE },
                        onDragCancel = { activeHandle = CropDragHandle.NONE },
                        onDrag = { change, dragAmount ->
                            if (activeHandle == CropDragHandle.NONE) return@detectDragGestures
                            val bounds = liveImageBounds ?: return@detectDragGestures
                            val bmp = liveOrientedBitmap ?: return@detectDragGestures
                            change.consume()

                            val dx = dragAmount.x / bounds.width
                            val dy = dragAmount.y / bounds.height
                            val cur = liveCropRect
                            val minSpan = 0.16f
                            val imgAspect = bmp.width.toFloat() / bmp.height.toFloat()
                            val targetRatio = liveAspectPreset.ratio
                            val normRatio = targetRatio?.let { it / imgAspect }

                            cropRect = when (activeHandle) {
                                CropDragHandle.MOVE -> {
                                    val w = cur.right - cur.left
                                    val h = cur.bottom - cur.top
                                    val newLeft = (cur.left + dx).coerceIn(0f, 1f - w)
                                    val newTop = (cur.top + dy).coerceIn(0f, 1f - h)
                                    NormalizedCropRect(newLeft, newTop, newLeft + w, newTop + h)
                                }
                                CropDragHandle.TOP_LEFT -> resizeWithAnchor(
                                    anchorX = cur.right,
                                    anchorY = cur.bottom,
                                    candX = cur.left + dx,
                                    candY = cur.top + dy,
                                    isLeft = true,
                                    isTop = true,
                                    minSpan = minSpan,
                                    normRatio = normRatio
                                )
                                CropDragHandle.TOP_RIGHT -> resizeWithAnchor(
                                    anchorX = cur.left,
                                    anchorY = cur.bottom,
                                    candX = cur.right + dx,
                                    candY = cur.top + dy,
                                    isLeft = false,
                                    isTop = true,
                                    minSpan = minSpan,
                                    normRatio = normRatio
                                )
                                CropDragHandle.BOTTOM_LEFT -> resizeWithAnchor(
                                    anchorX = cur.right,
                                    anchorY = cur.top,
                                    candX = cur.left + dx,
                                    candY = cur.bottom + dy,
                                    isLeft = true,
                                    isTop = false,
                                    minSpan = minSpan,
                                    normRatio = normRatio
                                )
                                CropDragHandle.BOTTOM_RIGHT -> resizeWithAnchor(
                                    anchorX = cur.left,
                                    anchorY = cur.top,
                                    candX = cur.right + dx,
                                    candY = cur.bottom + dy,
                                    isLeft = false,
                                    isTop = false,
                                    minSpan = minSpan,
                                    normRatio = normRatio
                                )
                                CropDragHandle.NONE -> cur
                            }
                        }
                    )
                }
                .testTag("photo_editor_crop_canvas"),
            contentAlignment = Alignment.Center
        ) {
            when {
                isDecoding -> {
                    CircularProgressIndicator(color = LightAccentMint)
                }
                orientedBitmap != null -> {
                    Image(
                        bitmap = orientedBitmap.asImageBitmap(),
                        contentDescription = "Editing preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    )

                    if (imageBounds != null) {
                        val handleColor = LightAccentMint
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val c = cropRect
                            val cLeft = imageBounds.left + c.left * imageBounds.width
                            val cTop = imageBounds.top + c.top * imageBounds.height
                            val cRight = imageBounds.left + c.right * imageBounds.width
                            val cBottom = imageBounds.top + c.bottom * imageBounds.height
                            val cWidth = cRight - cLeft
                            val cHeight = cBottom - cTop

                            val scrimColor = Color.Black.copy(alpha = 0.60f)
                            // Dimmed overlays around the crop frame (covering image area)
                            if (cTop > imageBounds.top) {
                                drawRect(
                                    color = scrimColor,
                                    topLeft = Offset(imageBounds.left, imageBounds.top),
                                    size = Size(imageBounds.width, cTop - imageBounds.top)
                                )
                            }
                            if (cBottom < imageBounds.bottom) {
                                drawRect(
                                    color = scrimColor,
                                    topLeft = Offset(imageBounds.left, cBottom),
                                    size = Size(imageBounds.width, imageBounds.bottom - cBottom)
                                )
                            }
                            if (cLeft > imageBounds.left) {
                                drawRect(
                                    color = scrimColor,
                                    topLeft = Offset(imageBounds.left, cTop),
                                    size = Size(cLeft - imageBounds.left, cHeight)
                                )
                            }
                            if (cRight < imageBounds.right) {
                                drawRect(
                                    color = scrimColor,
                                    topLeft = Offset(cRight, cTop),
                                    size = Size(imageBounds.right - cRight, cHeight)
                                )
                            }

                            // Crisp border
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(cLeft, cTop),
                                size = Size(cWidth, cHeight),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Rule of thirds grid
                            val gridColor = Color.White.copy(alpha = 0.38f)
                            val gridStroke = 1.dp.toPx()
                            for (i in 1..2) {
                                val gx = cLeft + cWidth * (i / 3f)
                                drawLine(gridColor, Offset(gx, cTop), Offset(gx, cBottom), gridStroke)
                                val gy = cTop + cHeight * (i / 3f)
                                drawLine(gridColor, Offset(cLeft, gy), Offset(cRight, gy), gridStroke)
                            }

                            // Corner L-Handles
                            val handleLen = min(22.dp.toPx(), min(cWidth, cHeight) * 0.35f)
                            val handleStroke = 3.5.dp.toPx()
                            val path = Path().apply {
                                // Top-left
                                moveTo(cLeft, cTop + handleLen)
                                lineTo(cLeft, cTop)
                                lineTo(cLeft + handleLen, cTop)
                                // Top-right
                                moveTo(cRight - handleLen, cTop)
                                lineTo(cRight, cTop)
                                lineTo(cRight, cTop + handleLen)
                                // Bottom-left
                                moveTo(cLeft, cBottom - handleLen)
                                lineTo(cLeft, cBottom)
                                lineTo(cLeft + handleLen, cBottom)
                                // Bottom-right
                                moveTo(cRight - handleLen, cBottom)
                                lineTo(cRight, cBottom)
                                lineTo(cRight, cBottom - handleLen)
                            }
                            drawPath(path, color = handleColor, style = Stroke(width = handleStroke))
                        }
                    }
                }
                else -> {
                    Text(
                        text = "Unable to decode selected image.",
                        style = LoopType.BodyMedium,
                        color = Color.White
                    )
                }
            }
        }

        // Bottom Controls Dock (Aspect Ratio Presets + Transform Tools)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141817))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ASPECT RATIO & CROP",
                style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.65f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CropAspectPreset.values().forEach { preset ->
                    val isSelected = aspectPreset == preset
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) LightAccentMint else Color.White.copy(alpha = 0.1f))
                            .border(
                                1.dp,
                                if (isSelected) LightAccentMint else Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(100.dp)
                            )
                            .clickable {
                                aspectPreset = preset
                                val bmp = orientedBitmap
                                if (bmp != null) {
                                    cropRect = PhotoBitmapProcessor.computeAspectFittedRect(
                                        bitmapWidth = bmp.width,
                                        bitmapHeight = bmp.height,
                                        targetRatio = preset.ratio
                                    )
                                }
                            }
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                            .testTag("photo_editor_ratio_${preset.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.label,
                            style = LoopType.Metadata.copy(
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) LightAccentForestGreen else Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EditorActionPill(
                    icon = Icons.AutoMirrored.Filled.RotateRight,
                    label = "Rotate 90°",
                    onClick = {
                        rotationDegrees = (rotationDegrees + 90) % 360
                        val src = sourceBitmap ?: return@EditorActionPill
                        val swapped = rotationDegrees == 90 || rotationDegrees == 270
                        val nextW = if (swapped) src.height else src.width
                        val nextH = if (swapped) src.width else src.height
                        cropRect = PhotoBitmapProcessor.computeAspectFittedRect(
                            bitmapWidth = nextW,
                            bitmapHeight = nextH,
                            targetRatio = aspectPreset.ratio
                        )
                    },
                    testTag = "photo_editor_rotate_btn",
                    modifier = Modifier.weight(1f)
                )

                EditorActionPill(
                    icon = Icons.Default.Flip,
                    label = if (flipHorizontal) "Flipped" else "Flip",
                    isHighlighted = flipHorizontal,
                    onClick = {
                        flipHorizontal = !flipHorizontal
                        cropRect = NormalizedCropRect(
                            left = (1f - cropRect.right).coerceIn(0f, 1f),
                            top = cropRect.top,
                            right = (1f - cropRect.left).coerceIn(0f, 1f),
                            bottom = cropRect.bottom
                        )
                    },
                    testTag = "photo_editor_flip_btn",
                    modifier = Modifier.weight(1f)
                )

                EditorActionPill(
                    icon = Icons.Default.Refresh,
                    label = "Reset",
                    onClick = {
                        rotationDegrees = 0
                        flipHorizontal = false
                        aspectPreset = CropAspectPreset.EDITORIAL_4_3
                        val src = sourceBitmap
                        cropRect = if (src != null) {
                            PhotoBitmapProcessor.computeAspectFittedRect(
                                bitmapWidth = src.width,
                                bitmapHeight = src.height,
                                targetRatio = CropAspectPreset.EDITORIAL_4_3.ratio
                            )
                        } else {
                            NormalizedCropRect()
                        }
                    },
                    testTag = "photo_editor_reset_btn",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EditorActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isHighlighted: Boolean = false,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isHighlighted) LightAccentMint else Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isHighlighted) LightAccentForestGreen else Color.White,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = LoopType.Metadata.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            color = if (isHighlighted) LightAccentForestGreen else Color.White,
            maxLines = 1
        )
    }
}

private fun resizeWithAnchor(
    anchorX: Float,
    anchorY: Float,
    candX: Float,
    candY: Float,
    isLeft: Boolean,
    isTop: Boolean,
    minSpan: Float,
    normRatio: Float?
): NormalizedCropRect {
    val maxW = if (isLeft) anchorX else (1f - anchorX)
    val maxH = if (isTop) anchorY else (1f - anchorY)

    var w = if (isLeft) (anchorX - candX) else (candX - anchorX)
    var h = if (isTop) (anchorY - candY) else (candY - anchorY)

    if (normRatio == null) {
        w = w.coerceIn(minSpan, maxW.coerceAtLeast(minSpan))
        h = h.coerceIn(minSpan, maxH.coerceAtLeast(minSpan))
    } else {
        val absRatio = abs(normRatio).coerceAtLeast(0.05f)
        val minWForRatio = maxOf(minSpan, minSpan * absRatio)
        val maxWForBounds = minOf(maxW, maxH * absRatio).coerceAtLeast(minWForRatio)
        w = w.coerceIn(minWForRatio, maxWForBounds)
        h = (w / absRatio).coerceIn(minSpan, maxH.coerceAtLeast(minSpan))
    }

    val left = if (isLeft) (anchorX - w).coerceIn(0f, anchorX) else anchorX
    val right = if (isLeft) anchorX else (anchorX + w).coerceIn(anchorX, 1f)
    val top = if (isTop) (anchorY - h).coerceIn(0f, anchorY) else anchorY
    val bottom = if (isTop) anchorY else (anchorY + h).coerceIn(anchorY, 1f)
    return NormalizedCropRect(left = left, top = top, right = right, bottom = bottom)
}
