package com.budjet.app

import com.budjet.app.data.CryptoBackup
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CryptoBackupTest {

    private val plain = "datos privados de Bud Jet".toByteArray(Charsets.UTF_8)

    @Test
    fun `password round trip`() {
        val payload = CryptoBackup.encrypt(plain, "una contraseña larga".toCharArray())
        val restored = CryptoBackup.decrypt(payload, "una contraseña larga".toCharArray()).getOrThrow()
        assertArrayEquals(plain, restored)
    }

    @Test
    fun `wrong password fails`() {
        val payload = CryptoBackup.encrypt(plain, "correcta".toCharArray())
        assertFalse(CryptoBackup.decrypt(payload, "incorrecta".toCharArray()).isSuccess)
    }

    @Test
    fun `tampered payload fails integrity check`() {
        val payload = CryptoBackup.encrypt(plain, "correcta".toCharArray())
        payload[payload.lastIndex] = (payload.last().toInt() xor 1).toByte()
        assertFalse(CryptoBackup.decrypt(payload, "correcta".toCharArray()).isSuccess)
    }
}
