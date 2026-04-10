package com.expensetracker.ui.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.model.Category
import com.expensetracker.data.model.Transaction
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditUiState(
    val amount: String = "",
    val note: String = "",
    val selectedCategory: Category? = null,
    val date: LocalDate = LocalDate.now(),
    val isIncome: Boolean = false,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
)

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = savedStateHandle["transactionId"] ?: -1L

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        if (transactionId > 0) loadExistingTransaction()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .collect { cats -> _uiState.update { it.copy(categories = cats) } }
        }
    }

    private fun loadExistingTransaction() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isEditMode = true) }
            // Get from combined repo (single emission is enough)
            transactionRepository.getAllTransactions()
                .map { list -> list.find { t -> t.id == transactionId } }
                .filterNotNull()
                .take(1)
                .collect { tx ->
                    _uiState.update { state ->
                        state.copy(
                            amount = tx.amount.toString(),
                            note = tx.note,
                            selectedCategory = tx.category,
                            date = tx.date,
                            isIncome = tx.isIncome,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onAmountChange(value: String) {
        // Allow only valid decimal numbers
        if (value.isEmpty() || value.matches(Regex("^\\d{0,10}(\\.\\d{0,2})?\$"))) {
            _uiState.update { it.copy(amount = value, error = null) }
        }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onCategorySelected(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onIsIncomeToggled(isIncome: Boolean) {
        _uiState.update { it.copy(isIncome = isIncome) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val amountDouble = state.amount.toDoubleOrNull()

        if (amountDouble == null || amountDouble <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }
        if (state.selectedCategory == null) {
            _uiState.update { it.copy(error = "Please select a category") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val transaction = Transaction(
                id = if (transactionId > 0) transactionId else 0,
                amount = amountDouble,
                category = state.selectedCategory,
                date = state.date,
                note = state.note,
                isIncome = state.isIncome
            )
            if (state.isEditMode) {
                transactionRepository.updateTransaction(transaction)
            } else {
                transactionRepository.addTransaction(transaction)
            }
            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}
