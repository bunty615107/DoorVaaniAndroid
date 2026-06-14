package com.doorvaani.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doorvaani.ui.components.MandalaBackground
import kotlinx.coroutines.delay

/**
 * Splash matching stitch_opendialer_sangam_design_system/splash_screen_doorvaani/ golden exactly in spirit.
 * - Subtle full-screen yantra / geometric pattern bg (use Mandala + extra lines)
 * - Central abstract geometric communication glyph (diamond yantra with converging nodes + cross lines)
 * - "DoorVaani" display typography
 * - "HARMONIZING FLOW" label-caps with divider
 * - Animations: converge nodes, draw lines, pulse (approx from code.html @keyframes)
 * - Tap anywhere or auto ( ~2.2s ) to proceed.
 *
 * Full screen, elegant entry. Uses theme tokens.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiary = MaterialTheme.colorScheme.tertiary
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Convergence + pulse animation values
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val convergeProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
            repeatMode = RepeatMode.Restart
        ), label = "converge"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    // Auto advance after animation cycle
    LaunchedEffect(Unit) {
        delay(2400)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onFinished() },
        contentAlignment = Alignment.Center
    ) {
        // Subtle full mandala background (low opacity per golden)
        MandalaBackground(
            primaryColor = primary,
            opacity = 0.06f,
            modifier = Modifier.fillMaxSize()
        )

        // Decorative converging structural lines (from golden HTML)
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val long = size.height * 1.2f
            val alpha = 0.18f

            // Cross + diagonal lines (converging to center)
            drawLine(outlineVariant.copy(alpha), center.copy(y = center.y - long), center.copy(y = center.y + long), 1.dp.toPx())
            drawLine(outlineVariant.copy(alpha), center.copy(x = center.x - long), center.copy(x = center.x + long), 1.dp.toPx())
            drawLine(outlineVariant.copy(alpha * 0.7f), Offset(0f, 0f), Offset(size.width, size.height), 0.8.dp.toPx())
            drawLine(outlineVariant.copy(alpha * 0.7f), Offset(size.width, 0f), Offset(0f, size.height), 0.8.dp.toPx())
        }

        // Main yantra / glyph container (converging)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 32.dp)
        ) {
            // Yantra logo - exact structure from golden SVG (polygons + nodes + lines)
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .offset(y = (-8).dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = center
                    val scale = pulse
                    val rOuter = 46.dp.toPx() * scale
                    val rInner = 30.dp.toPx() * scale

                    // Outer structural diamond (square rotated)
                    val outerPts = listOf(
                        Offset(c.x, c.y - rOuter),
                        Offset(c.x + rOuter, c.y),
                        Offset(c.x, c.y + rOuter),
                        Offset(c.x - rOuter, c.y)
                    )
                    drawPathForPoints(outerPts, outlineVariant, strokeWidth = 1.2.dp.toPx())

                    // Inner precision diamond (primaryContainer color)
                    val innerPts = listOf(
                        Offset(c.x, c.y - rInner),
                        Offset(c.x + rInner, c.y),
                        Offset(c.x, c.y + rInner),
                        Offset(c.x - rInner, c.y)
                    )
                    drawPathForPoints(innerPts, primaryContainer, strokeWidth = 2.2.dp.toPx())

                    // Central node (yellowish via primary)
                    drawCircle(
                        color = primary,
                        radius = 6.dp.toPx() * scale,
                        center = c
                    )

                    // Connecting network lines (tertiary)
                    val lineLen = 22.dp.toPx()
                    drawLine(tertiary, c.copy(x = c.x - lineLen), c.copy(x = c.x + lineLen * 0.4f), 1.8.dp.toPx())
                    drawLine(tertiary, c.copy(x = c.x + lineLen), c.copy(x = c.x - lineLen * 0.4f), 1.8.dp.toPx())
                    drawLine(tertiary, c.copy(y = c.y - lineLen), c.copy(y = c.y + lineLen * 0.4f), 1.8.dp.toPx())
                    drawLine(tertiary, c.copy(y = c.y + lineLen), c.copy(y = c.y - lineLen * 0.4f), 1.8.dp.toPx())

                    // Floating nodes (4 cardinal)
                    val nodeR = 2.8.dp.toPx()
                    drawCircle(tertiary, nodeR, Offset(c.x, c.y - rOuter * 0.78f))
                    drawCircle(tertiary, nodeR, Offset(c.x, c.y + rOuter * 0.78f))
                    drawCircle(tertiary, nodeR, Offset(c.x - rOuter * 0.78f, c.y))
                    drawCircle(tertiary, nodeR, Offset(c.x + rOuter * 0.78f, c.y))
                }
            }

            Spacer(Modifier.height(24.dp))

            // DoorVaani title - exact per golden typography
            Text(
                "DoorVaani",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.6).sp
                ),
                color = onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Divider + HARMONIZING FLOW
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(1.dp)
                    .background(outlineVariant)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "HARMONIZING FLOW",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    letterSpacing = 2.8.sp
                ),
                color = onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPathForPoints(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float
) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        close()
    }
    drawPath(path, color, style = Stroke(width = strokeWidth))
}
