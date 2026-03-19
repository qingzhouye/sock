package com.example.stockdecision.data.local

import androidx.room.*
import com.example.stockdecision.data.model.ApiKeyConfig
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ApiKeyConfig entity
 */
@Dao
interface ApiKeyConfigDao {
    
    @Query("SELECT * FROM api_key_config WHERE id = 1")
    fun getApiKeyConfig(): Flow<ApiKeyConfig?>
    
    @Query("SELECT * FROM api_key_config WHERE id = 1")
    suspend fun getApiKeyConfigSync(): ApiKeyConfig?
    
    @Query("SELECT apiKey FROM api_key_config WHERE id = 1 AND isActive = 1")
    suspend fun getApiKey(): String?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKeyConfig(config: ApiKeyConfig)
    
    @Update
    suspend fun updateApiKeyConfig(config: ApiKeyConfig)
    
    @Query("DELETE FROM api_key_config WHERE id = 1")
    suspend fun deleteApiKeyConfig()
    
    @Query("UPDATE api_key_config SET lastUsedAt = :timestamp WHERE id = 1")
    suspend fun updateLastUsed(timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT EXISTS(SELECT 1 FROM api_key_config WHERE id = 1)")
    suspend fun hasApiKeyConfig(): Boolean
    
    @Query("SELECT COUNT(*) FROM api_key_config")
    suspend fun getApiKeyCount(): Int
}
