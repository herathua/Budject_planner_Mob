package com.example.budject_planner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
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
        TabItem("Home", Icons.Default.Home),
        TabItem("Expense", Icons.Default.KeyboardArrowDown),
        TabItem("Income", Icons.Default.KeyboardArrowUp),
        TabItem("History", Icons.Default.List),
        TabItem("Settings", Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(70.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                tab.icon, 
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            ) 
                        },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.weight(1f),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(25.dp)
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "👋",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Welcome to CashTrack",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Track your finances easily",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Budget Summary Cards
        BudgetSummaryCards(budgetSummary = budgetSummary)
        
        // Chart
        BudgetChart(chartData = chartData)
        
        // Recent Transactions
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📝",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Text(
                                text = "No transactions yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Add your first transaction to get started",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        transactions.take(3).forEach { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onDelete = { /* Don't allow delete from dashboard */ }
                            )
                        }
                    }
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        // Expense Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                // Expense Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color(0xFFF44336).copy(alpha = 0.1f),
                            RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "📉",
                        style = MaterialTheme.typography.displayMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Expense Tracker",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Track your spending",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Manage your expenses wisely 💰",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC62828).copy(alpha = 0.8f)
                )
            }
        }
        
        // Add Expense Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Add New Expense",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                AddTransactionForm(
                    transactionType = TransactionType.EXPENSE,
                    onAddTransaction = onAddTransaction
                )
            }
        }
        
        // Expense List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Expense History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📝",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Text(
                                text = "No expenses yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Add your first expense to get started",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        // Income Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E8)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                // Income Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color(0xFF4CAF50).copy(alpha = 0.1f),
                            RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "📈",
                        style = MaterialTheme.typography.displayMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Income Tracker",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Track your earnings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Grow your income steadily 💰",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                )
            }
        }
        
        // Add Income Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Add New Income",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                AddTransactionForm(
                    transactionType = TransactionType.INCOME,
                    onAddTransaction = onAddTransaction
                )
            }
        }
        
        // Income List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Income History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📝",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Text(
                                text = "No income yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Add your first income to get started",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Enhanced Main Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (budgetSummary.balance >= 0)
                    Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                // Balance Icon with Animation
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            if (budgetSummary.balance >= 0) 
                                Color(0xFF4CAF50).copy(alpha = 0.1f) 
                            else Color(0xFFF44336).copy(alpha = 0.1f),
                            RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = if (budgetSummary.balance >= 0) "💰" else "⚠️",
                        style = MaterialTheme.typography.displayMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Current Balance",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (budgetSummary.balance >= 0)
                        Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Rs ${String.format("%.2f", budgetSummary.balance)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (budgetSummary.balance >= 0)
                        Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Balance Status
                Text(
                    text = if (budgetSummary.balance >= 0) "You're doing great! 💪" else "Time to save more! 🎯",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (budgetSummary.balance >= 0)
                        Color(0xFF2E7D32).copy(alpha = 0.8f) 
                    else Color(0xFFC62828).copy(alpha = 0.8f)
                )
            }
        }
        
        // Enhanced Income and Expense Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Income Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFF4CAF50).copy(alpha = 0.1f),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "📈",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Rs ${String.format("%.2f", budgetSummary.totalIncome)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    
                    Text(
                        text = "${budgetSummary.incomeCount} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32).copy(alpha = 0.7f)
                    )
                }
            }
            
            // Expense Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFFF44336).copy(alpha = 0.1f),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "📉",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Rs ${String.format("%.2f", budgetSummary.totalExpense)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    
                    Text(
                        text = "${budgetSummary.expenseCount} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828).copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
