package com.example.stockdecision.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Stock entity for Room database
 * Represents a monitored stock with buy information and target conditions
 */
@Entity(tableName = "stocks")
data class Stock(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,           // Stock symbol (e.g., "600036.SH", "AAPL")
    val buyPrice: Double,         // Purchase price
    val quantity: Double,         // Purchase quantity
    val targetReturnRate: Double, // Target return rate (e.g., 10.0 for 10%)
    val targetPrice: Double,      // Target price
    val isTriggered: Boolean = false,  // Whether alert has been triggered
    val createdAt: Date = Date(),      // Creation timestamp
    val triggeredAt: Date? = null      // Trigger timestamp
) {
    /**
     * Calculate current return rate based on current price
     */
    fun calculateReturnRate(currentPrice: Double): Double {
        return if (buyPrice > 0) {
            ((currentPrice - buyPrice) / buyPrice) * 100
        } else {
            0.0
        }
    }

    /**
     * Check if the stock should trigger an alert
     */
    fun shouldTrigger(currentPrice: Double): Boolean {
        if (isTriggered) return false
        
        val currentReturn = calculateReturnRate(currentPrice)
        return currentReturn >= targetReturnRate || currentPrice >= targetPrice
    }

    /**
     * Get display name for the stock
     */
    fun getDisplayName(): String {
        return symbol.uppercase()
    }
}

/**
 * Data class for stock price information
 */
data class StockPrice(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val timestamp: Date = Date()
)

/**
 * Data class for historical price data point
 */
data class HistoricalPrice(
    val date: Date,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

/**
 * Data class for stock statistics
 */
data class StockStatistics(
    val historicalHigh: Double,
    val historicalLow: Double,
    val threeMonthHigh: Double,
    val threeMonthLow: Double,
    val historicalPrices: List<HistoricalPrice>
)
