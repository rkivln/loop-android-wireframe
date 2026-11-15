package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Static base Material 3 & Editorial tokens (Light)
val LightInkCharcoal = Color(0xFF1E2322)
val LightInkSecondary = Color(0xFF4A5553)
val LightInkMuted = Color(0xFF7A8684)
val LightInkWhite = Color(0xFFFFFFFF)

val LightPaperPureWhite = Color(0xFFFFFFFF)
val LightPaperIvory = Color(0xFFF9F8F5)
val LightPaperWarm = Color(0xFFF4F1EA)
val LightPaperBorder = Color(0xFFE2DDD5)
val LightPaperBorderSubtle = Color(0xFFEFECE5)

val LightAccentForestGreen = Color(0xFF1B4D3E)
val LightAccentMint = Color(0xFFD4EDE4)
val LightAccentPeach = Color(0xFFFFDBCF)
val LightAccentPowderBlue = Color(0xFFD8EEF3)
val LightAccentWarmYellow = Color(0xFFE89800)
val LightAccentYellowSoft = Color(0xFFFFF3D4)
val LightAccentBeigeOat = Color(0xFFEBE6DC)
val LightAccentCobalt = Color(0xFF0284C7)
val LightAccentCobaltSubtle = Color(0xFFE0F2FE)
val LightAlertCrimson = Color(0xFFDC2626)
val LightStatusLive = Color(0xFF16A34A)
val LightAccentYellowDark = Color(0xFFB45309)
val LightHeartRed = Color(0xFFEF4444)

// Static base Material 3 & Editorial tokens (Dark)
val DarkInkCharcoal = Color(0xFFF2F0EB)
val DarkInkSecondary = Color(0xFFB3BEB9)
val DarkInkMuted = Color(0xFF7F8B87)
val DarkInkWhite = Color(0xFF101312)

val DarkPaperPureWhite = Color(0xFF1A201E)
val DarkPaperIvory = Color(0xFF111514)
val DarkPaperWarm = Color(0xFF0B0E0D)
val DarkPaperBorder = Color(0xFF2C3431)
val DarkPaperBorderSubtle = Color(0xFF212826)

val DarkAccentForestGreen = Color(0xFF5EEAD4)
val DarkAccentMint = Color(0xFF17362D)
val DarkAccentPeach = Color(0xFF3B241E)
val DarkAccentPowderBlue = Color(0xFF18313A)
val DarkAccentWarmYellow = Color(0xFFFBBF24)
val DarkAccentYellowSoft = Color(0xFF352B14)
val DarkAccentBeigeOat = Color(0xFF242A28)
val DarkAccentCobalt = Color(0xFF38BDF8)
val DarkAccentCobaltSubtle = Color(0xFF142E3D)
val DarkAlertCrimson = Color(0xFFF87171)
val DarkStatusLive = Color(0xFF4ADE80)
val DarkAccentYellowDark = Color(0xFFFCD34D)
val DarkHeartRed = Color(0xFFF87171)

// Static Material Theme defaults used in ColorSchemes
val PrimaryTeal = Color(0xFF006874)
val OnPrimaryTeal = Color(0xFFFFFFFF)
val PrimaryContainerTeal = Color(0xFF97F0FF)
val OnPrimaryContainerTeal = Color(0xFF001F24)
val SecondaryTerracotta = Color(0xFF984715)
val OnSecondaryTerracotta = Color(0xFFFFFFFF)
val SecondaryContainerTerracotta = Color(0xFFFFDBCF)
val OnSecondaryContainerTerracotta = Color(0xFF381000)
val TertiaryAmber = Color(0xFF7A5900)
val OnTertiaryAmber = Color(0xFFFFFFFF)
val TertiaryContainerAmber = Color(0xFFFFDEA3)
val OnTertiaryContainerAmber = Color(0xFF261900)
val SurfaceVariantLight = Color(0xFFDBE4E6)

val PrimaryTealDark = Color(0xFF5EEAD4)
val OnPrimaryTealDark = Color(0xFF00363D)
val PrimaryContainerTealDark = Color(0xFF17362D)
val OnPrimaryContainerTealDark = Color(0xFFD4EDE4)
val SecondaryTerracottaDark = Color(0xFFFFB59B)
val OnSecondaryTerracottaDark = Color(0xFF5B1B00)
val SecondaryContainerTerracottaDark = Color(0xFF7B3002)
val OnSecondaryContainerTerracottaDark = Color(0xFFFFDBCF)
val SurfaceDark = Color(0xFF111514)
val OnSurfaceDark = Color(0xFFF2F0EB)
val SurfaceVariantDark = Color(0xFF1A201E)
val OnSurfaceVariantDark = Color(0xFFB3BEB9)

data class LoopColors(
    val inkCharcoal: Color,
    val inkSecondary: Color,
    val inkMuted: Color,
    val inkWhite: Color,
    val paperPureWhite: Color,
    val paperIvory: Color,
    val paperWarm: Color,
    val paperBorder: Color,
    val paperBorderSubtle: Color,
    val accentForestGreen: Color,
    val accentMint: Color,
    val accentPeach: Color,
    val accentPowderBlue: Color,
    val accentWarmYellow: Color,
    val accentYellowSoft: Color,
    val accentBeigeOat: Color,
    val accentCobalt: Color,
    val accentCobaltSubtle: Color,
    val alertCrimson: Color,
    val statusLive: Color,
    val accentYellowDark: Color,
    val heartRed: Color,
    val isDark: Boolean
)

