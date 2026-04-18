package com.llicorp.memosontology.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.llicorp.memosontology.data.EntailmentPair
import com.llicorp.memosontology.ui.MemoViewModel
import com.llicorp.memosontology.ui.ontology.OntologyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoDetailScreen(
    viewModel: MemoViewModel,
    ontologyViewModel: OntologyViewModel,
    memoId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val memos by viewModel.memos.collectAsState()
    val memo = memos.find { it.id == memoId }
    val relatedEntailments by ontologyViewModel.relatedEntailments.collectAsState()

    // Load related entailments when memo is available
    LaunchedEffect(memo) {
        if (memo != null) {
            ontologyViewModel.loadRelatedEntailments(memo.title, memo.content)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(memo?.title ?: "Memo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (memo != null) {
                FloatingActionButton(onClick = { onEdit(memoId) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
        }
    ) { padding ->
        if (memo == null) {
            Box(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text("Memo not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                EpistemicChip(memo.epistemicStatus)

                Spacer(modifier = Modifier.height(8.dp))

                if (memo.timestamp.isNotEmpty()) {
                    Text(
                        text = memo.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = memo.content,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (memo.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        memo.tags.forEach { tag ->
                            SuggestionChip(onClick = {}, label = { Text(tag) })
                        }
                    }
                }

                // Related entailments section
                if (relatedEntailments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "연관 함의 관계",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    relatedEntailments.forEach { pair ->
                        EntailmentRow(pair)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EntailmentRow(pair: EntailmentPair) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pair.subject,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "→",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = pair.obj,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun EpistemicChip(status: String) {
    val (label, color) = when (status) {
        "Asserted"   -> "Asserted"   to Color(0xFF2E7D32)
        "Defeasible" -> "Defeasible" to Color(0xFFE65100)
        "Defeated"   -> "Defeated"   to Color(0xFF757575)
        else         -> "Presumed"   to Color(0xFF1565C0)
    }
    SuggestionChip(
        onClick = {},
        label = { Text(label, color = Color.White) },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = color)
    )
}
