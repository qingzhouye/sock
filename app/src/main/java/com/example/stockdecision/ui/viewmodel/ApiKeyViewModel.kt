package com.example.stockdecision.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdecision.data.local.StockDatabase
import com.example.stockdecision.data.model.ApiKeyConfig
import com.example.stockdecision.data.model.ApiKeyInput
import com.example.stockdecision.data.model.ApiKeyValidationResult
import com.example.stockdecision.data.remote.ApiResult
import com.example.stockdecision.data.repository.ApiKeyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for API Key screen
 */
data class ApiKeyUiState(
    val apiKeyConfig: ApiKeyConfig? = null,
    val apiKeyInput: ApiKeyInput = ApiKeyInput(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isValidating: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val validationResult: ApiKeyValidationResult? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for API Key management
 */
class ApiKeyViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ApiKeyRepository
    
    private val _uiState = MutableStateFlow(ApiKeyUiState())
    val uiState: StateFlow<ApiKeyUiState> = _uiState.asStateFlow()
    
    init {
        val database = StockDatabase.getDatabase(application)
        repository = ApiKeyRepository(database.apiKeyConfigDao(), application)
        
        // Load existing API key config
        viewModelScope.launch {
            repository.getApiKeyConfig().collect { config ->
                _uiState.update { 
                    it.copy(
                        apiKeyConfig = config,
                        apiKeyInput = if (config != null) {
                            ApiKeyInput(apiKey = config.apiKey)
                        } else {
                            ApiKeyInput()
                        }
                    )
                }
            }
        }
    }
    
    /**
     * Update API Key input
     */
    fun updateApiKeyInput(input: String) {
        _uiState.update { 
            it.copy(
                apiKeyInput = it.apiKeyInput.copy(apiKey = input),
                validationResult = null,
                errorMessage = null
            )
        }
    }
    
    /**
     * Toggle API Key visibility
     */
    fun toggleVisibility() {
        _uiState.update { 
            it.copy(
                apiKeyInput = it.apiKeyInput.copy(isVisible = !it.apiKeyInput.isVisible)
            )
        }
    }
    
    /**
     * Save API Key
     */
    fun saveApiKey() {
        viewModelScope.launch {
            val currentInput = _uiState.value.apiKeyInput
            
            if (!currentInput.isValid()) {
                _uiState.update { 
                    it.copy(errorMessage = "API Key 无效，至少需要10个字符")
                }
                return@launch
            }
            
            _uiState.update { 
                it.copy(
                    isSaving = true, 
                    saveSuccess = false,
                    errorMessage = null,
                    successMessage = null
                )
            }
            
            // First validate the API key
            _uiState.update { it.copy(isValidating = true) }
            
            when (val validationResult = repository.validateApiKey(currentInput.apiKey)) {
                is ApiResult.Success -> {
                    // Validation passed, save the key
                    when (val result = repository.saveApiKey(currentInput)) {
                        is Result.Success -> {
                            _uiState.update { 
                                it.copy(
                                    isSaving = false,
                                    isValidating = false,
                                    saveSuccess = true,
                                    successMessage = "API Key 保存成功",
                                    apiKeyInput = ApiKeyInput() // Clear input
                                )
                            }
                        }
                        is Result.Failure -> {
                            _uiState.update { 
                                it.copy(
                                    isSaving = false,
                                    isValidating = false,
                                    errorMessage = result.exceptionOrNull()?.message ?: "保存失败"
                                )
                            }
                        }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            isValidating = false,
                            errorMessage = "验证失败: ${validationResult.message}"
                        )
                    }
                }
                else -> { }
            }
        }
    }
    
    /**
     * Delete API Key
     */
    fun deleteApiKey() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    deleteSuccess = false,
                    errorMessage = null,
                    successMessage = null
                )
            }
            
            when (val result = repository.deleteApiKey()) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            deleteSuccess = true,
                            successMessage = "API Key 已删除",
                            apiKeyInput = ApiKeyInput()
                        )
                    }
                }
                is Result.Failure -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "删除失败"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Validate API Key without saving
     */
    fun validateApiKey() {
        viewModelScope.launch {
            val currentInput = _uiState.value.apiKeyInput
            
            if (!currentInput.isValid()) {
                _uiState.update { 
                    it.copy(validationResult = ApiKeyValidationResult.Invalid("API Key 无效"))
                }
                return@launch
            }
            
            _uiState.update { 
                it.copy(
                    isValidating = true,
                    validationResult = ApiKeyValidationResult.Checking
                )
            }
            
            when (val result = repository.validateApiKey(currentInput.apiKey)) {
                is ApiResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isValidating = false,
                            validationResult = ApiKeyValidationResult.Valid
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isValidating = false,
                            validationResult = ApiKeyValidationResult.Invalid(result.message)
                        )
                    }
                }
                else -> { }
            }
        }
    }
    
    /**
     * Clear messages
     */
    fun clearMessages() {
        _uiState.update { 
            it.copy(
                errorMessage = null,
                successMessage = null,
                validationResult = null
            )
        }
    }
    
    /**
     * Reset success flags
     */
    fun resetSuccessFlags() {
        _uiState.update { 
            it.copy(
                saveSuccess = false,
                deleteSuccess = false
            )
        }
    }
    
    /**
     * Get masked API Key for display
     */
    fun getMaskedApiKey(): String {
        return _uiState.value.apiKeyConfig?.let {
            repository.maskApiKey(it.apiKey)
        } ?: ""
    }
}
