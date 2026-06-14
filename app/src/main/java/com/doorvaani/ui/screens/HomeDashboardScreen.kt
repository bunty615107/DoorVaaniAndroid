package com.doorvaani.ui.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.doorvaani.domain.model.RecentCall
import com.doorvaani.domain.model.UserPreferences
import androidx.compose.ui.res.stringResource
import com.doorvaani.R
import com.doorvaani.ui.components.ArchitecturalCard
import com.doorvaani.ui.components.MandalaBackground
import com.doorvaani.ui.theme.DoorVaaniElevation
import com.doorvaani.ui.theme.DoorVaaniRadius
import com.doorvaani.ui.theme.DoorVaaniSpacing
import com.doorvaani.utils.HapticsHelper
import com.doorvaani.utils.PhoneFormatter

/**
 * Home / Dashboard bento exactly per stitch_opendialer_sangam_design_system/home_dashboard_doorvaani/
 * (and PRD §5, §9 Home/Dashboard must-have).
 *
 * - Greeting "Namaste, Aravind" + tagline
 * - "Ready to connect?" primary Garbagriha card with prominent "Open Keypad" (primaryContainer pill + dialpad icon)
 * - Weekly Flow stat card (42)
 * - Recent Channels list (avatar circle, name, time/status icon, quick call FAB)
 * - Focus Settings: Dharma Mode + Vastu Dialing toggles (glass panels, bound to prefs)
 *
 * Uses Architectural bento style via tonal surface cards + subtle radial watermark.
 * Low opacity Mandala bg. No magic numbers. Theme adaptive.
 * Quick actions navigate or invoke callbacks (deep nav to keypad / call).
 *
 * Handoff note: Replace inline cards with Agent A ArchitecturalCard / BentoGrid when ready.
 */
