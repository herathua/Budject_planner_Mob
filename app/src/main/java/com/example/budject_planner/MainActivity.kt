package com.example.budject_planner

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.budject_planner.ui.theme.Budject_plannerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// ------------------- Data Classes -------------------
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val amount: Double,
    val note: String,
    val date: Long = System.currentTimeMillis(),
    val source: TransactionSource = TransactionSource.MANUAL
)

enum class TransactionType {
    INCOME, EXPENSE
}

enum class TransactionSource {
    MANUAL, SMS
}

data class SmsMessage(
    val id: Long,
    val sender: String,
    val body: String,
    val date: Long,
    val amount: Double,
    val type: TransactionType
)

// ------------------- Main Composable -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetApp(
    transactions: List<Transaction> = emptyList(),
    smsMessages: List<SmsMessage> = emptyList(),
    onAddTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onRefreshSms: () -> Unit
) {
    var newIncome by remember { mutableStateOf("") }
    var newExpense by remember { mutableStateOf("") }
    var incomeNote by remember { mutableStateOf("") }
    var expenseNote by remember { mutableStateOf("") }
    var showSmsDropdown by remember { mutableStateOf(false) }

    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Current Balance", color = Color.White)
                        Text(
                            "Rs %.2f".format(balance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Income", color = Color.White.copy(alpha = 0.8f))
                                Text("Rs %.2f".format(totalIncome), color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Expense", color = Color.White.copy(alpha = 0.8f))
                                Text("Rs %.2f".format(totalExpense), color = Color.White)
                            }
                        }
                    }
                }
            }

            // SMS Messages
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSmsDropdown = !showSmsDropdown },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tracked SMS Messages (${smsMessages.size})",
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = onRefreshSms) { Text("Refresh") }
                                Icon(
                                    imageVector = if (showSmsDropdown) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showSmsDropdown,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column {
                                if (smsMessages.isEmpty()) {
                                    Text(
                                        "No SMS messages tracked yet",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 300.dp)
                                    ) {
                                        items(smsMessages) { sms ->
                                            SmsMessageItem(sms)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Add Income
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Income", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = newIncome,
                            onValueChange = { newIncome = it },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { Text("Rs ") }
                        )
                        OutlinedTextField(
                            value = incomeNote,
                            onValueChange = { incomeNote = it },
                            label = { Text("Note (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val amount = newIncome.toDoubleOrNull()
                                if (amount != null && amount > 0) {
                                    onAddTransaction(
                                        Transaction(
                                            type = TransactionType.INCOME,
                                            amount = amount,
                                            note = incomeNote.ifBlank { "Manual Income" },
                                            source = TransactionSource.MANUAL
                                        )
                                    )
                                    newIncome = ""
                                    incomeNote = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Income")
                        }
                    }
                }
            }

            // Add Expense
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Expense", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = newExpense,
                            onValueChange = { newExpense = it },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { Text("Rs ") }
                        )
                        OutlinedTextField(
                            value = expenseNote,
                            onValueChange = { expenseNote = it },
                            label = { Text("Note (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val amount = newExpense.toDoubleOrNull()
                                if (amount != null && amount > 0) {
                                    onAddTransaction(
                                        Transaction(
                                            type = TransactionType.EXPENSE,
                                            amount = amount,
                                            note = expenseNote.ifBlank { "Manual Expense" },
                                            source = TransactionSource.MANUAL
                                        )
                                    )
                                    newExpense = ""
                                    expenseNote = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Add Expense")
                        }
                    }
                }
            }

            // Transaction History
            item {
                Text("Transaction History", fontWeight = FontWeight.Bold)
            }
            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No transactions yet")
                        }
                    }
                }
            } else {
                items(transactions.sortedByDescending { it.date }) { transaction ->
                    TransactionItem(transaction, onDeleteTransaction)
                }
            }
        }
    }
}

// ------------------- UI Components -------------------
@Composable
fun TransactionItem(transaction: Transaction, onDelete: (String) -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.type == TransactionType.INCOME)
                Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.note, fontWeight = FontWeight.Bold)
                Text(dateFormat.format(Date(transaction.date)))
                if (transaction.source == TransactionSource.SMS) {
                    Text("From SMS", color = MaterialTheme.colorScheme.primary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${if (transaction.type == TransactionType.INCOME) "+" else "-"}Rs %.2f"
                        .format(transaction.amount),
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onDelete(transaction.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun SmsMessageItem(sms: SmsMessage) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(sms.sender, fontWeight = FontWeight.Bold)
                Text(
                    "${if (sms.type == TransactionType.INCOME) "+" else "-"}Rs ${sms.amount}",
                    fontWeight = FontWeight.Bold
                )
            }
            Text(sms.body, maxLines = 2)
            Text(dateFormat.format(Date(sms.date)))
        }
    }
}

