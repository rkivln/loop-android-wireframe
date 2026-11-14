package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.AiValuationUiState
import com.example.ai.GeminiAiService
import com.example.data.RentalRepository
import com.example.data.models.RentalCategory
import com.example.ui.components.CategoryTile
import com.example.ui.components.EditorialTopBar
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
import kotlinx.coroutines.launch

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
    var location by remember { mutableStateOf("White Town, Puducherry") }
    var selectedCategory by remember { mutableStateOf(RentalCategory.ELECTRONICS) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var aiState by remember { mutableStateOf<AiValuationUiState>(AiValuationUiState.Idle) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperIvory)
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
            // Upload Studio Photography Box (Zero-permission Android Photo Picker)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(20.dp))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("upload_photo_card"),
                contentAlignment = Alignment.Center
            ) {
                if (selectedPhotoUri != null) {
                    AsyncImage(
                        model = selectedPhotoUri,
                        contentDescription = "Selected piece photography",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperPureWhite.copy(alpha = 0.94f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentForestGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Photo attached · Tap to change",
                            style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                            color = InkCharcoal
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
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
                            text = "Natural lighting, front and detail perspectives",
                            style = LoopType.BodySmall,
                            color = InkMuted
                        )
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
                placeholder = "e.g. Leica M10 & 35mm Summicron",
                testTag = "listing_title_input"
            )

            // Daily Circulation Rate
            EditorialFormField(
                label = "DAILY CIRCULATION RATE (₹)",
                value = dailyRateStr,
                onValueChange = { dailyRateStr = it.filter { ch -> ch.isDigit() } },
                placeholder = "e.g. 750",
                keyboardType = KeyboardType.Number,
                testTag = "listing_rate_input"
            )

            // Security Deposit
            EditorialFormField(
                label = "SECURITY DEPOSIT (₹)",
                value = depositStr,
                onValueChange = { depositStr = it.filter { ch -> ch.isDigit() } },
                placeholder = "e.g. 2000",
                keyboardType = KeyboardType.Number,
                testTag = "listing_deposit_input"
            )

            // Pickup Neighborhood
            EditorialFormField(
                label = "PICKUP NEIGHBORHOOD",
                value = location,
                onValueChange = { location = it },
                placeholder = "White Town, Puducherry",
                testTag = "listing_location_input"
            )

            // Curator's Notes & Specs
            EditorialFormField(
                label = "CURATOR'S NOTES & SPECS",
                value = description,
                onValueChange = { description = it },
                placeholder = "Detail included lenses, flight case, battery health, and care instructions...",
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
                            text = "Estimate a fair Puducherry daily rate and security deposit from your title and notes.",
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
                        val base64Image = selectedPhotoUri?.let { uri ->
                            uriToBase64DataUri(context, uri)
                        }
                        val res = RentalRepository.createListing(
                            title = title,
                            price = rate,
                            deposit = dep,
                            location = location,
                            category = selectedCategory,
                            desc = description,
                            imageUrl = base64Image
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

private fun uriToBase64DataUri(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (bitmap != null) {
            val outputStream = java.io.ByteArrayOutputStream()
            val maxDim = 800
            val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val w = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
                val h = if (ratio > 1) (maxDim / ratio).toInt() else maxDim
                android.graphics.Bitmap.createScaledBitmap(bitmap, w, h, true)
            } else {
                bitmap
            }
            scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
            val bytes = outputStream.toByteArray()
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64"
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

