package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperPureWhite
import com.example.ui.theme.PaperWarm

@Composable
private fun rememberSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "skeleton_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    return alpha
}

@Composable
fun HeroSkeletonCard(modifier: Modifier = Modifier) {
    val alpha = rememberSkeletonAlpha()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(276.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(PaperWarm.copy(alpha = alpha))
            .border(1.dp, PaperBorderSubtle, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .testTag("hero_skeleton_card")
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(128.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(PaperPureWhite.copy(alpha = 0.7f))
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PaperPureWhite.copy(alpha = 0.75f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PaperPureWhite.copy(alpha = 0.65f))
            )
        }
    }
}

@Composable
fun CatalogSkeletonFeed(
    count: Int = 3,
    modifier: Modifier = Modifier
) {
    val alpha = rememberSkeletonAlpha()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("catalog_skeleton_feed"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(count) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(PaperWarm.copy(alpha = alpha))
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PaperWarm.copy(alpha = alpha))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PaperWarm.copy(alpha = alpha * 0.85f))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PaperWarm.copy(alpha = alpha * 0.85f))
                        )
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PaperWarm.copy(alpha = alpha))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessagesSkeletonList(
    count: Int = 4,
    modifier: Modifier = Modifier
) {
    val alpha = rememberSkeletonAlpha()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("messages_skeleton_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PaperWarm.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PaperWarm.copy(alpha = alpha))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(13.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PaperWarm.copy(alpha = alpha * 0.8f))
                    )
                }
            }
        }
    }
}

@Composable
fun BookingsSkeletonList(
    count: Int = 3,
    modifier: Modifier = Modifier
) {
    val alpha = rememberSkeletonAlpha()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bookings_skeleton_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PaperWarm.copy(alpha = alpha * 0.55f))
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PaperPureWhite.copy(alpha = 0.85f))
                    )
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(PaperPureWhite.copy(alpha = 0.85f))
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PaperPureWhite.copy(alpha = 0.7f))
                )
            }
        }
    }
}

@Composable
fun ProfileOverviewSkeleton(modifier: Modifier = Modifier) {
    val alpha = rememberSkeletonAlpha()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .testTag("profile_overview_skeleton"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(PaperWarm.copy(alpha = alpha))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(74.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PaperWarm.copy(alpha = alpha))
                )
            }
        }
    }
}
