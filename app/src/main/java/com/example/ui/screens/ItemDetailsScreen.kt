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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.ZoomIn
import com.example.ui.components.ItemImagePreviewModal
import com.example.ui.components.MemberProfileModalSheet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
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
    onContactOwner: (RentalOwner, RentalItem) -> Unit = { _, _ -> },
    onEditItem: (RentalItem) -> Unit = {}
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val allItems by RentalRepository.itemsFlow.collectAsStateWithLifecycle()
    val isCatalogLoading by RentalRepository.isCatalogLoadingFlow.collectAsStateWithLifecycle()
    val liveMatch = remember(allItems, item.id) { allItems.find { it.id == item.id } }
    var wasPresentInCatalog by remember(item.id) { mutableStateOf(liveMatch != null) }

    LaunchedEffect(liveMatch, isCatalogLoading) {
        if (liveMatch != null) {
            wasPresentInCatalog = true
        } else if (wasPresentInCatalog && !isCatalogLoading) {
            onNavigateBack()
        }
    }

    val currentItem = remember(liveMatch, item) {
        liveMatch ?: item
    }
    val isOwnListing = currentItem.owner.id.isNotBlank() && currentItem.owner.id == currentProfile.uid
    val isBooked = !currentItem.availableToday && currentItem.activeBookingId != null
    val isPaused = !currentItem.availableToday && currentItem.activeBookingId == null

    var aiState by remember(currentItem.id) { mutableStateOf<AiValuationUiState>(AiValuationUiState.Idle) }
    var showMemberProfile by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(false) }
    var previewInitialPage by remember { mutableIntStateOf(0) }

    val triggerAiValuation: () -> Unit = {
        if (aiState !is AiValuationUiState.Loading) {
            aiState = AiValuationUiState.Loading("Appraising kit with Gemini AI...")
            coroutineScope.launch {
                val result = GeminiAiService.evaluateGearAndSuggestPricing(
                    gearTitle = currentItem.title,
                    description = currentItem.description,
                    onStatusUpdate = { status ->
                        aiState = AiValuationUiState.Loading(status)
                    }
                )
                aiState = result.fold(
                    onSuccess = { AiValuationUiState.Success(it) },
                    onFailure = { AiValuationUiState.Error(it.localizedMessage ?: "AI appraisal failed.") }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
    ) {
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
            val galleryUrls = remember(currentItem.id, currentItem.imageUrl, currentItem.imageUrls) {
                if (currentItem.imageUrls.isNotEmpty()) currentItem.imageUrls else listOfNotNull(currentItem.imageUrl)
            }
            val pagerState = rememberPagerState(pageCount = { galleryUrls.size.coerceAtLeast(1) })
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
            ) {
                if (galleryUrls.size > 1) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("details_photo_pager")
                    ) { page ->
                        ListingPhoto(
                            imageUrl = galleryUrls[page],
                            fallbackRes = currentItem.primaryImageRes,
                            contentDescription = "${currentItem.title} photo ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    previewInitialPage = page
                                    showImagePreview = true
                                }
                                .testTag("details_hero_photo_tap_$page")
                        )
                    }

                    // Bottom-Right Gallery Page Indicator & Preview Pill
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(18.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable {
                                previewInitialPage = pagerState.currentPage
                                showImagePreview = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("details_preview_photos_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Preview photos fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
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
                        fallbackRes = currentItem.primaryImageRes,
                        contentDescription = currentItem.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                previewInitialPage = 0
                                showImagePreview = true
                            }
                            .testTag("details_hero_photo_tap")
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(18.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable {
                                previewInitialPage = 0
                                showImagePreview = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("details_preview_photos_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Preview photo fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Preview",
                            style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
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

                    if (isOwnListing) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(InkCharcoal.copy(alpha = 0.92f))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("details_own_badge")
                        ) {
                            Text(
                                text = "YOUR LISTING",
                                style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                                color = InkWhite
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PaperPureWhite.copy(alpha = 0.94f))
                                .border(1.dp, PaperBorderSubtle, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { RentalRepository.toggleFavorite(currentItem.id) }
                                )
                                .testTag("details_favorite_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentItem.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (currentItem.isFavorite) "Remove favorite" else "Favorite",
                                tint = if (currentItem.isFavorite) HeartRed else InkCharcoal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Category Pill Bottom-Left
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperPureWhite.copy(alpha = 0.94f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = currentItem.category.title.uppercase(),
                            style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                            color = InkCharcoal
                        )
                    }
                    if (!currentItem.availableToday) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isBooked) AccentPeach else AccentBeigeOat)
                                .padding(horizontal = 11.dp, vertical = 5.dp)
                                .testTag("details_status_pill")
                        ) {
                            Text(
                                text = if (isBooked) "BOOKED" else "PAUSED",
                                style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                                color = if (isBooked) AlertCrimson else InkCharcoal
                            )
                        }
                    }
                }
            }

            if (galleryUrls.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(galleryUrls) { idx, thumbUrl ->
                        val isSelected = pagerState.currentPage == idx
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentForestGreen else PaperBorderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(idx)
                                    }
                                }
                                .testTag("details_thumb_$idx")
                        ) {
                            ListingPhoto(
                                imageUrl = thumbUrl,
                                fallbackRes = currentItem.primaryImageRes,
                                contentDescription = "Thumbnail ${idx + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                if (isOwnListing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AccentMint)
                            .border(1.dp, AccentForestGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("details_own_listing_banner")
                    ) {
                        val stateLabel = when {
                            isBooked -> "Currently booked — pause and delete are locked until return"
                            isPaused -> "Availability paused — neighbors cannot reserve this piece"
                            else -> "Live in catalog — you host this piece"
                        }
                        Text(
                            text = stateLabel,
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                            color = AccentForestGreen
                        )
                    }
                } else if (!currentItem.availableToday) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isBooked) AccentPeach.copy(alpha = 0.5f) else AccentBeigeOat)
                            .border(1.dp, PaperBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("details_unavailable_banner")
                    ) {
                        Text(
                            text = if (isBooked) {
                                "Currently booked by another member. Message the host to ask about upcoming dates."
                            } else {
                                "Host has paused bookings for this piece."
                            },
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isBooked) AlertCrimson else InkCharcoal
                        )
                    }
                }

                // Editorial Headline & Metadata
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = currentItem.title,
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
                                text = if (currentItem.reviewCount > 0 && currentItem.rating > 0f) {
                                    "${currentItem.rating} (${currentItem.reviewCount} reviews)"
                                } else {
                                    "New listing"
                                },
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                                color = InkCharcoal
                            )
                        }

                        if (currentItem.location.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = InkSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = currentItem.location,
                                    style = LoopType.BodySmall,
                                    color = InkSecondary
                                )
                            }
                        }
                    }

                    if (currentItem.securityDeposit > 0) {
                        Text(
                            text = "Refundable Security Deposit: ₹${currentItem.securityDeposit}",
                            style = LoopType.Metadata,
                            color = InkMuted
                        )
                    }
                }

                // Editorial Description
                Text(
                    text = currentItem.description,
                    style = LoopType.BodyEditorial,
                    color = InkSecondary
                )

                // Included Specs Pills
                if (currentItem.features.isNotEmpty()) {
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
                            currentItem.features.forEach { feature ->
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
                        .clickable { showMemberProfile = true }
                        .padding(16.dp)
                        .testTag("host_dossier_card"),
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
                            text = currentItem.owner.initials,
                            style = LoopType.HeadlineMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Normal),
                            color = InkWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val isVerifiedHost = currentItem.owner.badge.contains("Verified", ignoreCase = true)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOwnListing) {
                                    "${currentItem.owner.name.ifBlank { "You" }} (You)"
                                } else {
                                    currentItem.owner.name.ifBlank { "Community Member" }
                                },
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
                        val ownerSubtitle = if (isOwnListing) {
                            "You host this piece"
                        } else {
                            listOfNotNull(
                                currentItem.owner.badge.takeIf { it.isNotBlank() },
                                currentItem.owner.responseTime.takeIf { it.isNotBlank() }?.let { "Responds $it" }
                            ).joinToString(" · ")
                        }
                        if (ownerSubtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ownerSubtitle,
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                        }
                    }

                    if (currentItem.owner.id.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Profile",
                                tint = InkMuted,
                                modifier = Modifier.size(20.dp)
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
                        is AiValuationUiState.Loading -> {
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
                                    text = state.statusMessage,
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
                    text = "₹${currentItem.pricePerDay}",
                    style = LoopType.PriceHeadline,
                    color = InkCharcoal
                )
                Text(
                    text = "per day",
                    style = LoopType.Metadata,
                    color = InkMuted
                )
            }

            if (isOwnListing) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (currentItem.availableToday) PaperIvory else AccentMint)
                        .border(
                            1.dp,
                            if (isBooked) PaperBorder else if (currentItem.availableToday) PaperBorder else AccentForestGreen,
                            RoundedCornerShape(24.dp)
                        )
                        .clickable(enabled = !isBooked) {
                            RentalRepository.toggleListingAvailability(currentItem.id)
                        }
                        .padding(horizontal = 16.dp)
                        .testTag("details_pause_resume_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isBooked -> "Booked"
                            currentItem.availableToday -> "Pause"
                            else -> "Resume"
                        },
                        style = LoopType.ButtonSecondaryLabel,
                        color = if (isBooked) InkMuted else if (currentItem.availableToday) InkCharcoal else AccentForestGreen
                    )
                }

                Box(modifier = Modifier.width(146.dp)) {
                    PrimaryCTA(
                        text = "Edit Listing",
                        onClick = { onEditItem(currentItem) },
                        testTag = "details_edit_listing_button"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorder, RoundedCornerShape(24.dp))
                        .clickable { onContactOwner(currentItem.owner, currentItem) }
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
                        text = when {
                            isBooked -> "Booked"
                            isPaused -> "Unavailable"
                            else -> "Book Rental"
                        },
                        enabled = currentItem.availableToday,
                        onClick = { onBookNow(currentItem) },
                        testTag = "book_rental_button"
                    )
                }
            }
        }
    }

        if (showMemberProfile) {
            MemberProfileModalSheet(
                owner = currentItem.owner,
                fallbackLocation = currentItem.location,
                isCurrentUser = isOwnListing,
                onMessageClick = {
                    onContactOwner(currentItem.owner, currentItem)
                },
                onDismiss = { showMemberProfile = false }
            )
        }

        if (showImagePreview) {
            ItemImagePreviewModal(
                item = currentItem,
                initialPage = previewInitialPage,
                onDismiss = { showImagePreview = false }
            )
        }
    }
}
