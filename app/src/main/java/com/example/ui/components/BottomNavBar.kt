package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ButtonCtaGradient
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple

enum class LoopNavScreen(val title: String) {
    HOME("Home"),
    EXPLORE("Explore"),
    COMMUNITY("Community"),
    CHATS("Chats"),
    PROFILE("Profile")
}

@Composable
fun LoopBottomNavigationBar(
    currentScreen: LoopNavScreen,
    onNavigate: (LoopNavScreen) -> Unit,
    onPlusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Glass Navigation Bar Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xE60E101A), Color(0xF5080A10))
                    )
                )
                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                NavTabItem(
                    label = "Home",
                    icon = if (currentScreen == LoopNavScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    isSelected = currentScreen == LoopNavScreen.HOME,
                    onClick = { onNavigate(LoopNavScreen.HOME) },
                    testTag = "nav_home"
                )

                // Explore
                NavTabItem(
                    label = "Explore",
                    icon = if (currentScreen == LoopNavScreen.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                    isSelected = currentScreen == LoopNavScreen.EXPLORE,
                    onClick = { onNavigate(LoopNavScreen.EXPLORE) },
                    testTag = "nav_explore"
                )

                // Placeholder space for center + button
                Box(modifier = Modifier.size(54.dp))

                // Chats / Community
                NavTabItem(
                    label = "Chats",
                    icon = if (currentScreen == LoopNavScreen.CHATS) Icons.Filled.ChatBubbleOutline else Icons.Outlined.ChatBubbleOutline,
                    isSelected = currentScreen == LoopNavScreen.CHATS,
                    onClick = { onNavigate(LoopNavScreen.CHATS) },
                    testTag = "nav_chats"
                )

                // Profile
                NavTabItem(
                    label = "Profile",
                    icon = if (currentScreen == LoopNavScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    isSelected = currentScreen == LoopNavScreen.PROFILE,
                    onClick = { onNavigate(LoopNavScreen.PROFILE) },
                    testTag = "nav_profile"
                )
            }
        }

        // Floating center + button
        Box(
            modifier = Modifier
                .offset(y = (-16).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(ButtonCtaGradient, CircleShape)
                .border(
                    2.dp,
                    Brush.sweepGradient(listOf(ElectricCyan, VividPurple, NeonMagenta, ElectricCyan)),
                    CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPlusClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Post or Event",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) NeonBlue else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) NeonBlue else TextMuted
        )
    }
}
