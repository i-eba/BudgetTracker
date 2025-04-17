package com.example.budgettracker.view.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.budgettracker.R
import com.example.budgettracker.databinding.FragmentAddTransactionBinding
import com.example.budgettracker.presenter.TransactionPresenter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var presenter: TransactionPresenter
    private val calendar = Calendar.getInstance()
    private var selectedDate = Date()
    
    // For edit mode
    private var isEditMode = false
    private var transactionToEdit: com.example.budgettracker.data.local.entities.Transaction? = null
    
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
        7L to "Others"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Find the text view displaying to the right of the toggle
        val statusText = view.findViewById<TextView>(R.id.tvIncome)
        // Or adjust the text view to the right of the toggle
        statusText?.text = "Income"
        
        setupCategoryDropdown()
        setupDatePicker()
        setupSaveButton()
        setupCancelButton()
        setupDeleteButton()
        
        // If in edit mode, populate the fields with existing transaction data
        if (isEditMode && transactionToEdit != null) {
            setupEditMode()
        }
    }
    
    private fun setupDeleteButton() {
        // Only show delete button in edit mode
        if (isEditMode && transactionToEdit != null) {
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
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteTransaction()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteTransaction() {
        transactionToEdit?.let { transaction ->
            presenter.deleteTransaction(transaction)
            Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    
    private fun setupEditMode() {
        transactionToEdit?.let { transaction ->
            // Update title
            binding.tvTitle.text = "Edit Transaction"
            
            // Set amount
            binding.etAmount.setText(transaction.amount.toString())
            
            // Set description
            binding.etDescription.setText(transaction.description)
            
            // Set category
            val categoryName = categoryIdToName[transaction.categoryId] ?: "Others"
            binding.actvCategory.setText(categoryName, false)
            
            // Set date
            selectedDate = transaction.date
            updateDateInView()
            
            // Set income/expense switch
            binding.switchType.isChecked = transaction.isIncome
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
    
    private fun setupDatePicker() {
        // Set initial date
        updateDateInView()
        
        // Set date picker listener
        binding.etDate.setOnClickListener {
            showDatePickerDialog()
        }
    }
    
    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDate = calendar.time
                updateDateInView()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    
    private fun updateDateInView() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(selectedDate))
    }
    
    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }
    }
    
    private fun setupCancelButton() {
        binding.btnCancel.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    
    private fun validateInputs(): Boolean {
        val amount = binding.etAmount.text.toString()
        val description = binding.etDescription.text.toString()
        val category = binding.actvCategory.text.toString()
        
        if (amount.isEmpty()) {
            binding.etAmount.error = "Please enter an amount"
            return false
        }
        
        if (description.isEmpty()) {
            binding.etDescription.error = "Please enter a description"
            return false
        }
        
        if (category.isEmpty() || !categories.containsKey(category)) {
            binding.actvCategory.error = "Please select a valid category"
            return false
        }
        
        return true
    }
    
    private fun saveTransaction() {
        val amount = binding.etAmount.text.toString().toDouble()
        val description = binding.etDescription.text.toString()
        val category = binding.actvCategory.text.toString()
        val isIncome = binding.switchType.isChecked
        
        val categoryId = categories[category] ?: 7L // Default to "Others" if not found
        
        if (isEditMode && transactionToEdit != null) {
            // Update existing transaction
            val updatedTransaction = transactionToEdit!!.copy(
                amount = amount,
                description = description,
                date = selectedDate,
                categoryId = categoryId,
                isIncome = isIncome
            )
            
            presenter.updateTransaction(updatedTransaction)
            Toast.makeText(requireContext(), "Transaction updated", Toast.LENGTH_SHORT).show()
        } else {
            // Create new transaction
            presenter.addTransaction(
                amount = amount,
                description = description,
                date = selectedDate,
                categoryId = categoryId,
                isIncome = isIncome
            )
            
            Toast.makeText(requireContext(), "Transaction saved", Toast.LENGTH_SHORT).show()
        }
        
        requireActivity().supportFragmentManager.popBackStack()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(presenter: TransactionPresenter): AddTransactionFragment {
            val fragment = AddTransactionFragment()
            fragment.presenter = presenter
            fragment.isEditMode = false
            fragment.transactionToEdit = null
            return fragment
        }
        
        fun newInstanceForEdit(
            presenter: TransactionPresenter,
            transaction: com.example.budgettracker.data.local.entities.Transaction
        ): AddTransactionFragment {
            val fragment = AddTransactionFragment()
            fragment.presenter = presenter
            fragment.isEditMode = true
            fragment.transactionToEdit = transaction
            return fragment
        }
    }
} 