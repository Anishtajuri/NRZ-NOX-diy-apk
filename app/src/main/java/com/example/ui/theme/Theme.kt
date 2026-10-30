package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.localization.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = Color.White,
    secondary = VioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = VioletDark,
    onSecondaryContainer = Color.White,
    tertiary = AmberAccent,
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = SlateSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    error = RoseDecline,
    onError = Color.White
)

private val WhiteColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = Color.Black,
    secondary = VioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = VioletLight,
    onSecondaryContainer = Color.Black,
    tertiary = AmberAccent,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    error = RoseDecline,
    onError = Color.White
)

private val NormalColorScheme = darkColorScheme(
    primary = Color(0xFF14B8A6), // Teal/Emerald
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0F766E),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF6366F1), // Indigo
    onSecondary = Color.White,
    tertiary = AmberAccent,
    background = Color(0xFF1E293B), // Slate 800
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF334155), // Slate 700
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF475569),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = RoseDecline,
    onError = Color.White
)

@Composable
fun LiveSnapTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.WHITE -> WhiteColorScheme
        AppThemeMode.NORMAL -> NormalColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
