package com.expensetracker.data.repository

import com.expensetracker.data.db.BudgetDao
import com.expensetracker.data.db.BudgetEntity
import com.expensetracker.data.db.TransactionDao
import com.expensetracker.data.model.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) {
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    fun getBudgetsForMonth(yearMonth: YearMonth): Flow<List<Budget>> {
        val monthStr = yearMonth.format(monthFormatter)
        val start    = yearMonth.atDay(1)
        val end      = yearMonth.atEndOfMonth()
        return budgetDao.getBudgetsForMonth(monthStr).map { budgets ->
            budgets.map { entity ->
                val spent = transactionDao.getSpentForCategory(entity.categoryId, start, end) ?: 0.0
                Budget(
                    categoryId   = entity.categoryId,
                    month        = entity.month,
                    limitAmount  = entity.limitAmount,
                    spentAmount  = spent
                )
            }
        }
    }

    fun getCurrentMonthBudgets(): Flow<List<Budget>> =
        getBudgetsForMonth(YearMonth.now())

    suspend fun setBudget(categoryId: Long, limitAmount: Double) {
        val month = YearMonth.now().format(monthFormatter)
        budgetDao.insert(BudgetEntity(categoryId, month, limitAmount))
    }

    suspend fun deleteBudget(categoryId: Long) {
        val month = YearMonth.now().format(monthFormatter)
        budgetDao.delete(categoryId, month)
    }
}
