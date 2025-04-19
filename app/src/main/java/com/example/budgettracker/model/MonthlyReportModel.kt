package com.example.budgettracker.model

data class MonthlyReportModel(
    val month: String,
    val amount: Double,
    val percentageChange: Double,
    val isIncrease: Boolean
) 