package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Title
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.LocationHelper
import com.example.data.models.CommunityPostType
import com.example.data.models.RentalCategory
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
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

data class CommunityFormData(
    val title: String,
    val type: CommunityPostType,
    val category: RentalCategory,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String
)

data class LocationPreset(
    val label: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

private val PUDUCHERRY_LOCATION_PRESETS = listOf(
    LocationPreset("White Town", "Suffren St Courtyard, White Town", 11.9338, 79.8350),
    LocationPreset("Promenade Seafront", "Rock Beach Promenade, White Town", 11.9295, 79.8370),
    LocationPreset("Heritage Quarter", "Romain Rolland Studio, Heritage Quarter", 11.9372, 79.8270),
    LocationPreset("Lawspet Quarter", "Lawspet Cultural Quarter, Puducherry", 11.9510, 79.8210)
)

/**
 * Reusable Form Component for creating Help Requests, Events, or Study Groups.
 * Supports title, description, initiative type, gear/topic category, location name, and GPS coordinates.
 */
@Composable
fun CommunityInitiativeForm(
    onSubmit: (CommunityFormData) -> Unit,
    modifier: Modifier = Modifier,
    initialType: CommunityPostType = CommunityPostType.EVENT,
    initialTitle: String = "",
    initialDescription: String = "",
    initialCategory: RentalCategory = RentalCategory.ELECTRONICS,
    initialLocationName: String = "Suffren St, White Town, Puducherry",
    initialLatitude: Double = 11.9338,
    initialLongitude: Double = 79.8350,
    initialDateTime: String = "Today · 5:00 PM – 7:00 PM",
    submitButtonLabel: String = "Publish Initiative",
    onCancel: (() -> Unit)? = null
) {
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var locationName by remember { mutableStateOf(initialLocationName) }
    var latitudeText by remember { mutableStateOf(initialLatitude.toString()) }
    var longitudeText by remember { mutableStateOf(initialLongitude.toString()) }
    var dateTime by remember { mutableStateOf(initialDateTime) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var gpsStatusMessage by remember { mutableStateOf<String?>(null) }

    // GPS Location Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            LocationHelper.fetchCurrentLocation(
                context = context,
                onLocationReceived = { lat, lng ->
                    latitudeText = "%.4f".format(lat)
                    longitudeText = "%.4f".format(lng)
                    gpsStatusMessage = "GPS Synced: (%.4f, %.4f)".format(lat, lng)
                }
            )
        } else {
            gpsStatusMessage = "Location permission denied"
        }
    }

    val requestCurrentGps = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            LocationHelper.fetchCurrentLocation(
                context = context,
                onLocationReceived = { lat, lng ->
                    latitudeText = "%.4f".format(lat)
                    longitudeText = "%.4f".format(lng)
                    gpsStatusMessage = "GPS Synced: (%.4f, %.4f)".format(lat, lng)
                }
            )
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Initiative Type Selector Chips
        Column {
            Text(
                text = "INITIATIVE TYPE",
                style = LoopType.EditorialTag,
                color = InkSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CommunityPostType.values().forEach { type ->
                    val isSelected = selectedType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) InkCharcoal else PaperIvory)
                            .border(
                                1.dp,
                                if (isSelected) InkCharcoal else PaperBorder,
                                RoundedCornerShape(100.dp)
                            )
                            .clickable { selectedType = type }
                            .padding(vertical = 9.dp)
                            .testTag("form_type_${type.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = type.emoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = type.title,
                                style = LoopType.Metadata.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) InkWhite else InkCharcoal
                            )
                        }
                    }
                }
            }
        }

        // 2. Initiative Title Field
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TITLE / SUBJECT",
                    style = LoopType.EditorialTag,
                    color = InkSecondary
                )
                Text(
                    text = "${title.length}/80",
                    style = LoopType.Metadata.copy(fontSize = 10.sp),
                    color = InkMuted
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PaperIvory)
                    .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Title,
                        contentDescription = null,
                        tint = InkMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = title,
                        onValueChange = { if (it.length <= 80) title = it },
                        textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                        cursorBrush = SolidColor(InkCharcoal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_title_input"),
                        decorationBox = { innerTextField ->
                            if (title.isEmpty()) {
                                Text(
                                    text = when (selectedType) {
                                        CommunityPostType.EVENT -> "e.g., 35mm Twilight Street Walk"
                                        CommunityPostType.HELP_REQUEST -> "e.g., Medium Format Darkroom Loading Help"
                                        CommunityPostType.STUDY_GROUP -> "e.g., Architecture Co-Study Circle"
                                    },
                                    style = LoopType.BodySmall.copy(color = InkMuted)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }

        // 3. Category Selector Chips
        Column {
            Text(
                text = "TOPIC / GEAR CATEGORY",
                style = LoopType.EditorialTag,
                color = InkSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RentalCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) InkCharcoal else PaperPureWhite)
                            .border(
                                1.dp,
                                if (isSelected) InkCharcoal else PaperBorder,
                                RoundedCornerShape(100.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("form_cat_${category.name}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = category.iconEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = category.title,
                                style = LoopType.Metadata.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) InkWhite else InkCharcoal
                            )
                        }
                    }
                }
            }
        }

        // 4. Description Field
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DESCRIPTION & DETAILS",
                    style = LoopType.EditorialTag,
                    color = InkSecondary
                )
                Text(
                    text = "${description.length}/300",
                    style = LoopType.Metadata.copy(fontSize = 10.sp),
                    color = InkMuted
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PaperIvory)
                    .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.TopStart
            ) {
                BasicTextField(
                    value = description,
                    onValueChange = { if (it.length <= 300) description = it },
                    textStyle = TextStyle(fontSize = 13.5.sp, color = InkCharcoal, lineHeight = 19.sp),
                    cursorBrush = SolidColor(InkCharcoal),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("form_description_input"),
                    decorationBox = { innerTextField ->
                        if (description.isEmpty()) {
                            Text(
                                text = "Describe what you need help with, gear you're sharing, or event itinerary...",
                                style = LoopType.BodySmall.copy(color = InkMuted)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        // 5. Location Name Field
        Column {
            Text(
                text = "LOCATION / VENUE NAME",
                style = LoopType.EditorialTag,
                color = InkSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PaperIvory)
                    .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = InkMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                        cursorBrush = SolidColor(InkCharcoal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_location_name_input"),
                        decorationBox = { innerTextField ->
                            if (locationName.isEmpty()) {
                                Text(
                                    text = "e.g., Café des Arts Courtyard, White Town",
                                    style = LoopType.BodySmall.copy(color = InkMuted)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }

        // 6. Location Coordinates & GPS Picker Section
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MAP COORDINATES (LAT / LNG)",
                    style = LoopType.EditorialTag,
                    color = InkSecondary
                )

                // Sync with GPS Action
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(AccentMint)
                        .clickable(onClick = requestCurrentGps)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = AccentForestGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Use Current GPS",
                        style = LoopType.Metadata.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = AccentForestGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Lat & Lng Input Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Latitude Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Lat:", style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, color = InkSecondary))
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = latitudeText,
                            onValueChange = { latitudeText = it },
                            textStyle = TextStyle(fontSize = 13.5.sp, color = InkCharcoal),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_latitude_input")
                        )
                    }
                }

                // Longitude Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Lng:", style = LoopType.Metadata.copy(fontWeight = FontWeight.Bold, color = InkSecondary))
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = longitudeText,
                            onValueChange = { longitudeText = it },
                            textStyle = TextStyle(fontSize = 13.5.sp, color = InkCharcoal),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            cursorBrush = SolidColor(InkCharcoal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_longitude_input")
                        )
                    }
                }
            }

            if (gpsStatusMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = gpsStatusMessage ?: "",
                    style = LoopType.Metadata.copy(fontSize = 10.5.sp, color = AccentForestGreen)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Preset Coordinates Chips
            Text(
                text = "Preset Quarters:",
                style = LoopType.Metadata.copy(fontSize = 10.5.sp, color = InkMuted)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PUDUCHERRY_LOCATION_PRESETS.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperPureWhite)
                            .border(1.dp, PaperBorder, RoundedCornerShape(100.dp))
                            .clickable {
                                locationName = preset.name
                                latitudeText = preset.latitude.toString()
                                longitudeText = preset.longitude.toString()
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = preset.label,
                            style = LoopType.Metadata.copy(fontSize = 10.5.sp),
                            color = InkCharcoal
                        )
                    }
                }
            }
        }

        // 7. Schedule / Date & Time Field
        Column {
            Text(
                text = "DATE & SCHEDULE",
                style = LoopType.EditorialTag,
                color = InkSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PaperIvory)
                    .border(1.dp, PaperBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = InkMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = dateTime,
                        onValueChange = { dateTime = it },
                        textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                        cursorBrush = SolidColor(InkCharcoal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_datetime_input"),
                        decorationBox = { innerTextField ->
                            if (dateTime.isEmpty()) {
                                Text(
                                    text = "e.g., Today · 5:00 PM – 7:00 PM",
                                    style = LoopType.BodySmall.copy(color = InkMuted)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }

        // Error message if validation fails
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEE2E2))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    style = LoopType.Metadata.copy(fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryCTA(
                text = submitButtonLabel,
                onClick = {
                    val lat = latitudeText.toDoubleOrNull()
                    val lng = longitudeText.toDoubleOrNull()

                    when {
                        title.isBlank() -> {
                            errorMessage = "Please enter an initiative title."
                        }
                        description.isBlank() -> {
                            errorMessage = "Please provide brief details or objectives."
                        }
                        locationName.isBlank() -> {
                            errorMessage = "Please specify a location name."
                        }
                        lat == null || lat < -90.0 || lat > 90.0 -> {
                            errorMessage = "Please enter a valid latitude (between -90 and 90)."
                        }
                        lng == null || lng < -180.0 || lng > 180.0 -> {
                            errorMessage = "Please enter a valid longitude (between -180 and 180)."
                        }
                        else -> {
                            errorMessage = null
                            val formData = CommunityFormData(
                                title = title.trim(),
                                type = selectedType,
                                category = selectedCategory,
                                description = description.trim(),
                                locationName = locationName.trim(),
                                latitude = lat,
                                longitude = lng,
                                dateTime = dateTime.trim().ifBlank { "Flexible timing" }
                            )
                            onSubmit(formData)
                        }
                    }
                },
                testTag = "submit_community_form_btn"
            )

            if (onCancel != null) {
                SecondaryCTA(
                    text = "Cancel",
                    onClick = onCancel,
                    testTag = "cancel_community_form_btn"
                )
            }
        }
    }
}
