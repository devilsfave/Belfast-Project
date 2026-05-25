package com.example.medgem.ui.theme

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
    primary = MedGemPrimary, // Keep primary distinct in dark mode, or adjust brightness if needed
    onPrimary = MedGemOnPrimary,
    primaryContainer = MedGemPrimaryContainer, // Maybe darken container for true dark mode
    onPrimaryContainer = MedGemOnPrimaryContainer,
    secondary = MedGemSecondary,
    onSecondary = MedGemOnSecondary,
    secondaryContainer = MedGemSecondaryContainer,
    onSecondaryContainer = MedGemOnSecondaryContainer,
    tertiary = MedGemTertiary,
    onTertiary = MedGemOnTertiary,
    background = MedGemBackgroundDark,
    surface = MedGemSurfaceDark,
    surfaceVariant = MedGemSurfaceVariantDark,
    onSurface = Color(0xFFE8EAED),
    onSurfaceVariant = Color(0xFFC4C7C5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = MedGemPrimary,
    onPrimary = MedGemOnPrimary,
    primaryContainer = MedGemPrimaryContainer,
    onPrimaryContainer = MedGemOnPrimaryContainer,
    secondary = MedGemSecondary, // Note: Secondary often used for FABs or accents
    onSecondary = MedGemOnSecondary,
    secondaryContainer = MedGemSecondaryContainer,
    onSecondaryContainer = MedGemOnSecondaryContainer,
    tertiary = MedGemTertiary,
    onTertiary = MedGemOnTertiary,
    tertiaryContainer = MedGemTertiaryContainer,
    onTertiaryContainer = MedGemOnTertiaryContainer,
    background = MedGemBackgroundLight,
    surface = MedGemSurfaceLight,
    surfaceVariant = MedGemSurfaceVariantLight,
    onBackground = Color(0xFF191C1C),
    onSurface = Color(0xFF191C1C),
    onSurfaceVariant = Color(0xFF404848),
    error = MedGemError,
    onError = MedGemOnError
)

@Composable
fun MedGemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
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