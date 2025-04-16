package com.example.budgettracker.presenter

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.budgettracker.data.BudgetRepository
import com.example.budgettracker.data.local.entities.Category
import com.example.budgettracker.data.local.entities.Transaction
import com.example.budgettracker.util.CSVExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.Date

class ReportPresenter(
    private val repository: BudgetRepository,
    private val csvExporter: CSVExporter
) {
    // Data sources
    val allTransactions: LiveData<List<Transaction>> = repository.getAllTransactions()
    val allCategories: LiveData<List<Category>> = repository.getAllCategories()
    val totalExpenses: LiveData<Double> = repository.getTotalExpenses()
    val totalIncome: LiveData<Double> = repository.getTotalIncome()
    
    // Filtered transactions by date range
    private val _currentDateRange = MutableLiveData<Pair<Date, Date>>()
    val currentDateRange: LiveData<Pair<Date, Date>> = _currentDateRange
    
    // Transactions for the current period
    private val _periodTransactions = MutableLiveData<List<Transaction>>()
    val periodTransactions: LiveData<List<Transaction>> = _periodTransactions
    
    // Category spending breakdown
    private val _categorySpending = MediatorLiveData<Map<Long, Double>>()
    val categorySpending: LiveData<Map<Long, Double>> = _categorySpending
    
    // Monthly spending trends
    private val _monthlySpending = MutableLiveData<List<Pair<String, Double>>>()
    val monthlySpending: LiveData<List<Pair<String, Double>>> = _monthlySpending
    
    // Export result
    private val _exportResult = MutableLiveData<Result<File>>()
    val exportResult: LiveData<Result<File>> = _exportResult
    
    init {
        // Set initial date range to current month
        setCurrentMonthDateRange()
        
        // Update category spending when transactions or categories change
        _categorySpending.addSource(periodTransactions) { transactions ->
            calculateCategorySpending(transactions)
        }
        
        _categorySpending.addSource(allCategories) { _ ->
            val transactions = periodTransactions.value ?: listOf()
            calculateCategorySpending(transactions)
        }
    }
    
    fun setCurrentMonthDateRange() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        // Set to first day of month
        calendar.set(year, month, 1, 0, 0, 0)
        val startDate = calendar.time
        
        // Set to last day of month
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endDate = calendar.time
        
        _currentDateRange.value = Pair(startDate, endDate)
        fetchTransactionsForDateRange(startDate, endDate)
    }
    
    fun setDateRange(startDate: Date, endDate: Date) {
        _currentDateRange.value = Pair(startDate, endDate)
        fetchTransactionsForDateRange(startDate, endDate)
    }
    
    private fun fetchTransactionsForDateRange(startDate: Date, endDate: Date) {
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = repository.getTransactionsByDateRange(startDate, endDate).value ?: listOf()
            _periodTransactions.postValue(transactions)
        }
    }
    
    private fun calculateCategorySpending(transactions: List<Transaction>) {
        val spendingMap = mutableMapOf<Long, Double>()
        
        // Only include expenses
        val expenses = transactions.filter { !it.isIncome }
        
        // Group by category and sum
        expenses.forEach { transaction ->
            val currentAmount = spendingMap[transaction.categoryId] ?: 0.0
            spendingMap[transaction.categoryId] = currentAmount + transaction.amount
        }
        
        _categorySpending.value = spendingMap
    }
    
    fun generateMonthlySpendingTrend() {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            
            val monthlyData = mutableListOf<Pair<String, Double>>()
            
            // Get data for the last 6 months
            for (i in 5 downTo 0) {
                val targetMonth = (currentMonth - i + 12) % 12
                val targetYear = currentYear - if (targetMonth > currentMonth) 1 else 0
                
                // Set calendar to first day of target month
                calendar.set(targetYear, targetMonth, 1, 0, 0, 0)
                val startDate = calendar.time
                
                // Set calendar to last day of target month
                calendar.set(targetYear, targetMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                val endDate = calendar.time
                
                // Get transactions for the month
                val monthTransactions = repository.getTransactionsByDateRange(startDate, endDate).value ?: listOf()
                
                // Calculate total expenses
                val totalExpense = monthTransactions
                    .filter { !it.isIncome }
                    .sumOf { it.amount }
                
                // Format month name (e.g., "Jan", "Feb")
                val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, java.util.Locale.getDefault())
                monthlyData.add(Pair("$monthName", totalExpense))
            }
            
            withContext(Dispatchers.Main) {
                _monthlySpending.value = monthlyData
            }
        }
    }
    
    fun exportTransactionsToCSV(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val transactions = allTransactions.value ?: listOf()
                val categories = allCategories.value ?: listOf()
                
                // Map category IDs to names for easier export
                val categoryMap = categories.associateBy { it.id }
                
                val file = csvExporter.exportTransactions(context, transactions, categoryMap)
                _exportResult.postValue(Result.success(file))
            } catch (e: Exception) {
                _exportResult.postValue(Result.failure(e))
            }
        }
    }
} 