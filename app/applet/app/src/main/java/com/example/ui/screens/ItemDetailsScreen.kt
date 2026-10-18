package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ai.GeminiAiService
import com.example.data.models.RentalItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    item: RentalItem,
    onBack: () -> Unit,
    onBookNow: (RentalItem) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var aiRecommendation by remember { mutableStateOf<String?>(null) }
    var isLoadingAi by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Daily Rate", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "$${item.dailyRate.toInt()} / day", style = MaterialTheme.typography.titleLarge)
                    }
                    Button(onClick = { onBookNow(item) }) {
                        Text("Book Rental")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )
            Text(text = item.title, style = MaterialTheme.typography.displayLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = "Rating", tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${item.rating} (${item.reviewCount} reviews)", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = item.description, style = MaterialTheme.typography.bodyLarge)

            // Smart AI Accessory Matcher Feature
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Smart AI Accessory & Bundle Matcher", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (aiRecommendation != null) {
                        Text(text = aiRecommendation!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    } else {
                        Button(
                            onClick = {
                                isLoadingAi = true
                                coroutineScope.launch {
                                    aiRecommendation = GeminiAiService.evaluateGearAndSuggestPricing(item.title, item.description)
                                    isLoadingAi = false
                                }
                            },
                            enabled = !isLoadingAi
                        ) {
                            Text(if (isLoadingAi) "Analyzing with Gemini AI..." else "Get AI Accessory Recommendations")
                        }
                    }
                }
            }
        }
    }
}
