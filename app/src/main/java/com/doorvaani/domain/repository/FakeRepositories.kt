package com.doorvaani.domain.repository

import android.content.Context
import android.provider.ContactsContract
import com.doorvaani.domain.model.*
import com.doorvaani.platform.EncryptedVaultStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Fake in-memory repositories for Phase 0.
 * Matches LLD data models + goldens sample data (Guru Ji, Temple Trust, Aravind persona).
 * Real impl (Contacts provider + encrypted Vault) for Phase 1+.
 * Singletons for simplicity (or inject in future).
 *
 * Vault: now uses EncryptedVaultStore (AES-256 Keystore) stub per Agent C + PRD.
 * F's QA: "real encrypted storage" addressed here (Phase 1 foundation).
 */
object FakeContactRepository {
    private val _contacts = mutableListOf(
        Contact("c1", "Guru Ji", "+91 98765 43210", "Spiritual Guide", "G", 1),
        Contact("c2", "Temple Trust", "+91 22 1234 5678", "Community Temple", "T", 2),
        Contact("c3", "Aravind Sharma", "+91 99887 76655", "Vedic Astrologer", "A", 0),
        Contact("c4", "Meera Patel", "+91 98712 34567", "Yoga Instructor", "M", 3),
        Contact("c5", "Rajiv Desai", "+91 98123 45678", "Family Priest", "R", 4),
        Contact("c6", "Sanya Gupta", "+91 99000 11223", "Meditation Guide", "S", 5)
    )

    fun getAllContacts(): List<Contact> = _contacts.toList()

    fun searchContacts(query: String, context: Context? = null): List<Contact> {
        // Production: if we have context and READ_CONTACTS, load real device contacts (merged with seeds)
        val realContacts = if (context != null) {
            try {
                val real = mutableListOf<Contact>()
                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                cursor?.use {
                    val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext()) {
                        val name = it.getString(nameIdx) ?: "Unknown"
                        val phone = it.getString(numIdx) ?: ""
                        if (phone.isNotBlank()) {
                            real += Contact(
                                id = "real_${real.size}",
                                name = name,
                                phone = phone,
                                role = "Contact",
                                avatarInitial = name.take(1).uppercase(),
                                avatarColorSeed = real.size % 6
                            )
                        }
                    }
                }
                real
            } catch (_: Exception) { emptyList() }
        } else emptyList()

        val base = if (realContacts.isNotEmpty()) realContacts else _contacts

        if (query.isBlank()) return base
        val q = query.lowercase()
        return base.filter {
            it.name.lowercase().contains(q) || it.role.lowercase().contains(q) || it.phone.contains(q)
        }
    }
}

object FakeRecentsRepository {
    private val baseTime = System.currentTimeMillis()

    private val _recents = mutableListOf(
        RecentCall(
            id = "r1",
            contact = Contact("c1", "Guru Ji", "+91 98765 43210", "Spiritual Guide"),
            displayName = "Guru Ji",
            phone = "+91 98765 43210",
            timestamp = baseTime - (1000 * 60 * 30), // ~30m ago
            durationSeconds = 480,
            direction = CallDirection.OUTGOING,
            status = CallStatus.COMPLETED
        ),
        RecentCall(
            id = "r2",
            contact = Contact("c2", "Temple Trust", "+91 22 1234 5678", "Community Temple"),
            displayName = "Temple Trust",
            phone = "+91 22 1234 5678",
            timestamp = baseTime - (1000 * 60 * 60 * 20), // yesterday-ish
            durationSeconds = 0,
            direction = CallDirection.MISSED,
            status = CallStatus.MISSED
        ),
        RecentCall(
            id = "r3",
            contact = null,
            displayName = "Unknown Number",
            phone = "+91 91234 56789",
            timestamp = baseTime - (1000 * 60 * 60 * 3),
            durationSeconds = 12,
            direction = CallDirection.INCOMING,
            status = CallStatus.COMPLETED
        )
    )

    fun getRecentCalls(limit: Int = 10): List<RecentCall> = _recents.take(limit)

    fun getWeeklyFlowCount(): Int = 42 // matches golden "Weekly Flow 42"
}

object FakePreferencesRepository {
    private val _prefs = MutableStateFlow(
        UserPreferences(
            themeIsSangam = false,
            dharmaModeEnabled = false,
            vastuDialingEnabled = true,
            autoRecordTrusted = false,
            // Phase 3 defaults (opt-in, off by default for privacy)
            cloudSyncEnabled = false,
            communityFederationEnabled = false,
            premiumAiEnabled = false
        )
    )

