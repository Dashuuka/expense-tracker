package com.expensetracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.model.Period
import com.expensetracker.data.model.Transaction
import com.expensetracker.data.repository.CurrencyRepository
import com.expensetracker.data.repository.SettingsRepository
import com.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val selectedPeriod: Period = Period.MONTH,
    val displayCurrency: String = "BYN",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val balance: Double get() = totalIncome - totalExpense
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(Period.MONTH)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
        refreshCurrencyRates()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                _selectedPeriod,
                settingsRepository.displayCurrency
            ) { period, currency -> period to currency }
                .flatMapLatest { (period, currency) ->
                    combine(
                        transactionRepository.getTransactionsForPeriod(period),
                        transactionRepository.getTotalIncomeForPeriod(period).map { it ?: 0.0 },
                        transactionRepository.getTotalExpenseForPeriod(period).map { it ?: 0.0 }
                    ) { transactions, income, expense ->
                        HomeUiState(
                            transactions = transactions,
                            totalIncome = income,
                            totalExpense = expense,
                            selectedPeriod = period,
                            displayCurrency = currency,
                            isLoading = false
                        )
                    }
                }
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { state -> _uiState.value = state }
        }
    }

    fun setPeriod(period: Period) {
        _selectedPeriod.value = period
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction.id)
        }
    }

    fun refreshCurrencyRates() {
        viewModelScope.launch {
            currencyRepository.refreshRates()
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
