package com.example.expenseapp

import com.example.expenseapp.storage.FakeDataStoreManager
import com.example.expenseapp.viewmodel.ExpenseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    private lateinit var viewModel: ExpenseViewModel

    @Before
    fun setUp() {
        viewModel = ExpenseViewModel(FakeDataStoreManager())
    }

    // ── add ──────────────────────────────────────────────────

    @Test
    fun `add valid expense increases count`() = runTest {
        val before = viewModel.filteredExpenses.value.size
        viewModel.addExpense(5000, "식비", "점심")
        assertEquals(before + 1, viewModel.filteredExpenses.value.size)
    }

    @Test
    fun `add zero amount does not add`() = runTest {
        val before = viewModel.filteredExpenses.value.size
        viewModel.addExpense(0, "식비", "")
        assertEquals(before, viewModel.filteredExpenses.value.size)
    }

    @Test
    fun `add over limit does not add`() = runTest {
        val before = viewModel.filteredExpenses.value.size
        viewModel.addExpense(99_999_999, "식비", "")
        assertEquals(before, viewModel.filteredExpenses.value.size)
    }

    // ── totalAmount ──────────────────────────────────────────

    @Test
    fun `total amount is correct after add`() = runTest {
        viewModel.addExpense(3000, "카페", "")
        viewModel.addExpense(7000, "식비", "")
        assertEquals(10000, viewModel.totalAmount.value)
    }

    // ── delete / undo ────────────────────────────────────────

    @Test
    fun `delete expense removes from list`() = runTest {
        viewModel.addExpense(5000, "교통", "버스")
        val expense = viewModel.filteredExpenses.value.first()
        viewModel.deleteExpense(expense)
        assertFalse(viewModel.filteredExpenses.value.any { it.id == expense.id })
    }

    @Test
    fun `undo delete restores expense`() = runTest {
        viewModel.addExpense(5000, "교통", "버스")
        val expense = viewModel.filteredExpenses.value.first()
        viewModel.deleteExpense(expense)
        viewModel.undoDelete(expense)
        assertTrue(viewModel.filteredExpenses.value.any { it.id == expense.id })
    }

    // ── filter ───────────────────────────────────────────────

    @Test
    fun `filter by category returns only matching`() = runTest {
        viewModel.addExpense(1000, "식비", "")
        viewModel.addExpense(500,  "교통", "")
        viewModel.addExpense(800,  "식비", "")
        viewModel.setFilterCategory("식비")
        val result = viewModel.filteredExpenses.value
        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "식비" })
    }

    @Test
    fun `filter null returns all`() = runTest {
        viewModel.addExpense(1000, "식비", "")
        viewModel.addExpense(500,  "교통", "")
        viewModel.setFilterCategory(null)
        assertEquals(2, viewModel.filteredExpenses.value.size)
    }

    // ── save / load ──────────────────────────────────────────

    @Test
    fun `save and reload persists expenses`() = runTest {
        val storage = FakeDataStoreManager()
        val vm1 = ExpenseViewModel(storage)
        vm1.addExpense(12000, "식비", "저장 테스트")

        val vm2 = ExpenseViewModel(storage)
        assertEquals(1, vm2.filteredExpenses.value.size)
        assertEquals(12000, vm2.filteredExpenses.value.first().amount)
    }

    // ── update ───────────────────────────────────────────────

    @Test
    fun `update changes expense amount`() = runTest {
        viewModel.addExpense(5000, "식비", "원래 메모")
        val original = viewModel.filteredExpenses.value.first()
        val updated  = original.copy(amount = 9999, note = "수정된 메모")
        viewModel.updateExpense(updated)
        val found = viewModel.filteredExpenses.value.find { it.id == original.id }
        assertNotNull(found)
        assertEquals(9999, found!!.amount)
        assertEquals("수정된 메모", found.note)
    }
}
