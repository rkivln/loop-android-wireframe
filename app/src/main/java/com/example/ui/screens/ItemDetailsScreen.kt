package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiValuationUiState
import com.example.ai.GeminiAiService
import com.example.data.RentalRepository
import com.example.data.models.RentalItem
import com.example.data.models.RentalOwner
import com.example.ui.components.ListingPhoto
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SecondaryCTA
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.HeartRed
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemDetailsScreen(
    item: RentalItem,
    onNavigateBack: () -> Unit,
    onBookNow: (RentalItem) -> Unit = {},
    onContactOwner: (RentalOwner, RentalItem) -> Unit = { _, _ -> }
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    var aiState by remember(item.id) { mutableStateOf<AiValuationUiState>(AiValuationUiState.Idle) }
    var isFavorite by remember(item.id, item.isFavorite) { mutableStateOf(item.isFavorite) }
    var showBlockDialog by remember { mutableStateOf(false) }

    val triggerAiValuation: () -> Unit = {
        aiState = AiValuationUiState.Loading
        coroutineScope.launch {
            val result = GeminiAiService.evaluateGearAndSuggestPricing(
                gearTitle = item.title,
                description = item.description
            )
            aiState = result.fold(
                onSuccess = { AiValuationUiState.Success(it) },
                onFailure = { AiValuationUiState.Error(it.localizedMessage ?: "AI appraisal failed.") }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Photo Gallery with Circular Editorial Controls
            val galleryUrls = remember(item.id, item.imageUrl, item.imageUrls) {
                if (item.imageUrls.isNotEmpty()) item.imageUrls else listOfNotNull(item.imageUrl)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
            ) {
                if (galleryUrls.size > 1) {
                    val pagerState = rememberPagerState(pageCount = { galleryUrls.size })
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("details_photo_pager")
                    ) { page ->
                        ListingPhoto(
                            imageUrl = galleryUrls[page],
                            fallbackRes = item.primaryImageRes,
                            contentDescription = "${item.title} photo ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Bottom-Right Gallery Page Indicator
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(18.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.Black.copy(alpha = 0.72f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        repeat(galleryUrls.size) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == idx) 7.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == idx) Color.White
                                        else Color.White.copy(alpha = 0.45f)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${pagerState.currentPage + 1}/${galleryUrls.size}",
                            style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                } else {
                    ListingPhoto(
                        imageUrl = galleryUrls.firstOrNull(),
                        fallbackRes = item.primaryImageRes,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PaperPureWhite.copy(alpha = 0.94f))
                            .border(1.dp, PaperBorderSubtle, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onNavigateBack
                            )
                            .testTag("details_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = InkCharcoal,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PaperPureWhite.copy(alpha = 0.94f))
                            .border(1.dp, PaperBorderSubtle, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    isFavorite = !isFavorite
                                    RentalRepository.toggleFavorite(item.id)
                                }
                            )
                            .testTag("details_favorite_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) HeartRed else InkCharcoal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Category Pill Bottom-Left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperPureWhite.copy(alpha = 0.94f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = item.category.title.uppercase(),
                        style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                        color = InkCharcoal
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Editorial Headline & Metadata
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = item.title,
                        style = LoopType.HeroDisplayLarge.copy(fontSize = 28.sp, lineHeight = 34.sp),
                        color = InkCharcoal
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = AccentWarmYellow,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.reviewCount > 0 && item.rating > 0f) {
                                    "${item.rating} (${item.reviewCount} reviews)"
                                } else {
                                    "New listing"
                                },
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                                color = InkCharcoal
                            )
                        }

                        if (item.location.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = InkSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = item.location,
                                    style = LoopType.BodySmall,
                                    color = InkSecondary
                                )
                            }
                        }
                    }

                    if (item.securityDeposit > 0) {
                        Text(
                            text = "Refundable Security Deposit: ₹${item.securityDeposit}",
                            style = LoopType.Metadata,
                            color = InkMuted
                        )
                    }
                }

                // Editorial Description
                Text(
                    text = item.description,
                    style = LoopType.BodyEditorial,
                    color = InkSecondary
                )

                // Included Specs Pills
                if (item.features.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SPECIFICATIONS & INCLUDED KIT",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.features.forEach { feature ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(PaperPureWhite)
                                        .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                                        .padding(horizontal = 13.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = feature.label,
                                        style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                        color = InkCharcoal
                                    )
                                }
                            }
                        }
                    }
                }

                // Host Dossier Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(InkCharcoal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.owner.initials,
                            style = LoopType.HeadlineMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Normal),
                            color = InkWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val isVerifiedHost = item.owner.badge.contains("Verified", ignoreCase = true)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.owner.name.ifBlank { "Community Member" },
                                style = LoopType.TitleMedium.copy(fontSize = 15.sp),
                                color = InkCharcoal
                            )
                            if (isVerifiedHost) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Host",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        val ownerSubtitle = listOfNotNull(
                            item.owner.badge.takeIf { it.isNotBlank() },
                            item.owner.responseTime.takeIf { it.isNotBlank() }?.let { "Responds $it" }
                        ).joinToString(" · ")
                        if (ownerSubtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ownerSubtitle,
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                        }
                    }

                    if (item.owner.id.isNotBlank() && item.owner.id != currentProfile.uid) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { showBlockDialog = true }
                                .testTag("details_block_host_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "Block Host",
                                tint = InkMuted,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                // Subtle Editorial Gemini AI Insights Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AccentMint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AccentForestGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GEMINI AI KIT & RATE INSIGHTS",
                            style = LoopType.EditorialTag,
                            color = AccentForestGreen
                        )
                    }

                    when (val state = aiState) {
                        AiValuationUiState.Idle -> {
                            Text(
                                text = "Check recommended companion accessories, local market rate fairness, and deposit guidance.",
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                            SecondaryCTA(
                                text = "Analyze Piece with Gemini AI",
                                onClick = triggerAiValuation,
                                testTag = "details_ai_match_button"
                            )
                        }
                        AiValuationUiState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = AccentForestGreen,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Appraising kit with Gemini AI...",
                                    style = LoopType.BodySmall,
                                    color = InkSecondary
                                )
                            }
                        }
                        is AiValuationUiState.Success -> {
                            Text(
                                text = state.valuation.toFormattedText(),
                                style = LoopType.BodySmall,
                                color = InkCharcoal
                            )
                        }
                        is AiValuationUiState.Error -> {
                            Text(
                                text = state.message,
                                style = LoopType.BodySmall,
                                color = AlertCrimson
                            )
                            SecondaryCTA(
                                text = "Retry AI Analysis",
                                onClick = triggerAiValuation,
                                testTag = "ai_retry_button"
                            )
                        }
                    }
                }
            }
        }

        // Sticky Editorial Bottom Action Bar
        HorizontalDivider(thickness = 1.dp, color = PaperBorderSubtle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PaperPureWhite)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "₹${item.pricePerDay}",
                    style = LoopType.PriceHeadline,
                    color = InkCharcoal
                )
                Text(
                    text = "per day",
                    style = LoopType.Metadata,
                    color = InkMuted
                )
            }

            Box(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PaperIvory)
                    .border(1.dp, PaperBorder, RoundedCornerShape(24.dp))
                    .clickable { onContactOwner(item.owner, item) }
                    .padding(horizontal = 16.dp)
                    .testTag("message_host_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Message Host",
                        tint = InkCharcoal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Message",
                        style = LoopType.ButtonSecondaryLabel,
                        color = InkCharcoal
                    )
                }
            }

            Box(modifier = Modifier.width(146.dp)) {
                PrimaryCTA(
                    text = "Book Rental",
                    onClick = { onBookNow(item) },
                    testTag = "book_rental_button"
                )
            }
        }
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            containerColor = PaperPureWhite,
            titleContentColor = InkCharcoal,
            textContentColor = InkSecondary,
            title = {
                Text(
                    text = "Block ${item.owner.name.ifBlank { "this user" }}?",
                    style = LoopType.HeadlineMedium.copy(fontSize = 18.sp)
                )
            },
            text = {
                Text(
                    text = "You will no longer see their listings, community posts, or chats. You can unblock them anytime from Settings.",
                    style = LoopType.BodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBlockDialog = false
                        RentalRepository.blockUser(context, item.owner.id, item.owner.name)
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("details_confirm_block_btn")
                ) {
                    Text("Block User", color = AlertCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text("Cancel", color = InkCharcoal)
                }
            }
        )
    }
}
