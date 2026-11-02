package com.budjet.app

import com.budjet.app.data.CategoryRule
import com.budjet.app.data.CsvMapping
import com.budjet.app.data.StatementImport
import com.budjet.app.data.TxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StatementImportTest {

    private val rules = listOf(
        CategoryRule("r1", "mercadona", "food"),
        CategoryRule("r2", "nomina", "salary"),
    )

    @Test
    fun `csv detects columns, amounts and categories`() {
        val csv = """
            Fecha;Concepto;Importe;Tipo
            01/11/2026;Mercadona;-45,90;Gasto
            02/11/2026;Nómina;2.450,00;Ingreso
        """.trimIndent()
        val parsed = StatementImport.parse(
            text = csv,
            accountId = "main",
            fallbackCategoryId = "other",
            rules = rules,
            existingHashes = emptySet(),
        ).getOrThrow()
        assertEquals(2, parsed.rows.size)
        assertEquals(4_590L, parsed.rows[0].amountCents)
        assertEquals(TxType.EXPENSE, parsed.rows[0].type)
        assertEquals("food", parsed.rows[0].categoryId)
        assertEquals(245_000L, parsed.rows[1].amountCents)
        assertEquals(TxType.INCOME, parsed.rows[1].type)
        assertEquals("salary", parsed.rows[1].categoryId)
    }

    @Test
    fun `csv detects duplicates by hash`() {
        val csv = "Fecha;Concepto;Importe\n01/11/2026;Mercadona;-45,90"
        val first = StatementImport.parse(csv, "main", "other", rules, emptySet()).getOrThrow()
        val second = StatementImport.parse(
            csv,
            "main",
            "other",
            rules,
            first.rows.map { it.hash }.toSet(),
        ).getOrThrow()
        assertTrue(second.rows.single().duplicate)
    }

    @Test
    fun `custom mapping can read debit and credit columns`() {
        val csv = """
            Data;Descripció;Càrrec;Abonament
            2026-11-01;Mercadona;45,90;
            2026-11-02;Nómina;;2.450,00
        """.trimIndent()
        val mapping = CsvMapping(
            dateIndex = 0,
            descriptionIndex = 1,
            debitIndex = 2,
            creditIndex = 3,
        )
        val parsed = StatementImport.parse(
            text = csv,
            accountId = "main",
            fallbackCategoryId = "other",
            rules = rules,
            existingHashes = emptySet(),
            mapping = mapping,
        ).getOrThrow()
        assertEquals(TxType.EXPENSE, parsed.rows[0].type)
        assertEquals(4_590L, parsed.rows[0].amountCents)
        assertEquals(TxType.INCOME, parsed.rows[1].type)
        assertEquals(245_000L, parsed.rows[1].amountCents)
    }

    @Test
    fun `ofx extracts statement transactions`() {
        val ofx = """
            <OFX><BANKMSGSRSV1><STMTTRNRS><STMTRS><BANKTRANLIST>
            <STMTTRN><TRNTYPE>DEBIT</TRNTYPE><DTPOSTED>20261101</DTPOSTED><TRNAMT>-12.50</TRNAMT><NAME>Mercadona</NAME><MEMO>Compra</MEMO></STMTTRN>
            <STMTTRN><TRNTYPE>CREDIT</TRNTYPE><DTPOSTED>20261102</DTPOSTED><TRNAMT>1000.00</TRNAMT><NAME>Nómina</NAME></STMTTRN>
            </BANKTRANLIST></STMTRS></STMTTRNRS></BANKMSGSRSV1></OFX>
        """.trimIndent()
        val parsed = StatementImport.parse(ofx, "main", "other", rules, emptySet()).getOrThrow()
        assertEquals(2, parsed.rows.size)
        assertEquals(LocalDate.of(2026, 11, 1), parsed.rows[0].date)
        assertEquals(TxType.EXPENSE, parsed.rows[0].type)
        assertEquals(1_250L, parsed.rows[0].amountCents)
        assertEquals(TxType.INCOME, parsed.rows[1].type)
        assertEquals(100_000L, parsed.rows[1].amountCents)
    }

    @Test
    fun `european and anglo amounts are parsed`() {
        assertEquals(123_456L, StatementImport.parseAmountCents("1.234,56"))
        assertEquals(123_456L, StatementImport.parseAmountCents("1,234.56"))
        assertEquals(123_400L, StatementImport.parseAmountCents("1.234"))
        assertEquals(-4_590L, StatementImport.parseAmountCents("(45,90)"))
    }
}
