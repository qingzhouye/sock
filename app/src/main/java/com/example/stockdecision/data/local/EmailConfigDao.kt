package com.example.stockdecision.data.local

import androidx.room.*
import com.example.stockdecision.data.model.EmailConfig
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for EmailConfig entity
 */
@Dao
interface EmailConfigDao {
    
    @Query("SELECT * FROM email_config WHERE id = 1")
    fun getEmailConfig(): Flow<EmailConfig?>
    
    @Query("SELECT * FROM email_config WHERE id = 1")
    suspend fun getEmailConfigSync(): EmailConfig?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmailConfig(config: EmailConfig)
    
    @Update
    suspend fun updateEmailConfig(config: EmailConfig)
    
    @Query("DELETE FROM email_config WHERE id = 1")
    suspend fun deleteEmailConfig()
    
    @Query("SELECT EXISTS(SELECT 1 FROM email_config WHERE id = 1)")
    suspend fun hasEmailConfig(): Boolean
}
