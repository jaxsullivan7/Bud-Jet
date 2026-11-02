package com.budjet.app

import android.content.Intent
import com.budjet.app.util.parseMoneyInput

data class QuickCaptureRequest(
    val amountCents: Long?,
    val note: String,
)

object QuickCapture {

    const val ACTION_QUICK_ADD = "com.budjet.app.action.QUICK_ADD"

    fun parse(intent: Intent?): QuickCaptureRequest? {
        if (intent == null) return null
        if (intent.action == ACTION_QUICK_ADD) return QuickCaptureRequest(null, "")
        if (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_PROCESS_TEXT) return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            ?: return null
        val cleaned = text.trim().replace('\n', ' ')
        if (cleaned.isBlank()) return null
        val match = Regex("([0-9]{1,3}(?:\\.[0-9]{3})*(?:,[0-9]{1,2})?|[0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:€|eur)?", RegexOption.IGNORE_CASE)
            .find(cleaned)
        val amount = match?.groupValues?.get(1)?.let(::parseMoneyInput)?.takeIf { it > 0L }
        return QuickCaptureRequest(
            amountCents = amount,
            note = cleaned.take(48),
        )
    }
}
