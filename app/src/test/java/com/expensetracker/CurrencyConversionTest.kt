package com.expensetracker

import com.expensetracker.data.model.CurrencyRate
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CurrencyConversionTest {

    private lateinit var usdRate: CurrencyRate
    private lateinit var eurRate: CurrencyRate
    private lateinit var rubRate: CurrencyRate

    @Before
    fun setUp() {
        // Approximate NBRB-style rates (BYN per N units)
        usdRate = CurrencyRate(abbreviation = "USD", name = "US Dollar",  scale = 1, rate = 3.2540)
        eurRate = CurrencyRate(abbreviation = "EUR", name = "Euro",       scale = 1, rate = 3.5200)
        rubRate = CurrencyRate(abbreviation = "RUB", name = "Rus. Ruble", scale = 100, rate = 3.6100)
    }

    // ── convertFromByn ────────────────────────────────────────────────────────

    @Test
    fun `convert BYN to USD with scale 1`() {
        val byn = 32.54
        val usd = usdRate.convertFromByn(byn)
        assertEquals(10.0, usd, 0.01)
    }

    @Test
    fun `convert BYN to EUR with scale 1`() {
        val byn = 35.20
        val eur = eurRate.convertFromByn(byn)
        assertEquals(10.0, eur, 0.01)
    }

    @Test
    fun `convert BYN to RUB with scale 100`() {
        val byn = 3.61
        val rub = rubRate.convertFromByn(byn)
        // scale = 100, so 3.61 BYN / 3.61 * 100 = 100 RUB
        assertEquals(100.0, rub, 0.01)
    }

    @Test
    fun `convert zero BYN returns zero`() {
        assertEquals(0.0, usdRate.convertFromByn(0.0), 0.001)
    }

    // ── convertToByn ─────────────────────────────────────────────────────────

    @Test
    fun `convert USD to BYN with scale 1`() {
        val usd = 10.0
        val byn = usdRate.convertToByn(usd)
        assertEquals(32.54, byn, 0.01)
    }

    @Test
    fun `convert EUR to BYN with scale 1`() {
        val eur = 10.0
        val byn = eurRate.convertToByn(eur)
        assertEquals(35.20, byn, 0.01)
    }

    @Test
    fun `convert RUB to BYN with scale 100`() {
        val rub = 100.0
        val byn = rubRate.convertToByn(rub)
        assertEquals(3.61, byn, 0.01)
    }

    @Test
    fun `convert zero foreign currency returns zero`() {
        assertEquals(0.0, usdRate.convertToByn(0.0), 0.001)
    }

    // ── Round-trip ────────────────────────────────────────────────────────────

    @Test
    fun `round trip BYN to USD and back`() {
        val original = 100.0
        val usd = usdRate.convertFromByn(original)
        val backToByn = usdRate.convertToByn(usd)
        assertEquals(original, backToByn, 0.001)
    }

    @Test
    fun `round trip BYN to RUB and back with scale 100`() {
        val original = 50.0
        val rub = rubRate.convertFromByn(original)
        val backToByn = rubRate.convertToByn(rub)
        assertEquals(original, backToByn, 0.001)
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `large amount conversion is precise`() {
        val byn = 10_000.0
        val usd = usdRate.convertFromByn(byn)
        assertEquals(byn, usdRate.convertToByn(usd), 0.01)
    }

    @Test
    fun `conversion with very small amount`() {
        val byn = 0.01
        val usd = usdRate.convertFromByn(byn)
        assertTrue(usd > 0.0)
        assertEquals(byn, usdRate.convertToByn(usd), 0.001)
    }
}
