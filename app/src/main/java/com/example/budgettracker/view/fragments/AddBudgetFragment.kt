package com.example.budgettracker.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.budgettracker.R
import com.example.budgettracker.databinding.FragmentAddBudgetBinding
import com.example.budgettracker.data.local.entities.Budget
import com.example.budgettracker.presenter.BudgetPresenter

class AddBudgetFragment : Fragment() {

    private var _binding: FragmentAddBudgetBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var presenter: BudgetPresenter
    
    // For edit mode
    private var isEditMode = false
    private var budgetToEdit: Budget? = null
    
    // Category map with IDs
    private val categories = mapOf(
        "Food" to 1L,
        "Transportation" to 2L,
        "Housing" to 3L,
        "Entertainment" to 4L,
        "Utilities" to 5L,
        "Healthcare" to 6L,
        "Others" to 7L
    )
    
    // Reverse map to get category name from ID
    private val categoryIdToName = mapOf(
        1L to "Food",
        2L to "Transportation",
        3L to "Housing",
        4L to "Entertainment",
        5L to "Utilities", 
        6L to "Healthcare",
        7L to "Others",
        8L to "Paycheck"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCategoryDropdown()
        setupSaveButton()
        setupCancelButton()
        setupDeleteButton()
        
        // If in edit mode, populate the fields with existing budget data
        if (isEditMode && budgetToEdit != null) {
            setupEditMode()
        }
    }
    
    private fun setupDeleteButton() {
        // Only show delete button in edit mode
        if (isEditMode && budgetToEdit != null) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                showDeleteConfirmationDialog()
            }
        } else {
            binding.btnDelete.visibility = View.GONE
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Budget")
            .setMessage("Are you sure you want to delete this budget? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteBudget()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteBudget() {
        budgetToEdit?.let { budget ->
            presenter.deleteBudget(budget)
            Toast.makeText(requireContext(), "Budget deleted", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    
    private fun setupEditMode() {
        budgetToEdit?.let { budget ->
            // Update title
            binding.tvTitle.text = "Edit Budget"
            
            // Set name
            val categoryName = categoryIdToName[budget.categoryId] ?: "Others"
            binding.etBudgetName.setText(categoryName)
            
            // Set max amount
            binding.etMaxAmount.setText(budget.amount.toString())
            
            // Set category
            binding.actvCategory.setText(categoryName, false)
        }
    }
    
    private fun setupCategoryDropdown() {
        val categoryNames = categories.keys.toList()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categoryNames
        )
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setText(categoryNames.first(), false)
    }
    
    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveBudget()
            }
        }
    }
    
    private fun setupCancelButton() {
        binding.btnCancel.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    
    private fun validateInputs(): Boolean {
        val budgetName = binding.etBudgetName.text.toString()
        val maxAmount = binding.etMaxAmount.text.toString()
        val category = binding.actvCategory.text.toString()
        
        if (budgetName.isEmpty()) {
            binding.etBudgetName.error = "Please enter a budget name"
            return false
        }
        
        if (maxAmount.isEmpty()) {
            binding.etMaxAmount.error = "Please enter a maximum amount"
            return false
        }
        
        if (category.isEmpty() || !categories.containsKey(category)) {
            binding.actvCategory.error = "Please select a valid category"
            return false
        }
        
        return true
    }
    
    private fun saveBudget() {
        val budgetName = binding.etBudgetName.text.toString()
        val maxAmount = binding.etMaxAmount.text.toString().toDouble()
        val category = binding.actvCategory.text.toString()
        
        val categoryId = categories[category] ?: 7L // Default to "Others" if not found
        
        if (isEditMode && budgetToEdit != null) {
            // Update existing budget
            val updatedBudget = budgetToEdit!!.copy(
                amount = maxAmount,
                categoryId = categoryId
            )
            
            presenter.updateBudget(updatedBudget)
            Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show()
        } else {
            // Create new budget
            presenter.addBudget(
                amount = maxAmount,
                categoryId = categoryId
            )
            
            Toast.makeText(requireContext(), "Budget saved", Toast.LENGTH_SHORT).show()
        }
        
        requireActivity().supportFragmentManager.popBackStack()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(presenter: BudgetPresenter): AddBudgetFragment {
            val fragment = AddBudgetFragment()
            fragment.presenter = presenter
            fragment.isEditMode = false
            fragment.budgetToEdit = null
            return fragment
        }
        
        fun newInstanceForEdit(
            presenter: BudgetPresenter,
            budget: Budget
        ): AddBudgetFragment {
            val fragment = AddBudgetFragment()
            fragment.presenter = presenter
            fragment.isEditMode = true
            fragment.budgetToEdit = budget
            return fragment
        }
    }
} 