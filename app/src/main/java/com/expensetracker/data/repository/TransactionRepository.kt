package com.expensetracker.data.repository

import com.expensetracker.data.db.CategoryDao
import com.expensetracker.data.db.TransactionDao
import com.expensetracker.data.db.TransactionEntity
import com.expensetracker.data.model.Category
import com.expensetracker.data.model.Period
import com.expensetracker.data.model.Transaction
import com.expensetracker.data.model.toDateRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    fun getTransactionsForPeriod(period: Period): Flow<List<Transaction>> {
        val range = period.toDateRange()
        return getTransactionsForRange(range.start, range.end)
    }

    fun getTransactionsForRange(start: LocalDate, end: LocalDate): Flow<List<Transaction>> =
        combine(
            transactionDao.getTransactionsByDateRange(start, end),
            categoryDao.getAllCategories()
        ) { transactions, categories ->
            val categoryMap = categories.associateBy { it.id }
            transactions.map { entity ->
                Transaction(
                    id = entity.id,
                    amount = entity.amount,
                    category = entity.categoryId?.let { id ->
                        categoryMap[id]?.let { cat ->
                            Category(id = cat.id, name = cat.name, icon = cat.icon,
                                     color = cat.color, isDefault = cat.isDefault)
                        }
                    },
                    date = entity.date,
                    note = entity.note,
                    isIncome = entity.isIncome
                )
            }
        }

    fun getAllTransactions(): Flow<List<Transaction>> =
        combine(
            transactionDao.getAllTransactions(),
            categoryDao.getAllCategories()
        ) { transactions, categories ->
            val categoryMap = categories.associateBy { it.id }
            transactions.map { entity ->
                Transaction(
                    id = entity.id,
                    amount = entity.amount,
                    category = entity.categoryId?.let { id ->
                        categoryMap[id]?.let { cat ->
                            Category(id = cat.id, name = cat.name, icon = cat.icon,
                                     color = cat.color, isDefault = cat.isDefault)
                        }
                    },
                    date = entity.date,
                    note = entity.note,
                    isIncome = entity.isIncome
                )
            }
        }

    fun getTotalIncomeForPeriod(period: Period): Flow<Double?> {
        val range = period.toDateRange()
        return transactionDao.getTotalIncomeForPeriod(range.start, range.end)
    }

    fun getTotalExpenseForPeriod(period: Period): Flow<Double?> {
        val range = period.toDateRange()
        return transactionDao.getTotalExpenseForPeriod(range.start, range.end)
    }

    fun getExpensesByCategoryForPeriod(period: Period): Flow<Map<Long?, Double>> {
        val range = period.toDateRange()
        return transactionDao
            .getExpensesByCategoryForPeriod(range.start, range.end)
            .map { sums -> sums.associate { it.categoryId to it.total } }
    }

    suspend fun addTransaction(transaction: Transaction): Long =
        transactionDao.insert(transaction.toEntity())

    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.update(transaction.toEntity())

    suspend fun deleteTransaction(id: Long) =
        transactionDao.deleteById(id)

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        amount = amount,
        categoryId = category?.id,
        date = date,
        note = note,
        isIncome = isIncome
    )
}
