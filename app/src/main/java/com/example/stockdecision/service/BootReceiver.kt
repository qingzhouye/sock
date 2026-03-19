package com.example.stockdecision.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.stockdecision.data.local.StockDatabase
import com.example.stockdecision.data.repository.StockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Broadcast receiver to restart monitoring service after device boot
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, checking for active stocks")
            
            scope.launch {
                try {
                    val database = StockDatabase.getDatabase(context)
                    val repository = StockRepository(database.stockDao(), context)
                    
                    val activeCount = repository.getActiveStockCount()
                    if (activeCount > 0) {
                        Log.d(TAG, "Found $activeCount active stocks, starting service")
                        StockMonitorService.start(context)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error restarting service after boot", e)
                }
            }
        }
    }
}
