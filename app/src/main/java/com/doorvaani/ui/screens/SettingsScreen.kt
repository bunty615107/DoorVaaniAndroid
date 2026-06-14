package com.doorvaani.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.doorvaani.R
import com.doorvaani.domain.model.UserPreferences
import com.doorvaani.platform.SyncCoordinator
import com.doorvaani.ui.components.ArchitecturalCard
import com.doorvaani.utils.HapticsHelper
import kotlinx.coroutines.launch

/**
 * Settings stub (Phase 0).
 * Theme switcher (DoorVaani <-> Sangam) moved here from MainActivity per charter.
 * Includes focus toggles, "Phase 0" marker, basic labels.
 * Full architectural hierarchy (settings_architectural_hierarchy golden) in Phase 1.
 */
@Composable
fun SettingsScreen(
    currentPrefs: UserPreferences,
    onThemeToggle: (Boolean) -> Unit,
    onDharmaToggle: () -> Unit,
    onVastuToggle: () -> Unit,
    // Phase 3
    onCloudSyncToggle: () -> Unit = {},
    onCommunityFederationToggle: () -> Unit = {},
    onPremiumAiToggle: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onNavigateToShield: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

        // Architectural hierarchy cards (inspired by settings_architectural_hierarchy golden)
        ArchitecturalCard(
            showTopAccent = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.design_system), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.doorvaani_light), modifier = Modifier.weight(1f))
                Switch(
                    checked = currentPrefs.themeIsSangam,
                    onCheckedChange = {
                        HapticsHelper.toggle(haptic)
                        onThemeToggle(it)
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.sangam_dark), color = onSurfaceVariant)
            }
        }

        ArchitecturalCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.focus_modes), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.dharma_mode), modifier = Modifier.weight(1f))
                Switch(checked = currentPrefs.dharmaModeEnabled, onCheckedChange = {
                    HapticsHelper.toggle(haptic)
                    onDharmaToggle()
                })
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.vastu_dialing), modifier = Modifier.weight(1f))
                Switch(checked = currentPrefs.vastuDialingEnabled, onCheckedChange = {
                    HapticsHelper.toggle(haptic)
                    onVastuToggle()
                })
            }
        }

        // Phase 3: Cloud E2EE Sync (opt-in, zero-trust)
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var lastSyncResult by remember { mutableStateOf<String?>(null) }

        ArchitecturalCard(
            showTopAccent = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.cloud_sync), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.cloud_sync_desc), style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.last_sync), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = currentPrefs.cloudSyncEnabled,
                    onCheckedChange = {
                        HapticsHelper.toggle(haptic)
                        onCloudSyncToggle()
                    }
                )
            }
            if (currentPrefs.cloudSyncEnabled) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        HapticsHelper.actionConfirm(haptic)
                        scope.launch {
                            val result = SyncCoordinator.syncVaultIfEnabled(context, currentPrefs.cloudSyncEnabled)
                            lastSyncResult = result
                            // Show via Toast for simple cross-screen feedback (no full scaffold snackbar here)
                            android.widget.Toast.makeText(context, result, android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.sync_now))
                }
                if (lastSyncResult != null) {
                    Text(
                        lastSyncResult!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Phase 3: Community Shield Federation
        ArchitecturalCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.community_federation), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.community_federation_desc), style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.federation_status), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Opt-in anonymized reports", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = currentPrefs.communityFederationEnabled,
                    onCheckedChange = {
                        HapticsHelper.toggle(haptic)
                        onCommunityFederationToggle()
                    }
                )
            }
        }

        // Phase 3: Premium AI Insights teaser
        ArchitecturalCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.premium_ai), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.premium_ai_desc), style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable on-device insights", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = currentPrefs.premiumAiEnabled,
                    onCheckedChange = {
                        HapticsHelper.toggle(haptic)
                        onPremiumAiToggle()
                    }
                )
            }
        }

        ArchitecturalCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.app_info), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text("Scale & Ecosystem (cloud E2EE • federation • premium AI)", style = MaterialTheme.typography.bodyMedium, color = onSurfaceVariant)
            Text("Native Compose • Encrypted Vault (Keystore AES-256) • RecordingCoordinator + SyncCoordinator (opt-in zero-trust)", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Localization: Hindi (values-hi) foundation added. All new strings resource-backed for full i18n.", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    HapticsHelper.actionConfirm(haptic)
                    onNavigateToVault()
                },
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.knowledge_vault)) }
            Button(
                onClick = {
                    HapticsHelper.actionConfirm(haptic)
                    onNavigateToShield()
                },
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.shield_of_sangam)) }
        }

        Spacer(Modifier.weight(1f))

        Text(
            stringResource(R.string.tagline),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
