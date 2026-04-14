package com.example.expenseapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expenseapp.model.Expense
import com.example.expenseapp.model.ExpenseConstants
import com.example.expenseapp.storage.DataStoreManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(
    private val dataStore: DataStoreManager
) : ViewModel() {

    // ── 원본 상태 ─────────────────────────────────────────────
    private val _expenses       = MutableStateFlow<List<Expense>>(emptyList())
    private val _filterCategory = MutableStateFlow<String?>(null)
    private val _searchText     = MutableStateFlow("")

    // ── 이벤트 (일회성) ──────────────────────────────────────
    private val _undoEvent    = MutableSharedFlow<Expense>()
    private val _errorMessage = MutableSharedFlow<String>()
    val undoEvent    = _undoEvent.asSharedFlow()
    val errorMessage = _errorMessage.asSharedFlow()

    // ── 노출 상태 ─────────────────────────────────────────────
    val filterCategory = _filterCategory.asStateFlow()
    val searchText     = _searchText.asStateFlow()

    // ── 파생 상태: 필터 + 검색 (3개 Flow 결합) ───────────────
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        _expenses, _filterCategory, _searchText
    ) { expenses, cat, query ->
        var result = expenses
        if (cat != null) result = result.filter { it.category == cat }
        if (query.isNotBlank()) result = result.filter {
            it.category.contains(query, ignoreCase = true) ||
            it.note.contains(query, ignoreCase = true)
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── 총액 ─────────────────────────────────────────────────
    val totalAmount: StateFlow<Int> = filteredExpenses
        .map { it.sumOf { e -> e.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── 통계 ─────────────────────────────────────────────────
    data class CategoryStat(
        val category: String,
        val total:    Int,
        val percent:  Float,
        val emoji:    String
    )

    val categoryStats: StateFlow<List<CategoryStat>> = _expenses.map { list ->
        val total = list.sumOf { it.amount }.toFloat().takeIf { it > 0f } ?: 1f
        list.groupBy { it.category }.map { (cat, items) ->
            val sum = items.sumOf { it.amount }
            CategoryStat(cat, sum, sum / total * 100f, items.first().categoryEmoji)
        }.sortedByDescending { it.total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val thisMonthTotal: StateFlow<Int> = _expenses.map { list ->
        val now = Calendar.getInstance()
        list.filter {
            Calendar.getInstance().apply { timeInMillis = it.date }.let { c ->
                c.get(Calendar.YEAR)  == now.get(Calendar.YEAR) &&
                c.get(Calendar.MONTH) == now.get(Calendar.MONTH)
            }
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── 초기 로드 ────────────────────────────────────────────
    init {
        viewModelScope.launch {
            _expenses.value = dataStore.load()
        }
    }

    // ── CRUD ─────────────────────────────────────────────────
    fun addExpense(amount: Int, category: String, note: String) {
        when {
            amount <= 0               -> emitError("0원보다 큰 금액을 입력해주세요")
            amount > ExpenseConstants.MAX_AMOUNT -> emitError("10,000,000원 이하로 입력해주세요")
            else -> {
                _expenses.update {
                    listOf(
                        Expense(
                            amount   = amount,
                            category = category,
                            note     = note.take(ExpenseConstants.MAX_NOTE_LENGTH)
                        )
                    ) + it
                }
                persist()
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        _expenses.update { it.filterNot { e -> e.id == expense.id } }
        persist()
        viewModelScope.launch { _undoEvent.emit(expense) }
    }

    fun undoDelete(expense: Expense) {
        _expenses.update { listOf(expense) + it }
        persist()
    }

    fun updateExpense(expense: Expense) {
        if (expense.amount <= 0) return
        _expenses.update { list ->
            list.map { if (it.id == expense.id) expense else it }
        }
        persist()
    }

    // ── 필터 / 검색 ──────────────────────────────────────────
    fun setFilterCategory(cat: String?) { _filterCategory.value = cat }
    fun setSearchText(query: String)    { _searchText.value = query }

    // ── 내부 헬퍼 ────────────────────────────────────────────
    private fun persist() {
        viewModelScope.launch { dataStore.save(_expenses.value) }
    }

    private fun emitError(msg: String) {
        viewModelScope.launch { _errorMessage.emit(msg) }
    }
}

// ── Factory ──────────────────────────────────────────────────
class ExpenseViewModelFactory(
    private val dataStore: DataStoreManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ExpenseViewModel(dataStore) as T
}
