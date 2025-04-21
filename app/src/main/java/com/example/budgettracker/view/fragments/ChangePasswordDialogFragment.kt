package com.example.budgettracker.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.budgettracker.R
import com.example.budgettracker.data.remote.FirebaseAuthManager
import com.google.android.material.textfield.TextInputEditText

class ChangePasswordDialogFragment : DialogFragment() {
    
    private lateinit var authManager: FirebaseAuthManager
    private var onPasswordChangedListener: (() -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        
        authManager = FirebaseAuthManager()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_change_password, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val etCurrentPassword = view.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = view.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelPassword)
        val btnSave = view.findViewById<Button>(R.id.btnSavePassword)
        
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnSave.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            
            // Validate inputs
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Update password
            authManager.updateUserPassword(currentPassword, newPassword)
            authManager.profileUpdateStatus.observe(viewLifecycleOwner) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                    onPasswordChangedListener?.invoke()
                    dismiss()
                } else {
                    val errorMessage = authManager.authError.value ?: "Failed to update password"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    companion object {
        fun newInstance(onPasswordChanged: () -> Unit): ChangePasswordDialogFragment {
            val fragment = ChangePasswordDialogFragment()
            fragment.onPasswordChangedListener = onPasswordChanged
            return fragment
        }
    }
} 