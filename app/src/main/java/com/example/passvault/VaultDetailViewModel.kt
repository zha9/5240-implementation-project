package com.example.passvault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class VaultDetailViewModel(itemId: UUID) : ViewModel() {
    private val itemsRepository = ItemsRepository.get()

    private val _item: MutableStateFlow<Item?> = MutableStateFlow(null)
    val item: StateFlow<Item?> = _item.asStateFlow()

    init {
        viewModelScope.launch {
            _item.value = itemsRepository.getItem(itemId)
        }
    }

    fun updateItem(onUpdate: (Item) -> Item) {
        _item.update { oldItem ->
            oldItem?.let {
                onUpdate(it)
            }
        }
    }

    fun deleteItem() {
        item.value?.let {
            itemsRepository.deleteItem(it)
        }
        _item.value = null
    }

    override fun onCleared() {
        super.onCleared()
        item.value?.let { itemsRepository.updateItem(it) }
    }
}

class VaultDetailViewModelFactory(private val itemId: UUID) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VaultDetailViewModel(itemId) as T
    }
}