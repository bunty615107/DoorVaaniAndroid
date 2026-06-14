package com.doorvaani.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.doorvaani.ui.theme.DoorVaaniElevation
import com.doorvaani.ui.theme.DoorVaaniRadius
import com.doorvaani.ui.theme.DoorVaaniSpacing
import com.doorvaani.ui.theme.LocalDoorVaaniTheme

/**
 * Architectural / Bento Card per Sangam + DoorVaani DESIGN.md and Stitch goldens.
 *
 * - DoorVaani: Soft tonal surfaces + subtle warm glow border, generous padding (Akasha breathing).
 * - Sangam: Tonal layering + optional 2dp Temple Gold top accent (priority / carved stone feel).
 * - Uses 8px rhythm tokens + radii.
 * - Glass-like or solid depending on elevation.
 *
 * Matches home_dashboard bento cards, settings hierarchy, vault lists, etc.
 */
@Composable
fun ArchitecturalCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showTopAccent: Boolean = false,           // Sangam priority accent
    hasGoldTopAccent: Boolean = false,        // Alias for Playground / active demos
    hasInnerPattern: Boolean = false,         // Radial dot grid (home bento golden)
    isSangam: Boolean? = null,                // Explicit override (for Playground)
    contentPadding: Dp = DoorVaaniSpacing.cardPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedIsSangam = isSangam ?: (LocalDoorVaaniTheme.current == "Sangam")
    val radius = DoorVaaniRadius.card
    val tonalContainer = if (resolvedIsSangam) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val cardModifier = modifier
        .clip(RoundedCornerShape(radius))
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else Modifier
        )

    Surface(
        modifier = cardModifier,
        color = tonalContainer,
        shadowElevation = if (resolvedIsSangam) DoorVaaniElevation.sm else DoorVaaniElevation.none,
        border = if (!resolvedIsSangam) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)) else null
    ) {
        Column {
            val effectiveGold = showTopAccent || hasGoldTopAccent
            if (effectiveGold && resolvedIsSangam) {
                // 2px Temple Gold top border accent (sangam/DESIGN.md)
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DoorVaaniElevation.goldAccentHeight)
                        .background(MaterialTheme.colorScheme.tertiary)
                )
            }

            Box {
                Column(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(DoorVaaniSpacing.itemGap),
                    content = content
                )

                if (hasInnerPattern) {
                    // Subtle radial dot grid watermark (home_dashboard_doorvaani golden + a_subtle background)
                    CanvasForCardPattern(
                        modifier = Modifier.matchParentSize(),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                    )
                }
            }
        }
    }
}

// Internal dot grid for hasInnerPattern (cheap, matches golden watermarks)
@Composable
private fun CanvasForCardPattern(modifier: Modifier, color: Color) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val step = 22.dp.toPx()
        val dotR = 1.2.dp.toPx()
        for (x in 0..(size.width / step).toInt()) {
            for (y in 0..(size.height / step).toInt()) {
                drawCircle(color, dotR, androidx.compose.ui.geometry.Offset(x * step + 4f, y * step + 4f))
            }
        }
    }
}

// Overload for simple content without onClick (common in lists)
@Composable
fun ArchitecturalCard(
    modifier: Modifier = Modifier,
    showTopAccent: Boolean = false,
    contentPadding: Dp = DoorVaaniSpacing.cardPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    ArchitecturalCard(
        modifier = modifier,
        onClick = null,
        showTopAccent = showTopAccent,
        contentPadding = contentPadding,
        content = content
    )
}