package com.example.stockdecision.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Email configuration entity for Room database
 * Stores SMTP server settings for email alerts
 */
@Entity(tableName = "email_config")
data class EmailConfig(
    @PrimaryKey
    val id: Int = 1,  // Single row table
    val smtpServer: String,      // SMTP server address (e.g., "smtp.gmail.com")
    val port: Int,               // SMTP port (e.g., 587)
    val username: String,        // Email username/address
    val encryptedPassword: String, // Encrypted password/auth token
    val recipientEmail: String   // Recipient email address
) {
    companion object {
        const val DEFAULT_ID = 1
    }
}

/**
 * Plain text email configuration for UI/editing
 * Password is in plain text during editing
 */
data class EmailConfigPlain(
    val smtpServer: String = "",
    val port: String = "587",
    val username: String = "",
    val password: String = "",
    val recipientEmail: String = ""
) {
    fun isValid(): Boolean {
        return smtpServer.isNotBlank() &&
                port.isNotBlank() &&
                username.isNotBlank() &&
                password.isNotBlank() &&
                recipientEmail.isNotBlank()
    }

    fun toIntPort(): Int {
        return port.toIntOrNull() ?: 587
    }
}
