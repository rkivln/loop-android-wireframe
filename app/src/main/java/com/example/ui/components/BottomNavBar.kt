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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(SurfaceDark)
                .border(1.dp, BorderSubtle, RoundedCornerShape(0.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Tab
                NavTabItem(
                    label = "Home",
                    icon = if (currentScreen == LoopNavScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    isSelected = currentScreen == LoopNavScreen.HOME,
                    onClick = { onNavigate(LoopNavScreen.HOME) },
                    testTag = "nav_home"
                )

                // Explore Tab
                NavTabItem(
                    label = "Explore",
                    icon = if (currentScreen == LoopNavScreen.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                    isSelected = currentScreen == LoopNavScreen.EXPLORE,
                    onClick = { onNavigate(LoopNavScreen.EXPLORE) },
                    testTag = "nav_explore"
                )

                // Center Post Action Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary, CircleShape)
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White),
                            onClick = onPlusClick
                        )
                        .testTag("nav_create_post_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Post",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Community / Chats Tab
                NavTabItem(
                    label = "Chats",
                    icon = if (currentScreen == LoopNavScreen.CHATS) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                    isSelected = currentScreen == LoopNavScreen.CHATS,
                    onClick = { onNavigate(LoopNavScreen.CHATS) },
                    testTag = "nav_chats"
                )

                // Profile Tab
                NavTabItem(
                    label = "Profile",
                    icon = if (currentScreen == LoopNavScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    isSelected = currentScreen == LoopNavScreen.PROFILE,
                    onClick = { onNavigate(LoopNavScreen.PROFILE) },
                    testTag = "nav_profile"
                )
            }
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color(0x22FFFFFF)),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) BrandSecondary else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) TextPrimary else TextMuted
        )
    }
}
