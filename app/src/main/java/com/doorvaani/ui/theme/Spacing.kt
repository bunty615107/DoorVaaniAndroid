package com.doorvaani.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sangam / DoorVaani Design System Spacing Tokens
 * 8px rhythm as per doorvaani/DESIGN.md + sangam/DESIGN.md
 * Source: stitch_opendialer_sangam_design_system/doorvaani/DESIGN.md & sangam/DESIGN.md
 * "Fixed Grid / Modular Grid philosophy", "Vertical rhythm strictly enforced"
 */
object DoorVaaniSpacing {
    val unit: Dp = 8.dp          // Base 8px unit
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp

    // Layout specific (from DESIGN + mocks)
    val gutter: Dp = 24.dp
    val marginMobile: Dp = 20.dp
    val marginDesktop: Dp = 64.dp
    val containerMax: Dp = 1200.dp   // Reference, not strictly enforced on mobile

    // Component specific breathing room (Akasha principle)
    val cardPadding: Dp = 20.dp
    val sectionGap: Dp = 24.dp
    val itemGap: Dp = 12.dp
    val keyGap: Dp = 20.dp           // Dial key horizontal spacing from mocks
    val keyRowGap: Dp = 12.dp
}