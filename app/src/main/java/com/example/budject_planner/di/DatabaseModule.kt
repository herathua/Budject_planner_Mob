package com.example.budject_planner.di

import android.content.Context
import com.example.budject_planner.data.database.BudgetDatabase
import com.example.budject_planner.data.dao.TransactionDao
import com.example.budject_planner.data.dao.SmsMessageDao
import com.example.budject_planner.data.repository.BudgetRepositoryImpl
import com.example.budject_planner.domain.repository.BudgetRepository

object DatabaseModule {
    
    private var database: BudgetDatabase? = null
    
    fun provideDatabase(context: Context): BudgetDatabase {
        return database ?: BudgetDatabase.getDatabase(context).also { database = it }
    }
    
    fun provideTransactionDao(database: BudgetDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    fun provideSmsMessageDao(database: BudgetDatabase): SmsMessageDao {
        return database.smsMessageDao()
    }
    
    fun provideBudgetRepository(
        transactionDao: TransactionDao,
        smsMessageDao: SmsMessageDao
    ): BudgetRepository {
        return BudgetRepositoryImpl(transactionDao, smsMessageDao)
    }
}

