package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SunsetOrange
import com.example.ui.theme.VividPurple
import com.example.ui.theme.WarmSunset

@Composable
fun GlowOrb(
    size: Dp = 240.dp,
    primaryColor: Color = VividPurple,
    secondaryColor: Color = NeonBlue,
    tertiaryColor: Color = WarmSunset,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val baseRadius = (w.coerceAtMost(h) / 2f) * pulseScale

        // Ambient outer blur halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.45f),
                    secondaryColor.copy(alpha = 0.25f),
                    tertiaryColor.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = center,
                radius = baseRadius * 1.5f
            ),
            radius = baseRadius * 1.5f,
            center = center
        )

        // Core 3D sphere gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tertiaryColor.copy(alpha = 0.95f),
                    primaryColor.copy(alpha = 0.90f),
                    secondaryColor.copy(alpha = 0.85f),
                    Color(0xFF0F111E)
                ),
                center = Offset(center.x - baseRadius * 0.3f, center.y - baseRadius * 0.3f),
                radius = baseRadius * 1.1f
            ),
            radius = baseRadius * 0.92f,
            center = center
        )

        // Specular inner reflection / glass highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.65f),
                    ElectricCyan.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(center.x - baseRadius * 0.35f, center.y - baseRadius * 0.35f),
                radius = baseRadius * 0.5f
            ),
            radius = baseRadius * 0.45f,
            center = Offset(center.x - baseRadius * 0.35f, center.y - baseRadius * 0.35f)
        )
    }
}

@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Top-right soft purple glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x388B5CF6), Color(0x153B82F6), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.15f),
                    radius = w * 0.7f
                ),
                center = Offset(w * 0.85f, h * 0.15f),
                radius = w * 0.7f
            )

            // Bottom-left sunset orange ambient light
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x28FF5E62), Color(0x10EC4899), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.75f),
                    radius = w * 0.65f
                ),
                center = Offset(w * 0.15f, h * 0.75f),
                radius = w * 0.65f
            )

            // Center subtle cyan depth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1800E5FF), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.5f
                ),
                center = Offset(w * 0.5f, h * 0.45f),
                radius = w * 0.5f
            )
        }
        content()
    }
}