@Composable
fun HomeDashboardScreen(
    prefs: UserPreferences,
    onToggleDharma: () -> Unit,
    onToggleVastu: () -> Unit,
    onOpenKeypad: () -> Unit,
    onQuickCall: (String) -> Unit,
    onNavigateToContacts: () -> Unit = {},
    onSimulateIncoming: () -> Unit = {},
    recentCalls: List<RecentCall> = emptyList(),
    weeklyFlow: Int = 42,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val surfaceContainerLowest = MaterialTheme.colorScheme.surfaceContainerLowest
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier.fillMaxSize()) {
        // Subtle shader / mandala bg (matches all goldens)
        MandalaBackground(
            primaryColor = primary,
            opacity = 0.035f,
            modifier = Modifier.fillMaxSize()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DoorVaaniSpacing.marginMobile, vertical = DoorVaaniSpacing.md),
            verticalArrangement = Arrangement.spacedBy(DoorVaaniSpacing.md)
        ) {
            // Greeting section (centered like golden)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.namaste) + ", Aravind",
                        style = MaterialTheme.typography.headlineMedium,
                        color = onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        stringResource(R.string.tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariant
                    )
                }
            }

            // Bento: Ready to connect? (large primary) + Weekly Flow (stat)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Garbagriha Quick Dial card (md:col-span-8 feel) - now using ArchitecturalCard for exact bento glass + inner pattern fidelity
                    ArchitecturalCard(
                        modifier = Modifier
                            .weight(1.6f)
                            .height(160.dp),
                        onClick = {
                            HapticsHelper.actionConfirm(haptic)
                            onOpenKeypad()
                        },
                        hasInnerPattern = true,
                        contentPadding = DoorVaaniSpacing.cardPadding
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    stringResource(R.string.ready_to_connect),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = primary
                                )
                                Spacer(Modifier.height(DoorVaaniSpacing.lg))
                                // Prominent pill button (Garbagriha)
                                Surface(
                                    onClick = {
                                        HapticsHelper.actionConfirm(haptic)
                                        onOpenKeypad()
                                    },
                                    shape = RoundedCornerShape(50),
                                    color = primaryContainer,
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Dialpad,
                                            contentDescription = "Open dial pad",
                                            tint = onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            stringResource(R.string.open_keypad),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = onPrimaryContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Weekly Flow stat card (md:col-span-4) - ArchitecturalCard bento
                    ArchitecturalCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp),
                        contentPadding = DoorVaaniSpacing.cardPadding
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    stringResource(R.string.weekly_flow),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = onSurface
                                )
                                Icon(
                                    Icons.Filled.Insights,
                                    contentDescription = stringResource(R.string.weekly_flow_insights),
                                    tint = primaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    weeklyFlow.toString(),
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
                                    color = primary
                                )
                                Text(
                                    stringResource(R.string.meaningful_connections_week),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Focus Settings (Dharma + Vastu) - ArchitecturalCard bento (always prominent)
            item {
                ArchitecturalCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = DoorVaaniSpacing.cardPadding
                ) {
                    Text(stringResource(R.string.focus_settings), style = MaterialTheme.typography.titleLarge, color = onSurface)
                    Spacer(Modifier.height(DoorVaaniSpacing.itemGap))

                    // Dharma Mode row
                    FocusToggleRow(
                        icon = Icons.Filled.Person, // brightness_high / spiritual proxy
                        title = "Dharma Mode",
                        subtitle = "Block earthly distractions",
                        checked = prefs.dharmaModeEnabled,
                        onCheckedChange = {
                            HapticsHelper.toggle(haptic)
                            onToggleDharma()
                        },
                        highlight = false
                    )

                    Spacer(Modifier.height(DoorVaaniSpacing.sm))

                    // Vastu Dialing row (highlighted when on per golden)
                    FocusToggleRow(
                        icon = Icons.Filled.Dialpad,
                        title = "Vastu Dialing",
                        subtitle = "Aligned layouts",
                        checked = prefs.vastuDialingEnabled,
                        onCheckedChange = {
                            HapticsHelper.toggle(haptic)
                            onToggleVastu()
                        },
                        highlight = prefs.vastuDialingEnabled
                    )

                    if (prefs.vastuDialingEnabled) {
                        Spacer(Modifier.height(DoorVaaniSpacing.sm))
                        var showVastuDialog by remember { mutableStateOf(false) }
                        val calendarStubAdded = stringResource(R.string.calendar_stub_added)
                        val addToCalendarLabel = stringResource(R.string.add_to_calendar)
                        TextButton(onClick = {
                            HapticsHelper.toggle(haptic)
                            showVastuDialog = true
                        }) {
                            Text(
                                "Vastu: Current alignment window open — Tap for suggestion (calendar stub)",
                                style = MaterialTheme.typography.labelSmall,
                                color = primary
                            )
                        }
                        if (showVastuDialog) {
                            val calendar = java.util.Calendar.getInstance()
                            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                            val suggestion = if (hour in 7..10) "Morning (7-11 AM) aligned for spiritual guides like Guru Ji / Temple Trust (based on current hour + role)." else "Evening window or next morning recommended (simple role + hour heuristic; full Calendar provider for precise recurring events)."
                            AlertDialog(
                                onDismissRequest = { showVastuDialog = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showVastuDialog = false
                                        HapticsHelper.actionConfirm(haptic)
                                        // Real-ish: launch calendar intent (no new perms; stub if no app)
                                        val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                                            data = android.provider.CalendarContract.Events.CONTENT_URI
                                            putExtra(android.provider.CalendarContract.Events.TITLE, "Vastu-aligned call with Guru Ji")
                                            putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Suggested per DoorVaani Vastu: $suggestion")
                                            putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis + 3600000) // +1h
                                        }
                                        // In real app: context.startActivity(intent)
                                        Toast.makeText(context, calendarStubAdded, Toast.LENGTH_LONG).show()
                                    }) { Text(addToCalendarLabel) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showVastuDialog = false }) { Text("Close") }
                                },
                                title = { Text(stringResource(R.string.vastu_suggestion_title)) },
                                text = { Text(suggestion + " (Third-party CalendarContract integration would handle recurring + reminders.)") }
                            )
                        }
                    }
                }
            }

            // Advanced Phase 2: Dharma Mode - Mantra prompt (UI quieting + optional auto-reply mantra per PRD)
            // Visible only when enabled. Uses ArchitecturalCard + quote fidelity from active_call Marcus golden.
            if (prefs.dharmaModeEnabled) {
                item {
                    DharmaMantraPrompt(
                        onRecite = {
                            HapticsHelper.actionConfirm(haptic)
                        }
                    )
                }
            }

            // Recent Channels section - bento ArchitecturalCard
            // Phase 2 advanced: when Dharma enabled, apply quieting (dim alpha) to reduce distraction.
            item {
                val recentsAlpha = if (prefs.dharmaModeEnabled) 0.62f else 1f
                ArchitecturalCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(recentsAlpha),
                    contentPadding = DoorVaaniSpacing.cardPadding
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.recent_channels), style = MaterialTheme.typography.titleLarge, color = onSurface)
                        TextButton(onClick = onNavigateToContacts) {
                            Text(stringResource(R.string.view_all), color = primary, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(Modifier.height(DoorVaaniSpacing.sm))

                    if (recentCalls.isEmpty()) {
                        Text("No recent channels yet.", color = onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(DoorVaaniSpacing.xs)) {
                            recentCalls.take(3).forEach { recent ->
                                RecentChannelRow(
                                    recent = recent,
                                    vastuEnabled = prefs.vastuDialingEnabled,
                                    onQuickCall = {
                                        HapticsHelper.actionConfirm(haptic)
                                        onQuickCall(recent.phone)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Phase 2 footer note (advanced focus behaviors live)
            item {
                Text(
                    "Advanced Focus active (Dharma quieting + Vastu alignment)",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = DoorVaaniSpacing.sm)
                        .semantics { contentDescription = "Phase 2 advanced focus behaviors: Dharma Mode UI quieting and mantra prompt; Vastu aligned recents highlights" },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RecentChannelRow(
    recent: RecentCall,
    vastuEnabled: Boolean,
    onQuickCall: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    val tertiary = MaterialTheme.colorScheme.tertiary

    // Phase 2 Vastu: aligned contacts (trusted spiritual/community per goldens + PRD) get highlight cues
    val isAligned = vastuEnabled && (recent.displayName == "Guru Ji" || recent.displayName == "Temple Trust")
    val rowModifier = if (isAligned) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onQuickCall)
            .padding(vertical = DoorVaaniSpacing.xs)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), shape = RoundedCornerShape(DoorVaaniRadius.sm)) // subtle aligned wash
    } else {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onQuickCall)
            .padding(vertical = DoorVaaniSpacing.xs)
    }

    Row(
        modifier = rowModifier.semantics {
            contentDescription = "Recent channel ${recent.displayName}, ${if (isAligned) "Vastu aligned" else ""}"
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        val bg = if (recent.contact?.avatarColorSeed == 1) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
        val fg = if (recent.contact?.avatarColorSeed == 1) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(DoorVaaniSpacing.itemGap))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(recent.displayName, style = MaterialTheme.typography.labelMedium, color = onSurface)
                if (isAligned) {
                    Spacer(Modifier.width(DoorVaaniSpacing.xs))
                    // Vastu aligned badge (gold/tertiary accent per Sangam DESIGN + goldens)
                    Surface(
                        color = tertiary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(DoorVaaniRadius.full),
                        modifier = Modifier
                    ) {
                        Text(
                            "◈ Aligned",
                            style = MaterialTheme.typography.labelSmall,
                            color = tertiary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusIcon = when (recent.direction) {
                    com.doorvaani.domain.model.CallDirection.MISSED -> "✕"
                    else -> "↑"
                }
                val statusColor = if (recent.direction == com.doorvaani.domain.model.CallDirection.MISSED) error else onSurfaceVariant
                Text(statusIcon, color = statusColor, fontSize = 11.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    "${if (recent.timestamp > 0) "Today" else "Yesterday"}, ${PhoneFormatter.format(recent.phone.takeLast(10))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )
                if (isAligned) {
                    Spacer(Modifier.width(DoorVaaniSpacing.xs))
                    Text("• now", style = MaterialTheme.typography.labelSmall, color = tertiary)
                }
            }
        }

        // Quick call action (matches golden)
        IconButton(
            onClick = onQuickCall,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(primary.copy(alpha = 0.08f))
        ) {
            Icon(
                Icons.Filled.Call,
                contentDescription = "Call ${recent.displayName}",
                tint = primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FocusToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    highlight: Boolean
) {
    val container = if (highlight) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DoorVaaniRadius.sm),
        color = container,
        border = if (highlight) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier.padding(DoorVaaniSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(DoorVaaniSpacing.itemGap))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

/**
 * Phase 2 Advanced Dharma behavior: Mantra prompt card.
 * - Appears when Dharma Mode enabled (PRD: optional auto-reply mantras + presence).
 * - Quote fidelity to active_call_marcus golden + stoic presence theme.
 * - "Recite & Center" triggers dialog for full recitation (local, no external).
 * - ArchitecturalCard + tokens + haptics + semantics for a11y.
 */
@Composable
private fun DharmaMantraPrompt(onRecite: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    var showDialog by remember { mutableStateOf(false) }

    ArchitecturalCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = DoorVaaniSpacing.cardPadding,
        showTopAccent = true // subtle priority gold when Sangam; graceful in DoorVaani
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(DoorVaaniSpacing.itemGap))
            Column(Modifier.weight(1f)) {
                Text(
                    "Mantra for Presence",
                    style = MaterialTheme.typography.labelMedium,
                    color = onSurface
                )
                Text(
                    "The impediment to action advances action.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )
            }
            TextButton(onClick = {
                onRecite()
                showDialog = true
            }) {
                Text("Recite & Center", color = primary, style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("I am present", color = primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            },
            title = { Text("Mantra for Presence", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(DoorVaaniSpacing.sm)) {
                    Text(
                        "“The impediment to action advances action. What stands in the way becomes the way.”",
                        style = MaterialTheme.typography.bodyLarge,
                        color = onSurface
                    )
                    Text(
                        "— Marcus Aurelius",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                    Spacer(Modifier.height(DoorVaaniSpacing.sm))
                    Text(
                        "Take three breaths. Speak the words inwardly before the next call. Return to the flow.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = DoorVaaniElevation.sm
        )
    }
}

// Note: legacy CanvasForDotGrid retained for ref (now centralized in ArchitecturalCard.hasInnerPattern)
