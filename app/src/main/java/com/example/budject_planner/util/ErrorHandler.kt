package com.example.budject_planner.util

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class AppError : Exception() {
    data class DatabaseError(override val message: String) : AppError()
    data class SmsPermissionError(override val message: String) : AppError()
    data class SmsReadError(override val message: String) : AppError()
    data class ValidationError(override val message: String) : AppError()
    data class NetworkError(override val message: String) : AppError()
    data class UnknownError(override val message: String) : AppError()
}

object ErrorHandler {
    
    fun handleError(context: Context, error: Throwable) {
        val message = when (error) {
            is AppError.DatabaseError -> "Database error: ${error.message}"
            is AppError.SmsPermissionError -> "SMS permission error: ${error.message}"
            is AppError.SmsReadError -> "SMS read error: ${error.message}"
            is AppError.ValidationError -> "Validation error: ${error.message}"
            is AppError.NetworkError -> "Network error: ${error.message}"
            is AppError.UnknownError -> "Unknown error: ${error.message}"
            else -> "An unexpected error occurred: ${error.message}"
        }
        
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    fun createCoroutineExceptionHandler(context: Context): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            handleError(context, throwable)
        }
    }
    
    fun safeExecute(
        context: Context,
        scope: CoroutineScope,
        operation: suspend () -> Unit
    ) {
        scope.launch(createCoroutineExceptionHandler(context)) {
            try {
                operation()
            } catch (e: Exception) {
                handleError(context, e)
            }
        }
    }
}
