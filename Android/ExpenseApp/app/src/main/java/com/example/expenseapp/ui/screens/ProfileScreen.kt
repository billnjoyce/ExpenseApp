package com.example.expenseapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.expenseapp.auth.AuthManager
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(
    authManager: AuthManager,
    user: FirebaseUser
) {
    var showSignOutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(32.dp))

        // 프로필 이미지
        if (user.photoUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "프로필 이미지",
                modifier      = Modifier.size(96.dp).clip(CircleShape),
                contentScale  = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint     = MaterialTheme.colorScheme.primary
            )
        }

        // 사용자 정보
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text       = user.displayName ?: "사용자",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = user.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // 정보 카드
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("계정 정보", style = MaterialTheme.typography.titleSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)

                ProfileInfoRow(label = "로그인 방식",
                    value = if (user.providerData.any { it.providerId == "google.com" }) "Google" else "Apple")
                ProfileInfoRow(label = "UID", value = user.uid.take(16) + "...")
                ProfileInfoRow(label = "클라우드 동기화", value = "✅ 활성화됨")
            }
        }

        Spacer(Modifier.weight(1f))

        // 로그아웃 버튼
        OutlinedButton(
            onClick  = { showSignOutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null,
                 modifier = Modifier.padding(end = 8.dp))
            Text("로그아웃")
        }

        Spacer(Modifier.height(16.dp))
    }

    // 로그아웃 확인 다이얼로그
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title   = { Text("로그아웃") },
            text    = { Text("정말 로그아웃하시겠습니까?\n로컬에 저장된 데이터는 유지됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authManager.signOut()
                        showSignOutDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("로그아웃") }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("취소") }
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
