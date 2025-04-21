package com.example.budgettracker.util

import android.content.Context
import com.example.budgettracker.data.local.entities.Category
import com.example.budgettracker.data.local.entities.Transaction
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Locale

class CSVExporter {
    
    suspend fun exportTransactions(
        context: Context,
        transactions: List<Transaction>,
        categories: Map<Long, Category>
    ): File {
        // Create file in app's external files directory
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(java.util.Date())
        val fileName = "budget_tracker_export_$timestamp.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        // Create CSV file
        FileWriter(file).use { fileWriter ->
            // Use builder pattern instead of deprecated withHeader method
            val csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Date", "Category", "Description", "Amount", "Type")
                .build()
                
            CSVPrinter(fileWriter, csvFormat).use { csvPrinter ->
                transactions.forEach { transaction ->
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(transaction.date)
                    val categoryName = categories[transaction.categoryId]?.name ?: "Unknown"
                    val type = if (transaction.isIncome) "Income" else "Expense"
                    
                    csvPrinter.printRecord(
                        dateStr,
                        categoryName,
                        transaction.description,
                        transaction.amount,
                        type
                    )
                }
                
                csvPrinter.flush()
            }
        }
        
        return file
    }
} 