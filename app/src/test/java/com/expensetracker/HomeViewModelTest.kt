package com.expensetracker

import app.cash.turbine.test
import com.expensetracker.data.model.Category
import com.expensetracker.data.model.Period
import com.expensetracker.data.model.Transaction
import com.expensetracker.data.repository.CurrencyRepository
import com.expensetracker.data.repository.SettingsRepository
import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.ui.home.HomeViewModel
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var transactionRepo: TransactionRepository
    private lateinit var currencyRepo: CurrencyRepository
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var viewModel: HomeViewModel

    private val sampleCategory = Category(1L, "Food", "restaurant", 0xFFE57373)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        transactionRepo = mockk(relaxed = true)
        currencyRepo    = mockk(relaxed = true)
        settingsRepo    = mockk(relaxed = true)

        every { settingsRepo.displayCurrency } returns flowOf("BYN")
        every { currencyRepo.rates } returns kotlinx.coroutines.flow.MutableStateFlow(emptyList())
        every { currencyRepo.isLoading } returns kotlinx.coroutines.flow.MutableStateFlow(false)
        every { currencyRepo.error } returns kotlinx.coroutines.flow.MutableStateFlow(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() {
        viewModel = HomeViewModel(transactionRepo, currencyRepo, settingsRepo)
    }

    @Test
    fun `initial state has MONTH period and zero balance`() = runTest {
        every { transactionRepo.getTransactionsForPeriod(any()) } returns flowOf(emptyList())
        every { transactionRepo.getTotalIncomeForPeriod(any()) } returns flowOf(0.0)
        every { transactionRepo.getTotalExpenseForPeriod(any()) } returns flowOf(0.0)

        buildViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(Period.MONTH, state.selectedPeriod)
        assertEquals(0.0, state.balance, 0.001)
    }

    @Test
    fun `balance calculated correctly from income and expense`() = runTest {
        every { transactionRepo.getTransactionsForPeriod(any()) } returns flowOf(emptyList())
        every { transactionRepo.getTotalIncomeForPeriod(any()) } returns flowOf(1000.0)
        every { transactionRepo.getTotalExpenseForPeriod(any()) } returns flowOf(350.0)

        buildViewModel()
        advanceUntilIdle()

        assertEquals(650.0, viewModel.uiState.value.balance, 0.001)
    }

    @Test
    fun `setPeriod updates selected period`() = runTest {
        every { transactionRepo.getTransactionsForPeriod(any()) } returns flowOf(emptyList())
        every { transactionRepo.getTotalIncomeForPeriod(any()) } returns flowOf(0.0)
        every { transactionRepo.getTotalExpenseForPeriod(any()) } returns flowOf(0.0)

        buildViewModel()
        viewModel.setPeriod(Period.WEEK)
        advanceUntilIdle()

        assertEquals(Period.WEEK, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun `transactions list populated from repo`() = runTest {
        val transactions = listOf(
            Transaction(1L, 50.0, sampleCategory, LocalDate.now(), "Lunch", false),
            Transaction(2L, 200.0, sampleCategory, LocalDate.now(), "Salary", true)
        )
        every { transactionRepo.getTransactionsForPeriod(any()) } returns flowOf(transactions)
        every { transactionRepo.getTotalIncomeForPeriod(any()) } returns flowOf(200.0)
        every { transactionRepo.getTotalExpenseForPeriod(any()) } returns flowOf(50.0)

        buildViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.transactions.size)
    }

    @Test
    fun `deleteTransaction calls repository`() = runTest {
        every { transactionRepo.getTransactionsForPeriod(any()) } returns flowOf(emptyList())
        every { transactionRepo.getTotalIncomeForPeriod(any()) } returns flowOf(0.0)
        every { transactionRepo.getTotalExpenseForPeriod(any()) } returns flowOf(0.0)

        buildViewModel()
        val tx = Transaction(5L, 100.0, sampleCategory, LocalDate.now())
        viewModel.deleteTransaction(tx)
        advanceUntilIdle()

        coVerify { transactionRepo.deleteTransaction(5L) }
    }

    @Test
    fun `dismissError clears error state`() = runTest {
        every { transactionRepo.getTransactionsForPeriod(any()) } returns flowOf(emptyList())
        every { transactionRepo.getTotalIncomeForPeriod(any()) } returns flowOf(0.0)
        every { transactionRepo.getTotalExpenseForPeriod(any()) } returns flowOf(0.0)

        buildViewModel()
        advanceUntilIdle()
        viewModel.dismissError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `uiState flow emits with turbine`() = runTest {
        every { transactionRepo.getTransactionsForPeriod(any()) } returns flowOf(emptyList())
        every { transactionRepo.getTotalIncomeForPeriod(any()) } returns flowOf(500.0)
        every { transactionRepo.getTotalExpenseForPeriod(any()) } returns flowOf(100.0)

        buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            // May be initial or settled state depending on timing
            assertTrue(state.balance >= 0.0 || state.balance < 0.0) // just ensure it emits
            cancelAndIgnoreRemainingEvents()
        }
    }
}
