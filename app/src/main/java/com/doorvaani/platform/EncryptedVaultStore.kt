package com.doorvaani.platform

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Basic local encrypted store stub for Vault / Knowledge Vault (Phase 1 foundation).
 * 
 * - Master AES-256 key generated/stored in Android Keystore (enclave, never exported).
 * - AES/GCM/NoPadding for confidentiality + integrity (IV random per op, prepended).
 * - Recordings stored as encrypted blobs in app-private filesDir/vault/ (no plaintext ever on disk).
 * - Metadata index also encrypted (simple delimited format to avoid extra serialization deps).
 * - Strict privacy: zero plaintext leakage. Matches PRD §6, §9, Agent C charter, call_recording_center goldens.
 * - Note: Full Room + SQLCipher or proper file-per-record + manifest for prod; this is working stub.
 * - Backup/restore will use encrypted bundles + manifest verification (future).
 *
 * References:
 * - call_recording_center_archive_vault/code.html (Secure/Standard, local-first)
 * - PRD: "Local-first AES-256 + key in enclave/Keystore", "encrypted export bundles + manifest"
 * - F QA gap: "need for real encrypted storage"
 */
object EncryptedVaultStore {
    private const val KEY_ALIAS = "doorvaani_vault_master_key"
    private const val VAULT_DIR = "vault"
    private const val INDEX_FILE = "vault_index.enc"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LEN = 128  // bits

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = getOrCreateSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plain)
        // Prepend IV (12 bytes typical for GCM) + ciphertext
        return iv + cipherText
    }

    private fun decrypt(cipherWithIv: ByteArray): ByteArray {
        if (cipherWithIv.size < 12) return ByteArray(0)
        val iv = cipherWithIv.copyOfRange(0, 12)
        val cipherText = cipherWithIv.copyOfRange(12, cipherWithIv.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = getOrCreateSecretKey()
        val spec = GCMParameterSpec(TAG_LEN, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(cipherText)
    }

    private fun getVaultDir(context: Context): File {
        val dir = File(context.filesDir, VAULT_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** Save encrypted stub recording file for a record (legacy / non-recorded calls). Returns relative path. */
    fun saveEncryptedRecording(context: Context, recordId: String, stubContent: String = "DOORVAANI_STUB_ENCRYPTED_RECORDING"): String {
        val dir = getVaultDir(context)
        val file = File(dir, "$recordId.enc")
        // Prevent Path Traversal
        if (!file.canonicalPath.startsWith(dir.canonicalPath + File.separator)) {
            throw SecurityException("Invalid record ID: potential path traversal")
        }
        val plain = (stubContent + "\nID=$recordId\nTS=${System.currentTimeMillis()}").toByteArray(Charsets.UTF_8)
        val enc = encrypt(plain)
        file.writeBytes(enc)
        return "vault/$recordId.enc"
    }

    /**
     * Production: Encrypt a real audio recording file (from MediaRecorder temp) and store as .enc in private vault.
     * Deletes the original temp plaintext audio file after successful encryption (zero plaintext leakage).
     * Returns the relative encrypted path for the VaultRecord.
     */
    fun saveEncryptedAudio(context: Context, recordId: String, audioFile: File): String {
        val dir = getVaultDir(context)
        val encFile = File(dir, "$recordId.enc")
        // Prevent Path Traversal
        if (!encFile.canonicalPath.startsWith(dir.canonicalPath + File.separator)) {
            throw SecurityException("Invalid record ID: potential path traversal")
        }
        return try {
            if (!audioFile.exists()) return saveEncryptedRecording(context, recordId)
            val plainBytes = audioFile.readBytes()
            val enc = encrypt(plainBytes)
            encFile.writeBytes(enc)
            "vault/$recordId.enc"
        } catch (e: Exception) {
            // Fallback: at least don't lose the record
            saveEncryptedRecording(context, recordId, "AUDIO_ENCRYPT_FAILED:${e.message}")
        } finally {
            // Privacy: ALWAYS remove raw temp audio immediately, even if encryption fails
            if (audioFile.exists()) {
                audioFile.delete()
            }
        }
    }

    /** Load (decrypt) stub for playback verification (returns plaintext only in memory). */
    fun loadDecryptedRecording(context: Context, relativePath: String): String {
        return try {
            val file = File(context.filesDir, relativePath)
            // Prevent Path Traversal
            if (!file.canonicalPath.startsWith(context.filesDir.canonicalPath + File.separator)) {
                return "[decrypt-failed: invalid path]"
            }
            if (!file.exists()) return "[stub-missing]"
            val enc = file.readBytes()
            val plain = decrypt(enc)
            String(plain, Charsets.UTF_8)
        } catch (e: Exception) {
            "[decrypt-failed: ${e.message}]"
        }
    }

    /** Persist the entire index (list of metadata rows) as encrypted file. Simple | delimited to avoid deps. */
    fun saveEncryptedIndex(context: Context, records: List<com.doorvaani.domain.model.VaultRecord>) {
        val dir = getVaultDir(context)
        val indexFile = File(dir, INDEX_FILE)
        // Format: id|name|phone|ts|dur|dir|sec|src|encPath\n  (direction+sec+src as ordinals)
        val lines = records.joinToString("\n") { r ->
            listOf(
                r.id,
                r.displayName.replace("|", "_"),
                r.phone ?: "",
                r.timestamp.toString(),
                r.durationSeconds.toString(),
                r.direction.ordinal.toString(),
                r.securityLevel.ordinal.toString(),
                r.source.ordinal.toString(),
                r.encryptedFilePath
            ).joinToString("|")
        }
        val enc = encrypt(lines.toByteArray(Charsets.UTF_8))
        indexFile.writeBytes(enc)
    }

    /** Load index (decrypt) or empty. */
    fun loadEncryptedIndex(context: Context): List<com.doorvaani.domain.model.VaultRecord> {
        return try {
            val dir = getVaultDir(context)
            val indexFile = File(dir, INDEX_FILE)
            if (!indexFile.exists()) return emptyList()
            val enc = indexFile.readBytes()
            val plain = String(decrypt(enc), Charsets.UTF_8)
            if (plain.isBlank()) return emptyList()
            plain.lines().filter { it.isNotBlank() }.mapNotNull { line ->
                val p = line.split("|")
                if (p.size < 9) return@mapNotNull null
                com.doorvaani.domain.model.VaultRecord(
                    id = p[0],
                    displayName = p[1],
                    phone = p[2].ifBlank { null },
                    timestamp = p[3].toLongOrNull() ?: 0L,
                    durationSeconds = p[4].toIntOrNull() ?: 0,
                    direction = com.doorvaani.domain.model.CallDirection.values().getOrElse(p[5].toIntOrNull() ?: 0) { com.doorvaani.domain.model.CallDirection.OUTGOING },
                    securityLevel = com.doorvaani.domain.model.SecurityLevel.values().getOrElse(p[6].toIntOrNull() ?: 0) { com.doorvaani.domain.model.SecurityLevel.Standard },
                    source = com.doorvaani.domain.model.RecordingSource.values().getOrElse(p[7].toIntOrNull() ?: 0) { com.doorvaani.domain.model.RecordingSource.External },
                    encryptedFilePath = p[8]
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}