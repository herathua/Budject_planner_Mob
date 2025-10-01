package com.example.budject_planner.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budject_planner.domain.model.Transaction
import com.example.budject_planner.domain.model.TransactionType
import com.example.budject_planner.domain.model.TransactionSource
import com.example.budject_planner.presentation.components.*
import com.example.budject_planner.presentation.viewmodel.BudgetViewModel
import com.example.budject_planner.util.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycleOwner.lifecycle)
    val smsMessages by viewModel.smsMessages.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycleOwner.lifecycle)
    val budgetSummary by viewModel.budgetSummary.collectAsStateWithLifecycle(
        initialValue = com.example.budject_planner.domain.model.BudgetSummary(0.0, 0.0, 0.0, 0, 0),
        lifecycle = lifecycleOwner.lifecycle
    )
    val chartData by viewModel.chartData.collectAsStateWithLifecycle(
        initialValue = com.example.budject_planner.domain.model.ChartData(emptyList(), emptyList(), emptyList()),
        lifecycle = lifecycleOwner.lifecycle
    )
    
    // Form states
    var newIncome by remember { mutableStateOf("") }
    var newExpense by remember { mutableStateOf("") }
    var incomeNote by remember { mutableStateOf("") }
    var expenseNote by remember { mutableStateOf("") }
    
    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Error is already handled in ViewModel
            viewModel.clearError()
        }
    }
    
    // Show success message
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // Message is already shown via Toast in ViewModel
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Planner") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Balance Card
            item {
                BalanceCard(budgetSummary = budgetSummary)
            }
            
            // Charts Section
            item {
                BudgetChart(chartData = chartData)
            }
            
            item {
                PieChart(
                    income = budgetSummary.totalIncome,
                    expense = budgetSummary.totalExpense
                )
            }
            
            // SMS Messages
            item {
                SmsMessagesSection(
                    smsMessages = smsMessages,
                    onRefreshSms = { viewModel.refreshSmsMessages() }
                )
            }
            
            // Add Income Form
            item {
                TransactionForm(
                    title = "Add Income",
                    amount = newIncome,
                    onAmountChange = { newIncome = it },
                    note = incomeNote,
                    onNoteChange = { incomeNote = it },
                    onAddClick = {
                        val amountResult = ValidationUtils.validateAmount(newIncome)
                        val noteResult = ValidationUtils.validateNote(
                            incomeNote.ifBlank { "Manual Income" }
                        )
                        
                        when {
                            amountResult.isFailure -> {
                                // Error will be shown via validation
                            }
                            noteResult.isFailure -> {
                                // Error will be shown via validation
                            }
                            else -> {
                                val transaction = Transaction(
                                    type = TransactionType.INCOME,
                                    amount = amountResult.getOrThrow(),
                                    note = noteResult.getOrThrow(),
                                    source = TransactionSource.MANUAL
                                )
                                viewModel.addTransaction(transaction)
                                newIncome = ""
                                incomeNote = ""
                            }
                        }
                    }
                )
            }
            
            // Add Expense Form
            item {
                TransactionForm(
                    title = "Add Expense",
                    amount = newExpense,
                    onAmountChange = { newExpense = it },
                    note = expenseNote,
                    onNoteChange = { expenseNote = it },
                    onAddClick = {
                        val amountResult = ValidationUtils.validateAmount(newExpense)
                        val noteResult = ValidationUtils.validateNote(
                            expenseNote.ifBlank { "Manual Expense" }
                        )
                        
                        when {
                            amountResult.isFailure -> {
                                // Error will be shown via validation
                            }
                            noteResult.isFailure -> {
                                // Error will be shown via validation
                            }
                            else -> {
                                val transaction = Transaction(
                                    type = TransactionType.EXPENSE,
                                    amount = amountResult.getOrThrow(),
                                    note = noteResult.getOrThrow(),
                                    source = TransactionSource.MANUAL
                                )
                                viewModel.addTransaction(transaction)
                                newExpense = ""
                                expenseNote = ""
                            }
                        }
                    },
                    isExpense = true
                )
            }
            
            // Transaction History
            item {
                TransactionList(
                    transactions = transactions,
                    onDeleteTransaction = { id -> viewModel.deleteTransaction(id) }
                )
            }
        }
    }
    
    // Loading indicator
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

