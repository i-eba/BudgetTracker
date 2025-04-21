package com.example.budgettracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.budgettracker.data.BudgetRepository
import com.example.budgettracker.data.local.AppDatabase
import com.example.budgettracker.data.remote.FirebaseAuthManager
import com.example.budgettracker.data.remote.FirestoreManager
import com.example.budgettracker.databinding.ActivityMainBinding
import com.example.budgettracker.presenter.AuthPresenter
import com.example.budgettracker.presenter.BudgetPresenter
import com.example.budgettracker.presenter.ReportPresenter
import com.example.budgettracker.presenter.TransactionPresenter
import com.example.budgettracker.util.CSVExporter
import com.example.budgettracker.util.TestDataGenerator
import com.example.budgettracker.view.AuthActivity
import com.example.budgettracker.view.fragments.BudgetFragment
import com.example.budgettracker.view.fragments.ProfileFragment
import com.example.budgettracker.view.fragments.ReportFragment
import com.example.budgettracker.view.fragments.TransactionFragment

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var repository: BudgetRepository
    private lateinit var authPresenter: AuthPresenter
    private lateinit var transactionPresenter: TransactionPresenter
    private lateinit var budgetPresenter: BudgetPresenter
    private lateinit var reportPresenter: ReportPresenter
    private lateinit var testDataGenerator: TestDataGenerator
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize dependencies
        authManager = FirebaseAuthManager()
        val firestoreManager = FirestoreManager()
        val database = AppDatabase.getDatabase(this)
        
        // Initialize repository
        repository = BudgetRepository(database, firestoreManager, authManager)
        
        // Initialize test data generator
        testDataGenerator = TestDataGenerator(repository)
        
        // Ensure default categories are created
        repository.ensureDefaultCategories()
        
        // Initialize presenters
        authPresenter = AuthPresenter(authManager, repository)
        transactionPresenter = TransactionPresenter(repository, authManager)
        budgetPresenter = BudgetPresenter(repository, authManager)
        reportPresenter = ReportPresenter(repository, CSVExporter())
        
        // Check if user is logged in
        if (authManager.getCurrentUserId() == null) {
            // Redirect to auth activity
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }
        
        // Sync data from Firestore
        syncDataFromFirestore()
        
        // Generate test data for missing categories
        authManager.getCurrentUserId()?.let { userId ->
            testDataGenerator.addTestTransactionsForMissingCategories(this, userId)
        }
        
        setupBottomNavigation()
        
        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container, TransactionFragment.newInstance(transactionPresenter))
            }
        }
    }
    
    private fun syncDataFromFirestore() {
        // Show syncing indicator if needed
        
        // Sync data from Firestore to local database
        repository.syncData()
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_transactions -> TransactionFragment.newInstance(transactionPresenter)
                R.id.nav_budget -> BudgetFragment.newInstance(budgetPresenter)
                R.id.nav_reports -> ReportFragment.newInstance(reportPresenter)
                R.id.nav_profile -> ProfileFragment.newInstance { signOut() }
                else -> TransactionFragment.newInstance(transactionPresenter)
            }
            
            supportFragmentManager.commit {
                replace(R.id.fragment_container, fragment)
            }
            
            true
        }
    }
    
    private fun signOut() {
        authPresenter.signOut()
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}