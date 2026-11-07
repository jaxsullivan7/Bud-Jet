package com.budjet.app.util

import android.content.Context
import com.budjet.app.R

/**
 * The validation layer reports errors in Spanish. For other languages we map the
 * message to a localized equivalent; Spanish keeps its more specific wording.
 */
fun localizedBackupMessage(context: Context, message: String?): String? {
    if (message == null) return null
    val locale = context.resources.configuration.locales[0]
    if (locale.language == "es") return message
    val text = message.lowercase(locale)
    val res = when {
        text.contains("demasiado grande") -> R.string.backup_error_too_large
        text.contains("incompleta") || text.contains("dañada") -> R.string.backup_error_corrupt
        text.contains("versión más nueva") -> R.string.backup_error_newer
        text.contains("formato") -> R.string.backup_error_format
        text.contains("moneda") -> R.string.backup_error_currency
        text.contains("nombre de perfil") -> R.string.backup_error_no_profile
        text.contains("desactivadas") -> R.string.backup_error_auto_disabled
        text.contains("no existe") -> R.string.backup_error_not_found
        text.contains("duplicad") ||
            text.contains("no válid") ||
            text.contains("no existen") ||
            text.contains("sin nombre") ||
            text.contains("más de un") ||
            text.contains("no contiene") -> R.string.backup_error_invalid_data
        text.contains("no es una copia") || text.contains("no parece una copia") ->
            R.string.backup_error_not_backup
        else -> R.string.backup_error_generic
    }
    return context.getString(res)
}
