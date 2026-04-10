package com.expensetracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.model.Category
import com.expensetracker.data.model.Period
import com.expensetracker.data.model.Transaction
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CategoryExpense(
    val category: Category?,
    val amount: Double,
    val percentage: Float
)

data class DailyTotal(
    val date: LocalDate,
    val income: Double,
    val expense: Double
)

data class StatsUiState(
    val selectedPeriod: Period = Period.MONTH,
    val categoryExpenses: List<CategoryExpense> = emptyList(),
    val dailyTotals: List<DailyTotal> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = false
) {
    val balance: Double get() = totalIncome - totalExpense
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(Period.MONTH)

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedPeriod.flatMapLatest { period ->
                combine(
                    transactionRepository.getTransactionsForPeriod(period),
                    categoryRepository.getAllCategories()
                ) { transactions, categories ->
                    buildUiState(period, transactions, categories)
                }
            }.collect { state -> _uiState.value = state }
        }
    }

    private fun buildUiState(
        period: Period,
        transactions: List<Transaction>,
        categories: List<Category>
    ): StatsUiState {
        val categoryMap = categories.associateBy { it.id }
        val expenses = transactions.filter { !it.isIncome }
        val incomes  = transactions.filter {  it.isIncome }

        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome  = incomes.sumOf  { it.amount }

        val byCategory = expenses
            .groupBy { it.category?.id }
            .map { (catId, txList) ->
                CategoryExpense(
                    category   = catId?.let { categoryMap[it] } ?: txList.firstOrNull()?.category,
                    amount     = txList.sumOf { it.amount },
                    percentage = if (totalExpense > 0)
                        (txList.sumOf { it.amount } / totalExpense * 100).toFloat() else 0f
                )
            }
            .sortedByDescending { it.amount }

        val dailyTotals = transactions
            .groupBy { it.date }
            .map { (date, txList) ->
                DailyTotal(
                    date    = date,
                    income  = txList.filter {  it.isIncome }.sumOf { it.amount },
                    expense = txList.filter { !it.isIncome }.sumOf { it.amount }
                )
            }
            .sortedBy { it.date }

        return StatsUiState(
            selectedPeriod   = period,
            categoryExpenses = byCategory,
            dailyTotals      = dailyTotals,
            totalIncome      = totalIncome,
            totalExpense     = totalExpense,
            isLoading        = false
        )
    }

    fun setPeriod(period: Period) {
        _selectedPeriod.value = period
    }
}
