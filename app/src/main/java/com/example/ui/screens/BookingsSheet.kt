package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.UserRentalBooking
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite

@Composable
fun BookingsModalSheet(
    bookings: List<UserRentalBooking>,
    onUpdateStatus: (bookingId: String, status: String) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(PaperPureWhite)
                .clickable(enabled = false) {}
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .navigationBarsPadding()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Reservations (${bookings.size})",
                        style = LoopType.HeadlineMedium,
                        color = InkCharcoal
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkSecondary
                        )
                    }
                }

                if (bookings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No gear reservations yet. Book an item from the Home catalog to track pickup PINs and dates here.",
                            style = LoopType.BodySmall,
                            color = InkSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(340.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(bookings, key = { it.id }) { booking ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(PaperIvory)
                                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                                    .testTag("booking_row_${booking.id}")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = booking.item.title,
                                            style = LoopType.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = InkCharcoal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(AccentMint)
                                                .padding(horizontal = 10.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = booking.status,
                                                style = LoopType.Metadata.copy(
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = AccentForestGreen
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Host: ${booking.hostName} · ${booking.startDate} – ${booking.endDate}",
                                        style = LoopType.BodySmall,
                                        color = InkSecondary
                                    )
                                    Text(
                                        text = "Total: ₹${booking.totalPaid} · Handover PIN: #${booking.handoverOtp}",
                                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                        color = InkCharcoal
                                    )
                                    if (!booking.isCompleted) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { onUpdateStatus(booking.id, "Completed") },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Mark Returned")
                                            }
                                            OutlinedButton(
                                                onClick = { onUpdateStatus(booking.id, "Cancelled") },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Cancel")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
