package com.example.stockdecision.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdecision.data.local.StockDatabase
import com.example.stockdecision.data.model.EmailConfigPlain
import com.example.stockdecision.data.repository.EmailConfigRepository
import com.example.stockdecision.service.EmailSender
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for Email Config screen
 */
data class EmailConfigUiState(
    val config: EmailConfigPlain? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val testResult: TestResult? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel for email configuration
 */
class EmailConfigViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: EmailConfigRepository
    private val emailSender: EmailSender
    
    private val _uiState = MutableStateFlow(EmailConfigUiState())
    val uiState: StateFlow<EmailConfigUiState> = _uiState.asStateFlow()
    
    init {
        val database = StockDatabase.getDatabase(application)
        repository = EmailConfigRepository(database.emailConfigDao(), application)
        emailSender = EmailSender(application)
        
        // Load existing config
        viewModelScope.launch {
            repository.getEmailConfig().collect { config ->
                _uiState.update { it.copy(config = config) }
            }
        }
    }
    
    /**
     * Save email configuration
     */
    fun saveConfig(config: EmailConfigPlain) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveSuccess = false, errorMessage = null) }
            
            when (val result = repository.saveEmailConfig(config)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isSaving = false, 
                            saveSuccess = true,
                            config = config
                        )
                    }
                }
                is Result.Failure -> {
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "保存失败"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Test email configuration
     */
    fun testConfig(config: EmailConfigPlain) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, testResult = null, errorMessage = null) }
            
            val result = emailSender.sendTestEmail(config)
            
            result.onSuccess {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        testResult = TestResult(true, "测试邮件发送成功")
                    )
                }
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        testResult = TestResult(false, "发送失败: ${e.message}")
                    )
                }
            }
        }
    }
    
    /**
     * Clear test result
     */
    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Reset save success flag
     */
    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}

data class TestResult(
    val success: Boolean,
    val message: String
)
