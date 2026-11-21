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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BookingDateUtils
import com.example.data.RentalRepository
import com.example.data.firebase.FirestoreBooking
import com.example.data.models.DeliveryOptionType
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SecondaryCTA
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
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

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun BookingScreen(
    item: RentalItem,
    onBack: () -> Unit,
    onBookingConfirmed: () -> Unit,
    onViewBookings: () -> Unit = {},
    onMessageHost: (RentalOwner, RentalItem) -> Unit = { _, _ -> }
) {
    BackHandler(onBack = onBack)

    val coroutineScope = rememberCoroutineScope()
    val allItems by RentalRepository.itemsFlow.collectAsStateWithLifecycle()
    val currentProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val liveItem = remember(allItems, item) {
        allItems.find { it.id == item.id } ?: item
    }
    val isOwnListing = liveItem.owner.id.isNotBlank() && liveItem.owner.id == currentProfile.uid
    val isUnavailable = !liveItem.availableToday || liveItem.activeBookingId != null

    val todayUtcMillis = remember { BookingDateUtils.todayUtcMidnightMillis() }
    var daysCount by remember { mutableIntStateOf(3) }
    var startDateMillis by remember { mutableLongStateOf(todayUtcMillis) }
    var endDateMillis by remember { mutableLongStateOf(BookingDateUtils.addDaysUtcMillis(todayUtcMillis, 3)) }
    val startDate = remember(startDateMillis) { BookingDateUtils.formatUtcDate(startDateMillis) }
    val endDate = remember(endDateMillis) { BookingDateUtils.formatUtcDate(endDateMillis) }
    val dateValidationError = remember(startDateMillis, endDateMillis, todayUtcMillis) {
        BookingDateUtils.validateRange(startDateMillis, endDateMillis, todayUtcMillis)
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var selectedDelivery by remember { mutableStateOf(DeliveryOptionType.SELF_PICKUP) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var confirmedBooking by remember { mutableStateOf<FirestoreBooking?>(null) }

    val pricing = remember(liveItem.pricePerDay, daysCount, selectedDelivery) {
        BookingDateUtils.calculateBookingPricing(
            pricePerDay = liveItem.pricePerDay,
            daysCount = daysCount,
            deliveryFee = selectedDelivery.fee
        )
    }
    val totalPayable = pricing.totalPayable

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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Share this PIN with ${booking.hostName} at pickup to begin the rental.",
                            style = LoopType.Metadata,
                            color = InkSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    Text(
                        text = "Total Reserved: ₹${booking.totalAmount}",
                        style = LoopType.TitleMedium,
                        color = InkCharcoal
                    )
                    PrimaryCTA(
                        text = "View My Reservations",
                        onClick = onViewBookings,
                        testTag = "booking_view_reservations_btn"
                    )
                    SecondaryCTA(
                        text = "Message Host",
                        onClick = { onMessageHost(liveItem.owner, liveItem) },
                        testTag = "booking_message_host_btn"
                    )
                    androidx.compose.material3.TextButton(
                        onClick = onBookingConfirmed,
                        modifier = Modifier.testTag("booking_done_button")
                    ) {
                        Text(
                            text = "Back to Catalog",
                            style = LoopType.BodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = InkSecondary
                        )
                    }
                }
            } else {
                if (isOwnListing || isUnavailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AccentPeach.copy(alpha = 0.55f))
                            .border(1.dp, AlertCrimson.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                            .testTag("booking_blocked_banner")
                    ) {
                        Text(
                            text = if (isOwnListing) {
                                "You host this listing and cannot book your own item."
                            } else {
                                "This piece is currently unavailable or already booked."
                            },
                            style = LoopType.BodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = AlertCrimson
                        )
                    }
                }

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
                        text = liveItem.category.title.uppercase(),
                        style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                        color = InkMuted
                    )
                    Text(
                        text = liveItem.title,
                        style = LoopType.HeadlineMedium,
                        color = InkCharcoal
                    )
                    val hostText = liveItem.owner.name.ifBlank { "Community Member" }
                    Text(
                        text = if (liveItem.location.isNotBlank()) {
                            "Hosted by $hostText · ${liveItem.location}"
                        } else {
                            "Hosted by $hostText"
                        },
                        style = LoopType.BodySmall,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${liveItem.pricePerDay} / day · Refundable Deposit ₹${liveItem.securityDeposit}",
                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                        color = InkCharcoal
                    )
                }

                // Duration Selector & Multi-Day Tier Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(PaperIvory)
                                    .border(1.dp, PaperBorder, CircleShape)
                                    .clickable {
                                        if (daysCount > BookingDateUtils.MIN_RENTAL_DAYS) {
                                            daysCount--
                                            endDateMillis = BookingDateUtils.addDaysUtcMillis(startDateMillis, daysCount)
                                            errorMessage = null
                                        }
                                    }
                                    .testTag("booking_days_minus"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease days", tint = InkCharcoal, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$daysCount ${if (daysCount == 1) "day" else "days"}",
                                style = LoopType.TitleMedium,
                                color = InkCharcoal
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(PaperIvory)
                                    .border(1.dp, PaperBorder, CircleShape)
                                    .clickable {
                                        if (daysCount < BookingDateUtils.MAX_RENTAL_DAYS) {
                                            daysCount++
                                            endDateMillis = BookingDateUtils.addDaysUtcMillis(startDateMillis, daysCount)
                                            errorMessage = null
                                        }
                                    }
                                    .testTag("booking_days_plus"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase days", tint = InkCharcoal, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val is3PlusSelected = daysCount in 3..6
                        val is7PlusSelected = daysCount >= 7
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (is3PlusSelected) AccentMint else PaperIvory)
                                .border(
                                    1.dp,
                                    if (is3PlusSelected) AccentForestGreen else PaperBorder,
                                    RoundedCornerShape(100.dp)
                                )
                                .clickable {
                                    daysCount = 3
                                    endDateMillis = BookingDateUtils.addDaysUtcMillis(startDateMillis, 3)
                                    errorMessage = null
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("booking_tier_3d"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = if (is3PlusSelected) AccentForestGreen else InkSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "3+ days · 10% off",
                                    style = LoopType.Metadata.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = if (is3PlusSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (is3PlusSelected) AccentForestGreen else InkCharcoal
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (is7PlusSelected) AccentMint else PaperIvory)
                                .border(
                                    1.dp,
                                    if (is7PlusSelected) AccentForestGreen else PaperBorder,
                                    RoundedCornerShape(100.dp)
                                )
                                .clickable {
                                    daysCount = 7
                                    endDateMillis = BookingDateUtils.addDaysUtcMillis(startDateMillis, 7)
                                    errorMessage = null
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("booking_tier_7d"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = if (is7PlusSelected) AccentForestGreen else InkSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "7+ days · 15% off",
                                    style = LoopType.Metadata.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = if (is7PlusSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (is7PlusSelected) AccentForestGreen else InkCharcoal
                                )
                            }
                        }
                    }
                }

                // Calendar Date-Range Selector Card
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PICKUP & RETURN DATES",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Text(
                            text = "Max ${BookingDateUtils.MAX_RENTAL_DAYS} days",
                            style = LoopType.Metadata.copy(fontSize = 11.sp),
                            color = InkMuted
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(PaperPureWhite)
                            .border(
                                1.dp,
                                if (dateValidationError != null) AlertCrimson else PaperBorderSubtle,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { showDatePickerDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("booking_date_picker_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(AccentMint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Select dates",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "$startDate  →  $endDate",
                                    style = LoopType.TitleMedium.copy(fontSize = 14.5.sp),
                                    color = InkCharcoal
                                )
                                Text(
                                    text = "Tap to adjust pickup & return calendar",
                                    style = LoopType.Metadata.copy(fontSize = 11.5.sp),
                                    color = InkSecondary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(PaperIvory)
                                .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Edit",
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                                color = InkCharcoal
                            )
                        }
                    }
                    if (dateValidationError != null) {
                        Text(
                            text = dateValidationError,
                            style = LoopType.Metadata,
                            color = AlertCrimson
                        )
                    }
                }

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
                        Text("₹${liveItem.pricePerDay} × $daysCount ${if (daysCount == 1) "day" else "days"}", style = LoopType.BodySmall, color = InkSecondary)
                        Text("₹${pricing.rentalSubtotal}", style = LoopType.BodySmall, color = InkCharcoal)
                    }
                    if (pricing.discountAmount > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("booking_discount_row"),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Multi-day savings (${pricing.discountPercent}%)",
                                style = LoopType.BodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = AccentForestGreen
                            )
                            Text(
                                text = "-₹${pricing.discountAmount}",
                                style = LoopType.BodySmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentForestGreen
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Community Trust & Service Fee", style = LoopType.BodySmall, color = InkSecondary)
                        Text("₹${pricing.serviceFee}", style = LoopType.BodySmall, color = InkCharcoal)
                    }
                    if (pricing.deliveryFee > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee", style = LoopType.BodySmall, color = InkSecondary)
                            Text("₹${pricing.deliveryFee}", style = LoopType.BodySmall, color = InkCharcoal)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = PaperBorderSubtle)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Due", style = LoopType.TitleMedium, color = InkCharcoal)
                        Text("₹$totalPayable", style = LoopType.PriceHeadline, color = InkCharcoal)
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentMint)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = AccentForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (liveItem.securityDeposit > 0) {
                                "Protected handover PIN + ₹${liveItem.securityDeposit} refundable deposit safeguard"
                            } else {
                                "Protected handover PIN + 5-point return custody check"
                            },
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.Medium),
                            color = AccentForestGreen
                        )
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
                    text = when {
                        isSubmitting -> "Confirming..."
                        isOwnListing -> "Your Own Listing"
                        isUnavailable -> "Currently Unavailable"
                        else -> "Confirm Reservation (₹$totalPayable)"
                    },
                    enabled = !isSubmitting && !isOwnListing && !isUnavailable && dateValidationError == null,
                    onClick = {
                        val validationErr = BookingDateUtils.validateRange(startDateMillis, endDateMillis, todayUtcMillis)
                        if (validationErr != null) {
                            errorMessage = validationErr
                            return@PrimaryCTA
                        }
                        isSubmitting = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = RentalRepository.createBooking(
                                item = liveItem,
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

    if (showDatePickerDialog) {
        val selectableDates = remember(todayUtcMillis) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= todayUtcMillis
                }
            }
        }
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDateMillis,
            initialSelectedEndDateMillis = endDateMillis,
            selectableDates = selectableDates
        )
        val pickedStart = dateRangePickerState.selectedStartDateMillis
        val pickedEnd = dateRangePickerState.selectedEndDateMillis
        val pickerValidationError = BookingDateUtils.validateRange(pickedStart, pickedEnd, todayUtcMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            colors = DatePickerDefaults.colors(containerColor = PaperPureWhite),
            confirmButton = {
                TextButton(
                    enabled = pickedStart != null && pickedEnd != null && pickerValidationError == null,
                    onClick = {
                        if (pickedStart != null && pickedEnd != null) {
                            val normalizedStart = BookingDateUtils.normalizeToUtcMidnight(pickedStart)
                            val computedDays = BookingDateUtils.calculateDurationDays(normalizedStart, pickedEnd)
                            startDateMillis = normalizedStart
                            daysCount = computedDays
                            endDateMillis = BookingDateUtils.addDaysUtcMillis(normalizedStart, computedDays)
                            errorMessage = null
                        }
                        showDatePickerDialog = false
                    },
                    modifier = Modifier.testTag("confirm_date_range_button")
                ) {
                    Text(
                        text = "Apply Dates",
                        color = AccentForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel", color = InkCharcoal)
                }
            }
        ) {
            Column {
                DateRangePicker(
                    state = dateRangePickerState,
                    colors = DatePickerDefaults.colors(containerColor = PaperPureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    title = {
                        Text(
                            text = "Select Pickup & Return",
                            style = LoopType.EditorialTag,
                            color = InkSecondary,
                            modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                        )
                    }
                )
                if (pickerValidationError != null && pickedStart != null && pickedEnd != null) {
                    Text(
                        text = pickerValidationError,
                        style = LoopType.Metadata,
                        color = AlertCrimson,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
