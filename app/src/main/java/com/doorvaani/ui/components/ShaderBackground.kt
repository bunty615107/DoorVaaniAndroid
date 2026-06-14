package com.doorvaani.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * High-fidelity animated Mandala / Shader Background.
 *
 * Faithful port + approximation of GLSL from:
 * - stitch_opendialer_sangam_design_system/shader_1/code.html (cream + rotating gold/orange lines, hash, nodes, mouse)
 * - dial_pad_doorvaani/code.html + home (mandala(r,a) lotus petals + sin waves, soft flowing gradient)
 * - shader_2 (Sangam dark + copper + Nagara vertical pulses + diamond lattice)
 * - a_subtle_elegant... screen.png (intricate repeating lotus/yantra concentric layers)
 *
 * Features (per charter + PRD):
 * - Time-animated via LaunchedEffect + frame time (smooth 60fps intent)
 * - Multiple rotating layers (lines, rings, petals)
 * - Intersection nodes / subtle dots
 * - Lotus / yantra petal suggestions
 * - Touch / pointer reactive highlight (pointerInput)
 * - Theme adaptive: DoorVaani (warm cream + #FFB347/#FCD400 gold tones, low opacity ~5-12%)
 *                 Sangam (granite dark + copper #FFB77B + temple gold accents)
 * - Pure Canvas for broad compatibility (minSdk 26). RuntimeShader (AGSL) opt-in for API 33+ later.
 * - Low opacity overall for "subtle" requirement. Performance: limited iterations, no heavy per-frame alloc.
 *
 * Usage: Place as first child in Box(Modifier.fillMaxSize()) behind content.
 * opacityMultiplier: 0.08f-0.12f recommended for screens.
 */
@Composable
fun ShaderBackground(
    modifier: Modifier = Modifier,
    isSangam: Boolean = false,
    opacity: Float = 0.09f,
    enableTouchReactive: Boolean = true
) {
    val density = LocalDensity.current

    // Phase 2/3 perf: isActive for adaptive (future Lifecycle/BatteryManager onPause/thermal per PRD NFRs <1.2s/60fps/battery). 
    var isActive by remember { mutableStateOf(true) }

    // Animated time state (seconds) - optimized with produceState + withFrameMillis for frame-synced 60fps, less GC than System + delay
    val time by produceState(0f) {
        val startTime = withFrameMillis { it }
        while (true) {
            if (isActive) {
                val now = withFrameMillis { it }
                value = (now - startTime) / 1000f
            } else {
                // yield when paused
                withFrameMillis { }
            }
        }
    }

    // Touch / mouse position (normalized 0-1 center relative). Reactive glow.
    var touchPos by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    val touchModifier = if (enableTouchReactive) {
        Modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                touchPos = Offset(
                    offset.x / size.width,
                    offset.y / size.height
                )
            }
        }
    } else Modifier

    // Phase 3 perf: adaptive (pause on background/low battery via Lifecycle/BatteryManager per PRD NFRs <1.2s launch/60fps/battery). isActive stub for future; current ~60fps Canvas cheap. Add semantics for a11y.

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .then(touchModifier)
    ) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val maxR = min(w, h) / 2f

        // Palette selection (exact from DESIGN.md + GLSL refs)
        val (bgTint, linePrimary, lineSecondary, nodeColor, accentBlueish) = if (isSangam) {
            // Sangam dark architectural (shader_2 spirit + DESIGN sangam)
            // Granite base is provided by parent Surface. Here accent copper/gold on low alpha.
            listOf(
                Color(0xFF131313).copy(alpha = 0f), // transparent over dark
                Color(0xFFFFB77B), // Copper Bronze primary
                Color(0xFFECC246), // Temple Gold tertiary
                Color(0xFFFFB77B),
                Color(0xFFACC7FC)  // Indigo secondary for variety
            )
        } else {
            // DoorVaani cream + Agni fire (shader_1 + dial/home GLSL)
            listOf(
                Color(0xFFF9F9F9).copy(alpha = 0f),
                Color(0xFFFFB347), // Primary orange
                Color(0xFFFCD400), // Secondary golden yellow
                Color(0xFFFFB347),
                Color(0xFF0060AC)  // Tertiary blue sparingly
            )
        }

        val baseAlpha = opacity.coerceIn(0.02f, 0.18f)
        val paintPrimary = linePrimary.copy(alpha = baseAlpha)
        val paintSecondary = lineSecondary.copy(alpha = baseAlpha * 0.75f)
        val paintNode = nodeColor.copy(alpha = baseAlpha * 0.9f)

        // === Core mandala layers (inspired by GLSL mandala() + rotating p) ===
        val t = time * 0.08f   // Slow elegant drift matching mocks (u_time * 0.1 / 0.3)

        // Layer 1-3: Rotating geometric line fields (shader_1 style)
        for (layer in 1..3) {
            val layerT = t * (0.7f + layer * 0.15f)
            val rot = layerT * (20f + layer * 8f)   // degrees-ish

            rotate(degrees = rot, pivot = center) {
                val pScale = 3.2f + layer * 0.6f
                val step = 18f

                // Horizontal + vertical-ish sin waves -> lines (fract like)
                for (i in -8..8) {
                    val xOff = i * step * (w / 200f)
                    val yBase = center.y + sin((i * 0.7f) + layerT * 1.3f) * (maxR * 0.2f)

                    // Soft line segments (approximating sin(p.x*2 +t) cos etc.)
                    drawLine(
                        color = if (layer % 2 == 0) paintPrimary else paintSecondary,
                        start = Offset(center.x - maxR * 0.95f + xOff * 0.3f, yBase - maxR * 0.4f),
                        end = Offset(center.x + maxR * 0.95f + xOff * 0.2f, yBase + maxR * 0.45f),
                        strokeWidth = (0.7f + layer * 0.1f).dp.toPx()
                    )
                }
            }
        }

        // Concentric rings / yantra circles (multiple per mocks)
        val ringCount = 7
        for (i in 1..ringCount) {
            val r = maxR * (i / (ringCount + 0.7f))
            val ringAlphaMul = 1f - (i / (ringCount + 2f)) * 0.6f
            val strokeW = if (i % 3 == 0) 1.2f else 0.7f

            drawCircle(
                color = paintPrimary.copy(alpha = paintPrimary.alpha * ringAlphaMul),
                radius = r,
                center = center,
                style = Stroke(width = strokeW.dp.toPx())
            )

            // Inner subtle offset ring for lotus depth (a_subtle pattern fidelity)
            if (i % 2 == 1 && i < 5) {
                drawCircle(
                    color = paintSecondary.copy(alpha = paintSecondary.alpha * 0.55f),
                    radius = r * 0.88f,
                    center = center,
                    style = Stroke(width = 0.5.dp.toPx())
                )
            }
        }

        // Radial spokes + intersection nodes (core of shader_1)
        val spokes = 12
        for (i in 0 until spokes) {
            val angleDeg = (i * (360f / spokes)) + (t * 12f) % 360f   // slow spin
            val angle = Math.toRadians(angleDeg.toDouble()).toFloat()

            val end = Offset(
                x = center.x + cos(angle) * maxR * 0.96f,
                y = center.y + sin(angle) * maxR * 0.96f
            )
            drawLine(
                color = paintPrimary,
                start = center,
                end = end,
                strokeWidth = 0.8.dp.toPx()
            )

            // Intersection "nodes" at ~0.6r (GLSL fract circles)
            val nodeDist = maxR * 0.62f
            val node = Offset(
                center.x + cos(angle) * nodeDist,
                center.y + sin(angle) * nodeDist
            )
            drawCircle(
                color = paintNode,
                radius = 3.5.dp.toPx(),
                center = node
            )
        }

        // Lotus / petal layer (from dial/home GLSL: sin(a*8), smoothstep, r)
        // + repeating subtle lotus from a_subtle_elegant
        val petalCount = 8
        for (j in 0 until petalCount) {
            val petalAngle = (j * (360f / petalCount)) + sin(t * 0.6f) * 6f
            val a = Math.toRadians(petalAngle.toDouble()).toFloat()
            val petalR = maxR * (0.48f + sin(t * 0.9f + j) * 0.03f)

            // Petal center offset
            val pc = Offset(
                x = center.x + cos(a) * petalR * 0.65f,
                y = center.y + sin(a) * petalR * 0.65f
            )

            // Soft ellipse-ish via stroked circle (cheap petal)
            val petalW = maxR * 0.13f
            drawCircle(
                color = paintSecondary.copy(alpha = baseAlpha * 0.65f),
                radius = petalW,
                center = pc,
                style = Stroke(width = 1.6.dp.toPx())
            )
        }

        // Additional fine concentric yantra detail (splash + dial pad SVG spirit)
        for (k in 3..5) {
            val r2 = maxR * (k / 7.5f)
            drawCircle(
                color = paintPrimary.copy(alpha = baseAlpha * 0.35f),
                radius = r2,
                center = center,
                style = Stroke(width = 0.6.dp.toPx())
            )
        }

        // === Touch / mouse reactive soft radial highlight (GLSL u_mouse) ===
        if (enableTouchReactive) {
            val mouseOffset = Offset(
                center.x + (touchPos.x - 0.5f) * w * 0.6f,
                center.y + (touchPos.y - 0.5f) * h * 0.6f
            )
            val mouseDistMax = maxR * 0.55f
            val dist = (center - mouseOffset).getDistance()
            val mouseGlow = (1f - (dist / mouseDistMax).coerceIn(0f, 1f)) * 0.07f

            drawCircle(
                color = if (isSangam) linePrimary.copy(alpha = mouseGlow * 0.8f)
                else Color(1f, 0.92f, 0.75f).copy(alpha = mouseGlow),
                radius = maxR * 0.38f,
                center = mouseOffset
            )
        }

        // Central Garbagriha sanctum dot (convergence point)
        drawCircle(
            color = (if (isSangam) lineSecondary else linePrimary).copy(alpha = baseAlpha * 1.4f),
            radius = 7.dp.toPx(),
            center = center
        )
        drawCircle(
            color = (if (isSangam) linePrimary else lineSecondary).copy(alpha = baseAlpha * 0.7f),
            radius = 3.dp.toPx(),
            center = center
        )
    }
}

// (Wrapper MandalaBackground removed to eliminate conflicting overload with dedicated MandalaBackground.kt; callers use the Canvas-based one or migrate to ShaderBackground)