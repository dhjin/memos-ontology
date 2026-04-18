package com.llicorp.memosontology.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.llicorp.memosontology.ui.MemoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MemoViewModel,
    onBack: () -> Unit
) {
    val serverUrl by viewModel.serverConfig.serverUrl.collectAsState(initial = "")
    val username by viewModel.serverConfig.username.collectAsState(initial = "")
    val password by viewModel.serverConfig.password.collectAsState(initial = "")
    val fusekiUrl by viewModel.serverConfig.fusekiUrl.collectAsState(initial = "")
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    var urlInput by remember(serverUrl) { mutableStateOf(serverUrl) }
    var userInput by remember(username) { mutableStateOf(username) }
    var passInput by remember(password) { mutableStateOf(password) }
    var fusekiInput by remember(fusekiUrl) { mutableStateOf(fusekiUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Server URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("http://192.168.x.x:8080") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = passInput,
                onValueChange = { passInput = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Fuseki (온톨로지 서버)", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fusekiInput,
                onValueChange = { fusekiInput = it },
                label = { Text("Fuseki URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("http://192.168.x.x:30000/memos") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.updateFusekiUrl(fusekiInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = fusekiInput.isNotBlank()
            ) { Text("Fuseki 저장") }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.checkConnection(urlInput, userInput, passInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = connectionStatus != null || urlInput.isNotBlank()
            ) {
                Text("연결 테스트")
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (connectionStatus) {
                true -> Text(
                    "연결 성공",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                false -> Text(
                    "연결 실패 — URL/계정을 확인하세요.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                null -> {}
            }
        }
    }
}
