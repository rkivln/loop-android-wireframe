package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RentalRepository
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.CaptionTechnical
import com.example.ui.theme.H1
import com.example.ui.theme.H2
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoopType
import com.example.ui.theme.ParchmentBg
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg

@Composable
fun CommunityPhotowalksSheet(
    onDismiss: () -> Unit,
    onCreatePostClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val communityPosts by RentalRepository.userCommunityPostsFlow.collectAsStateWithLifecycle()
    val rsvpIds by RentalRepository.rsvpIdsFlow.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(ParchmentBg)
                .clickable(enabled = false) {}
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentYellow)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "COMMUNITY BOARD",
                                    style = LoopType.CaptionTechnical,
                                    color = InkBlack
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Local Meetups & Posts",
                                style = LoopType.H1
                            )
                            Text(
                                text = "Connect with neighbors, collaborate, and join live community gatherings",
                                style = LoopType.BodySmall
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = InkBlack
                            )
                        }
                    }
                }

                if (communityPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(ParchmentWhite)
                                .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
                                .padding(24.dp)
                                .testTag("community_posts_empty_state"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = InkMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "No community posts yet",
                                    style = LoopType.H2,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Meetups, study circles, and help requests shared by members will appear here in real time.",
                                    style = LoopType.BodySmall,
                                    color = InkSecondary,
                                    textAlign = TextAlign.Center
                                )
                                if (onCreatePostClick != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    PrimaryCTA(
                                        text = "Create Community Post",
                                        onClick = {
                                            onDismiss()
                                            onCreatePostClick()
                                        },
                                        testTag = "community_sheet_create_btn"
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(communityPosts, key = { it.id }) { post ->
                        val isRsvped = post.id in rsvpIds

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(ParchmentWhite)
                                .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ParchmentBg)
                                            .border(1.dp, ParchmentBorder, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (post.dateTime.isNotBlank()) {
                                                "${post.type.emoji} ${post.type.title} · ${post.dateTime}"
                                            } else {
                                                "${post.type.emoji} ${post.type.title}"
                                            },
                                            style = LoopType.CaptionTechnical,
                                            color = InkBlack
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isRsvped) StatusGreenBg else InkBlack)
                                            .border(1.dp, if (isRsvped) StatusGreen else InkBlack, RoundedCornerShape(8.dp))
                                            .clickable {
                                                RentalRepository.rsvpToCommunityPost(post.id)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                            .testTag("rsvp_btn_${post.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isRsvped) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = StatusGreen,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = if (isRsvped) "Attending ✓" else "RSVP",
                                                style = LoopType.BodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isRsvped) StatusGreen else Color.White
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = post.title,
                                    style = LoopType.H2
                                )

                                if (post.description.isNotBlank()) {
                                    Text(
                                        text = post.description,
                                        style = LoopType.BodyEditorial
                                    )
                                }

                                val hostLabel = post.authorName.ifBlank { "Community Member" }
                                val locationText = if (post.location.isNotBlank()) {
                                    "${post.location}  ·  Posted by $hostLabel"
                                } else {
                                    "Posted by $hostLabel"
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = InkMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = locationText,
                                        style = LoopType.BodySmall,
                                        color = InkSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
