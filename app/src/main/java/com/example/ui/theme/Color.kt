package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Architectural & Editorial Studio Palette (High-Taste P2P Equipment Exchange)

// Canvas & Surfaces
val CanvasGround = Color(0xFFF9F9FB)         // Warm architectural studio off-white
val CanvasWhite = Color(0xFFFFFFFF)          // Pure white surface
val CanvasSubtle = Color(0xFFF3F4F6)         // Secondary subtle surface
val CanvasMuted = Color(0xFFEFEFF2)          // Muted container fill
val CanvasDark = Color(0xFF111315)           // Primary dark ink / CTAs

// Typography & Ink
val InkPrimary = Color(0xFF111315)           // Deep charcoal black ink
val InkSecondary = Color(0xFF4B5563)         // Secondary editorial graphite
val InkMuted = Color(0xFF9CA3AF)             // Technical micro-captions & meta
val InkInverse = Color(0xFFFFFFFF)           // White text on dark

// Structural Lines & Borders
val LineHairline = Color(0xFFE5E7EB)         // Crisp 1px structural hairline
val LineMedium = Color(0xFFD1D5DB)           // Focused or active borders
val LineDark = Color(0xFF111315)             // High-contrast anchor border

// Functional Accents (Strictly restrained to communicate state)
val AccentCobalt = Color(0xFF1D4ED8)         // Active state, links, verified identity
val AccentCobaltSubtle = Color(0xFFEFF6FF)   // Active chip background fill
val StatusLive = Color(0xFF15803D)           // Equipment available now (forest green)
val StatusLiveSubtle = Color(0xFFF0FDF4)     // Live badge background
val RatingAmber = Color(0xFFD97706)          // Editorial rating star
val AlertCrimson = Color(0xFFDC2626)         // Destructive / Favorite active

// Backwards-compatible aliases for existing screen references
val BackgroundLight = CanvasGround
val SurfacePureWhite = CanvasWhite
val SurfaceSecondary = CanvasSubtle
val SurfaceCard = CanvasWhite
val SurfaceDarkPill = CanvasDark
val SurfaceMuted = CanvasMuted

val BrandDark = CanvasDark
val BrandDarkHover = Color(0xFF1F2937)
val BrandBlue = AccentCobalt
val BrandStarAmber = RatingAmber
val BrandGreen = StatusLive
val BrandRed = AlertCrimson

val TextPrimary = InkPrimary
val TextSecondary = InkSecondary
val TextMuted = InkMuted
val TextLight = InkInverse

val BorderSubtle = LineHairline
val BorderDefault = LineHairline
val BorderFocused = LineDark
