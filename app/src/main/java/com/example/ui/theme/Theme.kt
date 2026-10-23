package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GitGreenBright,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF194D25),
    onPrimaryContainer = Color(0xFF98E6A2),
    secondary = GitBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1B3C68),
    onSecondaryContainer = Color(0xFFBFE3FF),
    tertiary = HiveGold,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF5C4300),
    onTertiaryContainer = Color(0xFFFFE082),
    background = GitBackground,
    onBackground = TextPrimary,
    surface = GitSurface,
    onSurface = TextPrimary,
    surfaceVariant = GitSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GitBorder,
    outlineVariant = Color(0xFF21262D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1F883D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2F9D5),
    onPrimaryContainer = Color(0xFF09441B),
    secondary = Color(0xFF0969DA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF4FF),
    onSecondaryContainer = Color(0xFF033D8B),
    tertiary = Color(0xFF9A6700),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF2D4),
    onTertiaryContainer = Color(0xFF4C3000),
    background = Color(0xFFF6F8FA),
    onBackground = Color(0xFF1F2328),
    surface = Color.White,
    onSurface = Color(0xFF1F2328),
    surfaceVariant = Color(0xFFEAEEF2),
    onSurfaceVariant = Color(0xFF656D76),
    outline = Color(0xFFD0D7DE),
    outlineVariant = Color(0xFFE1E4E8)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek GitHub dark mode
    dynamicColor: Boolean = false, // Keep branded GitHub + Osun Hive theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
