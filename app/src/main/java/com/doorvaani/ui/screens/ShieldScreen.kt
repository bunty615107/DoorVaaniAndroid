package com.doorvaani.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.doorvaani.R
import com.doorvaani.ui.components.ArchitecturalCard
import com.doorvaani.ui.components.ShaderBackground
import com.doorvaani.ui.theme.LocalDoorVaaniTheme

/**
 * Shield of Sangam (Phase 1 base + Phase 3 community federation).
 * Fidelity to spam_protection_center_shield_of_sangam golden:
 * - Animated threat gauge (semicircle + needle)
 * - Enhanced Protection
 * - Recently Blocked list + tags
 * - "Help Improve Sangam" / Report New Spam
 * - 30-day metrics
 *
 * Phase 3 addition: Community Federation opt-in card (E2EE anonymous reports to Sangam network).
 */
@Composable
fun ShieldScreen(
    federationEnabled: Boolean = false,
    onToggleFederation: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSangam = LocalDoorVaaniTheme.current == "Sangam"
    var enhanced by remember { mutableStateOf(true) }
    // Local demo for federation (real source of truth lives in Settings prefs + homeViewModel)
    var localFederation by remember { mutableStateOf(federationEnabled) }
    // Phase 3: community contribution count (increases on federation actions for demo reactivity)
    var communityReports by remember { mutableStateOf(if (federationEnabled) 24 else 0) }

    // Animated needle position (0.0 low green -> 1.0 high red, "Elevated" ~0.55)
    val targetThreat by remember { mutableStateOf(0.55f) }
    val animatedThreat by animateFloatAsState(
        targetValue = targetThreat,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "threatGauge"
    )

    Box(modifier = modifier.fillMaxSize()) {
        ShaderBackground(isSangam = isSangam, opacity = 0.05f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.shield_of_sangam), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text("Community-powered spam protection • On-device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(24.dp))

            // Threat Gauge (animated semicircle + needle per spam_protection_center_shield_of_sangam + F QA)
            ArchitecturalCard(showTopAccent = true, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.global_threat_level), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val center = Offset(size.width / 2, size.height * 0.9f)
                    val radius = size.width * 0.38f

                    // Gradient arc (green low -> yellow -> red high) for visual fidelity
                    val colors = listOf(Color(0xFF4CAF50), Color(0xFFFFEB3B), Color(0xFFF44336))
                    val sweep = 180f
                    for (i in 0 until 3) {
                        drawArc(
                            color = colors[i],
                            startAngle = 180f + (i * sweep / 3),
                            sweepAngle = sweep / 3,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                            style = Stroke(width = 18.dp.toPx())
                        )
                    }

                    // Animated needle
                    val needleAngle = 180f + (animatedThreat * 180f)
                    val rad = Math.toRadians(needleAngle.toDouble()).toFloat()
                    val needleEnd = Offset(
                        center.x + radius * 0.85f * kotlin.math.cos(rad),
                        center.y + radius * 0.85f * kotlin.math.sin(rad)
                    )
                    drawLine(
                        color = if (isSangam) Color(0xFFECC246) else Color(0xFFFFB347),
                        start = center,
                        end = needleEnd,
                        strokeWidth = 6.dp.toPx()
                    )
                    drawCircle(color = Color.Red, radius = 8.dp.toPx(), center = needleEnd)
                }

                val threatLabel = if (animatedThreat > 0.7f) "High" else if (animatedThreat > 0.4f) "Elevated" else "Low"
                Text("$threatLabel • 30-day trend: ↓ 12%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            // Enhanced toggle
            ArchitecturalCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.enhanced_protection), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.enhanced_protection_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = enhanced, onCheckedChange = { enhanced = it })
                }
            }

            Spacer(Modifier.height(12.dp))

            ArchitecturalCard(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.recently_blocked), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("• +91 98765...  Telemarketing", style = MaterialTheme.typography.bodySmall)
                Text("• +1 (555) 123...  Fraud Alert", style = MaterialTheme.typography.bodySmall)
                Text("• Unknown  Survey", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { /* Report flow stub per goldens */ }) {
                    Text(stringResource(R.string.report_new_spam))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Phase 3: Community Federation card (fidelity to "Help Improve Sangam" golden + PRD)
            ArchitecturalCard(showTopAccent = true, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.help_improve_sangam), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.help_improve_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.federation_status), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    if (localFederation) {
                        Text("Your contributions this period: $communityReports anonymized reports", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Share anonymized reports (E2EE)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = localFederation,
                            onCheckedChange = {
                                localFederation = it
                                onToggleFederation(it)
                                if (it) communityReports += (3..7).random() // demo bump
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            // Stub: would trigger E2EE anonymous report bundle
                            if (localFederation) communityReports += (1..3).random()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.contribute_to_sangam))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("30-day: 142 blocked • 4 reports submitted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Phase 3 marker
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.phase_3_scale), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}