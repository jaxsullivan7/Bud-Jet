package com.budjet.app.util

import android.content.Context
import com.budjet.app.R

/** Maps the parser's Spanish validation messages to the current language. */
fun localizedImportMessage(context: Context, message: String?): String? {
    if (message == null) return null
    val locale = context.resources.configuration.locales[0]
    if (locale.language == "es") return message
    val text = message.lowercase(locale)
    val res = when {
        text.contains("vacío") -> R.string.import_error_empty
        text.contains("cabecera") -> R.string.import_error_need_rows
        text.contains("detectar las columnas") -> R.string.import_error_columns
        text.contains("columna de importe") -> R.string.import_error_no_amount
        text.contains("del csv") -> R.string.import_error_no_rows_csv
        text.contains("ofx no contiene") -> R.string.import_error_no_rows_ofx
        text.contains("del ofx") -> R.string.import_error_no_rows_ofx_read
        else -> R.string.import_error_generic
    }
    return context.getString(res)
}
