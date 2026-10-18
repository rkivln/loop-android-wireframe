package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.models.RentalItem
import com.example.ui.components.RentalItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    items: List<RentalItem>,
    onItemClick: (RentalItem) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = items.filter { it.title.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Explore Equipment") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search cameras, drones, lenses...") },
                singleLine = true
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filtered) { item ->
                    RentalItemCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }
    }
}
