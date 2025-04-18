package com.example.budgettracker.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgettracker.databinding.ItemBudgetBinding
import com.example.budgettracker.model.BudgetModel

class BudgetAdapter(
    private var budgets: List<BudgetModel>,
    private val onItemClick: (BudgetModel) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BudgetViewHolder(binding)
    }

    override fun getItemCount(): Int = budgets.size

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgets[position])
    }

    fun updateBudgets(newBudgets: List<BudgetModel>) {
        budgets = newBudgets
        notifyDataSetChanged()
    }

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(budget: BudgetModel) {
            binding.tvBudgetName.text = budget.name
            
            // Set progress
            binding.progressBudget.max = 100
            binding.progressBudget.progress = budget.progressPercentage.toInt()
            
            // Change color to red if over 80%
            if (budget.progressPercentage >= 80) {
                binding.progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(Color.RED)
            } else {
                binding.progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            }
            
            // Format amounts with currency sign
            binding.tvCurrentAmount.text = "$${budget.currentAmount}"
            binding.tvMaxAmount.text = "$${budget.maxAmount}"
            
            // Set click listener
            binding.root.setOnClickListener {
                onItemClick(budget)
            }
        }
    }
} 