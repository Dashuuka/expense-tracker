package com.expensetracker

import com.expensetracker.data.model.Budget
import org.junit.Assert.*
import org.junit.Test

class BudgetTest {

    // ── remainingAmount ───────────────────────────────────────────────────────

    @Test
    fun `remaining amount is positive when under budget`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 300.0)
        assertEquals(200.0, budget.remainingAmount, 0.001)
    }

    @Test
    fun `remaining amount is zero when exactly on budget`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 500.0)
        assertEquals(0.0, budget.remainingAmount, 0.001)
    }

    @Test
    fun `remaining amount is negative when over budget`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 600.0)
        assertEquals(-100.0, budget.remainingAmount, 0.001)
    }

    // ── progressFraction ─────────────────────────────────────────────────────

    @Test
    fun `progress fraction is zero at start`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 0.0)
        assertEquals(0f, budget.progressFraction, 0.001f)
    }

    @Test
    fun `progress fraction is 0_5 at half spent`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 250.0)
        assertEquals(0.5f, budget.progressFraction, 0.001f)
    }

    @Test
    fun `progress fraction is 1_0 when fully spent`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 500.0)
        assertEquals(1.0f, budget.progressFraction, 0.001f)
    }

    @Test
    fun `progress fraction is capped at 1_0 when over budget`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 750.0)
        assertEquals(1.0f, budget.progressFraction, 0.001f)
    }

    @Test
    fun `progress fraction is zero when limit is zero`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 0.0, spentAmount = 0.0)
        assertEquals(0f, budget.progressFraction, 0.001f)
    }

    // ── isOverBudget ──────────────────────────────────────────────────────────

    @Test
    fun `isOverBudget is false when under limit`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 499.99)
        assertFalse(budget.isOverBudget)
    }

    @Test
    fun `isOverBudget is false when exactly at limit`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 500.0)
        assertFalse(budget.isOverBudget)
    }

    @Test
    fun `isOverBudget is true when over limit`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 500.01)
        assertTrue(budget.isOverBudget)
    }

    // ── Warning threshold ────────────────────────────────────────────────────

    @Test
    fun `warning threshold at 90 percent`() {
        val budget = Budget(categoryId = 1, month = "2025-01", limitAmount = 500.0, spentAmount = 450.0)
        assertTrue(budget.progressFraction >= 0.9f)
        assertFalse(budget.isOverBudget)
    }
}
