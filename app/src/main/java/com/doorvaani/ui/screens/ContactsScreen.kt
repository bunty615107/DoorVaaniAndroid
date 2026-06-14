package com.doorvaani.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.doorvaani.R
import com.doorvaani.domain.model.Contact
import com.doorvaani.domain.repository.FakeContactRepository
import com.doorvaani.platform.rememberPermissionHelper
import com.doorvaani.ui.components.MandalaBackground
import com.doorvaani.utils.HapticsHelper

/**
 * Contacts screen bento grid matching stitch_opendialer_sangam_design_system/contacts_doorvaani/ golden.
 * Searchable, roles ("Vedic Astrologer" etc), circular initial avatars, Call + Message actions.
 * Uses bento cards with subtle watermark (spa icon).
 * Quick call wires to nav + CallState.
 */
@Composable
fun ContactsScreen(
    onCallContact: (Contact) -> Unit,
    onMessageContact: (Contact) -> Unit,
    onOpenDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val permissionHelper = rememberPermissionHelper()
    var searchQuery by remember { mutableStateOf("") }

    val contacts = remember(searchQuery, context) {
        FakeContactRepository.searchContacts(searchQuery, context)
    }

    val primary = MaterialTheme.colorScheme.primary
    val surfaceContainerLowest = MaterialTheme.colorScheme.surfaceContainerLowest
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(Modifier.fillMaxSize()) {
        MandalaBackground(primaryColor = primary, opacity = 0.03f, modifier = Modifier.fillMaxSize())

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 80.dp) // space for bottom nav
        ) {
            // Header
            Text(stringResource(R.string.contacts_title), style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp), color = primary)
            Text(
                stringResource(R.string.contacts_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            // Search (matches golden recessed + filter)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = surfaceContainerLowest,
                border = BorderStroke(1.dp, outlineVariant.copy(0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = outlineVariant)
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = onSurface),
                        cursorBrush = SolidColor(primary),
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(stringResource(R.string.search_contacts), color = onSurfaceVariant)
                            }
                            inner()
                        }
                    )
                    IconButton(onClick = { /* filter stub */ }) {
                        Text("≡", color = onSurfaceVariant) // filter_list proxy
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bento grid of ContactCards
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(contacts, key = { it.id }) { contact ->
                    ContactBentoCard(
                        contact = contact,
                        onCall = {
                            // Production real call + permission at screen scope (has context + helper)
                            permissionHelper.requestCallPermission { granted ->
                                if (granted) {
                                    try {
                                        val intent = Intent(Intent.ACTION_CALL).apply {
                                            data = Uri.parse("tel:${contact.phone}")
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                                HapticsHelper.actionConfirm(haptic)
                                onCallContact(contact)
                            }
                        },
                        onMessage = {
                            HapticsHelper.toggle(haptic)
                            onMessageContact(contact)
                        },
                        onDetails = { onOpenDetail(contact.id) },
                        modifier = Modifier.semantics { contentDescription = "Contact card for ${contact.name}, role ${contact.role}" }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactBentoCard(
    contact: Contact,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onDetails: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val surfaceContainerLowest = MaterialTheme.colorScheme.surfaceContainerLowest
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Choose bg tint by seed
    val avatarBg = when (contact.avatarColorSeed % 4) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.tertiaryContainer
        2 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val avatarFg = when (contact.avatarColorSeed % 4) {
        0 -> MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.onTertiaryContainer
        2 -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDetails() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceContainerLowest),
        border = BorderStroke(1.dp, outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            // Avatar + name/role + subtle watermark
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        contact.avatarInitial,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = avatarFg,
                        modifier = Modifier.semantics { contentDescription = "Avatar for ${contact.name}" }
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(contact.name, style = MaterialTheme.typography.titleLarge, color = onSurface)
                    Text(contact.role, style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                }
                // Subtle decorative (spa proxy)
                Text("❀", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), fontSize = 28.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Call + Message actions (bento buttons)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.call), style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onMessage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.message), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
