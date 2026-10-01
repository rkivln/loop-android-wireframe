package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCobalt
import com.example.ui.theme.AccentCobaltSubtle
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CanvasDark
import com.example.ui.theme.CanvasSubtle
import com.example.ui.theme.CanvasWhite
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LineHairline
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

// Primary Action Button: Architectural, solid, crisp 8dp radius
@Composable
fun LoopPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    showArrow: Boolean = true,
    enabled: Boolean = true,
    testTag: String = "primary_button"
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(if (enabled) CanvasDark else InkMuted, shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.3.sp
            )
            if (showArrow) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Secondary Action Button: Clean white, hairline border, dark text
@Composable
fun LoopSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    testTag: String = "secondary_button"
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(CanvasWhite, shape)
            .border(1.dp, LineHairline, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkPrimary,
            letterSpacing = 0.2.sp
        )
    }
}

// Technical Filter Chip with counts
@Composable
fun LoopFilterChip(
    label: String,
    count: Int? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (isSelected) CanvasDark else CanvasSubtle,
                shape
            )
            .border(
                1.dp,
                if (isSelected) CanvasDark else LineHairline,
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else InkPrimary
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "$count",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White.copy(alpha = 0.7f) else InkMuted
                )
            }
        }
    }
}

// Backwards-compatible aliases with modern refined styling
@Composable
fun DarkPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    showArrow: Boolean = true,
    enabled: Boolean = true,
    testTag: String = "dark_pill_button"
) {
    LoopPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        height = height,
        showArrow = showArrow,
        enabled = enabled,
        testTag = testTag
    )
}

@Composable
fun OutlinedPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    testTag: String = "outlined_pill_button"
) {
    LoopSecondaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        height = height,
        testTag = testTag
    )
}

@Composable
fun CircularIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    backgroundColor: Color = CanvasWhite,
    tint: Color = InkPrimary,
    testTag: String = "circular_icon_btn"
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .testTag(testTag)
            .size(size)
            .clip(shape)
            .background(backgroundColor, shape)
            .border(1.dp, LineHairline, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
