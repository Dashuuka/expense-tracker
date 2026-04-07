package com.expensetracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, BudgetEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "expense_tracker.db"

        fun buildCallback(getDb: () -> AppDatabase) = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    getDb().categoryDao().insertAll(defaultCategories())
                }
            }
        }

        fun defaultCategories() = listOf(
            CategoryEntity(name = "Food & Dining",   icon = "restaurant",     color = 0xFFE57373, isDefault = true),
            CategoryEntity(name = "Transport",        icon = "directions_car", color = 0xFF64B5F6, isDefault = true),
            CategoryEntity(name = "Shopping",         icon = "shopping_bag",   color = 0xFFBA68C8, isDefault = true),
            CategoryEntity(name = "Entertainment",    icon = "movie",          color = 0xFFFFB74D, isDefault = true),
            CategoryEntity(name = "Health",           icon = "local_hospital", color = 0xFF81C784, isDefault = true),
            CategoryEntity(name = "Utilities",        icon = "bolt",           color = 0xFFFFF176, isDefault = true),
            CategoryEntity(name = "Housing",          icon = "home",           color = 0xFF4DB6AC, isDefault = true),
            CategoryEntity(name = "Education",        icon = "school",         color = 0xFFA1887F, isDefault = true),
            CategoryEntity(name = "Travel",           icon = "flight",         color = 0xFF4FC3F7, isDefault = true),
            CategoryEntity(name = "Other",            icon = "more_horiz",     color = 0xFF90A4AE, isDefault = true),
            CategoryEntity(name = "Salary",           icon = "work",           color = 0xFF66BB6A, isDefault = true),
            CategoryEntity(name = "Freelance",        icon = "laptop",         color = 0xFF26C6DA, isDefault = true)
        )
    }
}
