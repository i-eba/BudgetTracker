package com.example.budgettracker.util

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
    
    /**
     * Add test transactions for categories that need data
     */
    fun addTestTransactionsForMissingCategories(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Add Utilities transactions
            addTestTransaction(
                categoryId = 5L, // Utilities
                amount = 50.0,
                description = "Electricity bill",
                isIncome = false,
                userId = userId
            )
            
            // Add Healthcare transactions
            addTestTransaction(
                categoryId = 6L, // Healthcare
                amount = 75.0,
                description = "Doctor visit",
                isIncome = false,
                userId = userId
            )
            
            Log.d(TAG, "Added test transactions for missing categories")
        }
    }
    
    private suspend fun addTestTransaction(
        categoryId: Long,
        amount: Double,
        description: String,
        isIncome: Boolean,
        userId: String
    ) {
        val calendar = Calendar.getInstance()
        // Set date to within the current month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 3)
        
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