package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class CompatibleAccessory(
    val id: String,
    val title: String,
    val pricePerDay: Int,
    val icon: ImageVector,
    val note: String
)

@Composable
fun SmartKitBundler(
    baseItemTitle: String,
    onBundlePriceChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accessories = remember {
        listOf(
            CompatibleAccessory("acc_lens", "50mm f/1.8 Portrait Prime", 200, Icons.Default.ZoomIn, "Creamy bokeh portraits"),
            CompatibleAccessory("acc_mic", "Røde VideoMic GO II", 150, Icons.Default.Mic, "Directional shotgun audio"),
            CompatibleAccessory("acc_batt", "Dual LP-E17 + USB Hub", 80, Icons.Default.BatteryChargingFull, "Full-day shoot power"),
            CompatibleAccessory("acc_sd", "128GB Extreme Pro V30", 50, Icons.Default.SdCard, "4K 60fps recording ready")
        )
    }

    val selectedMap = remember { mutableStateMapOf<String, Boolean>("acc_lens" to true) }

    val addedTotal = remember(selectedMap) {
        accessories.filter { selectedMap[it.id] == true }.sumOf { it.pricePerDay }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ParchmentWhite)
            .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SMART KIT BUILDER",
                            style = LoopType.CaptionTechnical,
                            color = InkBlack
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentYellow)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BUNDLE 15% OFF",
                                style = LoopType.CaptionTechnical,
                                color = InkBlack
                            )
                        }
                    }
                    Text(
                        text = "Compatible Add-ons for $baseItemTitle",
                        style = LoopType.H3
                    )
                }

                if (addedTotal > 0) {
                    Text(
                        text = "+₹$addedTotal/d",
                        style = LoopType.PriceCard,
                        color = StatusGreen
                    )
                }
            }

            // Accessories List
            accessories.forEach { item ->
                val isSelected = selectedMap[item.id] == true

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ParchmentBg else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) InkBlack else ParchmentBorder.copy(alpha = 0.6f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedMap[item.id] = !isSelected
                            val newTotal = accessories.filter { selectedMap[it.id] == true }.sumOf { it.pricePerDay }
                            onBundlePriceChanged(newTotal)
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = InkBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = item.title,
                                style = LoopType.BodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = item.note,
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "+₹${item.pricePerDay}/d",
                            style = LoopType.CaptionTechnical,
                            color = InkBlack
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) InkBlack else ParchmentBg)
                                .border(1.dp, if (isSelected) InkBlack else ParchmentBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = InkMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