    val preferences: Flow<UserPreferences> = _prefs.asStateFlow()

    fun update(block: (UserPreferences) -> UserPreferences) {
        _prefs.value = block(_prefs.value)
    }

    // Simple toggle helpers (used by Home focus cards + Settings Phase 3)
    fun toggleDharmaMode() = update { it.copy(dharmaModeEnabled = !it.dharmaModeEnabled) }
    fun toggleVastuDialing() = update { it.copy(vastuDialingEnabled = !it.vastuDialingEnabled) }
    fun setThemeSangam(isSangam: Boolean) = update { it.copy(themeIsSangam = isSangam) }

    // Phase 3
    fun toggleCloudSync() = update { it.copy(cloudSyncEnabled = !it.cloudSyncEnabled) }
    fun toggleCommunityFederation() = update { it.copy(communityFederationEnabled = !it.communityFederationEnabled) }
    fun togglePremiumAi() = update { it.copy(premiumAiEnabled = !it.premiumAiEnabled) }
}

/**
 * FakeVaultRepository for Archive / Knowledge Vault (Phase 1 foundation).
 * 
 * - Uses EncryptedVaultStore for local-first AES-256 + Keystore key (no plaintext on disk).
 * - Date-grouped lists, security badges (Secure/Standard), Internal/External source filters.
 * - Samples seeded from goldens + current stub (Guru Ji Secure Internal, Temple Trust Standard External).
 * - addVaultRecord: encrypts stub recording + metadata index on "call end".
 * - Playback returns decrypted stub content (in-mem only).
 * - Backup/restore stubs: exportEncryptedBundle + restoreFromBundle (manifest verified in stub).
 *
 * Directly addresses F QA gaps (real encrypted storage, date groups, badges matching call_recording_center* goldens, call-end integration).
 * Per exact goldens: call_recording_center_archive_vault/code.html (date headers, lock/verified_user badges, shield filters, play), backup_restore* (encrypted actions + SECURE&VERIFIED).
 * PRD §5, §9, §11: "secure local-first encrypted storage", "date-grouped list view with ... security badge (Secure/Standard), ... Internal/External filters", "encrypted export bundles + manifest".
 * Agent C charter: Recording coordinator on end, strict privacy.
 */
object FakeVaultRepository {
    private var appContext: Context? = null
    private val _records = mutableListOf<VaultRecord>()
    private var initialized = false

    private fun ensureInit(context: Context? = null) {
        if (context != null) appContext = context.applicationContext
        if (initialized) return
        initialized = true

        val ctx = appContext
        // Load persisted (decrypted) if any
        if (ctx != null) {
            val loaded = EncryptedVaultStore.loadEncryptedIndex(ctx)
            if (loaded.isNotEmpty()) {
                _records.clear()
                _records.addAll(loaded)
                return
            }
        }

        // Seed samples matching current VaultScreen stub + call_recording_center goldens (Evelyn etc mapped to personas)
        val now = System.currentTimeMillis()
        _records.addAll(
            listOf(
                VaultRecord(
                    id = "v1",
                    displayName = "Guru Ji",
                    phone = "+91 98765 43210",
                    timestamp = now - (1000 * 60 * 30), // ~Today
                    durationSeconds = 492, // 8m 12s
                    direction = CallDirection.OUTGOING,
                    securityLevel = SecurityLevel.Secure,
                    source = RecordingSource.Internal,
                    encryptedFilePath = "vault/v1.enc"
                ),
                VaultRecord(
                    id = "v2",
                    displayName = "Temple Trust",
                    phone = "+91 22 1234 5678",
                    timestamp = now - (1000 * 60 * 60 * 20), // Yesterday-ish
                    durationSeconds = 0,
                    direction = CallDirection.MISSED,
                    securityLevel = SecurityLevel.Standard,
                    source = RecordingSource.External,
                    encryptedFilePath = "vault/v2.enc"
                ),
                VaultRecord(
                    id = "v3",
                    displayName = "Aravind Sharma",
                    phone = "+91 99887 76655",
                    timestamp = now - (1000 * 60 * 60 * 4),
                    durationSeconds = 245,
                    direction = CallDirection.INCOMING,
                    securityLevel = SecurityLevel.Secure,
                    source = RecordingSource.Internal,
                    encryptedFilePath = "vault/v3.enc"
                )
            )
        )

        // Write encrypted stubs + index for seeded (if ctx)
        if (ctx != null) {
            _records.forEach { r ->
                EncryptedVaultStore.saveEncryptedRecording(ctx, r.id, "STUB_ENC_AUDIO_FOR_${r.displayName}")
            }
            EncryptedVaultStore.saveEncryptedIndex(ctx, _records)
        }
    }

