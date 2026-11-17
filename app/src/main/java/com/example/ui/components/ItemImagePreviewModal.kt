package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.RentalItem
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import kotlinx.coroutines.launch

@Composable
fun ItemImagePreviewModal(
    item: RentalItem,
    initialPage: Int = 0,
    onDismiss: () -> Unit,
    onViewDetails: (() -> Unit)? = null
) {
    val galleryUrls = remember(item.id, item.imageUrl, item.imageUrls) {
        val urls = if (item.imageUrls.isNotEmpty()) item.imageUrls else listOfNotNull(item.imageUrl)
        if (urls.isEmpty()) listOf("") else urls
    }
    val clampedInitial = initialPage.coerceIn(0, (galleryUrls.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(initialPage = clampedInitial, pageCount = { galleryUrls.size })
    val coroutineScope = rememberCoroutineScope()

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(pagerState.currentPage) {
        zoomScale = 1f
        panOffset = Offset.Zero
    }

    val animatedScale by animateFloatAsState(
        targetValue = zoomScale,
        animationSpec = spring(stiffness = 380f),
        label = "preview_zoom_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler(onBack = onDismiss)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .testTag("item_image_preview_modal")
        ) {
            // Main Image Pager Canvas
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val maxW = with(LocalDensity.current) { maxWidth.toPx() }
                val maxH = with(LocalDensity.current) { maxHeight.toPx() }

                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = zoomScale <= 1.05f,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("image_preview_pager")
                ) { page ->
                    val currentUrl = galleryUrls[page].takeIf { it.isNotBlank() }
                    val isCurrentPage = page == pagerState.currentPage

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(page) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (zoomScale > 1.05f) {
                                            zoomScale = 1f
                                            panOffset = Offset.Zero
                                        } else {
                                            zoomScale = 2.2f
                                        }
                                    }
                                )
                            }
                            .then(
                                if (isCurrentPage && zoomScale > 1.05f) {
                                    Modifier.pointerInput(zoomScale) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            val newScale = (zoomScale * zoom).coerceIn(1f, 3.5f)
                                            zoomScale = newScale
                                            if (newScale <= 1.05f) {
                                                zoomScale = 1f
                                                panOffset = Offset.Zero
                                            } else {
                                                val maxX = (maxW * (newScale - 1f)) / 2f
                                                val maxY = (maxH * (newScale - 1f)) / 2f
                                                val nextOffset = panOffset + pan
                                                panOffset = Offset(
                                                    x = nextOffset.x.coerceIn(-maxX, maxX),
                                                    y = nextOffset.y.coerceIn(-maxY, maxY)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ListingPhoto(
                            imageUrl = currentUrl,
                            fallbackRes = item.primaryImageRes,
                            contentDescription = "${item.title} image ${page + 1}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 84.dp)
                                .graphicsLayer {
                                    if (isCurrentPage) {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                        translationX = panOffset.x
                                        translationY = panOffset.y
                                    }
                                }
                        )
                    }
                }
            }

            // Top Darkroom Chrome
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .testTag("image_preview_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close preview",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.title,
                        style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append("₹${item.pricePerDay}/day")
                            if (item.location.isNotBlank()) append(" · ${item.location}")
                        },
                        style = LoopType.Metadata.copy(fontSize = 11.5.sp),
                        color = Color.White.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (zoomScale > 1.05f) {
                                zoomScale = 1f
                                panOffset = Offset.Zero
                            } else {
                                zoomScale = 2.2f
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .testTag("image_preview_zoom_button")
                    ) {
                        Icon(
                            imageVector = if (zoomScale > 1.05f) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                            contentDescription = if (zoomScale > 1.05f) "Reset zoom" else "Zoom in",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom Thumbnails, Counter & Quick Action
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (zoomScale > 1.05f) {
                            "Zoom ${"%.1f".format(zoomScale)}x · Drag to pan"
                        } else {
                            "Double-tap photo to zoom"
                        },
                        style = LoopType.Metadata.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.72f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("image_preview_counter")
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${galleryUrls.size}",
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                if (galleryUrls.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(galleryUrls) { idx, url ->
                            val isSelected = idx == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) AccentWarmYellow else Color.White.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(idx)
                                        }
                                    }
                                    .testTag("preview_thumbnail_$idx")
                            ) {
                                ListingPhoto(
                                    imageUrl = url.takeIf { it.isNotBlank() },
                                    fallbackRes = item.primaryImageRes,
                                    contentDescription = "Photo ${idx + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                if (onViewDetails != null) {
                    PrimaryCTA(
                        text = if (item.availableToday) "View Details & Reserve" else "View Listing Details",
                        onClick = {
                            onDismiss()
                            onViewDetails()
                        },
                        backgroundColor = Color.White,
                        contentColor = InkCharcoal,
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        testTag = "image_preview_view_details_button"
                    )
                }
            }
        }
    }
}
