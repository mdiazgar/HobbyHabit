package com.example.hobbyhabit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhabit.data.remote.TicketmasterEvent
import com.example.hobbyhabit.data.remote.RetrofitInstance
import com.example.hobbyhabit.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EventUiState {
    object Idle : EventUiState()
    object Loading : EventUiState()
    data class Success(val events: List<TicketmasterEvent>) : EventUiState()
    data class Error(val message: String) : EventUiState()
}

class EventViewModel : ViewModel() {

    private val repository = EventRepository(RetrofitInstance.api)

    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Idle)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    fun searchEvents(apiKey: String, query: String, lat: Double? = null, lng: Double? = null) {
        _uiState.value = EventUiState.Loading
        viewModelScope.launch {
            val result = repository.searchEvents(apiKey, query, lat, lng)
            _uiState.value = result.fold(
                onSuccess = { EventUiState.Success(it) },
                onFailure = { EventUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }
}