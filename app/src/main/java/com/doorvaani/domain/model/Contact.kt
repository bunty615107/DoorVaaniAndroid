package com.doorvaani.domain.model

/**
 * Core Contact entity (LLD §9).
 * Adapted from PRD data models + stitch contacts_doorvaani/ cards.
 * Sample roles match "Spiritual Guide", "Vedic Astrologer" etc.
 */
data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val role: String = "",          // e.g. "Spiritual Guide", "Vedic Astrologer"
    val avatarInitial: String = name.take(1).uppercase(),
    val avatarColorSeed: Int = 0,   // For deterministic tint in UI (primary/tertiary container etc.)
    val lastContacted: Long? = null,
    val notes: String = ""
)

/**
 * Recent call / channel for Home "Recent Channels" and future recents list.
 * Direction + status for icons (call_made / call_missed per golden).
 */
data class RecentCall(
    val id: String,
    val contact: Contact? = null,   // null = unknown number
    val displayName: String,
    val phone: String,
    val timestamp: Long,            // epoch millis
    val durationSeconds: Int = 0,
    val direction: CallDirection,
    val status: CallStatus = CallStatus.COMPLETED
)

enum class CallDirection {
    OUTGOING, INCOMING, MISSED
}

enum class CallStatus {
    COMPLETED, MISSED, REJECTED
}

/**
 * User preferences (focus modes, theme). Bound to Home toggles.
 * Persist in Phase 1+. For now in-memory + simple store.
 * Phase 3 additions: cloud E2EE sync opt-in, community shield federation, premium AI insights (per PRD).
 */
data class UserPreferences(
    val themeIsSangam: Boolean = false,
    val dharmaModeEnabled: Boolean = false,
    val vastuDialingEnabled: Boolean = true,
    val autoRecordTrusted: Boolean = false,
    // Phase 3
    val cloudSyncEnabled: Boolean = false,
    val communityFederationEnabled: Boolean = false,
    val premiumAiEnabled: Boolean = false
)

/**
 * Call state machine (Agent D full lifecycle).
 * Per PRD LLD, MultiAgent charter, goldens: Idle → Outgoing/Dialing/Ringing → Active → Ended (or Hold substate via VM flags).
 * Drives Incoming (Ringing visual), Active duration/hex, End transitions + recording archive.
 * Extended from Phase 0 stub.
 */
sealed interface CallState {
    data object Idle : CallState
    data class Dialing(val number: String) : CallState   // Outgoing tone / platform dial
    data class Ringing(val number: String) : CallState   // Remote ringing (incoming or simulated)
    data class Active(val number: String, val startTimeMillis: Long) : CallState
    data class Ended(val number: String, val durationSeconds: Int, val reason: String = "") : CallState
}

/**
 * Vault / Archive Recording entity.
 * Per PRD LLD §9 + exact call_recording_center* goldens + backup_restore*:
 * - id, displayName (contact or number), timestamp, duration, direction
 * - securityLevel: Secure (lock icon + tertiary, top gold accent) | Standard (verified_user)
 * - source: Internal (trusted/known, shield filter) | External
 * - encryptedFilePath: points to AES-256 encrypted stub file (key in Android Keystore)
 * Strict local-first, E2EE, zero plaintext leakage (Phase 1 foundation per Agent C charter).
 * Matches samples: Guru Ji / Temple Trust, date-grouped, badges.
 */
data class VaultRecord(
    val id: String,
    val displayName: String,
    val phone: String? = null,
    val timestamp: Long,            // epoch millis for date grouping
    val durationSeconds: Int,
    val direction: CallDirection,
    val securityLevel: SecurityLevel,
    val source: RecordingSource,
    val encryptedFilePath: String,   // relative to internal vault dir; contents AES encrypted
    val aiInsight: String? = null,    // Phase 3 premium AI summary (local generator)
    val transcriptSummary: String? = null
)

enum class SecurityLevel {
    Secure,   // High-trust (e.g. known contact, Internal); uses lock + gold accent per golden
    Standard  // verified_user per call_recording_center_archive_vault/code.html
}

enum class RecordingSource {
    Internal,  // shield icon filter, trusted
    External
}
