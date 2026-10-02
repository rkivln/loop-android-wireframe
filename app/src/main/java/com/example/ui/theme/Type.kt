package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * LOOP Unified Editorial Typography System
 * Enforces strict hierarchy across Display, H1, H2, H3, Body, Technical Captions, and Editorial Script.
 */
object LoopType {
    // 1. Display / Poster Scale (Marketing, Onboarding, Hero Mastheads)
    val DisplayPosterHuge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 68.sp,
        lineHeight = 68.sp,
        letterSpacing = (-2.5).sp,
        color = InkBlack
    )

    val DisplayPosterMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.0).sp,
        color = InkBlack
    )

    // 2. Headings Scale
    val H1 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.5).sp,
        color = InkBlack
    )

    val H2 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
        color = InkBlack
    )

    val H3 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = InkBlack
    )

    // 3. Body Scale
    val BodyEditorial = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp,
        color = InkSecondary
    )

    val BodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.1.sp,
        color = InkBlack
    )

    val BodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp,
        color = InkSecondary
    )

    // 4. Technical / Monospace Captions (Metadata, Coordinates, Hardware Specs)
    val CaptionTechnical = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
        color = InkMuted
    )

    val PriceLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.3).sp,
        color = InkBlack
    )

    val PriceCard = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = InkBlack
    )

    // 5. Editorial Vintage Script (Postcards, Hand-lettered Callouts)
    val ScriptEditorial = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        fontSize = 19.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.sp,
        color = InkBlack
    )

    val ScriptHandwritten = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontStyle = FontStyle.Italic,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
        color = InkBlack
    )

    // 6. Interactive Labels
    val ButtonLabel = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp,
        color = InkInverse
    )
}

// Material 3 Typography Mapping
val Typography = Typography(
    displayLarge = LoopType.DisplayPosterHuge,
    displayMedium = LoopType.DisplayPosterMedium,
    displaySmall = LoopType.H1,
    headlineLarge = LoopType.H1,
    headlineMedium = LoopType.H2,
    headlineSmall = LoopType.H3,
    titleLarge = LoopType.H2,
    titleMedium = LoopType.H3,
    titleSmall = LoopType.BodyMedium,
    bodyLarge = LoopType.BodyEditorial,
    bodyMedium = LoopType.BodyMedium,
    bodySmall = LoopType.BodySmall,
    labelLarge = LoopType.ButtonLabel,
    labelMedium = LoopType.CaptionTechnical,
    labelSmall = LoopType.CaptionTechnical
)
