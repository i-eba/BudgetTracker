package com.example.budgettracker.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgettracker.data.local.entities.Budget

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long
    
    @Update
    suspend fun update(budget: Budget)
    
    @Delete
    suspend fun delete(budget: Budget)
    
    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun getAllBudgets(userId: String): LiveData<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    fun getBudgetsForMonth(userId: String, month: Int, year: Int): LiveData<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId = :categoryId AND month = :month AND year = :year")
    fun getBudgetForCategoryAndMonth(userId: String, categoryId: Long, month: Int, year: Int): LiveData<Budget>
    
    @Query("SELECT SUM(amount) FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    fun getTotalBudgetForMonth(userId: String, month: Int, year: Int): LiveData<Double>
} 