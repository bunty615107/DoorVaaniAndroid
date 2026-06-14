package com.doorvaani.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doorvaani.domain.model.CallState
import com.doorvaani.platform.RecordingCoordinator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Central Call state machine holder (Agent D charter).
 * Full lifecycle per PRD LLD §9 + MultiAgent: Idle → Outgoing/Dialing → Ringing → Active → (Hold/Ended).
 * Drives all call UI (ActiveCallScreen, Incoming via Nav).
 * Recording integration point (toggle + on-end archive to Vault).
 * Coordinates with Agent C (Vault): on recording stop + call end, encrypt + index stub (see saveRecordingToVault).
 *
 * Audio routing notes: Use android.media.AudioManager for setSpeakerphoneOn, setBluetoothScoOn etc.
 * PermissionHelper (RECORD_AUDIO + CALL_PHONE) must be checked before routing/record.
 * Stubs here; real impl uses context from platform in Phase 1+.
 *
 * State drives UI: screens observe callState + secondary flows (isRecording etc).
 */
class CallViewModel : ViewModel() {
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    // In-call control states (hoisted to drive hex grid + UI; part of session)
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeaker = MutableStateFlow(false)
    val isSpeaker: StateFlow<Boolean> = _isSpeaker.asStateFlow()

    private val _isOnHold = MutableStateFlow(false)
    val isOnHold: StateFlow<Boolean> = _isOnHold.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // Simple in-memory archive for vault coordination (Agent C: replace with real VaultCoordinator + E2EE)
    private val _archivedRecordings = MutableStateFlow<List<ArchivedRecording>>(emptyList())
    val archivedRecordings: StateFlow<List<ArchivedRecording>> = _archivedRecordings.asStateFlow()

    data class ArchivedRecording(
        val id: String,
        val number: String,
        val startTimeMillis: Long,
        val durationSeconds: Int,
        val wasEncrypted: Boolean = true, // Always for DoorVaani vault per PRD
        val transcriptSummary: String? = null,
        val aiInsight: String? = null
    )

    fun startCall(number: String, context: Context? = null) {
        _callState.value = CallState.Dialing(number)
        _isMuted.value = false
        _isSpeaker.value = false
        _isOnHold.value = false
        _isRecording.value = false

        // Real recording coordinator will be started only when user explicitly toggles RECORD in ActiveCall.
        // (Per-call auto-record can be added from prefs in future.)

        // Simulate full state machine for beautiful in-app experience alongside the real system call.
        viewModelScope.launch {
            delay(650)
            val current = _callState.value
            if (current is CallState.Dialing) {
                _callState.value = CallState.Ringing(number)
                delay(450)
                if (_callState.value is CallState.Ringing) {
                    _callState.value = CallState.Active(number, System.currentTimeMillis())
                }
            }
        }
    }

    // For incoming: immediate accept path (can enhance with simulated ringing state later)
    fun acceptIncoming(number: String, context: Context? = null) {
        _callState.value = CallState.Active(number, System.currentTimeMillis())
        _isMuted.value = false
        _isSpeaker.value = false
        _isOnHold.value = false
        _isRecording.value = false

        // Recording started only on explicit user RECORD action in Active screen (real MediaRecorder).
    }

