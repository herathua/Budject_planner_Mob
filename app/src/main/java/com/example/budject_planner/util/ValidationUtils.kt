package com.example.budject_planner.util

import com.example.budject_planner.domain.model.Transaction
import com.example.budject_planner.domain.model.TransactionType

object ValidationUtils {
    
    fun validateTransaction(transaction: Transaction): Result<Unit> {
        return when {
            transaction.amount <= 0 -> Result.failure(
                AppError.ValidationError("Amount must be greater than 0")
            )
            transaction.note.isBlank() -> Result.failure(
                AppError.ValidationError("Note cannot be empty")
            )
            transaction.note.length > 100 -> Result.failure(
                AppError.ValidationError("Note is too long (max 100 characters)")
            )
            transaction.amount > 999999999.99 -> Result.failure(
                AppError.ValidationError("Amount is too large")
            )
            else -> Result.success(Unit)
        }
    }
    
    fun validateAmount(amount: String): Result<Double> {
        return try {
            val parsedAmount = amount.toDoubleOrNull()
            when {
                parsedAmount == null -> Result.failure(
                    AppError.ValidationError("Invalid amount format")
                )
                parsedAmount <= 0 -> Result.failure(
                    AppError.ValidationError("Amount must be greater than 0")
                )
                parsedAmount > 999999999.99 -> Result.failure(
                    AppError.ValidationError("Amount is too large")
                )
                else -> Result.success(parsedAmount)
            }
        } catch (e: Exception) {
            Result.failure(AppError.ValidationError("Invalid amount format"))
        }
    }
    
    fun validateNote(note: String): Result<String> {
        return when {
            note.isBlank() -> Result.failure(
                AppError.ValidationError("Note cannot be empty")
            )
            note.length > 100 -> Result.failure(
                AppError.ValidationError("Note is too long (max 100 characters)")
            )
            else -> Result.success(note.trim())
        }
    }
}
