package com.example.stockdecision.data.local

import androidx.room.*
import com.example.stockdecision.data.model.Stock
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for Stock entity
 */
@Dao
interface StockDao {
    
    @Query("SELECT * FROM stocks ORDER BY createdAt DESC")
    fun getAllStocks(): Flow<List<Stock>>
    
    @Query("SELECT * FROM stocks WHERE isTriggered = 0 ORDER BY createdAt DESC")
    fun getActiveStocks(): Flow<List<Stock>>
    
    @Query("SELECT * FROM stocks WHERE isTriggered = 0")
    suspend fun getActiveStocksSync(): List<Stock>
    
    @Query("SELECT * FROM stocks WHERE id = :id")
    suspend fun getStockById(id: Long): Stock?
    
    @Query("SELECT * FROM stocks WHERE symbol = :symbol LIMIT 1")
    suspend fun getStockBySymbol(symbol: String): Stock?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: Stock): Long
    
    @Update
    suspend fun updateStock(stock: Stock)
    
    @Delete
    suspend fun deleteStock(stock: Stock)
    
    @Query("DELETE FROM stocks WHERE id = :id")
    suspend fun deleteStockById(id: Long)
    
    @Query("UPDATE stocks SET isTriggered = 1, triggeredAt = :triggeredAt WHERE id = :id")
    suspend fun markAsTriggered(id: Long, triggeredAt: Date = Date())
    
    @Query("SELECT COUNT(*) FROM stocks WHERE isTriggered = 0")
    suspend fun getActiveStockCount(): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM stocks WHERE symbol = :symbol AND isTriggered = 0)")
    suspend fun isStockBeingMonitored(symbol: String): Boolean
}
