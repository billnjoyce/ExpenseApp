package com.example.expenseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expenseapp.navigation.AppNavigation
import com.example.expenseapp.storage.RealDataStoreManager
import com.example.expenseapp.ui.theme.ExpenseAppTheme
import com.example.expenseapp.viewmodel.ExpenseViewModel
import com.example.expenseapp.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = ExpenseViewModelFactory(
            RealDataStoreManager(applicationContext)
        )

        setContent {
            ExpenseAppTheme {
                val vm: ExpenseViewModel = viewModel(factory = factory)
                AppNavigation(viewModel = vm)
            }
        }
    }
}
