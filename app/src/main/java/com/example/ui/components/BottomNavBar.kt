package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperPureWhite

enum class LoopTab(
    val title: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector,
    val testTag: String
) {
    CATALOG("Catalog", Icons.Outlined.Home, Icons.Filled.Home, "nav_catalog"),
    OBJECTS("Objects", Icons.Outlined.Explore, Icons.Filled.Explore, "nav_objects"),
    PUBLISH("Publish", Icons.Outlined.AddCircleOutline, Icons.Filled.AddCircleOutline, "nav_publish"),
    STUDIO("Studio", Icons.AutoMirrored.Outlined.Chat, Icons.AutoMirrored.Filled.Chat, "nav_studio"),
    PROFILE("Profile", Icons.Outlined.Person, Icons.Filled.Person, "nav_profile")
}

@Composable
fun BottomNavBar(
    currentTab: LoopTab,
    onTabSelected: (LoopTab) -> Unit,
    unreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PaperPureWhite)
            .navigationBarsPadding()
            .testTag("bottom_nav_bar")
    ) {
        HorizontalDivider(thickness = 1.dp, color = PaperBorderSubtle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoopTab.entries.forEach { tab ->
                val selected = currentTab == tab
                val tint = if (selected) InkCharcoal else InkMuted

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 32.dp),
                            onClick = { onTabSelected(tab) }
                        )
                        .testTag(tab.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box {
                        Icon(
                            imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
                            contentDescription = tab.title,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                        if (tab == LoopTab.STUDIO && unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AccentWarmYellow)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = tab.title,
                        style = LoopType.Metadata.copy(
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = tint,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (selected) InkCharcoal else PaperPureWhite)
                    )
                }
            }
        }
    }
}
