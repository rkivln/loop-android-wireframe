package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Clean, disciplined Dark Theme Surface Palette (Apple / Linear inspired)
val VoidBlack = Color(0xFF090A0F)
val SurfaceDark = Color(0xFF11131A)
val SurfaceElevated = Color(0xFF181B24)
val SurfaceCard = Color(0xFF161922)
val SurfaceInteractive = Color(0xFF1F2330)
val SurfaceGlass = Color(0xF2141722)
val SurfaceGlassHigh = Color(0xF81A1D2A)

// Restrained Brand & Semantic Accents
val BrandPrimary = Color(0xFF4F46E5)      // Modern Indigo
val BrandPrimaryLight = Color(0xFF6366F1) // Soft Indigo
val BrandSecondary = Color(0xFF3B82F6)    // Clean Electric Blue
val BrandAccent = Color(0xFF8B5CF6)       // Violet highlight

// Backward compatible aliases
val VividPurple = Color(0xFF6366F1)
val NeonBlue = Color(0xFF3B82F6)
val ElectricCyan = Color(0xFF06B6D4)
val NeonMagenta = Color(0xFF8B5CF6)
val WarmSunset = Color(0xFFF97316)
val SunsetOrange = Color(0xFFFB923C)
val SoftPink = Color(0xFFEC4899)
val EmeraldGreen = Color(0xFF10B981)

// Semantic status colors
val StatusSuccess = Color(0xFF10B981)
val StatusWarning = Color(0xFFF59E0B)
val StatusError = Color(0xFFEF4444)
val StatusInfo = Color(0xFF3B82F6)

// Precise, low-noise Borders
val BorderSubtle = Color(0x18FFFFFF)      // ~10% white for clean card contours
val BorderDefault = Color(0x28FFFFFF)     // ~16% white for inputs and active controls
val BorderStrong = Color(0x40FFFFFF)      // ~25% white for focused/selected items
val GlassBorder = BorderDefault
val GlassBorderSubtle = BorderSubtle
val GlassBorderHighlight = BorderStrong

// Text Hierarchy
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val TextDisabled = Color(0xFF475569)

// Controlled, elegant subtle gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF4F46E5), Color(0xFF3B82F6))
)

val ButtonCtaGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF4F46E5), Color(0xFF3B82F6))
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(Color(0x144F46E5), Color(0x083B82F6), Color(0x00000000))
)

val HeroSurfaceGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A1D28), Color(0xFF12141C))
)
