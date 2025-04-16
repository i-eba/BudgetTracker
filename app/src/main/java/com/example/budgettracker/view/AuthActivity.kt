package com.example.budgettracker.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.budgettracker.MainActivity
import com.example.budgettracker.data.BudgetRepository
import com.example.budgettracker.data.local.AppDatabase
import com.example.budgettracker.data.remote.FirebaseAuthManager
import com.example.budgettracker.data.remote.FirestoreManager
import com.example.budgettracker.databinding.ActivityAuthBinding
import com.example.budgettracker.presenter.AuthPresenter
import com.example.budgettracker.view.fragments.SignInFragment
import com.example.budgettracker.view.fragments.SignUpFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AuthActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAuthBinding
    private lateinit var presenter: AuthPresenter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize presenter
        val authManager = FirebaseAuthManager()
        val firestoreManager = FirestoreManager()
        val database = AppDatabase.getDatabase(this)
        val repository = BudgetRepository(database, firestoreManager, authManager)
        presenter = AuthPresenter(authManager, repository)
        
        setupViewPager()
        observeAuthState()
    }
    
    private fun setupViewPager() {
        viewPager = binding.viewPager
        tabLayout = binding.tabLayout
        
        val pagerAdapter = AuthPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Sign in" else "Sign up"
        }.attach()
    }
    
    private fun observeAuthState() {
        presenter.currentUser.observe(this, Observer { user ->
            if (user != null) {
                // User is logged in
                presenter.syncDataAfterLogin()
                
                // Navigate to main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        
        presenter.authError.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        })
    }
    
    fun getPresenter(): AuthPresenter = presenter
    
    private inner class AuthPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2
        
        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                SignInFragment()
            } else {
                SignUpFragment()
            }
        }
    }
} 