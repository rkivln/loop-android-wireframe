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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceInteractive
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

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

    val tags = listOf("General", "Study", "Event", "Help")

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconButton(
                        icon = Icons.Default.Close,
                        onClick = onClose,
                        contentDescription = "Close",
                        size = 40.dp,
                        iconSize = 18.dp,
                        testTag = "create_post_close_btn"
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "New Post",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Post Type Segmented Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(
                        LoopCategory.ALL to "General",
                        LoopCategory.EVENTS to "Event",
                        LoopCategory.HELP to "Help",
                        LoopCategory.STUDY to "Study"
                    )

                    types.forEach { (cat, label) ->
                        val isSelected = postType == cat
                        val shape = RoundedCornerShape(8.dp)
                        val bg = if (isSelected) BrandPrimary else SurfaceCard
                        val border = if (isSelected) Color(0x33FFFFFF) else BorderSubtle

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(shape)
                                .background(bg, shape)
                                .border(1.dp, border, shape)
                                .clickable { postType = cat },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Text Composer Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    BasicTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = TextPrimary
                        ),
                        cursorBrush = SolidColor(BrandPrimary),
                        decorationBox = { innerTextField ->
                            if (contentText.isEmpty()) {
                                Text(
                                    text = "Share something with your nearby community...",
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
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

                Spacer(modifier = Modifier.height(20.dp))

                // Metadata Options Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Option: Add location
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* location picker */ }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = BrandSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Location",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Pondicherry Beach",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Option: Add tags
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sell,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Tag",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                tags.forEach { tag ->
                                    val isSelected = selectedTag == tag
                                    val shape = RoundedCornerShape(6.dp)
                                    val bg = if (isSelected) BrandPrimary else SurfaceInteractive
                                    val border = if (isSelected) Color(0x33FFFFFF) else BorderSubtle

                                    Box(
                                        modifier = Modifier
                                            .clip(shape)
                                            .background(bg, shape)
                                            .border(1.dp, border, shape)
                                            .clickable { selectedTag = tag }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Option: Visible to
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* visibility */ }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = StatusSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Visibility",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Nearby Community",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom CTA Button: Publish Post
            val isEnabled = contentText.isNotBlank()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isEnabled) BrandPrimary else SurfaceInteractive, RoundedCornerShape(12.dp))
                    .border(1.dp, if (isEnabled) Color(0x33FFFFFF) else BorderSubtle, RoundedCornerShape(12.dp))
                    .clickable(enabled = isEnabled) {
                        val category = when (selectedTag) {
                            "Event" -> LoopCategory.EVENTS
                            "Help" -> LoopCategory.HELP
                            "Study" -> LoopCategory.STUDY
                            else -> LoopCategory.ALL
                        }
                        onPostCreated(contentText, category, 0)
                    }
                    .testTag("create_post_submit_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Publish Post",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) Color.White else TextDisabled
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isEnabled) Color.White else TextDisabled,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
