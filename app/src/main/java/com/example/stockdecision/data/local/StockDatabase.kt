package com.example.stockdecision.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.stockdecision.data.model.ApiKeyConfig
import com.example.stockdecision.data.model.EmailConfig
import com.example.stockdecision.data.model.Stock

/**
 * Room database for the stock decision app
 */
@Database(
    entities = [Stock::class, EmailConfig::class, ApiKeyConfig::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StockDatabase : RoomDatabase() {
    
    abstract fun stockDao(): StockDao
    abstract fun emailConfigDao(): EmailConfigDao
    abstract fun apiKeyConfigDao(): ApiKeyConfigDao
    
    companion object {
        @Volatile
        private var INSTANCE: StockDatabase? = null
        
        fun getDatabase(context: Context): StockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockDatabase::class.java,
                    "stock_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
