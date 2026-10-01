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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DateRange
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.ParchmentBg
import com.example.ui.theme.ParchmentBorder

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
            .background(ParchmentBg)
            .border(width = 1.dp, color = ParchmentBorder.copy(alpha = 0.6f), shape = RoundedCornerShape(0.dp))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home
            LoopNavItem(
                icon = if (selectedTab == LoopTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = selectedTab == LoopTab.HOME,
                onClick = { onTabSelected(LoopTab.HOME) },
                testTag = "nav_home"
            )

            // Tab 2: Explore
            LoopNavItem(
                icon = if (selectedTab == LoopTab.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                label = "Explore",
                isSelected = selectedTab == LoopTab.EXPLORE,
                onClick = { onTabSelected(LoopTab.EXPLORE) },
                testTag = "nav_explore"
            )

            // Tab 3: Center Solid Black Circular "+" Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(InkBlack)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White),
                        onClick = { onTabSelected(LoopTab.LIST) }
                    )
                    .testTag("nav_list_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "List gear",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Tab 4: Bookings (Calendar)
            LoopNavItem(
                icon = if (selectedTab == LoopTab.MESSAGES) Icons.Filled.DateRange else Icons.Outlined.DateRange,
                label = "Bookings",
                isSelected = selectedTab == LoopTab.MESSAGES,
                onClick = { onTabSelected(LoopTab.MESSAGES) },
                testTag = "nav_messages"
            )

            // Tab 5: Profile
            LoopNavItem(
                icon = if (selectedTab == LoopTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                label = "Profile",
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
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) InkBlack else InkMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) InkBlack else InkMuted
        )
    }
}
