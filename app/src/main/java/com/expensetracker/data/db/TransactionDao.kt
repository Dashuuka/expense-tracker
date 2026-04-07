package com.expensetracker.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE isIncome = 1 AND date >= :startDate AND date <= :endDate
    """)
    fun getTotalIncomeForPeriod(startDate: LocalDate, endDate: LocalDate): Flow<Double?>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE isIncome = 0 AND date >= :startDate AND date <= :endDate
    """)
    fun getTotalExpenseForPeriod(startDate: LocalDate, endDate: LocalDate): Flow<Double?>

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM transactions 
        WHERE isIncome = 0 AND date >= :startDate AND date <= :endDate
        GROUP BY categoryId
    """)
    fun getExpensesByCategoryForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<CategorySum>>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE isIncome = 0 AND categoryId = :categoryId 
        AND date >= :startDate AND date <= :endDate
    """)
    suspend fun getSpentForCategory(
        categoryId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class CategorySum(
    val categoryId: Long?,
    val total: Double
)
