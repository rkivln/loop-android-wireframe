package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.LoopCategory
import com.example.ui.components.AtmosphericBackground
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.ButtonCtaGradient
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceGlassHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple
import com.example.ui.theme.WarmSunset

@Composable
fun CreatePostScreen(
    onPostCreated: (content: String, category: LoopCategory, visualType: Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    var postType by remember { mutableStateOf(LoopCategory.ALL) }
    var contentText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Study") }
    var selectedVisualIndex by remember { mutableIntStateOf(0) }
    var locationName by remember { mutableStateOf("Pondicherry Beach") }

    val tags = listOf("General", "Study", "Event", "Help")

    val visualGradients = listOf(
        listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)),
        listOf(Color(0xFFFF5E62), Color(0xFFFF9966)),
        listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
        listOf(Color(0xFF10B981), Color(0xFF06B6D4))
    )

    AtmosphericBackground(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header: Close and Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconButton(
                        icon = Icons.Default.Close,
                        onClick = onClose,
                        contentDescription = "Close",
                        size = 40.dp,
                        iconSize = 20.dp,
                        testTag = "create_post_close_btn"
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Create Post",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Type Selector Pills: Post, Event, Help, Study
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(
                        LoopCategory.ALL to "Post",
                        LoopCategory.EVENTS to "Event",
                        LoopCategory.HELP to "Help",
                        LoopCategory.STUDY to "Study"
                    )

                    types.forEach { (cat, label) ->
                        val isSelected = postType == cat
                        val shape = RoundedCornerShape(18.dp)
                        val bg = if (isSelected) {
                            Brush.horizontalGradient(listOf(NeonBlue, VividPurple))
                        } else {
                            Brush.linearGradient(listOf(Color(0x2B1F2436), Color(0x1A181C28)))
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(shape)
                                .background(bg, shape)
                                .border(1.dp, if (isSelected) Color(0x668B5CF6) else GlassBorderSubtle, shape)
                                .clickable { postType = cat },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Text Area Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    BasicTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 26.sp,
                            color = TextPrimary
                        ),
                        cursorBrush = SolidColor(NeonBlue),
                        decorationBox = { innerTextField ->
                            if (contentText.isEmpty()) {
                                Text(
                                    text = "Share something with\nyour community...",
                                    fontSize = 18.sp,
                                    lineHeight = 26.sp,
                                    color = TextMuted
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("create_post_text_field")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image / Gradient Attachment Cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // + Add Media slot
                    item {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x331E2235))
                                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                                .clickable { /* pick attachment */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Media",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Gradient Visual Card Options
                    items(visualGradients.size) { index ->
                        val isSelected = selectedVisualIndex == index + 1
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(visualGradients[index]),
                                    RoundedCornerShape(16.dp)
                                )
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) Color.White else Color(0x33FFFFFF),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    selectedVisualIndex = if (isSelected) 0 else index + 1
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Option: Add location
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { /* location picker */ }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = NeonBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Add location",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Option: Add tags
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sell,
                            contentDescription = null,
                            tint = VividPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Add tags",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            val isSelected = selectedTag == tag
                            val shape = RoundedCornerShape(14.dp)
                            val bg = if (isSelected) Color(0xFF2C2448) else Color(0x221F2436)
                            val border = if (isSelected) VividPurple else GlassBorderSubtle

                            Box(
                                modifier = Modifier
                                    .clip(shape)
                                    .background(bg, shape)
                                    .border(1.dp, border, shape)
                                    .clickable { selectedTag = tag }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFFC4B5FD) else TextMuted
                                )
                            }
                        }
                    }
                }

                // Option: Visible to
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { /* visibility picker */ }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Visible to",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nearby community",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bottom CTA Button: "Post ->"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        if (contentText.isNotBlank()) ButtonCtaGradient else Brush.linearGradient(listOf(Color(0xFF232738), Color(0xFF1B1E2B))),
                        RoundedCornerShape(28.dp)
                    )
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(28.dp))
                    .clickable(enabled = contentText.isNotBlank()) {
                        val category = when (selectedTag) {
                            "Event" -> LoopCategory.EVENTS
                            "Help" -> LoopCategory.HELP
                            "Study" -> LoopCategory.STUDY
                            else -> LoopCategory.ALL
                        }
                        onPostCreated(contentText, category, selectedVisualIndex)
                    }
                    .testTag("create_post_submit_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Post",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (contentText.isNotBlank()) Color.White else TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (contentText.isNotBlank()) Color.White else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
