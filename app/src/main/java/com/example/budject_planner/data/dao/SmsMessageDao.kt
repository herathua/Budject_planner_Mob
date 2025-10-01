package com.example.budject_planner.data.dao

import androidx.room.*
import com.example.budject_planner.data.entity.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsMessageDao {
    
    @Query("SELECT * FROM sms_messages ORDER BY date DESC")
    fun getAllSmsMessages(): Flow<List<SmsMessageEntity>>
    
    @Query("SELECT * FROM sms_messages WHERE processed = 0 ORDER BY date DESC")
    fun getUnprocessedSmsMessages(): Flow<List<SmsMessageEntity>>
    
    @Query("SELECT * FROM sms_messages WHERE id = :id")
    suspend fun getSmsMessageById(id: Long): SmsMessageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsMessage(smsMessage: SmsMessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsMessages(smsMessages: List<SmsMessageEntity>)
    
    @Update
    suspend fun updateSmsMessage(smsMessage: SmsMessageEntity)
    
    @Delete
    suspend fun deleteSmsMessage(smsMessage: SmsMessageEntity)
    
    @Query("DELETE FROM sms_messages WHERE id = :id")
    suspend fun deleteSmsMessageById(id: Long)
    
    @Query("DELETE FROM sms_messages")
    suspend fun deleteAllSmsMessages()
    
    @Query("UPDATE sms_messages SET processed = 1 WHERE id = :id")
    suspend fun markAsProcessed(id: Long)
}
