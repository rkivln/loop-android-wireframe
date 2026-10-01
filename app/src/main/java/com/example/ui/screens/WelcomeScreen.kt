package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.ParchmentBg

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit = onGetStarted,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ParchmentBg)
    ) {
        // Right-aligned Photographic Collage of Puducherry Lighthouse & Street Photographer
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.68f)
                .align(Alignment.CenterEnd)
        ) {
            Image(
                painter = painterResource(id = R.drawable.welcome_hero_photographer_1790872123419),
                contentDescription = "Puducherry Photographer",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Torn / Deckled paper gradient blend on left edge of the photo
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.45f)
                    .align(Alignment.CenterStart)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                ParchmentBg,
                                ParchmentBg.copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Top fade blend
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ParchmentBg,
                                Color.Transparent
                            )
                        )
                    )
            )

            // Bottom fade blend
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ParchmentBg.copy(alpha = 0.9f),
                                ParchmentBg
                            )
                        )
                    )
            )
        }

        // Foreground Editorial Content Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: LOOP Logo (left) | RENT CREATE EXPLORE (right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "LOOP",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    color = InkBlack,
                    fontFamily = FontFamily.SansSerif
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RENT",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = InkBlack
                    )
                    Text(
                        text = "CREATE",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = InkBlack
                    )
                    Text(
                        text = "EXPLORE",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = InkBlack
                    )
                }
            }

            // Main Poster Headline with Yellow Marker Highlight on "NEAR"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                // "GEAR"
                Text(
                    text = "GEAR",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2.5).sp,
                    lineHeight = 68.sp,
                    color = InkBlack
                )

                // "NEAR" with yellow highlighter brush background
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                ) {
                    // Yellow highlight marker box
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(horizontal = 2.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentYellow)
                    )

                    Text(
                        text = "NEAR",
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-2.5).sp,
                        lineHeight = 68.sp,
                        color = InkBlack,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // "YOU"
                Text(
                    text = "YOU",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2.5).sp,
                    lineHeight = 68.sp,
                    color = InkBlack
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Hand-script editorial lines: "Same City Bigger Possibilities" with arrows
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = "Same City",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            color = InkBlack
                        )
                        Text(
                            text = "Bigger Possibilities",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            color = InkBlack
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "↗",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = "↕",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                    }
                }
            }

            // Bottom Section: Categories List (Left) | Yellow Action Button (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "CAMERAS",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = InkBlack
                    )
                    Text(
                        text = "LAPTOPS",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = InkBlack
                    )
                    Text(
                        text = "VEHICLES",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = InkBlack
                    )
                    Text(
                        text = "& MORE",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = InkBlack
                    )
                }

                // Vibrant Yellow Circle Button with Black Arrow
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(AccentYellow)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.Black.copy(alpha = 0.2f)),
                            onClick = onGetStarted
                        )
                        .testTag("welcome_get_started_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Get Started",
                        tint = InkBlack,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
