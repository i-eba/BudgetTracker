package com.example.budgettracker.presenter

import androidx.lifecycle.LiveData
import com.example.budgettracker.data.BudgetRepository
import com.example.budgettracker.data.remote.FirebaseAuthManager
import com.google.firebase.auth.FirebaseUser

class AuthPresenter(
    private val authManager: FirebaseAuthManager,
    private val repository: BudgetRepository
) {
    // User state
    val currentUser: LiveData<FirebaseUser?> = authManager.currentUser
    val authError: LiveData<String?> = authManager.authError
    
    fun signUp(email: String, password: String) {
        if (validateInput(email, password)) {
            authManager.signUp(email, password)
        }
    }
    
    fun signIn(email: String, password: String) {
        if (validateInput(email, password)) {
            authManager.signIn(email, password)
        }
    }
    
    fun signOut() {
        authManager.signOut()
    }
    
    fun resetPassword(email: String) {
        if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            authManager.resetPassword(email)
        }
    }
    
    fun syncDataAfterLogin() {
        repository.syncData()
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        
        if (password.length < 6) {
            return false
        }
        
        return true
    }
} 