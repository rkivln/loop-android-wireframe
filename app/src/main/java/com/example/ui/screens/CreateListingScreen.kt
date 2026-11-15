package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ai.AiValuationUiState
import com.example.ai.GeminiAiService
import com.example.data.LocationHelper
import com.example.data.RentalRepository
import com.example.data.models.LocationSharingPreference
import com.example.data.models.RentalCategory
import com.example.ui.components.CategoryTile
import com.example.ui.components.DraftListingPhoto
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.PhotoBitmapProcessor
import com.example.ui.components.PhotoEditorModal
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SecondaryCTA
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentWarmYellow
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun CreateListingScreen(
    onBack: () -> Unit = {},
    onListingCreated: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dailyRateStr by remember { mutableStateOf("") }
    var depositStr by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var isLocatingGps by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(RentalCategory.ELECTRONICS) }
    var attachedPhotos by remember { mutableStateOf<List<DraftListingPhoto>>(emptyList()) }
    var activeEditorPhotoId by remember { mutableStateOf<String?>(null) }
    var pendingEditQueue by remember { mutableStateOf<List<String>>(emptyList()) }
    var queueTotalCount by remember { mutableIntStateOf(0) }
    var queueCompletedCount by remember { mutableIntStateOf(0) }

    var aiState by remember { mutableStateOf<AiValuationUiState>(AiValuationUiState.Idle) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    val detectLiveLocation: (Boolean) -> Unit = { overwriteManual ->
        isLocatingGps = true
        LocationHelper.fetchCurrentLocation(
            context = context,
            onLocationReceived = { lat, lng ->
                latitude = lat
                longitude = lng
                coroutineScope.launch {
                    val resolved = LocationHelper.reverseGeocodeLocation(context, lat, lng)
                    if (!resolved.isNullOrBlank() && (overwriteManual || location.isBlank())) {
                        location = resolved
                    }
                    isLocatingGps = false
                }
            },
            onLocationUnavailable = {
                isLocatingGps = false
            }
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            detectLiveLocation(true)
        }
    }

    val userSettings by RentalRepository.userSettingsFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (userSettings.locationSharing != LocationSharingPreference.HIDDEN &&
            LocationHelper.hasLocationPermission(context)
        ) {
            detectLiveLocation(false)
        }
    }

    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        val remainingSlots = (4 - attachedPhotos.size).coerceAtLeast(0)
        val picked = uris.take(remainingSlots)
        if (picked.isNotEmpty()) {
            val newDrafts = picked.map { uri ->
                DraftListingPhoto(
                    id = "photo_${UUID.randomUUID()}",
                    sourceUri = uri,
                    isNewlyAdded = true
                )
            }
            val newIds = newDrafts.map { it.id }
            attachedPhotos = attachedPhotos + newDrafts
            pendingEditQueue = newIds.drop(1)
            queueTotalCount = newIds.size
            queueCompletedCount = 1
            activeEditorPhotoId = newIds.firstOrNull()
        }
    }

    val launchPhotoPicker: () -> Unit = {
        multiPhotoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val evaluateWithAi: () -> Unit = {
        if (title.isNotBlank()) {
            aiState = AiValuationUiState.Loading
            coroutineScope.launch {
                val result = GeminiAiService.evaluateGearAndSuggestPricing(title, description)
                aiState = result.fold(
                    onSuccess = { valuation ->
                        if (dailyRateStr.isBlank()) {
                            dailyRateStr = valuation.suggestedDailyRateInr.toString()
                        }
                        if (depositStr.isBlank()) {
                            depositStr = valuation.recommendedDepositInr.toString()
                        }
                        AiValuationUiState.Success(valuation)
                    },
                    onFailure = {
                        AiValuationUiState.Error(it.localizedMessage ?: "Failed to evaluate pricing.")
                    }
                )
            }
        }
    }

    val departmentCategories = remember {
        listOf(
            RentalCategory.ELECTRONICS,
            RentalCategory.FURNITURE,
            RentalCategory.VEHICLES,
            RentalCategory.TOOLS,
            RentalCategory.EVENTS,
            RentalCategory.STUDY_OFFICE,
            RentalCategory.HOME_LIVING
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        EditorialTopBar(
            title = "Publish Piece",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Multi-Photo Studio Tray (Zero-permission Android Photo Picker + Editor)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val coverPhoto = attachedPhotos.firstOrNull()
                if (coverPhoto == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(158.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                            .clickable(onClick = launchPhotoPicker)
                            .testTag("upload_photo_card"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(PaperIvory),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = "Upload Studio Photography",
                                    tint = InkCharcoal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Upload Studio Photography",
                                style = LoopType.TitleMedium.copy(fontSize = 14.5.sp),
                                color = InkCharcoal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Select up to 4 photos · Crop, rotate & frame",
                                style = LoopType.BodySmall,
                                color = InkMuted
                            )
                        }
                    }
                } else {
                    // Cover Preview Box with Edit Trigger
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(196.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                            .clickable {
                                pendingEditQueue = emptyList()
                                queueTotalCount = 1
                                queueCompletedCount = 1
                                activeEditorPhotoId = coverPhoto.id
                            }
                            .testTag("upload_photo_card")
                    ) {
                        val previewBmp = coverPhoto.previewBitmap
                        if (previewBmp != null) {
                            Image(
                                bitmap = previewBmp.asImageBitmap(),
                                contentDescription = "Cover piece photography",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AsyncImage(
                                model = coverPhoto.sourceUri,
                                contentDescription = "Cover piece photography",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Top-left Cover Count Pill
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.Black.copy(alpha = 0.72f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "COVER · ${attachedPhotos.size}/4 PHOTOS",
                                style = LoopType.EditorialTag.copy(fontSize = 9.5.sp),
                                color = Color.White
                            )
                        }

                        // Bottom-right Edit & Add Pills (min 48dp accessible hit targets)
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(PaperPureWhite.copy(alpha = 0.95f))
                                    .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                                    .clickable {
                                        pendingEditQueue = emptyList()
                                        queueTotalCount = 1
                                        queueCompletedCount = 1
                                        activeEditorPhotoId = coverPhoto.id
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("edit_cover_photo_button"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit cover photo",
                                    tint = InkCharcoal,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Photo",
                                    style = LoopType.Metadata.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                                    color = InkCharcoal
                                )
                            }
                        }
                    }

                    // Multi-Photo Thumbnail Strip (Edit, Set Cover, Remove, Add)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        attachedPhotos.forEachIndexed { index, photo ->
                            val isCover = index == 0
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(PaperPureWhite)
                                        .border(
                                            width = if (isCover) 2.dp else 1.dp,
                                            color = if (isCover) AccentForestGreen else PaperBorder,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            pendingEditQueue = emptyList()
                                            queueTotalCount = 1
                                            queueCompletedCount = 1
                                            activeEditorPhotoId = photo.id
                                        }
                                        .testTag("photo_thumb_$index")
                                ) {
                                    val thumbBmp = photo.previewBitmap
                                    if (thumbBmp != null) {
                                        Image(
                                            bitmap = thumbBmp.asImageBitmap(),
                                            contentDescription = "Photo ${index + 1}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        AsyncImage(
                                            model = photo.sourceUri,
                                            contentDescription = "Photo ${index + 1}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    // Edit Badge Bottom-Left
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.72f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit photo ${index + 1}",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }

                                    // Remove Button Top-Right
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.72f))
                                            .clickable {
                                                attachedPhotos = attachedPhotos.filterNot { it.id == photo.id }
                                            }
                                            .testTag("remove_photo_$index"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove photo ${index + 1}",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                // Cover / Promote Action Pill
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(if (isCover) AccentMint else PaperPureWhite)
                                        .border(
                                            1.dp,
                                            if (isCover) AccentForestGreen.copy(alpha = 0.35f) else PaperBorder,
                                            RoundedCornerShape(100.dp)
                                        )
                                        .clickable(enabled = !isCover) {
                                            val rest = attachedPhotos.filterNot { it.id == photo.id }
                                            attachedPhotos = listOf(photo) + rest
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("set_cover_photo_$index"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCover) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = if (isCover) "Cover photo" else "Set as cover",
                                        tint = if (isCover) AccentForestGreen else InkSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isCover) "Cover" else "Make Cover",
                                        style = LoopType.Metadata.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isCover) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isCover) AccentForestGreen else InkSecondary
                                    )
                                }
                            }
                        }

                        if (attachedPhotos.size < 4) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(PaperPureWhite)
                                        .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
                                        .clickable(onClick = launchPhotoPicker)
                                        .testTag("add_more_photos_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add more photos",
                                            tint = InkCharcoal,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Add",
                                            style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                            color = InkCharcoal
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(22.dp))
                            }
                        }
                    }
                }
            }

            // Department Selector
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DEPARTMENT",
                    style = LoopType.EditorialTag,
                    color = InkSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    departmentCategories.forEach { category ->
                        CategoryTile(
                            category = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }

            // Object Name
            EditorialFormField(
                label = "OBJECT NAME",
                value = title,
                onValueChange = { title = it },
                placeholder = "e.g. Mirrorless Camera, Drill Kit, City Bike",
                testTag = "listing_title_input"
            )

            // Daily Circulation Rate
            EditorialFormField(
                label = "DAILY CIRCULATION RATE (₹)",
                value = dailyRateStr,
                onValueChange = { dailyRateStr = it.filter { ch -> ch.isDigit() } },
                placeholder = "e.g. 500",
                keyboardType = KeyboardType.Number,
                testTag = "listing_rate_input"
            )

            // Security Deposit
            EditorialFormField(
                label = "SECURITY DEPOSIT (₹)",
                value = depositStr,
                onValueChange = { depositStr = it.filter { ch -> ch.isDigit() } },
                placeholder = "e.g. 1500",
                keyboardType = KeyboardType.Number,
                testTag = "listing_deposit_input"
            )

            // Pickup Neighborhood + Live GPS Action
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PICKUP NEIGHBORHOOD",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(AccentMint)
                            .clickable {
                                if (LocationHelper.hasLocationPermission(context)) {
                                    detectLiveLocation(true)
                                } else {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("listing_detect_gps_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Detect GPS location",
                            tint = AccentForestGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isLocatingGps) "Locating..." else "Use Live GPS",
                            style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = AccentForestGreen
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (location.isEmpty()) {
                        Text(
                            text = "Neighborhood or city (auto-detected via GPS or enter manually)",
                            style = LoopType.BodyMedium,
                            color = InkMuted
                        )
                    }
                    BasicTextField(
                        value = location,
                        onValueChange = { location = it },
                        singleLine = true,
                        textStyle = LoopType.BodyMedium.copy(color = InkCharcoal),
                        cursorBrush = SolidColor(InkCharcoal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("listing_location_input")
                    )
                }
            }

            // Curator's Notes & Specs
            EditorialFormField(
                label = "DESCRIPTION & PICKUP NOTES",
                value = description,
                onValueChange = { description = it },
                placeholder = "Detail condition, included accessories, and pickup instructions...",
                singleLine = false,
                minHeightDp = 104,
                testTag = "listing_description_input"
            )

            // Subtle Editorial Gemini AI Pricing Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                            text = "GEMINI AI VALUATION",
                            style = LoopType.EditorialTag,
                            color = AccentForestGreen
                        )
                    }
                }

                when (val state = aiState) {
                    AiValuationUiState.Idle -> {
                        Text(
                            text = "Estimate a fair daily rate and security deposit from your title and notes.",
                            style = LoopType.BodySmall,
                            color = InkSecondary
                        )
                        SecondaryCTA(
                            text = if (title.isBlank()) "Enter object name for AI appraisal" else "Suggest Fair INR Rate & Deposit",
                            onClick = { if (title.isNotBlank()) evaluateWithAi() },
                            testTag = "create_listing_ai_button"
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
                                text = "Appraising piece with Gemini AI...",
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
                        SecondaryCTA(
                            text = "Apply Suggested INR Rates",
                            onClick = {
                                dailyRateStr = state.valuation.suggestedDailyRateInr.toString()
                                depositStr = state.valuation.recommendedDepositInr.toString()
                            }
                        )
                    }
                    is AiValuationUiState.Error -> {
                        Text(
                            text = state.message,
                            style = LoopType.BodySmall,
                            color = AlertCrimson
                        )
                        SecondaryCTA(
                            text = "Retry AI Appraisal",
                            onClick = { if (title.isNotBlank()) evaluateWithAi() }
                        )
                    }
                }
            }

            if (submitError != null) {
                Text(
                    text = submitError ?: "",
                    style = LoopType.BodySmall,
                    color = AlertCrimson
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Bottom Publish CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PaperIvory)
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            PrimaryCTA(
                text = if (isSubmitting) "Publishing..." else "Publish to Living Catalog",
                onClick = {
                    val rate = dailyRateStr.toIntOrNull() ?: 500
                    val dep = depositStr.toIntOrNull() ?: 1000
                    if (title.isBlank()) {
                        submitError = "Please enter an object name."
                        return@PrimaryCTA
                    }
                    isSubmitting = true
                    submitError = null
                    coroutineScope.launch {
                        val encodedUrls = withContext(Dispatchers.Default) {
                            attachedPhotos.mapNotNull { draft ->
                                val bmp = draft.previewBitmap ?: PhotoBitmapProcessor
                                    .loadOrientedSourceBitmap(context, draft.sourceUri, maxEdge = 1080)
                                    ?.let { src ->
                                        PhotoBitmapProcessor.renderTransformedBitmap(src, draft.editState, maxOutputEdge = 768)
                                    }
                                bmp?.let { PhotoBitmapProcessor.bitmapToBase64DataUri(it, targetMaxBytes = 140_000) }
                            }
                        }
                        val res = RentalRepository.createListing(
                            title = title,
                            price = rate,
                            deposit = dep,
                            location = location,
                            category = selectedCategory,
                            desc = description,
                            imageUrl = encodedUrls.firstOrNull(),
                            imageUrls = encodedUrls,
                            latitude = latitude,
                            longitude = longitude
                        )
                        isSubmitting = false
                        res.fold(
                            onSuccess = { onListingCreated() },
                            onFailure = { submitError = it.localizedMessage ?: "Could not publish piece." }
                        )
                    }
                },
                enabled = !isSubmitting,
                backgroundColor = if (title.isNotBlank()) InkCharcoal else InkMuted,
                testTag = "publish_listing_button"
            )
        }
    }

    // Full-Screen Interactive Photo Editor Modal (Auto-opens on pick & via Edit button)
    val editingDraft = attachedPhotos.find { it.id == activeEditorPhotoId }
    AnimatedVisibility(
        visible = editingDraft != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        if (editingDraft != null) {
            val queueLabel = if (queueTotalCount > 1) {
                "Photo $queueCompletedCount of $queueTotalCount"
            } else {
                null
            }
            PhotoEditorModal(
                draftPhoto = editingDraft,
                queuePositionText = queueLabel,
                hasNextInQueue = pendingEditQueue.isNotEmpty(),
                onApply = { updatedEditState, renderedBitmap ->
                    attachedPhotos = attachedPhotos.map { item ->
                        if (item.id == editingDraft.id) {
                            item.copy(
                                editState = updatedEditState,
                                previewBitmap = renderedBitmap,
                                isNewlyAdded = false
                            )
                        } else {
                            item
                        }
                    }
                    val nextId = pendingEditQueue.firstOrNull()
                    if (nextId != null) {
                        pendingEditQueue = pendingEditQueue.drop(1)
                        queueCompletedCount += 1
                        activeEditorPhotoId = nextId
                    } else {
                        activeEditorPhotoId = null
                        queueTotalCount = 0
                    }
                },
                onCancel = {
                    if (editingDraft.isNewlyAdded) {
                        attachedPhotos = attachedPhotos.filterNot { it.id == editingDraft.id }
                    }
                    val nextId = pendingEditQueue.firstOrNull()
                    if (nextId != null) {
                        pendingEditQueue = pendingEditQueue.drop(1)
                        queueCompletedCount += 1
                        activeEditorPhotoId = nextId
                    } else {
                        activeEditorPhotoId = null
                        queueTotalCount = 0
                    }
                }
            )
        }
    }
    }
}

@Composable
private fun EditorialFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minHeightDp: Int = 52,
    testTag: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = LoopType.EditorialTag,
            color = InkSecondary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PaperPureWhite)
                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .then(if (!singleLine) Modifier.height(minHeightDp.dp) else Modifier),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = LoopType.BodyMedium,
                    color = InkMuted
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = LoopType.BodyMedium.copy(color = InkCharcoal),
                cursorBrush = SolidColor(InkCharcoal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag)
            )
        }
    }
}

