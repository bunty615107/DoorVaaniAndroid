package com.doorvaani.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.doorvaani.ui.theme.DoorVaaniRadius

/**
 * Concentric Pulse Rings
 * Faithful to active_call_doorvaani_refresh_1 goldens + active_call code.html (pulse-ring keyframes)
 * - 3 rings, staggered infinite pulse (scale + opacity)
 * - Architectural dashed outer ring option
 * - Themed colors (orange/gold ring for DoorVaani; copper for Sangam)
 * - Used around avatar in active call, incoming call, focus moments.
 *
 * Non-interactive, pointerEvents none friendly.
 */
@Composable
fun PulseRing(
    modifier: Modifier = Modifier,
    size: Dp = 192.dp,
    isSangam: Boolean = false,
    ringColor: Color? = null,
    showDashedArchitectural: Boolean = true
) {
    val color = ringColor ?: if (isSangam) {
        Color(0xFFFFB77B).copy(alpha = 0.55f)  // Copper
    } else {
        Color(0xFFFFB347).copy(alpha = 0.55f)  // DoorVaani primary-container
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // Staggered animations (4s cycle, offsets matching the HTML keyframes)
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, delayMillis = 1330, easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale2"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, delayMillis = 1330, easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, delayMillis = 2660, easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale3"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, delayMillis = 2660, easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha3"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = center
            val baseRadius = size.toPx() / 2f * 0.82f

            // Three animated rings (concentric pulse)
            drawCircle(
                color = color.copy(alpha = color.alpha * alpha1),
                radius = baseRadius * scale1,
                center = c,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = color.copy(alpha = color.alpha * alpha2),
                radius = baseRadius * scale2,
                center = c,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = color.copy(alpha = color.alpha * alpha3),
                radius = baseRadius * scale3,
                center = c,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Static architectural rings (dashed outer for "temple" feel, per active call golden)
            if (showDashedArchitectural) {
                drawCircle(
                    color = color.copy(alpha = 0.22f),
                    radius = baseRadius * 1.08f,
                    center = c,
                    style = Stroke(width = 1.dp.toPx())
                )
                // Dashed outer (approximated with multiple small strokes or simple for perf)
                drawCircle(
                    color = (if (isSangam) Color(0xFFECC246) else Color(0xFFFCD400)).copy(alpha = 0.18f),
                    radius = baseRadius * 1.22f,
                    center = c,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
        }
    }
}