package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.DeliveryOptionType
import com.example.data.models.RentalItem
import com.example.ui.components.EditorialHeadline
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SectionLabel
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite
import com.example.ui.theme.PaperWarm

@Composable
fun BookingScreen(
    item: RentalItem,
    onBookingConfirmed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDays by remember { mutableIntStateOf(3) }
    var selectedDelivery by remember { mutableStateOf(DeliveryOptionType.SELF_PICKUP) }
    var isConfirmedModalOpen by remember { mutableStateOf(false) }

    val dailyRate = item.pricePerDay
    val rentalSubtotal = dailyRate * selectedDays
    val deliveryFee = selectedDelivery.fee
    val serviceFee = 40
    val totalAmount = rentalSubtotal + deliveryFee + serviceFee

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperWarm)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            EditorialTopBar(
                title = "Reservation",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp)
            ) {
                // Item Preview Card
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                painter = painterResource(id = item.primaryImageRes),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                                color = InkCharcoal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Host: ${item.owner.name} · ${item.location}",
                                style = LoopType.Metadata,
                                color = InkSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${item.pricePerDay} / day",
                                style = LoopType.PriceSmall,
                                color = InkCharcoal
                            )
                        }
                    }
                }

                // Rental Duration Selector
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "DURATION OF USE",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(1 to "1 Day", 3 to "3 Days", 7 to "1 Week").forEach { (days, label) ->
                            val isSelected = selectedDays == days
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) InkCharcoal else PaperPureWhite)
                                    .border(
                                        1.dp,
                                        if (isSelected) InkCharcoal else PaperBorder,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedDays = days }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = label,
                                        style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isSelected) InkWhite else InkCharcoal
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${item.pricePerDay * days}",
                                        style = LoopType.Metadata.copy(fontSize = 11.sp),
                                        color = if (isSelected) AccentWarmYellow else InkSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Hand-off Method
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "HAND-OFF MODE",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Self Pickup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selectedDelivery == DeliveryOptionType.SELF_PICKUP) PaperPureWhite else PaperIvory)
                                .border(
                                    1.dp,
                                    if (selectedDelivery == DeliveryOptionType.SELF_PICKUP) InkCharcoal else PaperBorder,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { selectedDelivery = DeliveryOptionType.SELF_PICKUP }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DirectionsWalk,
                                contentDescription = null,
                                tint = InkCharcoal,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Self Pickup (Café des Arts)",
                                    style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = InkCharcoal
                                )
                                Text(
                                    text = "Meet the host at 10, Suffren St, White Town",
                                    style = LoopType.Metadata,
                                    color = InkSecondary
                                )
                            }
                            Text(
                                text = "Free",
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, color = AccentForestGreen)
                            )
                        }

                        // Doorstep Delivery
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selectedDelivery == DeliveryOptionType.OWNER_DELIVERY) PaperPureWhite else PaperIvory)
                                .border(
                                    1.dp,
                                    if (selectedDelivery == DeliveryOptionType.OWNER_DELIVERY) InkCharcoal else PaperBorder,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { selectedDelivery = DeliveryOptionType.OWNER_DELIVERY }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                tint = InkCharcoal,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Host Courier Delivery",
                                    style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = InkCharcoal
                                )
                                Text(
                                    text = "Direct delivery to your doorstep in Puducherry",
                                    style = LoopType.Metadata,
                                    color = InkSecondary
                                )
                            }
                            Text(
                                text = "+₹100",
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, color = InkCharcoal)
                            )
                        }
                    }
                }

                // Transparent Price Breakdown
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "PRICE BREAKDOWN",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "₹${item.pricePerDay} × $selectedDays days",
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                            Text(
                                text = "₹$rentalSubtotal",
                                style = LoopType.BodyMedium,
                                color = InkCharcoal
                            )
                        }

                        if (selectedDelivery.fee > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Delivery Fee",
                                    style = LoopType.BodySmall,
                                    color = InkSecondary
                                )
                                Text(
                                    text = "₹$deliveryFee",
                                    style = LoopType.BodyMedium,
                                    color = InkCharcoal
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Community Protection & Guarantee",
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                            Text(
                                text = "₹$serviceFee",
                                style = LoopType.BodyMedium,
                                color = InkCharcoal
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(PaperBorderSubtle)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Due",
                                style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                                color = InkCharcoal
                            )
                            Text(
                                text = "₹$totalAmount",
                                style = LoopType.PriceHeadline,
                                color = InkCharcoal
                            )
                        }
                    }
                }
            }

            // Bottom CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                PrimaryCTA(
                    text = "Confirm & Request Piece",
                    onClick = {
                        isConfirmedModalOpen = true
                    },
                    testTag = "booking_confirm_btn"
                )
            }
        }

        // Confirmation Sheet / Dialog
        if (isConfirmedModalOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorder, RoundedCornerShape(26.dp))
                        .padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AccentMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = AccentForestGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Reservation Sent",
                        style = LoopType.HeroDisplay.copy(fontSize = 22.sp),
                        color = InkCharcoal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${item.owner.name} has received your reservation request for ${item.title}. Chat initiated in Studio tab.",
                        style = LoopType.BodySmall,
                        color = InkSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryCTA(
                        text = "Go to Studio Messages",
                        onClick = {
                            isConfirmedModalOpen = false
                            onBookingConfirmed()
                        },
                        testTag = "modal_go_to_chat_btn"
                    )
                }
            }
        }
    }
}
