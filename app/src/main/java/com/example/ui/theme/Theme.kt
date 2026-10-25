package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GliseoPrimary,
    onPrimary = GliseoOnPrimary,
    primaryContainer = GliseoPrimaryContainer,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = GliseoSecondary,
    onSecondary = GliseoOnSecondary,
    secondaryContainer = GliseoSecondaryContainer,
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = GliseoTertiary,
    onTertiary = Color.Black,
    tertiaryContainer = GliseoTertiaryContainer,
    background = GliseoBackgroundDark,
    onBackground = Color(0xFFF1F5F9),
    surface = GliseoSurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = GliseoSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = GliseoOutlineDark,
    error = GliseoError,
    onError = Color.White,
    errorContainer = GliseoErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = GliseoPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = GliseoSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = GliseoTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    background = GliseoBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = GliseoSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = GliseoSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = GliseoOutlineLight,
    error = GliseoError,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek executive dark mode for dashboard
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
