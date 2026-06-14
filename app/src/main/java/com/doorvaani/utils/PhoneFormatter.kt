package com.doorvaani.utils

/**
 * Reusable phone number formatter.
 * Improves on DialPadScreen inline version.
 * Phase 0: simple grouping (3-3-4 for 10 digits). Matches golden display style.
 * Future: international, +91 handling, carrier formatting.
 */
object PhoneFormatter {
    fun format(raw: String): String {
        val digits = raw.filter { it.isDigit() || it == '*' || it == '#' }
        if (digits.length <= 3) return digits
        if (digits.length <= 6) return "${digits.substring(0, 3)} ${digits.substring(3)}"
        if (digits.length <= 10) {
            return "${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6)}"
        }
        // Longer or special chars: chunk conservatively
        return digits.chunked(3).joinToString(" ").take(20)
    }

    fun isValidForCall(raw: String): Boolean {
        val cleaned = raw.filter { it.isDigit() }
        return cleaned.length >= 3
    }
}
