package com.expensetracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.model.CurrencyRate
import com.expensetracker.data.repository.CurrencyRepository
import com.expensetracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val displayCurrency: String = "BYN",
    val monthlyBudget: Double = 0.0,
    val isDarkTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val currencyRates: List<CurrencyRate> = emptyList(),
    val isLoadingRates: Boolean = false,
    val ratesError: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.displayCurrency,
        settingsRepository.monthlyBudget,
        settingsRepository.isDarkTheme,
        settingsRepository.notificationsEnabled,
        currencyRepository.rates
    ) { currency, budget, darkTheme, notifications, rates ->
        SettingsUiState(
            displayCurrency = currency,
            monthlyBudget = budget,
            isDarkTheme = darkTheme,
            notificationsEnabled = notifications,
            currencyRates = rates,
            isLoadingRates = currencyRepository.isLoading.value,
            ratesError = currencyRepository.error.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setDisplayCurrency(currency: String) {
        viewModelScope.launch { settingsRepository.setDisplayCurrency(currency) }
    }

    fun setMonthlyBudget(amount: Double) {
        viewModelScope.launch { settingsRepository.setMonthlyBudget(amount) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkTheme(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationsEnabled(enabled) }
    }
}
