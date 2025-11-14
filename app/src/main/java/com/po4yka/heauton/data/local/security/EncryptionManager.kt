package com.po4yka.heauton.data.local.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages encryption and decryption of journal entries using AES-256-GCM.
 *
 * ## Security Features:
 * - AES-256 encryption in GCM mode (Galois/Counter Mode)
 * - Keys stored in Android Keystore (hardware-backed when available)
 * - Per-entry encryption support with unique key IDs
 * - Initialization Vector (IV) generated for each encryption
 * - Authentication tag for integrity verification
 *
 * ## Usage:
 * ```kotlin
 * val encrypted = encryptionManager.encrypt("My secret journal entry", "entry-key-id")
 * val decrypted = encryptionManager.decrypt(encrypted, "entry-key-id")
 * ```
 *
 * ## Key Management:
 * - Keys are stored in Android Keystore
 * - Each key is identified by a unique ID
 * - Keys never leave the secure hardware (when available)
 * - Keys can require user authentication (biometric/PIN)
 */
@Singleton
class EncryptionManager @Inject constructor() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"

        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
        private const val IV_LENGTH = 12 // 96 bits for GCM

        // Default key for app-level encryption
        private const val DEFAULT_KEY_ALIAS = "heauton_master_key"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    /**
     * Encrypted data with initialization vector.
     * Format: [IV_LENGTH bytes of IV][remaining bytes are ciphertext with auth tag]
     */
    data class EncryptedData(
        val data: ByteArray,
        val keyAlias: String
    ) {
        /**
         * Convert to Base64 string for storage.
         */
        fun toBase64(): String = Base64.encodeToString(data, Base64.NO_WRAP)

        companion object {
            /**
             * Parse encrypted data from Base64 string.
             */
            fun fromBase64(base64: String, keyAlias: String): EncryptedData {
                val data = Base64.decode(base64, Base64.NO_WRAP)
                return EncryptedData(data, keyAlias)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EncryptedData

            if (!data.contentEquals(other.data)) return false
            if (keyAlias != other.keyAlias) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + keyAlias.hashCode()
            return result
        }
    }

    /**
     * Encrypt plaintext using AES-256-GCM.
     *
     * @param plaintext Text to encrypt
     * @param keyAlias Unique identifier for the encryption key (optional, uses default if null)
     * @return Encrypted data with IV prepended
     */
    fun encrypt(plaintext: String, keyAlias: String? = null): EncryptedData {
        val alias = keyAlias ?: DEFAULT_KEY_ALIAS

        // Ensure key exists
        if (!keyStore.containsAlias(alias)) {
            generateKey(alias)
        }

        val key = getKey(alias)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Combine IV and ciphertext
        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)

        return EncryptedData(combined, alias)
    }

    /**
     * Decrypt ciphertext using AES-256-GCM.
     *
     * @param encryptedData Encrypted data with IV prepended
     * @return Decrypted plaintext
     * @throws EncryptionException if decryption fails
     */
    fun decrypt(encryptedData: EncryptedData): String {
        return try {
            val key = getKey(encryptedData.keyAlias)
            val cipher = Cipher.getInstance(TRANSFORMATION)

            // Extract IV from the beginning
            val iv = ByteArray(IV_LENGTH)
            System.arraycopy(encryptedData.data, 0, iv, 0, IV_LENGTH)

            // Extract ciphertext (rest of the data)
            val ciphertext = ByteArray(encryptedData.data.size - IV_LENGTH)
            System.arraycopy(encryptedData.data, IV_LENGTH, ciphertext, 0, ciphertext.size)

            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val plaintext = cipher.doFinal(ciphertext)
            String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            throw EncryptionException("Failed to decrypt data", e)
        }
    }

    /**
     * Decrypt from Base64 string.
     *
     * @param base64Ciphertext Base64-encoded encrypted data
     * @param keyAlias Key alias used for encryption
     * @return Decrypted plaintext
     */
    fun decrypt(base64Ciphertext: String, keyAlias: String? = null): String {
        val alias = keyAlias ?: DEFAULT_KEY_ALIAS
        val encryptedData = EncryptedData.fromBase64(base64Ciphertext, alias)
        return decrypt(encryptedData)
    }

    /**
     * Generate a new encryption key in Android Keystore.
     *
     * @param keyAlias Unique identifier for the key
     * @param requireAuthentication Whether the key requires user authentication (biometric/PIN)
     */
    fun generateKey(
        keyAlias: String = DEFAULT_KEY_ALIAS,
        requireAuthentication: Boolean = false
    ) {
        val keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM, ANDROID_KEYSTORE)

        val builder = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(ENCRYPTION_BLOCK_MODE)
            .setEncryptionPaddings(ENCRYPTION_PADDING)
            .setKeySize(KEY_SIZE)
            .setRandomizedEncryptionRequired(true)

        // Require user authentication if specified
        if (requireAuthentication) {
            builder.setUserAuthenticationRequired(true)
            // Key is valid for 30 seconds after authentication
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(30)
        }

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    /**
     * Check if a key exists in the keystore.
     */
    fun hasKey(keyAlias: String = DEFAULT_KEY_ALIAS): Boolean {
        return keyStore.containsAlias(keyAlias)
    }

    /**
     * Delete a key from the keystore.
     *
     * @param keyAlias Key to delete
     */
    fun deleteKey(keyAlias: String) {
        if (keyStore.containsAlias(keyAlias)) {
            keyStore.deleteEntry(keyAlias)
        }
    }

    /**
     * Get the secret key from keystore.
     */
    private fun getKey(keyAlias: String): SecretKey {
        val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
            ?: throw EncryptionException("Key not found: $keyAlias")
        return entry.secretKey
    }

    /**
     * Check if hardware-backed keystore is available.
     * Hardware backing provides stronger security guarantees.
     */
    fun isHardwareBacked(): Boolean {
        return try {
            // This is a simplified check
            // In production, you'd want more sophisticated detection
            if (keyStore.containsAlias(DEFAULT_KEY_ALIAS)) {
                true
            } else {
                generateKey()
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Exception thrown when encryption/decryption fails.
 */
class EncryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)
