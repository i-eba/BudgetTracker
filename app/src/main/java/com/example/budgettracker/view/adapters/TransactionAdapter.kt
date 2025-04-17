package com.example.budgettracker.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgettracker.databinding.ItemTransactionBinding
import com.example.budgettracker.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun getItemCount(): Int = transactions.size

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvTransactionName.text = transaction.name
            binding.tvTransactionCategory.text = transaction.category
            
            // Format amount with currency sign
            val amountText = if (transaction.isExpense) "-$${transaction.amount}" else "+$${transaction.amount}"
            binding.tvTransactionAmount.text = amountText
            
            // Set color based on transaction type: red for expenses, green for income
            val textColor = if (transaction.isExpense) {
                Color.RED
            } else {
                Color.parseColor("#4CAF50") // Material Green
            }
            binding.tvTransactionAmount.setTextColor(textColor)
            
            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvTransactionDate.text = dateFormat.format(transaction.date)
            
            // Set click listener
            binding.root.setOnClickListener {
                onItemClick(transaction)
            }
        }
    }
} 