package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

enum class LoopTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    EXPLORE("Explore", Icons.Outlined.Search),
    CREATE("List", Icons.Outlined.AddBox),
    MESSAGES("Messages", Icons.Outlined.Chat),
    PROFILE("Profile", Icons.Outlined.Person)
}

@Composable
fun BottomNavBar(currentTab: LoopTab, onTabSelected: (LoopTab) -> Unit, unreadCount: Int = 0) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        LoopTab.values().forEach { tab ->
            val selected = currentTab == tab
            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            if (tab == LoopTab.MESSAGES && unreadCount > 0) {
                                Badge { Text("$unreadCount") }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selected) {
                                when (tab) {
                                    LoopTab.HOME -> Icons.Filled.Home
                                    LoopTab.EXPLORE -> Icons.Filled.Search
                                    LoopTab.CREATE -> Icons.Filled.AddBox
                                    LoopTab.MESSAGES -> Icons.Filled.Chat
                                    LoopTab.PROFILE -> Icons.Filled.Person
                                }
                            } else tab.icon,
                            contentDescription = tab.title
                        )
                    }
                },
                label = { Text(tab.title) },
                selected = selected,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
