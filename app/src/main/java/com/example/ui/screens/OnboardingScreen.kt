package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AtmosphericBackground
import com.example.ui.components.AvatarBadge
import com.example.ui.components.GlowingCircularArrowButton
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple
import com.example.ui.theme.WarmSunset

data class OnboardingStep(
    val titlePrefix: String,
    val highlightedWord: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = remember {
        listOf(
            OnboardingStep(
                titlePrefix = "Find people\nwho share\nyour ",
                highlightedWord = "interests.",
                description = "Study, events, help or just\nhangout — all nearby."
            ),
            OnboardingStep(
                titlePrefix = "Discover\nlocal events &\ncreative ",
                highlightedWord = "workshops.",
                description = "Join design sessions, tech meetups,\nand beach sketching nearby."
            ),
            OnboardingStep(
                titlePrefix = "Learn and\ncollaborate\nin study ",
                highlightedWord = "groups.",
                description = "Connect with peers, share notes,\nand master tough subjects together."
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]

    AtmosphericBackground(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "Skip",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            // Visual: Floating Circular Cosmic Network
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background radial ring glow
                Canvas(modifier = Modifier.size(280.dp)) {
                    val center = Offset(size.width * 0.5f, size.height * 0.5f)
                    val r = size.width * 0.42f

                    // Soft ambient gradient glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VividPurple.copy(alpha = 0.35f),
                                WarmSunset.copy(alpha = 0.20f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = r * 1.3f
                        ),
                        radius = r * 1.3f,
                        center = center
                    )

                    // Concentric vector connection orbits
                    drawCircle(
                        color = Color(0x338B5CF6),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                    drawCircle(
                        color = Color(0x22388BFF),
                        radius = r * 0.65f,
                        center = center,
                        style = Stroke(width = 1f)
                    )

                    // Connection lines between nodes
                    drawLine(
                        color = Color(0x33FFFFFF),
                        start = Offset(center.x - r * 0.6f, center.y - r * 0.2f),
                        end = Offset(center.x + r * 0.5f, center.y - r * 0.5f),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = Color(0x33FFFFFF),
                        start = Offset(center.x + r * 0.5f, center.y - r * 0.5f),
                        end = Offset(center.x + r * 0.55f, center.y + r * 0.45f),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = Color(0x33FFFFFF),
                        start = Offset(center.x - r * 0.6f, center.y - r * 0.2f),
                        end = Offset(center.x - r * 0.4f, center.y + r * 0.5f),
                        strokeWidth = 1.5f
                    )
                }

                // Node: Top Center (Avatar)
                AvatarBadge(
                    initials = "AK",
                    size = 40.dp,
                    colorIndex = 0,
                    modifier = Modifier.offset(x = 10.dp, y = (-85).dp)
                )

                // Node: Top Right (Study Icon badge)
                Box(
                    modifier = Modifier
                        .offset(x = 90.dp, y = (-55).dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A284D), CircleShape)
                        .border(1.dp, Color(0x668B5CF6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFFC4B5FD),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Node: Center Left (Avatar)
                AvatarBadge(
                    initials = "SR",
                    size = 46.dp,
                    colorIndex = 1,
                    modifier = Modifier.offset(x = (-95).dp, y = (-20).dp)
                )

                // Node: Center (Book badge)
                Box(
                    modifier = Modifier
                        .offset(x = (-25).dp, y = (-30).dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E2746), CircleShape)
                        .border(1.dp, Color(0x55388BFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF93C5FD),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Node: Center Right (Avatar)
                AvatarBadge(
                    initials = "PS",
                    size = 40.dp,
                    colorIndex = 4,
                    modifier = Modifier.offset(x = (-20).dp, y = 35.dp)
                )

                // Node: Bottom Left (Help/Group badge)
                Box(
                    modifier = Modifier
                        .offset(x = (-70).dp, y = 65.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B323F), CircleShape)
                        .border(1.dp, Color(0x5510B981), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = Color(0xFF6EE7B7),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Node: Bottom Right (Event badge)
                Box(
                    modifier = Modifier
                        .offset(x = 80.dp, y = 55.dp)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3F2134), CircleShape)
                        .border(1.dp, Color(0x66EC4899), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFFF472B6),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Text Content with subtle step transitions
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                label = "onboarding_text"
            ) { step ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    val annotatedText = buildAnnotatedString {
                        append(step.titlePrefix)
                        withStyle(
                            style = SpanStyle(
                                color = VividPurple,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(step.highlightedWord)
                        }
                    }

                    Text(
                        text = annotatedText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 38.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = step.description,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp,
                        color = TextSecondary
                    )
                }
            }

            // Bottom Pagination Dots & Circular Arrow Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 5 Dots indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 5) {
                        val isActive = i == currentStepIndex
                        val width = if (isActive) 20.dp else 6.dp
                        val color = if (isActive) NeonBlue else Color(0x448B5CF6)

                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(width)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                        )
                    }
                }

                GlowingCircularArrowButton(
                    onClick = {
                        if (currentStepIndex < steps.size - 1) {
                            currentStepIndex++
                        } else {
                            onFinish()
                        }
                    },
                    size = 58.dp,
                    testTag = "onboarding_next_button"
                )
            }
        }
    }
}
