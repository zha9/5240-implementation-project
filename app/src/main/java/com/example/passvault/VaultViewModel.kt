package com.example.passvault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VaultViewModel : ViewModel() {
    private val itemsRepository = ItemsRepository.get()

    private val _uiState: MutableStateFlow<VaultUiState> = MutableStateFlow(VaultUiState())

    val uiState: StateFlow<VaultUiState>
        get() = _uiState.asStateFlow()

    suspend fun addItem(item: Item) {
        itemsRepository.addItem(item)
    }

    init {
        viewModelScope.launch {
            itemsRepository.getItems().collect {
                _uiState.update { oldState ->
                    oldState.copy(
                        items = it
                    )
                }
            }
        }
    }

    data class VaultUiState(
        val items: List<Item> = listOf()
    )
}