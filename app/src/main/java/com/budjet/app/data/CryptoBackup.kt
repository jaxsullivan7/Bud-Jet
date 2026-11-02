package com.budjet.app.data

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-based encrypted backups. Format:
 * magic "BUDJET1" | salt (16) | iv (12) | AES-GCM ciphertext.
 */
object CryptoBackup {

    private val MAGIC = "BUDJET1".toByteArray(Charsets.US_ASCII)
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private const val ITERATIONS = 180_000
    private const val KEY_BITS = 256

    fun encrypt(plain: ByteArray, password: CharArray): ByteArray {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_BYTES).also { SecureRandom().nextBytes(it) }
        val key = derive(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        val encrypted = cipher.doFinal(plain)
        return ByteBuffer.allocate(MAGIC.size + SALT_BYTES + IV_BYTES + encrypted.size)
            .put(MAGIC)
            .put(salt)
            .put(iv)
            .put(encrypted)
            .array()
    }

    fun decrypt(payload: ByteArray, password: CharArray): Result<ByteArray> = runCatching {
        require(payload.size > MAGIC.size + SALT_BYTES + IV_BYTES + 16) {
            "La copia está incompleta o dañada."
        }
        val buffer = ByteBuffer.wrap(payload)
        val magic = ByteArray(MAGIC.size).also { buffer.get(it) }
        require(magic.contentEquals(MAGIC)) { "El archivo no es una copia cifrada de Bud Jet." }
        val salt = ByteArray(SALT_BYTES).also { buffer.get(it) }
        val iv = ByteArray(IV_BYTES).also { buffer.get(it) }
        val encrypted = ByteArray(buffer.remaining()).also { buffer.get(it) }
        val key = derive(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        cipher.doFinal(encrypted)
    }

    fun encryptWithKey(plain: ByteArray, key: SecretKey): ByteArray {
        val iv = ByteArray(IV_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        val encrypted = cipher.doFinal(plain)
        return ByteBuffer.allocate(MAGIC.size + IV_BYTES + encrypted.size)
            .put(MAGIC)
            .put(iv)
            .put(encrypted)
            .array()
    }

    fun decryptWithKey(payload: ByteArray, key: SecretKey): Result<ByteArray> = runCatching {
        require(payload.size > MAGIC.size + IV_BYTES + 16) { "La copia está incompleta o dañada." }
        val buffer = ByteBuffer.wrap(payload)
        val magic = ByteArray(MAGIC.size).also { buffer.get(it) }
        require(magic.contentEquals(MAGIC)) { "El archivo no es una copia de Bud Jet." }
        val iv = ByteArray(IV_BYTES).also { buffer.get(it) }
        val encrypted = ByteArray(buffer.remaining()).also { buffer.get(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        cipher.doFinal(encrypted)
    }

    private fun derive(password: CharArray, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val bytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(bytes, "AES")
    }
}
