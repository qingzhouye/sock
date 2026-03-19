package com.example.stockdecision.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * API Key configuration entity for Room database
 * Stores Alpha Vantage API Key
 */
@Entity(tableName = "api_key_config")
data class ApiKeyConfig(
    @PrimaryKey
    val id: Int = 1,  // Single row table
    val apiKey: String,           // Alpha Vantage API Key
    val isActive: Boolean = true, // Whether this API key is active
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null  // Last time this key was used
) {
    companion object {
        const val DEFAULT_ID = 1
    }
}

/**
 * UI state for API Key input
 */
data class ApiKeyInput(
    val apiKey: String = "",
    val isVisible: Boolean = false
) {
    fun isValid(): Boolean {
        return apiKey.isNotBlank() && apiKey.length >= 10
    }
}

/**
 * API Key validation result
 */
sealed class ApiKeyValidationResult {
    object Valid : ApiKeyValidationResult()
    data class Invalid(val message: String) : ApiKeyValidationResult()
    object Checking : ApiKeyValidationResult()
}
