package com.expensetracker

import androidx.lifecycle.SavedStateHandle
import com.expensetracker.data.model.Category
import com.expensetracker.data.model.Transaction
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.ui.add.AddEditViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionRepo: TransactionRepository
    private lateinit var categoryRepo: CategoryRepository
    private lateinit var viewModel: AddEditViewModel

    private val categories = listOf(
        Category(1L, "Food",      "restaurant",    0xFFE57373),
        Category(2L, "Transport", "directions_car", 0xFF64B5F6)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionRepo = mockk(relaxed = true)
        categoryRepo    = mockk(relaxed = true)
        every { categoryRepo.getAllCategories() } returns flowOf(categories)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(transactionId: Long = -1L): AddEditViewModel {
        val handle = SavedStateHandle(mapOf("transactionId" to transactionId))
        return AddEditViewModel(transactionRepo, categoryRepo, handle)
    }

    @Test
    fun `initial state has empty amount and today date`() = runTest {
        viewModel = buildViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("", state.amount)
        assertEquals(LocalDate.now(), state.date)
        assertFalse(state.isIncome)
    }

    @Test
    fun `categories loaded from repository`() = runTest {
        viewModel = buildViewModel()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.categories.size)
    }

    @Test
    fun `onAmountChange updates amount`() = runTest {
        viewModel = buildViewModel()
        viewModel.onAmountChange("123.45")
        assertEquals("123.45", viewModel.uiState.value.amount)
    }

    @Test
    fun `onAmountChange rejects invalid amount format`() = runTest {
        viewModel = buildViewModel()
        viewModel.onAmountChange("abc")
        assertEquals("", viewModel.uiState.value.amount)
    }

    @Test
    fun `onAmountChange rejects more than 2 decimal places`() = runTest {
        viewModel = buildViewModel()
        viewModel.onAmountChange("12.345")
        assertEquals("", viewModel.uiState.value.amount)
    }

    @Test
    fun `onAmountChange accepts valid decimal`() = runTest {
        viewModel = buildViewModel()
        viewModel.onAmountChange("99.99")
        assertEquals("99.99", viewModel.uiState.value.amount)
    }

    @Test
    fun `onNoteChange updates note`() = runTest {
        viewModel = buildViewModel()
        viewModel.onNoteChange("Lunch at work")
        assertEquals("Lunch at work", viewModel.uiState.value.note)
    }

    @Test
    fun `onCategorySelected updates category`() = runTest {
        viewModel = buildViewModel()
        advanceUntilIdle()
        val cat = categories[0]
        viewModel.onCategorySelected(cat)
        assertEquals(cat, viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `onDateSelected updates date`() = runTest {
        viewModel = buildViewModel()
        val newDate = LocalDate.of(2025, 6, 15)
        viewModel.onDateSelected(newDate)
        assertEquals(newDate, viewModel.uiState.value.date)
    }

    @Test
    fun `onIsIncomeToggled sets income flag`() = runTest {
        viewModel = buildViewModel()
        viewModel.onIsIncomeToggled(true)
        assertTrue(viewModel.uiState.value.isIncome)
        viewModel.onIsIncomeToggled(false)
        assertFalse(viewModel.uiState.value.isIncome)
    }

    @Test
    fun `saveTransaction shows error when amount is empty`() = runTest {
        viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCategorySelected(categories[0])
        // amount left empty
        viewModel.saveTransaction()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `saveTransaction shows error when amount is zero`() = runTest {
        viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onAmountChange("0")
        viewModel.onCategorySelected(categories[0])
        viewModel.saveTransaction()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `saveTransaction shows error when category not selected`() = runTest {
        viewModel = buildViewModel()
        viewModel.onAmountChange("100.0")
        // no category selected
        viewModel.saveTransaction()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `saveTransaction succeeds with valid input`() = runTest {
        coEvery { transactionRepo.addTransaction(any()) } returns 1L
        viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onAmountChange("150.0")
        viewModel.onCategorySelected(categories[1])
        viewModel.saveTransaction()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSaved)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `edit mode detected from savedStateHandle`() = runTest {
        val existingTx = Transaction(
            id = 10L, amount = 75.0, category = categories[0],
            date = LocalDate.now(), note = "test", isIncome = false
        )
        every { transactionRepo.getAllTransactions() } returns flowOf(listOf(existingTx))

        viewModel = buildViewModel(transactionId = 10L)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEditMode)
    }
}
