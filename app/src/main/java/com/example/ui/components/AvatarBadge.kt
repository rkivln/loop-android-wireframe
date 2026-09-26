package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SunsetOrange
import com.example.ui.theme.VividPurple
import com.example.ui.theme.WarmSunset

private val AvatarGradients = listOf(
    listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)),
    listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
    listOf(Color(0xFF10B981), Color(0xFF06B6D4)),
    listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
    listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
)

@Composable
fun AvatarBadge(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    colorIndex: Int = 0,
    showOnlineIndicator: Boolean = false,
    hasGlowBorder: Boolean = false
) {
    val gradientColors = AvatarGradients[colorIndex.coerceIn(0, AvatarGradients.size - 1)]
    val borderBrush = if (hasGlowBorder) {
        Brush.sweepGradient(listOf(NeonBlue, VividPurple, NeonMagenta, NeonBlue))
    } else {
        Brush.linearGradient(listOf(GlassBorder, Color.Transparent))
    }

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(gradientColors), CircleShape)
                .border(if (hasGlowBorder) 1.5.dp else 1.dp, borderBrush, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38).sp
            )
        }

        if (showOnlineIndicator) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(EmeraldGreen, CircleShape)
                    .border(1.5.dp, Color(0xFF0B0D14), CircleShape)
            )
        }
    }
}

@Composable
fun AvatarPile(
    initialsList: List<String>,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 28.dp,
    overflowCount: Int = 0
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        initialsList.take(4).forEachIndexed { index, initials ->
            AvatarBadge(
                initials = initials,
                size = avatarSize,
                colorIndex = index,
                modifier = Modifier.offset(x = (-8 * index).dp)
            )
        }

        if (overflowCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (-8 * initialsList.take(4).size).dp)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(Color(0xFF222738), CircleShape)
                    .border(1.dp, GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$overflowCount",
                    fontSize = (avatarSize.value * 0.36).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
