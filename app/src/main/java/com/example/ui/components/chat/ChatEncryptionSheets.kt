package com.example.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PeerEncryptionState
import com.example.data.models.DisappearingMessageTimer
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentYellowDark
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

@Composable
fun ChatLockStatusBadge(
    encryptionState: PeerEncryptionState?,
    e2eeEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isKeyChanged = encryptionState?.keyChangedWarning == true
    val isVerified = encryptionState?.isVerified == true
    val isReady = e2eeEnabled && encryptionState?.isE2eeReady == true

    val bgColor: Color
    val borderColor: Color
    val contentColor: Color
    val icon = when {
        isKeyChanged -> Icons.Default.Warning
        isVerified -> Icons.Default.VerifiedUser
        isReady -> Icons.Default.Lock
        else -> Icons.Default.LockClock
    }
    val label = when {
        isKeyChanged -> "Key Changed"
        isVerified -> "Verified E2EE"
        isReady -> "E2EE Active"
        else -> "Key Pending"
    }

    when {
        isKeyChanged -> {
            bgColor = AccentPeach
            borderColor = AlertCrimson.copy(alpha = 0.35f)
            contentColor = AlertCrimson
        }
        isVerified -> {
            bgColor = AccentMint
            borderColor = AccentForestGreen.copy(alpha = 0.35f)
            contentColor = AccentForestGreen
        }
        isReady -> {
            bgColor = AccentMint.copy(alpha = 0.65f)
            borderColor = AccentForestGreen.copy(alpha = 0.2f)
            contentColor = AccentForestGreen
        }
        else -> {
            bgColor = PaperPureWhite
            borderColor = PaperBorder
            contentColor = InkSecondary
        }
    }

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag("chat_e2ee_badge"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(100.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SafetyNumberVerificationModal(
    peerName: String,
    encryptionState: PeerEncryptionState?,
    ownFingerprint: String,
    onVerifyToggle: (Boolean) -> Unit,
    onCopySafetyNumber: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val safetyInfo = encryptionState?.safetyNumber
    val isVerified = encryptionState?.isVerified == true
    val keyChanged = encryptionState?.keyChangedWarning == true
    val peerFp = encryptionState?.peerFingerprint.orEmpty()
    val ownFp = ownFingerprint.ifBlank { encryptionState?.ownFingerprint.orEmpty() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("safety_number_modal"),
            shape = RoundedCornerShape(26.dp),
            color = PaperPureWhite,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        keyChanged -> AccentPeach
                                        isVerified -> AccentMint
                                        else -> PaperIvory
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    keyChanged -> Icons.Default.Warning
                                    isVerified -> Icons.Default.VerifiedUser
                                    else -> Icons.Default.Security
                                },
                                contentDescription = null,
                                tint = when {
                                    keyChanged -> AlertCrimson
                                    isVerified -> AccentForestGreen
                                    else -> InkCharcoal
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Safety Number",
                                style = LoopType.HeadlineMedium.copy(fontSize = 18.sp),
                                color = InkCharcoal
                            )
                            Text(
                                text = "Conversation with $peerName",
                                style = LoopType.Metadata.copy(fontSize = 11.5.sp),
                                color = InkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("safety_modal_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close safety number modal",
                            tint = InkCharcoal
                        )
                    }
                }

                if (keyChanged) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AccentYellowSoft)
                            .border(1.dp, AccentYellowDark.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AccentYellowDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$peerName's encryption identity key changed since you last verified them. Compare the 60 digits below before sharing sensitive details.",
                            style = LoopType.BodySmall,
                            color = InkCharcoal
                        )
                    }
                }

                if (safetyInfo != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(PaperIvory)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        safetyInfo.chunks.chunked(3).forEach { rowChunks ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowChunks.forEach { block ->
                                    Text(
                                        text = block,
                                        style = LoopType.TitleMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = InkCharcoal
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = PaperBorderSubtle,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { onCopySafetyNumber(safetyInfo.singleLine) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("copy_safety_number_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy safety number",
                                tint = AccentForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Copy 60-Digit Safety Number",
                                style = LoopType.Metadata.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AccentForestGreen
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PaperIvory)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockClock,
                            contentDescription = null,
                            tint = InkSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Awaiting $peerName's device key",
                            style = LoopType.TitleMedium.copy(fontSize = 14.5.sp),
                            color = InkCharcoal
                        )
                        Text(
                            text = "As soon as $peerName signs in on this version of Loop, their P-256 public key will sync automatically for full end-to-end encryption.",
                            style = LoopType.BodySmall,
                            color = InkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FingerprintRow(label = "Your Identity Key", fingerprint = ownFp.ifBlank { "Generating..." })
                    HorizontalDivider(color = PaperBorderSubtle)
                    FingerprintRow(
                        label = "$peerName's Identity Key",
                        fingerprint = peerFp.ifBlank { "Not published yet" }
                    )
                }

                Text(
                    text = "Messages, pickup pins, and shared photos are encrypted with P-256 ECDH + AES-256-GCM before leaving your device.",
                    style = LoopType.BodySmall,
                    color = InkSecondary
                )

                if (safetyInfo != null && peerFp.isNotBlank()) {
                    if (isVerified) {
                        OutlinedButton(
                            onClick = { onVerifyToggle(false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("unverify_safety_number_btn"),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = AccentForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verified · Tap to Clear Verification",
                                style = LoopType.Metadata.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                color = AccentForestGreen
                            )
                        }
                    } else {
                        Button(
                            onClick = { onVerifyToggle(true) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("verify_safety_number_btn"),
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentForestGreen,
                                contentColor = InkWhite
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mark as Verified",
                                style = LoopType.Metadata.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FingerprintRow(label: String, fingerprint: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = InkSecondary
        )
        Text(
            text = fingerprint,
            style = LoopType.Metadata.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            color = InkCharcoal
        )
    }
}

@Composable
fun DisappearingTimerSheet(
    title: String = "Disappearing Messages",
    subtitle: String = "New encrypted messages sent in this conversation will expire automatically after the selected duration.",
    selectedSeconds: Long,
    onSelectTimer: (DisappearingMessageTimer) -> Unit,
    onDismiss: () -> Unit
) {
    val activeTimer = DisappearingMessageTimer.fromSecondsOrDefault(selectedSeconds)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("disappearing_timer_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = PaperPureWhite,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AccentMint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = AccentForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = LoopType.HeadlineMedium.copy(fontSize = 18.sp),
                            color = InkCharcoal
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close disappearing messages picker",
                            tint = InkCharcoal
                        )
                    }
                }

                Text(
                    text = subtitle,
                    style = LoopType.BodySmall,
                    color = InkSecondary
                )

                DisappearingMessageTimer.entries.forEach { option ->
                    val isSelected = option == activeTimer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AccentMint.copy(alpha = 0.5f) else PaperIvory)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AccentForestGreen else PaperBorderSubtle,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onSelectTimer(option)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("timer_option_${option.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.label,
                                style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                                color = InkCharcoal
                            )
                            Text(
                                text = option.subtitle,
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentForestGreen else Color.Transparent)
                                .border(
                                    width = if (isSelected) 0.dp else 1.5.dp,
                                    color = if (isSelected) AccentForestGreen else PaperBorder,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = InkWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyEncryptionKeyDialog(
    ownFingerprint: String,
    onCopyFingerprint: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("my_encryption_key_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = PaperPureWhite,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AccentMint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = AccentForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Device Encryption Key",
                                style = LoopType.HeadlineMedium.copy(fontSize = 18.sp),
                                color = InkCharcoal
                            )
                            Text(
                                text = "P-256 ECDH · Android Keystore Protected",
                                style = LoopType.Metadata.copy(fontSize = 11.sp),
                                color = InkSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close key dialog",
                            tint = InkCharcoal
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SHA-256 KEY FINGERPRINT",
                        style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                        color = InkMuted
                    )
                    Text(
                        text = ownFingerprint.ifBlank { "Generating identity key..." },
                        style = LoopType.TitleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = InkCharcoal
                    )
                }

                Text(
                    text = "Your private key stays encrypted inside Android Keystore on this device and never leaves your phone. Share or compare this fingerprint or any thread's 60-digit Safety Number with verified neighbors.",
                    style = LoopType.BodySmall,
                    color = InkSecondary
                )

                if (ownFingerprint.isNotBlank()) {
                    Button(
                        onClick = { onCopyFingerprint(ownFingerprint) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("copy_own_fingerprint_btn"),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InkCharcoal,
                            contentColor = InkWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Copy Fingerprint",
                            style = LoopType.Metadata.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
