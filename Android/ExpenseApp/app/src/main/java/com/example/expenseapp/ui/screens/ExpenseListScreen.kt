package com.example.expenseapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expenseapp.model.Expense
import com.example.expenseapp.ui.components.*
import com.example.expenseapp.utils.formatAmount
import com.example.expenseapp.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(viewModel: ExpenseViewModel) {

    val expenses       by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val totalAmount    by viewModel.totalAmount.collectAsStateWithLifecycle()
    val filterCategory by viewModel.filterCategory.collectAsStateWithLifecycle()
    val searchText     by viewModel.searchText.collectAsStateWithLifecycle()

    var showAddSheet   by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    val snackbarHost = remember { SnackbarHostState() }
    val scope        = rememberCoroutineScope()

    // Undo 이벤트 수신
    LaunchedEffect(Unit) {
        viewModel.undoEvent.collect { deleted ->
            val result = snackbarHost.showSnackbar(
                message     = "삭제됨",
                actionLabel = "실행 취소",
                duration    = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete(deleted)
            }
        }
    }

    // 에러 이벤트 수신
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { msg ->
            snackbarHost.showSnackbar(msg, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("지출 내역", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, "지출 추가")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            // 총액 카드
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier              = Modifier.fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "총 지출",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₩${totalAmount.formatAmount()}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // 검색창
            OutlinedTextField(
                value         = searchText,
                onValueChange = viewModel::setSearchText,
                modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder   = { Text("지출 검색") },
                leadingIcon   = { Text("🔍") },
                singleLine    = true,
                shape         = MaterialTheme.shapes.large
            )

            // 카테고리 필터 칩
            CategoryFilterChips(
                selected = filterCategory,
                onSelect = viewModel::setFilterCategory
            )

            HorizontalDivider()

            // 목록 or 빈 상태
            if (expenses.isEmpty()) {
                EmptyExpenseView(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseItem(
                            expense  = expense,
                            onDelete = { viewModel.deleteExpense(expense) },
                            onTap    = { editingExpense = expense }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                    }
                }
            }
        }
    }

    // AddExpenseSheet
    if (showAddSheet) {
        AddExpenseSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { amount, cat, note ->
                viewModel.addExpense(amount, cat, note)
                showAddSheet = false
            }
        )
    }

    // EditExpenseSheet
    editingExpense?.let { expense ->
        EditExpenseSheet(
            expense   = expense,
            onDismiss = { editingExpense = null },
            onConfirm = { updated ->
                viewModel.updateExpense(updated)
                editingExpense = null
            }
        )
    }
}
