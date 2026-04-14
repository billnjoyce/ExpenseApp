package com.example.expenseapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expenseapp.utils.formatAmount
import com.example.expenseapp.viewmodel.ExpenseViewModel

@Composable
fun StatisticsScreen(viewModel: ExpenseViewModel) {

    val monthTotal by viewModel.thisMonthTotal.collectAsStateWithLifecycle()
    val stats      by viewModel.categoryStats.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 이번 달 총액 카드
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "이번 달 총 지출",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "₩${monthTotal.formatAmount()}",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Text(
                "카테고리별 지출",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (stats.isEmpty()) {
            item {
                Box(
                    modifier         = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("아직 지출 데이터가 없습니다",
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(stats) { stat ->
                CategoryStatCard(stat)
            }
        }
    }
}

@Composable
fun CategoryStatCard(stat: ExpenseViewModel.CategoryStat) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stat.emoji, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(stat.category,
                         style      = MaterialTheme.typography.bodyLarge,
                         fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₩${stat.total.formatAmount()}",
                         style      = MaterialTheme.typography.bodyMedium,
                         fontWeight = FontWeight.Bold)
                    Text("${String.format("%.1f", stat.percent)}%",
                         style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { stat.percent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color    = MaterialTheme.colorScheme.primary
            )
        }
    }
}
