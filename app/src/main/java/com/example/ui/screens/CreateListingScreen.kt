package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RentalCategory
import com.example.ui.components.CategoryTile
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SectionLabel
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite
import com.example.ui.theme.PaperWarm

@Composable
fun CreateListingScreen(
    onListingCreated: (title: String, price: Int, category: RentalCategory, description: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(RentalCategory.ELECTRONICS) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperWarm)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            EditorialTopBar(
                title = "Publish Piece",
                onBack = onClose
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp)
            ) {
                // Photo Drop Zone
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {}
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(PaperIvory),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload",
                                    tint = InkCharcoal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Upload Studio Photography",
                                style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = InkCharcoal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Natural lighting, front and detail perspectives",
                                style = LoopType.Metadata,
                                color = InkSecondary
                            )
                        }
                    }
                }

                // Category Selection with Tactile Tiles
                item {
                    Spacer(modifier = Modifier.height(22.dp))
                    Text(
                        text = "DEPARTMENT",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(RentalCategory.values()) { cat ->
                            CategoryTile(
                                category = cat,
                                isSelected = selectedCategory == cat,
                                onClick = { selectedCategory = cat }
                            )
                        }
                    }
                }

                // Object Title
                item {
                    Spacer(modifier = Modifier.height(22.dp))
                    Text(
                        text = "OBJECT NAME",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(fontSize = 15.sp, color = InkCharcoal, fontWeight = FontWeight.Medium),
                            cursorBrush = SolidColor(InkCharcoal),
                            decorationBox = { inner ->
                                if (title.isEmpty()) Text("e.g. Leica M10 & 35mm Summicron", color = InkMuted, fontSize = 14.sp)
                                inner()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("listing_title_input")
                        )
                    }
                }

                // Daily Rate
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "DAILY CIRCULATION RATE (₹)",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 15.sp, color = InkCharcoal, fontWeight = FontWeight.SemiBold),
                            cursorBrush = SolidColor(InkCharcoal),
                            decorationBox = { inner ->
                                if (priceText.isEmpty()) Text("e.g. 750", color = InkMuted, fontSize = 14.sp)
                                inner()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("listing_price_input")
                        )
                    }
                }

                // Description & Object Notes
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "CURATOR'S NOTES & SPECS",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        BasicTextField(
                            value = description,
                            onValueChange = { description = it },
                            textStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, color = InkCharcoal),
                            cursorBrush = SolidColor(InkCharcoal),
                            decorationBox = { inner ->
                                if (description.isEmpty()) {
                                    Text(
                                        text = "Detail included lenses, flight case, battery health, and care instructions...",
                                        color = InkMuted,
                                        fontSize = 13.sp
                                    )
                                }
                                inner()
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("listing_desc_input")
                        )
                    }
                }
            }

            // Bottom Publish CTA
            val sanitizedTitle = title.trim().take(100)
            val parsedPrice = priceText.filter { it.isDigit() }.toIntOrNull()?.coerceIn(10, 1_000_000)
            val isEnabled = sanitizedTitle.isNotBlank() && parsedPrice != null

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                PrimaryCTA(
                    text = "Publish to Living Catalog",
                    onClick = {
                        val price = parsedPrice ?: 500
                        val sanitizedDesc = description.trim().take(1000)
                        onListingCreated(sanitizedTitle, price, selectedCategory, sanitizedDesc)
                    },
                    enabled = isEnabled,
                    testTag = "listing_publish_btn"
                )
            }
        }
    }
}
