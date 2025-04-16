package com.example.budgettracker.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.budgettracker.R

class ProfileFragment : Fragment() {
    
    private lateinit var onSignOut: () -> Unit
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up sign out button
        view.findViewById<View>(R.id.btnSignOut)?.setOnClickListener {
            onSignOut.invoke()
        }
    }
    
    companion object {
        fun newInstance(onSignOut: () -> Unit): ProfileFragment {
            val fragment = ProfileFragment()
            fragment.onSignOut = onSignOut
            return fragment
        }
    }
} 