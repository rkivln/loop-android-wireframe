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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.HouseRentalRepository
import com.example.data.RentalRepository
import com.example.data.models.HouseListing
import com.example.data.models.VisitRequestStatus
import com.example.ui.components.ListingPhoto
import com.example.ui.components.MemberProfileModalSheet
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HouseDetailsScreen(
    house: HouseListing,
    onNavigateBack: () -> Unit,
    onMessageLandlord: (HouseListing) -> Unit = {},
    onEditHouse: (HouseListing) -> Unit = {},
    onOpenVisitsManager: () -> Unit = {}
) {
    BackHandler(onBack = onNavigateBack)

    val currentProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val allHouses by HouseRentalRepository.housesFlow.collectAsStateWithLifecycle()
    val visits by HouseRentalRepository.visitRequestsFlow.collectAsStateWithLifecycle()
    val isHousesLoading by HouseRentalRepository.isHousesLoadingFlow.collectAsStateWithLifecycle()

    val liveMatch = remember(allHouses, house.id) { allHouses.find { it.id == house.id } }
    var wasPresent by remember(house.id) { mutableStateOf(liveMatch != null) }
    LaunchedEffect(liveMatch, isHousesLoading) {
        if (liveMatch != null) {
            wasPresent = true
        } else if (wasPresent && !isHousesLoading) {
            onNavigateBack()
        }
    }

    val currentHouse = liveMatch ?: house
    val isOwnHouse = currentHouse.owner.id.isNotBlank() && currentHouse.owner.id == currentProfile.uid
    val activeVisit = remember(visits, currentHouse.id, currentProfile.uid) {
        HouseRentalRepository.activeVisitForHouse(currentHouse.id, currentProfile.uid)
    }

    var showScheduleVisitSheet by remember { mutableStateOf(false) }
    var showLandlordProfile by remember { mutableStateOf(false) }
    var showFullscreenGallery by remember { mutableStateOf(false) }
    var galleryStartPage by remember { mutableIntStateOf(0) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val galleryUrls = remember(currentHouse.id, currentHouse.imageUrl, currentHouse.imageUrls) {
        currentHouse.resolvedGalleryUrls.ifEmpty { listOf("") }
    }
    val pagerState = rememberPagerState(pageCount = { galleryUrls.size.coerceAtLeast(1) })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .testTag("house_details_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Room Gallery
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("house_details_pager")
                    ) { page ->
                        ListingPhoto(
                            imageUrl = galleryUrls[page].takeIf { it.isNotBlank() },
                            fallbackRes = R.drawable.img_welcome_hero,
                            contentDescription = "${currentHouse.title} ${currentHouse.roomLabelAt(page)}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    galleryStartPage = page
                                    showFullscreenGallery = true
                                }
                        )
                    }

                    // Top Navigation Overlay
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
                                .testTag("house_details_back_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = InkCharcoal,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        if (isOwnHouse) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(InkCharcoal.copy(alpha = 0.92f))
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "YOUR PROPERTY",
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
                                    .clickable {
                                        HouseRentalRepository.toggleFavoriteHouse(currentHouse.id)
                                    }
                                    .testTag("house_details_favorite_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentHouse.isFavorite) {
                                        Icons.Default.Favorite
                                    } else {
                                        Icons.Default.FavoriteBorder
                                    },
                                    contentDescription = if (currentHouse.isFavorite) "Saved home" else "Save home",
                                    tint = if (currentHouse.isFavorite) HeartRed else InkCharcoal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Bottom Room Tag & Page Indicator
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.Black.copy(alpha = 0.72f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentHouse.roomLabelAt(pagerState.currentPage),
                            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(18.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable {
                                galleryStartPage = pagerState.currentPage
                                showFullscreenGallery = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom room photo",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${pagerState.currentPage + 1}/${galleryUrls.size}",
                            style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Editorial House Dossier Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    if (!feedbackMessage.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AccentMint)
                                .border(1.dp, AccentForestGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = feedbackMessage.orEmpty(),
                                style = LoopType.BodySmall,
                                color = AccentForestGreen
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (currentHouse.isAvailableForRent) AccentMint else AccentPeach)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (currentHouse.isAvailableForRent) {
                                        "AVAILABLE · ${currentHouse.availableFromLabel.uppercase()}"
                                    } else {
                                        "CURRENTLY RENTED OUT"
                                    },
                                    style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                                    color = if (currentHouse.isAvailableForRent) AccentForestGreen else InkCharcoal
                                )
                            }
                            Text(
                                text = "${currentHouse.propertyType.label} · ${currentHouse.configurationLabel}",
                                style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                color = InkSecondary
                            )
                        }

                        Text(
                            text = currentHouse.title,
                            style = LoopType.HeroDisplayLarge.copy(fontSize = 26.sp),
                            color = InkCharcoal
                        )

                        if (currentHouse.location.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Neighborhood",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentHouse.location,
                                    style = LoopType.BodySmall,
                                    color = InkSecondary
                                )
                            }
                        }
                    }

                    // Active Visit Status Banner (if user already submitted one)
                    activeVisit?.let { visit ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(AccentMint)
                                .border(1.dp, AccentForestGreen.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                                .clickable { onOpenVisitsManager() }
                                .padding(14.dp)
                                .testTag("house_active_visit_card"),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "YOUR VISIT STATUS · ${visit.status.label.uppercase()}",
                                style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                                color = AccentForestGreen
                            )
                            Text(
                                text = "${visit.preferredDate} · ${visit.preferredTimeSlot.shortWindow}",
                                style = LoopType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = InkCharcoal
                            )
                            if (visit.status == VisitRequestStatus.CONFIRMED && visit.confirmedAddressNote.isNotBlank()) {
                                Text(
                                    text = "Directions: ${visit.confirmedAddressNote}",
                                    style = LoopType.BodySmall,
                                    color = InkCharcoal
                                )
                            } else if (visit.landlordResponseNote.isNotBlank()) {
                                Text(
                                    text = "Landlord note: ${visit.landlordResponseNote}",
                                    style = LoopType.BodySmall,
                                    color = InkSecondary
                                )
                            }
                        }
                    }

                    // Monthly Lease Ledger Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "LONG-TERM MONTHLY LEASE TERMS",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        LedgerRow("Monthly Rent", "₹${currentHouse.monthlyRent} / month", highlight = true)
                        LedgerRow("Refundable Security Deposit", "₹${currentHouse.securityDeposit}")
                        LedgerRow(
                            "Monthly Maintenance",
                            if (currentHouse.maintenanceMonthly > 0) "₹${currentHouse.maintenanceMonthly} / mo" else "Included"
                        )
                        LedgerRow("Minimum Lease Duration", "${currentHouse.minLeaseMonths} Months")
                        LedgerRow("Tenant Preference", currentHouse.tenantPreference.label)
                    }

                    // Key Property Specs
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "HOME SPECIFICATIONS",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HouseSpecTile(
                                icon = Icons.Default.Bed,
                                label = "Layout",
                                value = currentHouse.configurationLabel,
                                modifier = Modifier.weight(1f)
                            )
                            HouseSpecTile(
                                icon = Icons.Default.Bathtub,
                                label = "Baths",
                                value = "${currentHouse.bathrooms} Bath",
                                modifier = Modifier.weight(1f)
                            )
                            HouseSpecTile(
                                icon = Icons.Default.SquareFoot,
                                label = "Area",
                                value = "${currentHouse.areaSqFt} sqft",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HouseSpecTile(
                                icon = Icons.Default.Apartment,
                                label = "Floor",
                                value = if (currentHouse.floorNumber <= 0) "Ground" else "Floor ${currentHouse.floorNumber}/${currentHouse.totalFloors}",
                                modifier = Modifier.weight(1f)
                            )
                            HouseSpecTile(
                                icon = Icons.Default.CalendarMonth,
                                label = "Furnishing",
                                value = currentHouse.furnishing.label,
                                modifier = Modifier.weight(2f)
                            )
                        }
                    }

                    // Description
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ABOUT THIS HOME",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Text(
                            text = currentHouse.description,
                            style = LoopType.BodyEditorial,
                            color = InkCharcoal
                        )
                    }

                    // Amenities
                    if (currentHouse.amenities.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "AMENITIES & ESSENTIALS",
                                style = LoopType.EditorialTag,
                                color = InkSecondary
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentHouse.amenities.forEach { amenity ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100.dp))
                                            .background(PaperPureWhite)
                                            .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = amenity.label,
                                            style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                            color = InkCharcoal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Landlord Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                            .clickable { showLandlordProfile = true }
                            .padding(16.dp)
                            .testTag("house_landlord_card"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            avatarUrl = currentHouse.owner.avatarUrl,
                            initials = currentHouse.owner.initials,
                            size = 48.dp,
                            avatarPresetIndex = currentHouse.owner.avatarPresetIndex,
                            contentDescription = "${currentHouse.owner.name} avatar"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentHouse.owner.name,
                                    style = LoopType.TitleMedium,
                                    color = InkCharcoal
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Landlord",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = "Listed by Verified Owner · Tap for profile",
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
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
                        text = "₹${currentHouse.monthlyRent}",
                        style = LoopType.PriceHeadline,
                        color = InkCharcoal
                    )
                    Text(
                        text = "per month",
                        style = LoopType.Metadata,
                        color = InkMuted
                    )
                }

                if (isOwnHouse) {
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PaperIvory)
                            .border(1.dp, PaperBorder, RoundedCornerShape(24.dp))
                            .clickable { onOpenVisitsManager() }
                            .padding(horizontal = 16.dp)
                            .testTag("house_manage_visits_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Visits",
                            style = LoopType.ButtonSecondaryLabel,
                            color = InkCharcoal
                        )
                    }
                    Box(modifier = Modifier.width(146.dp)) {
                        PrimaryCTA(
                            text = "Edit Home",
                            onClick = { onEditHouse(currentHouse) },
                            testTag = "house_edit_btn"
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PaperIvory)
                            .border(1.dp, PaperBorder, RoundedCornerShape(24.dp))
                            .clickable { onMessageLandlord(currentHouse) }
                            .padding(horizontal = 16.dp)
                            .testTag("house_message_landlord_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Message Landlord",
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

                    Box(modifier = Modifier.width(156.dp)) {
                        PrimaryCTA(
                            text = when {
                                !currentHouse.isAvailableForRent -> "Rented Out"
                                activeVisit != null -> "Visit Sent"
                                else -> "Request Visit"
                            },
                            enabled = currentHouse.isAvailableForRent && activeVisit == null,
                            onClick = { showScheduleVisitSheet = true },
                            testTag = "house_request_visit_btn"
                        )
                    }
                }
            }
        }

        if (showScheduleVisitSheet) {
            ScheduleVisitBottomSheet(
                house = currentHouse,
                onVisitSubmitted = {
                    showScheduleVisitSheet = false
                    feedbackMessage = "Visit request sent to ${currentHouse.owner.name}. You'll see confirmation & directions here."
                },
                onDismiss = { showScheduleVisitSheet = false }
            )
        }

        if (showLandlordProfile) {
            MemberProfileModalSheet(
                owner = currentHouse.owner,
                fallbackLocation = currentHouse.location,
                isCurrentUser = isOwnHouse,
                onMessageClick = { onMessageLandlord(currentHouse) },
                onDismiss = { showLandlordProfile = false }
            )
        }

        if (showFullscreenGallery) {
            HouseGalleryFullscreenDialog(
                house = currentHouse,
                initialPage = galleryStartPage,
                onDismiss = { showFullscreenGallery = false }
            )
        }
    }
}

