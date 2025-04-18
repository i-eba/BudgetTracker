package com.example.budgettracker.data.remote

import com.example.budgettracker.data.local.entities.Budget
import com.example.budgettracker.data.local.entities.Category
import com.example.budgettracker.data.local.entities.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    
    // Collection references
    private val transactionsCollection = db.collection("transactions")
    private val budgetsCollection = db.collection("budgets")
    private val categoriesCollection = db.collection("categories")
    
    // Transaction operations
    suspend fun saveTransaction(transaction: Transaction): String {
        val transactionMap = mapOf(
            "id" to transaction.id,
            "amount" to transaction.amount,
            "description" to transaction.description,
            "date" to transaction.date,
            "categoryId" to transaction.categoryId,
            "isIncome" to transaction.isIncome,
            "userId" to transaction.userId
        )
        
        val documentRef = if (transaction.id == 0L) {
            transactionsCollection.add(transactionMap).await()
        } else {
            val docRef = transactionsCollection.document(transaction.id.toString())
            docRef.set(transactionMap).await()
            docRef
        }
        
        return documentRef.id
    }
    
    suspend fun getTransactions(userId: String): List<Transaction> {
        return transactionsCollection
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("__name__", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val id = doc.getLong("id") ?: 0
                val amount = doc.getDouble("amount") ?: 0.0
                val description = doc.getString("description") ?: ""
                val date = doc.getDate("date") ?: Date()
                val categoryId = doc.getLong("categoryId") ?: 0
                val isIncome = doc.getBoolean("isIncome") ?: false
                
                Transaction(
                    id = id,
                    amount = amount,
                    description = description,
                    date = date,
                    categoryId = categoryId,
                    isIncome = isIncome,
                    userId = userId
                )
            }
    }
    
    suspend fun deleteTransaction(transactionId: String) {
        transactionsCollection.document(transactionId).delete().await()
    }
    
    // Budget operations
    suspend fun saveBudget(budget: Budget): String {
        val budgetMap = mapOf(
            "id" to budget.id,
            "amount" to budget.amount,
            "categoryId" to budget.categoryId,
            "month" to budget.month,
            "year" to budget.year,
            "userId" to budget.userId
        )
        
        val documentRef = if (budget.id == 0L) {
            budgetsCollection.add(budgetMap).await()
        } else {
            val docRef = budgetsCollection.document(budget.id.toString())
            docRef.set(budgetMap).await()
            docRef
        }
        
        return documentRef.id
    }
    
    suspend fun getBudgets(userId: String, month: Int, year: Int): List<Budget> {
        return budgetsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val id = doc.getLong("id") ?: 0
                val amount = doc.getDouble("amount") ?: 0.0
                val categoryId = doc.getLong("categoryId") ?: 0
                
                Budget(
                    id = id,
                    amount = amount,
                    categoryId = categoryId,
                    month = month,
                    year = year,
                    userId = userId
                )
            }
    }
    
    suspend fun deleteBudget(budgetId: String) {
        budgetsCollection.document(budgetId).delete().await()
    }
    
    // Category operations
    suspend fun saveCategory(category: Category): String {
        val categoryMap = mapOf(
            "id" to category.id,
            "name" to category.name,
            "color" to category.color,
            "iconResId" to category.iconResId,
            "userId" to category.userId
        )
        
        val documentRef = if (category.id == 0L) {
            categoriesCollection.add(categoryMap).await()
        } else {
            val docRef = categoriesCollection.document(category.id.toString())
            docRef.set(categoryMap).await()
            docRef
        }
        
        return documentRef.id
    }
    
    suspend fun getCategories(userId: String): List<Category> {
        return categoriesCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val id = doc.getLong("id") ?: 0
                val name = doc.getString("name") ?: ""
                val color = doc.getLong("color")?.toInt() ?: 0
                val iconResId = doc.getLong("iconResId")?.toInt()
                
                Category(
                    id = id,
                    name = name,
                    color = color,
                    iconResId = iconResId,
                    userId = userId
                )
            }
    }
    
    suspend fun deleteCategory(categoryId: String) {
        categoriesCollection.document(categoryId).delete().await()
    }
} 