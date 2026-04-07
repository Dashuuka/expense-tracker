package com.expensetracker.data.repository

import android.util.Log
import com.expensetracker.data.api.NbrbApi
import com.expensetracker.data.model.CurrencyRate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepository @Inject constructor(
    private val nbrbApi: NbrbApi
) {
    private val _rates = MutableStateFlow<List<CurrencyRate>>(emptyList())
    val rates: StateFlow<List<CurrencyRate>> = _rates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    companion object {
        private val TRACKED_CURRENCIES = listOf("USD", "EUR", "RUB", "CNY", "GBP")
        private const val TAG = "CurrencyRepository"
    }

    suspend fun refreshRates() {
        _isLoading.value = true
        _error.value = null
        try {
            val allRates = nbrbApi.getRates()
            val filtered = allRates
                .filter { it.abbreviation in TRACKED_CURRENCIES }
                .map { rate ->
                    CurrencyRate(
                        abbreviation = rate.abbreviation,
                        name = rate.name,
                        scale = rate.scale,
                        rate = rate.officialRate
                    )
                }
            _rates.value = filtered
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch rates", e)
            _error.value = e.message ?: "Unknown error fetching currency rates"
        } finally {
            _isLoading.value = false
        }
    }

    fun getRateForCurrency(abbreviation: String): CurrencyRate? =
        _rates.value.find { it.abbreviation == abbreviation }

    fun convertAmount(amount: Double, fromByn: Boolean, currencyAbbr: String): Double {
        val rate = getRateForCurrency(currencyAbbr) ?: return amount
        return if (fromByn) rate.convertFromByn(amount) else rate.convertToByn(amount)
    }
}
