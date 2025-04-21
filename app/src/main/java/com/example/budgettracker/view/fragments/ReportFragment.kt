package com.example.budgettracker.view.fragments

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.budgettracker.R
import com.example.budgettracker.data.local.entities.Transaction
import com.example.budgettracker.databinding.FragmentReportBinding
import com.example.budgettracker.model.CategoryReportModel
import com.example.budgettracker.presenter.ReportPresenter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

class ReportFragment : Fragment() {
    
    private val TAG = "ReportFragment"
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var presenter: ReportPresenter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Configure the pie chart
        setupPieChart()
        
        // Configure the bar chart
        setupBarChart()
        
        // Set up export button
        binding.btnExportData.setOnClickListener {
            exportData()
        }
        
        // Set up refresh button
        binding.btnRefresh.setOnClickListener {
            refreshData()
        }
        
        // Observe data
        observeData()
    }
    
    private fun refreshData() {
        // Show loading state
        binding.btnRefresh.isEnabled = false
        binding.btnRefresh.text = "Refreshing..."
        
        // Refresh data from Firestore
        presenter.refreshDataFromFirestore()
        
        // Restore button after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            binding.btnRefresh.isEnabled = true
            binding.btnRefresh.text = "Refresh"
        }, 1500)
    }
    
    private fun exportData() {
        // Show loading state
        binding.btnExportData.isEnabled = false
        binding.btnExportData.text = "Exporting..."
        
        // Call presenter to export data
        context?.let { ctx ->
            presenter.exportTransactionsToCSV(ctx)
        }
    }
    
    private fun setupPieChart() {
        binding.pieChartExpenses.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(12f, 16f, 12f, 12f)
            setDrawCenterText(true)
            setCenterTextSize(16f)
            setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            
            // Legend configuration
            legend.isEnabled = true
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.setDrawInside(false)
            legend.textSize = 12f
            legend.xEntrySpace = 10f
            legend.yEntrySpace = 6f
            
            // Hole configuration
            isDrawHoleEnabled = true
            holeRadius = 58f
            transparentCircleRadius = 63f
            setHoleColor(Color.WHITE)

            // Interaction
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            
            // Animation
            animateY(1200, Easing.EaseInOutQuad)
        }
    }
    
    private fun setupBarChart() {
        binding.barChartIncome.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            
            val xAxis = xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            
            animateY(1500)
        }
    }

    private fun createTestTransactionData() {
        // Show loading indicator
        binding.btnExportData.isEnabled = false
        binding.btnExportData.text = "Creating test data..."
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user"
                Log.d(TAG, "Creating test data for user: $currentUserId")
                
                // Create transactions for the last 3 months
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                
                // Log current date information
                Log.d(TAG, "Current date: ${formatDate(calendar.time)}, month: $currentMonth, year: $currentYear")
                
                // Create category IDs from 1 to 7 (excluding paycheck which is 8)
                val expenseCategoryIds = listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L)
                val paycheckCategoryId = 8L
                
                var totalTransactionsCreated = 0
                
                // For each of the last 3 months
                for (monthOffset in 0..2) {
                    val targetMonth = (currentMonth - monthOffset + 12) % 12
                    val targetYear = currentYear - if (targetMonth > currentMonth) 1 else 0
                    
                    // Set calendar to the target month
                    calendar.set(Calendar.YEAR, targetYear)
                    calendar.set(Calendar.MONTH, targetMonth)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    
                    // Create 1-2 income transactions (paychecks) for the month
                    val incomeCount = (1..2).random()
                    for (i in 1..incomeCount) {
                        val incomeDay = (5..25).random() // Random day of month (avoiding edge days)
                        calendar.set(Calendar.DAY_OF_MONTH, incomeDay)
                        
                        val incomeTransaction = Transaction(
                            amount = (1800..3500).random().toDouble(),
                            description = "Paycheck ${if (i > 1) "#$i" else ""}",
                            date = calendar.time,
                            categoryId = paycheckCategoryId,
                            isIncome = true,
                            userId = currentUserId
                        )
                        
                        presenter.repository.insertTransaction(incomeTransaction)
                        totalTransactionsCreated++
                        Log.d(TAG, "Created income transaction: $incomeTransaction")
                    }
                    
                    // Create more expense transactions for the current month, fewer for older months
                    val expenseCount = when (monthOffset) {
                        0 -> (8..12).random()    // Current month: 8-12 transactions
                        1 -> (5..8).random()     // Last month: 5-8 transactions
                        else -> (3..6).random()  // Two months ago: 3-6 transactions
                    }
                    
                    for (i in 1..expenseCount) {
                        val expenseDay = (1..28).random() // Random day of month
                        calendar.set(Calendar.DAY_OF_MONTH, expenseDay)
                        
                        val categoryId = expenseCategoryIds.random()
                        val expenseAmount = when (categoryId) {
                            1L -> (40..150).random().toDouble()   // Food
                            2L -> (30..200).random().toDouble()   // Transportation
                            3L -> (800..1800).random().toDouble() // Housing
                            4L -> (50..300).random().toDouble()   // Entertainment
                            5L -> (80..350).random().toDouble()   // Utilities
                            6L -> (50..500).random().toDouble()   // Healthcare
                            else -> (30..200).random().toDouble() // Others
                        }
                        
                        val descriptions = mapOf(
                            1L to listOf("Groceries", "Restaurant", "Coffee", "Lunch", "Dinner"),
                            2L to listOf("Gas", "Uber", "Bus ticket", "Train", "Car maintenance"),
                            3L to listOf("Rent", "Mortgage", "Home repairs", "Furniture"),
                            4L to listOf("Movies", "Concert", "Games", "Streaming service"),
                            5L to listOf("Electricity", "Water", "Internet", "Phone bill"),
                            6L to listOf("Doctor visit", "Medication", "Insurance", "Gym"),
                            7L to listOf("Clothing", "Gifts", "Books", "Miscellaneous")
                        )
                        
                        val description = descriptions[categoryId]?.random() ?: "Expense"
                        
                        val expenseTransaction = Transaction(
                            amount = expenseAmount,
                            description = description,
                            date = calendar.time,
                            categoryId = categoryId,
                            isIncome = false,
                            userId = currentUserId
                        )
                        
                        presenter.repository.insertTransaction(expenseTransaction)
                        totalTransactionsCreated++
                        Log.d(TAG, "Created expense transaction: $expenseTransaction")
                    }
                }
                
                // Notify the user that test data was created
                launch(Dispatchers.Main) {
                    binding.btnExportData.isEnabled = true
                    binding.btnExportData.text = "Export Data to CSV"
                    
                    Toast.makeText(
                        context,
                        "Created $totalTransactionsCreated test transactions for the last 3 months",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Refresh the data
                    presenter.setCurrentMonthDateRange()
                    presenter.generateMonthlyIncomeData()
                    presenter.generateMonthlySpendingTrend()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error creating test data: ${e.message}", e)
                
                launch(Dispatchers.Main) {
                    binding.btnExportData.isEnabled = true
                    binding.btnExportData.text = "Export Data to CSV"
                    
                    Toast.makeText(
                        context,
                        "Error creating test data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun observeData() {
        // Observe category expense report data for pie chart
        presenter.categoryExpenseReport.observe(viewLifecycleOwner) { categories ->
            Log.d(TAG, "Received category expense report data: ${categories.size} items")
            if (categories.isEmpty()) {
                // Force reload with dummy data if we have empty categories
                presenter.generateDummyDataForReports()
            } else {
                updatePieChart(categories)
            }
        }
        
        // Observe monthly income data for bar chart
        presenter.monthlyIncome.observe(viewLifecycleOwner) { monthlyIncome ->
            Log.d(TAG, "Received monthly income data: ${monthlyIncome.size} months")
            updateBarChart(monthlyIncome)
        }

        // Observe export result as a one-time event
        presenter.exportResult.observe(viewLifecycleOwner) { event ->
            // Only process the export if it hasn't been handled yet
            event.getContentIfNotHandled()?.let { result ->
                // Reset button state
                binding.btnExportData.isEnabled = true
                binding.btnExportData.text = "Export Data to CSV"
                
                result.fold(
                    onSuccess = { file ->
                        shareExportedFile(file)
                        Toast.makeText(context, "Export successful!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            context,
                            "Export failed: ${error.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }

    private fun shareExportedFile(file: File) {
        context?.let { ctx ->
            val uri = FileProvider.getUriForFile(
                ctx,
                "${ctx.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(intent, "Share CSV Export"))
        }
    }
    
    private fun updatePieChart(categories: List<CategoryReportModel>) {
        // Filter out only the Paycheck category, but include others even if they have zero amount
        val filteredCategories = categories
            .filter { it.categoryName != "Paycheck" }
            .sortedWith(compareByDescending<CategoryReportModel> { 
                // If the category is "Others", give it lowest priority 
                if (it.categoryName == "Others") -1.0 else it.amount 
            })

        // Calculate total amount for percentage calculation
        val totalAmount = filteredCategories.sumOf { it.amount }
        
        // Only proceed if we have data
        if (filteredCategories.isNotEmpty()) {
            val entries = ArrayList<PieEntry>()
            val colors = ArrayList<Int>()
            
            // Modern color palette
            val colorPalette = listOf(
                Color.rgb(66, 133, 244),   // Google Blue
                Color.rgb(219, 68, 55),    // Google Red
                Color.rgb(244, 180, 0),    // Google Yellow
                Color.rgb(15, 157, 88),    // Google Green
                Color.rgb(171, 71, 188),   // Purple
                Color.rgb(0, 172, 193),    // Cyan
                Color.rgb(255, 112, 67),   // Deep Orange
                Color.rgb(158, 157, 36),   // Lime
                Color.rgb(121, 85, 72),    // Brown
                Color.rgb(84, 110, 122)    // Blue Grey
            )

            // Add entries for all categories, even those with zero amounts
            filteredCategories.forEachIndexed { index, category ->
                val categoryName = category.categoryName
                val amount = category.amount
                
                // For zero amount categories, use a minimal value to make them visible on the chart
                val chartAmount = if (amount > 0) amount else totalAmount * 0.01
                
                val percentageText = if (amount > 0) {
                    String.format("%.1f%%", (amount / totalAmount) * 100)
                } else {
                    "0.0%"
                }
                
                // Keep category name for the legend
                entries.add(PieEntry(chartAmount.toFloat(), categoryName))
                
                // Use either the predefined color from the model or from our palette
                val color = if (category.color != Color.GRAY) category.color else colorPalette[index % colorPalette.size]
                colors.add(color)
            }

            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors
                sliceSpace = 3.5f
                selectionShift = 7f
                yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
                xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
                valueFormatter = PercentFormatter(binding.pieChartExpenses)
            }

            val data = PieData(dataSet).apply {
                setValueTextSize(12f)
                setValueTextColor(Color.WHITE)
                setValueFormatter(PercentFormatter(binding.pieChartExpenses))
            }

            binding.pieChartExpenses.apply {
                this.data = data
                highlightValues(null)
                setCenterText("Total\n$${formatCurrency(totalAmount)}")
                setDrawEntryLabels(false)
                invalidate()
            }
        } else {
            binding.pieChartExpenses.apply {
                setNoDataText("No expense data available")
                setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                invalidate()
            }
        }
    }
    
    private fun updateBarChart(monthlyIncome: List<Pair<String, Double>>) {
        if (monthlyIncome.isEmpty()) {
            Log.d(TAG, "No income data for bar chart")
            
            // Create sample data for demo purposes
            val sampleLabels = listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr")
            val sampleData = listOf(
                BarEntry(0f, 1200f),
                BarEntry(1f, 900f),
                BarEntry(2f, 1500f),
                BarEntry(3f, 1300f),
                BarEntry(4f, 1750f),
                BarEntry(5f, 1400f)
            )
            
            val dataSet = BarDataSet(sampleData, "Monthly Income")
            dataSet.apply {
                color = ContextCompat.getColor(requireContext(), R.color.green_80)
                valueTextColor = Color.BLACK
                valueTextSize = 10f
            }
            
            val data = BarData(dataSet)
            data.barWidth = 0.6f
            
            binding.barChartIncome.apply {
                this.data = data
                xAxis.valueFormatter = IndexAxisValueFormatter(sampleLabels)
                setFitBars(true)
                invalidate()
            }
            
            return
        }
        
        Log.d(TAG, "Updating bar chart with ${monthlyIncome.size} months")
        
        val entries = monthlyIncome.mapIndexed { index, (_, amount) ->
            BarEntry(index.toFloat(), amount.toFloat())
        }
        
        val dataSet = BarDataSet(entries, "Monthly Income")
        dataSet.apply {
            color = ContextCompat.getColor(requireContext(), R.color.green_80)
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }
        
        val labels = monthlyIncome.map { it.first }
        
        val data = BarData(dataSet)
        data.barWidth = 0.6f
        
        binding.barChartIncome.apply {
            this.data = data
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            setFitBars(true)
            invalidate()
        }
    }
    
    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(date)
    }
    
    private fun formatCurrency(amount: Double): String {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        return currencyFormat.format(amount)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(presenter: ReportPresenter): ReportFragment {
            val fragment = ReportFragment()
            fragment.presenter = presenter
            return fragment
        }
    }
} 