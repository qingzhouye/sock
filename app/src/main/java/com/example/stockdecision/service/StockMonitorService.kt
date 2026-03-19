package com.example.stockdecision.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import com.example.stockdecision.data.local.StockDatabase
import com.example.stockdecision.data.model.Stock
import com.example.stockdecision.data.remote.ApiResult
import com.example.stockdecision.data.repository.EmailConfigRepository
import com.example.stockdecision.data.repository.StockRepository
import com.example.stockdecision.utils.Constants
import com.example.stockdecision.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service for monitoring stock prices
 * Checks prices every minute and sends alerts when conditions are met
 */
class StockMonitorService : Service() {
    
    companion object {
        private const val TAG = "StockMonitorService"
        
        fun start(context: Context) {
            val intent = Intent(context, StockMonitorService::class.java)
            context.startForegroundService(intent)
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, StockMonitorService::class.java)
            context.stopService(intent)
        }
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var handler: Handler
    private lateinit var stockRepository: StockRepository
    private lateinit var emailConfigRepository: EmailConfigRepository
    private lateinit var emailSender: EmailSender
    private lateinit var notificationHelper: NotificationHelper
    private var wakeLock: PowerManager.WakeLock? = null
    
    private val checkRunnable = object : Runnable {
        override fun run() {
            checkStockPrices()
            handler.postDelayed(this, Constants.MONITOR_INTERVAL_MS)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        handler = Handler(Looper.getMainLooper())
        
        val database = StockDatabase.getDatabase(this)
        stockRepository = StockRepository(database.stockDao(), this)
        emailConfigRepository = EmailConfigRepository(database.emailConfigDao(), this)
        emailSender = EmailSender(this)
        notificationHelper = NotificationHelper(this)
        
        // Acquire wake lock to keep CPU running
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "StockDecision::StockMonitorWakeLock"
        ).apply {
            setReferenceCounted(false)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        serviceScope.launch {
            val activeCount = stockRepository.getActiveStockCount()
            if (activeCount == 0) {
                // No stocks to monitor, stop service
                stopSelf()
                return@launch
            }
            
            // Start as foreground service
            val notification = notificationHelper.buildServiceNotification(activeCount)
            startForeground(Constants.SERVICE_NOTIFICATION_ID, notification)
            
            // Acquire wake lock
            wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes timeout
            
            // Start checking prices
            handler.post(checkRunnable)
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        
        handler.removeCallbacks(checkRunnable)
        serviceScope.cancel()
        
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }
    
    /**
     * Check prices for all active stocks
     */
    private fun checkStockPrices() {
        serviceScope.launch {
            try {
                val activeStocks = stockRepository.getActiveStocksSync()
                
                if (activeStocks.isEmpty()) {
                    // No active stocks, stop service
                    stopSelf()
                    return@launch
                }
                
                // Update notification with current count
                val notification = notificationHelper.buildServiceNotification(activeStocks.size)
                startForeground(Constants.SERVICE_NOTIFICATION_ID, notification)
                
                // Get email config
                val emailConfig = emailConfigRepository.getEmailConfigSync()
                
                // Check each stock
                for (stock in activeStocks) {
                    checkSingleStock(stock, emailConfig)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error checking stock prices", e)
            }
        }
    }
    
    /**
     * Check a single stock's price
     */
    private suspend fun checkSingleStock(stock: Stock, emailConfig: com.example.stockdecision.data.model.EmailConfigPlain?) {
        when (val result = stockRepository.getCurrentPrice(stock.symbol)) {
            is ApiResult.Success -> {
                val currentPrice = result.data.price
                
                if (stock.shouldTrigger(currentPrice)) {
                    // Stock has triggered alert
                    handleStockTriggered(stock, currentPrice, emailConfig)
                }
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error getting price for ${stock.symbol}: ${result.message}")
            }
            else -> { /* Loading state, ignore */ }
        }
    }
    
    /**
     * Handle stock alert trigger
     */
    private suspend fun handleStockTriggered(
        stock: Stock,
        currentPrice: Double,
        emailConfig: com.example.stockdecision.data.model.EmailConfigPlain?
    ) {
        Log.d(TAG, "Stock ${stock.symbol} triggered at price $currentPrice")
        
        // Mark as triggered in database
        stockRepository.markAsTriggered(stock.id)
        
        // Show notification
        val notificationId = Constants.ALERT_NOTIFICATION_ID_BASE + stock.id.toInt()
        notificationHelper.showAlertNotification(stock.symbol, currentPrice, notificationId)
        
        // Send email if configured
        emailConfig?.let { config ->
            if (config.isValid()) {
                val emailResult = emailSender.sendStockAlert(config, stock, currentPrice)
                emailResult.onFailure { e ->
                    Log.e(TAG, "Failed to send email alert", e)
                }
            }
        }
        
        // Send broadcast to update UI if app is in foreground
        sendBroadcast(Intent("com.example.stockdecision.STOCK_TRIGGERED").apply {
            putExtra("stock_id", stock.id)
            putExtra("stock_symbol", stock.symbol)
            putExtra("current_price", currentPrice)
        })
    }
}
