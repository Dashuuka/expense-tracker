package com.expensetracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_CURRENCY        = stringPreferencesKey("display_currency")
        val KEY_MONTHLY_BUDGET  = doublePreferencesKey("monthly_budget")
        val KEY_DARK_THEME      = booleanPreferencesKey("dark_theme")
        val KEY_NOTIFICATIONS   = booleanPreferencesKey("notifications_enabled")

        const val DEFAULT_CURRENCY       = "BYN"
        const val DEFAULT_MONTHLY_BUDGET = 0.0
    }

    val displayCurrency: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_CURRENCY] ?: DEFAULT_CURRENCY }

    val monthlyBudget: Flow<Double> = context.dataStore.data
        .map { prefs -> prefs[KEY_MONTHLY_BUDGET] ?: DEFAULT_MONTHLY_BUDGET }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_DARK_THEME] ?: false }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_NOTIFICATIONS] ?: true }

    suspend fun setDisplayCurrency(currency: String) {
        context.dataStore.edit { it[KEY_CURRENCY] = currency }
    }

    suspend fun setMonthlyBudget(amount: Double) {
        context.dataStore.edit { it[KEY_MONTHLY_BUDGET] = amount }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }
}
