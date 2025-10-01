package com.example.budject_planner.domain.mapper

import com.example.budject_planner.data.entity.TransactionEntity
import com.example.budject_planner.data.entity.SmsMessageEntity
import com.example.budject_planner.domain.model.Transaction
import com.example.budject_planner.domain.model.SmsMessage
import com.example.budject_planner.domain.model.TransactionType
import com.example.budject_planner.domain.model.TransactionSource

object TransactionMapper {
    
    fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            type = TransactionType.valueOf(type),
            amount = amount,
            note = note,
            date = date,
            source = TransactionSource.valueOf(source),
            category = category
        )
    }
    
    fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            type = type.name,
            amount = amount,
            note = note,
            date = date,
            source = source.name,
            category = category
        )
    }
    
    fun SmsMessageEntity.toDomain(): SmsMessage {
        return SmsMessage(
            id = id,
            sender = sender,
            body = body,
            date = date,
            amount = amount,
            type = TransactionType.valueOf(type),
            processed = processed
        )
    }
    
    fun SmsMessage.toEntity(): SmsMessageEntity {
        return SmsMessageEntity(
            id = id,
            sender = sender,
            body = body,
            date = date,
            amount = amount,
            type = type.name,
            processed = processed
        )
    }
    
    @JvmName("transactionEntitiesToDomain")
    fun List<TransactionEntity>.toDomain(): List<Transaction> {
        return map { it.toDomain() }
    }
    
    @JvmName("transactionsToDomain")
    fun List<Transaction>.toEntity(): List<TransactionEntity> {
        return map { it.toEntity() }
    }
    
    @JvmName("smsMessageEntitiesToDomain")
    fun List<SmsMessageEntity>.toDomain(): List<SmsMessage> {
        return map { it.toDomain() }
    }
    
    @JvmName("smsMessagesToEntity")
    fun List<SmsMessage>.toEntity(): List<SmsMessageEntity> {
        return map { it.toEntity() }
    }
}
