package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite

@Composable
fun NotificationPermissionPromptDialog(
    onEnableNotifications: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperPureWhite,
        titleContentColor = InkCharcoal,
        textContentColor = InkSecondary,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AccentMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = AccentForestGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Stay in the Loop",
                        style = LoopType.HeadlineMedium.copy(fontSize = 19.sp),
                        color = InkCharcoal
                    )
                    Text(
                        text = "REAL-TIME MOBILE ALERTS",
                        style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                        color = AccentForestGreen
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Turn on push notifications so you never miss time-sensitive neighbor activity:",
                    style = LoopType.BodyMedium,
                    color = InkSecondary
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("• Instant neighbor chat replies & inquiries", style = LoopType.BodySmall, color = InkCharcoal)
                    Text("• Booking confirmations & 4-digit handover PINs", style = LoopType.BodySmall, color = InkCharcoal)
                    Text("• Community drops, meetups & RSVPs", style = LoopType.BodySmall, color = InkCharcoal)
                    Text("• Host payouts & new gear reviews", style = LoopType.BodySmall, color = InkCharcoal)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onEnableNotifications,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentForestGreen,
                    contentColor = InkWhite
                ),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("notif_prompt_enable_btn")
            ) {
                Text(
                    text = "Enable Alerts",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("notif_prompt_later_btn")
            ) {
                Text(
                    text = "Not Now",
                    color = InkSecondary
                )
            }
        }
    )
}
