package com.doorvaani.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Design System Tokens (extracted + extended per DESIGN.md + PRD)
// Available to all composables via import com.doorvaani.ui.theme.*
import com.doorvaani.ui.theme.DoorVaaniSpacing
import com.doorvaani.ui.theme.DoorVaaniRadius
import com.doorvaani.ui.theme.DoorVaaniElevation

// =====================================================
// DOORVAANI LIGHT THEME (Primary)
// =====================================================
private val DoorVaaniLightColorScheme = lightColorScheme(
    primary = DoorVaaniPrimary,
    onPrimary = Color.White,
    primaryContainer = DoorVaaniPrimaryContainer,
    onPrimaryContainer = DoorVaaniOnPrimaryContainer,
    secondary = DoorVaaniSecondary,
    onSecondary = Color.White,
    secondaryContainer = DoorVaaniSecondaryContainer,
    onSecondaryContainer = Color(0xFF6E5C00),
    tertiary = DoorVaaniTertiary,
    onTertiary = Color.White,
    tertiaryContainer = DoorVaaniTertiaryContainer,
    onTertiaryContainer = Color(0xFF005092),
    error = DoorVaaniError,
    onError = Color.White,
    errorContainer = DoorVaaniErrorContainer,
    onErrorContainer = Color(0xFF93000A),
    background = DoorVaaniBackground,
    onBackground = DoorVaaniOnSurface,
    surface = DoorVaaniSurface,
    onSurface = DoorVaaniOnSurface,
    surfaceVariant = DoorVaaniSurfaceContainerHigh,
    onSurfaceVariant = DoorVaaniOnSurfaceVariant,
    outline = DoorVaaniOutline,
    outlineVariant = DoorVaaniOutlineVariant,
    surfaceContainerLowest = DoorVaaniSurfaceContainerLowest,
    surfaceContainerLow = DoorVaaniSurfaceContainerLow,
    surfaceContainer = DoorVaaniSurfaceContainer,
    surfaceContainerHigh = DoorVaaniSurfaceContainerHigh,
    surfaceContainerHighest = DoorVaaniSurfaceContainerHighest,
)

// =====================================================
// SANGAM DARK THEME (Architectural)
// =====================================================
private val SangamDarkColorScheme = darkColorScheme(
    primary = SangamPrimary,
    onPrimary = Color(0xFF4D2700),
    primaryContainer = SangamPrimaryContainer,
    onPrimaryContainer = SangamOnPrimaryContainer,
    secondary = SangamSecondary,
    onSecondary = Color(0xFF10305C),
    secondaryContainer = SangamSecondaryContainer,
    onSecondaryContainer = Color(0xFF9EB9ED),
    tertiary = SangamTertiary,
    onTertiary = Color(0xFF3D2E00),
    tertiaryContainer = SangamTertiaryContainer,
    onTertiaryContainer = Color(0xFF4F3D00),
    error = SangamError,
    onError = Color(0xFF690005),
    errorContainer = SangamErrorContainer,
    onErrorContainer = Color(0xFFFFDAD6),
    background = SangamBackground,
    onBackground = SangamOnSurface,
    surface = SangamSurface,
    onSurface = SangamOnSurface,
    surfaceVariant = SangamSurfaceContainerHigh,
    onSurfaceVariant = SangamOnSurfaceVariant,
    outline = SangamOutline,
    outlineVariant = SangamOutlineVariant,
    surfaceContainerLowest = SangamSurfaceContainerLowest,
    surfaceContainerLow = SangamSurfaceContainerLow,
    surfaceContainer = SangamSurfaceContainer,
    surfaceContainerHigh = SangamSurfaceContainerHigh,
    surfaceContainerHighest = SangamSurfaceContainerHighest,
)

// Local for current theme name (DoorVaani or Sangam) so screens can react
val LocalDoorVaaniTheme = staticCompositionLocalOf { "DoorVaani" }

@Composable
fun DoorVaaniTheme(
    useSangam: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useSangam) SangamDarkColorScheme else DoorVaaniLightColorScheme
    val typography = if (useSangam) SangamTypography else DoorVaaniTypography

    CompositionLocalProvider(
        LocalDoorVaaniTheme provides if (useSangam) "Sangam" else "DoorVaani"
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

// Convenience for preview / quick switching
@Composable
fun DoorVaaniPreviewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    DoorVaaniTheme(useSangam = darkTheme, content = content)
}