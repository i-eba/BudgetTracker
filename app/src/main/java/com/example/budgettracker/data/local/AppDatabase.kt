package com.example.budgettracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgettracker.data.local.dao.BudgetDao
import com.example.budgettracker.data.local.dao.CategoryDao
import com.example.budgettracker.data.local.dao.TransactionDao
import com.example.budgettracker.data.local.entities.Budget
import com.example.budgettracker.data.local.entities.Category
import com.example.budgettracker.data.local.entities.Transaction
import com.example.budgettracker.util.DateConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class, Budget::class, Category::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with default categories
                        CoroutineScope(Dispatchers.IO).launch {
                            val categoryDao = getDatabase(context).categoryDao()
                            
                            // Create default categories if none exist
                            if (categoryDao.getCategoryCount() == 0) {
                                val defaultCategories = listOf(
                                    Category(name = "Food", color = 0xFF4CAF50.toInt()),
                                    Category(name = "Transportation", color = 0xFF2196F3.toInt()),
                                    Category(name = "Housing", color = 0xFF9C27B0.toInt()),
                                    Category(name = "Entertainment", color = 0xFFE91E63.toInt()),
                                    Category(name = "Utilities", color = 0xFFFF9800.toInt()),
                                    Category(name = "Health", color = 0xFFF44336.toInt()),
                                    Category(name = "Education", color = 0xFF3F51B5.toInt()),
                                    Category(name = "Shopping", color = 0xFF00BCD4.toInt()),
                                    Category(name = "Salary", color = 0xFF009688.toInt()),
                                    Category(name = "Paycheck", color = 0xFF8BC34A.toInt()),
                                    Category(name = "Other", color = 0xFF607D8B.toInt())
                                )
                                categoryDao.insertAll(defaultCategories)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 