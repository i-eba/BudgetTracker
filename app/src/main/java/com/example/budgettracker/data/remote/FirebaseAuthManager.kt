package com.example.budgettracker.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.budgettracker.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseAuthManager {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser
    
    private val _authError = MutableLiveData<String?>()
    val authError: LiveData<String?> = _authError
    
    private val _profileUpdateStatus = MutableLiveData<Boolean>()
    val profileUpdateStatus: LiveData<Boolean> = _profileUpdateStatus
    
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
    
    fun updateUserProfile(name: String, phone: String, avatarUrl: String, birthday: String) {
        val user = firebaseAuth.currentUser ?: return
        
        // Update displayName in Firebase Auth
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        
        user.updateProfile(profileUpdates)
            .addOnSuccessListener {
                // Update additional user info in Firestore
                val userId = user.uid
                val userMap = hashMapOf(
                    "name" to name,
                    "email" to user.email,
                    "phone" to phone,
                    "avatarUrl" to avatarUrl,
                    "birthday" to birthday
                )
                
                firestore.collection("users").document(userId)
                    .set(userMap)
                    .addOnSuccessListener {
                        _profileUpdateStatus.value = true
                    }
                    .addOnFailureListener { e ->
                        _authError.value = e.message
                        _profileUpdateStatus.value = false
                    }
            }
            .addOnFailureListener { e ->
                _authError.value = e.message
                _profileUpdateStatus.value = false
            }
    }
    
    fun updateUserEmail(newEmail: String, password: String) {
        val user = firebaseAuth.currentUser ?: return
        
        // Re-authenticate user first
        firebaseAuth.signInWithEmailAndPassword(user.email ?: "", password)
            .addOnSuccessListener {
                // Update email
                user.updateEmail(newEmail)
                    .addOnSuccessListener {
                        // Update email in Firestore
                        val userId = user.uid
                        firestore.collection("users").document(userId)
                            .update("email", newEmail)
                            .addOnSuccessListener {
                                _profileUpdateStatus.value = true
                            }
                            .addOnFailureListener { e ->
                                _authError.value = e.message
                                _profileUpdateStatus.value = false
                            }
                    }
                    .addOnFailureListener { e ->
                        _authError.value = e.message
                        _profileUpdateStatus.value = false
                    }
            }
            .addOnFailureListener { e ->
                _authError.value = "Authentication failed: ${e.message}"
                _profileUpdateStatus.value = false
            }
    }
    
    fun updateUserPassword(currentPassword: String, newPassword: String) {
        val user = firebaseAuth.currentUser ?: return
        
        // Re-authenticate user first
        firebaseAuth.signInWithEmailAndPassword(user.email ?: "", currentPassword)
            .addOnSuccessListener {
                // Update password
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        _profileUpdateStatus.value = true
                    }
                    .addOnFailureListener { e ->
                        _authError.value = e.message
                        _profileUpdateStatus.value = false
                    }
            }
            .addOnFailureListener { e ->
                _authError.value = "Authentication failed: ${e.message}"
                _profileUpdateStatus.value = false
            }
    }
    
    fun getUserData(callback: (User?) -> Unit) {
        val userId = getCurrentUserId() ?: run {
            callback(null)
            return
        }
        
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = User(
                        id = userId,
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        phone = document.getString("phone") ?: "",
                        avatarUrl = document.getString("avatarUrl") ?: "",
                        birthday = document.getString("birthday") ?: ""
                    )
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }
} 