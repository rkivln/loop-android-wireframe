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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CanvasDark
import com.example.ui.theme.CanvasWhite
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.LineHairline

enum class LoopTab {
    HOME,
    EXPLORE,
    LIST,
    MESSAGES,
    PROFILE
}

@Composable
fun LoopBottomNavigation(
    selectedTab: LoopTab,
    onTabSelected: (LoopTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CanvasWhite)
            .border(width = 1.dp, color = LineHairline, shape = RoundedCornerShape(0.dp))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home / Inventory
            LoopNavItem(
                icon = if (selectedTab == LoopTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Inventory",
                isSelected = selectedTab == LoopTab.HOME,
                onClick = { onTabSelected(LoopTab.HOME) },
                testTag = "nav_home"
            )

            // Explore / Map
            LoopNavItem(
                icon = if (selectedTab == LoopTab.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                label = "Explore",
                isSelected = selectedTab == LoopTab.EXPLORE,
                onClick = { onTabSelected(LoopTab.EXPLORE) },
                testTag = "nav_explore"
            )

            // Center Technical "+ List" Action
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CanvasDark)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White),
                        onClick = { onTabSelected(LoopTab.LIST) }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("nav_list_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "List gear",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "LIST GEAR",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }

            // Messages
            LoopNavItem(
                icon = if (selectedTab == LoopTab.MESSAGES) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                label = "Chats",
                isSelected = selectedTab == LoopTab.MESSAGES,
                onClick = { onTabSelected(LoopTab.MESSAGES) },
                testTag = "nav_messages"
            )

            // Profile
            LoopNavItem(
                icon = if (selectedTab == LoopTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                label = "Identity",
                isSelected = selectedTab == LoopTab.PROFILE,
                onClick = { onTabSelected(LoopTab.PROFILE) },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
private fun LoopNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(testTag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) InkPrimary else InkMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) InkPrimary else InkMuted
        )

        // Architectural indicator bar
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(2.dp)
                .background(if (isSelected) InkPrimary else Color.Transparent)
        )
    }
}
