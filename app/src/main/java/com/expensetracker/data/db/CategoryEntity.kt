package com.expensetracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,       // material icon name
    val color: Long,        // ARGB color as Long
    val isDefault: Boolean = false
)
