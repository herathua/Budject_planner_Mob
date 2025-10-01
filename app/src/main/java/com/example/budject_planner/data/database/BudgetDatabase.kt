package com.example.budject_planner.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.budject_planner.data.dao.TransactionDao
import com.example.budject_planner.data.dao.SmsMessageDao
import com.example.budject_planner.data.entity.TransactionEntity
import com.example.budject_planner.data.entity.SmsMessageEntity

@Database(
    entities = [TransactionEntity::class, SmsMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BudgetDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun smsMessageDao(): SmsMessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: BudgetDatabase? = null
        
        fun getDatabase(context: Context): BudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "budget_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
