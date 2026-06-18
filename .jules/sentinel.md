
## 2024-05-24 - Unencrypted Temp Audio File Leak & Path Traversal in Vault Store
**Vulnerability:**
1. A data leak vulnerability existed in `EncryptedVaultStore.saveEncryptedAudio` where a temporary unencrypted audio file (`audioFile`) was not deleted if the encryption process failed (e.g. an exception was thrown). This left sensitive plaintext call recordings on the file system.
2. A path traversal vulnerability existed in `EncryptedVaultStore` where `saveEncryptedRecording`, `saveEncryptedAudio`, and `loadDecryptedRecording` constructed file paths using input variables (`recordId` and `relativePath`) without validating that the final path stayed within the secure vault directory. This allowed arbitrary file reads/writes if malicious IDs were provided.

**Learning:**
1. When dealing with temporary sensitive files, cleanup logic must be placed in a `finally` block to ensure execution regardless of exceptions. Relying on an inline `.delete()` after a successful operation is not sufficient.
2. Any time a file path is constructed from user-provided or external inputs, it must be validated. Even if an ID seems benign, it should be verified against canonical paths to ensure it doesn't escape its designated sandbox directory.

**Prevention:**
1. Use `try { ... } finally { file.delete() }` for all temporary files containing sensitive data.
2. Validate resolved file paths by asserting that `file.canonicalPath.startsWith(baseDir.canonicalPath)` before proceeding with read/write operations.
## 2023-10-27 - [URI Injection Prevention in Intents]
**Vulnerability:** Unsanitized user inputs appended to `Uri.parse("tel:...")`.
**Learning:** Raw input strings containing special characters like `#` can cause URI truncation or injection, as `#` is a fragment separator. This is particularly problematic for phone numbers that may include USSD codes or extensions.
**Prevention:** Always use `Uri.encode()` when constructing URIs from untrusted or user-provided input, especially for intents like `ACTION_CALL`.
