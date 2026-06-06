package com.offshore.platform.mobile.ui.view.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.offshore.platform.mobile.ui.navigation.Routes

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToWorkOrderList: () -> Unit,
    onNavigateToLoginOffline: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAutoLogin()
    }

    LaunchedEffect(authState) {
        when (authState) {
            is LoginUiState.LoggedIn -> onNavigateToWorkOrderList()
            is LoginUiState.OfflineLoggedIn -> onNavigateToWorkOrderList()
            is LoginUiState.NotLoggedIn -> onNavigateToLogin()
            else -> { /* loading or error — stay */ }
        }
    }

    // Minimal splash UI while checking
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "海上平台现场作业管理系统",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "正在加载…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
