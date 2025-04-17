package com.example.budgettracker.view.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
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
        
        setupCategoryDropdown()
        setupDatePicker()
        setupSaveButton()
        setupCancelButton()
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
        
        presenter.addTransaction(
            amount = amount,
            description = description,
            date = selectedDate,
            categoryId = categoryId,
            isIncome = isIncome
        )
        
        Toast.makeText(requireContext(), "Transaction saved", Toast.LENGTH_SHORT).show()
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
            return fragment
        }
    }
} 