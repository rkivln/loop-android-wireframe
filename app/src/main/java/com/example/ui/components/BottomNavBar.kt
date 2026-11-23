package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.LoopTheme
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperPureWhite
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

enum class LoopTab(
    val title: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector,
    val testTag: String
) {
    CATALOG("Catalog", Icons.Outlined.Home, Icons.Filled.Home, "nav_catalog"),
    OBJECTS("Objects", Icons.Outlined.Explore, Icons.Filled.Explore, "nav_objects"),
    HOUSES("Houses", Icons.Outlined.Apartment, Icons.Filled.Apartment, "nav_houses"),
    PUBLISH("Publish", Icons.Outlined.AddCircleOutline, Icons.Filled.AddCircleOutline, "nav_publish"),
    STUDIO("Studio", Icons.AutoMirrored.Outlined.Chat, Icons.AutoMirrored.Filled.Chat, "nav_studio"),
    PROFILE("Profile", Icons.Outlined.Person, Icons.Filled.Person, "nav_profile")
}

private val LiquidPillShape = RoundedCornerShape(percent = 50)

@Composable
fun BottomNavBar(
    currentTab: LoopTab,
    onTabSelected: (LoopTab) -> Unit,
    unreadCount: Int = 0,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
    val fallbackBackdrop = rememberLayerBackdrop()
    val resolvedBackdrop = backdrop ?: fallbackBackdrop
    val isDark = LoopTheme.colors.isDark
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val tabs = LoopTab.entries
    val tabCount = tabs.size
    val selectedIndex = currentTab.ordinal.coerceIn(0, tabCount - 1)

    // Spring-driven fractional index for smooth jelly capsule glide across tabs
    val animatedIndex = remember { Animatable(selectedIndex.toFloat()) }
    // Press / scrub lift progress (0f = resting, 1f = dragging/pressing)
    val pressScale = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(selectedIndex) {
        if (!isDragging) {
            animatedIndex.animateTo(
                targetValue = selectedIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = 0.64f,
                    stiffness = 320f
                )
            )
        }
    }

    val outerSurfaceColor = if (isDark) {
        Color(0xFF1A1E22).copy(alpha = 0.56f)
    } else {
        PaperPureWhite.copy(alpha = 0.54f)
    }

    val outerRimBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.24f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.92f),
                PaperBorderSubtle.copy(alpha = 0.55f)
            )
        )
    }

    val activeCapsuleFill = if (isDark) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color(0xFFE7E4DC).copy(alpha = 0.68f)
    }

    val activeCapsuleBorder = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.95f),
                PaperBorderSubtle.copy(alpha = 0.35f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("bottom_nav_bar"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .height(66.dp)
                .drawBackdrop(
                    backdrop = resolvedBackdrop,
                    shape = { LiquidPillShape },
                    effects = {
                        vibrancy()
                        blur(18f.dp.toPx())
                        lens(
                            refractionHeight = 14f.dp.toPx(),
                            refractionAmount = 24f.dp.toPx(),
                            chromaticAberration = true
                        )
                    },
                    highlight = { Highlight.Default },
                    shadow = { Shadow.Default },
                    onDrawSurface = {
                        drawRect(outerSurfaceColor)
                    }
                )
                .border(1.dp, outerRimBrush, LiquidPillShape)
                .clip(LiquidPillShape)
                .padding(4.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val tabWidthDp = maxWidth / tabCount
                val tabWidthPx = with(density) { tabWidthDp.toPx() }.coerceAtLeast(1f)

                val currentPosition = animatedIndex.value.coerceIn(0f, (tabCount - 1).toFloat())
                val travelVelocity = abs(animatedIndex.velocity)
                // Velocity-coupled horizontal stretch & subtle vertical squash for jelly elasticity
                val jellyStretch = (travelVelocity * 0.036f).coerceIn(0f, 0.28f)
                val capsuleScaleX = 1f + jellyStretch + (pressScale.value * 0.06f)
                val capsuleScaleY = (1f - jellyStretch * 0.30f) + (pressScale.value * 0.08f)

                // Gesture layer supporting smooth horizontal scrubbing across all 5 tabs
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(tabCount, tabWidthPx) {
                            var dragStartPos = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { startOffset ->
                                    isDragging = true
                                    dragStartPos = (startOffset.x / tabWidthPx - 0.5f)
                                        .coerceIn(0f, (tabCount - 1).toFloat())
                                    scope.launch {
                                        pressScale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                                        animatedIndex.snapTo(dragStartPos)
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    val nextPos = (animatedIndex.value + dragAmount / tabWidthPx)
                                        .coerceIn(0f, (tabCount - 1).toFloat())
                                    scope.launch {
                                        animatedIndex.snapTo(nextPos)
                                    }
                                },
                                onDragEnd = {
                                    isDragging = false
                                    val targetIndex = animatedIndex.value
                                        .roundToInt()
                                        .coerceIn(0, tabCount - 1)
                                    val targetTab = tabs[targetIndex]
                                    scope.launch {
                                        pressScale.animateTo(0f, spring(dampingRatio = 0.65f, stiffness = 360f))
                                    }
                                    if (targetTab != currentTab) {
                                        onTabSelected(targetTab)
                                    } else {
                                        scope.launch {
                                            animatedIndex.animateTo(
                                                selectedIndex.toFloat(),
                                                spring(dampingRatio = 0.64f, stiffness = 320f)
                                            )
                                        }
                                    }
                                },
                                onDragCancel = {
                                    isDragging = false
                                    scope.launch {
                                        pressScale.animateTo(0f, spring(dampingRatio = 0.65f, stiffness = 360f))
                                        animatedIndex.animateTo(
                                            selectedIndex.toFloat(),
                                            spring(dampingRatio = 0.64f, stiffness = 320f)
                                        )
                                    }
                                }
                            )
                        }
                ) {
                    // Sliding Liquid Glass Jelly Capsule Indicator
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (currentPosition * tabWidthPx).roundToInt(),
                                    y = 0
                                )
                            }
                            .width(tabWidthDp)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = capsuleScaleX
                                scaleY = capsuleScaleY
                            }
                            .drawBackdrop(
                                backdrop = resolvedBackdrop,
                                shape = { LiquidPillShape },
                                effects = {
                                    vibrancy()
                                    lens(
                                        refractionHeight = 10f.dp.toPx(),
                                        refractionAmount = 14f.dp.toPx()
                                    )
                                },
                                highlight = { Highlight.Default },
                                onDrawSurface = {
                                    drawRect(activeCapsuleFill)
                                }
                            )
                            .border(0.8.dp, activeCapsuleBorder, LiquidPillShape)
                    )

                    // Foreground Tab Items
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val selected = currentTab == tab
                            val proximity = (1f - abs(currentPosition - index)).coerceIn(0f, 1f)
                            val tint = lerp(InkMuted, InkCharcoal, proximity)
                            val itemScale = 1f + (0.06f * proximity)

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .minimumInteractiveComponentSize()
                                    .clip(LiquidPillShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            if (tab == LoopTab.PUBLISH) {
                                                // Tactile jelly pulse while opening listing creator
                                                scope.launch {
                                                    pressScale.animateTo(0.85f, tween(90))
                                                    pressScale.animateTo(
                                                        0f,
                                                        spring(dampingRatio = 0.55f, stiffness = 340f)
                                                    )
                                                }
                                            }
                                            onTabSelected(tab)
                                        }
                                    )
                                    .testTag(tab.testTag),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = itemScale
                                        scaleY = itemScale
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selected || proximity > 0.55f) tab.filledIcon else tab.outlinedIcon,
                                        contentDescription = tab.title,
                                        tint = tint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    if (tab == LoopTab.STUDIO && unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(AccentWarmYellow)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = tab.title,
                                    style = LoopType.Metadata.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = if (selected || proximity > 0.55f) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = tint,
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
