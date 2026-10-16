package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier
) {
    // Delegates to UserLocationMapScreen which displays Google Maps using maps-compose
    // and centers on user's current coordinates with GPS integration and permissions.
    UserLocationMapScreen(modifier = modifier)
}
