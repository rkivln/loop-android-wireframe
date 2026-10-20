package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RentalItem
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoopType
import com.example.ui.theme.ParchmentBg
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg

data class InspectionPoint(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val initialPass: Boolean = true
)

@Composable
fun GearInspectionModal(
    item: RentalItem,
    onCompleteInspection: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkPoints = remember {
        listOf(
            InspectionPoint("sensor", "Sensor Glass & Cleanliness", "Checked for sensor dust & scratch-free glass", Icons.Default.CameraAlt),
            InspectionPoint("lens", "Optics & Aperture Blades", "Front/rear elements pristine, zoom ring smooth", Icons.Default.ZoomIn),
            InspectionPoint("battery", "Battery Health & Charger", "Tested at 100% capacity, original LP-E17", Icons.Default.BatteryChargingFull),
            InspectionPoint("storage", "SD Slot & Write Speed", "SanDisk Extreme Pro V30 benchmark verified", Icons.Default.SdCard),
            InspectionPoint("body", "Mount Pins & Lock System", "Gold electronic contacts clean, tight lens lock", Icons.Default.Security)
        )
    }

    val passedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            checkPoints.forEach { put(it.id, true) }
        }
    }

    var isSigned by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(ParchmentBg)
                .clickable(enabled = false) {}
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentYellow)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "HANDOVER PROTOCOL",
                                        style = LoopType.CaptionTechnical,
                                        color = InkBlack
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Condition Inspection Checklist",
                                style = LoopType.H1
                            )
                            Text(
                                text = "Verify hardware state with owner at pickup for zero-liability protection",
                                style = LoopType.BodySmall
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = InkBlack
                            )
                        }
                    }
                }

                // Gear Mini Summary Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ParchmentWhite)
                            .border(1.dp, ParchmentBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.title,
                                    style = LoopType.H3
                                )
                                Text(
                                    text = "Host: ${item.owner.name} · White Town Hub",
                                    style = LoopType.BodySmall
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusGreenBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "5 / 5 POINTS OK",
                                    style = LoopType.CaptionTechnical,
                                    color = StatusGreen
                                )
                            }
                        }
                    }
                }

                // Checklist Items
                items(checkPoints, key = { it.id }) { point ->
                    val isChecked = passedStates[point.id] ?: true

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ParchmentWhite)
                            .border(
                                1.dp,
                                if (isChecked) ParchmentBorder else Color(0xFFEF4444),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                passedStates[point.id] = !isChecked
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChecked) StatusGreenBg else Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = point.icon,
                                        contentDescription = null,
                                        tint = if (isChecked) StatusGreen else Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = point.title,
                                        style = LoopType.BodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = point.subtitle,
                                        style = LoopType.BodySmall,
                                        color = InkSecondary
                                    )
                                }
                            }

                            // Status Check Box
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (isChecked) StatusGreen else Color(0xFFEF4444)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Digital Dual Signature Signoff Box
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ParchmentWhite)
                            .border(1.dp, ParchmentBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DIGITAL CUSTODY SIGN-OFF",
                                    style = LoopType.CaptionTechnical,
                                    color = InkBlack
                                )
                                Text(
                                    text = "ENCRYPTED LEDGER",
                                    style = LoopType.CaptionTechnical,
                                    color = StatusGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "By confirming, both parties agree the hardware matches the listed condition with zero undisclosed defects.",
                                style = LoopType.BodySmall
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSigned) StatusGreenBg else ParchmentBg)
                                    .border(1.dp, if (isSigned) StatusGreen else ParchmentBorder, RoundedCornerShape(8.dp))
                                    .clickable { isSigned = !isSigned }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSigned) Icons.Default.CheckCircle else Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = if (isSigned) StatusGreen else InkBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSigned) "✓ Digitally Signed by Renter & Host" else "Tap to Sign Handover Receipt",
                                        style = LoopType.BodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSigned) StatusGreen else InkBlack
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ParchmentWhite)
                                .border(1.dp, ParchmentBorder, RoundedCornerShape(10.dp))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                style = LoopType.BodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(InkBlack)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = Color.White),
                                    onClick = onCompleteInspection
                                )
                                .testTag("confirm_handover_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Confirm & Lock Custody",
                                style = LoopType.ButtonLabel
                            )
                        }
                    }
                }
            }
        }
    }
}
