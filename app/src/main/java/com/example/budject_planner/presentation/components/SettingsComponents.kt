package com.example.budject_planner.presentation.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budject_planner.domain.model.TransactionType
import com.example.budject_planner.service.SmsParserService
import com.example.budject_planner.service.SmsParseResult

@Composable
fun SettingsTab(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val smsParserService = remember { SmsParserService(context) }
    
    var amountPattern by remember { mutableStateOf(smsParserService.getAmountPattern()) }
    var datePattern by remember { mutableStateOf(smsParserService.getDatePattern()) }
    var timePattern by remember { mutableStateOf(smsParserService.getTimePattern()) }
    var locationPattern by remember { mutableStateOf(smsParserService.getLocationPattern()) }
    var accountPattern by remember { mutableStateOf(smsParserService.getAccountPattern()) }
    var transactionType by remember { mutableStateOf(smsParserService.getDefaultTransactionType()) }
    var sampleMessage by remember { mutableStateOf(smsParserService.getSampleMessage()) }
    var extractedAmount by remember { mutableStateOf("") }
    var extractedDate by remember { mutableStateOf("") }
    var extractedTime by remember { mutableStateOf("") }
    var extractedLocation by remember { mutableStateOf("") }
    var extractedAccount by remember { mutableStateOf("") }
    var parseResult by remember { mutableStateOf<SmsParseResult?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SMS Template Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Sample Message Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sample SMS Message",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = sampleMessage,
                    onValueChange = { sampleMessage = it },
                    label = { Text("SMS Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }

        // Pattern Configuration Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Extraction Patterns",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Amount Pattern
                OutlinedTextField(
                    value = amountPattern,
                    onValueChange = { amountPattern = it },
                    label = { Text("Amount Pattern (Regex)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Example: LKR ([0-9,]+\\.[0-9]{2})") }
                )

                // Date Pattern
                OutlinedTextField(
                    value = datePattern,
                    onValueChange = { datePattern = it },
                    label = { Text("Date Pattern (Regex)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Example: on (\\d{2} \\w{3} \\d{4})") }
                )

                // Time Pattern
                OutlinedTextField(
                    value = timePattern,
                    onValueChange = { timePattern = it },
                    label = { Text("Time Pattern (Regex)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Example: at (\\d{2}:\\d{2})") }
                )

                // Location Pattern
                OutlinedTextField(
                    value = locationPattern,
                    onValueChange = { locationPattern = it },
                    label = { Text("Location Pattern (Regex)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Example: at ([A-Za-z0-9\\s]+)\\. Avl") }
                )

                // Account Pattern
                OutlinedTextField(
                    value = accountPattern,
                    onValueChange = { accountPattern = it },
                    label = { Text("Account Pattern (Regex)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Example: AC (\\w+)") }
                )

                // Transaction Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Default Type:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { transactionType = TransactionType.EXPENSE },
                            label = { Text("Expense") },
                            selected = transactionType == TransactionType.EXPENSE
                        )
                        FilterChip(
                            onClick = { transactionType = TransactionType.INCOME },
                            label = { Text("Income") },
                            selected = transactionType == TransactionType.INCOME
                        )
                    }
                }
            }
        }

        // Test Extraction Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Test Extraction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        // Test the patterns against the sample message
                        parseResult = smsParserService.parseSmsMessage(sampleMessage)
                        extractedAmount = parseResult?.amount?.toString() ?: ""
                        extractedDate = parseResult?.date?.let { 
                            java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it))
                        } ?: ""
                        extractedTime = ""
                        extractedLocation = parseResult?.location ?: ""
                        extractedAccount = parseResult?.account ?: ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Patterns")
                }

                // Display extracted values
                if (extractedAmount.isNotEmpty() || extractedDate.isNotEmpty() || 
                    extractedTime.isNotEmpty() || extractedLocation.isNotEmpty() || 
                    extractedAccount.isNotEmpty()) {
                    
                    Text(
                        text = "Extracted Values:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (extractedAmount.isNotEmpty()) {
                        Text("Amount: $extractedAmount")
                    }
                    if (extractedDate.isNotEmpty()) {
                        Text("Date: $extractedDate")
                    }
                    if (extractedTime.isNotEmpty()) {
                        Text("Time: $extractedTime")
                    }
                    if (extractedLocation.isNotEmpty()) {
                        Text("Location: $extractedLocation")
                    }
                    if (extractedAccount.isNotEmpty()) {
                        Text("Account: $extractedAccount")
                    }
                }
            }
        }

        // Save Settings Button
        Button(
            onClick = {
                smsParserService.saveAmountPattern(amountPattern)
                smsParserService.saveDatePattern(datePattern)
                smsParserService.saveTimePattern(timePattern)
                smsParserService.saveLocationPattern(locationPattern)
                smsParserService.saveAccountPattern(accountPattern)
                smsParserService.saveDefaultTransactionType(transactionType)
                smsParserService.saveSampleMessage(sampleMessage)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Save Settings", color = Color.White)
        }
    }
}

private fun extractWithPattern(text: String, pattern: String): String {
    return try {
        val regex = pattern.toRegex()
        val matchResult = regex.find(text)
        matchResult?.groupValues?.get(1) ?: ""
    } catch (e: Exception) {
        ""
    }
}
