package com.example.stockdecision.data.repository

import android.content.Context
import com.example.stockdecision.data.local.ApiKeyConfigDao
import com.example.stockdecision.data.model.ApiKeyConfig
import com.example.stockdecision.data.model.ApiKeyInput
import com.example.stockdecision.data.remote.ApiResult
import com.example.stockdecision.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository for API Key management
 */
class ApiKeyRepository(
    private val apiKeyConfigDao: ApiKeyConfigDao,
    private val context: Context
) {
    private val apiService = RetrofitClient.stockApiService
    
    /**
     * Get API Key configuration as Flow
     */
    fun getApiKeyConfig(): Flow<ApiKeyConfig?> = apiKeyConfigDao.getApiKeyConfig()
    
    /**
     * Get API Key configuration synchronously
     */
    suspend fun getApiKeyConfigSync(): ApiKeyConfig? = apiKeyConfigDao.getApiKeyConfigSync()
    
    /**
     * Get API Key string
     */
    suspend fun getApiKey(): String? = apiKeyConfigDao.getApiKey()
    
    /**
     * Save API Key
     */
    suspend fun saveApiKey(apiKeyInput: ApiKeyInput): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!apiKeyInput.isValid()) {
                return@withContext Result.failure(Exception("API Key 无效，至少需要10个字符"))
            }
            
            val config = ApiKeyConfig(
                apiKey = apiKeyInput.apiKey.trim()
            )
            
            apiKeyConfigDao.insertApiKeyConfig(config)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete API Key
     */
    suspend fun deleteApiKey(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiKeyConfigDao.deleteApiKeyConfig()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate API Key by making a test request
     */
    suspend fun validateApiKey(apiKey: String): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Use IBM as a test symbol (reliable for testing)
            val response = apiService.getGlobalQuote(
                symbol = "IBM",
                apiKey = apiKey
            )
            
            if (response.isSuccessful) {
                val quote = response.body()?.globalQuote
                if (quote != null && quote.symbol != null) {
                    // Valid API key
                    ApiResult.Success(true)
                } else {
                    // Check if it's a rate limit or invalid key error
                    val errorBody = response.errorBody()?.string()
                    when {
                        errorBody?.contains("Invalid API call") == true ->
                            ApiResult.Error("API Key 无效")
                        errorBody?.contains("Note") == true && errorBody.contains("API call frequency") == true ->
                            ApiResult.Error("API 调用频率限制，请稍后再试")
                        else ->
                            ApiResult.Error("无法验证 API Key")
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                when (response.code()) {
                    401, 403 -> ApiResult.Error("API Key 无效或已过期")
                    429 -> ApiResult.Error("API 调用频率限制")
                    else -> ApiResult.Error("验证失败: ${errorBody ?: response.message()}")
                }
            }
        } catch (e: Exception) {
            ApiResult.Error("网络错误: ${e.message}")
        }
    }
    
    /**
     * Update last used timestamp
     */
    suspend fun updateLastUsed() {
        apiKeyConfigDao.updateLastUsed()
    }
    
    /**
     * Check if API Key exists
     */
    suspend fun hasApiKey(): Boolean = apiKeyConfigDao.hasApiKeyConfig()
    
    /**
     * Mask API Key for display (show only first 4 and last 4 characters)
     */
    fun maskApiKey(apiKey: String): String {
        return if (apiKey.length > 8) {
            "${apiKey.take(4)}${"*".repeat(apiKey.length - 8)}${apiKey.takeLast(4)}"
        } else {
            "*".repeat(apiKey.length)
        }
    }
}