@Composable
private fun LedgerRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = LoopType.BodySmall,
            color = InkSecondary
        )
        Text(
            text = value,
            style = if (highlight) LoopType.TitleMedium else LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
            color = if (highlight) AccentForestGreen else InkCharcoal
        )
    }
}

@Composable
private fun HouseSpecTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PaperPureWhite)
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AccentForestGreen,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = value,
            style = LoopType.TitleMedium.copy(fontSize = 14.sp),
            color = InkCharcoal
        )
        Text(
            text = label,
            style = LoopType.Metadata.copy(fontSize = 10.5.sp),
            color = InkMuted
        )
    }
}

@Composable
private fun HouseGalleryFullscreenDialog(
    house: HouseListing,
    initialPage: Int,
    onDismiss: () -> Unit
) {
    val urls = house.resolvedGalleryUrls.ifEmpty { listOf("") }
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, (urls.size - 1).coerceAtLeast(0)),
        pageCount = { urls.size }
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        BackHandler(onBack = onDismiss)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ListingPhoto(
                    imageUrl = urls[page].takeIf { it.isNotBlank() },
                    fallbackRes = R.drawable.img_welcome_hero,
                    contentDescription = house.roomLabelAt(page),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close gallery",
                        tint = Color.White
                    )
                }
                Text(
                    text = "${house.roomLabelAt(pagerState.currentPage)} · ${pagerState.currentPage + 1}/${urls.size}",
                    style = LoopType.TitleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}