val LoopLightPalette = LoopColors(
    inkCharcoal = LightInkCharcoal,
    inkSecondary = LightInkSecondary,
    inkMuted = LightInkMuted,
    inkWhite = LightInkWhite,
    paperPureWhite = LightPaperPureWhite,
    paperIvory = LightPaperIvory,
    paperWarm = LightPaperWarm,
    paperBorder = LightPaperBorder,
    paperBorderSubtle = LightPaperBorderSubtle,
    accentForestGreen = LightAccentForestGreen,
    accentMint = LightAccentMint,
    accentPeach = LightAccentPeach,
    accentPowderBlue = LightAccentPowderBlue,
    accentWarmYellow = LightAccentWarmYellow,
    accentYellowSoft = LightAccentYellowSoft,
    accentBeigeOat = LightAccentBeigeOat,
    accentCobalt = LightAccentCobalt,
    accentCobaltSubtle = LightAccentCobaltSubtle,
    alertCrimson = LightAlertCrimson,
    statusLive = LightStatusLive,
    accentYellowDark = LightAccentYellowDark,
    heartRed = LightHeartRed,
    isDark = false
)

val LoopDarkPalette = LoopColors(
    inkCharcoal = DarkInkCharcoal,
    inkSecondary = DarkInkSecondary,
    inkMuted = DarkInkMuted,
    inkWhite = DarkInkWhite,
    paperPureWhite = DarkPaperPureWhite,
    paperIvory = DarkPaperIvory,
    paperWarm = DarkPaperWarm,
    paperBorder = DarkPaperBorder,
    paperBorderSubtle = DarkPaperBorderSubtle,
    accentForestGreen = DarkAccentForestGreen,
    accentMint = DarkAccentMint,
    accentPeach = DarkAccentPeach,
    accentPowderBlue = DarkAccentPowderBlue,
    accentWarmYellow = DarkAccentWarmYellow,
    accentYellowSoft = DarkAccentYellowSoft,
    accentBeigeOat = DarkAccentBeigeOat,
    accentCobalt = DarkAccentCobalt,
    accentCobaltSubtle = DarkAccentCobaltSubtle,
    alertCrimson = DarkAlertCrimson,
    statusLive = DarkStatusLive,
    accentYellowDark = DarkAccentYellowDark,
    heartRed = DarkHeartRed,
    isDark = true
)

val LocalLoopColors = compositionLocalOf { LoopLightPalette }

// Reactive Semantic Editorial Color Tokens (auto-adapt to Light, Dark, & System Default)
val InkCharcoal: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.inkCharcoal
val InkSecondary: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.inkSecondary
val InkMuted: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.inkMuted
val InkWhite: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.inkWhite

val PaperPureWhite: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.paperPureWhite
val PaperIvory: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.paperIvory
val PaperWarm: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.paperWarm
val PaperBorder: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.paperBorder
val PaperBorderSubtle: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.paperBorderSubtle

val AccentForestGreen: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.accentForestGreen
val AccentMint: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.accentMint
val AccentPeach: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.accentPeach
val AccentPowderBlue: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.accentPowderBlue
val AccentWarmYellow: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.accentWarmYellow
val AccentYellowSoft: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.accentYellowSoft
val AccentBeigeOat: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.accentBeigeOat

val AccentCobalt: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.accentCobalt
val AccentCobaltSubtle: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.accentCobaltSubtle
val AlertCrimson: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.alertCrimson
val StatusLive: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.statusLive
val AccentYellowDark: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.accentYellowDark
val HeartRed: Color @Composable @ReadOnlyComposable get() = LocalLoopColors.current.heartRed

// Compatibility reactive aliases
val AccentYellow: Color @Composable @ReadOnlyComposable get() = AccentWarmYellow
val InkBlack: Color @Composable @ReadOnlyComposable get() = InkCharcoal
val ParchmentBg: Color @Composable @ReadOnlyComposable get() = PaperWarm
val ParchmentBorder: Color @Composable @ReadOnlyComposable get() = PaperBorder
val ParchmentWhite: Color @Composable @ReadOnlyComposable get() = PaperPureWhite
val StatusGreen: Color @Composable @ReadOnlyComposable get() = AccentForestGreen
val StatusGreenBg: Color @Composable @ReadOnlyComposable get() = AccentMint
val BorderDefault: Color @Composable @ReadOnlyComposable get() = PaperBorder
val BorderSubtle: Color @Composable @ReadOnlyComposable get() = PaperBorderSubtle
val CanvasDark: Color @Composable @ReadOnlyComposable get() = InkCharcoal
val CanvasSubtle: Color @Composable @ReadOnlyComposable get() = PaperWarm
val CanvasWhite: Color @Composable @ReadOnlyComposable get() = PaperPureWhite
val InkPrimary: Color @Composable @ReadOnlyComposable get() = InkCharcoal
val LineHairline: Color @Composable @ReadOnlyComposable get() = PaperBorderSubtle
val TextLight: Color @Composable @ReadOnlyComposable get() = PaperPureWhite
val TextMuted: Color @Composable @ReadOnlyComposable get() = InkMuted
val TextPrimary: Color @Composable @ReadOnlyComposable get() = InkCharcoal
val TextSecondary: Color @Composable @ReadOnlyComposable get() = InkSecondary
val BackgroundLight: Color @Composable @ReadOnlyComposable get() = PaperIvory
val BrandBlue: Color @Composable @ReadOnlyComposable get() = AccentCobalt
val BrandDark: Color @Composable @ReadOnlyComposable get() = InkCharcoal
val BrandGreen: Color @Composable @ReadOnlyComposable get() = AccentForestGreen
val SurfaceCard: Color @Composable @ReadOnlyComposable get() = PaperPureWhite
val SurfaceSecondary: Color @Composable @ReadOnlyComposable get() = PaperWarm
val ParchmentSurface: Color @Composable @ReadOnlyComposable get() = PaperIvory
