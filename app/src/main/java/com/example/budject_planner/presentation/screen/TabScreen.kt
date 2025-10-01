package com.example.budject_planner.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budject_planner.domain.model.Transaction
import com.example.budject_planner.domain.model.TransactionType
import com.example.budject_planner.domain.model.TransactionSource
import com.example.budject_planner.presentation.components.*
import com.example.budject_planner.presentation.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabScreen(
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

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem("", Icons.Default.Home),
        TabItem("", Icons.Default.KeyboardArrowDown),
        TabItem("", Icons.Default.KeyboardArrowUp),
        TabItem("", Icons.Default.List),
        TabItem("", Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(80.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                tab.icon, 
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
            0 -> DashboardTab(
                budgetSummary = budgetSummary,
                chartData = chartData,
                transactions = transactions.take(5), // Show recent 5 transactions
                onAddTransaction = { type, amount, note ->
                    val transaction = Transaction(
                        type = type,
                        amount = amount,
                        note = note,
                        date = System.currentTimeMillis(),
                        source = TransactionSource.MANUAL
                    )
                    viewModel.addTransaction(transaction)
                }
            )
            1 -> ExpenseTab(
                transactions = transactions.filter { it.type == TransactionType.EXPENSE },
                onAddTransaction = { amount, note ->
                    val transaction = Transaction(
                        type = TransactionType.EXPENSE,
                        amount = amount,
                        note = note,
                        date = System.currentTimeMillis(),
                        source = TransactionSource.MANUAL
                    )
                    viewModel.addTransaction(transaction)
                },
                onDeleteTransaction = { id ->
                    viewModel.deleteTransaction(id)
                }
            )
            2 -> IncomeTab(
                transactions = transactions.filter { it.type == TransactionType.INCOME },
                onAddTransaction = { amount, note ->
                    val transaction = Transaction(
                        type = TransactionType.INCOME,
                        amount = amount,
                        note = note,
                        date = System.currentTimeMillis(),
                        source = TransactionSource.MANUAL
                    )
                    viewModel.addTransaction(transaction)
                },
                onDeleteTransaction = { id ->
                    viewModel.deleteTransaction(id)
                }
            )
            3 -> TransactionHistoryTab(
                transactions = transactions,
                onDeleteTransaction = { id ->
                    viewModel.deleteTransaction(id)
                }
            )
            4 -> SettingsTab()
            }
        }
    }
}

data class TabItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun DashboardTab(
    budgetSummary: com.example.budject_planner.domain.model.BudgetSummary,
    chartData: com.example.budject_planner.domain.model.ChartData,
    transactions: List<Transaction>,
    onAddTransaction: (TransactionType, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Budget Summary Cards
        BudgetSummaryCards(budgetSummary = budgetSummary)
        
        // Chart
        BudgetChart(chartData = chartData)
        
        // Recent Transactions
        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleMedium
        )
        
        if (transactions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No transactions yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                transactions.forEach { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { /* Don't allow delete from dashboard */ }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseTab(
    transactions: List<Transaction>,
    onAddTransaction: (Double, String) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Add Expense Form
        AddTransactionForm(
            transactionType = TransactionType.EXPENSE,
            onAddTransaction = onAddTransaction
        )
        
        // Expense List
        Text(
            text = "Expense History",
            style = MaterialTheme.typography.titleMedium
        )
        
        if (transactions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No expenses yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                transactions.sortedByDescending { it.date }.forEach { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = onDeleteTransaction
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeTab(
    transactions: List<Transaction>,
    onAddTransaction: (Double, String) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Add Income Form
        AddTransactionForm(
            transactionType = TransactionType.INCOME,
            onAddTransaction = onAddTransaction
        )
        
        // Income List
        Text(
            text = "Income History",
            style = MaterialTheme.typography.titleMedium
        )
        
        if (transactions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No income yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                transactions.sortedByDescending { it.date }.forEach { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = onDeleteTransaction
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionHistoryTab(
    transactions: List<Transaction>,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "All Transactions",
            style = MaterialTheme.typography.titleMedium
        )
        
        if (transactions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No transactions yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                transactions.sortedByDescending { it.date }.forEach { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = onDeleteTransaction
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetSummaryCards(
    budgetSummary: com.example.budject_planner.domain.model.BudgetSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Income Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "$${String.format("%.2f", budgetSummary.totalIncome)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }
        
        // Expense Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC62828)
                )
                Text(
                    text = "$${String.format("%.2f", budgetSummary.totalExpense)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )
            }
        }
        
        // Balance Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = if (budgetSummary.balance >= 0) 
                    Color(0xFFE3F2FD) else Color(0xFFFFF3E0)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Balance",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (budgetSummary.balance >= 0) 
                        Color(0xFF1976D2) else Color(0xFFF57C00)
                )
                Text(
                    text = "$${String.format("%.2f", budgetSummary.balance)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (budgetSummary.balance >= 0) 
                        Color(0xFF1976D2) else Color(0xFFF57C00)
                )
            }
        }
    }
}
