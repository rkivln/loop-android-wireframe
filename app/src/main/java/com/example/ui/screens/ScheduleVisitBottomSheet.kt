package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HouseRentalRepository
import com.example.data.models.HouseListing
import com.example.data.models.HouseVisitRequest
import com.example.data.models.TenantPreference
import com.example.data.models.VisitTimeSlot
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleVisitBottomSheet(
    house: HouseListing,
    bottomBarPadding: Dp = 0.dp,
    onVisitSubmitted: (HouseVisitRequest) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val coroutineScope = rememberCoroutineScope()
    val upcomingDates = remember {
        val fmt = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        val cal = Calendar.getInstance()
        (0 until 14).map { offset ->
            val day = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            fmt.format(day.time)
        }
    }

    val moveInOptions = remember(house.availableFromLabel) {
        val cal = Calendar.getInstance()
        val monthFmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val next1 = (cal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        val next2 = (cal.clone() as Calendar).apply { add(Calendar.MONTH, 2) }
        listOf(
            house.availableFromLabel.ifBlank { "Immediate" },
            monthFmt.format(next1.time),
            monthFmt.format(next2.time),
            "Flexible"
        ).distinct()
    }

    var selectedDate by remember { mutableStateOf(upcomingDates.firstOrNull() ?: "Tomorrow") }
    var selectedTimeSlot by remember { mutableStateOf(VisitTimeSlot.MORNING) }
    var householdType by remember {
        mutableStateOf(
            if (house.tenantPreference == TenantPreference.ANY) TenantPreference.FAMILY else house.tenantPreference
        )
    }
    var occupantsCount by remember { mutableIntStateOf(1) }
    var targetMoveIn by remember { mutableStateOf(moveInOptions.first()) }
    var tenantNote by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomClearance = maxOf(
        imeBottom + 12.dp,
        if (bottomBarPadding > 0.dp) bottomBarPadding + 10.dp else navBottom + 16.dp
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("schedule_visit_sheet"),
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
                        text = "Schedule Property Visit",
                        style = LoopType.HeadlineMedium,
                        color = InkCharcoal
                    )
                    Text(
                        text = "${house.title} · ₹${house.monthlyRent}/mo",
                        style = LoopType.Metadata,
                        color = InkSecondary,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("schedule_visit_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close visit request",
                        tint = InkSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = AccentForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PREFERRED VISIT DATE",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        upcomingDates.forEachIndexed { idx, dateLabel ->
                            val isSelected = selectedDate == dateLabel
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) InkCharcoal else PaperIvory)
                                    .border(
                                        1.dp,
                                        if (isSelected) InkCharcoal else PaperBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedDate = dateLabel }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("visit_date_chip_$idx"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dateLabel,
                                    style = LoopType.Metadata.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) InkWhite else InkCharcoal
                                )
                            }
                        }
                    }
                }

                // Time Slot Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AccentForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TIME WINDOW",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VisitTimeSlot.entries.forEach { slot ->
                            val isSelected = selectedTimeSlot == slot
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) AccentMint else PaperIvory)
                                    .border(
                                        1.dp,
                                        if (isSelected) AccentForestGreen else PaperBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedTimeSlot = slot }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("visit_slot_${slot.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = slot.label,
                                    style = LoopType.Metadata.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) AccentForestGreen else InkCharcoal
                                )
                            }
                        }
                    }
                }

                // Household Profile & Occupants
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "HOUSEHOLD & OCCUPANTS",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TenantPreference.entries.forEach { pref ->
                            val isSelected = householdType == pref
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isSelected) InkCharcoal else PaperIvory)
                                    .border(
                                        1.dp,
                                        if (isSelected) InkCharcoal else PaperBorder,
                                        RoundedCornerShape(100.dp)
                                    )
                                    .clickable { householdType = pref }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("visit_household_${pref.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pref.label,
                                    style = LoopType.Metadata.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) InkWhite else InkCharcoal
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperIvory)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Number of Residents",
                            style = LoopType.BodyMedium,
                            color = InkCharcoal
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .minimumInteractiveComponentSize()
                                    .clip(CircleShape)
                                    .background(PaperPureWhite)
                                    .border(1.dp, PaperBorder, CircleShape)
                                    .clickable { if (occupantsCount > 1) occupantsCount-- }
                                    .testTag("visit_occupants_minus"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease occupants",
                                    tint = InkCharcoal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "$occupantsCount",
                                style = LoopType.TitleMedium,
                                color = InkCharcoal,
                                modifier = Modifier.testTag("visit_occupants_count")
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .minimumInteractiveComponentSize()
                                    .clip(CircleShape)
                                    .background(PaperPureWhite)
                                    .border(1.dp, PaperBorder, CircleShape)
                                    .clickable { if (occupantsCount < 15) occupantsCount++ }
                                    .testTag("visit_occupants_plus"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase occupants",
                                    tint = InkCharcoal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Target Move-In Month
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TARGET MOVE-IN (MIN ${house.minLeaseMonths} MONTHS LEASE)",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moveInOptions.forEach { month ->
                            val isSelected = targetMoveIn == month
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isSelected) AccentMint else PaperIvory)
                                    .border(
                                        1.dp,
                                        if (isSelected) AccentForestGreen else PaperBorder,
                                        RoundedCornerShape(100.dp)
                                    )
                                    .clickable { targetMoveIn = month }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = month,
                                    style = LoopType.Metadata.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) AccentForestGreen else InkCharcoal
                                )
                            }
                        }
                    }
                }

                // Intro Note
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "NOTE TO LANDLORD (OPTIONAL)",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 84.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperIvory)
                            .padding(14.dp)
                    ) {
                        if (tenantNote.isEmpty()) {
                            Text(
                                text = "Introduce your household, work/study schedule, or questions about the flat...",
                                style = LoopType.BodySmall,
                                color = InkMuted
                            )
                        }
                        BasicTextField(
                            value = tenantNote,
                            onValueChange = { if (it.length <= 400) tenantNote = it },
                            textStyle = LoopType.BodyMedium.copy(color = InkCharcoal),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("visit_tenant_note_input")
                        )
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = LoopType.Metadata,
                        color = AlertCrimson
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperPureWhite)
                    .padding(horizontal = 22.dp)
                    .padding(top = 10.dp, bottom = bottomClearance)
            ) {
                PrimaryCTA(
                    text = if (isSubmitting) "Sending Visit Request..." else "Send Visit Request",
                    enabled = !isSubmitting,
                    onClick = {
                        isSubmitting = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = HouseRentalRepository.submitVisitRequest(
                                house = house,
                                preferredDate = selectedDate,
                                timeSlot = selectedTimeSlot,
                                occupantsCount = occupantsCount,
                                householdType = householdType,
                                targetMoveInMonth = targetMoveIn,
                                tenantNote = tenantNote
                            )
                            isSubmitting = false
                            result.fold(
                                onSuccess = { saved -> onVisitSubmitted(saved) },
                                onFailure = { err ->
                                    errorMessage = err.localizedMessage ?: "Failed to submit visit request."
                                }
                            )
                        }
                    },
                    testTag = "submit_visit_request_btn"
                )
            }
        }
    }
}
