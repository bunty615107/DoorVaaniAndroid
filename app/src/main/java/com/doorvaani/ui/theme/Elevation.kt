package com.doorvaani.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sangam / DoorVaani Design System Elevation & Glassmorphism Tokens
 * Per DESIGN.md:
 * - DoorVaani: Tonal layering + ambient glows (rgba(255,179,71,0.08)) instead of heavy shadows. Glassmorphism (blur 12px) for nav/FABs.
 * - Sangam: Tonal surface tiering + "Heavy & Low" shadows with indigo tint. 2px gold top-border accents on priority cards.
 * Low opacity geometric overlays always.
 */
object DoorVaaniElevation {
    // Standard Material elevations kept minimal (prefer tonal + glow)
    val none: Dp = 0.dp
    val xs: Dp = 1.dp
    val sm: Dp = 2.dp
    val md: Dp = 4.dp
    val lg: Dp = 8.dp
    val xl: Dp = 12.dp

    // Ambient glow tint for DoorVaani (soft warm light bed)
    val ambientGlow: Dp = 2.dp   // Used with DoorVaaniAmbientGlow color

    // Glass / architectural
    val glassBlur: Dp = 12.dp    // Reference value (Compose backdrop not native blur on all layers; approximate via alpha + border)

    // Priority card top accent (Sangam)
    val goldAccentHeight: Dp = 2.dp

    // Dial key subtle press / resting
    val keyResting: Dp = 2.dp
    val keyPressed: Dp = 1.dp
}