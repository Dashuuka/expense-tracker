package com.expensetracker.data.model

import java.time.LocalDate

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val category: Category?,
    val date: LocalDate,
    val note: String = "",
    val isIncome: Boolean = false
)

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Long,
    val isDefault: Boolean = false
)

data class Budget(
    val categoryId: Long,
    val month: String,
    val limitAmount: Double,
    val spentAmount: Double = 0.0
) {
    val remainingAmount: Double get() = limitAmount - spentAmount
    val progressFraction: Float get() = if (limitAmount > 0) (spentAmount / limitAmount).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget: Boolean get() = spentAmount > limitAmount
}

data class CurrencyRate(
    val abbreviation: String,
    val name: String,
    val scale: Int,
    val rate: Double,
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** Convert [amount] in BYN to this currency */
    fun convertFromByn(amountByn: Double): Double =
        amountByn / rate * scale

    /** Convert [amount] in this currency to BYN */
    fun convertToByn(amount: Double): Double =
        amount * rate / scale
}

enum class Period { DAY, WEEK, MONTH, YEAR, ALL }

data class PeriodRange(val start: LocalDate, val end: LocalDate)

fun Period.toDateRange(): PeriodRange {
    val today = LocalDate.now()
    return when (this) {
        Period.DAY   -> PeriodRange(today, today)
        Period.WEEK  -> PeriodRange(today.minusDays(6), today)
        Period.MONTH -> PeriodRange(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()))
        Period.YEAR  -> PeriodRange(today.withDayOfYear(1), today.withDayOfYear(today.lengthOfYear()))
        Period.ALL   -> PeriodRange(LocalDate.of(2000, 1, 1), today)
    }
}
