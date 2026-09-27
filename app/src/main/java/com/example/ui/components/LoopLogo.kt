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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.TextPrimary

@Composable
fun LoopVectorGlyph(
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val strokeWidth = w * 0.12f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        val gradient = Brush.linearGradient(
            colors = listOf(BrandSecondary, BrandPrimary),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )

        // Precision looped infinity node
        val leftCenter = Offset(w * 0.36f, h * 0.5f)
        val rightCenter = Offset(w * 0.64f, h * 0.5f)
        val radius = w * 0.22f

        // Left arc
        drawArc(
            brush = gradient,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(leftCenter.x - radius, leftCenter.y - radius),
            size = Size(radius * 2, radius * 2),
            style = stroke
        )

        // Right arc
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
    fontSize: Int = 26,
    showGlyph: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (showGlyph) {
            LoopVectorGlyph(size = (fontSize * 0.95).dp)
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = "loop",
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp,
            fontFamily = FontFamily.SansSerif,
            color = TextPrimary
        )
    }
}
