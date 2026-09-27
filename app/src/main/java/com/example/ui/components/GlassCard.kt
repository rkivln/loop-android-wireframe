package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceInteractive

/**
 * Standard Surface Card with disciplined border, dark fill, and touch ripple.
 * Replaces heavy glassmorphism with crisp, modern product card surfaces.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = SurfaceCard,
    borderColor: Color = BorderSubtle,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    gradientOverlay: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val clickableModifier = if (onClick != null) {
        Modifier
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0x33FFFFFF)),
                onClick = onClick
            )
    } else {
        Modifier.clip(shape)
    }

    Box(
        modifier = modifier
            .then(clickableModifier)
            .background(backgroundColor, shape)
            .then(
                if (gradientOverlay != null) {
                    Modifier.background(gradientOverlay, shape)
                } else Modifier
            )
            .border(borderWidth, borderColor, shape)
    ) {
        content()
    }
}

/**
 * Clean interactive pill for tags, filter chips, and segmented status indicators.
 */
@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    selectedColor: Color = BrandPrimary,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val backgroundColor = if (isSelected) selectedColor else SurfaceInteractive
    val borderColor = if (isSelected) Color(0x664F46E5) else BorderSubtle

    Box(
        modifier = modifier
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color(0x22FFFFFF)),
                onClick = onClick
            )
            .background(backgroundColor, shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        content()
    }
}
