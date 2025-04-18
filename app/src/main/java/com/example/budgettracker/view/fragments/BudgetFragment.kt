package com.example.budgettracker.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgettracker.R
import com.example.budgettracker.databinding.FragmentBudgetBinding
import com.example.budgettracker.model.BudgetModel
import com.example.budgettracker.presenter.BudgetPresenter
import com.example.budgettracker.view.adapters.BudgetAdapter

class BudgetFragment : Fragment() {
    
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var presenter: BudgetPresenter
    private lateinit var budgetAdapter: BudgetAdapter
    
    // Map of category IDs to category names
    private val categoryMap = mapOf(
        1L to "Food",
        2L to "Transportation",
        3L to "Housing",
        4L to "Entertainment",
        5L to "Utilities", 
        6L to "Healthcare",
        7L to "Others",
        8L to "Paycheck"
    )
    
    // Track spending per category
    private val categorySpending = mutableMapOf<Long, Double>()
    // Track current budgets
    private var currentBudgets = listOf<com.example.budgettracker.data.local.entities.Budget>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeBudgets()
        setupAddBudgetButton()
    }
    
    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter(emptyList()) { budget ->
            // Handle budget click - navigate to edit view
            openEditBudgetScreen(budget)
        }
        
        binding.rvBudgets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = budgetAdapter
        }
    }
    
    private fun openEditBudgetScreen(budget: BudgetModel) {
        presenter.currentMonthBudgets.value?.find { it.id.toString() == budget.id }?.let { dbBudget ->
            val addBudgetFragment = AddBudgetFragment.newInstanceForEdit(presenter, dbBudget)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addBudgetFragment)
                .addToBackStack(null)
                .commit()
        }
    }
    
    private fun observeBudgets() {
        // Observe budgets for current month
        presenter.currentMonthBudgets.observe(viewLifecycleOwner) { budgets ->
            currentBudgets = budgets
            updateBudgetDisplay()
            
            // Setup category spending observers for each budget
            budgets.forEach { budget ->
                // Observe spending for this category
                presenter.getCategorySpendingForCurrentMonth(budget.categoryId)
                    .observe(viewLifecycleOwner, Observer { spending ->
                        categorySpending[budget.categoryId] = spending ?: 0.0
                        updateBudgetDisplay()
                    })
            }
        }
        
        // Also observe transactions to refresh data when transactions change
        presenter.allTransactions.observe(viewLifecycleOwner) {
            // This will trigger refreshing all category spending
            currentBudgets.forEach { budget ->
                presenter.getCategoryExpenseForCurrentMonth(budget.categoryId)
            }
        }
    }
    
    private fun updateBudgetDisplay() {
        if (currentBudgets.isNotEmpty()) {
            binding.tvEmptyBudgets.visibility = View.GONE
            binding.rvBudgets.visibility = View.VISIBLE
            
            val budgetModels = mutableListOf<BudgetModel>()
            
            // For each budget, get the current spending
            currentBudgets.forEach { budget ->
                val categoryName = categoryMap[budget.categoryId] ?: "Others"
                val currentAmount = categorySpending[budget.categoryId] ?: 0.0
                val percentage = if (budget.amount > 0) (currentAmount / budget.amount) * 100 else 0.0
                
                budgetModels.add(
                    BudgetModel(
                        id = budget.id.toString(),
                        name = categoryName,
                        categoryId = budget.categoryId,
                        categoryName = categoryName,
                        maxAmount = budget.amount,
                        currentAmount = currentAmount,
                        progressPercentage = percentage
                    )
                )
            }
            
            budgetAdapter.updateBudgets(budgetModels)
        } else {
            binding.tvEmptyBudgets.visibility = View.VISIBLE
            binding.rvBudgets.visibility = View.GONE
        }
    }
    
    private fun setupAddBudgetButton() {
        binding.fabAddBudget.setOnClickListener {
            val addBudgetFragment = AddBudgetFragment.newInstance(presenter)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addBudgetFragment)
                .addToBackStack(null)
                .commit()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(presenter: BudgetPresenter): BudgetFragment {
            val fragment = BudgetFragment()
            fragment.presenter = presenter
            return fragment
        }
    }
} 