package com.example.ui.auth

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PaginationDots
import com.example.ui.components.PhotoCollage
import com.example.ui.components.PrimaryCTA
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperIvory
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val issueBadge: String,
    val title: String,
    val subtitle: String,
    @DrawableRes val heroRes: Int,
    @DrawableRes val secondaryRes: Int,
    val rotation: Float
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = remember {
        listOf(
            OnboardingSlide(
                issueBadge = "ISSUE 01 — OBJECTS OF INTENT",
                title = "Curated objects for\ntemporary living",
                subtitle = "Access high-end cameras, analog synths, and design pieces without the burden of ownership.",
                heroRes = R.drawable.img_welcome_hero,
                secondaryRes = R.drawable.img_synth,
                rotation = -4.5f
            ),
            OnboardingSlide(
                issueBadge = "ISSUE 02 — URBAN MOBILITY",
                title = "Travel light,\ncreate boldly",
                subtitle = "Pick up premium e-bikes and gear from local creators in your neighborhood within minutes.",
                heroRes = R.drawable.img_ebike,
                secondaryRes = R.drawable.img_synth,
                rotation = 4.0f
            ),
            OnboardingSlide(
                issueBadge = "ISSUE 03 — TRUSTED MAKERS",
                title = "A neighborhood\nof trusted makers",
                subtitle = "Connect with local photographers, artists, and hosts sharing items with full transparency.",
                heroRes = R.drawable.img_street,
                secondaryRes = R.drawable.img_cafe,
                rotation = -3.5f
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLastSlide = currentPage == slides.lastIndex

    Surface(
        modifier = modifier.fillMaxSize(),
        color = PaperIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLastSlide) {
                    Text(
                        text = "Skip",
                        style = LoopType.BodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = InkSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onGetStarted
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("welcome_skip_button")
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val slide = slides[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PhotoCollage(
                        heroImageRes = slide.heroRes,
                        secondaryImageRes = slide.secondaryRes,
                        badgeText = slide.issueBadge,
                        secondaryRotation = slide.rotation
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = slide.title,
                        style = LoopType.HeroDisplayLarge.copy(
                            fontSize = 31.sp,
                            lineHeight = 37.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = InkCharcoal,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = slide.subtitle,
                        style = LoopType.BodyEditorial.copy(fontSize = 14.5.sp, lineHeight = 21.sp),
                        color = InkSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            PaginationDots(
                totalPages = slides.size,
                currentPage = currentPage,
                activeColor = InkCharcoal
            )

            Spacer(modifier = Modifier.height(22.dp))

            PrimaryCTA(
                text = if (isLastSlide) "Explore the Catalog" else "Continue",
                onClick = {
                    if (isLastSlide) {
                        onGetStarted()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp),
                testTag = "welcome_get_started_button"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .clickable(
                        enabled = isLastSlide,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onGetStarted
                    )
                    .testTag("welcome_sign_in_link"),
                contentAlignment = Alignment.Center
            ) {
                if (isLastSlide) {
                    Text(
                        text = "Already have an account? Sign In",
                        style = LoopType.BodySmall.copy(fontWeight = FontWeight.Medium),
                        color = InkMuted
                    )
                }
            }
        }
    }
}
