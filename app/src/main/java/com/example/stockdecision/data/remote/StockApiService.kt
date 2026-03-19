package com.example.stockdecision.data.remote

import com.example.stockdecision.data.remote.dto.GlobalQuoteResponse
import com.example.stockdecision.data.remote.dto.TimeSeriesDailyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Alpha Vantage API Service Interface
 * 
 * Note: Alpha Vantage has rate limits:
 * - Free tier: 5 API calls per minute, 500 API calls per day
 */
interface StockApiService {
    
    /**
     * Get global quote (current price) for a symbol
     * @param symbol Stock symbol (e.g., "IBM", "600036.SH")
     * @param apiKey Alpha Vantage API key
     */
    @GET("query")
    suspend fun getGlobalQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<GlobalQuoteResponse>
    
    /**
     * Get daily time series data for a symbol
     * @param symbol Stock symbol
     * @param apiKey Alpha Vantage API key
     * @param outputSize "compact" (last 100 data points) or "full" (up to 20 years)
     */
    @GET("query")
    suspend fun getTimeSeriesDaily(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
        @Query("outputsize") outputSize: String = "full"
    ): Response<TimeSeriesDailyResponse>
    
    companion object {
        const val BASE_URL = "https://www.alphavantage.co/"
        const val DEFAULT_API_KEY = "YOUR_API_KEY_HERE"
    }
}

/**
 * API Result wrapper for handling success/error states
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}
