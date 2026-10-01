package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CanvasDark
import com.example.ui.theme.CanvasSubtle
import com.example.ui.theme.CanvasWhite
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LineHairline
import com.example.ui.theme.StatusLive

@Composable
fun LoopLogoText(
    fontSize: Int = 20,
    showSubtext: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LOOP",
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    color = InkPrimary,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CanvasDark)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "EQUIPMENT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (showSubtext) {
                Text(
                    text = "PEER-TO-PEER GEAR EXCHANGE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = InkMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun LocationDropdownSelector(
    selectedLocation: String = "White Town, Puducherry",
    onLocationSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val locations = listOf("White Town, Puducherry", "Heritage Town", "Auroville", "Lawspet", "Mission Street")

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(CanvasWhite)
                .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.LightGray),
                    onClick = { expanded = true }
                )
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("location_dropdown_btn")
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StatusLive)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = selectedLocation,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Change Location",
                tint = InkSecondary,
                modifier = Modifier.size(15.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CanvasWhite)
        ) {
            locations.forEach { loc ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = loc,
                            fontSize = 13.sp,
                            fontWeight = if (loc == selectedLocation) FontWeight.Bold else FontWeight.Normal,
                            color = InkPrimary
                        )
                    },
                    onClick = {
                        onLocationSelected(loc)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationBellButton(
    onClick: () -> Unit,
    hasUnread: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(CanvasWhite)
            .border(1.dp, LineHairline, RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray),
                onClick = onClick
            )
            .testTag("notification_bell_btn"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = InkPrimary,
            modifier = Modifier.size(18.dp)
        )

        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 6.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(AlertCrimson)
            )
        }
    }
}
