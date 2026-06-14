package com.doorvaani.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.doorvaani.R
import com.doorvaani.ui.components.PulseRing
import com.doorvaani.ui.components.ShaderBackground
import com.doorvaani.ui.theme.LocalDoorVaaniTheme
import com.doorvaani.utils.HapticsHelper
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Incoming Call screen (Phase 1).
 * High fidelity to incoming_call_doorvaani*, incoming_call_doorvaani_rebrand, incoming_call_marcus_aurelius goldens + code.html:
 * - Full-bleed shader + staggered PulseRing (concentric pulse + architectural) around avatar.
 * - Large portrait/avatar placeholder (192dp+ frame, borders per Sangam/DoorVaani).
 * - "Incoming Call" caps header (label-caps tracking-widest), name (large display), number • location with dot separator.
 * - Large Decline (bordered surface/granite with primary accent) and Accept (primary copper/bronze filled) ~80-88dp circles.
 * - Sangam architectural (dark granite/copper) or DoorVaani warm Vedic aesthetic via theme.
 * - Focused transactional, no bottom nav (handled by NavHost).
 * - References F report emphasis on full rings + avatar for match to goldens.
 */
@Composable
fun IncomingCallScreen(
    number: String,
    contactName: String = "Unknown",
    location: String = "Unknown location",
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isSangam = LocalDoorVaaniTheme.current == "Sangam"

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ShaderBackground(
            isSangam = isSangam,
            opacity = 0.09f
        )

        // Staggered pulse rings (matches incoming goldens + code.html pulse-ring keyframes, 2-3 rings)
        // Larger container to accommodate outer architectural rings
        PulseRing(
            modifier = Modifier.size(340.dp),
            isSangam = isSangam,
            size = 340.dp,
            showDashedArchitectural = true
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(60.dp))

            // "Incoming Call" header caps (exact per goldens: font-label-caps uppercase tracking-widest)
            Text(
                stringResource(R.string.incoming_call),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = if (isSangam) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(24.dp))

            // Avatar / portrait area with rings (enlarged to ~192dp+ to match golden w-48 h-48 frames + border)
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Caller portrait",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(96.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Name (large, per golden display/headline style)
            Text(
                contactName,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            // Number • location (with dot separator per incoming_call goldens)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    number,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(72.dp))

            // Accept / Decline large circles (exact layout/spacing per incoming goldens)
            // Decline: bordered granite/surface + primary accent; Accept: filled primaryContainer/bronze
            Row(
                horizontalArrangement = Arrangement.spacedBy(52.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline (granite black with copper/primary border per Sangam + DoorVaani variants)
                FloatingActionButton(
                    onClick = {
                        HapticsHelper.actionConfirm(haptic)
                        onDecline()
                    },
                    containerColor = if (isSangam) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSangam) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        Icons.Filled.CallEnd,
                        contentDescription = stringResource(R.string.decline),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Accept (copper bronze primary solid fill)
                FloatingActionButton(
                    onClick = {
                        HapticsHelper.actionConfirm(haptic)
                        onAccept()
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        Icons.Filled.Call,
                        contentDescription = stringResource(R.string.accept),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            Text(
                "Swipe or tap to respond",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}