package com.doorvaani.platform

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.doorvaani.domain.model.*
import com.doorvaani.domain.repository.FakeContactRepository
import com.doorvaani.domain.repository.FakeVaultRepository
import java.io.File
import java.util.*

/**
 * RecordingCoordinator (Agent C charter).
 * - start on call (stub flag + potential MediaRecorder hook for D).
 * - On call end: encrypt metadata + stub file via EncryptedVaultStore, index in vault.
 * - Determines securityLevel (Secure if known trusted contact/Internal) + source.
 * - Strict privacy: everything through EncryptedVaultStore (Keystore AES-256). No plaintext.
 *
 * Integration: Called from CallViewModel.endCall (Phase 1 foundation). 
 * Coordinate with Agent D (Telephony/Call Lifecycle) for real audio capture start in ActiveCall, background service, etc.
 * Per PRD: "Recording coordinator (start on call, encrypt + metadata index on end)", "Vault write: Always local-first AES-256 + key in enclave."
 *
 * References F QA: integration with call end; goldens call_recording_center* for resulting badges/filters; backup flows use resulting records.
 */
object RecordingCoordinator {
    private var recordingActiveForCall: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentTempFile: File? = null
    private var currentContext: Context? = null

    /** Production-ready: start real device audio capture to a temp file using MediaRecorder.
     * Must have RECORD_AUDIO permission (caller should check via PermissionHelper before).
     * Uses VOICE_COMMUNICATION source for call-friendly capture where possible.
     */
    fun startRecording(number: String, context: Context?) {
        if (context == null) return
        recordingActiveForCall = number
        currentContext = context.applicationContext

        try {
            val safeNumber = number.replace(Regex("[^0-9a-zA-Z]"), "_")
            val temp = File(context.cacheDir, "doorvaani_rec_${safeNumber}_${System.currentTimeMillis()}.m4a")
            currentTempFile = temp

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128 * 1000)
                setAudioSamplingRate(44100)
                setOutputFile(temp.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
        } catch (e: Exception) {
            // Graceful: recording UI can still toggle; vault will get synthetic entry
            recordingActiveForCall = null
            currentTempFile = null
            mediaRecorder = null
            android.util.Log.e("RecordingCoordinator", "Failed to start real recorder: ${e.message}")
        }
    }

    private fun stopAndReleaseRecorder(): File? {
        val file = currentTempFile
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Ignore stop errors (e.g. nothing recorded yet)
        }
        mediaRecorder = null
        currentTempFile = null
        return file
    }

    /** Primary hook: from CallViewModel.endCall (after Ended state).
     * Now handles real recorded audio file if active: encrypts the actual bytes (no plaintext left on disk).
     */
    fun onCallEnded(
        number: String,
        durationSeconds: Int,
        context: Context? = null
    ) {
        val wasRecording = recordingActiveForCall == number
        recordingActiveForCall = null

        val recordedAudioFile = if (wasRecording) stopAndReleaseRecorder() else null

        // Resolve friendly name (prefers real contacts when available in repo)
        val contacts = FakeContactRepository.searchContacts(number)
        val contact = contacts.firstOrNull { it.phone == number || it.phone.contains(number.takeLast(5)) }
        val displayName = contact?.name ?: number

        val isKnownContact = contact != null
        val security = if (isKnownContact) SecurityLevel.Secure else SecurityLevel.Standard
        val source = if (isKnownContact) RecordingSource.Internal else RecordingSource.External
        val direction = CallDirection.OUTGOING

        var record = VaultRecord(
            id = "v-${UUID.randomUUID().toString().take(8)}",
            displayName = displayName,
            phone = number,
            timestamp = System.currentTimeMillis() - (durationSeconds * 1000L),
            durationSeconds = durationSeconds.coerceAtLeast(1),
            direction = direction,
            securityLevel = security,
            source = source,
            encryptedFilePath = ""
        )

        val ctx = context ?: currentContext

        if (recordedAudioFile != null && ctx != null && recordedAudioFile.exists()) {
            // Real production path: encrypt the captured audio bytes
            val encPath = EncryptedVaultStore.saveEncryptedAudio(ctx, record.id, recordedAudioFile)
            record = record.copy(encryptedFilePath = encPath)
        }

        // Persist (repo will skip stub encryption if path already populated)
        FakeVaultRepository.addVaultRecord(record, ctx)

        currentContext = null
    }

    fun isRecordingFor(number: String): Boolean = recordingActiveForCall == number

    fun reset() {
        stopAndReleaseRecorder()
        recordingActiveForCall = null
        currentContext = null
    }
}