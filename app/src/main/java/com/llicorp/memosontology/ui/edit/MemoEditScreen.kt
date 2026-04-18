package com.llicorp.memosontology.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llicorp.memosontology.data.Memo
import com.llicorp.memosontology.ui.MemoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoEditScreen(
    viewModel: MemoViewModel,
    memoId: String?,
    onBack: () -> Unit
) {
    val memos by viewModel.memos.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tagsStr by remember { mutableStateOf("") }
    var originalTimestamp by remember { mutableStateOf("") }

    // Initialize fields once the memo list is available
    LaunchedEffect(memoId, memos) {
        if (memoId != null) {
            val memo = memos.find { it.id == memoId }
            if (memo != null && title.isEmpty() && content.isEmpty()) {
                title = memo.title
                content = memo.content
                tagsStr = memo.tags.joinToString(", ")
                originalTimestamp = memo.timestamp
            }
        }
    }

    // Show snackbar on save error
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error ?: "저장 실패")
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (memoId == null) "New Memo" else "Edit Memo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                val tags = tagsStr.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                viewModel.saveMemo(
                                    Memo(
                                        id = memoId ?: "",
                                        title = title,
                                        content = content,
                                        tags = tags,
                                        timestamp = originalTimestamp
                                    ),
                                    onComplete = onBack
                                )
                            },
                            enabled = title.isNotBlank()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tagsStr,
                onValueChange = { tagsStr = it },
                label = { Text("Tags (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                singleLine = false
            )
        }
    }
}
