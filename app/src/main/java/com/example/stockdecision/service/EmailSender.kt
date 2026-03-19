package com.example.stockdecision.service

import android.content.Context
import com.example.stockdecision.data.model.EmailConfigPlain
import com.example.stockdecision.data.model.Stock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Email sender utility for sending stock alerts
 */
class EmailSender(private val context: Context) {
    
    /**
     * Send stock alert email
     */
    suspend fun sendStockAlert(
        config: EmailConfigPlain,
        stock: Stock,
        currentPrice: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", config.smtpServer)
                put("mail.smtp.port", config.port)
                put("mail.smtp.timeout", "10000")
                put("mail.smtp.connectiontimeout", "10000")
            }
            
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(config.username, config.password)
                }
            })
            
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.recipientEmail))
                subject = "股票价格提醒 - ${stock.getDisplayName()}"
                
                val returnRate = stock.calculateReturnRate(currentPrice)
                val content = buildString {
                    appendLine("股票代码: ${stock.getDisplayName()}")
                    appendLine("当前价格: $currentPrice")
                    appendLine("买入价格: ${stock.buyPrice}")
                    appendLine("当前收益率: ${String.format("%.2f", returnRate)}%")
                    appendLine("目标收益率: ${stock.targetReturnRate}%")
                    appendLine("目标价格: ${stock.targetPrice}")
                    appendLine()
                    appendLine("已达到您设置的条件，请及时操作。")
                    appendLine()
                    appendLine("---")
                    appendLine("此邮件由股票决策应用自动发送")
                }
                
                setText(content)
            }
            
            Transport.send(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send test email
     */
    suspend fun sendTestEmail(config: EmailConfigPlain): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", config.smtpServer)
                put("mail.smtp.port", config.port)
                put("mail.smtp.timeout", "10000")
                put("mail.smtp.connectiontimeout", "10000")
            }
            
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(config.username, config.password)
                }
            })
            
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.recipientEmail))
                subject = "测试邮件 - 股票决策应用"
                setText("这是一封测试邮件。\n\n如果您收到此邮件，说明您的邮件配置正确。")
            }
            
            Transport.send(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
