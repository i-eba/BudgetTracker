package com.example.budgettracker.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.budgettracker.R
import com.example.budgettracker.data.remote.FirebaseAuthManager
import com.example.budgettracker.presenter.ProfilePresenter
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {
    
    private lateinit var presenter: ProfilePresenter
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var onSignOut: () -> Unit
    
    private lateinit var avatarImageView: CircleImageView
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var changePasswordButton: TextView
    private lateinit var saveButton: Button
    private lateinit var signOutButton: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Firebase Auth Manager
        authManager = FirebaseAuthManager()
        
        // Initialize Presenter
        presenter = ProfilePresenter(authManager)
        
        // Initialize views
        avatarImageView = view.findViewById(R.id.ivProfileAvatar)
        nameEditText = view.findViewById(R.id.etName)
        phoneEditText = view.findViewById(R.id.etPhone)
        emailEditText = view.findViewById(R.id.etEmail)
        changePasswordButton = view.findViewById(R.id.tvChangePassword)
        saveButton = view.findViewById(R.id.btnSaveChanges)
        signOutButton = view.findViewById(R.id.btnSignOut)
        
        // Set the static avatar image
        avatarImageView.setImageResource(R.drawable.default_avatar)
        
        // Set up observers
        observeUser()
        
        // Set up click listeners
        setupClickListeners()
        
        // Load user profile
        presenter.loadUserProfile()
    }
    
    private fun observeUser() {
        presenter.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                nameEditText.setText(it.name)
                phoneEditText.setText(it.phone)
                emailEditText.setText(it.email)
            } ?: run {
                // If user is null, use current Firebase user info
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                emailEditText.setText(firebaseUser?.email ?: "")
                nameEditText.setText(firebaseUser?.displayName ?: "")
            }
        }
        
        presenter.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator if needed
        }
        
        authManager.profileUpdateStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                presenter.loadUserProfile()
            } else {
                val error = authManager.authError.value ?: "Failed to update profile"
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        // Change Password
        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }
        
        // Save Changes
        saveButton.setOnClickListener {
            updateProfile()
        }
        
        // Sign Out
        signOutButton.setOnClickListener {
            presenter.signOut()
            onSignOut.invoke()
        }
    }
    
    private fun showChangePasswordDialog() {
        val dialog = ChangePasswordDialogFragment.newInstance {
            // Password changed successfully
            Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
        }
        
        dialog.show(childFragmentManager, "ChangePasswordDialog")
    }
    
    private fun updateProfile() {
        val name = nameEditText.text.toString()
        val phone = phoneEditText.text.toString()
        
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        presenter.updateProfile(name, phone, "")
    }
    
    companion object {
        fun newInstance(onSignOut: () -> Unit): ProfileFragment {
            val fragment = ProfileFragment()
            fragment.onSignOut = onSignOut
            return fragment
        }
    }
} 