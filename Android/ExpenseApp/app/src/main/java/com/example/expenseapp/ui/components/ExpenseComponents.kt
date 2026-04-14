package com.example.expenseapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expenseapp.model.Expense
import com.example.expenseapp.model.ExpenseConstants
import com.example.expenseapp.utils.formatAmount
import com.example.expenseapp.utils.formatDate

// ── ExpenseItem (스와이프 삭제 포함) ─────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItem(
    expense:  Expense,
    onDelete: () -> Unit,
    onTap:    () -> Unit = {}
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { v ->
            if (v == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        }
    )

    SwipeToDismissBox(
        state                    = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier          = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(end = 24.dp),
                contentAlignment  = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete, "삭제",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onTap() },
            color    = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier             = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment    = Alignment.CenterVertically
            ) {
                // 카테고리 이모지 원형 배경
                Surface(
                    shape    = MaterialTheme.shapes.extraLarge,
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(expense.categoryEmoji, style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        expense.category,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (expense.note.isNotBlank()) {
                        Text(
                            expense.note,
                            style   = MaterialTheme.typography.bodySmall,
                            color   = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Text(
                        expense.date.formatDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Text(
                    "₩${expense.amount.formatAmount()}",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── CategoryFilterChips ──────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterChips(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    val categories = listOf("전체") + ExpenseConstants.DEFAULT_CATEGORIES
    LazyRow(
        contentPadding          = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val isSelected = if (cat == "전체") selected == null else selected == cat
            FilterChip(
                selected = isSelected,
                onClick  = { onSelect(if (cat == "전체") null else cat) },
                label    = { Text(cat) }
            )
        }
    }
}

// ── ExpenseInputField (재사용 입력 필드) ─────────────────────
@Composable
fun ExpenseInputField(
    label:         String,
    value:         String,
    onValueChange: (String) -> Unit,
    modifier:      Modifier      = Modifier,
    keyboardType:  KeyboardType  = KeyboardType.Text,
    isError:       Boolean       = false,
    errorMessage:  String?       = null,
    singleLine:    Boolean       = true,
    placeholder:   String        = ""
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,
            label           = { Text(label) },
            placeholder     = { if (placeholder.isNotEmpty()) Text(placeholder) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError         = isError,
            singleLine      = singleLine,
            modifier        = Modifier.fillMaxWidth()
        )
        if (isError && errorMessage != null) {
            Text(
                errorMessage,
                color    = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ── CategoryChipsGroup (선택용) ──────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChipsGroup(
    selected: String,
    onSelect: (String) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ExpenseConstants.DEFAULT_CATEGORIES.forEach { cat ->
            FilterChip(
                selected = selected == cat,
                onClick  = { onSelect(cat) },
                label    = { Text(cat) }
            )
        }
    }
}

// ── AddExpenseSheet ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    onDismiss: () -> Unit,
    onConfirm: (amount: Int, category: String, note: String) -> Unit
) {
    var amountText       by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseConstants.DEFAULT_CATEGORIES.first()) }
    var note             by remember { mutableStateOf("") }

    val amountError: String? = when {
        amountText.isBlank()                               -> null
        amountText.toIntOrNull() == null                   -> "숫자만 입력해주세요"
        (amountText.toIntOrNull() ?: 0) <= 0              -> "0원보다 큰 금액을 입력해주세요"
        (amountText.toIntOrNull() ?: 0) > ExpenseConstants.MAX_AMOUNT -> "10,000,000원 이하로 입력해주세요"
        else                                               -> null
    }
    val isValid = amountText.isNotBlank() && amountError == null

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier              = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement   = Arrangement.spacedBy(16.dp)
        ) {
            Text("지출 추가", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            ExpenseInputField(
                label         = "금액",
                value         = amountText,
                onValueChange = { amountText = it },
                keyboardType  = KeyboardType.Number,
                isError       = amountError != null,
                errorMessage  = amountError,
                placeholder   = "0"
            )

            Text("카테고리", style = MaterialTheme.typography.labelLarge)
            CategoryChipsGroup(selected = selectedCategory, onSelect = { selectedCategory = it })

            ExpenseInputField(
                label         = "메모 (선택사항)",
                value         = note,
                onValueChange = { note = it },
                singleLine    = false,
                placeholder   = "메모를 입력하세요"
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("취소") }
                Button(
                    onClick  = { onConfirm(amountText.toInt(), selectedCategory, note) },
                    enabled  = isValid,
                    modifier = Modifier.weight(1f)
                ) { Text("저장") }
            }
        }
    }
}

// ── EditExpenseSheet ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseSheet(
    expense:   Expense,
    onDismiss: () -> Unit,
    onConfirm: (Expense) -> Unit
) {
    var amountText       by remember { mutableStateOf(expense.amount.toString()) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var note             by remember { mutableStateOf(expense.note) }
    val isValid = (amountText.toIntOrNull() ?: 0) > 0

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier              = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement   = Arrangement.spacedBy(16.dp)
        ) {
            Text("지출 수정", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            ExpenseInputField(
                label         = "금액",
                value         = amountText,
                onValueChange = { amountText = it },
                keyboardType  = KeyboardType.Number
            )

            Text("카테고리", style = MaterialTheme.typography.labelLarge)
            CategoryChipsGroup(selected = selectedCategory, onSelect = { selectedCategory = it })

            ExpenseInputField(
                label         = "메모 (선택사항)",
                value         = note,
                onValueChange = { note = it },
                singleLine    = false
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("취소") }
                Button(
                    onClick = {
                        onConfirm(expense.copy(
                            amount   = amountText.toInt(),
                            category = selectedCategory,
                            note     = note
                        ))
                    },
                    enabled  = isValid,
                    modifier = Modifier.weight(1f)
                ) { Text("저장") }
            }
        }
    }
}

// ── EmptyExpenseView ─────────────────────────────────────────
@Composable
fun EmptyExpenseView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("💸", style = MaterialTheme.typography.displayMedium)
            Text("지출 내역 없음", style = MaterialTheme.typography.titleMedium)
            Text(
                "+ 버튼을 눌러 첫 지출을 기록해보세요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
