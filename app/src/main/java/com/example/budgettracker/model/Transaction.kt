package com.example.budgettracker.model

import java.util.Date

data class Transaction(
    val id: String,
    val name: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val isExpense: Boolean
) 