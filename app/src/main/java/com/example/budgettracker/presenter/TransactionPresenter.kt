package com.example.budgettracker.presenter

import androidx.lifecycle.LiveData
import com.example.budgettracker.data.BudgetRepository
import com.example.budgettracker.data.local.entities.Category
import com.example.budgettracker.data.local.entities.Transaction
import com.example.budgettracker.data.remote.FirebaseAuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class TransactionPresenter(
    private val repository: BudgetRepository,
    private val authManager: FirebaseAuthManager
) {
    // Get user ID for transactions
    private val userId: String
        get() = authManager.getCurrentUserId() ?: ""
    
    // Transactions
    val allTransactions: LiveData<List<Transaction>> = repository.getAllTransactions()
    val totalExpenses: LiveData<Double> = repository.getTotalExpenses()
    val totalIncome: LiveData<Double> = repository.getTotalIncome()
    
    // Categories
    val allCategories: LiveData<List<Category>> = repository.getAllCategories()
    
    fun getTransactionsByType(isIncome: Boolean): LiveData<List<Transaction>> {
        return repository.getTransactionsByType(isIncome)
    }
    
    fun getTransactionsByCategory(categoryId: Long): LiveData<List<Transaction>> {
        return repository.getTransactionsByCategory(categoryId)
    }
    
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return repository.getTransactionsByDateRange(startDate, endDate)
    }
    
    fun addTransaction(amount: Double, description: String, date: Date, categoryId: Long, isIncome: Boolean) {
        if (validateTransaction(amount, description, date, categoryId)) {
            val transaction = Transaction(
                amount = amount,
                description = description,
                date = date,
                categoryId = categoryId,
                isIncome = isIncome,
                userId = userId
            )
            
            CoroutineScope(Dispatchers.IO).launch {
                repository.insertTransaction(transaction)
            }
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        if (validateTransaction(transaction.amount, transaction.description, transaction.date, transaction.categoryId)) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.updateTransaction(transaction)
            }
        }
    }
    
    fun deleteTransaction(transaction: Transaction) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteTransaction(transaction)
        }
    }
    
    private fun validateTransaction(amount: Double, description: String, date: Date, categoryId: Long): Boolean {
        if (amount <= 0) {
            return false
        }
        
        if (description.isBlank()) {
            return false
        }
        
        if (date.after(Date())) {
            return false
        }
        
        if (categoryId <= 0) {
            return false
        }
        
        return true
    }
} 