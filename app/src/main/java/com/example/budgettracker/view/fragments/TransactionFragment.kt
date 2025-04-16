package com.example.budgettracker.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgettracker.R
import com.example.budgettracker.model.Transaction
import com.example.budgettracker.databinding.FragmentTransactionsBinding
import com.example.budgettracker.presenter.TransactionPresenter
import com.example.budgettracker.view.adapters.TransactionAdapter
import java.util.Date

class TransactionFragment : Fragment() {
    
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var presenter: TransactionPresenter
    private lateinit var transactionAdapter: TransactionAdapter
    
    // Map of category IDs to category names
    private val categoryMap = mapOf(
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
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeTransactions()
        setupAddTransactionButton()
    }
    
    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(emptyList()) { transaction ->
            // Handle transaction click - could navigate to details/edit view
        }
        
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }
    
    private fun observeTransactions() {
        presenter.allTransactions.observe(viewLifecycleOwner) { dbTransactions ->
            if (dbTransactions.isNotEmpty()) {
                binding.tvEmptyTransactions.visibility = View.GONE
                binding.rvTransactions.visibility = View.VISIBLE
                
                // Map database entities to model objects
                val modelTransactions = dbTransactions.map { dbTransaction ->
                    // Get the category name from the categoryId or use the default
                    val categoryName = categoryMap[dbTransaction.categoryId] ?: "Other"
                    
                    Transaction(
                        id = dbTransaction.id.toString(),
                        name = dbTransaction.description,
                        amount = dbTransaction.amount,
                        category = categoryName,
                        date = dbTransaction.date,
                        isExpense = !dbTransaction.isIncome
                    )
                }
                
                transactionAdapter.updateTransactions(modelTransactions)
            } else {
                binding.tvEmptyTransactions.visibility = View.VISIBLE
                binding.rvTransactions.visibility = View.GONE
            }
        }
    }
    
    private fun setupAddTransactionButton() {
        binding.fabAddTransaction.setOnClickListener {
            val addTransactionFragment = AddTransactionFragment.newInstance(presenter)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addTransactionFragment)
                .addToBackStack(null)
                .commit()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(presenter: TransactionPresenter): TransactionFragment {
            val fragment = TransactionFragment()
            fragment.presenter = presenter
            return fragment
        }
    }
} 