package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// LOOP Editorial Paper / Vintage Creator Aesthetic Palette (from design mockups)

val ParchmentBg = Color(0xFFF4F0E8)         // Warm newsprint / paper textured background
val ParchmentSurface = Color(0xFFFAF8F3)    // Clean card surface
val ParchmentWhite = Color(0xFFFFFFFF)      // Pure white card element
val ParchmentBorder = Color(0xFFE6E1D8)     // Subtle paper edge border

val AccentYellow = Color(0xFFFFD54F)        // Signature highlighter yellow (Near You, Camera card, CTA)
val AccentYellowDark = Color(0xFFF59E0B)    // Amber / Star rating
val AccentYellowSoft = Color(0xFFFEF3C7)    // Soft yellow pill fill

val InkBlack = Color(0xFF111215)            // Primary bold poster typography
val InkPrimary = InkBlack
val InkSecondary = Color(0xFF4B4F58)        // Subtitles and editorial captions
val InkMuted = Color(0xFF8A909D)            // Light metadata & borders
val InkInverse = Color(0xFFFFFFFF)          // White text

val StatusGreen = Color(0xFF16A34A)         // "Available" badge green
val StatusGreenBg = Color(0xFFDCFCE7)       // "Available" badge background

val HeartRed = Color(0xFFEF4444)            // Favorite heart accent

// Backwards-compatible aliases
val CanvasGround = ParchmentBg
val CanvasWhite = ParchmentWhite
val CanvasSubtle = ParchmentSurface
val CanvasDark = InkBlack
val BackgroundLight = ParchmentBg
val SurfacePureWhite = ParchmentWhite
val SurfaceSecondary = ParchmentSurface
val SurfaceCard = ParchmentWhite
val SurfaceDarkPill = InkBlack
val SurfaceMuted = ParchmentBorder

val BrandDark = InkBlack
val BrandDarkHover = Color(0xFF262626)
val BrandBlue = Color(0xFF2563EB)
val BrandStarAmber = AccentYellowDark
val BrandGreen = StatusGreen
val BrandRed = HeartRed

val TextPrimary = InkBlack
val TextSecondary = InkSecondary
val TextMuted = InkMuted
val TextLight = InkInverse

val BorderSubtle = ParchmentBorder
val BorderDefault = ParchmentBorder
val BorderFocused = InkBlack

val LineHairline = ParchmentBorder
val LineMedium = Color(0xFFD4CEBF)
val LineDark = InkBlack
val AccentCobalt = Color(0xFF2563EB)
val AccentCobaltSubtle = Color(0xFFEFF6FF)
val StatusLive = StatusGreen
val StatusLiveSubtle = StatusGreenBg
val RatingAmber = AccentYellowDark
val AlertCrimson = HeartRed
