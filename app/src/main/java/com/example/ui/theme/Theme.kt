package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.DarkThemeStyle

data class CalculatorColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val numKey: Color,
    val numKeyText: Color,
    val sciKey: Color,
    val sciKeyText: Color,
    val sciKeyAccent: Color,
    val operatorKey: Color,
    val operatorText: Color,
    val clearKey: Color,
    val clearText: Color,
    val equalsKey: Color,
    val equalsText: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val isDark: Boolean
)

val CyberSlateColors = CalculatorColors(
    background = CyberBg,
    surface = CyberSurface,
    surfaceVariant = CyberSurfaceVariant,
    numKey = CyberNumKey,
    numKeyText = CyberTextPrimary,
    sciKey = CyberSciKey,
    sciKeyText = CyberAccentCyan,
    sciKeyAccent = CyberAccentCyan,
    operatorKey = CyberOperatorKey,
    operatorText = CyberOperatorText,
    clearKey = CyberClearKey,
    clearText = CyberClearKeyText,
    equalsKey = CyberAccentEmerald,
    equalsText = Color(0xFF042F2E),
    textPrimary = CyberTextPrimary,
    textSecondary = CyberTextSecondary,
    border = CyberBorder,
    isDark = true
)

val OledBlackColors = CalculatorColors(
    background = OledBg,
    surface = OledSurface,
    surfaceVariant = Color(0xFF141418),
    numKey = OledNumKey,
    numKeyText = Color(0xFFFFFFFF),
    sciKey = OledSciKey,
    sciKeyText = Color(0xFF67E8F9),
    sciKeyAccent = Color(0xFF67E8F9),
    operatorKey = Color(0xFF1E1E24),
    operatorText = Color(0xFFFBBF24),
    clearKey = Color(0xFF2A1215),
    clearText = Color(0xFFF43F5E),
    equalsKey = Color(0xFF10B981),
    equalsText = Color(0xFF000000),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA1A1AA),
    border = OledBorder,
    isDark = true
)

val MidnightNavyColors = CalculatorColors(
    background = NavyBg,
    surface = NavySurface,
    surfaceVariant = Color(0xFF16203D),
    numKey = NavyNumKey,
    numKeyText = Color(0xFFF1F5F9),
    sciKey = NavySciKey,
    sciKeyText = NavyAccent,
    sciKeyAccent = NavyAccent,
    operatorKey = Color(0xFF1B284F),
    operatorText = Color(0xFF38BDF8),
    clearKey = Color(0xFF32192A),
    clearText = Color(0xFFFB7185),
    equalsKey = Color(0xFF6366F1),
    equalsText = Color(0xFFFFFFFF),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    border = Color(0xFF22315E),
    isDark = true
)

val LightCalcColors = CalculatorColors(
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    numKey = LightNumKey,
    numKeyText = LightTextPrimary,
    sciKey = LightSciKey,
    sciKeyText = Color(0xFF0284C7),
    sciKeyAccent = Color(0xFF0284C7),
    operatorKey = LightOperatorKey,
    operatorText = LightOperatorText,
    clearKey = LightClearKey,
    clearText = LightClearKeyText,
    equalsKey = Color(0xFF059669),
    equalsText = Color(0xFFFFFFFF),
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    border = Color(0xFFE2E8F0),
    isDark = false
)

val LocalCalculatorColors = staticCompositionLocalOf { CyberSlateColors }

object CalculatorTheme {
    val colors: CalculatorColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCalculatorColors.current
}

@Composable
fun SciCalcTheme(
    themeStyle: DarkThemeStyle = DarkThemeStyle.CYBER_DARK,
    content: @Composable () -> Unit
) {
    val customColors = when (themeStyle) {
        DarkThemeStyle.CYBER_DARK -> CyberSlateColors
        DarkThemeStyle.OLED_BLACK -> OledBlackColors
        DarkThemeStyle.MIDNIGHT_NAVY -> MidnightNavyColors
        DarkThemeStyle.LIGHT -> LightCalcColors
    }

    val m3Colors = if (customColors.isDark) {
        darkColorScheme(
            primary = customColors.sciKeyAccent,
            background = customColors.background,
            surface = customColors.surface,
            onBackground = customColors.textPrimary,
            onSurface = customColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = customColors.sciKeyAccent,
            background = customColors.background,
            surface = customColors.surface,
            onBackground = customColors.textPrimary,
            onSurface = customColors.textPrimary
        )
    }

    CompositionLocalProvider(LocalCalculatorColors provides customColors) {
        MaterialTheme(
            colorScheme = m3Colors,
            typography = Typography,
            content = content
        )
    }
}
