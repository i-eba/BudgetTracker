package com.example.budgettracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val categoryId: Long,
    val month: Int, // 1-12 for Jan-Dec
    val year: Int,
    val userId: String // To associate with Firebase Auth user ID
) 