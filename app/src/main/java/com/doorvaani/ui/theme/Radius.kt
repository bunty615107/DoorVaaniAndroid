package com.doorvaani.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sangam / DoorVaani Design System Radius / Shape Tokens
 * Source: doorvaani/DESIGN.md (Rounded synthesis of circle+square) + sangam/DESIGN.md (disciplined carved stone)
 * "8dp small, 16dp medium, 24/32dp large"
 */
object DoorVaaniRadius {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp          // 0.25rem
    val sm: Dp = 8.dp          // 0.5rem (DEFAULT in both)
    val md: Dp = 12.dp         // 0.75rem
    val lg: Dp = 16.dp         // 1rem
    val xl: Dp = 24.dp         // 1.5rem
    val xxl: Dp = 32.dp
    val full: Dp = 9999.dp     // Pill / circle (9999px)

    // Semantic
    val button: Dp = sm
    val card: Dp = lg          // Architectural cards
    val dialKey: Dp = full     // Perfect circles per goldens
    val chip: Dp = full        // Pill
    val fab: Dp = full
    val modal: Dp = xl
}