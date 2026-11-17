package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RentalRepository
import com.example.data.models.AVATAR_PRESETS
import com.example.data.models.LocationSharingPreference
import com.example.data.models.ProfileVisibility
import com.example.data.models.RentalOwner
import com.example.data.models.UserProfile
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentWarmYellow
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberProfileModalSheet(
    owner: RentalOwner,
    fallbackLocation: String = "",
    isCurrentUser: Boolean = false,
    showMessageButton: Boolean = !isCurrentUser,
    hasActiveRelationship: Boolean = false,
    onMessageClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    BackHandler(onBack = onDismiss)

    var memberProfile by remember(owner.id) { mutableStateOf<UserProfile?>(null) }
    var isLoadingProfile by remember(owner.id) { mutableStateOf(owner.id.isNotBlank()) }

    LaunchedEffect(owner.id) {
        if (owner.id.isNotBlank()) {
            isLoadingProfile = true
            memberProfile = RentalRepository.fetchMemberProfile(owner.id)
        }
        isLoadingProfile = false
    }

    val isPrivateProfile = memberProfile != null &&
        !isCurrentUser &&
        !hasActiveRelationship &&
        memberProfile?.settings?.profileVisibility == ProfileVisibility.PRIVATE

    val isLocationHidden = memberProfile != null &&
        !isCurrentUser &&
        memberProfile?.settings?.locationSharing == LocationSharingPreference.HIDDEN

    val displayName = memberProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: owner.name.ifBlank { "Community Member" }

    val initials = remember(displayName, owner.initials) {
        val parts = displayName.trim().split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            owner.initials.isNotBlank() -> owner.initials
            else -> "M"
        }
    }

    val avatarPresetIndex = memberProfile?.avatarPresetIndex
        ?: (abs(owner.id.hashCode()) % AVATAR_PRESETS.size)
    val avatarPreset = AVATAR_PRESETS.getOrElse(avatarPresetIndex) { AVATAR_PRESETS[0] }

    val isVerified = memberProfile?.isVerified == true ||
        owner.badge.contains("Verified", ignoreCase = true)

    val effectiveRating = when {
        (memberProfile?.rating ?: 0f) > 0f -> memberProfile!!.rating
        owner.rating > 0f -> owner.rating
        else -> 0f
    }
    val effectiveReviewCount = memberProfile?.reviewCount ?: 0

    val effectiveMemberSince = memberProfile?.memberSince?.takeIf { it.isNotBlank() }
        ?: owner.memberSince

    val resolvedLocation = when {
        isPrivateProfile || isLocationHidden -> "Location hidden"
        !memberProfile?.location.isNullOrBlank() -> memberProfile!!.location
        fallbackLocation.isNotBlank() -> fallbackLocation
        else -> "Neighborhood location not shared"
    }

    val resolvedBio = when {
        isPrivateProfile -> "This member keeps their profile details private until an active rental or chat is underway."
        !memberProfile?.bio.isNullOrBlank() -> memberProfile!!.bio
        isLoadingProfile -> "Loading member bio..."
        else -> "No bio added yet."
    }

    val resolvedBadges = remember(memberProfile, owner.badge, isPrivateProfile) {
        if (isPrivateProfile) {
            listOf(owner.badge.ifBlank { "Community Member" }).distinct()
        } else {
            val fromProfile = memberProfile?.badges.orEmpty()
                .map { it.trim() }
                .filter { it.isNotBlank() }
            if (fromProfile.isNotEmpty()) {
                fromProfile.distinct()
            } else {
                listOf(owner.badge.ifBlank { "Community Member" }).distinct()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(PaperPureWhite)
                .clickable(enabled = false) {}
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .navigationBarsPadding()
                .testTag("member_profile_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "MEMBER PROFILE",
                            style = LoopType.EditorialTag,
                            color = InkSecondary
                        )
                        if (isLoadingProfile) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = AccentForestGreen
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("member_profile_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close profile card",
                            tint = InkSecondary
                        )
                    }
                }

                // Header Profile Dossier: Avatar, Name, Location, Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(avatarPreset.bgHex1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = LoopType.HeadlineMedium.copy(fontSize = 22.sp),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isCurrentUser) "$displayName (You)" else displayName,
                                style = LoopType.HeadlineMedium.copy(fontSize = 19.sp),
                                color = InkCharcoal,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .testTag("member_profile_name")
                            )
                            if (isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Identity",
                                    tint = AccentForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Location
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("member_profile_location")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = InkSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = resolvedLocation,
                                style = LoopType.BodySmall,
                                color = InkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Rating & Member Since
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("member_profile_rating")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = if (effectiveRating > 0f) AccentWarmYellow else InkMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                val ratingLabel = when {
                                    effectiveRating > 0f && effectiveReviewCount > 0 ->
                                        "$effectiveRating ($effectiveReviewCount ${if (effectiveReviewCount == 1) "review" else "reviews"})"
                                    effectiveRating > 0f -> "$effectiveRating rating"
                                    else -> "No reviews yet"
                                }
                                Text(
                                    text = ratingLabel,
                                    style = LoopType.Metadata.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (effectiveRating > 0f) InkCharcoal else InkSecondary
                                    )
                                )
                            }

                            if (effectiveMemberSince.isNotBlank()) {
                                Text(
                                    text = "·",
                                    style = LoopType.Metadata,
                                    color = InkMuted
                                )
                                Text(
                                    text = effectiveMemberSince,
                                    style = LoopType.Metadata.copy(fontSize = 11.sp),
                                    color = InkSecondary
                                )
                            }
                        }
                    }
                }

                // Bio Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                        .testTag("member_profile_bio")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "BIO",
                            style = LoopType.EditorialTag.copy(fontSize = 9.5.sp),
                            color = InkSecondary
                        )
                        Text(
                            text = resolvedBio,
                            style = LoopType.BodyEditorial.copy(fontSize = 13.5.sp),
                            color = if (!memberProfile?.bio.isNullOrBlank() && !isPrivateProfile) InkCharcoal else InkSecondary
                        )
                    }
                }

                // Badges Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_profile_badges"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "BADGES",
                        style = LoopType.EditorialTag.copy(fontSize = 9.5.sp),
                        color = InkSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        resolvedBadges.forEachIndexed { index, badge ->
                            val isPrimaryVerified = index == 0 && isVerified
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isPrimaryVerified) AccentMint else PaperIvory)
                                    .border(
                                        1.dp,
                                        if (isPrimaryVerified) AccentForestGreen.copy(alpha = 0.35f) else PaperBorder,
                                        RoundedCornerShape(100.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = badge,
                                    style = LoopType.Metadata.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isPrimaryVerified) AccentForestGreen else InkCharcoal
                                )
                            }
                        }
                    }
                }

                if (showMessageButton && !isCurrentUser && owner.id.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    PrimaryCTA(
                        text = "Message Member",
                        onClick = {
                            onDismiss()
                            onMessageClick()
                        },
                        testTag = "member_profile_message_btn"
                    )
                }
            }
        }
    }
}
