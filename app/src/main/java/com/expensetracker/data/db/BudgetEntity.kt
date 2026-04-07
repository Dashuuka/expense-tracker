package com.expensetracker.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "budgets",
    primaryKeys = ["categoryId", "month"],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class BudgetEntity(
    val categoryId: Long,
    val month: String,   // format: "YYYY-MM"
    val limitAmount: Double
)
