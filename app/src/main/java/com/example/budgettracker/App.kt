package com.example.budgettracker

import android.app.Application
import com.google.firebase.FirebaseApp

class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
} 