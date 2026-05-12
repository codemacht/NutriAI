package com.example.nutriai.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nutriai.data.db.AppDatabase
import com.example.nutriai.data.db.UserEntity
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                _authResult.value = AuthResult.Error("User with this email already exists!")
            } else {
                userDao.registerUser(UserEntity(username = username, email = email, password = password))
                _authResult.value = AuthResult.Success("Registration successful!")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = userDao.loginUser(email, password)
            if (user != null) {
                _authResult.value = AuthResult.Success(user.username)
            } else {
                _authResult.value = AuthResult.Error("Invalid email or password!")
            }
        }
    }
}

sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}