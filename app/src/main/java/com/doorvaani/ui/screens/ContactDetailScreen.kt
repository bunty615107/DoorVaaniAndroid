package com.doorvaani.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.doorvaani.R
import com.doorvaani.ui.components.ArchitecturalCard
import com.doorvaani.ui.components.ShaderBackground
import com.doorvaani.ui.theme.LocalDoorVaaniTheme

/**
 * Rich Contact Details stub (Phase 2 per PRD).
 * Target: contact_details_* goldens (photo, notes, call stats, "Saved Mantra").
 * Stub for now: avatar, name, role, quick actions, sample stats, mantra note.
 * Wire real data + rich UI (photo, history list) in full Phase 2.
 */
@Composable
fun ContactDetailScreen(
    contactId: String,
    modifier: Modifier = Modifier
) {
    val isSangam = LocalDoorVaaniTheme.current == "Sangam"

    Box(modifier = modifier.fillMaxSize()) {
        ShaderBackground(isSangam = isSangam, opacity = 0.04f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Avatar stub (large per golden)
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(120.dp)
            ) {}

            Spacer(Modifier.height(16.dp))

            Text("Guru Ji", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.mobile) + " • +91 98765 43210", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(24.dp))

            // Quick actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {}) { Icon(Icons.Filled.Call, contentDescription = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.call)) }
                OutlinedButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.message)) }
            }

            Spacer(Modifier.height(24.dp))

            // Sample stats + notes (Phase 2 rich: call history, saved mantra)
            ArchitecturalCard(modifier = Modifier.fillMaxWidth(), showTopAccent = true) {
                Text(stringResource(R.string.call_stats), style = MaterialTheme.typography.titleMedium)
                Text("12 calls • Last: Today 08:30 • Avg duration: 8m")
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.notes) + ": Prefers morning calls. Favorite mantra: 'Om Shanti'.", style = MaterialTheme.typography.bodySmall)
                // Phase 3: AI insight for contact (stub from generator)
                Text("AI: Theme - spiritual guidance. Suggested: ॐ शान्ति (premium local stub)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(12.dp))

            ArchitecturalCard(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.saved_mantra), style = MaterialTheme.typography.titleMedium)
                Text("\"May all beings be happy and free.\" — Tap to insert during call (future).")
            }

            Spacer(Modifier.weight(1f))

            Text("Rich details + photo + full history (extensible)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}