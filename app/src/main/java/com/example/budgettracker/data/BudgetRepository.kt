package com.example.budgettracker.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.budgettracker.data.local.AppDatabase
import com.example.budgettracker.data.local.entities.Budget
import com.example.budgettracker.data.local.entities.Category
import com.example.budgettracker.data.local.entities.Transaction
import com.example.budgettracker.data.remote.FirebaseAuthManager
import com.example.budgettracker.data.remote.FirestoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class BudgetRepository(
    private val database: AppDatabase,
    private val firestoreManager: FirestoreManager,
    private val authManager: FirebaseAuthManager
) {
    // Get current user ID
    private val currentUserId: String?
        get() = authManager.getCurrentUserId()
    
    // Transaction operations
    suspend fun insertTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            // Insert into local database
            val id = database.transactionDao().insert(transaction)
            
            // Only sync with Firestore if we have a user ID
            currentUserId?.let { userId ->
                val transactionWithId = if (transaction.id == 0L) transaction.copy(id = id) else transaction
                firestoreManager.saveTransaction(transactionWithId)
            }
        }
    }
    
    suspend fun updateTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            database.transactionDao().update(transaction)
            
            currentUserId?.let {
                firestoreManager.saveTransaction(transaction)
            }
        }
    }
    
    suspend fun deleteTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            database.transactionDao().delete(transaction)
            
            currentUserId?.let {
                firestoreManager.deleteTransaction(transaction.id.toString())
            }
        }
    }
    
    fun getAllTransactions(): LiveData<List<Transaction>> {
        return currentUserId?.let {
            database.transactionDao().getAllTransactions(it)
        } ?: database.transactionDao().getAllTransactions("")
    }
    
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return currentUserId?.let {
            database.transactionDao().getTransactionsByDateRange(it, startDate, endDate)
        } ?: database.transactionDao().getTransactionsByDateRange("", startDate, endDate)
    }
    
    fun getTransactionsByCategory(categoryId: Long): LiveData<List<Transaction>> {
        return currentUserId?.let {
            database.transactionDao().getTransactionsByCategory(it, categoryId)
        } ?: database.transactionDao().getTransactionsByCategory("", categoryId)
    }
    
    fun getTransactionsByType(isIncome: Boolean): LiveData<List<Transaction>> {
        return currentUserId?.let {
            database.transactionDao().getTransactionsByType(it, isIncome)
        } ?: database.transactionDao().getTransactionsByType("", isIncome)
    }
    
    fun getTotalExpenses(): LiveData<Double> {
        return currentUserId?.let {
            database.transactionDao().getTotalExpenses(it)
        } ?: database.transactionDao().getTotalExpenses("")
    }
    
    fun getTotalIncome(): LiveData<Double> {
        return currentUserId?.let {
            database.transactionDao().getTotalIncome(it)
        } ?: database.transactionDao().getTotalIncome("")
    }
    
    fun getCategoryExpenseForCurrentMonth(categoryId: Long): LiveData<Double> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        // Set to first day of month
        calendar.set(year, month, 1, 0, 0, 0)
        val startDate = calendar.time
        
        // Set to last day of month
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endDate = calendar.time
        
        return currentUserId?.let {
            database.transactionDao().getCategoryExpenseForPeriod(it, categoryId, startDate, endDate)
        } ?: database.transactionDao().getCategoryExpenseForPeriod("", categoryId, startDate, endDate)
    }
    
    // Budget operations
    suspend fun insertBudget(budget: Budget) {
        withContext(Dispatchers.IO) {
            val id = database.budgetDao().insert(budget)
            
            currentUserId?.let {
                val budgetWithId = if (budget.id == 0L) budget.copy(id = id) else budget
                firestoreManager.saveBudget(budgetWithId)
            }
        }
    }
    
    suspend fun updateBudget(budget: Budget) {
        withContext(Dispatchers.IO) {
            database.budgetDao().update(budget)
            
            currentUserId?.let {
                firestoreManager.saveBudget(budget)
            }
        }
    }
    
    suspend fun deleteBudget(budget: Budget) {
        withContext(Dispatchers.IO) {
            database.budgetDao().delete(budget)
            
            currentUserId?.let {
                firestoreManager.deleteBudget(budget.id.toString())
            }
        }
    }
    
    fun getAllBudgets(): LiveData<List<Budget>> {
        return currentUserId?.let {
            database.budgetDao().getAllBudgets(it)
        } ?: database.budgetDao().getAllBudgets("")
    }
    
    fun getBudgetsForCurrentMonth(): LiveData<List<Budget>> {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar months are 0-based, our Budget entity uses 1-based
        val year = calendar.get(Calendar.YEAR)
        
        return currentUserId?.let {
            database.budgetDao().getBudgetsForMonth(it, month, year)
        } ?: database.budgetDao().getBudgetsForMonth("", month, year)
    }
    
    fun getBudgetForCategoryAndCurrentMonth(categoryId: Long): LiveData<Budget> {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar months are 0-based, our Budget entity uses 1-based
        val year = calendar.get(Calendar.YEAR)
        
        return currentUserId?.let {
            database.budgetDao().getBudgetForCategoryAndMonth(it, categoryId, month, year)
        } ?: database.budgetDao().getBudgetForCategoryAndMonth("", categoryId, month, year)
    }
    
    fun getTotalBudgetForCurrentMonth(): LiveData<Double> {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar months are 0-based, our Budget entity uses 1-based
        val year = calendar.get(Calendar.YEAR)
        
        return currentUserId?.let {
            database.budgetDao().getTotalBudgetForMonth(it, month, year)
        } ?: database.budgetDao().getTotalBudgetForMonth("", month, year)
    }
    
    // Category operations
    suspend fun insertCategory(category: Category) {
        withContext(Dispatchers.IO) {
            val id = database.categoryDao().insert(category)
            
            currentUserId?.let {
                val categoryWithId = if (category.id == 0L) category.copy(id = id) else category
                firestoreManager.saveCategory(categoryWithId)
            }
        }
    }
    
    suspend fun updateCategory(category: Category) {
        withContext(Dispatchers.IO) {
            database.categoryDao().update(category)
            
            currentUserId?.let {
                firestoreManager.saveCategory(category)
            }
        }
    }
    
    suspend fun deleteCategory(category: Category) {
        withContext(Dispatchers.IO) {
            database.categoryDao().delete(category)
            
            currentUserId?.let {
                firestoreManager.deleteCategory(category.id.toString())
            }
        }
    }
    
    fun getAllCategories(): LiveData<List<Category>> {
        return currentUserId?.let {
            database.categoryDao().getAllCategories(it)
        } ?: database.categoryDao().getAllCategories("")
    }
    
    fun getCategoryById(id: Long): LiveData<Category> {
        return database.categoryDao().getCategoryById(id)
    }
    
    // Sync data from Firestore to local database
    fun syncData() {
        currentUserId?.let { userId ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Sync transactions
                    val transactions = firestoreManager.getTransactions(userId)
                    transactions.forEach { transaction ->
                        database.transactionDao().insert(transaction)
                    }
                    
                    // Sync budgets
                    val calendar = Calendar.getInstance()
                    val month = calendar.get(Calendar.MONTH) + 1
                    val year = calendar.get(Calendar.YEAR)
                    val budgets = firestoreManager.getBudgets(userId, month, year)
                    budgets.forEach { budget ->
                        database.budgetDao().insert(budget)
                    }
                    
                    // Sync categories
                    val categories = firestoreManager.getCategories(userId)
                    categories.forEach { category ->
                        database.categoryDao().insert(category)
                    }
                } catch (e: Exception) {
                    // Log the error but don't crash the app
                    Log.e("BudgetRepository", "Error syncing data: ${e.message}")
                }
            }
        }
    }
} 