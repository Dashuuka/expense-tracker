package com.expensetracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var budgetText by remember(uiState.monthlyBudget) {
        mutableStateOf(if (uiState.monthlyBudget > 0) uiState.monthlyBudget.toString() else "")
    }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            current = uiState.displayCurrency,
            onSelected = { viewModel.setDisplayCurrency(it); showCurrencyDialog = false },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Preferences ───────────────────────────────────────────────────
            item { SectionHeader("Preferences") }

            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Filled.CurrencyExchange,
                        title = "Display Currency",
                        subtitle = uiState.displayCurrency,
                        onClick = { showCurrencyDialog = true }
                    )
                    HorizontalDivider()
                    // Monthly budget
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Monthly Budget", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("Set your global monthly spending limit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = budgetText,
                            onValueChange = { v ->
                                if (v.isEmpty() || v.matches(Regex("^\\d{0,10}(\\.\\d{0,2})?\$"))) {
                                    budgetText = v
                                    v.toDoubleOrNull()?.let { viewModel.setMonthlyBudget(it) }
                                }
                            },
                            label = { Text("Budget (BYN)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── Appearance ────────────────────────────────────────────────────
            item { SectionHeader("Appearance") }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.DarkMode, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Switch(checked = uiState.isDarkTheme, onCheckedChange = viewModel::setDarkTheme)
                    }
                }
            }

            // ── Notifications ─────────────────────────────────────────────────
            item { SectionHeader("Notifications") }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Budget & Rate Alerts", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Get notified about rate updates and budget limits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = uiState.notificationsEnabled, onCheckedChange = viewModel::setNotificationsEnabled)
                    }
                }
            }

            // ── Exchange Rates ────────────────────────────────────────────────
            item { SectionHeader("Live Exchange Rates (NBRB)") }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rates vs BYN", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (uiState.isLoadingRates) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = viewModel::refreshRates) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Refresh rates")
                            }
                        }
                    }
                    if (uiState.ratesError != null) {
                        Text(
                            "Error: ${uiState.ratesError}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    uiState.currencyRates.forEach { rate ->
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(rate.abbreviation, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(rate.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "${String.format("%.4f", rate.rate)} BYN${if (rate.scale != 1) "/${rate.scale}" else ""}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    if (uiState.currencyRates.isEmpty() && !uiState.isLoadingRates) {
                        Text(
                            "Tap refresh to load rates",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CurrencyPickerDialog(current: String, onSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val options = listOf("BYN", "USD", "EUR", "RUB", "CNY", "GBP")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column {
                options.forEach { currency ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == currency,
                            onClick = { onSelected(currency) }
                        )
                        Text(currency, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
