package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Deep Space Void Backgrounds
val VoidBlack = Color(0xFF07080C)
val SurfaceDark = Color(0xFF0F1118)
val SurfaceElevated = Color(0xFF161924)
val SurfaceCard = Color(0xFF1A1D2B)
val SurfaceGlass = Color(0x99181B28)
val SurfaceGlassHigh = Color(0xCC202436)

// Neon & Gradient Accents
val NeonBlue = Color(0xFF388BFF)
val ElectricCyan = Color(0xFF00F0FF)
val VividPurple = Color(0xFF8B5CF6)
val DeepViolet = Color(0xFF6366F1)
val NeonMagenta = Color(0xFFD946EF)
val WarmSunset = Color(0xFFFF5E62)
val SunsetOrange = Color(0xFFFF9966)
val SoftPink = Color(0xFFEC4899)
val EmeraldGreen = Color(0xFF10B981)

// Borders & Strokes
val GlassBorder = Color(0x2EFFFFFF)
val GlassBorderSubtle = Color(0x1AFFFFFF)
val GlassBorderHighlight = Color(0x55FFFFFF)

// Text Colors
val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)
val TextDisabled = Color(0xFF4B5563)

// Signature Loop Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899))
)

val GlowGradient = Brush.radialGradient(
    colors = listOf(Color(0x668B5CF6), Color(0x333B82F6), Color(0x00000000))
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(Color(0x308B5CF6), Color(0x103B82F6), Color(0x0507080C))
)

val OrbGradient1 = Brush.radialGradient(
    colors = listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B), Color(0x00FEB47B))
)

val OrbGradient2 = Brush.radialGradient(
    colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC), Color(0x002575FC))
)

val ButtonCtaGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFD946EF))
)

val WarmOrbGradient = Brush.radialGradient(
    colors = listOf(Color(0xFFFF5E62), Color(0xFFFF9966), Color(0x00000000))
)
