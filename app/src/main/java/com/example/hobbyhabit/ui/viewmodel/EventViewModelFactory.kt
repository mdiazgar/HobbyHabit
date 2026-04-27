package com.example.hobbyhabit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hobbyhabit.data.repository.EventRepository
import com.example.hobbyhabit.data.repository.HobbyRepository
import com.example.hobbyhabit.data.repository.TicketmasterRepository

class EventViewModelFactory(
    private val hobbyRepository: HobbyRepository,
    private val eventRepository: EventRepository,
    private val ticketmasterRepository: TicketmasterRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            return EventViewModel(
                hobbyRepository,
                eventRepository,
                ticketmasterRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}