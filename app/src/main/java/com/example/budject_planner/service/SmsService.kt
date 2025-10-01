package com.example.budject_planner.service

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.example.budject_planner.domain.model.SmsMessage
import com.example.budject_planner.domain.model.TransactionType
import com.example.budject_planner.util.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsService(private val context: Context) {
    
    suspend fun readSmsInbox(): Result<List<SmsMessage>> = withContext(Dispatchers.IO) {
        try {
            val smsMessages = mutableListOf<SmsMessage>()
            
            val cursor: Cursor? = context.contentResolver.query(
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
                        val type = when {
                            isIncomeMessage(body) -> TransactionType.INCOME
                            isExpenseMessage(body) -> TransactionType.EXPENSE
                            else -> continue
                        }
                        
                        smsMessages.add(
                            SmsMessage(
                                id = id,
                                sender = address,
                                body = body,
                                date = date,
                                amount = amount,
                                type = type
                            )
                        )
                    }
                }
            }
            
            Result.success(smsMessages)
        } catch (e: Exception) {
            Result.failure(AppError.SmsReadError("Failed to read SMS: ${e.message}"))
        }
    }
    
    private fun isIncomeMessage(body: String): Boolean {
        val incomeKeywords = listOf(
            "credited", "credit", "received", "deposited", "income", 
            "salary", "refund", "cashback", "bonus", "interest"
        )
        return incomeKeywords.any { body.contains(it, ignoreCase = true) }
    }
    
    private fun isExpenseMessage(body: String): Boolean {
        val expenseKeywords = listOf(
            "debited", "debit", "spent", "withdrawn", "expense", 
            "paid", "payment", "purchase", "transfer", "withdrawal"
        )
        return expenseKeywords.any { body.contains(it, ignoreCase = true) }
    }
    
    private fun extractAmount(sms: String): Double {
        val patterns = listOf(
            Regex("""(?:Rs\.?\s*|LKR\s*|INR\s*)?(\d{1,3}(?:,\d{3})*(?:\.\d{2})?)"""),
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
}
