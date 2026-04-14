package com.example.expenseapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.*
import com.example.expenseapp.ui.screens.ExpenseListScreen
import com.example.expenseapp.ui.screens.StatisticsScreen
import com.example.expenseapp.viewmodel.ExpenseViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Expense    : Screen("expense_list", "지출",  Icons.Default.List)
    object Statistics : Screen("statistics",   "통계",  Icons.Default.BarChart)
}

val bottomScreens = listOf(Screen.Expense, Screen.Statistics)

@Composable
fun AppNavigation(viewModel: ExpenseViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val current   = backStack?.destination?.route

                bottomScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = current == screen.route,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon  = { Icon(screen.icon, screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Expense.route
        ) {
            composable(Screen.Expense.route) {
                ExpenseListScreen(viewModel = viewModel)
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = viewModel)
            }
        }
    }
}
