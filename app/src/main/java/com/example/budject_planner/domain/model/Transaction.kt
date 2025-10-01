package com.example.budject_planner.domain.model

import java.util.Date

data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: TransactionType,
    val amount: Double,
    val note: String,
    val date: Long = System.currentTimeMillis(),
    val source: TransactionSource = TransactionSource.MANUAL,
    val category: String? = null
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
    val type: TransactionType,
    val processed: Boolean = false
)

data class BudgetSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val incomeCount: Int,
    val expenseCount: Int
)

data class ChartData(
    val labels: List<String>,
    val incomeData: List<Double>,
    val expenseData: List<Double>
)
