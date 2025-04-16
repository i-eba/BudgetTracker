package com.example.budgettracker.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.budgettracker.R
import com.example.budgettracker.presenter.BudgetPresenter

class BudgetFragment : Fragment() {
    
    private lateinit var presenter: BudgetPresenter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up UI and listeners
    }
    
    companion object {
        fun newInstance(presenter: BudgetPresenter): BudgetFragment {
            val fragment = BudgetFragment()
            fragment.presenter = presenter
            return fragment
        }
    }
} 