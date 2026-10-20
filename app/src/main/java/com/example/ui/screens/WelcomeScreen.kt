package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BodyCopy
import com.example.ui.components.EditorialHeadline
import com.example.ui.components.PaginationDots
import com.example.ui.components.PhotoCollage
import com.example.ui.components.PrimaryCTA
import com.example.ui.components.SecondaryCTA
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperWarm
import kotlinx.coroutines.launch

data class OnboardingSlide(
    val heroImageRes: Int,
    val secondaryImageRes: Int,
    val editionBadge: String,
    val headline: String,
    val description: String,
    val rotation: Float
)

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit = onGetStarted,
    modifier: Modifier = Modifier
) {
    val slides = remember {
        listOf(
            OnboardingSlide(
                heroImageRes = R.drawable.img_editorial_lifestyle_1_1791087263359,
                secondaryImageRes = R.drawable.img_editorial_studio_2_1791087277042,
                editionBadge = "ISSUE 01 — OBJECTS OF INTENT",
                headline = "Curated objects for\ntemporary living",
                description = "Access high-end cameras, analog synths, and design pieces without the burden of ownership.",
                rotation = -4f
            ),
            OnboardingSlide(
                heroImageRes = R.drawable.img_editorial_outdoor_3_1791087293034,
                secondaryImageRes = R.drawable.canon_eos_camera_1790477967758,
                editionBadge = "ISSUE 02 — URBAN MOBILITY",
                headline = "Travel light,\ncreate boldly",
                description = "Pick up premium e-bikes and gear from local creators in your neighborhood within minutes.",
                rotation = 3.5f
            ),
            OnboardingSlide(
                heroImageRes = R.drawable.welcome_hero_photographer_1790872123419,
                secondaryImageRes = R.drawable.modern_armchair_1790478015816,
                editionBadge = "ISSUE 03 — TRUSTED MAKERS",
                headline = "A neighborhood\nof trusted makers",
                description = "Connect with local photographers, artists, and hosts sharing items with full transparency.",
                rotation = -3f
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperWarm)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Utility Bar: "Skip" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage < slides.size - 1) {
                    Text(
                        text = "Skip",
                        style = LoopType.BodyMedium,
                        color = InkSecondary,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onGetStarted
                            )
                            .padding(8.dp)
                            .testTag("onboarding_skip_btn")
                    )
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Center Horizontal Pager: Photo Collage & Editorial Copy
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                val slide = slides[pageIndex]

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Center Hero Photo Collage with overlap and tactile moodboard feel
                    PhotoCollage(
                        heroImageRes = slide.heroImageRes,
                        secondaryImageRes = slide.secondaryImageRes,
                        badgeText = slide.editionBadge,
                        secondaryRotation = slide.rotation
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Strong Editorial Playfair Headline
                    EditorialHeadline(
                        text = slide.headline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Concise Supporting Body Copy
                    BodyCopy(
                        text = slide.description,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                    )
                }
            }

            // Bottom Zone: Pagination Indicator & Primary Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Refined Tiny Pagination Dots
                PaginationDots(
                    totalPages = slides.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Primary Action Button
                val isLastPage = pagerState.currentPage == slides.size - 1
                PrimaryCTA(
                    text = if (isLastPage) "Explore the Catalog" else "Continue",
                    onClick = {
                        if (isLastPage) {
                            onGetStarted()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    testTag = "onboarding_continue_btn"
                )

                if (isLastPage) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Already have an account? Sign In",
                        style = LoopType.Metadata.copy(fontSize = 12.sp),
                        color = InkSecondary,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onLogin
                            )
                            .padding(vertical = 6.dp)
                            .testTag("onboarding_login_btn")
                    )
                }
            }
        }
    }
}
