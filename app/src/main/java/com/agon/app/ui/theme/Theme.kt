package com.agon.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Navy900,
    onPrimary = Color.White,
    primaryContainer = Navy50,
    onPrimaryContainer = Navy900,
    secondary = Amber600,
    onSecondary = Color.White,
    secondaryContainer = Amber100,
    onSecondaryContainer = Color(0xFF7C4A03),
    tertiary = Teal500,
    onTertiary = Color.White,
    tertiaryContainer = Teal50,
    onTertiaryContainer = Navy900,
    background = BgLight,
    onBackground = Ink900,
    surface = SurfaceLight,
    onSurface = Ink900,
    surfaceVariant = Color(0xFFEEF1F6),
    onSurfaceVariant = Ink600,
    outline = LineLight,
    outlineVariant = Color(0xFFF2F4F7),
    error = Color(0xFFD92D20),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF9FAFB),
    surfaceContainerHigh = Color(0xFFEFF2F6)
)

private val DarkScheme = darkColorScheme(
    primary = AmberDark,
    onPrimary = Color(0xFF2A1A00),
    primaryContainer = Color(0xFF3A2E12),
    onPrimaryContainer = Amber100,
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF06283A),
    secondaryContainer = Color(0xFF12384E),
    onSecondaryContainer = Color(0xFFD6EFFC),
    tertiary = Color(0xFF2DD4BF),
    background = BgDark,
    onBackground = Color(0xFFF2F4F7),
    surface = SurfaceDark,
    onSurface = Color(0xFFF2F4F7),
    surfaceVariant = SurfaceDark2,
    onSurfaceVariant = Color(0xFFB8C3D4),
    outline = LineDark,
    outlineVariant = Color(0xFF1B2C44),
    error = Color(0xFFF97066),
    surfaceContainer = SurfaceDark,
    surfaceContainerLow = Color(0xFF0E1728),
    surfaceContainerHigh = SurfaceDark2
)

@Composable
fun AgonAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}
