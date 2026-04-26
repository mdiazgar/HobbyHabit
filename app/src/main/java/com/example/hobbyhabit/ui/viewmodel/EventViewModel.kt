package com.example.hobbyhabit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhabit.data.local.Event // ✅ use local model
import com.example.hobbyhabit.data.remote.RetrofitInstance
import com.example.hobbyhabit.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EventUiState {
    object Idle : EventUiState()
    object Loading : EventUiState()
    data class Success(val events: List<Event>) : EventUiState() // ✅ FIXED
    data class Error(val message: String) : EventUiState()
}

class EventViewModel : ViewModel() {

    private val repository = EventRepository(RetrofitInstance.api)

    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Idle)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    fun searchEvents(
        token: String,
        query: String,
        lat: Double? = null,
        lng: Double? = null
    ) {
        _uiState.value = EventUiState.Loading

        viewModelScope.launch {
            val result = repository.searchEvents(token, query, lat, lng)

            _uiState.value = result.fold(
                onSuccess = { events ->
                    EventUiState.Success(events) // ✅ now List<Event>
                },
                onFailure = {
                    EventUiState.Error(it.message ?: "Unknown error")
                }
            )
        }
    }
}