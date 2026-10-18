package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ai.GeminiAiService
import com.example.data.models.RentalCategory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    onListingCreated: (title: String, category: RentalCategory, dailyRate: Double, deposit: Double, location: String, description: String, imageUrl: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dailyRateStr by remember { mutableStateOf("") }
    var depositStr by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("White Town, Puducherry") }
    var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=800&q=80") }
    var selectedCategory by remember { mutableStateOf(RentalCategory.CAMERAS) }

    var aiValuationSuggestion by remember { mutableStateOf<String?>(null) }
    var isEvaluating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("List Pro Gear") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Gear Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Condition") },
                modifier = Modifier.fillMaxWidth()
            )

            // AI Valuation Assistant Feature
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "AI Valuation & Pricing Assistant", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (aiValuationSuggestion != null) {
                        Text(text = aiValuationSuggestion!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    isEvaluating = true
                                    coroutineScope.launch {
                                        aiValuationSuggestion = GeminiAiService.evaluateGearAndSuggestPricing(title, description)
                                        isEvaluating = false
                                    }
                                }
                            },
                            enabled = !isEvaluating && title.isNotBlank()
                        ) {
                            Text(if (isEvaluating) "Calculating Fair Price..." else "Get AI Price & Valuation Suggestion")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = dailyRateStr,
                onValueChange = { dailyRateStr = it },
                label = { Text("Daily Rental Rate ($)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = depositStr,
                onValueChange = { depositStr = it },
                label = { Text("Security Deposit ($)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val rate = dailyRateStr.toDoubleOrNull() ?: 50.0
                    val dep = depositStr.toDoubleOrNull() ?: 200.0
                    if (title.isNotBlank()) {
                        onListingCreated(title, selectedCategory, rate, dep, location, description, imageUrl)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publish Listing")
            }
        }
    }
}
