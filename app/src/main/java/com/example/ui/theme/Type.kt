package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

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

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.6.sp
    )
)

object LoopType {
    val HeadlineLarge = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    )
    val HeadlineMedium = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    )
    val TitleMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )
    val BodyMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val BodySmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 17.sp
    )
    val Metadata = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 15.sp
    )
    val EditorialTag = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        letterSpacing = 1.2.sp
    )
    val HeroDisplayLarge = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 31.sp,
        lineHeight = 38.sp
    )
    val BodyEditorial = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 21.sp
    )
    val PriceHeadline = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    )
    val CaptionTechnical = Metadata
    val H1 = HeadlineLarge
    val H2 = HeadlineMedium
    val H3 = TitleMedium
    val HeroDisplay = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp
    )
    val ButtonLabel = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        lineHeight = 20.sp
    )
    val ButtonSecondaryLabel = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val PriceSmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
    val PriceCard = PriceHeadline
}

val HeroDisplay = LoopType.HeroDisplay
val ButtonLabel = LoopType.ButtonLabel
val ButtonSecondaryLabel = LoopType.ButtonSecondaryLabel
val PriceSmall = LoopType.PriceSmall
val CaptionTechnical = LoopType.CaptionTechnical
val H1 = LoopType.H1
val H2 = LoopType.H2
val H3 = LoopType.H3
val PriceCard = LoopType.PriceCard
