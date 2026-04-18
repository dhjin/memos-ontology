package com.llicorp.memosontology.ui.ontology

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.llicorp.memosontology.data.EntailmentPair
import com.llicorp.memosontology.data.FusekiRepository
import com.llicorp.memosontology.data.SparqlResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class OntologyUiState {
    object Idle : OntologyUiState()
    object Loading : OntologyUiState()
    data class SparqlSuccess(val result: SparqlResult) : OntologyUiState()
    data class EntailmentSuccess(val pairs: List<EntailmentPair>) : OntologyUiState()
    data class Error(val message: String) : OntologyUiState()
}

class OntologyViewModel(private val repository: FusekiRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<OntologyUiState>(OntologyUiState.Idle)
    val uiState: StateFlow<OntologyUiState> = _uiState

    private val _relatedEntailments = MutableStateFlow<List<EntailmentPair>>(emptyList())
    val relatedEntailments: StateFlow<List<EntailmentPair>> = _relatedEntailments

    fun runSparql(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = OntologyUiState.Loading
            _uiState.value = try {
                OntologyUiState.SparqlSuccess(repository.query(query))
            } catch (e: Exception) {
                OntologyUiState.Error(e.message ?: "Query failed")
            }
        }
    }

    fun loadEntailments() {
        viewModelScope.launch {
            _uiState.value = OntologyUiState.Loading
            _uiState.value = try {
                OntologyUiState.EntailmentSuccess(repository.getEntailments())
            } catch (e: Exception) {
                OntologyUiState.Error(e.message ?: "Failed to load entailments")
            }
        }
    }

    /** Extract keywords from memo title+content and find related entailment pairs */
    fun loadRelatedEntailments(title: String, content: String) {
        val keywords = (title.split(" ") + content.split(" "))
            .map { it.trim().lowercase() }
            .filter { it.length >= 2 }
            .distinct()
            .take(10)
        viewModelScope.launch {
            _relatedEntailments.value = try {
                repository.getRelatedEntailments(keywords)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    fun reset() { _uiState.value = OntologyUiState.Idle }

    class Factory(private val repository: FusekiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(OntologyViewModel::class.java))
            return OntologyViewModel(repository) as T
        }
    }
}
