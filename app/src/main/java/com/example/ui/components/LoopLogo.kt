package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividPurple

@Composable
fun LoopVectorGlyph(
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val gradient = Brush.linearGradient(
            colors = listOf(ElectricCyan, VividPurple, NeonMagenta),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )

        val strokeWidth = w * 0.14f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        // Draw double looped infinity infinity/ring structure
        val leftCenter = Offset(w * 0.35f, h * 0.5f)
        val rightCenter = Offset(w * 0.65f, h * 0.5f)
        val radius = w * 0.22f

        // Subtle glow underlying
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(VividPurple.copy(alpha = 0.5f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.6f
            )
        )

        // Left circle arc
        drawArc(
            brush = gradient,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(leftCenter.x - radius, leftCenter.y - radius),
            size = Size(radius * 2, radius * 2),
            style = stroke
        )

        // Right circle arc
        drawArc(
            brush = gradient,
            startAngle = 225f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(rightCenter.x - radius, rightCenter.y - radius),
            size = Size(radius * 2, radius * 2),
            style = stroke
        )
    }
}

@Composable
fun LoopBrandHeader(
    fontSize: Int = 28,
    showGlyph: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (showGlyph) {
            LoopVectorGlyph(size = (fontSize * 1.1).dp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "loop",
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            fontFamily = FontFamily.SansSerif,
            color = TextPrimary
        )
    }
}
