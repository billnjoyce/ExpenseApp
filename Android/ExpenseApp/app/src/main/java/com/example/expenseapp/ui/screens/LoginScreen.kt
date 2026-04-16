package com.example.expenseapp.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.expenseapp.auth.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    authManager: AuthManager,
    webClientId: String,
    onSignInSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Google Sign-In 결과 처리 Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken ?: throw Exception("ID Token이 없습니다.")
                isLoading = true
                // Coroutine은 LaunchedEffect로 처리
            } catch (e: ApiException) {
                errorMessage = "Google 로그인 실패: ${e.statusCode}"
                isLoading = false
            }
        }
    }

    // Google Sign-In 코루틴 처리
    var pendingIdToken by remember { mutableStateOf<String?>(null) }

    // Google Sign-In 결과 재처리 (LaunchedEffect에서 코루틴 실행)
    val googleSignInLauncher2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            pendingIdToken = account.idToken
        } catch (e: Exception) {
            errorMessage = e.message
            isLoading = false
        }
    }

    // 코루틴에서 Firebase 로그인 실행
    LaunchedEffect(pendingIdToken) {
        val token = pendingIdToken ?: return@LaunchedEffect
        isLoading = true
        val result = authManager.signInWithGoogle(token)
        result.onSuccess { onSignInSuccess() }
             .onFailure { errorMessage = it.message }
        isLoading = false
        pendingIdToken = null
    }

    // UI
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier            = Modifier.padding(horizontal = 32.dp)
        ) {
            // 로고
            Surface(
                shape  = RoundedCornerShape(24.dp),
                color  = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("💳", style = MaterialTheme.typography.displayMedium)
                }
            }

            // 앱 이름
            Text(
                "가계부",
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            // 설명
            Text(
                "로그인하여 데이터를 클라우드에\n안전하게 저장하고 동기화하세요.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // 에러 메시지
            errorMessage?.let { msg ->
                Text(
                    msg,
                    color  = MaterialTheme.colorScheme.error,
                    style  = MaterialTheme.typography.bodySmall
                )
            }

            // Google 로그인 버튼
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                OutlinedButton(
                    onClick = {
                        errorMessage = null
                        val intent = authManager.getGoogleSignInIntent(webClientId)
                        googleSignInLauncher2.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("G", style = MaterialTheme.typography.titleLarge,
                             color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
                        Text("Google로 계속하기",
                             style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}
