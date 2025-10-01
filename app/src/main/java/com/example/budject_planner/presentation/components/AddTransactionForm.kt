package com.example.budject_planner.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budject_planner.domain.model.TransactionType
import com.example.budject_planner.util.ValidationUtils

@Composable
fun AddTransactionForm(
    transactionType: TransactionType,
    onAddTransaction: (Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (transactionType == TransactionType.INCOME) 
                Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add ${transactionType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transactionType == TransactionType.INCOME) 
                    Color(0xFF2E7D32) else Color(0xFFC62828)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    amount = it
                    showError = false
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = showError && amount.isBlank(),
                supportingText = if (showError && amount.isBlank()) {
                    { Text("Amount is required") }
                } else null
            )

            OutlinedTextField(
                value = note,
                onValueChange = { 
                    note = it
                    showError = false
                },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                isError = showError && note.isBlank(),
                supportingText = if (showError && note.isBlank()) {
                    { Text("Note is required") }
                } else null
            )

            if (showError && errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    when {
                        amount.isBlank() -> {
                            showError = true
                            errorMessage = "Amount is required"
                        }
                        note.isBlank() -> {
                            showError = true
                            errorMessage = "Note is required"
                        }
                        ValidationUtils.validateAmount(amount).isFailure -> {
                            showError = true
                            errorMessage = "Please enter a valid amount"
                        }
                        ValidationUtils.validateNote(note).isFailure -> {
                            showError = true
                            errorMessage = "Note must be between 1 and 100 characters"
                        }
                        else -> {
                            try {
                                val amountValue = amount.toDouble()
                                if (amountValue <= 0) {
                                    showError = true
                                    errorMessage = "Amount must be greater than 0"
                                } else {
                                    onAddTransaction(amountValue, note)
                                    amount = ""
                                    note = ""
                                    showError = false
                                    errorMessage = ""
                                }
                            } catch (e: NumberFormatException) {
                                showError = true
                                errorMessage = "Please enter a valid number"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (transactionType == TransactionType.INCOME) 
                        Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            ) {
                Text(
                    text = "Add ${transactionType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    color = Color.White
                )
            }
        }
    }
}
