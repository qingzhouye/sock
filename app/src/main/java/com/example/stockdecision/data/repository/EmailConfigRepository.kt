package com.example.stockdecision.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.stockdecision.data.local.EmailConfigDao
import com.example.stockdecision.data.model.EmailConfig
import com.example.stockdecision.data.model.EmailConfigPlain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.GeneralSecurityException

/**
 * Repository for email configuration
 * Handles encrypted storage of email credentials
 */
class EmailConfigRepository(
    private val emailConfigDao: EmailConfigDao,
    private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                "email_config_secure",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences("email_config_fallback", Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Get email configuration as Flow
     */
    fun getEmailConfig(): Flow<EmailConfigPlain?> {
        return emailConfigDao.getEmailConfig().map { config ->
            config?.let {
                EmailConfigPlain(
                    smtpServer = it.smtpServer,
                    port = it.port.toString(),
                    username = it.username,
                    password = decryptPassword(it.encryptedPassword),
                    recipientEmail = it.recipientEmail
                )
            }
        }
    }
    
    /**
     * Get email configuration synchronously
     */
    suspend fun getEmailConfigSync(): EmailConfigPlain? = withContext(Dispatchers.IO) {
        emailConfigDao.getEmailConfigSync()?.let {
            EmailConfigPlain(
                smtpServer = it.smtpServer,
                port = it.port.toString(),
                username = it.username,
                password = decryptPassword(it.encryptedPassword),
                recipientEmail = it.recipientEmail
            )
        }
    }
    
    /**
     * Save email configuration
     */
    suspend fun saveEmailConfig(config: EmailConfigPlain): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!config.isValid()) {
                return@withContext Result.failure(Exception("请填写所有必填项"))
            }
            
            val encryptedConfig = EmailConfig(
                smtpServer = config.smtpServer,
                port = config.toIntPort(),
                username = config.username,
                encryptedPassword = encryptPassword(config.password),
                recipientEmail = config.recipientEmail
            )
            
            emailConfigDao.insertEmailConfig(encryptedConfig)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete email configuration
     */
    suspend fun deleteEmailConfig() = withContext(Dispatchers.IO) {
        emailConfigDao.deleteEmailConfig()
        encryptedPrefs.edit().clear().apply()
    }
    
    /**
     * Check if email configuration exists
     */
    suspend fun hasEmailConfig(): Boolean = withContext(Dispatchers.IO) {
        emailConfigDao.hasEmailConfig()
    }
    
    /**
     * Encrypt password using EncryptedSharedPreferences
     */
    private fun encryptPassword(password: String): String {
        // Store in EncryptedSharedPreferences and return a key
        val key = "email_password_key"
        encryptedPrefs.edit().putString(key, password).apply()
        return key
    }
    
    /**
     * Decrypt password from EncryptedSharedPreferences
     */
    private fun decryptPassword(key: String): String {
        return encryptedPrefs.getString(key, "") ?: ""
    }
}
