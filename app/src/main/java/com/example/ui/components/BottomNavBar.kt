package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperPureWhite

enum class LoopTab(
    val title: String,
    val icon: ImageVector,
    val tag: String
) {
    HOME("Catalog", Icons.Outlined.Home, "tab_home"),
    EXPLORE("Objects", Icons.Outlined.Explore, "tab_explore"),
    LIST("Publish", Icons.Outlined.AddCircleOutline, "tab_list"),
    MESSAGES("Studio", Icons.AutoMirrored.Outlined.Chat, "tab_messages"),
    PROFILE("Profile", Icons.Outlined.PersonOutline, "tab_profile")
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
            .background(PaperPureWhite.copy(alpha = 0.97f))
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .navigationBarsPadding()
            .padding(top = 10.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoopTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) InkCharcoal else InkMuted,
                    animationSpec = tween(200),
                    label = "tab_icon_color"
                )

                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag(tab.tag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(23.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tab.title,
                        style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                        color = if (isSelected) InkCharcoal else InkMuted
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Minimal active dot
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) InkCharcoal else Color.Transparent)
                    )
                }
            }
        }
    }
}
