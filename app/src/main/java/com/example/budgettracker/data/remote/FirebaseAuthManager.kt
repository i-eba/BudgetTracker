package com.example.budgettracker.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthManager {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser
    
    private val _authError = MutableLiveData<String?>()
    val authError: LiveData<String?> = _authError
    
    init {
        _currentUser.value = firebaseAuth.currentUser
        
        // Set up auth state listener
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }
    
    fun signUp(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _authError.value = null
            }
            .addOnFailureListener { e ->
                _authError.value = e.message
            }
    }
    
    fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _authError.value = null
            }
            .addOnFailureListener { e ->
                _authError.value = e.message
            }
    }
    
    fun signOut() {
        firebaseAuth.signOut()
    }
    
    fun resetPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _authError.value = null
            }
            .addOnFailureListener { e ->
                _authError.value = e.message
            }
    }
    
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
} 