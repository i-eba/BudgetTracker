package com.example.budgettracker.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.budgettracker.data.BudgetRepository
import com.example.budgettracker.data.local.entities.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * Utility class to generate test data for debugging
 */
class TestDataGenerator(private val repository: BudgetRepository) {
    
    private val TAG = "TestDataGenerator"
    private lateinit var preferences: SharedPreferences
    
    // Initialize with application context
    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("budget_tracker_prefs", Context.MODE_PRIVATE)
    }
    
    /**
     * Add test transactions for categories that need data
     */
    fun addTestTransactionsForMissingCategories(context: Context, userId: String) {
        // Initialize preferences if not done already
        if (!::preferences.isInitialized) {
            initialize(context)
        }
        
        // Check if we've already added test data for this user
        val prefKey = "test_data_added_$userId"
        if (preferences.getBoolean(prefKey, false)) {
            Log.d(TAG, "Test data already added for user $userId, skipping")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            // Add Utilities transactions
            addTestTransaction(
                categoryId = 5L, // Utilities
                amount = 50.0,
                description = "Electricity bill",
                isIncome = false,
                userId = userId
            )
            
            // Add multiple Healthcare transactions to ensure they show in reports
            addTestTransaction(
                categoryId = 6L, // Healthcare
                amount = 75.0,
                description = "Doctor visit",
                isIncome = false,
                userId = userId
            )
            
            // Add additional healthcare transactions
            addTestTransaction(
                categoryId = 6L, // Healthcare
                amount = 125.0,
                description = "Prescription medication",
                isIncome = false,
                userId = userId,
                daysAgo = 5
            )
            
            addTestTransaction(
                categoryId = 6L, // Healthcare
                amount = 200.0,
                description = "Dental checkup",
                isIncome = false,
                userId = userId,
                daysAgo = 10
            )
            
            Log.d(TAG, "Added test transactions for missing categories")
            
            // Mark that we've added test data for this user
            preferences.edit().putBoolean(prefKey, true).apply()
        }
    }
    
    /**
     * Force add test data even if it was previously added
     */
    fun forceAddTestTransactions(context: Context, userId: String) {
        // Initialize preferences if not done already
        if (!::preferences.isInitialized) {
            initialize(context)
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            // Add Utilities transactions
            addTestTransaction(
                categoryId = 5L, // Utilities
                amount = 50.0,
                description = "Electricity bill",
                isIncome = false,
                userId = userId
            )
            
            // Add multiple Healthcare transactions to ensure they show in reports
            addTestTransaction(
                categoryId = 6L, // Healthcare
                amount = 75.0,
                description = "Doctor visit",
                isIncome = false,
                userId = userId
            )
            
            // Add additional healthcare transactions
            addTestTransaction(
                categoryId = 6L, // Healthcare
                amount = 125.0,
                description = "Prescription medication",
                isIncome = false,
                userId = userId,
                daysAgo = 5
            )
            
            addTestTransaction(
                categoryId = 6L, // Healthcare
                amount = 200.0,
                description = "Dental checkup",
                isIncome = false,
                userId = userId,
                daysAgo = 10
            )
            
            Log.d(TAG, "Force added test transactions")
            
            // Mark that we've added test data for this user
            preferences.edit().putBoolean("test_data_added_$userId", true).apply()
        }
    }
    
    /**
     * Reset test data flag to add it again on next app launch
     */
    fun resetTestDataFlag(context: Context, userId: String) {
        // Initialize preferences if not done already
        if (!::preferences.isInitialized) {
            initialize(context)
        }
        
        preferences.edit().putBoolean("test_data_added_$userId", false).apply()
        Log.d(TAG, "Reset test data flag for user $userId")
    }
    
    private suspend fun addTestTransaction(
        categoryId: Long,
        amount: Double,
        description: String,
        isIncome: Boolean,
        userId: String,
        daysAgo: Int = 3
    ) {
        val calendar = Calendar.getInstance()
        // Set date to within the current month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - daysAgo)
        
        val transaction = Transaction(
            amount = amount,
            description = description,
            date = calendar.time,
            categoryId = categoryId,
            isIncome = isIncome,
            userId = userId
        )
        
        repository.insertTransaction(transaction)
        Log.d(TAG, "Added test transaction: $transaction")
    }
} 