package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Google Fonts installed locally via font-util
val PlayfairFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.Medium),
    Font(R.font.playfair_display, FontWeight.SemiBold),
    Font(R.font.playfair_display, FontWeight.Bold)
)

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

/**
 * Editorial Lifestyle Typography Hierarchy
 * Restrained, elegant serif headlines paired with clean contemporary sans-serif body.
 */
object LoopType {

    // 1. Hero & Masthead Display (Playfair Serif)
    val HeroDisplay = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp,
        color = InkCharcoal
    )

    val HeroDisplayLarge = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.8).sp,
        color = InkCharcoal
    )

    // 2. Headlines
    val HeadlineLarge = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp,
        color = InkCharcoal
    )

    val HeadlineMedium = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
        color = InkCharcoal
    )

    val Subtitle = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = InkCharcoal
    )

    // 3. Body Text (Plus Jakarta Sans)
    val BodyEditorial = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
        color = InkSecondary
    )

    val BodyMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = InkCharcoal
    )

    val BodySmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        color = InkSecondary
    )

    // 4. Metadata & Editorial Badges
    val Metadata = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
        color = InkMuted
    )

    val EditorialTag = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp,
        color = InkCharcoal
    )

    // 5. Button Text
    val ButtonLabel = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
        color = InkWhite
    )

    val ButtonSecondaryLabel = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
        color = InkCharcoal
    )

    // 6. Price & Numeric Callouts
    val PriceHeadline = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.4).sp,
        color = InkCharcoal
    )

    val PriceSmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.2).sp,
        color = InkCharcoal
    )

    // 7. Script / Italic Accent
    val EditorialScript = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = InkSecondary
    )

    // Legacy compatibility constants
    val DisplayPosterHuge = HeroDisplayLarge
    val DisplayPosterMedium = HeroDisplay
    val H1 = HeadlineLarge
    val H2 = HeadlineMedium
    val H3 = Subtitle
    val CaptionTechnical = Metadata
    val PriceLarge = PriceHeadline
    val PriceCard = PriceSmall
    val ScriptEditorial = EditorialScript
    val ScriptHandwritten = EditorialScript
}

// Material 3 Typography integration
val Typography = Typography(
    displayLarge = LoopType.HeroDisplayLarge,
    displayMedium = LoopType.HeroDisplay,
    displaySmall = LoopType.HeadlineLarge,
    headlineLarge = LoopType.HeadlineLarge,
    headlineMedium = LoopType.HeadlineMedium,
    headlineSmall = LoopType.Subtitle,
    titleLarge = LoopType.HeadlineMedium,
    titleMedium = LoopType.Subtitle,
    titleSmall = LoopType.BodyMedium,
    bodyLarge = LoopType.BodyEditorial,
    bodyMedium = LoopType.BodyMedium,
    bodySmall = LoopType.BodySmall,
    labelLarge = LoopType.ButtonLabel,
    labelMedium = LoopType.Metadata,
    labelSmall = LoopType.Metadata
)
