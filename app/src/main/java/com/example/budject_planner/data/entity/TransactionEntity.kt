package com.example.budject_planner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val note: String,
    val date: Long,
    val source: String, // "MANUAL" or "SMS"
    val category: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
