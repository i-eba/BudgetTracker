package com.example.budgettracker.presenter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.budgettracker.data.BudgetRepository
import com.example.budgettracker.data.local.entities.Budget
import com.example.budgettracker.data.local.entities.Category
import com.example.budgettracker.data.remote.FirebaseAuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetPresenter(
    private val repository: BudgetRepository,
    private val authManager: FirebaseAuthManager
) {
    // Get user ID for budgets
    private val userId: String
        get() = authManager.getCurrentUserId() ?: ""
    
    // Budgets
    val allBudgets: LiveData<List<Budget>> = repository.getAllBudgets()
    val currentMonthBudgets: LiveData<List<Budget>> = repository.getBudgetsForCurrentMonth()
    val totalBudgetForCurrentMonth: LiveData<Double> = repository.getTotalBudgetForCurrentMonth()
    
    // Categories
    val allCategories: LiveData<List<Category>> = repository.getAllCategories()
    
    // Budget progress for all categories
    private val _budgetProgress = MediatorLiveData<Map<Long, Pair<Double, Double>>>() // CategoryId -> (Spent, Budget)
    val budgetProgress: LiveData<Map<Long, Pair<Double, Double>>> = _budgetProgress
    
    init {
        _budgetProgress.value = mapOf()
        
        // Update budget progress whenever budgets or categories change
        _budgetProgress.addSource(currentMonthBudgets) { budgets ->
            updateBudgetProgress(budgets)
        }
        
        _budgetProgress.addSource(allCategories) { categories ->
            val budgets = currentMonthBudgets.value ?: listOf()
            updateBudgetProgress(budgets)
        }
    }
    
    private fun updateBudgetProgress(budgets: List<Budget>) {
        val progressMap = mutableMapOf<Long, Pair<Double, Double>>()
        
        CoroutineScope(Dispatchers.IO).launch {
            budgets.forEach { budget ->
                val spent = repository.getCategoryExpenseForCurrentMonth(budget.categoryId).value ?: 0.0
                progressMap[budget.categoryId] = Pair(spent, budget.amount)
            }
            
            _budgetProgress.postValue(progressMap)
        }
    }
    
    fun getBudgetForCategory(categoryId: Long): LiveData<Budget> {
        return repository.getBudgetForCategoryAndCurrentMonth(categoryId)
    }
    
    fun getCategorySpendingForCurrentMonth(categoryId: Long): LiveData<Double> {
        return repository.getCategoryExpenseForCurrentMonth(categoryId)
    }
    
    fun addBudget(amount: Double, categoryId: Long) {
        if (validateBudget(amount, categoryId)) {
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1 // 1-based for our Budget entity
            val year = calendar.get(Calendar.YEAR)
            
            val budget = Budget(
                amount = amount,
                categoryId = categoryId,
                month = month,
                year = year,
                userId = userId
            )
            
            CoroutineScope(Dispatchers.IO).launch {
                repository.insertBudget(budget)
            }
        }
    }
    
    fun updateBudget(budget: Budget) {
        if (validateBudget(budget.amount, budget.categoryId)) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.updateBudget(budget)
            }
        }
    }
    
    fun deleteBudget(budget: Budget) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteBudget(budget)
        }
    }
    
    private fun validateBudget(amount: Double, categoryId: Long): Boolean {
        if (amount <= 0) {
            return false
        }
        
        if (categoryId <= 0) {
            return false
        }
        
        return true
    }
    
    // Helper functions for budget tracking UI
    fun getBudgetProgressPercentage(categoryId: Long): Double {
        val progress = _budgetProgress.value?.get(categoryId) ?: return 0.0
        val (spent, budget) = progress
        
        return if (budget > 0) {
            (spent / budget) * 100
        } else {
            0.0
        }
    }
    
    fun isBudgetNearLimit(categoryId: Long): Boolean {
        val percentage = getBudgetProgressPercentage(categoryId)
        return percentage >= 80
    }
    
    fun isBudgetOverLimit(categoryId: Long): Boolean {
        val percentage = getBudgetProgressPercentage(categoryId)
        return percentage > 100
    }
} 