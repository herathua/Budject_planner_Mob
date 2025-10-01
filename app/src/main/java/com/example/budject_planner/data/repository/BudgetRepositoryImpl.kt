package com.example.budject_planner.data.repository

import com.example.budject_planner.data.dao.TransactionDao
import com.example.budject_planner.data.dao.SmsMessageDao
import com.example.budject_planner.domain.mapper.TransactionMapper.toDomain
import com.example.budject_planner.domain.mapper.TransactionMapper.toEntity
import com.example.budject_planner.domain.model.Transaction
import com.example.budject_planner.domain.model.SmsMessage
import com.example.budject_planner.domain.model.BudgetSummary
import com.example.budject_planner.domain.model.ChartData
import com.example.budject_planner.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
class BudgetRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val smsMessageDao: SmsMessageDao
) : BudgetRepository {
    
    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { it.toDomain() }
    }
    
    override fun getTransactionsByType(type: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type).map { it.toDomain() }
    }
    
    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate).map { it.toDomain() }
    }
    
    override suspend fun getTransactionById(id: String): Transaction? {
        return try {
            transactionDao.getTransactionById(id)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun insertTransaction(transaction: Transaction): Result<Unit> {
        return try {
            transactionDao.insertTransaction(transaction.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun insertTransactions(transactions: List<Transaction>): Result<Unit> {
        return try {
            transactionDao.insertTransactions(transactions.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            transactionDao.updateTransaction(transaction.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            transactionDao.deleteTransactionById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteSmsTransactions(): Result<Unit> {
        return try {
            transactionDao.deleteSmsTransactions()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getAllSmsMessages(): Flow<List<SmsMessage>> {
        return smsMessageDao.getAllSmsMessages().map { it.toDomain() }
    }
    
    override fun getUnprocessedSmsMessages(): Flow<List<SmsMessage>> {
        return smsMessageDao.getUnprocessedSmsMessages().map { it.toDomain() }
    }
    
    override suspend fun insertSmsMessage(smsMessage: SmsMessage): Result<Unit> {
        return try {
            smsMessageDao.insertSmsMessage(smsMessage.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun insertSmsMessages(smsMessages: List<SmsMessage>): Result<Unit> {
        return try {
            smsMessageDao.insertSmsMessages(smsMessages.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markSmsAsProcessed(id: Long): Result<Unit> {
        return try {
            smsMessageDao.markAsProcessed(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteSmsMessage(id: Long): Result<Unit> {
        return try {
            smsMessageDao.deleteSmsMessageById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getBudgetSummary(): Flow<BudgetSummary> {
        return combine(
            transactionDao.getTotalIncome(),
            transactionDao.getTotalExpense(),
            transactionDao.getTransactionsByType("INCOME"),
            transactionDao.getTransactionsByType("EXPENSE")
        ) { income, expense, incomeList, expenseList ->
            BudgetSummary(
                totalIncome = income ?: 0.0,
                totalExpense = expense ?: 0.0,
                balance = (income ?: 0.0) - (expense ?: 0.0),
                incomeCount = incomeList.size,
                expenseCount = expenseList.size
            )
        }
    }
    
    override fun getBudgetSummaryByDateRange(startDate: Long, endDate: Long): Flow<BudgetSummary> {
        return combine(
            transactionDao.getIncomeByDateRange(startDate, endDate),
            transactionDao.getExpenseByDateRange(startDate, endDate),
            transactionDao.getTransactionsByDateRange(startDate, endDate)
        ) { income, expense, transactions ->
            val incomeList = transactions.filter { it.type == "INCOME" }
            val expenseList = transactions.filter { it.type == "EXPENSE" }
            
            BudgetSummary(
                totalIncome = income ?: 0.0,
                totalExpense = expense ?: 0.0,
                balance = (income ?: 0.0) - (expense ?: 0.0),
                incomeCount = incomeList.size,
                expenseCount = expenseList.size
            )
        }
    }
    
    override fun getChartData(days: Int): Flow<ChartData> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -days)
        val startDate = calendar.timeInMillis
        
        return transactionDao.getTransactionsByDateRange(startDate, endDate).map { transactions ->
            val groupedByDay = transactions.groupBy { transaction ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = transaction.date
                "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
            }
            
            val labels = groupedByDay.keys.sorted()
            val incomeData = labels.map { day ->
                groupedByDay[day]?.filter { it.type == "INCOME" }?.sumOf { it.amount } ?: 0.0
            }
            val expenseData = labels.map { day ->
                groupedByDay[day]?.filter { it.type == "EXPENSE" }?.sumOf { it.amount } ?: 0.0
            }
            
            ChartData(labels, incomeData, expenseData)
        }
    }
}
