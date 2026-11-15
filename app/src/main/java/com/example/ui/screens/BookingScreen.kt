package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RentalRepository
import com.example.data.firebase.FirestoreBooking
import com.example.data.models.DeliveryOptionType
import com.example.data.models.RentalItem
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.PrimaryCTA
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun formatOffsetDays(offsetDays: Int): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, offsetDays)
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(cal.time)
}

@Composable
fun BookingScreen(
    item: RentalItem,
    onBack: () -> Unit,
    onBookingConfirmed: () -> Unit
) {
    BackHandler(onBack = onBack)

    val coroutineScope = rememberCoroutineScope()
    var daysCount by remember { mutableIntStateOf(3) }
    var startDate by remember { mutableStateOf(formatOffsetDays(0)) }
    var endDate by remember { mutableStateOf(formatOffsetDays(3)) }
    var selectedDelivery by remember { mutableStateOf(DeliveryOptionType.SELF_PICKUP) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var confirmedBooking by remember { mutableStateOf<FirestoreBooking?>(null) }

    val rentalSubtotal = item.pricePerDay * daysCount
    val serviceFee = 50
    val deliveryFee = selectedDelivery.fee
    val totalPayable = rentalSubtotal + serviceFee + deliveryFee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .navigationBarsPadding()
    ) {
        EditorialTopBar(
            title = "Reserve Piece",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (confirmedBooking != null) {
                val booking = confirmedBooking!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                        .testTag("booking_confirmed_card"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AccentMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Confirmed",
                            tint = AccentForestGreen,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text(
                        text = "Reservation Confirmed",
                        style = LoopType.HeroDisplay.copy(fontSize = 24.sp),
                        color = InkCharcoal,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${booking.itemTitle} reserved with ${booking.hostName} (${booking.startDate} – ${booking.endDate}).",
                        style = LoopType.BodyEditorial,
                        color = InkSecondary,
                        textAlign = TextAlign.Center
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PaperIvory)
                            .border(1.dp, PaperBorder, RoundedCornerShape(16.dp))
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HANDOVER PIN",
                            style = LoopType.EditorialTag,
                            color = InkMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "#${booking.handoverOtp}",
                            style = LoopType.HeroDisplayLarge.copy(fontSize = 30.sp),
                            color = AccentForestGreen
                        )
                    }
                    Text(
                        text = "Total Reserved: ₹${booking.totalAmount}",
                        style = LoopType.TitleMedium,
                        color = InkCharcoal
                    )
                    PrimaryCTA(
                        text = "Back to Catalog",
                        onClick = onBookingConfirmed,
                        testTag = "booking_done_button"
                    )
                }
            } else {
                // Piece Summary Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.category.title.uppercase(),
                        style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                        color = InkMuted
                    )
                    Text(
                        text = item.title,
                        style = LoopType.HeadlineMedium,
                        color = InkCharcoal
                    )
                    val hostText = item.owner.name.ifBlank { "Community Member" }
                    Text(
                        text = if (item.location.isNotBlank()) {
                            "Hosted by $hostText · ${item.location}"
                        } else {
                            "Hosted by $hostText"
                        },
                        style = LoopType.BodySmall,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${item.pricePerDay} / day · Refundable Deposit ₹${item.securityDeposit}",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                        color = InkCharcoal
                    )
                }

                // Duration Selector Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CIRCULATION DURATION",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PaperIvory)
                                .border(1.dp, PaperBorder, CircleShape)
                                .clickable { if (daysCount > 1) daysCount-- }
                                .testTag("booking_days_minus"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease days", tint = InkCharcoal, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "$daysCount days",
                            style = LoopType.TitleMedium,
                            color = InkCharcoal
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PaperIvory)
                                .border(1.dp, PaperBorder, CircleShape)
                                .clickable { if (daysCount < 30) daysCount++ }
                                .testTag("booking_days_plus"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase days", tint = InkCharcoal, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Dates
                BookingEditorialInput(
                    label = "PICKUP DATE",
                    value = startDate,
                    onValueChange = { startDate = it }
                )

                BookingEditorialInput(
                    label = "RETURN DATE",
                    value = endDate,
                    onValueChange = { endDate = it }
                )

                // Handover Method Pills
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "HANDOVER METHOD",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DeliveryOptionType.entries.forEach { option ->
                            val isSelected = selectedDelivery == option
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isSelected) InkCharcoal else PaperPureWhite)
                                    .border(
                                        1.dp,
                                        if (isSelected) InkCharcoal else PaperBorder,
                                        RoundedCornerShape(100.dp)
                                    )
                                    .clickable { selectedDelivery = option }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .testTag("delivery_option_${option.name}")
                            ) {
                                Text(
                                    text = if (option.fee > 0) "${option.title} (+₹${option.fee})" else option.title,
                                    style = LoopType.Metadata.copy(
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) InkWhite else InkCharcoal
                                )
                            }
                        }
                    }
                }

                // Price Breakdown Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "RESERVATION SUMMARY",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("₹${item.pricePerDay} × $daysCount days", style = LoopType.BodySmall, color = InkSecondary)
                        Text("₹$rentalSubtotal", style = LoopType.BodySmall, color = InkCharcoal)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Community Trust & Service Fee", style = LoopType.BodySmall, color = InkSecondary)
                        Text("₹$serviceFee", style = LoopType.BodySmall, color = InkCharcoal)
                    }
                    if (deliveryFee > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee", style = LoopType.BodySmall, color = InkSecondary)
                            Text("₹$deliveryFee", style = LoopType.BodySmall, color = InkCharcoal)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = PaperBorderSubtle)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Due", style = LoopType.TitleMedium, color = InkCharcoal)
                        Text("₹$totalPayable", style = LoopType.PriceHeadline, color = InkCharcoal)
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = LoopType.BodySmall,
                        color = AlertCrimson
                    )
                }
            }
        }

        if (confirmedBooking == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperIvory)
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                PrimaryCTA(
                    text = if (isSubmitting) "Confirming..." else "Confirm Reservation (₹$totalPayable)",
                    enabled = !isSubmitting,
                    onClick = {
                        isSubmitting = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = RentalRepository.createBooking(
                                item = item,
                                startDate = startDate,
                                endDate = endDate,
                                daysCount = daysCount,
                                deliveryOption = selectedDelivery.title
                            )
                            isSubmitting = false
                            result.fold(
                                onSuccess = { confirmedBooking = it },
                                onFailure = { errorMessage = it.localizedMessage ?: "Booking failed." }
                            )
                        }
                    },
                    testTag = "confirm_booking_button"
                )
            }
        }
    }
}

@Composable
private fun BookingEditorialInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = LoopType.EditorialTag,
            color = InkSecondary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PaperPureWhite)
                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LoopType.BodyMedium.copy(color = InkCharcoal),
                cursorBrush = SolidColor(InkCharcoal),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
