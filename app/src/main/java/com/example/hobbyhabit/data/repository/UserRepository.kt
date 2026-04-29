package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.local.User
import com.example.hobbyhabit.data.local.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getUser(): Flow<User?> = userDao.getUser()

    suspend fun saveUser(username: String, email: String) {
        userDao.saveUser(User(username = username, email = email))
    }

    suspend fun deleteUser() = userDao.deleteUser()
}
