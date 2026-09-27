package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceInteractive
import com.example.ui.theme.VoidBlack

private val AvatarColors = listOf(
    Color(0xFF3730A3), // Indigo
    Color(0xFF1E3A8A), // Blue
    Color(0xFF065F46), // Emerald
    Color(0xFF854D0E), // Amber
    Color(0xFF4C1D95)  // Violet
)

@Composable
fun AvatarBadge(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    colorIndex: Int = 0,
    showOnlineIndicator: Boolean = false,
    hasGlowBorder: Boolean = false
) {
    val bgColor = AvatarColors[colorIndex.coerceIn(0, AvatarColors.size - 1)]

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor, CircleShape)
                .border(1.dp, if (hasGlowBorder) BrandPrimary else BorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value * 0.38).sp
            )
        }

        if (showOnlineIndicator) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(StatusSuccess, CircleShape)
                    .border(1.5.dp, VoidBlack, CircleShape)
            )
        }
    }
}

@Composable
fun AvatarPile(
    initialsList: List<String>,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 26.dp,
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
                modifier = Modifier.offset(x = (-6 * index).dp)
            )
        }

        if (overflowCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (-6 * initialsList.take(4).size).dp)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(SurfaceInteractive, CircleShape)
                    .border(1.dp, BorderSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$overflowCount",
                    fontSize = (avatarSize.value * 0.36).sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}
