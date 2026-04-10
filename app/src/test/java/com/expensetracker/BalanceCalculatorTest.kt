package com.expensetracker

import com.expensetracker.data.model.Category
import com.expensetracker.data.model.Period
import com.expensetracker.data.model.Transaction
import com.expensetracker.data.model.toDateRange
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class BalanceCalculatorTest {

    private val sampleCategory = Category(
        id = 1L, name = "Food", icon = "restaurant", color = 0xFFE57373
    )

    private fun makeTransaction(
        id: Long,
        amount: Double,
        isIncome: Boolean,
        daysAgo: Long = 0
    ) = Transaction(
        id = id,
        amount = amount,
        category = sampleCategory,
        date = LocalDate.now().minusDays(daysAgo),
        isIncome = isIncome
    )

    // ── Balance ──────────────────────────────────────────────────────────────

    @Test
    fun `balance is zero when no transactions`() {
        val transactions = emptyList<Transaction>()
        assertEquals(0.0, calculateBalance(transactions), 0.001)
    }

    @Test
    fun `balance with only income`() {
        val transactions = listOf(
            makeTransaction(1, 500.0, isIncome = true),
            makeTransaction(2, 300.0, isIncome = true)
        )
        assertEquals(800.0, calculateBalance(transactions), 0.001)
    }

    @Test
    fun `balance with only expenses`() {
        val transactions = listOf(
            makeTransaction(1, 200.0, isIncome = false),
            makeTransaction(2, 150.0, isIncome = false)
        )
        assertEquals(-350.0, calculateBalance(transactions), 0.001)
    }

    @Test
    fun `balance with mixed transactions`() {
        val transactions = listOf(
            makeTransaction(1, 1000.0, isIncome = true),
            makeTransaction(2, 200.0,  isIncome = false),
            makeTransaction(3, 50.0,   isIncome = false),
            makeTransaction(4, 500.0,  isIncome = true)
        )
        assertEquals(1250.0, calculateBalance(transactions), 0.001)
    }

    @Test
    fun `balance handles fractional amounts correctly`() {
        val transactions = listOf(
            makeTransaction(1, 99.99, isIncome = true),
            makeTransaction(2, 10.01, isIncome = false)
        )
        assertEquals(89.98, calculateBalance(transactions), 0.001)
    }

    // ── Total Income ─────────────────────────────────────────────────────────

    @Test
    fun `total income sums only income transactions`() {
        val transactions = listOf(
            makeTransaction(1, 500.0, isIncome = true),
            makeTransaction(2, 200.0, isIncome = false),
            makeTransaction(3, 300.0, isIncome = true)
        )
        assertEquals(800.0, calculateTotalIncome(transactions), 0.001)
    }

    @Test
    fun `total expense sums only expense transactions`() {
        val transactions = listOf(
            makeTransaction(1, 500.0, isIncome = true),
            makeTransaction(2, 200.0, isIncome = false),
            makeTransaction(3, 100.0, isIncome = false)
        )
        assertEquals(300.0, calculateTotalExpense(transactions), 0.001)
    }

    // ── Period Date Range ─────────────────────────────────────────────────────

    @Test
    fun `period DAY returns today`() {
        val range = Period.DAY.toDateRange()
        assertEquals(LocalDate.now(), range.start)
        assertEquals(LocalDate.now(), range.end)
    }

    @Test
    fun `period WEEK returns 7 day window`() {
        val range = Period.WEEK.toDateRange()
        assertEquals(LocalDate.now().minusDays(6), range.start)
        assertEquals(LocalDate.now(), range.end)
    }

    @Test
    fun `period MONTH returns current calendar month`() {
        val range = Period.MONTH.toDateRange()
        val today = LocalDate.now()
        assertEquals(today.withDayOfMonth(1), range.start)
        assertEquals(today.withDayOfMonth(today.lengthOfMonth()), range.end)
    }

    @Test
    fun `period YEAR returns current calendar year`() {
        val range = Period.YEAR.toDateRange()
        val today = LocalDate.now()
        assertEquals(today.withDayOfYear(1), range.start)
        assertEquals(today.withDayOfYear(today.lengthOfYear()), range.end)
    }

    // ── Category Grouping ─────────────────────────────────────────────────────

    @Test
    fun `expenses grouped by category correctly`() {
        val cat2 = sampleCategory.copy(id = 2L, name = "Transport")
        val transactions = listOf(
            makeTransaction(1, 100.0, isIncome = false).copy(category = sampleCategory),
            makeTransaction(2, 200.0, isIncome = false).copy(category = sampleCategory),
            makeTransaction(3, 50.0,  isIncome = false).copy(category = cat2),
        )
        val grouped = groupExpensesByCategory(transactions)
        assertEquals(300.0, grouped[sampleCategory.id], 0.001)
        assertEquals(50.0, grouped[cat2.id], 0.001)
    }

    @Test
    fun `grouping excludes income transactions`() {
        val transactions = listOf(
            makeTransaction(1, 500.0, isIncome = true).copy(category = sampleCategory),
            makeTransaction(2, 100.0, isIncome = false).copy(category = sampleCategory),
        )
        val grouped = groupExpensesByCategory(transactions)
        assertEquals(100.0, grouped[sampleCategory.id], 0.001)
        assertNull(grouped[null])
    }

    // ── Helpers (pure functions extracted for testability) ────────────────────

    private fun calculateBalance(transactions: List<Transaction>): Double =
        transactions.sumOf { if (it.isIncome) it.amount else -it.amount }

    private fun calculateTotalIncome(transactions: List<Transaction>): Double =
        transactions.filter { it.isIncome }.sumOf { it.amount }

    private fun calculateTotalExpense(transactions: List<Transaction>): Double =
        transactions.filter { !it.isIncome }.sumOf { it.amount }

    private fun groupExpensesByCategory(transactions: List<Transaction>): Map<Long?, Double> =
        transactions
            .filter { !it.isIncome }
            .groupBy { it.category?.id }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
}