// ------------------- MainActivity -------------------
class MainActivity : ComponentActivity() {
    private val SMS_PERMISSION_CODE = 100
    private val transactions = mutableStateListOf<Transaction>()
    private val smsMessages = mutableStateListOf<SmsMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Budject_plannerTheme {
                BudgetApp(
                    transactions = transactions,
                    smsMessages = smsMessages,
                    onAddTransaction = { transaction -> transactions.add(transaction) },
                    onDeleteTransaction = { id -> transactions.removeAll { it.id == id } },
                    onRefreshSms = {
                        if (checkSmsPermission()) {
                            readSmsInbox()
                        } else {
                            requestSmsPermission()
                        }
                    }
                )
            }
        }

        if (!checkSmsPermission()) {
            requestSmsPermission()
        } else {
            readSmsInbox()
        }
    }

    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_SMS),
            SMS_PERMISSION_CODE
        )
    }

    private fun readSmsInbox() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                smsMessages.clear()
                transactions.removeAll { it.source == TransactionSource.SMS }

                val cursor: Cursor? = contentResolver.query(
                    Uri.parse("content://sms/inbox"),
                    arrayOf("_id", "address", "date", "body"),
                    null, null, "date DESC LIMIT 100"
                )

                cursor?.use {
                    val idIndex = it.getColumnIndexOrThrow("_id")
                    val addressIndex = it.getColumnIndexOrThrow("address")
                    val dateIndex = it.getColumnIndexOrThrow("date")
                    val bodyIndex = it.getColumnIndexOrThrow("body")

                    while (it.moveToNext()) {
                        val id = it.getLong(idIndex)
                        val address = it.getString(addressIndex) ?: "Unknown"
                        val date = it.getLong(dateIndex)
                        val body = it.getString(bodyIndex) ?: ""

                        val amount = extractAmount(body)

                        if (amount > 0) {
                            when {
                                isIncomeMessage(body) -> {
                                    val smsMsg = SmsMessage(id, address, body, date, amount, TransactionType.INCOME)
                                    withContext(Dispatchers.Main) {
                                        smsMessages.add(smsMsg)
                                        transactions.add(
                                            Transaction(
                                                type = TransactionType.INCOME,
                                                amount = amount,
                                                note = "SMS from $address",
                                                date = date,
                                                source = TransactionSource.SMS
                                            )
                                        )
                                    }
                                }
                                isExpenseMessage(body) -> {
                                    val smsMsg = SmsMessage(id, address, body, date, amount, TransactionType.EXPENSE)
                                    withContext(Dispatchers.Main) {
                                        smsMessages.add(smsMsg)
                                        transactions.add(
                                            Transaction(
                                                type = TransactionType.EXPENSE,
                                                amount = amount,
                                                note = "SMS from $address",
                                                date = date,
                                                source = TransactionSource.SMS
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Loaded ${smsMessages.size} SMS transactions",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isIncomeMessage(body: String): Boolean {
        val incomeKeywords = listOf("credited", "credit", "received", "deposited", "income", "salary", "refund", "cashback")
        return incomeKeywords.any { body.contains(it, ignoreCase = true) }
    }

    private fun isExpenseMessage(body: String): Boolean {
        val expenseKeywords = listOf("debited", "debit", "spent", "withdrawn", "expense", "paid", "payment", "purchase")
        return expenseKeywords.any { body.contains(it, ignoreCase = true) }
    }

    private fun extractAmount(sms: String): Double {
        val patterns = listOf(
            Regex("""(?:Rs\.?\s*|LKR\s*)?(\d{1,3}(?:,\d{3})*(?:\.\d{2})?)"""),
            Regex("""\b(\d+(?:\.\d{1,2})?)\b""")
        )
        for (pattern in patterns) {
            val match = pattern.find(sms)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0) return amount
            }
        }
        return 0.0
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readSmsInbox()
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }
}
