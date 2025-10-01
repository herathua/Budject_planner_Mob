package com.example.budject_planner.domain.repository

import com.example.budject_planner.domain.model.Transaction
import com.example.budject_planner.domain.model.SmsMessage
import com.example.budject_planner.domain.model.BudgetSummary
import com.example.budject_planner.domain.model.ChartData
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    
    // Transaction operations
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByType(type: String): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Result<Unit>
    suspend fun insertTransactions(transactions: List<Transaction>): Result<Unit>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(id: String): Result<Unit>
    suspend fun deleteSmsTransactions(): Result<Unit>
    
    // SMS operations
    fun getAllSmsMessages(): Flow<List<SmsMessage>>
    fun getUnprocessedSmsMessages(): Flow<List<SmsMessage>>
    suspend fun insertSmsMessage(smsMessage: SmsMessage): Result<Unit>
    suspend fun insertSmsMessages(smsMessages: List<SmsMessage>): Result<Unit>
    suspend fun markSmsAsProcessed(id: Long): Result<Unit>
    suspend fun deleteSmsMessage(id: Long): Result<Unit>
    
    // Summary operations
    fun getBudgetSummary(): Flow<BudgetSummary>
    fun getBudgetSummaryByDateRange(startDate: Long, endDate: Long): Flow<BudgetSummary>
    
    // Chart data
    fun getChartData(days: Int = 30): Flow<ChartData>
}
