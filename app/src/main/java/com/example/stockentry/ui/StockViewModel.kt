package com.example.stockentry.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stockentry.data.StockEntry
import com.example.stockentry.data.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StockUiState(
    val entries: List<StockEntry> = emptyList(),
    val message: String? = null,
    val loading: Boolean = false
)

class StockViewModel(
    private val repository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        refreshEntries()
    }

    fun addEntry(type: String, rate: String, qty: String) {
        val parsedRate = rate.toDoubleOrNull()
        val parsedQty = qty.toIntOrNull()

        if (parsedRate == null || parsedQty == null) {
            setMessage("Please enter valid rate and quantity")
            return
        }

        viewModelScope.launch {
            repository.addEntry(type, parsedRate, parsedQty)
            refreshEntries("Entry saved")
        }
    }

    fun deleteEntry(entry: StockEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            refreshEntries("Entry deleted")
        }
    }

    fun syncEntries() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val result = repository.syncUnsyncedEntries()
            val message = result.fold(
                onSuccess = { count -> "Synced $count entries to Google Spreadsheet" },
                onFailure = { "Sync failed. Check webhook URL/network." }
            )
            refreshEntries(message)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun refreshEntries(message: String? = null) {
        viewModelScope.launch {
            val list = repository.last10Entries()
            _uiState.value = _uiState.value.copy(entries = list, message = message, loading = false)
        }
    }

    private fun setMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }
}

class StockViewModelFactory(
    private val repository: StockRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StockViewModel(repository) as T
    }
}
