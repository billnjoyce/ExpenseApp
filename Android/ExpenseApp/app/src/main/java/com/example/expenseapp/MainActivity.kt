package com.example.expenseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expenseapp.auth.AuthManager
import com.example.expenseapp.auth.AuthState
import com.example.expenseapp.firebase.FirestoreManager
import com.example.expenseapp.navigation.AppNavigation
import com.example.expenseapp.ui.screens.LoginScreen
import com.example.expenseapp.ui.theme.ExpenseAppTheme
import com.example.expenseapp.viewmodel.ExpenseViewModel
import com.example.expenseapp.viewmodel.ExpenseViewModelFactory

// google-services.json → client_id (client_type: 3) 값으로 교체
private const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID_HERE"

class MainActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authManager = AuthManager(applicationContext)
        setContent {
            ExpenseAppTheme {
                RootScreen(authManager = authManager)
            }
        }
    }
}

@Composable
fun RootScreen(authManager: AuthManager) {
    val authState by authManager.authState.collectAsStateWithLifecycle(
        initialValue = AuthState.Loading
    )

    when (val state = authState) {
        is AuthState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AuthState.SignedOut -> {
            LoginScreen(
                authManager     = authManager,
                webClientId     = WEB_CLIENT_ID,
                onSignInSuccess = { }
            )
        }
        is AuthState.SignedIn -> {
            val firestoreManager = remember(state.user.uid) {
                FirestoreManager(userId = state.user.uid)
            }
            val vm: ExpenseViewModel = viewModel(
                factory = remember(firestoreManager) {
                    ExpenseViewModelFactory(firestoreManager)
                }
            )
            AppNavigation(
                viewModel   = vm,
                authManager = authManager,
                user        = state.user
            )
        }
    }
}
