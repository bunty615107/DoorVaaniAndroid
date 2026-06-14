package com.doorvaani.utils

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Haptics helper for key presses, FABs, toggles (per PRD NFR + golden scripts: navigator.vibrate).
 * Use in Compose: HapticsHelper.keyPress(haptic)
 * Phase 0: standard types (keyboard tap, long press).
 * Future: custom patterns for "stone/metal" feel on Sangam.
 */
object HapticsHelper {
    fun keyPress(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // light tap feel
    }

    fun actionConfirm(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun toggle(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
}
