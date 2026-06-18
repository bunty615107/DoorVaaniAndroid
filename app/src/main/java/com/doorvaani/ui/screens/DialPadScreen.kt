package com.doorvaani.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doorvaani.R
import com.doorvaani.platform.rememberPermissionHelper
import com.doorvaani.ui.components.MandalaBackground
import com.doorvaani.ui.theme.DoorVaaniSpacing
import com.doorvaani.ui.theme.LocalDoorVaaniTheme
import com.doorvaani.utils.HapticsHelper
import com.doorvaani.utils.PhoneFormatter

/**
 * Dial Pad screen inspired directly by
 * stitch_opendialer_sangam_design_system/dial_pad_doorvaani/code.html
 * and the PRD + DESIGN.md specifications.
 *
 * Features:
 * - Large number display with formatting
 * - 3x4 classic keypad with letter sublabels
 * - Prominent call FAB (Garbagriha centrality)
 * - Mandala shader-like background
 * - Theme aware (DoorVaani / Sangam)
 */
@Composable
fun DialPadScreen(
    onCall: (String) -> Unit = {},
    onAddContact: (String) -> Unit = {},
    initialNumber: String = "",
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentNumber by remember { mutableStateOf(initialNumber) }
    val haptic = LocalHapticFeedback.current
    val isSangam = LocalDoorVaaniTheme.current == "Sangam"
    val context = LocalContext.current
    val permissionHelper = rememberPermissionHelper()
    val backspaceDesc = stringResource(R.string.backspace)

    Box(modifier = modifier.fillMaxSize()) {
        // Use existing MandalaBackground (compatible; high fidelity ShaderBackground in future graphics/)
        MandalaBackground(
            primaryColor = MaterialTheme.colorScheme.primary,
            opacity = 0.09f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Number display (large, elegant)
            Text(
                text = PhoneFormatter.format(currentNumber),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 1,
                maxLines = 1
            )

            if (currentNumber.isNotEmpty()) {
                TextButton(onClick = { onAddContact(currentNumber) }) {
                    Text(stringResource(R.string.add_to_contacts), color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                Spacer(Modifier.height(32.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Keypad - 3x4 grid
            val keys = listOf(
                listOf("1", "", "2", "ABC", "3", "DEF"),
                listOf("4", "GHI", "5", "JKL", "6", "MNO"),
                listOf("7", "PQRS", "8", "TUV", "9", "WXYZ"),
                listOf("*", "", "0", "+", "#", "")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(DoorVaaniSpacing.keyRowGap),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in 0 until 4) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DoorVaaniSpacing.keyGap)
                    ) {
                        for (col in 0 until 3) {
                            val idx = col * 2
                            val main = keys[row][idx]
                            val sub = keys[row][idx + 1]

                            DialKey(
                                main = main,
                                sub = sub,
                                onClick = {
                                    HapticsHelper.keyPress(haptic)
                                    if (currentNumber.length < 15) {
                                        currentNumber += main
                                    }
                                }
                            )  // Note: add contentDescription inside DialKey for full a11y if needed
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Action row: Call FAB (central, Garbagriha) + backspace
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Spacer to balance
                Spacer(Modifier.size(48.dp))

                // Big Call Button (Garbagriha)
                FloatingActionButton(
                    onClick = {
                        if (currentNumber.isNotEmpty()) {
                            HapticsHelper.actionConfirm(haptic)
                            // Production: request CALL_PHONE then launch real system call intent.
                            // We still invoke onCall(...) so our beautiful ActiveCallScreen + state machine + optional recording UI runs alongside.
                            permissionHelper.requestCallPermission { granted ->
                                if (granted) {
                                    try {
                                        val intent = Intent(Intent.ACTION_CALL).apply {
                                            data = Uri.parse("tel:${Uri.encode(currentNumber)}")
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        // Fallback (no dialer or permission edge): still drive our in-app experience
                                    }
                                }
                                // Always drive our DoorVaani call UI / recording / vault flow (even if real dial not possible)
                                onCall(currentNumber)
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Text(stringResource(R.string.call_button), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // Backspace
                if (currentNumber.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            HapticsHelper.keyPress(haptic)
                            if (currentNumber.isNotEmpty()) currentNumber =
                                currentNumber.dropLast(1)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("⌫", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.semantics { contentDescription = backspaceDesc })
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
private fun DialKey(
    main: String,
    sub: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Exact fidelity to dial_pad_doorvaani goldens + code.html:
    // w-20 h-20 rounded-full, soft shadow[0_-4px_20px_rgba(255,179,71,0.08)], border subtle
    // press: scale(0.9) + primary-container tint bg
    // Primary color numerals, on-surface-variant letters at caption size
    // Uses 8px rhythm spacing tokens indirectly via parent grid
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isPressed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
                else MaterialTheme.colorScheme.surface,
        shadowElevation = if (isPressed) 1.dp else 2.dp,
        interactionSource = interactionSource,
        modifier = modifier
            .size(80.dp)  // w-20 exact from mocks
            .clip(CircleShape)
            .scale(if (isPressed) 0.90f else 1f)
            .semantics { contentDescription = if (sub.isNotBlank()) "$main $sub" else main }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                main,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            if (sub.isNotEmpty()) {
                Spacer(Modifier.height(1.dp))
                Text(
                    sub,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }
    }
}

private fun formatPhoneNumber(raw: String): String {
    if (raw.length <= 3) return raw
    if (raw.length <= 6) return "${raw.substring(0, 3)} ${raw.substring(3)}"
    return "${raw.substring(0, 3)} ${raw.substring(3, 6)} ${raw.substring(6)}"
}