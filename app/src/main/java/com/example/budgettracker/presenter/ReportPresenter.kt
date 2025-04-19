package com.example.budgettracker.presenter

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.budgettracker.data.BudgetRepository
import com.example.budgettracker.data.local.entities.Category
import com.example.budgettracker.data.local.entities.Transaction
import com.example.budgettracker.model.CategoryReportModel
import com.example.budgettracker.model.MonthlyReportModel
import com.example.budgettracker.util.CSVExporter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class ReportPresenter(
    // Expose the repository for test data creation
    val repository: BudgetRepository,
    private val csvExporter: CSVExporter
) {
    private val TAG = "ReportPresenter"
    
    // Current user ID from Firebase Auth
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
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
    
    // Category expense report data for pie chart
    private val _categoryExpenseReport = MutableLiveData<List<CategoryReportModel>>()
    val categoryExpenseReport: LiveData<List<CategoryReportModel>> = _categoryExpenseReport
    
    // Total spending amount for the current period
    private val _totalSpending = MutableLiveData<Double>()
    val totalSpending: LiveData<Double> = _totalSpending
    
    // Monthly income data for bar chart
    private val _monthlyIncome = MutableLiveData<List<Pair<String, Double>>>()
    val monthlyIncome: LiveData<List<Pair<String, Double>>> = _monthlyIncome
    
    // Monthly spending trends
    private val _monthlySpending = MutableLiveData<List<Pair<String, Double>>>()
    val monthlySpending: LiveData<List<Pair<String, Double>>> = _monthlySpending
    
    // Export result
    private val _exportResult = MutableLiveData<Result<File>>()
    val exportResult: LiveData<Result<File>> = _exportResult
    
    // Category colors
    private val categoryColors = mapOf(
        1L to Color.parseColor("#558B2F"),  // Food - Dark green
        2L to Color.parseColor("#3F51B5"),  // Transportation - Blue
        3L to Color.parseColor("#5D4037"),  // Housing - Brown
        4L to Color.parseColor("#FFA000"),  // Entertainment - Amber
        5L to Color.parseColor("#4CAF50"),  // Utilities - Green
        6L to Color.parseColor("#7C4DFF"),  // Healthcare - Deep Purple
        7L to Color.parseColor("#8BC34A"),  // Others - Light Green
        8L to Color.parseColor("#F44336")   // Paycheck - Red
    )
    
    init {
        Log.d(TAG, "Initializing ReportPresenter")
        
        // Set initial date range to current month
        setCurrentMonthDateRange()
        
        // Get all transactions initially for testing
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allTrans = repository.getAllTransactionsSync()
                Log.d(TAG, "All transactions sync: ${allTrans.size}")
                allTrans.forEach { transaction ->
                    Log.d(TAG, "Transaction sync: id=${transaction.id}, date=${formatDate(transaction.date)}, amount=${transaction.amount}, isIncome=${transaction.isIncome}, category=${transaction.categoryId}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting all transactions sync", e)
            }
        }
        
        // Update category spending when transactions or categories change
        _categorySpending.addSource(periodTransactions) { transactions ->
            Log.d(TAG, "Period transactions updated: ${transactions.size}")
            calculateCategorySpending(transactions)
        }
        
        _categorySpending.addSource(allCategories) { categories ->
            Log.d(TAG, "Categories updated: ${categories.size}")
            val transactions = periodTransactions.value ?: listOf()
            calculateCategorySpending(transactions)
        }
        
        // Update category expense report when categories or spending changes
        allCategories.observeForever { categories ->
            Log.d(TAG, "Categories observed: ${categories.size}")
            val spendingMap = _categorySpending.value ?: mapOf()
            updateCategoryExpenseReport(categories, spendingMap)
        }
        
        _categorySpending.observeForever { spendingMap ->
            Log.d(TAG, "Spending map observed: ${spendingMap.size}")
            val categories = allCategories.value ?: listOf()
            updateCategoryExpenseReport(categories, spendingMap)
        }
        
        // Generate monthly income data using sync method
        generateMonthlyIncomeDataSync()
    }
    
    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(date)
    }
    
    fun setCurrentMonthDateRange() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        // Set to first day of month at midnight
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        // Set to last day of month at midnight
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time
        
        Log.d(TAG, "Setting date range: ${formatDate(startDate)} to ${formatDate(endDate)}")
        
        _currentDateRange.value = Pair(startDate, endDate)
        fetchTransactionsForDateRange(startDate, endDate)
    }
    
    fun setDateRange(startDate: Date, endDate: Date) {
        _currentDateRange.value = Pair(startDate, endDate)
        fetchTransactionsForDateRange(startDate, endDate)
    }
    
    private fun fetchTransactionsForDateRange(startDate: Date, endDate: Date) {
        Log.d(TAG, "Fetching transactions from ${formatDate(startDate)} to ${formatDate(endDate)}")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use direct synchronous call instead of LiveData
                val transactions = repository.getTransactionsByDateRangeSync(startDate, endDate)
                Log.d(TAG, "Found ${transactions.size} transactions in date range using sync method")
                
                if (transactions.isEmpty()) {
                    Log.d(TAG, "No transactions found in date range using sync method")
                }
                
                // Switch to main thread before updating LiveData
                withContext(Dispatchers.Main) {
                    _periodTransactions.value = transactions
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching transactions", e)
                // Switch to main thread before updating LiveData
                withContext(Dispatchers.Main) {
                    _periodTransactions.value = emptyList()
                }
            }
        }
    }
    
    private fun calculateCategorySpending(transactions: List<Transaction>) {
        val spendingMap = mutableMapOf<Long, Double>()
        
        // Only include expenses
        val expenses = transactions.filter { !it.isIncome }
        
        Log.d(TAG, "Calculating spending for ${expenses.size} expense transactions out of ${transactions.size} total")
        
        if (expenses.isEmpty()) {
            Log.d(TAG, "No expense transactions found to calculate spending")
            _categorySpending.value = spendingMap
            return
        }
        
        // Group by category and sum
        expenses.forEach { transaction ->
            val currentAmount = spendingMap[transaction.categoryId] ?: 0.0
            spendingMap[transaction.categoryId] = currentAmount + transaction.amount
            Log.d(TAG, "Adding ${transaction.amount} to category ${transaction.categoryId}, new total: ${currentAmount + transaction.amount}")
        }
        
        Log.d(TAG, "Category spending map: $spendingMap")
        _categorySpending.value = spendingMap
    }
    
    private fun updateCategoryExpenseReport(categories: List<Category>, spendingMap: Map<Long, Double>) {
        val expenseDataList = ArrayList<PieEntry>()
        var totalSpending = 0.0
        val colorsArray = ArrayList<Int>()
        
        // First, calculate total actual spending (to prevent division by zero)
        spendingMap.entries.forEach { entry ->
            if (entry.key != 8L) { // Skip Paycheck category
                totalSpending += entry.value
            }
        }
        
        // Include all categories except Paycheck, even with zero spending
        categories.forEach { category ->
            if (category.id != 8L) { // Exclude Paycheck category
                val amount = spendingMap[category.id] ?: 0.0
                
                // Add all categories to the pie chart, even those with zero spending
                if (totalSpending > 0) {
                    // If there's actual spending, use the real values
                    Log.d(TAG, "Adding category ${category.name} with spending: $amount")
                    // Use a minimum value of 1f for categories with zero spending to make them visible
                    val displayAmount = if (amount > 0) amount.toFloat() else 1f
                    expenseDataList.add(PieEntry(displayAmount, category.name))
                    colorsArray.add(categoryColors[category.id] ?: Color.GRAY)
                } else {
                    // If there's no spending at all, show equal parts for all categories
                    Log.d(TAG, "No spending yet, adding category ${category.name} with minimal value")
                    expenseDataList.add(PieEntry(1f, category.name))
                    colorsArray.add(categoryColors[category.id] ?: Color.GRAY)
                }
            }
        }
        
        // Only update if there's data to show
        if (expenseDataList.isNotEmpty()) {
            val dataSet = PieDataSet(expenseDataList, "Expenses by Category")
            dataSet.colors = colorsArray
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = Color.WHITE
            
            val pieData = PieData(dataSet)
            // Format values as currency
            pieData.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (totalSpending == 0.0 || value <= 1f && totalSpending > 0) {
                        "$0" // Show $0 for categories with no spending
                    } else {
                        "$${value.toInt()}"
                    }
                }
            })
            
            val reportItems = expenseDataList.mapIndexed { index, entry ->
                // Find the category for this entry
                val category = categories.find { it.name == entry.label }
                
                CategoryReportModel(
                    categoryId = category?.id ?: 0L,
                    categoryName = entry.label,
                    // Store actual spending amount (0.0 for categories with no spending)
                    amount = spendingMap[category?.id] ?: 0.0,
                    color = category?.let { categoryColors[it.id] } ?: Color.GRAY
                )
            }
            
            Log.d(TAG, "Updating category expense report with ${reportItems.size} items, total spending: $totalSpending")
            _categoryExpenseReport.value = reportItems
            // Update total spending
            _totalSpending.value = totalSpending
        }
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
                val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                monthlyData.add(Pair("$monthName", totalExpense))
            }
            
            withContext(Dispatchers.Main) {
                _monthlySpending.value = monthlyData
            }
        }
    }
    
    fun generateMonthlyIncomeData() {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            
            Log.d(TAG, "Generating monthly income data for current month: $currentMonth, year: $currentYear")
            
            val monthlyData = mutableListOf<Pair<String, Double>>()
            
            // First, get all income transactions with categoryId=8 (Paycheck)
            val allPaychecks = allTransactions.value
                ?.filter { it.isIncome && it.categoryId == 8L }
                ?: listOf()
            
            Log.d(TAG, "Found ${allPaychecks.size} total paycheck transactions")
            allPaychecks.forEach { transaction ->
                Log.d(TAG, "Paycheck: amount=${transaction.amount}, date=${formatDate(transaction.date)}")
            }
            
            // Use sample data only if there are no paycheck transactions at all
            val useSampleData = allPaychecks.isEmpty()
            
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
                
                Log.d(TAG, "Processing income for month[$i]: ${formatDate(startDate)} to ${formatDate(endDate)}")
                
                if (!useSampleData) {
                    // Filter paychecks for this month only
                    val monthPaychecks = allPaychecks.filter { 
                        it.date.time >= startDate.time && it.date.time <= endDate.time 
                    }
                    
                    Log.d(TAG, "Found ${monthPaychecks.size} paycheck transactions for ${calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())}")
                    
                    // Calculate total income for this month
                    val totalIncome = monthPaychecks.sumOf { it.amount }
                    
                    // Format month name (e.g., "Jan", "Feb")
                    val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                    monthlyData.add(Pair(monthName, totalIncome))
                    
                    Log.d(TAG, "Month: $monthName, Real Income: $totalIncome")
                } else {
                    // Generate sample income data as fallback
                    val sampleIncome = when (i) {
                        5 -> 1200.0  // Nov
                        4 -> 1450.0  // Dec
                        3 -> 1700.0  // Jan
                        2 -> 1650.0  // Feb
                        1 -> 2040.0  // Mar
                        0 -> 2300.0  // Apr (current)
                        else -> 1500.0
                    }
                    
                    // Format month name (e.g., "Jan", "Feb")
                    val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                    monthlyData.add(Pair(monthName, sampleIncome))
                    
                    Log.d(TAG, "Month: $monthName, Sample Income: $sampleIncome")
                }
            }
            
            withContext(Dispatchers.Main) {
                _monthlyIncome.value = monthlyData
                Log.d(TAG, "Updated monthly income LiveData with ${monthlyData.size} months of data")
            }
        }
    }
    
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(amount)
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
    
    // Add this function to directly generate dummy pie chart data
    fun generateDummyDataForReports() {
        Log.d(TAG, "Explicitly generating dummy data for reports")
        
        // Create dummy category data for the pie chart
        val dummyCategories = listOf(
            CategoryReportModel(1L, "Food", 450.0, Color.parseColor("#558B2F")),
            CategoryReportModel(2L, "Transportation", 320.0, Color.parseColor("#3F51B5")),
            CategoryReportModel(3L, "Housing", 980.0, Color.parseColor("#5D4037")),
            CategoryReportModel(4L, "Entertainment", 250.0, Color.parseColor("#FFA000")),
            CategoryReportModel(5L, "Utilities", 180.0, Color.parseColor("#4CAF50")),
            CategoryReportModel(6L, "Healthcare", 120.0, Color.parseColor("#7C4DFF")),
            CategoryReportModel(7L, "Others", 220.0, Color.parseColor("#8BC34A"))
        )
        
        // Update category expense report LiveData with dummy data
        _categoryExpenseReport.value = dummyCategories
        
        // Also generate dummy data for monthly income
        val monthNames = listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr")
        val monthlyIncomeDummy = monthNames.mapIndexed { index, month ->
            // Random values between 1000 and 3000
            val amount = 1000.0 + (2000.0 * index / 5.0)
            Pair(month, amount)
        }
        _monthlyIncome.value = monthlyIncomeDummy
    }
    
    // Add generateMonthlyIncomeDataSync as a replacement for the current method
    fun generateMonthlyIncomeDataSync() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                
                Log.d(TAG, "Generating monthly income data (sync) for current month: $currentMonth, year: $currentYear")
                
                val monthlyData = mutableListOf<Pair<String, Double>>()
                
                // Get all paycheck transactions directly
                val allPaychecks = repository.getTransactionsByCategoryIdSync(8L, true)
                
                Log.d(TAG, "Found ${allPaychecks.size} total paycheck transactions using sync method")
                allPaychecks.forEach { transaction ->
                    Log.d(TAG, "Paycheck: amount=${transaction.amount}, date=${formatDate(transaction.date)}")
                }
                
                // Use sample data only if there are no paycheck transactions at all
                val useSampleData = allPaychecks.isEmpty()
                
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
                    
                    Log.d(TAG, "Processing income for month[$i]: ${formatDate(startDate)} to ${formatDate(endDate)}")
                    
                    if (!useSampleData) {
                        // Filter paychecks for this month manually
                        val monthPaychecks = allPaychecks.filter { 
                            it.date.time >= startDate.time && it.date.time <= endDate.time 
                        }
                        
                        Log.d(TAG, "Found ${monthPaychecks.size} paycheck transactions for ${calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())}")
                        
                        // Calculate total income for this month
                        val totalIncome = monthPaychecks.sumOf { it.amount }
                        
                        // Format month name (e.g., "Jan", "Feb")
                        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
                        monthlyData.add(Pair(monthName, totalIncome))
                        
                        Log.d(TAG, "Month: $monthName, Real Income: $totalIncome")
                    } else {
                        // Generate sample income data as fallback
                        val sampleIncome = when (i) {
                            5 -> 1200.0  // Nov
                            4 -> 1450.0  // Dec
                            3 -> 1700.0  // Jan
                            2 -> 1650.0  // Feb
                            1 -> 2040.0  // Mar
                            0 -> 2300.0  // Apr (current)
                            else -> 1500.0
                        }
                        
                        // Format month name (e.g., "Jan", "Feb")
                        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
                        monthlyData.add(Pair(monthName, sampleIncome))
                        
                        Log.d(TAG, "Month: $monthName, Sample Income: $sampleIncome")
                    }
                }
                
                withContext(Dispatchers.Main) {
                    _monthlyIncome.value = monthlyData
                    Log.d(TAG, "Updated monthly income LiveData with ${monthlyData.size} months of data")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating monthly income data", e)
            }
        }
    }
    
    // Replace the existing refreshDataFromFirestore to use the new sync methods
    fun refreshDataFromFirestore() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Forcing data refresh from Firestore")
                repository.syncData()
                
                // Wait a brief moment for sync to complete
                delay(1000)
                
                // Then refresh our current data
                withContext(Dispatchers.Main) {
                    // Get the current date range
                    val dateRange = _currentDateRange.value
                    if (dateRange != null) {
                        // Re-fetch transactions with the current date range
                        Log.d(TAG, "Re-fetching transactions with current date range after sync")
                        fetchTransactionsForDateRange(dateRange.first, dateRange.second)
                    } else {
                        // If no date range is set, use current month
                        Log.d(TAG, "No date range set, using current month")
                        setCurrentMonthDateRange()
                    }
                    
                    // Refresh other data using new sync methods
                    generateMonthlyIncomeDataSync()
                    generateMonthlySpendingTrend()
                }
                
                Log.d(TAG, "Data refresh completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data: ${e.message}", e)
            }
        }
    }
} 