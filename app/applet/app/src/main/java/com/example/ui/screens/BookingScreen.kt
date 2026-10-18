package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.models.RentalItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    item: RentalItem,
    onBack: () -> Unit,
    onConfirmBooking: () -> Unit
) {
    var startDate by remember { mutableStateOf("Oct 20, 2026") }
    var endDate by remember { mutableStateOf("Oct 23, 2026") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Rental: ${item.title}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Daily Rate: $${item.dailyRate.toInt()}")
                    Text(text = "Security Deposit (Refundable): $${item.deposit.toInt()}")
                }
            }

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Rental Start Date") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("Rental End Date") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onConfirmBooking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Booking Request")
            }
        }
    }
}
