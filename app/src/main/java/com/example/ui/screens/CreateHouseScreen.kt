package com.example.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ai.AiHouseAutoFillUiState
import com.example.ai.GeminiAiService
import com.example.data.HouseRentalRepository
import com.example.data.LocationHelper
import com.example.data.RentalRepository
import com.example.data.models.DEFAULT_ROOM_LABELS
import com.example.data.models.FurnishingStatus
import com.example.data.models.HouseAmenity
import com.example.data.models.HouseListing
import com.example.data.models.HousePropertyType
import com.example.data.models.LocationSharingPreference
import com.example.data.models.RentalOwner
import com.example.data.models.TenantPreference
import com.example.ui.components.DraftListingPhoto
import com.example.ui.components.EditorialTopBar
import com.example.ui.components.ListingPhoto
import com.example.ui.components.PhotoBitmapProcessor
import com.example.ui.components.PhotoEditorModal
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SecondaryCTA
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateHouseScreen(
    editingHouse: HouseListing? = null,
    onBack: () -> Unit = {},
    onHouseSaved: (HouseListing) -> Unit = {},
    onHouseDeleted: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val isEditMode = editingHouse != null
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentProfile by RentalRepository.userProfileFlow.collectAsStateWithLifecycle()
    val userSettings by RentalRepository.userSettingsFlow.collectAsStateWithLifecycle()

    var title by remember(editingHouse?.id) { mutableStateOf(editingHouse?.title.orEmpty()) }
    var propertyType by remember(editingHouse?.id) {
        mutableStateOf(editingHouse?.propertyType ?: HousePropertyType.APARTMENT)
    }
    var furnishing by remember(editingHouse?.id) {
        mutableStateOf(editingHouse?.furnishing ?: FurnishingStatus.SEMI_FURNISHED)
    }
    var tenantPref by remember(editingHouse?.id) {
        mutableStateOf(editingHouse?.tenantPreference ?: TenantPreference.ANY)
    }
    var bhkStr by remember(editingHouse?.id) { mutableStateOf((editingHouse?.bhkCount ?: 2).toString()) }
    var bathStr by remember(editingHouse?.id) { mutableStateOf((editingHouse?.bathrooms ?: 2).toString()) }
    var areaStr by remember(editingHouse?.id) { mutableStateOf((editingHouse?.areaSqFt ?: 1050).toString()) }
    var floorStr by remember(editingHouse?.id) { mutableStateOf((editingHouse?.floorNumber ?: 1).toString()) }
    var totalFloorsStr by remember(editingHouse?.id) { mutableStateOf((editingHouse?.totalFloors ?: 3).toString()) }

    var monthlyRentStr by remember(editingHouse?.id) {
        mutableStateOf(editingHouse?.monthlyRent?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var depositStr by remember(editingHouse?.id) {
        mutableStateOf(editingHouse?.securityDeposit?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var maintenanceStr by remember(editingHouse?.id) {
        mutableStateOf(editingHouse?.maintenanceMonthly?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var minLeaseStr by remember(editingHouse?.id) {
        mutableStateOf((editingHouse?.minLeaseMonths ?: 11).toString())
    }
    var availableFromLabel by remember(editingHouse?.id) {
        mutableStateOf(editingHouse?.availableFromLabel ?: "Immediate")
    }
    var isAvailableForRent by remember(editingHouse?.id) {
        mutableStateOf(editingHouse?.isAvailableForRent ?: true)
    }
    var location by remember(editingHouse?.id) { mutableStateOf(editingHouse?.location.orEmpty()) }
    var fullAddressNote by remember(editingHouse?.id) { mutableStateOf(editingHouse?.fullAddressNote.orEmpty()) }
    var description by remember(editingHouse?.id) { mutableStateOf(editingHouse?.description.orEmpty()) }
    var latitude by remember(editingHouse?.id) { mutableStateOf(editingHouse?.latitude) }
    var longitude by remember(editingHouse?.id) { mutableStateOf(editingHouse?.longitude) }
    var selectedAmenities by remember(editingHouse?.id) {
        mutableStateOf(editingHouse?.amenities?.toSet() ?: setOf(HouseAmenity.WATER_24X7))
    }

    var attachedPhotos by remember(editingHouse?.id) {
        mutableStateOf<List<DraftListingPhoto>>(
            editingHouse?.resolvedGalleryUrls?.mapIndexed { idx, url ->
                DraftListingPhoto(id = "existing_${editingHouse.id}_$idx", existingImageUrl = url)
            } ?: emptyList()
        )
    }
    var photoLabels by remember(editingHouse?.id) {
        mutableStateOf<List<String>>(
            editingHouse?.photoLabels.orEmpty().ifEmpty { listOf("Living Room", "Bedroom", "Kitchen", "Exterior") }
        )
    }

    var activeEditorPhotoId by remember { mutableStateOf<String?>(null) }
    var pendingEditQueue by remember { mutableStateOf<List<String>>(emptyList()) }
    var queueTotalCount by remember { mutableIntStateOf(0) }
    var queueCompletedCount by remember { mutableIntStateOf(0) }
    var isLocatingGps by remember { mutableStateOf(false) }
    var aiState by remember { mutableStateOf<AiHouseAutoFillUiState>(AiHouseAutoFillUiState.Idle) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val detectGps: (Boolean) -> Unit = { overwrite ->
        isLocatingGps = true
        LocationHelper.fetchCurrentLocation(
            context = context,
            onLocationReceived = { lat, lng ->
                latitude = lat
                longitude = lng
                coroutineScope.launch {
                    val area = LocationHelper.reverseGeocodeLocation(context, lat, lng)
                    if (!area.isNullOrBlank() && (overwrite || location.isBlank())) {
                        location = area
                    }
                    isLocatingGps = false
                }
            },
            onLocationUnavailable = { isLocatingGps = false }
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) detectGps(true)
    }

    LaunchedEffect(editingHouse?.id) {
        if (!isEditMode &&
            userSettings.locationSharing != LocationSharingPreference.HIDDEN &&
            LocationHelper.hasLocationPermission(context)
        ) {
            detectGps(false)
        }
    }

    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        val remaining = (4 - attachedPhotos.size).coerceAtLeast(0)
        val picked = uris.take(remaining)
        if (picked.isNotEmpty()) {
            val newDrafts = picked.map { uri ->
                DraftListingPhoto(id = "house_photo_${UUID.randomUUID()}", sourceUri = uri, isNewlyAdded = true)
            }
            val newIds = newDrafts.map { it.id }
            val startIdx = attachedPhotos.size
            attachedPhotos = attachedPhotos + newDrafts
            val updatedLabels = photoLabels.toMutableList()
            while (updatedLabels.size < attachedPhotos.size) {
                updatedLabels.add(DEFAULT_ROOM_LABELS.getOrElse(updatedLabels.size) { "Room ${updatedLabels.size + 1}" })
            }
            if (updatedLabels.size > startIdx && updatedLabels[startIdx].isBlank()) {
                updatedLabels[startIdx] = DEFAULT_ROOM_LABELS.getOrElse(startIdx) { "Living Room" }
            }
            photoLabels = updatedLabels
            pendingEditQueue = newIds.drop(1)
            queueTotalCount = newIds.size
            queueCompletedCount = 1
            activeEditorPhotoId = newIds.firstOrNull()
        }
    }

    val runAiHouseScan: () -> Unit = {
        coroutineScope.launch {
            aiState = AiHouseAutoFillUiState.Loading("Preparing property photos for Gemini...")
            val bitmaps = withContext(Dispatchers.IO) {
                attachedPhotos.mapNotNull { photo ->
                    photo.previewBitmap ?: when {
                        photo.sourceUri != Uri.EMPTY -> PhotoBitmapProcessor.loadOrientedSourceBitmap(context, photo.sourceUri, 640)
                        !photo.existingImageUrl.isNullOrBlank() -> PhotoBitmapProcessor.loadExistingBitmap(photo.existingImageUrl, 640)
                        else -> null
                    }
                }
            }
            val result = GeminiAiService.analyzeHousePhotosForListing(
                photos = bitmaps,
                existingTitle = title,
                neighborhoodHint = location,
                onStatusUpdate = { aiState = AiHouseAutoFillUiState.Loading(it) }
            )
            aiState = result.fold(
                onSuccess = { draft ->
                    if (title.isBlank()) title = draft.title
                    propertyType = HousePropertyType.fromNameOrDefault(draft.propertyTypeKey, propertyType)
                    furnishing = FurnishingStatus.fromNameOrDefault(draft.furnishingKey, furnishing)
                    bhkStr = draft.estimatedBhk.toString()
                    areaStr = draft.estimatedAreaSqFt.toString()
                    if (description.isBlank()) description = draft.description
                    if (monthlyRentStr.isBlank()) monthlyRentStr = draft.suggestedMonthlyRentInr.toString()
                    if (depositStr.isBlank()) depositStr = draft.recommendedDepositInr.toString()
                    if (draft.photoRoomLabels.isNotEmpty()) {
                        photoLabels = attachedPhotos.indices.map { idx ->
                            draft.photoRoomLabels.getOrNull(idx) ?: photoLabels.getOrNull(idx) ?: DEFAULT_ROOM_LABELS[idx % DEFAULT_ROOM_LABELS.size]
                        }
                    }
                    AiHouseAutoFillUiState.Success(draft)
                },
                onFailure = { err ->
                    AiHouseAutoFillUiState.Error(err.localizedMessage ?: "AI scan failed.")
                }
            )
        }
    }

    val submitHouse: () -> Unit = {
        val rent = monthlyRentStr.trim().toIntOrNull()
        val deposit = depositStr.trim().toIntOrNull() ?: 0
        val maintenance = maintenanceStr.trim().toIntOrNull() ?: 0
        val minLease = (minLeaseStr.trim().toIntOrNull() ?: 11).coerceIn(1, 60)
        val bhk = (bhkStr.trim().toIntOrNull() ?: 2).coerceIn(0, 12)
        val baths = (bathStr.trim().toIntOrNull() ?: 1).coerceIn(1, 12)
        val area = (areaStr.trim().toIntOrNull() ?: 900).coerceIn(150, 20000)
        val floor = (floorStr.trim().toIntOrNull() ?: 1).coerceIn(0, 99)
        val totalFloors = (totalFloorsStr.trim().toIntOrNull() ?: 2).coerceIn(1, 99)

        when {
            title.trim().length < 3 -> errorMessage = "Enter a home title (at least 3 characters)."
            rent == null || rent < 1000 -> errorMessage = "Enter a monthly rent of at least ₹1,000."
            location.trim().isBlank() -> errorMessage = "Enter the neighborhood or locality."
            description.trim().length < 10 -> errorMessage = "Add a helpful description of the property and lease terms."
            else -> {
                isSubmitting = true
                errorMessage = null
                coroutineScope.launch {
                    val encodedUrls = withContext(Dispatchers.IO) {
                        attachedPhotos.take(4).mapNotNull { draft ->
                            when {
                                draft.previewBitmap != null ->
                                    PhotoBitmapProcessor.bitmapToBase64DataUri(draft.previewBitmap, 120_000)
                                !draft.existingImageUrl.isNullOrBlank() -> draft.existingImageUrl
                                draft.sourceUri != Uri.EMPTY ->
                                    PhotoBitmapProcessor.loadOrientedSourceBitmap(context, draft.sourceUri, 768)
                                        ?.let { PhotoBitmapProcessor.bitmapToBase64DataUri(it, 120_000) }
                                else -> null
                            }
                        }
                    }
                    val labelsForSave = encodedUrls.indices.map { idx ->
                        photoLabels.getOrNull(idx)?.takeIf { it.isNotBlank() }
                            ?: DEFAULT_ROOM_LABELS.getOrElse(idx) { "Room ${idx + 1}" }
                    }
                    val owner = editingHouse?.owner ?: RentalOwner(
                        id = currentProfile.uid,
                        name = currentProfile.displayName.ifBlank { "Verified Owner" },
                        badge = "Verified Landlord",
                        initials = currentProfile.initials,
                        avatarUrl = currentProfile.customAvatarUri,
                        avatarPresetIndex = currentProfile.avatarPresetIndex
                    )
                    val payload = HouseListing(
                        id = editingHouse?.id ?: "house_${UUID.randomUUID().toString().replace("-", "").take(14)}",
                        title = title.trim(),
                        propertyType = propertyType,
                        bhkCount = bhk,
                        bathrooms = baths,
                        areaSqFt = area,
                        floorNumber = floor,
                        totalFloors = totalFloors.coerceAtLeast(floor.coerceAtLeast(1)),
                        furnishing = furnishing,
                        tenantPreference = tenantPref,
                        monthlyRent = rent,
                        securityDeposit = deposit,
                        maintenanceMonthly = maintenance,
                        minLeaseMonths = minLease,
                        availableFromLabel = availableFromLabel.trim().ifBlank { "Immediate" },
                        isAvailableForRent = isAvailableForRent,
                        location = location.trim(),
                        fullAddressNote = fullAddressNote.trim(),
                        description = description.trim(),
                        amenities = selectedAmenities.toList(),
                        imageUrl = encodedUrls.firstOrNull(),
                        imageUrls = encodedUrls,
                        photoLabels = labelsForSave,
                        imageCount = encodedUrls.size,
                        latitude = latitude,
                        longitude = longitude,
                        owner = owner,
                        rating = editingHouse?.rating ?: 0f,
                        reviewCount = editingHouse?.reviewCount ?: 0
                    )
                    val res = if (isEditMode) {
                        HouseRentalRepository.updateHouse(payload)
                    } else {
                        HouseRentalRepository.publishHouse(payload)
                    }
                    isSubmitting = false
                    res.fold(
                        onSuccess = { saved -> onHouseSaved(saved) },
                        onFailure = { err ->
                            errorMessage = err.localizedMessage ?: "Failed to publish home."
                        }
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
            .testTag("create_house_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EditorialTopBar(
                title = if (isEditMode) "Edit Home Listing" else "List a Home for Rent",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Multi-photo Gallery + Room Labels
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ROOM & PROPERTY PHOTOS (${attachedPhotos.size}/4)",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        if (attachedPhotos.size < 4) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(InkCharcoal)
                                    .clickable {
                                        multiPhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                    .testTag("create_house_add_photos_btn"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = "Add room photos",
                                    tint = InkWhite,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add Photos",
                                    style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold),
                                    color = InkWhite
                                )
                            }
                        }
                    }

                    if (attachedPhotos.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            attachedPhotos.forEachIndexed { idx, draft ->
                                Column(
                                    modifier = Modifier.width(126.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(126.dp, 96.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(PaperPureWhite)
                                            .border(1.dp, PaperBorder, RoundedCornerShape(16.dp))
                                            .clickable { activeEditorPhotoId = draft.id }
                                    ) {
                                        if (draft.previewBitmap != null) {
                                            Image(
                                                bitmap = draft.previewBitmap.asImageBitmap(),
                                                contentDescription = "Room photo ${idx + 1}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            ListingPhoto(
                                                imageUrl = draft.existingImageUrl ?: draft.sourceUri.toString(),
                                                fallbackRes = com.example.R.drawable.img_welcome_hero,
                                                contentDescription = "Room photo ${idx + 1}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.65f))
                                                .clickable {
                                                    attachedPhotos = attachedPhotos.filterNot { it.id == draft.id }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove photo",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(PaperPureWhite.copy(alpha = 0.9f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Crop photo",
                                                tint = InkCharcoal,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    val currentTag = photoLabels.getOrNull(idx)
                                        ?: DEFAULT_ROOM_LABELS.getOrElse(idx) { "Room ${idx + 1}" }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(PaperPureWhite)
                                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(10.dp))
                                            .clickable {
                                                val nextTag = DEFAULT_ROOM_LABELS[
                                                    (DEFAULT_ROOM_LABELS.indexOf(currentTag) + 1) % DEFAULT_ROOM_LABELS.size
                                                ]
                                                val list = photoLabels.toMutableList()
                                                while (list.size <= idx) list.add("Room ${list.size + 1}")
                                                list[idx] = nextTag
                                                photoLabels = list
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentTag,
                                            style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                            color = AccentForestGreen,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Optional Gemini AI Room Scan
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(PaperPureWhite)
                                .border(1.dp, PaperBorder, RoundedCornerShape(16.dp))
                                .clickable(enabled = aiState !is AiHouseAutoFillUiState.Loading) { runAiHouseScan() }
                                .padding(14.dp)
                                .testTag("create_house_ai_scan_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (aiState is AiHouseAutoFillUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = AccentForestGreen,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI room scan",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (val st = aiState) {
                                        is AiHouseAutoFillUiState.Loading -> st.statusMessage
                                        is AiHouseAutoFillUiState.Success -> "AI filled details & monthly rent suggestion"
                                        is AiHouseAutoFillUiState.Error -> st.message
                                        else -> "Auto-Fill Details & Rent with Gemini AI"
                                    },
                                    style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (aiState is AiHouseAutoFillUiState.Error) AlertCrimson else InkCharcoal
                                )
                                Text(
                                    text = "Detects room types, furnishing & fair monthly rent",
                                    style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                                    color = InkSecondary
                                )
                            }
                        }
                    }
                }

                // Title
                HouseTextField(
                    label = "PROPERTY HEADLINE",
                    value = title,
                    onValueChange = { title = it; errorMessage = null },
                    placeholder = "e.g. Sunlit 2BHK Heritage Flat in White Town",
                    testTag = "house_title_input"
                )

                // Property Type & Furnishing
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PROPERTY TYPE",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HousePropertyType.entries.forEach { pt ->
                            val selected = propertyType == pt
                            ChoicePill(
                                label = pt.label,
                                selected = selected,
                                onClick = { propertyType = pt },
                                testTag = "house_type_pill_${pt.name}"
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FURNISHING STATUS",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FurnishingStatus.entries.forEach { fs ->
                            ChoicePill(
                                label = fs.label,
                                selected = furnishing == fs,
                                onClick = { furnishing = fs },
                                testTag = "house_furnish_pill_${fs.name}"
                            )
                        }
                    }
                }

                // Configuration Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HouseTextField(
                        label = "BHK",
                        value = bhkStr,
                        onValueChange = { bhkStr = it.filter(Char::isDigit).take(2) },
                        placeholder = "2",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "house_bhk_input"
                    )
                    HouseTextField(
                        label = "BATHS",
                        value = bathStr,
                        onValueChange = { bathStr = it.filter(Char::isDigit).take(2) },
                        placeholder = "2",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "house_bath_input"
                    )
                    HouseTextField(
                        label = "AREA (SQFT)",
                        value = areaStr,
                        onValueChange = { areaStr = it.filter(Char::isDigit).take(5) },
                        placeholder = "1050",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1.3f),
                        testTag = "house_area_input"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HouseTextField(
                        label = "FLOOR NO.",
                        value = floorStr,
                        onValueChange = { floorStr = it.filter(Char::isDigit).take(2) },
                        placeholder = "1",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    HouseTextField(
                        label = "TOTAL FLOORS",
                        value = totalFloorsStr,
                        onValueChange = { totalFloorsStr = it.filter(Char::isDigit).take(2) },
                        placeholder = "3",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Long-Term Monthly Lease Ledger Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HouseTextField(
                        label = "MONTHLY RENT (₹)",
                        value = monthlyRentStr,
                        onValueChange = { monthlyRentStr = it.filter(Char::isDigit).take(7); errorMessage = null },
                        placeholder = "18000",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "house_monthly_rent_input"
                    )
                    HouseTextField(
                        label = "DEPOSIT (₹)",
                        value = depositStr,
                        onValueChange = { depositStr = it.filter(Char::isDigit).take(7) },
                        placeholder = "50000",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "house_deposit_input"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HouseTextField(
                        label = "MAINTENANCE (₹/MO)",
                        value = maintenanceStr,
                        onValueChange = { maintenanceStr = it.filter(Char::isDigit).take(6) },
                        placeholder = "0",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    HouseTextField(
                        label = "MIN LEASE (MONTHS)",
                        value = minLeaseStr,
                        onValueChange = { minLeaseStr = it.filter(Char::isDigit).take(2) },
                        placeholder = "11",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }

                HouseTextField(
                    label = "AVAILABLE FROM",
                    value = availableFromLabel,
                    onValueChange = { availableFromLabel = it.take(40) },
                    placeholder = "Immediate / 1st Next Month"
                )

                // Tenant Preference
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TENANT PREFERENCE",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TenantPreference.entries.forEach { pref ->
                            ChoicePill(
                                label = pref.label,
                                selected = tenantPref == pref,
                                onClick = { tenantPref = pref },
                                testTag = "house_pref_pill_${pref.name}"
                            )
                        }
                    }
                }

                // Neighborhood & Private Confirmed Directions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NEIGHBORHOOD / LOCALITY",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(AccentMint)
                                .clickable {
                                    if (LocationHelper.hasLocationPermission(context)) {
                                        detectGps(true)
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("create_house_gps_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Use GPS",
                                tint = AccentForestGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isLocatingGps) "Locating..." else "Use GPS",
                                style = LoopType.Metadata.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = AccentForestGreen
                            )
                        }
                    }
                    HouseTextField(
                        label = null,
                        value = location,
                        onValueChange = { location = it; errorMessage = null },
                        placeholder = "e.g. White Town, Puducherry",
                        testTag = "house_location_input"
                    )
                }

                HouseTextField(
                    label = "STREET LANDMARK (SHARED ONLY ON CONFIRMED VISITS)",
                    value = fullAddressNote,
                    onValueChange = { fullAddressNote = it.take(240) },
                    placeholder = "e.g. Rue Dumas, Gate #14 opposite Aurobindo Ashram"
                )

                // Description
                HouseTextField(
                    label = "HOME DESCRIPTION & HOUSE RULES",
                    value = description,
                    onValueChange = { description = it; errorMessage = null },
                    placeholder = "Describe natural light, ventilation, kitchen setup, water/power supply, nearby transit...",
                    minHeight = 96.dp,
                    testTag = "house_description_input"
                )

                // Amenities
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "AMENITIES",
                        style = LoopType.EditorialTag,
                        color = InkSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HouseAmenity.entries.forEach { amenity ->
                            val selected = amenity in selectedAmenities
                            ChoicePill(
                                label = amenity.label,
                                selected = selected,
                                onClick = {
                                    selectedAmenities = if (selected) {
                                        selectedAmenities - amenity
                                    } else {
                                        selectedAmenities + amenity
                                    }
                                },
                                testTag = "house_amenity_${amenity.name}"
                            )
                        }
                    }
                }

                if (isEditMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Available for Rent",
                                style = LoopType.TitleMedium,
                                color = InkCharcoal
                            )
                            Text(
                                text = "Pause when rented out or undergoing maintenance",
                                style = LoopType.BodySmall,
                                color = InkSecondary
                            )
                        }
                        Switch(
                            checked = isAvailableForRent,
                            onCheckedChange = { isAvailableForRent = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = AccentForestGreen,
                                checkedThumbColor = InkWhite
                            )
                        )
                    }

                    SecondaryCTA(
                        text = "Remove Property Listing",
                        onClick = { showDeleteConfirm = true },
                        testTag = "delete_house_btn"
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = LoopType.Metadata,
                        color = AlertCrimson
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(thickness = 1.dp, color = PaperBorderSubtle)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperPureWhite)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                PrimaryCTA(
                    text = when {
                        isSubmitting -> if (isEditMode) "Saving Changes..." else "Publishing Home..."
                        isEditMode -> "Save Home Listing"
                        else -> "Publish Home Listing"
                    },
                    enabled = !isSubmitting,
                    onClick = submitHouse,
                    testTag = "publish_house_submit_btn"
                )
            }
        }

        val editorPhoto = activeEditorPhotoId?.let { id -> attachedPhotos.find { it.id == id } }
        if (editorPhoto != null) {
            PhotoEditorModal(
                draftPhoto = editorPhoto,
                queuePositionText = if (queueTotalCount > 1) "Photo $queueCompletedCount of $queueTotalCount" else null,
                hasNextInQueue = pendingEditQueue.isNotEmpty(),
                onApply = { newState, renderedBmp ->
                    attachedPhotos = attachedPhotos.map { p ->
                        if (p.id == editorPhoto.id) p.copy(editState = newState, previewBitmap = renderedBmp) else p
                    }
                    if (pendingEditQueue.isNotEmpty()) {
                        val nextId = pendingEditQueue.first()
                        pendingEditQueue = pendingEditQueue.drop(1)
                        queueCompletedCount++
                        activeEditorPhotoId = nextId
                    } else {
                        activeEditorPhotoId = null
                        queueTotalCount = 0
                        queueCompletedCount = 0
                    }
                },
                onCancel = {
                    activeEditorPhotoId = null
                    pendingEditQueue = emptyList()
                    queueTotalCount = 0
                    queueCompletedCount = 0
                }
            )
        }

        if (showDeleteConfirm && editingHouse != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                containerColor = PaperPureWhite,
                title = { Text("Remove house listing?", style = LoopType.HeadlineMedium, color = InkCharcoal) },
                text = {
                    Text(
                        text = "'${editingHouse.title}' will be permanently removed from Loop Houses.",
                        style = LoopType.BodyMedium,
                        color = InkSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        coroutineScope.launch {
                            HouseRentalRepository.deleteHouse(editingHouse.id).onSuccess {
                                onHouseDeleted()
                            }
                        }
                    }) {
                        Text("Delete", color = AlertCrimson, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel", color = InkCharcoal)
                    }
                }
            )
        }
    }
}

@Composable
private fun ChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) InkCharcoal else PaperPureWhite)
            .border(1.dp, if (selected) InkCharcoal else PaperBorder, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = LoopType.Metadata.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
            color = if (selected) InkWhite else InkCharcoal
        )
    }
}

@Composable
private fun HouseTextField(
    label: String?,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = LoopType.EditorialTag,
                color = InkSecondary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(PaperPureWhite)
                .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = if (minHeight > 54.dp) Alignment.TopStart else Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = LoopType.BodySmall,
                    color = InkMuted
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = LoopType.BodyMedium.copy(color = InkCharcoal),
                cursorBrush = SolidColor(InkCharcoal),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            )
        }
    }
}
