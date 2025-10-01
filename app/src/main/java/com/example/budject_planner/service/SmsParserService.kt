package com.example.budject_planner.service

import android.content.Context
import android.content.SharedPreferences
import com.example.budject_planner.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

data class SmsParseResult(
    val amount: Double?,
    val date: Long?,
    val location: String?,
    val account: String?,
    val transactionType: TransactionType,
    val isValid: Boolean
)

class SmsParserService(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("sms_parser_settings", Context.MODE_PRIVATE)
    
    // Default patterns based on your sample message
    private val defaultAmountPattern = "LKR ([0-9,]+\\.[0-9]{2})"
    private val defaultDatePattern = "on (\\d{2} \\w{3} \\d{4})"
    private val defaultTimePattern = "at (\\d{2}:\\d{2})"
    private val defaultLocationPattern = "at ([A-Za-z0-9\\s]+)\\. Avl"
    private val defaultAccountPattern = "AC (\\w+)"
    
    fun parseSmsMessage(message: String): SmsParseResult {
        val amountPattern = getAmountPattern()
        val datePattern = getDatePattern()
        val timePattern = getTimePattern()
        val locationPattern = getLocationPattern()
        val accountPattern = getAccountPattern()
        val defaultType = getDefaultTransactionType()
        
        val amount = extractAmount(message, amountPattern)
        val date = extractDate(message, datePattern, timePattern)
        val location = extractWithPattern(message, locationPattern)
        val account = extractWithPattern(message, accountPattern)
        
        return SmsParseResult(
            amount = amount,
            date = date,
            location = location,
            account = account,
            transactionType = defaultType,
            isValid = amount != null && date != null
        )
    }
    
    private fun extractAmount(text: String, pattern: String): Double? {
        return try {
            val regex = pattern.toRegex()
            val matchResult = regex.find(text)
            val amountStr = matchResult?.groupValues?.get(1)?.replace(",", "")
            amountStr?.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractDate(text: String, datePattern: String, timePattern: String): Long? {
        return try {
            val dateStr = extractWithPattern(text, datePattern)
            val timeStr = extractWithPattern(text, timePattern)
            
            if (dateStr.isNotEmpty() && timeStr.isNotEmpty()) {
                val fullDateTime = "$dateStr $timeStr"
                val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH)
                formatter.parse(fullDateTime)?.time
            } else {
                null
            }
        } catch (e: Exception) {
            null
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
    
    // Settings management
    fun saveAmountPattern(pattern: String) {
        prefs.edit().putString("amount_pattern", pattern).apply()
    }
    
    fun getAmountPattern(): String {
        return prefs.getString("amount_pattern", defaultAmountPattern) ?: defaultAmountPattern
    }
    
    fun saveDatePattern(pattern: String) {
        prefs.edit().putString("date_pattern", pattern).apply()
    }
    
    fun getDatePattern(): String {
        return prefs.getString("date_pattern", defaultDatePattern) ?: defaultDatePattern
    }
    
    fun saveTimePattern(pattern: String) {
        prefs.edit().putString("time_pattern", pattern).apply()
    }
    
    fun getTimePattern(): String {
        return prefs.getString("time_pattern", defaultTimePattern) ?: defaultTimePattern
    }
    
    fun saveLocationPattern(pattern: String) {
        prefs.edit().putString("location_pattern", pattern).apply()
    }
    
    fun getLocationPattern(): String {
        return prefs.getString("location_pattern", defaultLocationPattern) ?: defaultLocationPattern
    }
    
    fun saveAccountPattern(pattern: String) {
        prefs.edit().putString("account_pattern", pattern).apply()
    }
    
    fun getAccountPattern(): String {
        return prefs.getString("account_pattern", defaultAccountPattern) ?: defaultAccountPattern
    }
    
    fun saveDefaultTransactionType(type: TransactionType) {
        prefs.edit().putString("default_transaction_type", type.name).apply()
    }
    
    fun getDefaultTransactionType(): TransactionType {
        val typeStr = prefs.getString("default_transaction_type", TransactionType.EXPENSE.name)
        return try {
            TransactionType.valueOf(typeStr ?: TransactionType.EXPENSE.name)
        } catch (e: Exception) {
            TransactionType.EXPENSE
        }
    }
    
    fun saveSampleMessage(message: String) {
        prefs.edit().putString("sample_message", message).apply()
    }
    
    fun getSampleMessage(): String {
        return prefs.getString("sample_message", 
            "LKR 6,1234.30 debited from AC XXXXXXXX1234 as POS TXN on 01 Oct 2025 17:49 at ABCDE. Avl Bal 12,123.98 Call 94112448888 for info"
        ) ?: "LKR 6,1234.30 debited from AC XXXXXXXX1234 as POS TXN on 01 Oct 2025 17:49 at ABCDE. Avl Bal 12,123.98 Call 94112448888 for info"
    }
}
