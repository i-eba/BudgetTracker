package com.example.budgettracker.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.budgettracker.data.local.dao.TransactionDao
import com.example.budgettracker.data.local.entities.Transaction
import com.example.budgettracker.data.remote.FirestoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val firestoreManager: FirestoreManager,
    private val userId: String
) {
    private val tag = "TransactionRepository"
    
    // Get all transactions from local database
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions(userId)
    
    // Insert transaction to both local and remote database
    fun insertTransaction(transaction: Transaction) {
        Log.d(tag, "Inserting transaction: $transaction")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Insert to local database first
                val id = transactionDao.insert(transaction)
                
                // Update the transaction with the generated ID
                val updatedTransaction = transaction.copy(id = id)
                
                // Save to Firestore
                val docId = firestoreManager.saveTransaction(updatedTransaction)
                Log.d(tag, "Transaction saved to Firestore with ID: $docId")
                Log.d(tag, "Transaction inserted successfully with ID: $id")
            } catch (e: Exception) {
                Log.e(tag, "Error saving transaction: ${e.message}")
            }
        }
    }
    
    // Update transaction in both local and remote database
    fun updateTransaction(transaction: Transaction) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                transactionDao.update(transaction)
                
                // Update in Firestore
                firestoreManager.saveTransaction(transaction)
                Log.d(tag, "Transaction updated in Firestore")
            } catch (e: Exception) {
                Log.e(tag, "Error updating transaction: ${e.message}")
            }
        }
    }
    
    // Delete transaction from both local and remote database
    fun deleteTransaction(transaction: Transaction) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                transactionDao.delete(transaction)
                
                // Delete from Firestore
                firestoreManager.deleteTransaction(transaction.id.toString())
                Log.d(tag, "Transaction deleted from Firestore")
            } catch (e: Exception) {
                Log.e(tag, "Error deleting transaction: ${e.message}")
            }
        }
    }
    
    // Get transactions by category
    fun getTransactionsByCategory(categoryId: Long): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(userId, categoryId)
    }
    
    // Get transactions by date range
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
    }
    
    // Get income transactions
    fun getIncomeTransactions(): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(userId, true)
    }
    
    // Get expense transactions
    fun getExpenseTransactions(): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(userId, false)
    }
    
    // Sync transactions with Firestore
    fun syncWithFirestore() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val transactions = firestoreManager.getTransactions(userId)
                // Process the transactions if needed
                Log.d(tag, "Fetched ${transactions.size} transactions from Firestore")
                
                // You could implement a more sophisticated sync here
                // For example, compare timestamps and only update newer records
            } catch (e: Exception) {
                Log.e(tag, "Error syncing with Firestore: ${e.message}")
            }
        }
    }
} 