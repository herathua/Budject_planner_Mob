package com.example.budject_planner.presentation.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budject_planner.domain.model.Transaction
import com.example.budject_planner.domain.model.SmsMessage
import com.example.budject_planner.domain.model.BudgetSummary
import com.example.budject_planner.domain.model.ChartData
import com.example.budject_planner.domain.model.TransactionType
import com.example.budject_planner.domain.model.TransactionSource
import com.example.budject_planner.domain.repository.BudgetRepository
import com.example.budject_planner.service.SmsService
import com.example.budject_planner.util.AppError
import com.example.budject_planner.util.ErrorHandler
import com.example.budject_planner.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.Manifest

class BudgetViewModel(
    application: Application,
    private val repository: BudgetRepository
) : AndroidViewModel(application) {
    
    private val smsService = SmsService(application)
    
    // UI State
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()
    
    // Data flows
    val transactions = repository.getAllTransactions()
    val smsMessages = repository.getAllSmsMessages()
    val budgetSummary = repository.getBudgetSummary()
    val chartData = repository.getChartData(30)
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Check SMS permission and load SMS if available
                if (checkSmsPermission()) {
                    refreshSmsMessages()
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
                ErrorHandler.handleError(getApplication(), e)
            }
        }
    }
    
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                // Validate transaction
                ValidationUtils.validateTransaction(transaction).getOrThrow()
                
                val result = repository.insertTransaction(transaction)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            message = "Transaction added successfully"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message
                        )
                        ErrorHandler.handleError(getApplication(), error)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
                ErrorHandler.handleError(getApplication(), e)
            }
        }
    }
    
    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteTransaction(id)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            message = "Transaction deleted successfully"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message
                        )
                        ErrorHandler.handleError(getApplication(), error)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
                ErrorHandler.handleError(getApplication(), e)
            }
        }
    }
    
    fun refreshSmsMessages() {
        viewModelScope.launch {
            try {
                if (!checkSmsPermission()) {
                    _uiState.value = _uiState.value.copy(
                        error = "SMS permission not granted"
                    )
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Clear existing SMS transactions
                repository.deleteSmsTransactions()
                
                // Read new SMS messages
                val smsResult = smsService.readSmsInbox()
                smsResult.fold(
                    onSuccess = { smsList ->
                        // Insert SMS messages
                        repository.insertSmsMessages(smsList)
                        
                        // Convert SMS to transactions
                        val transactions = smsList.map { sms ->
                            Transaction(
                                type = sms.type,
                                amount = sms.amount,
                                note = "SMS from ${sms.sender}",
                                date = sms.date,
                                source = TransactionSource.SMS
                            )
                        }
                        
                        repository.insertTransactions(transactions)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Loaded ${smsList.size} SMS transactions"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                        ErrorHandler.handleError(getApplication(), error)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
                ErrorHandler.handleError(getApplication(), e)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

data class BudgetUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)
