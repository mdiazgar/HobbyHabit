package com.example.hobbyhabit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhabit.data.local.HobbyDatabase
import com.example.hobbyhabit.data.local.User
import com.example.hobbyhabit.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(
        HobbyDatabase.getDatabase(application).userDao()
    )

    val user: StateFlow<User?> = repository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun saveUser(username: String, email: String) {
        viewModelScope.launch {
            repository.saveUser(username, email)
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            repository.deleteUser()
        }
    }
}