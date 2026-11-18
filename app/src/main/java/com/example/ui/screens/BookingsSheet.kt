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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RentalItem
import com.example.data.models.UserRentalBooking
import com.example.ui.components.BookingsSkeletonList
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
import com.example.ui.theme.AccentWarmYellow
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

private enum class BookingsRoleFilter(val label: String) {
    ALL("All"),
    BORROWING("Borrowing"),
    HOSTING("Hosting")
}

@Composable
fun BookingsModalSheet(
    bookings: List<UserRentalBooking>,
    currentUserId: String = "",
    highlightedBookingId: String? = null,
    isLoading: Boolean = false,
    onVerifyHandoverPin: suspend (bookingId: String, pin: String) -> Result<Unit> = { _, _ -> Result.success(Unit) },
    onCompleteBooking: suspend (bookingId: String) -> Result<Unit> = { Result.success(Unit) },
    onCancelBooking: suspend (bookingId: String) -> Result<Unit> = { Result.success(Unit) },
    onSubmitReview: suspend (bookingId: String, rating: Int, comment: String) -> Result<Unit> = { _, _, _ -> Result.success(Unit) },
    onOpenItem: (RentalItem) -> Unit = {},
    onMessageCounterparty: (UserRentalBooking) -> Unit = {},
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val coroutineScope = rememberCoroutineScope()
    var roleFilter by remember { mutableStateOf(BookingsRoleFilter.ALL) }
    var pinDialogBooking by remember { mutableStateOf<UserRentalBooking?>(null) }
    var reviewDialogBooking by remember { mutableStateOf<UserRentalBooking?>(null) }
    var actionBusyBookingId by remember { mutableStateOf<String?>(null) }
    var sheetError by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    val filteredBookings = remember(bookings, roleFilter, currentUserId) {
        when (roleFilter) {
            BookingsRoleFilter.ALL -> bookings
            BookingsRoleFilter.BORROWING -> bookings.filter { currentUserId.isBlank() || it.hostId != currentUserId }
            BookingsRoleFilter.HOSTING -> bookings.filter { currentUserId.isNotBlank() && it.hostId == currentUserId }
        }
    }

    LaunchedEffect(highlightedBookingId, bookings, filteredBookings) {
        val targetId = highlightedBookingId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        val existsInAll = bookings.any { it.id == targetId }
        if (existsInAll) {
            val filteredIndex = filteredBookings.indexOfFirst { it.id == targetId }
            if (filteredIndex >= 0) {
                listState.animateScrollToItem(filteredIndex)
            } else if (roleFilter != BookingsRoleFilter.ALL) {
                roleFilter = BookingsRoleFilter.ALL
            }
        }
    }

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
                        text = "Reservations (${bookings.size})",
                        style = LoopType.HeadlineMedium,
                        color = InkCharcoal
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BookingsRoleFilter.entries.forEach { filter ->
                        val isSelected = roleFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) InkCharcoal else PaperIvory)
                                .border(
                                    1.dp,
                                    if (isSelected) InkCharcoal else PaperBorder,
                                    RoundedCornerShape(100.dp)
                                )
                                .clickable { roleFilter = filter }
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                                .testTag("bookings_filter_${filter.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.label,
                                style = LoopType.Metadata.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) InkWhite else InkCharcoal
                            )
                        }
                    }
                }

                if (sheetError != null) {
                    Text(
                        text = sheetError ?: "",
                        style = LoopType.BodySmall,
                        color = AlertCrimson
                    )
                }

                if (isLoading && bookings.isEmpty()) {
                    BookingsSkeletonList(
                        count = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    )
                } else if (filteredBookings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (roleFilter) {
                                BookingsRoleFilter.HOSTING -> "No incoming reservations on your hosted gear yet."
                                BookingsRoleFilter.BORROWING -> "You haven't reserved any pieces yet."
                                BookingsRoleFilter.ALL -> "No gear reservations yet. Book an item from the catalog to track pickup PINs and handovers here."
                            },
                            style = LoopType.BodySmall,
                            color = InkSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.height(380.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredBookings, key = { it.id }) { booking ->
                            val isHost = currentUserId.isNotBlank() && booking.hostId == currentUserId
                            val isConfirmed = booking.status.equals("Confirmed", ignoreCase = true)
                            val isActive = booking.status.equals("Active", ignoreCase = true)
                            val isCompletedStatus = booking.status.equals("Completed", ignoreCase = true)
                            val isCancelled = booking.status.equals("Cancelled", ignoreCase = true)
                            val isBusy = actionBusyBookingId == booking.id
                            val isHighlighted = !highlightedBookingId.isNullOrBlank() && booking.id == highlightedBookingId

                            val statusBg = when {
                                isActive -> AccentPowderBlue
                                isCompletedStatus -> AccentMint
                                isCancelled -> AccentPeach
                                else -> AccentBeigeOat
                            }
                            val statusFg = when {
                                isActive -> InkCharcoal
                                isCompletedStatus -> AccentForestGreen
                                isCancelled -> AlertCrimson
                                else -> InkCharcoal
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(PaperIvory)
                                    .border(
                                        width = if (isHighlighted) 2.dp else 1.dp,
                                        color = if (isHighlighted) AccentForestGreen else PaperBorderSubtle,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .padding(14.dp)
                                    .testTag("booking_row_${booking.id}")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    onDismiss()
                                                    onOpenItem(booking.item)
                                                }
                                                .padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = booking.item.title,
                                                style = LoopType.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = InkCharcoal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "›",
                                                style = LoopType.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = AccentForestGreen
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(100.dp))
                                                    .background(PaperPureWhite)
                                                    .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = if (isHost) "HOSTING" else "BORROWING",
                                                    style = LoopType.Metadata.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                                                    color = InkSecondary
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(100.dp))
                                                    .background(statusBg)
                                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = booking.status,
                                                    style = LoopType.Metadata.copy(
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = statusFg
                                                )
                                            }
                                        }
                                    }

                                    val counterpartyLine = if (isHost) {
                                        "Renter: ${booking.renterName.ifBlank { "Member" }}"
                                    } else {
                                        "Host: ${booking.hostName.ifBlank { "Host" }}"
                                    }
                                    Text(
                                        text = "$counterpartyLine · ${booking.startDate} – ${booking.endDate}",
                                        style = LoopType.BodySmall,
                                        color = InkSecondary
                                    )
                                    Text(
                                        text = "Total: ₹${booking.totalPaid} · ${booking.deliveryOption}",
                                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                        color = InkCharcoal
                                    )

                                    if (isConfirmed && !isHost) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(AccentMint)
                                                .border(1.dp, AccentForestGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "Show to host on pickup: PIN #${booking.handoverOtp}",
                                                style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                                                color = AccentForestGreen
                                            )
                                        }
                                    } else if (isActive && !isHost) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(AccentPowderBlue.copy(alpha = 0.6f))
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "Rental active — return gear to ${booking.hostName.ifBlank { "host" }} when finished",
                                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                                color = InkCharcoal
                                            )
                                        }
                                    } else if (isCompletedStatus && booking.isReviewed) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(PaperPureWhite)
                                                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = AccentWarmYellow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = buildString {
                                                    append("Rated ${booking.ratingGiven}/5")
                                                    if (booking.reviewComment.isNotBlank()) {
                                                        append(" · \"${booking.reviewComment}\"")
                                                    }
                                                },
                                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                                color = InkCharcoal,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Action Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        when {
                                            isConfirmed && isHost -> {
                                                Button(
                                                    onClick = { pinDialogBooking = booking },
                                                    enabled = !isBusy,
                                                    colors = ButtonDefaults.buttonColors(containerColor = InkCharcoal),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(min = 48.dp)
                                                        .testTag("verify_pin_btn_${booking.id}")
                                                ) {
                                                    Text("Verify PIN", maxLines = 1)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        actionBusyBookingId = booking.id
                                                        sheetError = null
                                                        coroutineScope.launch {
                                                            onCancelBooking(booking.id).onFailure {
                                                                sheetError = it.localizedMessage ?: "Cancel failed."
                                                            }
                                                            actionBusyBookingId = null
                                                        }
                                                    },
                                                    enabled = !isBusy,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(min = 48.dp)
                                                        .testTag("cancel_booking_btn_${booking.id}")
                                                ) {
                                                    Text("Cancel", maxLines = 1)
                                                }
                                            }
                                            isConfirmed && !isHost -> {
                                                OutlinedButton(
                                                    onClick = {
                                                        onDismiss()
                                                        onMessageCounterparty(booking)
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(min = 48.dp)
                                                        .testTag("message_host_booking_btn_${booking.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Message Host", maxLines = 1)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        actionBusyBookingId = booking.id
                                                        sheetError = null
                                                        coroutineScope.launch {
                                                            onCancelBooking(booking.id).onFailure {
                                                                sheetError = it.localizedMessage ?: "Cancel failed."
                                                            }
                                                            actionBusyBookingId = null
                                                        }
                                                    },
                                                    enabled = !isBusy,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(min = 48.dp)
                                                        .testTag("cancel_booking_btn_${booking.id}")
                                                ) {
                                                    Text("Cancel", maxLines = 1)
                                                }
                                            }
                                            isActive && isHost -> {
                                                Button(
                                                    onClick = {
                                                        actionBusyBookingId = booking.id
                                                        sheetError = null
                                                        coroutineScope.launch {
                                                            onCompleteBooking(booking.id).onFailure {
                                                                sheetError = it.localizedMessage ?: "Completion failed."
                                                            }
                                                            actionBusyBookingId = null
                                                        }
                                                    },
                                                    enabled = !isBusy,
                                                    colors = ButtonDefaults.buttonColors(containerColor = AccentForestGreen),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(min = 48.dp)
                                                        .testTag("mark_returned_btn_${booking.id}")
                                                ) {
                                                    Text("Mark Returned", maxLines = 1)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onDismiss()
                                                        onMessageCounterparty(booking)
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(min = 48.dp)
                                                ) {
                                                    Text("Message Renter", maxLines = 1)
                                                }
                                            }
                                            isActive && !isHost -> {
                                                OutlinedButton(
                                                    onClick = {
                                                        onDismiss()
                                                        onMessageCounterparty(booking)
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(min = 48.dp)
                                                ) {
                                                    Text("Message Host")
                                                }
                                            }
                                            isCompletedStatus && !isHost && !booking.isReviewed -> {
                                                Button(
                                                    onClick = { reviewDialogBooking = booking },
                                                    colors = ButtonDefaults.buttonColors(containerColor = InkCharcoal),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(min = 48.dp)
                                                        .testTag("rate_review_btn_${booking.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Star,
                                                        contentDescription = null,
                                                        tint = AccentWarmYellow,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Rate & Review Gear")
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

    pinDialogBooking?.let { booking ->
        var enteredPin by remember(booking.id) { mutableStateOf("") }
        var pinError by remember(booking.id) { mutableStateOf<String?>(null) }
        var isVerifying by remember(booking.id) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isVerifying) pinDialogBooking = null },
            containerColor = PaperPureWhite,
            titleContentColor = InkCharcoal,
            textContentColor = InkSecondary,
            title = {
                Text(
                    text = "Verify Handover PIN",
                    style = LoopType.HeadlineMedium.copy(fontSize = 18.sp)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the 4-digit handover PIN shown by ${booking.renterName.ifBlank { "the renter" }} to confirm pickup of ${booking.item.title}.",
                        style = LoopType.BodySmall
                    )
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { next ->
                            enteredPin = next.filter { it.isDigit() }.take(4)
                            pinError = null
                        },
                        label = { Text("4-digit PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("handover_pin_input")
                    )
                    if (pinError != null) {
                        Text(
                            text = pinError ?: "",
                            style = LoopType.BodySmall,
                            color = AlertCrimson
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isVerifying && enteredPin.length == 4,
                    onClick = {
                        isVerifying = true
                        pinError = null
                        coroutineScope.launch {
                            val result = onVerifyHandoverPin(booking.id, enteredPin)
                            isVerifying = false
                            result.fold(
                                onSuccess = { pinDialogBooking = null },
                                onFailure = { pinError = it.localizedMessage ?: "Invalid PIN." }
                            )
                        }
                    },
                    modifier = Modifier.testTag("confirm_handover_pin_btn")
                ) {
                    Text(
                        text = if (isVerifying) "Verifying..." else "Start Rental",
                        color = AccentForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isVerifying,
                    onClick = { pinDialogBooking = null }
                ) {
                    Text("Cancel", color = InkCharcoal)
                }
            }
        )
    }

    reviewDialogBooking?.let { booking ->
        var selectedStars by remember(booking.id) { mutableIntStateOf(5) }
        var comment by remember(booking.id) { mutableStateOf("") }
        var reviewError by remember(booking.id) { mutableStateOf<String?>(null) }
        var isSubmitting by remember(booking.id) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) reviewDialogBooking = null },
            containerColor = PaperPureWhite,
            titleContentColor = InkCharcoal,
            textContentColor = InkSecondary,
            title = {
                Text(
                    text = "Rate ${booking.item.title}",
                    style = LoopType.HeadlineMedium.copy(fontSize = 18.sp)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "How was the gear condition and handover with ${booking.hostName.ifBlank { "the host" }}?",
                        style = LoopType.BodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (star in 1..5) {
                            IconButton(
                                onClick = { selectedStars = star },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("review_star_$star")
                            ) {
                                Icon(
                                    imageVector = if (star <= selectedStars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "$star stars",
                                    tint = if (star <= selectedStars) AccentWarmYellow else InkMuted,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { if (it.length <= 240) comment = it },
                        label = { Text("Share quick notes (optional)") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_comment_input")
                    )
                    if (reviewError != null) {
                        Text(
                            text = reviewError ?: "",
                            style = LoopType.BodySmall,
                            color = AlertCrimson
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isSubmitting,
                    onClick = {
                        isSubmitting = true
                        reviewError = null
                        coroutineScope.launch {
                            val result = onSubmitReview(booking.id, selectedStars, comment)
                            isSubmitting = false
                            result.fold(
                                onSuccess = { reviewDialogBooking = null },
                                onFailure = { reviewError = it.localizedMessage ?: "Failed to submit review." }
                            )
                        }
                    },
                    modifier = Modifier.testTag("submit_review_btn")
                ) {
                    Text(
                        text = if (isSubmitting) "Submitting..." else "Submit Review",
                        color = AccentForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSubmitting,
                    onClick = { reviewDialogBooking = null }
                ) {
                    Text("Cancel", color = InkCharcoal)
                }
            }
        )
    }
}
