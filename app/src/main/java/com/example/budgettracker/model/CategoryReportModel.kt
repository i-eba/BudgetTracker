package com.example.budgettracker.model

data class CategoryReportModel(
    val categoryId: Long,
    val categoryName: String,
    val amount: Double,
    val color: Int
) 