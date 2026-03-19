package com.example.stockdecision

import android.app.Application

/**
 * Application class for Stock Decision app
 */
class StockDecisionApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: StockDecisionApplication
            private set
    }
}
