package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.VoidBlack

/**
 * Clean, restrained background with subtle top depth illumination.
 * Replaces distracting neon glowing blobs with a sleek, premium dark backdrop.
 */
@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(VoidBlack)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Very subtle, quiet top-center illumination for soft depth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x144F46E5),
                        Color(0x063B82F6),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, 0f),
                    radius = w * 0.75f
                ),
                center = Offset(w * 0.5f, 0f),
                radius = w * 0.75f
            )
        }
        content()
    }
}

/**
 * Clean visual anchor for hero cards and welcome screens.
 * Purpose-driven geometric focal element rather than a fake 3D glowing sphere.
 */
@Composable
fun GlowOrb(
    size: Dp = 140.dp,
    primaryColor: Color = BrandPrimary,
    secondaryColor: Color = BrandSecondary,
    tertiaryColor: Color = Color(0xFF6366F1),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Subtle outer concentric contour
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0x0C4F46E5), CircleShape)
                .border(1.dp, BorderSubtle, CircleShape)
        )

        // Inner solid core with clean geometric gradient
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
                    ),
                    CircleShape
                )
                .border(1.dp, Color(0x336366F1), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            LoopVectorGlyph(size = size * 0.38f)
        }
    }
}
