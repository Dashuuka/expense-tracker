package com.expensetracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.data.model.Period

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Statistics") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector
            item {
                PeriodTabs(selected = uiState.selectedPeriod, onSelected = viewModel::setPeriod)
            }

            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Income",
                        amount = uiState.totalIncome,
                        color = Color(0xFF43A047),
                        icon = Icons.Filled.TrendingUp
                    )
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Expenses",
                        amount = uiState.totalExpense,
                        color = MaterialTheme.colorScheme.error,
                        icon = Icons.Filled.TrendingDown
                    )
                }
            }

            // Category Breakdown
            if (uiState.categoryExpenses.isNotEmpty()) {
                item {
                    Text(
                        "Expenses by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    CategoryPieChart(expenses = uiState.categoryExpenses)
                }
                items(uiState.categoryExpenses) { expense ->
                    CategoryExpenseRow(expense = expense)
                }
            } else {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No expense data for this period",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodTabs(selected: Period, onSelected: (Period) -> Unit) {
    val items = listOf(Period.WEEK to "Week", Period.MONTH to "Month", Period.YEAR to "Year")
    TabRow(selectedTabIndex = items.indexOfFirst { it.first == selected }.coerceAtLeast(0)) {
        items.forEach { (period, label) ->
            Tab(
                selected = selected == period,
                onClick = { onSelected(period) },
                text = { Text(label) }
            )
        }
    }
}

@Composable
fun SummaryStatCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                String.format("%.2f", amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun CategoryPieChart(expenses: List<CategoryExpense>) {
    // Simple custom pie-like bar representation (Vico charts can replace if desired)
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            expenses.take(5).forEach { item ->
                val catColor = item.category?.let { Color(it.color) } ?: Color.Gray
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item.category?.name ?: "Other",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${String.format("%.1f", item.percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { item.percentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = catColor,
                    trackColor = catColor.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun CategoryExpenseRow(expense: CategoryExpense) {
    val catColor = expense.category?.let { Color(it.color) } ?: Color.Gray
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(catColor)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                expense.category?.name ?: "Other",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                String.format("%.2f", expense.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