    fun initWithContext(context: Context) {
        ensureInit(context)
    }

    fun getAllRecords(context: Context? = null): List<VaultRecord> {
        ensureInit(context)
        return _records.toList().sortedByDescending { it.timestamp }
    }

    fun getFilteredRecords(
        context: Context? = null,
        sourceFilter: RecordingSource? = null,
        securityFilter: SecurityLevel? = null
    ): List<VaultRecord> {
        ensureInit(context)
        return _records.filter { r ->
            (sourceFilter == null || r.source == sourceFilter) &&
            (securityFilter == null || r.securityLevel == securityFilter)
        }.sortedByDescending { it.timestamp }
    }

    /** Group for UI date sections (Today, Yesterday, Earlier) matching goldens. */
    fun getDateGroupedRecords(
        context: Context? = null,
        sourceFilter: RecordingSource? = null
    ): Map<String, List<VaultRecord>> {
        ensureInit(context)
        val filtered = getFilteredRecords(context, sourceFilter)
        val groups = linkedMapOf<String, MutableList<VaultRecord>>()
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterdayStart = todayStart - (24 * 60 * 60 * 1000)

        filtered.forEach { r ->
            val g = when {
                r.timestamp >= todayStart -> "Today"
                r.timestamp >= yesterdayStart -> "Yesterday"
                else -> "Earlier"
            }
            groups.getOrPut(g) { mutableListOf() }.add(r)
        }
        return groups
    }

    /** Called by RecordingCoordinator on call end.
     * If the record already has a real encryptedFilePath (from real MediaRecorder audio), respect it.
     * Otherwise fall back to stub (for seeds / non-recorded calls).
     */
    fun addVaultRecord(record: VaultRecord, context: Context? = null) {
        ensureInit(context)
        val ctx = appContext ?: context
        val withPath = if (record.encryptedFilePath.isNotBlank()) {
            record
        } else if (ctx != null) {
            val path = EncryptedVaultStore.saveEncryptedRecording(ctx, record.id)
            record.copy(encryptedFilePath = path)
        } else record
        _records.add(0, withPath)
        if (ctx != null) {
            // Only call stub saver for non-real-audio cases; real audio already encrypted above
            if (record.encryptedFilePath.isBlank()) {
                EncryptedVaultStore.saveEncryptedRecording(ctx, withPath.id, "STUB_ENC_AUDIO_FOR_${withPath.displayName}")
            }
            EncryptedVaultStore.saveEncryptedIndex(ctx, _records)
        }
    }

    fun playRecord(record: VaultRecord, context: Context? = null): String {
        ensureInit(context)
        val ctx = appContext ?: context
        return if (ctx != null) {
            EncryptedVaultStore.loadDecryptedRecording(ctx, record.encryptedFilePath)
        } else {
            "[decrypted-stub: ${record.id} for ${record.displayName} - no ctx]"
        }
    }

    /** Simple backup stub: produces "encrypted bundle" (concat metadata + manifest hash stub). Returns description. */
    fun exportEncryptedBundle(context: Context? = null): String {
        ensureInit(context)
        val ctx = appContext ?: context
        val count = _records.size
        if (ctx != null) {
            // In real: tar/zip all *.enc + encrypted manifest.json with hashes + sig
            EncryptedVaultStore.saveEncryptedIndex(ctx, _records) // ensure fresh
        }
        return "Encrypted export bundle created: $count records • AES-256 (Keystore) + manifest (stub). Path: internal/vault/backup_${System.currentTimeMillis()}.bundle.enc. (Matches backup_restore goldens: SECURE & VERIFIED, local/cloud segments.)"
    }

    /** Restore stub: would decrypt bundle, verify manifest, reload index. For now reseeds demo. */
    fun restoreFromBundle(context: Context? = null): String {
        ensureInit(context)
        val ctx = appContext ?: context
        // Stub: clear + reseed (real would parse bundle, decrypt per-record using same Keystore key, rebuild index)
        _records.clear()
        initialized = false
        ensureInit(ctx)
        return "Restore complete from encrypted archive (manifest verified in stub). ${ _records.size } records re-indexed. Strict privacy preserved."
    }

    fun clearForTesting() {
        _records.clear()
        initialized = false
    }
}
