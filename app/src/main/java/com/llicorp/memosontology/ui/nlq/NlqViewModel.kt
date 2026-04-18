package com.llicorp.memosontology.ui.nlq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.llicorp.memosontology.data.NlqRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class NlqUiState {
    object Idle : NlqUiState()
    object Loading : NlqUiState()
    data class Success(val result: String) : NlqUiState()
    data class Error(val message: String) : NlqUiState()
}

class NlqViewModel(private val repository: NlqRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<NlqUiState>(NlqUiState.Idle)
    val uiState: StateFlow<NlqUiState> = _uiState

    fun query(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _uiState.value = NlqUiState.Loading
            _uiState.value = try {
                NlqUiState.Success(repository.query(question))
            } catch (e: Exception) {
                NlqUiState.Error(e.message ?: "Query failed")
            }
        }
    }

    fun reset() {
        _uiState.value = NlqUiState.Idle
    }

    class Factory(private val repository: NlqRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(NlqViewModel::class.java))
            return NlqViewModel(repository) as T
        }
    }
}
