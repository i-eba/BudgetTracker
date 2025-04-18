package com.example.budgettracker.model

data class BudgetModel(
    val id: String,
    val name: String,
    val categoryId: Long,
    val categoryName: String,
    val maxAmount: Double,
    val currentAmount: Double,
    val progressPercentage: Double
) 