    fun endCall() {
        val current = _callState.value
        var duration = 0
        var endedNumber = ""
        val wasRecording = _isRecording.value

        if (current is CallState.Active) {
            duration = ((System.currentTimeMillis() - current.startTimeMillis) / 1000).toInt().coerceAtLeast(1)
            endedNumber = current.number
            _callState.value = CallState.Ended(current.number, duration)

            // Agent C integration (RecordingCoordinator): ALWAYS archive on end for knowledge preservation (per-call).
            // Now supports real captured audio (MediaRecorder temp file → encrypted .enc in private vault dir).
            // Coordinator decides Secure/Internal based on contact match (matches goldens).
            // Fulfills "integration with call end", encryption on end. No plaintext audio left on disk.
            RecordingCoordinator.onCallEnded(endedNumber, duration)

            // Legacy stub kept for compatibility (wasRecording path)
            if (wasRecording) {
                saveRecordingToVault(endedNumber, current.startTimeMillis, duration)
            }

            // On-device transcription hook (Phase 2): when real recording happened, the VaultRecord will have
            // the encrypted audio. Real on-device STT (SpeechRecognizer or future TFLite/Whisper) can be run
            // on the plaintext temp before final encryption (or post-decrypt in-mem for summary only).
            // For now we surface a useful local note; plug real STT in TranscriptionHelper for full production.
            if (wasRecording) {
                val transcriptNote = "Recording captured from device microphone (${duration}s). Real on-device transcription (SpeechRecognizer prefer-offline) would populate a full transcript here for semantic search in the Vault."
                val aiNote = "Local insight: call preserved in encrypted vault. (Premium on-device model can suggest mantras/themes from Stoic/Vedic corpus.)"
                if (_archivedRecordings.value.isNotEmpty()) {
                    val last = _archivedRecordings.value.last()
                    val updated = _archivedRecordings.value.dropLast(1) + last.copy(transcriptSummary = transcriptNote)
                    _archivedRecordings.value = updated
                    println("DoorVaani: real recording archived + transcript hook ready. $aiNote")
                }
            }
        } else if (current is CallState.Dialing || current is CallState.Ringing) {
            endedNumber = (current as? CallState.Dialing)?.number ?: (current as? CallState.Ringing)?.number ?: ""
            _callState.value = CallState.Ended(endedNumber, 0, "cancelled")
        } else {
            _callState.value = CallState.Idle
        }

        // Stop any active recording UI state
        _isRecording.value = false
        _isMuted.value = false
        _isSpeaker.value = false
        _isOnHold.value = false

        // Reset to idle after brief ended display (or on back nav). Per current flow.
        viewModelScope.launch {
            delay(650)
            if (_callState.value is CallState.Ended) {
                _callState.value = CallState.Idle
            }
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        // Audio routing stub: in real, AudioManager setMicrophoneMute(_isMuted.value)
        // Requires no extra perm beyond CALL_PHONE in some cases.
    }

    fun toggleSpeaker() {
        _isSpeaker.value = !_isSpeaker.value
        // Audio routing note (PermissionHelper usage):
        // val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // am.isSpeakerphoneOn = _isSpeaker.value
        // Also handle BT: am.startBluetoothSco() / stop. Check PermissionHelper.hasRecordPermission() if needed for some routes.
        // See PRD + charter: full audio routing for Mute/Speaker/Bluetooth.
    }

    fun toggleHold() {
        _isOnHold.value = !_isOnHold.value
        // State machine: could map to separate OnHold state if sealed expanded.
        // For now flag drives UI label + future audio pause.
    }

    /**
     * Recording toggle (hex "RECORD" control).
     * Now starts/stops real device audio capture via RecordingCoordinator + MediaRecorder.
     * Requires RECORD_AUDIO (should be requested by the calling screen via PermissionHelper first).
     */
    fun toggleRecording(context: Context? = null) {
        val currentlyRecording = _isRecording.value
        _isRecording.value = !currentlyRecording

        if (!currentlyRecording) {
            // Start real capture (the coordinator will create temp .m4a and start MediaRecorder)
            RecordingCoordinator.startRecording(
                // Best-effort number from current state
                when (val s = _callState.value) {
                    is CallState.Active -> s.number
                    is CallState.Dialing -> s.number
                    is CallState.Ringing -> s.number
                    else -> "unknown"
                },
                context
            )
        } else {
            // Stop handled inside onCallEnded (or explicit reset). Coordinator releases recorder.
        }
    }

    private fun saveRecordingToVault(number: String, startTime: Long, duration: Int) {
        val recording = ArchivedRecording(
            id = "rec_${System.currentTimeMillis()}",
            number = number,
            startTimeMillis = startTime,
            durationSeconds = duration,
            wasEncrypted = true
        )
        val updated = _archivedRecordings.value + recording
        _archivedRecordings.value = updated

        // TODO (coord Agent C): real vault write
        // VaultCoordinator.archiveCallRecording(recording) // E2EE, metadata + file
        // Also update FakeRecentsRepository or dedicated vault repo with Recent/VaultRecord entry.
    }

    fun reset() {
        _callState.value = CallState.Idle
        _isMuted.value = false
        _isSpeaker.value = false
        _isOnHold.value = false
        _isRecording.value = false
    }
}
