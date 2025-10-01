package com.example.budject_planner.data.dao

import androidx.room.*
import com.example.budject_planner.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate")
    fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    fun getExpenseByDateRange(startDate: Long, endDate: Long): Flow<Double?>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
    
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)
    
    @Query("DELETE FROM transactions WHERE source = 'SMS'")
    suspend fun deleteSmsTransactions()
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
