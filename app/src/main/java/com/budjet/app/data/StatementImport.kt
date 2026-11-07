package com.budjet.app.data

import java.security.MessageDigest
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CsvMapping(
    val dateIndex: Int,
    val descriptionIndex: Int,
    val amountIndex: Int? = null,
    val debitIndex: Int? = null,
    val creditIndex: Int? = null,
    val typeIndex: Int? = null,
)

data class ParsedStatement(
    val rows: List<ImportRow>,
    val headers: List<String>,
    val mapping: CsvMapping?,
    val isOfx: Boolean,
)

data class ImportRow(
    val date: LocalDate,
    val amountCents: Long,
    val type: TxType,
    val note: String,
    val categoryId: String,
    val hash: String,
    val duplicate: Boolean = false,
)

/**
 * Local CSV/OFX parser. It never talks to a bank: the user picks a file and
 * Bud Jet turns it into candidate transactions for review.
 */
object StatementImport {

    private val dateFormats = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("d-M-yyyy"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
    )

    fun parse(
        text: String,
        accountId: String,
        fallbackCategoryId: String,
        rules: List<CategoryRule>,
        existingHashes: Set<String>,
        mapping: CsvMapping? = null,
        defaultNote: String = "Movimiento importado",
    ): Result<ParsedStatement> = runCatching {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "El archivo está vacío." }
        if (looksLikeOfx(trimmed)) {
            parseOfx(trimmed, accountId, fallbackCategoryId, rules, existingHashes, defaultNote)
        } else {
            parseCsv(
                trimmed,
                accountId,
                fallbackCategoryId,
                rules,
                existingHashes,
                mapping,
                defaultNote,
            )
        }
    }

    fun csvHeaders(text: String): List<String> {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || looksLikeOfx(trimmed)) return emptyList()
        val delimiter = detectDelimiter(trimmed)
        return splitCsvLine(trimmed.lineSequence().first(), delimiter)
    }

    fun categoryFor(note: String, rules: List<CategoryRule>, fallback: String): String {
        val normalized = normalize(note)
        return rules.firstOrNull { normalized.contains(normalize(it.pattern)) }?.categoryId ?: fallback
    }

    private fun parseCsv(
        text: String,
        accountId: String,
        fallbackCategoryId: String,
        rules: List<CategoryRule>,
        existingHashes: Set<String>,
        supplied: CsvMapping?,
        defaultNote: String,
    ): ParsedStatement {
        val delimiter = detectDelimiter(text)
        val lines = text.lineSequence().filter { it.isNotBlank() }.toList()
        require(lines.size >= 2) { "El CSV necesita cabecera y al menos una fila." }
        val headers = splitCsvLine(lines.first(), delimiter)
        val mapping = supplied ?: detectMapping(headers)
        require(mapping != null) {
            "No se han podido detectar las columnas. Elige fecha, concepto e importe manualmente."
        }
        val requiredOk = mapping.amountIndex != null || (mapping.debitIndex != null && mapping.creditIndex != null)
        require(requiredOk) { "No se ha encontrado una columna de importe, cargo o abono." }

        val rows = lines.drop(1).mapNotNull { line ->
            val cells = splitCsvLine(line, delimiter)
            fun cell(index: Int?): String = index?.let { cells.getOrNull(it)?.trim().orEmpty() }.orEmpty()
            val date = parseDate(cell(mapping.dateIndex)) ?: return@mapNotNull null
            val note = cell(mapping.descriptionIndex).ifBlank { defaultNote }
            val rawAmount = mapping.amountIndex?.let { cell(it) }.orEmpty()
            val debit = mapping.debitIndex?.let { cell(it) }.orEmpty()
            val credit = mapping.creditIndex?.let { cell(it) }.orEmpty()
            val typeHint = cell(mapping.typeIndex)
            val signed = when {
                rawAmount.isNotBlank() -> parseAmountCents(rawAmount) ?: return@mapNotNull null
                debit.isNotBlank() -> -(parseAmountCents(debit) ?: return@mapNotNull null)
                credit.isNotBlank() -> parseAmountCents(credit) ?: return@mapNotNull null
                else -> return@mapNotNull null
            }
            if (signed == 0L) return@mapNotNull null
            val type = when {
                signed < 0L -> TxType.EXPENSE
                looksIncome(typeHint, credit) -> TxType.INCOME
                else -> TxType.EXPENSE
            }
            val amount = kotlin.math.abs(signed)
            val hash = hash(accountId, date, amount, type, note)
            ImportRow(
                date = date,
                amountCents = amount,
                type = type,
                note = note,
                categoryId = categoryFor(note, rules, fallbackCategoryId),
                hash = hash,
                duplicate = hash in existingHashes,
            )
        }
        require(rows.isNotEmpty()) { "No se ha podido leer ningún movimiento del CSV." }
        return ParsedStatement(rows, headers, mapping, isOfx = false)
    }

    private fun parseOfx(
        text: String,
        accountId: String,
        fallbackCategoryId: String,
        rules: List<CategoryRule>,
        existingHashes: Set<String>,
        defaultNote: String,
    ): ParsedStatement {
        val blocks = Regex("<STMTTRN>(.*?)</STMTTRN>", RegexOption.DOT_MATCHES_ALL)
            .findAll(text)
            .map { it.groupValues[1] }
            .toList()
        require(blocks.isNotEmpty()) { "El OFX no contiene movimientos." }
        val rows = blocks.mapNotNull { block ->
            fun tag(name: String): String =
                Regex("<$name>([^<\\r\\n]*)", RegexOption.IGNORE_CASE).find(block)?.groupValues?.get(1)?.trim().orEmpty()
            val date = parseOfxDate(tag("DTPOSTED")) ?: return@mapNotNull null
            val signed = parseAmountCents(tag("TRNAMT")) ?: return@mapNotNull null
            if (signed == 0L) return@mapNotNull null
            val note = listOf(tag("NAME"), tag("MEMO")).filter { it.isNotBlank() }.joinToString(" · ")
                .ifBlank { defaultNote }
            val type = if (signed > 0L && tag("TRNTYPE").uppercase(Locale.US) in setOf("CREDIT", "DEP", "DIRECTDEP")) {
                TxType.INCOME
            } else {
                TxType.EXPENSE
            }
            val amount = kotlin.math.abs(signed)
            val hash = hash(accountId, date, amount, type, note)
            ImportRow(
                date = date,
                amountCents = amount,
                type = type,
                note = note,
                categoryId = categoryFor(note, rules, fallbackCategoryId),
                hash = hash,
                duplicate = hash in existingHashes,
            )
        }
        require(rows.isNotEmpty()) { "No se ha podido leer ningún movimiento del OFX." }
        return ParsedStatement(rows, emptyList(), null, isOfx = true)
    }

    private fun looksLikeOfx(text: String): Boolean =
        text.contains("<OFX>", ignoreCase = true) || text.contains("<STMTTRN>", ignoreCase = true)

    private fun detectDelimiter(text: String): Char {
        val first = text.lineSequence().firstOrNull().orEmpty()
        val candidates = listOf(';', ',', '\t')
        return candidates.maxByOrNull { first.count { c -> c == it } } ?: ';'
    }

    fun detectMapping(headers: List<String>): CsvMapping? {
        fun find(vararg keys: String): Int? = headers.indexOfFirst { header ->
            val n = normalize(header)
            keys.any { n.contains(it) }
        }
        val date = find("fecha", "date", "f. valor", "fecha operacion") ?: 0
        val description = find("concepto", "descrip", "detalle", "nota", "memo", "beneficiario") ?: 2
        val amount = find("importe", "amount", "cantidad", "valor")
        val debit = find("cargo", "debito", "debit", "salida", "gasto")
        val credit = find("abono", "credito", "credit", "entrada", "ingreso")
        val type = find("tipo", "type", "naturaleza")
        if (amount == null && (debit == null || credit == null)) return null
        if (date >= headers.size || description >= headers.size) return null
        return CsvMapping(
            dateIndex = date,
            descriptionIndex = description,
            amountIndex = amount,
            debitIndex = debit,
            creditIndex = credit,
            typeIndex = type,
        )
    }

    private fun splitCsvLine(line: String, delimiter: Char): List<String> {
        val out = mutableListOf<String>()
        val current = StringBuilder()
        var quoted = false
        var index = 0
        while (index < line.length) {
            val c = line[index]
            when {
                c == '"' && quoted && index + 1 < line.length && line[index + 1] == '"' -> {
                    current.append('"')
                    index++
                }
                c == '"' -> quoted = !quoted
                c == delimiter && !quoted -> {
                    out += current.toString()
                    current.clear()
                }
                else -> current.append(c)
            }
            index++
        }
        out += current.toString()
        return out
    }

    private fun parseDate(raw: String): LocalDate? {
        val value = raw.trim().substringBefore(' ').substringBefore('T')
        if (value.isBlank()) return null
        for (format in dateFormats) {
            runCatching { return LocalDate.parse(value, format) }
        }
        // Some banks use 8-digit dates without separators.
        if (value.length == 8 && value.all(Char::isDigit)) {
            runCatching {
                return LocalDate.of(value.take(4).toInt(), value.substring(4, 6).toInt(), value.takeLast(2).toInt())
            }
        }
        return null
    }

    private fun parseOfxDate(raw: String): LocalDate? {
        val digits = raw.take(8)
        if (digits.length != 8 || !digits.all(Char::isDigit)) return null
        return runCatching {
            LocalDate.of(digits.take(4).toInt(), digits.substring(4, 6).toInt(), digits.takeLast(2).toInt())
        }.getOrNull()
    }

    fun parseAmountCents(raw: String): Long? {
        var value = raw.trim()
            .replace("€", "")
            .replace("$", "")
            .replace("£", "")
            .replace(" ", "")
            .replace("\u00A0", "")
            .replace("EUR", "", ignoreCase = true)
            .replace("USD", "", ignoreCase = true)
        if (value.isBlank()) return null
        val negative = value.startsWith("-") || (value.startsWith("(") && value.endsWith(")"))
        value = value.removePrefix("-").removePrefix("(").removeSuffix(")")
        value = when {
            value.contains(',') && value.contains('.') -> {
                if (value.lastIndexOf(',') > value.lastIndexOf('.')) {
                    value.replace(".", "").replace(",", ".")
                } else {
                    value.replace(",", "")
                }
            }
            value.contains(',') -> {
                val decimals = value.substringAfterLast(',').length
                if (decimals in 1..2) value.replace(",", ".") else value.replace(",", "")
            }
            value.contains('.') -> {
                val decimals = value.substringAfterLast('.').length
                val integer = value.substringBefore('.')
                if (decimals == 3 && integer.length in 1..3) value.replace(".", "") else value
            }
            else -> value
        }
        val number = value.toDoubleOrNull() ?: return null
        return (number * 100).toLong().let { if (negative) -it else it }
    }

    private fun looksIncome(typeHint: String, credit: String): Boolean {
        if (credit.isNotBlank()) return true
        val hint = normalize(typeHint)
        return hint.contains("ingreso") || hint.contains("abono") || hint.contains("credit") ||
            hint.contains("entrada") || hint.contains("deposito")
    }

    private fun hash(
        accountId: String,
        date: LocalDate,
        amountCents: Long,
        type: TxType,
        note: String,
    ): String {
        val source = "$accountId|${date}|$amountCents|$type|${normalize(note)}"
        val digest = MessageDigest.getInstance("SHA-256").digest(source.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
}
