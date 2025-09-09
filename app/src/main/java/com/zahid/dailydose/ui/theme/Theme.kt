package com.zahid.dailydose.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MedicalGreen,
    onPrimary = TextOnPrimary,
    primaryContainer = MedicalGreenLight,
    onPrimaryContainer = TextPrimary,

    onSecondary = TextOnPrimary,
    onSecondaryContainer = TextPrimary,

    tertiary = MedicalTeal,
    onTertiary = TextOnPrimary,
    tertiaryContainer = MedicalCyan,
    onTertiaryContainer = TextPrimary,

    error = MedicalRed,
    onError = TextOnPrimary,
    errorContainer = MedicalRedLight,
    onErrorContainer = TextOnPrimary,

    background = BackgroundDark,
    onBackground = TextOnDark,
    surface = SurfaceDark,
    onSurface = TextOnDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextTertiary,

    outline = MedicalGrayLight,
    outlineVariant = MedicalGray,

    scrim = Color.Black.copy(alpha = 0.32f),
    surfaceTint = MedicalGreen
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalGreen,
    onPrimary = TextOnPrimary,
    primaryContainer = MedicalGreenLight,
    onPrimaryContainer = TextPrimary,

    onSecondary = TextOnPrimary,
    onSecondaryContainer = TextPrimary,

    tertiary = MedicalTeal,
    onTertiary = TextOnPrimary,
    tertiaryContainer = MedicalCyan,
    onTertiaryContainer = TextPrimary,

    error = MedicalRed,
    onError = TextOnPrimary,
    errorContainer = MedicalRedLight,
    onErrorContainer = TextOnPrimary,

    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,

    outline = MedicalGray,
    outlineVariant = MedicalGrayLight,

    scrim = Color.Black.copy(alpha = 0.32f),
    surfaceTint = MedicalGreen
)

@Composable
fun DailyDoseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our medical theme
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}