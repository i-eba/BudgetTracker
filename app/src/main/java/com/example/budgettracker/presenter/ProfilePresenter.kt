package com.example.budgettracker.presenter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.budgettracker.data.remote.FirebaseAuthManager
import com.example.budgettracker.model.User

class ProfilePresenter(private val authManager: FirebaseAuthManager) {
    
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadUserProfile() {
        _isLoading.value = true
        authManager.getUserData { user ->
            _user.value = user
            _isLoading.value = false
        }
    }
    
    fun updateProfile(name: String, phone: String, birthday: String) {
        _isLoading.value = true
        authManager.updateUserProfile(name, phone, "", birthday)
    }
    
    fun updateEmail(newEmail: String, password: String) {
        _isLoading.value = true
        authManager.updateUserEmail(newEmail, password)
    }
    
    fun updatePassword(currentPassword: String, newPassword: String) {
        _isLoading.value = true
        authManager.updateUserPassword(currentPassword, newPassword)
    }
    
    fun signOut() {
        authManager.signOut()
    }
} // DiceBear implementation marker
