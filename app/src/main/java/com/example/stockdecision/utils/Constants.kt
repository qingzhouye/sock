package com.example.stockdecision.utils

/**
 * Application constants
 */
object Constants {
    
    // Notification
    const val NOTIFICATION_CHANNEL_ID = "stock_alert_channel"
    const val NOTIFICATION_CHANNEL_NAME = "股票价格提醒"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "股票价格达到目标时发送提醒"
    const val SERVICE_NOTIFICATION_ID = 1
    const val ALERT_NOTIFICATION_ID_BASE = 1000
    
    // Service
    const val MONITOR_INTERVAL_MS = 60000L // 1 minute
    const val ACTION_START_SERVICE = "com.example.stockdecision.START_MONITOR_SERVICE"
    const val ACTION_STOP_SERVICE = "com.example.stockdecision.STOP_MONITOR_SERVICE"
    const val ACTION_CHECK_PRICES = "com.example.stockdecision.CHECK_PRICES"
    
    // Email
    const val EMAIL_SUBJECT = "股票价格提醒"
    const val SMTP_TIMEOUT_MS = 10000
    
    // Stock
    const val DEFAULT_TARGET_RETURN = 10.0 // 10%
}
