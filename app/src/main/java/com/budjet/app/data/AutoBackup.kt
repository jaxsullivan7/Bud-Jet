package com.budjet.app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

data class AutoBackupInfo(
    val name: String,
    val date: LocalDate,
    val sizeBytes: Long,
)

/**
 * Encrypted local snapshots kept inside the app's private storage. Once per day
 * at most, and only the last 14 are retained.
 */
object AutoBackup {

    private const val KEY_ALIAS = "budjet_auto_backup_v1"
    private const val FOLDER = "backups"
    private const val MAX_FILES = 14
    private val nameFormat = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun maybeCreate(context: Context, data: AppData): Result<File> = runCatching {
        if (!data.settings.autoBackupEnabled) error("Las copias automáticas están desactivadas.")
        val folder = File(context.filesDir, FOLDER).apply { mkdirs() }
        val today = LocalDate.now()
        val target = File(folder, "auto-${today.format(nameFormat)}.bjet")
        if (target.exists()) return@runCatching target
        val plain = Backup.encode(data).toByteArray(Charsets.UTF_8)
        val payload = CryptoBackup.encryptWithKey(plain, key())
        target.writeBytes(payload)
        folder.listFiles()
            ?.filter { it.name.startsWith("auto-") && it.extension == "bjet" }
            ?.sortedByDescending { it.name }
            ?.drop(MAX_FILES)
            ?.forEach { it.delete() }
        target
    }

    fun list(context: Context): List<AutoBackupInfo> {
        val folder = File(context.filesDir, FOLDER)
        if (!folder.isDirectory) return emptyList()
        return folder.listFiles()
            ?.filter { it.name.startsWith("auto-") && it.extension == "bjet" }
            ?.mapNotNull { file ->
                val raw = file.name.removePrefix("auto-").removeSuffix(".bjet")
                runCatching { LocalDate.parse(raw, nameFormat) }.getOrNull()?.let { date ->
                    AutoBackupInfo(file.name, date, file.length())
                }
            }
            ?.sortedByDescending { it.date }
            .orEmpty()
    }

    fun restore(context: Context, name: String): Result<AppData> = runCatching {
        val folder = File(context.filesDir, FOLDER)
        val file = File(folder, name)
        require(file.parentFile == folder && file.exists()) { "La copia automática no existe." }
        val payload = file.readBytes()
        val plain = CryptoBackup.decryptWithKey(payload, key()).getOrThrow()
        Backup.decode(String(plain, Charsets.UTF_8)).getOrThrow()
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }
}
