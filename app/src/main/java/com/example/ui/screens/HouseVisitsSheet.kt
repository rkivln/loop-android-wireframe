package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HouseRentalRepository
import com.example.data.models.HouseListing
import com.example.data.models.HouseVisitRequest
import com.example.data.models.VisitRequestStatus
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentYellowSoft
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

@Composable
fun HouseVisitsSheet(
    bottomBarPadding: Dp = 0.dp,
    onOpenHouse: (HouseListing) -> Unit = {},
    onMessageCounterparty: (HouseVisitRequest) -> Unit = {},
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val coroutineScope = rememberCoroutineScope()
    val myTenantVisits by HouseRentalRepository.myTenantVisitsFlow.collectAsStateWithLifecycle()
    val incomingVisits by HouseRentalRepository.incomingLandlordVisitsFlow.collectAsStateWithLifecycle()
    val allHouses by HouseRentalRepository.housesFlow.collectAsStateWithLifecycle()
    val isLoading by HouseRentalRepository.isVisitsLoadingFlow.collectAsStateWithLifecycle()

    var selectedTabIndex by remember {
        mutableIntStateOf(if (incomingVisits.isNotEmpty() && myTenantVisits.isEmpty()) 1 else 0)
    }
    var confirmVisitTarget by remember { mutableStateOf<HouseVisitRequest?>(null) }
    var rescheduleVisitTarget by remember { mutableStateOf<HouseVisitRequest?>(null) }
    var addressNoteDraft by remember { mutableStateOf("") }
    var landlordNoteDraft by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf<String?>(null) }

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val sheetBottomPad = if (bottomBarPadding > 0.dp) bottomBarPadding + 10.dp else navBottom + 16.dp

    val displayedVisits = if (selectedTabIndex == 0) myTenantVisits else incomingVisits

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("house_visits_sheet"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(PaperPureWhite)
                .pointerInput(Unit) { detectTapGestures {} }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "House Visits & Inquiries",
                        style = LoopType.HeadlineMedium,
                        color = InkCharcoal
                    )
                    Text(
                        text = "Track walkthrough requests & landlord confirmations",
                        style = LoopType.Metadata,
                        color = InkSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("house_visits_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close visits manager",
                        tint = InkSecondary
                    )
                }
            }

            // Segmented Tabs: My Visits vs Tenant Inquiries
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "My Visits (${myTenantVisits.size})",
                    "Inquiries (${incomingVisits.size})"
                ).forEachIndexed { idx, label ->
                    val selected = selectedTabIndex == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (selected) InkCharcoal else PaperIvory)
                            .border(1.dp, if (selected) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
                            .clickable { selectedTabIndex = idx }
                            .testTag("house_visits_tab_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = LoopType.Metadata.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                            color = if (selected) InkWhite else InkCharcoal
                        )
                    }
                }
            }

            if (!feedbackText.isNullOrBlank()) {
                Text(
                    text = feedbackText.orEmpty(),
                    style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                    color = AccentForestGreen,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp)
                )
            }

            if (isLoading && displayedVisits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentForestGreen)
                }
            } else if (displayedVisits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = InkMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (selectedTabIndex == 0) "No scheduled house visits yet" else "No incoming tenant inquiries yet",
                            style = LoopType.HeadlineMedium.copy(fontSize = 17.sp),
                            color = InkCharcoal
                        )
                        Text(
                            text = if (selectedTabIndex == 0) {
                                "Browse the Houses tab and request a walkthrough on any available long-term rental."
                            } else {
                                "Prospective tenants requesting visits on your homes will appear here."
                            },
                            style = LoopType.BodySmall,
                            color = InkSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 8.dp, bottom = sheetBottomPad),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedVisits, key = { it.id }) { visit ->
                        val isLandlordView = selectedTabIndex == 1
                        val matchingHouse = allHouses.find { it.id == visit.houseId }
                        VisitRequestCard(
                            visit = visit,
                            isLandlordView = isLandlordView,
                            onOpenHouse = { matchingHouse?.let(onOpenHouse) },
                            onMessageCounterparty = { onMessageCounterparty(visit) },
                            onLandlordConfirm = {
                                addressNoteDraft = matchingHouse?.fullAddressNote?.ifBlank { visit.houseLocation }
                                    ?: visit.houseLocation
                                landlordNoteDraft = "Looking forward to showing you the flat."
                                confirmVisitTarget = visit
                            },
                            onLandlordSuggestTime = {
                                landlordNoteDraft = ""
                                rescheduleVisitTarget = visit
                            },
                            onLandlordDecline = {
                                coroutineScope.launch {
                                    HouseRentalRepository.declineVisitByLandlord(
                                        visit.id,
                                        "Home is currently finalized with another applicant."
                                    ).onSuccess { feedbackText = "Visit request declined." }
                                }
                            },
                            onTenantCancel = {
                                coroutineScope.launch {
                                    HouseRentalRepository.cancelVisitByTenant(visit.id)
                                        .onSuccess { feedbackText = "Visit request cancelled." }
                                }
                            },
                            onMarkCompleted = {
                                coroutineScope.launch {
                                    HouseRentalRepository.markVisitCompleted(visit)
                                        .onSuccess { feedbackText = "Visit marked as completed." }
                                }
                            }
                        )
                    }
                }
            }
        }

        confirmVisitTarget?.let { target ->
            AlertDialog(
                onDismissRequest = { confirmVisitTarget = null },
                containerColor = PaperPureWhite,
                title = { Text("Confirm Property Visit", style = LoopType.HeadlineMedium, color = InkCharcoal) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Share exact building directions for ${target.tenantName} (${target.preferredDate} · ${target.preferredTimeSlot.shortWindow}):",
                            style = LoopType.BodySmall,
                            color = InkSecondary
                        )
                        DialogInputField(
                            value = addressNoteDraft,
                            onValueChange = { addressNoteDraft = it },
                            placeholder = "Gate / Flat number & landmark"
                        )
                        DialogInputField(
                            value = landlordNoteDraft,
                            onValueChange = { landlordNoteDraft = it },
                            placeholder = "Optional welcome note"
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val t = target
                            confirmVisitTarget = null
                            coroutineScope.launch {
                                HouseRentalRepository.confirmVisitByLandlord(
                                    visitId = t.id,
                                    responseNote = landlordNoteDraft.trim(),
                                    confirmedAddressNote = addressNoteDraft.trim()
                                ).onSuccess {
                                    feedbackText = "Visit confirmed & directions shared with ${t.tenantName}."
                                }
                            }
                        },
                        modifier = Modifier.testTag("confirm_visit_submit_btn")
                    ) {
                        Text("Confirm Visit", color = AccentForestGreen, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmVisitTarget = null }) {
                        Text("Cancel", color = InkCharcoal)
                    }
                }
            )
        }

        rescheduleVisitTarget?.let { target ->
            AlertDialog(
                onDismissRequest = { rescheduleVisitTarget = null },
                containerColor = PaperPureWhite,
                title = { Text("Suggest Another Time", style = LoopType.HeadlineMedium, color = InkCharcoal) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Propose an alternate visit window to ${target.tenantName}:",
                            style = LoopType.BodySmall,
                            color = InkSecondary
                        )
                        DialogInputField(
                            value = landlordNoteDraft,
                            onValueChange = { landlordNoteDraft = it },
                            placeholder = "e.g. Saturday 4 PM works better"
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val t = target
                        rescheduleVisitTarget = null
                        coroutineScope.launch {
                            HouseRentalRepository.suggestRescheduleByLandlord(
                                visitId = t.id,
                                responseNote = landlordNoteDraft.trim().ifBlank { "Let's coordinate an alternate time in chat." }
                            ).onSuccess {
                                feedbackText = "Reschedule suggestion sent to ${t.tenantName}."
                            }
                        }
                    }) {
                        Text("Send Suggestion", color = InkCharcoal, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { rescheduleVisitTarget = null }) {
                        Text("Cancel", color = InkSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun VisitRequestCard(
    visit: HouseVisitRequest,
    isLandlordView: Boolean,
    onOpenHouse: () -> Unit,
    onMessageCounterparty: () -> Unit,
    onLandlordConfirm: () -> Unit,
    onLandlordSuggestTime: () -> Unit,
    onLandlordDecline: () -> Unit,
    onTenantCancel: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    val statusBg = when (visit.status) {
        VisitRequestStatus.CONFIRMED, VisitRequestStatus.COMPLETED -> AccentMint
        VisitRequestStatus.REQUESTED -> AccentYellowSoft
        VisitRequestStatus.RESCHEDULE_SUGGESTED -> AccentPeach
        VisitRequestStatus.DECLINED, VisitRequestStatus.CANCELLED -> AccentBeigeOat
    }
    val statusColor = when (visit.status) {
        VisitRequestStatus.CONFIRMED, VisitRequestStatus.COMPLETED -> AccentForestGreen
        VisitRequestStatus.DECLINED, VisitRequestStatus.CANCELLED -> InkSecondary
        else -> InkCharcoal
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PaperIvory)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("visit_card_${visit.id}"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = visit.status.label.uppercase(),
                    style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                    color = statusColor
                )
            }
            Text(
                text = "₹${visit.monthlyRent}/mo",
                style = LoopType.PriceSmall,
                color = InkCharcoal
            )
        }

        Text(
            text = visit.houseTitle,
            style = LoopType.TitleMedium,
            color = InkCharcoal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable(onClick = onOpenHouse)
        )

        Text(
            text = "${visit.preferredDate} · ${visit.preferredTimeSlot.shortWindow} · Move-in: ${visit.targetMoveInMonth}",
            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
            color = InkCharcoal
        )

        Text(
            text = if (isLandlordView) {
                "Applicant: ${visit.tenantName} (${visit.householdType.label}, ${visit.occupantsCount} ${if (visit.occupantsCount == 1) "person" else "people"})"
            } else {
                "Landlord: ${visit.landlordName} · ${visit.houseLocation}"
            },
            style = LoopType.BodySmall,
            color = InkSecondary
        )

        if (visit.tenantNote.isNotBlank()) {
            Text(
                text = "\"${visit.tenantNote}\"",
                style = LoopType.BodySmall,
                color = InkSecondary
            )
        }

        if (visit.status == VisitRequestStatus.CONFIRMED && visit.confirmedAddressNote.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PaperPureWhite)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Confirmed visit address",
                    tint = AccentForestGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = visit.confirmedAddressNote,
                    style = LoopType.BodySmall,
                    color = InkCharcoal
                )
            }
        } else if (visit.landlordResponseNote.isNotBlank()) {
            Text(
                text = "Landlord response: ${visit.landlordResponseNote}",
                style = LoopType.BodySmall,
                color = AccentForestGreen
            )
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onMessageCounterparty,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("Message", maxLines = 1)
            }

            if (isLandlordView) {
                if (visit.status == VisitRequestStatus.REQUESTED || visit.status == VisitRequestStatus.RESCHEDULE_SUGGESTED) {
                    Button(
                        onClick = onLandlordConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentForestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("visit_confirm_btn_${visit.id}")
                    ) {
                        Text("Confirm", maxLines = 1, color = InkWhite)
                    }
                } else if (visit.status == VisitRequestStatus.CONFIRMED) {
                    Button(
                        onClick = onMarkCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = InkCharcoal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text("Visited", maxLines = 1, color = InkWhite)
                    }
                }
            } else if (!visit.status.isTerminal) {
                OutlinedButton(
                    onClick = if (visit.status == VisitRequestStatus.CONFIRMED) onMarkCompleted else onTenantCancel,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("visit_tenant_action_${visit.id}")
                ) {
                    Text(
                        text = if (visit.status == VisitRequestStatus.CONFIRMED) "Mark Visited" else "Cancel Visit",
                        color = if (visit.status == VisitRequestStatus.CONFIRMED) AccentForestGreen else AlertCrimson,
                        maxLines = 1
                    )
                }
            }
        }

        if (isLandlordView && (visit.status == VisitRequestStatus.REQUESTED || visit.status == VisitRequestStatus.RESCHEDULE_SUGGESTED)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onLandlordSuggestTime) {
                    Text("Suggest Another Time", color = InkCharcoal, fontSize = 12.sp)
                }
                TextButton(onClick = onLandlordDecline) {
                    Text("Decline", color = AlertCrimson, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DialogInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PaperIvory)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, style = LoopType.BodySmall, color = InkMuted)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LoopType.BodySmall.copy(color = InkCharcoal),
            cursorBrush = SolidColor(InkCharcoal),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
