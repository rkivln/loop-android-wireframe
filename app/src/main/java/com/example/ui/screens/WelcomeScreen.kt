package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AtmosphericBackground
import com.example.ui.components.GlowOrb
import com.example.ui.components.GlowingCircularArrowButton
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    AtmosphericBackground(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top branding & header
            Column(modifier = Modifier.padding(top = 28.dp)) {
                Text(
                    text = "loop",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.5).sp,
                    fontFamily = FontFamily.SansSerif,
                    color = TextPrimary,
                    modifier = Modifier.testTag("welcome_logo_text")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "People near you.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
                Text(
                    text = "Real connections.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }

            // Center large glowing 3D-like orb visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                GlowOrb(size = 260.dp)
            }

            // Bottom section with ethos text & glowing arrow action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Grow.\nLearn.\nShare.\nTogether.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp,
                        color = TextMuted
                    )
                }

                GlowingCircularArrowButton(
                    onClick = onContinue,
                    size = 58.dp,
                    testTag = "welcome_start_button"
                )
            }
        }
    }
}
