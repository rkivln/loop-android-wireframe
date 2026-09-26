package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceGlass

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = SurfaceCard,
    borderColor: Color = GlassBorderSubtle,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    gradientOverlay: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier
            .clip(shape)
            .clickable(onClick = onClick)
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

@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    selectedColor: Color = Color(0xFF388BFF),
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(50)
    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0x331F2438), Color(0x22181B28))
        )
    }
    val borderColor = if (isSelected) Color(0x668B5CF6) else GlassBorderSubtle

    Box(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .background(backgroundBrush, shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        content()
    }
}
