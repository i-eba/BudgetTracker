package com.example.budgettracker.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgettracker.data.local.entities.Transaction
import java.util.Date

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long
    
    @Update
    suspend fun update(transaction: Transaction)
    
    @Delete
    suspend fun delete(transaction: Transaction)
    
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): LiveData<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(userId: String, startDate: Date, endDate: Date): LiveData<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(userId: String, categoryId: Long): LiveData<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND isIncome = :isIncome ORDER BY date DESC")
    fun getTransactionsByType(userId: String, isIncome: Boolean): LiveData<List<Transaction>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isIncome = 0")
    fun getTotalExpenses(userId: String): LiveData<Double>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND isIncome = 1")
    fun getTotalIncome(userId: String): LiveData<Double>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND categoryId = :categoryId AND isIncome = 0 AND date BETWEEN :startDate AND :endDate")
    fun getCategoryExpenseForPeriod(userId: String, categoryId: Long, startDate: Date, endDate: Date): LiveData<Double>
} 