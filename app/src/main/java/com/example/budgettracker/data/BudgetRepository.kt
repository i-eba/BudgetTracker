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
import java.text.SimpleDateFormat
import java.util.Locale

class BudgetRepository(
    private val database: AppDatabase,
    private val firestoreManager: FirestoreManager = FirestoreManager(),
    private val authManager: FirebaseAuthManager = FirebaseAuthManager()
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
        val userId = currentUserId
        Log.d("BudgetRepository", "Getting all transactions for userId: $userId")
        return userId?.let {
            database.transactionDao().getAllTransactions(it)
        } ?: database.transactionDao().getAllTransactions("")
    }
    
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        val userId = currentUserId
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        Log.d("BudgetRepository", "Getting transactions by date range for userId: $userId, " +
              "startDate: ${dateFormat.format(startDate)}, endDate: ${dateFormat.format(endDate)}")
        return userId?.let {
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
                    Log.d("BudgetRepository", "Starting data sync for user: $userId")
                    
                    // Sync transactions
                    val transactions = firestoreManager.getTransactions(userId)
                    Log.d("BudgetRepository", "Fetched ${transactions.size} transactions from Firestore")
                    
                    if (transactions.isNotEmpty()) {
                        // Insert or update each transaction in the local database
                        transactions.forEach { transaction ->
                            try {
                                // Check if transaction already exists
                                val existingTransaction = database.transactionDao().getTransactionById(transaction.id)
                                
                                if (existingTransaction == null) {
                                    // If not exists, insert
                                    database.transactionDao().insert(transaction)
                                    Log.d("BudgetRepository", "Inserted transaction: ${transaction.id}")
                                } else {
                                    // If exists, update
                                    database.transactionDao().update(transaction)
                                    Log.d("BudgetRepository", "Updated transaction: ${transaction.id}")
                                }
                            } catch (e: Exception) {
                                Log.e("BudgetRepository", "Error saving transaction ${transaction.id}: ${e.message}")
                            }
                        }
                    }
                    
                    // Sync budgets
                    val calendar = Calendar.getInstance()
                    val month = calendar.get(Calendar.MONTH) + 1
                    val year = calendar.get(Calendar.YEAR)
                    val budgets = firestoreManager.getBudgets(userId, month, year)
                    Log.d("BudgetRepository", "Fetched ${budgets.size} budgets from Firestore")
                    
                    budgets.forEach { budget ->
                        try {
                            database.budgetDao().insert(budget)
                        } catch (e: Exception) {
                            Log.e("BudgetRepository", "Error saving budget ${budget.id}: ${e.message}")
                        }
                    }
                    
                    // Sync categories
                    val categories = firestoreManager.getCategories(userId)
                    Log.d("BudgetRepository", "Fetched ${categories.size} categories from Firestore")
                    
                    categories.forEach { category ->
                        try {
                            database.categoryDao().insert(category)
                        } catch (e: Exception) {
                            Log.e("BudgetRepository", "Error saving category ${category.id}: ${e.message}")
                        }
                    }
                    
                    Log.d("BudgetRepository", "Data sync completed successfully")
                    
                } catch (e: Exception) {
                    // Log the error but don't crash the app
                    Log.e("BudgetRepository", "Error syncing data: ${e.message}", e)
                }
            }
        }
    }

    fun ensureDefaultCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if categories exist
            val existingCategories = getAllCategories().value
            if (existingCategories.isNullOrEmpty()) {
                Log.d("BudgetRepository", "Creating default categories")
                
                // Get current user ID
                val userId = currentUserId ?: "default_user"
                
                // Category colors based on material design palette
                val categoryColors = mapOf(
                    1 to android.graphics.Color.parseColor("#558B2F"),  // Food - Dark green
                    2 to android.graphics.Color.parseColor("#3F51B5"),  // Transportation - Blue
                    3 to android.graphics.Color.parseColor("#5D4037"),  // Housing - Brown
                    4 to android.graphics.Color.parseColor("#FFA000"),  // Entertainment - Amber
                    5 to android.graphics.Color.parseColor("#4CAF50"),  // Utilities - Green
                    6 to android.graphics.Color.parseColor("#7C4DFF"),  // Healthcare - Deep Purple
                    7 to android.graphics.Color.parseColor("#8BC34A"),  // Others - Light Green
                    8 to android.graphics.Color.parseColor("#F44336")   // Paycheck - Red
                )
                
                // Create default categories
                val defaultCategories = listOf(
                    Category(1, "Food", categoryColors[1]!!, null, userId),
                    Category(2, "Transportation", categoryColors[2]!!, null, userId),
                    Category(3, "Housing", categoryColors[3]!!, null, userId),
                    Category(4, "Entertainment", categoryColors[4]!!, null, userId),
                    Category(5, "Utilities", categoryColors[5]!!, null, userId),
                    Category(6, "Healthcare", categoryColors[6]!!, null, userId),
                    Category(7, "Others", categoryColors[7]!!, null, userId),
                    Category(8, "Paycheck", categoryColors[8]!!, null, userId)
                )
                
                // Insert default categories
                defaultCategories.forEach { category ->
                    insertCategory(category)
                }
            }
        }
    }
} 