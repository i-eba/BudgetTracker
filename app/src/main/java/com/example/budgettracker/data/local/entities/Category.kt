package com.example.budgettracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int, // Color for charts and UI elements
    val iconResId: Int? = null, // Optional icon resource ID
    val userId: String? = null // Null for default categories, otherwise Firebase Auth user ID
) 