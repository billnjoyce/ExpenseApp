package com.example.expenseapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expenseapp.firebase.FirestoreManager
import com.example.expenseapp.model.Expense
import com.example.expenseapp.model.ExpenseConstants
import com.example.expenseapp.storage.DataStoreManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(private val dataStore: DataStoreManager) : ViewModel() {

    private val _expenses       = MutableStateFlow<List<Expense>>(emptyList())
    private val _filterCategory = MutableStateFlow<String?>(null)
    private val _searchText     = MutableStateFlow("")
    private val _undoEvent      = MutableSharedFlow<Expense>()
    private val _errorMessage   = MutableSharedFlow<String>()

    val undoEvent      = _undoEvent.asSharedFlow()
    val errorMessage   = _errorMessage.asSharedFlow()
    val filterCategory = _filterCategory.asStateFlow()
    val searchText     = _searchText.asStateFlow()

    val filteredExpenses: StateFlow<List<Expense>> = combine(
        _expenses, _filterCategory, _searchText
    ) { expenses, cat, query ->
        var r = expenses
        if (cat != null) r = r.filter { it.category == cat }
        if (query.isNotBlank()) r = r.filter {
            it.category.contains(query, true) || it.note.contains(query, true)
        }
        r
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAmount: StateFlow<Int> = filteredExpenses
        .map { it.sumOf { e -> e.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    data class CategoryStat(val category: String, val total: Int, val percent: Float, val emoji: String)

    val categoryStats: StateFlow<List<CategoryStat>> = _expenses.map { list ->
        val total = list.sumOf { it.amount }.toFloat().takeIf { it > 0f } ?: 1f
        list.groupBy { it.category }.map { (cat, items) ->
            CategoryStat(cat, items.sumOf { it.amount }.also { s -> s }, items.sumOf { it.amount }, items.sumOf { it.amount } / total * 100f, items.first().categoryEmoji)
        }.sortedByDescending { it.total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val thisMonthTotal: StateFlow<Int> = _expenses.map { list ->
        val now = Calendar.getInstance()
        list.filter {
            Calendar.getInstance().apply { timeInMillis = it.date }.let { c ->
                c.get(Calendar.YEAR) == now.get(Calendar.YEAR) && c.get(Calendar.MONTH) == now.get(Calendar.MONTH)
            }
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        if (dataStore is FirestoreManager) {
            viewModelScope.launch { dataStore.expensesFlow.collect { _expenses.value = it } }
        } else {
            viewModelScope.launch { _expenses.value = dataStore.load() }
        }
    }

    fun addExpense(amount: Int, category: String, note: String) {
        when {
            amount <= 0                          -> emitError("0원보다 큰 금액을 입력해주세요")
            amount > ExpenseConstants.MAX_AMOUNT -> emitError("10,000,000원 이하로 입력해주세요")
            else -> {
                val expense = Expense(amount = amount, category = category, note = note.take(ExpenseConstants.MAX_NOTE_LENGTH))
                _expenses.update { listOf(expense) + it }
                if (dataStore is FirestoreManager) {
                    viewModelScope.launch {
                        dataStore.addExpense(expense).onFailure {
                            _expenses.update { list -> list.filterNot { e -> e.id == expense.id } }
                            emitError("저장 실패: ${it.message}")
                        }
                    }
                } else persist()
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        _expenses.update { it.filterNot { e -> e.id == expense.id } }
        if (dataStore is FirestoreManager) {
            viewModelScope.launch {
                dataStore.deleteExpense(expense).onFailure {
                    _expenses.update { list -> listOf(expense) + list }
                    emitError("삭제 실패: ${it.message}")
                }
                _undoEvent.emit(expense)
            }
        } else {
            persist()
            viewModelScope.launch { _undoEvent.emit(expense) }
        }
    }

    fun undoDelete(expense: Expense) {
        _expenses.update { listOf(expense) + it }
        if (dataStore is FirestoreManager) {
            viewModelScope.launch { dataStore.addExpense(expense) }
        } else persist()
    }

    fun updateExpense(expense: Expense) {
        if (expense.amount <= 0) return
        _expenses.update { list -> list.map { if (it.id == expense.id) expense else it } }
        if (dataStore is FirestoreManager) {
            viewModelScope.launch {
                dataStore.updateExpense(expense).onFailure { emitError("수정 실패: ${it.message}") }
            }
        } else persist()
    }

    fun setFilterCategory(cat: String?) { _filterCategory.value = cat }
    fun setSearchText(query: String)    { _searchText.value = query }

    private fun persist() { viewModelScope.launch { dataStore.save(_expenses.value) } }
    private fun emitError(msg: String) { viewModelScope.launch { _errorMessage.emit(msg) } }
}

class ExpenseViewModelFactory(private val dataStore: DataStoreManager) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ExpenseViewModel(dataStore) as T
}
