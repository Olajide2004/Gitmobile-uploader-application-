package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 1. Hive Gold & GitHub Emerald (Branded Default)
private val HiveGoldDarkScheme = darkColorScheme(
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

private val HiveGoldLightScheme = lightColorScheme(
    primary = Color(0xFF1F883D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2F9D5),
    onPrimaryContainer = Color(0xFF09441B),
    secondary = Color(0xFF0969DA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF4FF),
    onSecondaryContainer = Color(0xFF033D8B),
    tertiary = Color(0xFFB48500),
    onTertiary = Color.Black,
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

// 2. Ocean Blue & Cyan
private val OceanBlueDarkScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF3730A3),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = Color(0xFF2DD4BF),
    onTertiary = Color(0xFF042F2E),
    background = Color(0xFF0B1329),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF111E38),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val OceanBlueLightScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF075985),
    secondary = Color(0xFF4F46E5),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1)
)

// 3. Cyberpunk Violet & Neon Pink
private val CyberPurpleDarkScheme = darkColorScheme(
    primary = Color(0xFFA855F7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF6B21A8),
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = Color(0xFFEC4899),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF831843),
    onSecondaryContainer = Color(0xFFFCE7F3),
    tertiary = Color(0xFF06B6D4),
    onTertiary = Color.Black,
    background = Color(0xFF120B24),
    onBackground = Color(0xFFF5F3FF),
    surface = Color(0xFF1C1338),
    onSurface = Color(0xFFF5F3FF),
    surfaceVariant = Color(0xFF2E1F54),
    onSurfaceVariant = Color(0xFFDDD6FE),
    outline = Color(0xFF4C1D95)
)

private val CyberPurpleLightScheme = lightColorScheme(
    primary = Color(0xFF7E22CE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    secondary = Color(0xFFBE185D),
    onSecondary = Color.White,
    background = Color(0xFFFAF5FF),
    onBackground = Color(0xFF2E1065),
    surface = Color.White,
    onSurface = Color(0xFF2E1065),
    surfaceVariant = Color(0xFFEDE9FE),
    outline = Color(0xFFD8B4FE)
)

// 4. Sunset Amber & Flame Orange
private val SunsetAmberDarkScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),
    onPrimary = Color(0xFF451A03),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFFEF4444),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF7F1D1D),
    onSecondaryContainer = Color(0xFFFEE2E2),
    tertiary = HiveGold,
    onTertiary = Color.Black,
    background = Color(0xFF18100A),
    onBackground = Color(0xFFFFFBEB),
    surface = Color(0xFF24180E),
    onSurface = Color(0xFFFFFBEB),
    surfaceVariant = Color(0xFF332014),
    onSurfaceVariant = Color(0xFFFDE68A),
    outline = Color(0xFF573418)
)

private val SunsetAmberLightScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFFDC2626),
    onSecondary = Color.White,
    background = Color(0xFFFFFDF5),
    onBackground = Color(0xFF451A03),
    surface = Color.White,
    onSurface = Color(0xFF451A03),
    surfaceVariant = Color(0xFFFEF3C7),
    outline = Color(0xFFFCD34D)
)

// 5. High-Contrast AMOLED Pitch Black
private val AmoledDarkScheme = darkColorScheme(
    primary = Color(0xFF00FF88),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D25),
    onPrimaryContainer = Color(0xFF80FFC4),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color.Black,
    tertiary = HiveGold,
    onTertiary = Color.Black,
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0A0A0A),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF151515),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF2A2A2A),
    outlineVariant = Color(0xFF1A1A1A)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "DARK",
    colorPreset: String = "HIVE_GOLD",
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode.uppercase()) {
        "LIGHT" -> false
        "SYSTEM" -> systemInDark
        else -> true // "DARK" by default
    }

    val context = LocalContext.current
    val colorScheme: ColorScheme = when (colorPreset.uppercase()) {
        "DYNAMIC" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) HiveGoldDarkScheme else HiveGoldLightScheme
            }
        }
        "OCEAN_BLUE" -> if (isDark) OceanBlueDarkScheme else OceanBlueLightScheme
        "CYBER_PURPLE" -> if (isDark) CyberPurpleDarkScheme else CyberPurpleLightScheme
        "SUNSET_AMBER" -> if (isDark) SunsetAmberDarkScheme else SunsetAmberLightScheme
        "AMOLED_BLACK" -> if (isDark) AmoledDarkScheme else HiveGoldLightScheme
        else -> if (isDark) HiveGoldDarkScheme else HiveGoldLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
