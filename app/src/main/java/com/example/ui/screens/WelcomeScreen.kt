package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.DarkPillButton
import com.example.ui.components.LoopLogoText
import com.example.ui.components.OutlinedPillButton
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Fluid 3D Ribbon Background Art (Positioned in lower-middle half)
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.loop_welcome_ribbon_1790477952152),
                contentDescription = "Loop Fluid Artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top gradient overlay to keep text ultra crisp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color.White.copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Bottom gradient overlay for buttons
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.92f),
                                Color.White
                            )
                        )
                    )
            )
        }

        // Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Tagline Header
            Column(modifier = Modifier.padding(top = 28.dp)) {
                LoopLogoText(fontSize = 38, showSubtext = true)

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Everything\nyou need,\nonly when you need it.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "A smarter way to rent from\npeople around you.",
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = TextSecondary
                )
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                DarkPillButton(
                    text = "Get Started",
                    onClick = onGetStarted,
                    height = 54.dp,
                    showArrow = true,
                    testTag = "welcome_get_started_btn"
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedPillButton(
                    text = "I already have an account",
                    onClick = onLogin,
                    height = 54.dp,
                    testTag = "welcome_login_btn"
                )
            }
        }
    }
}
