package com.example.budject_planner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_messages")
data class SmsMessageEntity(
    @PrimaryKey
    val id: Long,
    val sender: String,
    val body: String,
    val date: Long,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val processed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
