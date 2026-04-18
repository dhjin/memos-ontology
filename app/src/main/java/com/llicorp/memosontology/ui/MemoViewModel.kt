package com.llicorp.memosontology.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.llicorp.memosontology.data.Memo
import com.llicorp.memosontology.data.MemoRemoteRepository
import com.llicorp.memosontology.data.ServerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MemoViewModel(
    private val repository: MemoRemoteRepository,
    val serverConfig: ServerConfig
) : ViewModel() {

    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // null = unknown, true = connected, false = failed
    private val _connectionStatus = MutableStateFlow<Boolean?>(null)
    val connectionStatus: StateFlow<Boolean?> = _connectionStatus

    init {
        refreshMemos()
    }

    fun refreshMemos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _memos.value = repository.getAllMemos()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchMemos(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _memos.value = repository.searchMemos(query)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveMemo(memo: Memo, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            val success = try {
                repository.saveMemo(memo)
            } catch (e: Exception) {
                _error.value = e.message
                false
            } finally {
                _isSaving.value = false
            }
            if (success) {
                // Update list locally to avoid a full reload
                val updated = _memos.value.toMutableList()
                val idx = updated.indexOfFirst { it.id == memo.id }
                if (idx >= 0) updated[idx] = memo else updated.add(0, memo)
                _memos.value = updated
                onComplete()
            } else {
                if (_error.value == null) _error.value = "Failed to save memo"
            }
        }
    }

    fun deleteMemo(memoId: String) {
        viewModelScope.launch {
            val success = try {
                repository.deleteMemo(memoId)
            } catch (e: Exception) {
                _error.value = e.message
                false
            }
            if (success) {
                _memos.value = _memos.value.filter { it.id != memoId }
            }
        }
    }

    fun checkConnection(url: String, user: String, pass: String) {
        viewModelScope.launch {
            _connectionStatus.value = null
            serverConfig.updateConfig(url, user, pass)
            _connectionStatus.value = try {
                repository.login()
            } catch (e: Exception) {
                false
            }
        }
    }

    fun updateFusekiUrl(url: String) {
        viewModelScope.launch { serverConfig.updateFusekiUrl(url) }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(
        private val repository: MemoRemoteRepository,
        private val serverConfig: ServerConfig
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MemoViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return MemoViewModel(repository, serverConfig) as T
        }
    }
}
