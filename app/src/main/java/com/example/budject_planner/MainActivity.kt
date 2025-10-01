package com.example.budject_planner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

import com.example.budject_planner.di.DatabaseModule
import com.example.budject_planner.presentation.screen.TabScreen
import com.example.budject_planner.presentation.viewmodel.BudgetViewModel
import com.example.budject_planner.ui.theme.Budject_plannerTheme

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "SMS permission denied", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request SMS permission if not granted
        if (!checkSmsPermission()) {
            requestSmsPermission()
        }

        setContent {
            Budject_plannerTheme {
                // Initialize database and repository
                val database = DatabaseModule.provideDatabase(this@MainActivity)
                val transactionDao = DatabaseModule.provideTransactionDao(database)
                val smsMessageDao = DatabaseModule.provideSmsMessageDao(database)
                val repository = DatabaseModule.provideBudgetRepository(transactionDao, smsMessageDao)
                
                // Create ViewModel
                val viewModel: BudgetViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            BudgetViewModel(application, repository)
                        }
                    }
                )
                
                    TabScreen(viewModel = viewModel)
            }
        }
    }

    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermission() {
        requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
    }
}
