package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.LoopCategory
import com.example.data.models.MapPinItem
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple
import com.example.ui.theme.WarmSunset

@Composable
fun DarkVectorMapView(
    pins: List<MapPinItem>,
    selectedPin: MapPinItem?,
    onPinSelected: (MapPinItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Vector map layer: Dark stylised terrain, grid, streets, water body
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background deep slate
            drawRect(Color(0xFF0C0E16))

            // Water coastline (curving blue flow on right side)
            val waterPath = Path().apply {
                moveTo(size.width * 0.72f, 0f)
                cubicTo(
                    size.width * 0.68f, size.height * 0.3f,
                    size.width * 0.85f, size.height * 0.7f,
                    size.width * 0.82f, size.height
                )
                lineTo(size.width, size.height)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(
                path = waterPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0A1B30), Color(0xFF071424)),
                    start = Offset(size.width * 0.7f, 0f),
                    end = Offset(size.width, size.height)
                )
            )

            // Coastline contour glow
            drawPath(
                path = waterPath,
                brush = Brush.horizontalGradient(
                    listOf(Color(0x3300E5FF), Color.Transparent)
                ),
                style = Stroke(width = 3f)
            )

            // Stylized Road Grid lines
            val roadColor = Color(0x18374151)
            val majorRoadColor = Color(0x284B5563)

            // Diagonal avenues
            drawLine(
                color = majorRoadColor,
                start = Offset(0f, size.height * 0.2f),
                end = Offset(size.width, size.height * 0.65f),
                strokeWidth = 6f
            )
            drawLine(
                color = majorRoadColor,
                start = Offset(size.width * 0.15f, 0f),
                end = Offset(size.width * 0.65f, size.height),
                strokeWidth = 5f
            )
            drawLine(
                color = roadColor,
                start = Offset(0f, size.height * 0.55f),
                end = Offset(size.width * 0.8f, size.height * 0.1f),
                strokeWidth = 3f
            )
            drawLine(
                color = roadColor,
                start = Offset(size.width * 0.35f, 0f),
                end = Offset(size.width * 0.85f, size.height * 0.9f),
                strokeWidth = 3f
            )

            // Street grid
            for (i in 1..8) {
                val y = size.height * (i / 9f)
                drawLine(
                    color = roadColor,
                    start = Offset(0f, y),
                    end = Offset(size.width * 0.78f, y),
                    strokeWidth = 2f
                )
            }
            for (i in 1..6) {
                val x = size.width * (i / 7f)
                drawLine(
                    color = roadColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2f
                )
            }

            // User location central node (pulse rings)
            val userCenter = Offset(size.width * 0.52f, size.height * 0.44f)

            drawCircle(
                color = NeonBlue.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = userCenter,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = VividPurple.copy(alpha = (pulseAlpha * 0.6f)),
                radius = pulseRadius * 1.5f,
                center = userCenter,
                style = Stroke(width = 1.5f)
            )

            // User pin center
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF388BFF), Color(0xFF1D4ED8)),
                    center = userCenter,
                    radius = 16f
                ),
                radius = 14f,
                center = userCenter
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = userCenter
            )
        }

        // Render interactive map markers
        pins.forEach { pin ->
            val isSelected = pin.id == selectedPin?.id
            val pinX = (widthPx * pin.xRatio).dp / 2.75f
            val pinY = (heightPx * pin.yRatio).dp / 2.75f

            MapMarkerItem(
                pin = pin,
                isSelected = isSelected,
                onClick = { onPinSelected(pin) },
                modifier = Modifier.offset(x = pinX, y = pinY)
            )
        }
    }
}

@Composable
fun MapMarkerItem(
    pin: MapPinItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val markerGradient = when (pin.category) {
        LoopCategory.STUDY -> listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
        LoopCategory.EVENTS -> listOf(Color(0xFFFF5E62), Color(0xFFFF9966))
        LoopCategory.HELP -> listOf(Color(0xFF10B981), Color(0xFF06B6D4))
        LoopCategory.PEOPLE -> listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
        LoopCategory.ALL -> listOf(Color(0xFFD946EF), Color(0xFF8B5CF6))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        // Floating Callout Badge for Selected Pin or Highlights
        if (isSelected || pin.id == "pin_event") {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xE61B1E2D))
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (pin.category) {
                            LoopCategory.STUDY -> Icons.Default.School
                            LoopCategory.EVENTS -> Icons.Default.DateRange
                            LoopCategory.HELP -> Icons.Default.Handshake
                            else -> Icons.Default.AutoAwesome
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = pin.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${pin.distance} away",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 4.dp))
        }

        // Circular Map Pin
        val pinSize = if (isSelected) 42.dp else 36.dp
        Box(
            modifier = Modifier
                .size(pinSize)
                .clip(CircleShape)
                .background(Brush.linearGradient(markerGradient), CircleShape)
                .border(
                    if (isSelected) 2.dp else 1.5.dp,
                    if (isSelected) Color.White else GlassBorder,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when (pin.category) {
                LoopCategory.STUDY -> Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                LoopCategory.EVENTS -> Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                LoopCategory.HELP -> Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                LoopCategory.PEOPLE -> Text(
                    text = pin.initials,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                else -> Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
