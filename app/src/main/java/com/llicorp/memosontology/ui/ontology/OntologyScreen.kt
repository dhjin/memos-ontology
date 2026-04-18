package com.llicorp.memosontology.ui.ontology

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.llicorp.memosontology.data.EntailmentPair
import com.llicorp.memosontology.data.SparqlResult

private val DEFAULT_SPARQL = """PREFIX : <http://example.org/ontology#>
SELECT ?s ?o WHERE {
  ?s :entails ?o .
}
LIMIT 20""".trimIndent()

@Composable
fun OntologyScreen(viewModel: OntologyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("SPARQL", "함의 목록")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = selectedTab == i,
                    onClick = {
                        selectedTab = i
                        viewModel.reset()
                    },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> SparqlTab(uiState) { sparql -> viewModel.runSparql(sparql) }
            1 -> EntailmentTab(uiState) { viewModel.loadEntailments() }
        }
    }
}

@Composable
private fun SparqlTab(uiState: OntologyUiState, onRun: (String) -> Unit) {
    var sparql by remember { mutableStateOf(DEFAULT_SPARQL) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = sparql,
            onValueChange = { sparql = it },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 200.dp),
            label = { Text("SPARQL") },
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onRun(sparql) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("실행")
        }
        Spacer(modifier = Modifier.height(8.dp))

        when (uiState) {
            is OntologyUiState.Loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is OntologyUiState.SparqlSuccess -> SparqlTable(uiState.result)

            is OntologyUiState.Error -> Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.message,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            else -> Text(
                "쿼리를 입력하고 실행 버튼을 누르세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SparqlTable(result: SparqlResult) {
    if (result.rows.isEmpty()) {
        Text("결과 없음", style = MaterialTheme.typography.bodyMedium)
        return
    }
    Text(
        "${result.rows.size}건",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))
    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        Column {
            // Header
            Row {
                result.variables.forEach { v ->
                    Text(
                        text = v,
                        modifier = Modifier.width(160.dp).padding(4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            HorizontalDivider()
            // Rows
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(result.rows) { row ->
                    Row {
                        result.variables.forEach { v ->
                            val value = row[v] ?: ""
                            val display = value.substringAfterLast("/").substringAfterLast("#")
                                .ifEmpty { value }
                            Text(
                                text = display,
                                modifier = Modifier.width(160.dp).padding(4.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun EntailmentTab(uiState: OntologyUiState, onLoad: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Button(onClick = onLoad, modifier = Modifier.fillMaxWidth()) {
            Text("함의 관계 불러오기")
        }
        Spacer(modifier = Modifier.height(8.dp))

        when (uiState) {
            is OntologyUiState.Loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is OntologyUiState.EntailmentSuccess -> EntailmentList(uiState.pairs)

            is OntologyUiState.Error -> Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.message,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            else -> Text(
                "버튼을 눌러 Fuseki에서 함의 관계를 불러오세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EntailmentList(pairs: List<EntailmentPair>) {
    if (pairs.isEmpty()) {
        Text("함의 관계가 없습니다.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    Text(
        "${pairs.size}개 함의 관계",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(pairs) { pair ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pair.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = pair.obj,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
