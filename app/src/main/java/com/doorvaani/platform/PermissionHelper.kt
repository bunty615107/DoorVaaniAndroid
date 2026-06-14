package com.doorvaani.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Platform permission helper (pure Compose, no extra libs).
 * Declared in AndroidManifest (CALL_PHONE, READ_CONTACTS, RECORD_AUDIO).
 * Phase 0: runtime request on first use (Dial, Contacts quick call).
 * Graceful: show rationale + proceed with stubs if denied (no crash).
 *
 * Usage in screen:
 *   val permissionHelper = rememberPermissionHelper()
 *   permissionHelper.requestCallPermission { granted -> if (granted) doCall() }
 */
@Composable
fun rememberPermissionHelper(): PermissionHelper {
    val context = LocalContext.current
    var pendingOnResult by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        pendingOnResult?.invoke(allGranted)
        pendingOnResult = null
    }

    return remember {
        PermissionHelper(context, launcher, { pendingOnResult = it })
    }
}

class PermissionHelper(
    private val context: Context,
    private val launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    private val setPending: (((Boolean) -> Unit)?) -> Unit
) {
    private val requiredPermissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.RECORD_AUDIO
    )

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCallPermission() = hasPermission(Manifest.permission.CALL_PHONE)
    fun hasContactsPermission() = hasPermission(Manifest.permission.READ_CONTACTS)
    fun hasRecordPermission() = hasPermission(Manifest.permission.RECORD_AUDIO)

    /**
     * Request all needed; callback with overall granted.
     * For Phase 0 we often only need CALL_PHONE for dial action.
     */
    fun requestPermissions(onResult: (Boolean) -> Unit) {
        val needed = requiredPermissions.filter { !hasPermission(it) }.toTypedArray()
        if (needed.isEmpty()) {
            onResult(true)
            return
        }
        // Note: real rationale dialog would use shouldShowRequestPermissionRationale + AlertDialog.
        // Simplified here: direct launch. Enhance in Settings or before critical flows.
        setPending(onResult)
        launcher.launch(needed)
    }

    fun requestCallPermission(onResult: (Boolean) -> Unit) {
        if (hasCallPermission()) {
            onResult(true)
            return
        }
        setPending(onResult)
        launcher.launch(arrayOf(Manifest.permission.CALL_PHONE))
    }

    fun requestRecordPermission(onResult: (Boolean) -> Unit) {
        if (hasRecordPermission()) {
            onResult(true)
            return
        }
        setPending(onResult)
        launcher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
    }

    /**
     * Audio routing + recording notes (for Agent D telephony):
     * - Call controls (Mute/Speaker/Bluetooth/Hold/Record) in ActiveCallScreen + CallViewModel use these.
     * - RECORD_AUDIO required for in-call recording (MediaRecorder) and some BT SCO.
     * - CALL_PHONE for actual dial intents / telecom.
     * - Usage pattern in screens/VM:
     *     val ph = rememberPermissionHelper()
     *     ph.requestRecordPermission { granted -> if (granted) vm.toggleRecording() else /* toast fallback */ }
     * - Real routing (future): AudioManager.setSpeakerphoneOn, startBluetoothSco, setMode(MODE_IN_CALL).
     * - Graceful: if denied, controls still toggle UI state; real audio no-op or system default.
     * See CallViewModel for full audio routing stubs + PRD NFRs.
     */
}
