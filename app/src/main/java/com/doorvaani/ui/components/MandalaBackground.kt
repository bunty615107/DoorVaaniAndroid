package com.doorvaani.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Mandala / Lotus inspired background.
 * Approximates the GLSL shader from stitch_opendialer_sangam_design_system / * / code.html (seq broken)
 * (Vedic geometric layers, lotus petals, circles, lines).
 *
 * For full fidelity later: port the exact GLSL using RuntimeShader (API 33+) or GLSurfaceView.
 */
@Composable
fun MandalaBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFFFB347),   // DoorVaani primary-container approx
    opacity: Float = 0.12f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = minOf(size.width, size.height) / 2f

        val paintColor = primaryColor.copy(alpha = opacity)

        // Concentric circles (mandala layers)
        for (i in 1..6) {
            val r = maxRadius * (i / 7f)
            drawCircle(
                color = paintColor,
                radius = r,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Radial lines (like yantra / astronomical diagrams)
        val lineCount = 12
        for (i in 0 until lineCount) {
            val angle = (i * (360f / lineCount)) * (Math.PI / 180f)
            val end = Offset(
                x = center.x + cos(angle).toFloat() * maxRadius * 0.92f,
                y = center.y + sin(angle).toFloat() * maxRadius * 0.92f
            )
            drawLine(
                color = paintColor,
                start = center,
                end = end,
                strokeWidth = 1.dp.toPx()
            )
        }

        // Inner lotus-petal suggestion (simplified)
        for (i in 0 until 8) {
            val angle = (i * 45f) * (Math.PI / 180f)
            val petalLength = maxRadius * 0.55f
            val petalWidth = maxRadius * 0.18f

            val petalCenter = Offset(
                x = center.x + cos(angle).toFloat() * petalLength * 0.6f,
                y = center.y + sin(angle).toFloat() * petalLength * 0.6f
            )

            // Simple ellipse suggestion via two arcs (approximation)
            drawCircle(
                color = paintColor.copy(alpha = opacity * 0.6f),
                radius = petalWidth,
                center = petalCenter,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Central node (Garbagriha)
        drawCircle(
            color = primaryColor.copy(alpha = opacity * 1.5f),
            radius = 8.dp.toPx(),
            center = center
        )
    }
}