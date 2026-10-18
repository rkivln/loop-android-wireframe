package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.models.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(userProfile: UserProfile) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Creator Profile") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userProfile.getInitials(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Text(text = userProfile.displayName, style = MaterialTheme.typography.titleLarge)
            Text(text = userProfile.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            Text(text = userProfile.bio, style = MaterialTheme.typography.bodyLarge)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Marketplace Stats", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Rentals Completed: ${userProfile.rentalsCompleted}")
                    Text(text = "Active Listings: ${userProfile.listingsCount}")
                    Text(text = "Total Earned: $${userProfile.earnedAmount}")
                    Text(text = "Rating: ${userProfile.rating} ⭐ (${userProfile.reviewCount} reviews)")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                userProfile.badges.forEach { badge ->
                    AssistChip(onClick = {}, label = { Text(badge) })
                }
            }
        }
    }
}
