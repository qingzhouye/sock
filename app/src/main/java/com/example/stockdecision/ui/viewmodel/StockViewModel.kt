package com.example.stockdecision.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdecision.data.local.StockDatabase
import com.example.stockdecision.data.model.*
import com.example.stockdecision.data.remote.ApiResult
import com.example.stockdecision.data.repository.StockRepository
import com.example.stockdecision.service.StockMonitorService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

/**
 * UI State for Stock screen
 */
data class StockUiState(
    val stocks: List<Stock> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * UI State for Stock Detail screen
 */
data class StockDetailUiState(
    val stock: Stock? = null,
    val currentPrice: StockPrice? = null,
    val statistics: StockStatistics? = null,
    val news: List<NewsItem> = emptyList(),
    val aiPrediction: AIPrediction? = null,
    val aiRecommendations: List<AIRecommendation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for stock-related operations
 */
class StockViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: StockRepository
    
    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()
    
    private val _detailUiState = MutableStateFlow(StockDetailUiState())
    val detailUiState: StateFlow<StockDetailUiState> = _detailUiState.asStateFlow()
    
    // Cache for current prices
    private val _currentPrices = MutableStateFlow<Map<String, StockPrice>>(emptyMap())
    val currentPrices: StateFlow<Map<String, StockPrice>> = _currentPrices.asStateFlow()
    
    init {
        val database = StockDatabase.getDatabase(application)
        repository = StockRepository(database.stockDao(), application)
        
        // Collect stocks from database
        viewModelScope.launch {
            repository.getAllStocks().collect { stocks ->
                _uiState.update { it.copy(stocks = stocks) }
                // Refresh prices for all stocks
                refreshPrices(stocks)
            }
        }
    }
    
    /**
     * Add a new stock to monitor
     */
    fun addStock(stock: Stock) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            when (val result = repository.addStock(stock)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    // Start monitoring service
                    StockMonitorService.start(getApplication())
                }
                is Result.Failure -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = result.exceptionOrNull()?.message ?: "添加失败"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Delete a stock
     */
    fun deleteStock(stock: Stock) {
        viewModelScope.launch {
            repository.deleteStock(stock)
            // Remove from price cache
            _currentPrices.update { prices ->
                prices - stock.symbol.uppercase()
            }
            // Check if we need to stop service
            val activeCount = repository.getActiveStockCount()
            if (activeCount == 0) {
                StockMonitorService.stop(getApplication())
            }
        }
    }
    
    /**
     * Refresh prices for all stocks
     */
    private fun refreshPrices(stocks: List<Stock>) {
        viewModelScope.launch {
            stocks.forEach { stock ->
                if (!stock.isTriggered) {
                    when (val result = repository.getCurrentPrice(stock.symbol)) {
                        is ApiResult.Success -> {
                            _currentPrices.update { prices ->
                                prices + (stock.symbol.uppercase() to result.data)
                            }
                            
                            // Check if should trigger
                            if (stock.shouldTrigger(result.data.price)) {
                                handleStockTriggered(stock, result.data.price)
                            }
                        }
                        else -> { /* Ignore errors */ }
                    }
                }
            }
        }
    }
    
    /**
     * Handle stock trigger
     */
    private suspend fun handleStockTriggered(stock: Stock, currentPrice: Double) {
        repository.markAsTriggered(stock.id)
    }
    
    /**
     * Load stock details
     */
    fun loadStockDetail(stock: Stock) {
        viewModelScope.launch {
            _detailUiState.update { 
                it.copy(
                    stock = stock,
                    isLoading = true,
                    errorMessage = null
                )
            }
            
            // Get current price
            val currentPrice = _currentPrices.value[stock.symbol.uppercase()]
            
            // Get statistics
            when (val statsResult = repository.getStockStatistics(stock.symbol)) {
                is ApiResult.Success -> {
                    _detailUiState.update {
                        it.copy(
                            currentPrice = currentPrice,
                            statistics = statsResult.data,
                            news = generateMockNews(stock.symbol),
                            aiPrediction = generateMockAIPrediction(stock.symbol),
                            aiRecommendations = generateMockAIRecommendations(),
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _detailUiState.update {
                        it.copy(
                            currentPrice = currentPrice,
                            news = generateMockNews(stock.symbol),
                            aiPrediction = generateMockAIPrediction(stock.symbol),
                            aiRecommendations = generateMockAIRecommendations(),
                            isLoading = false,
                            errorMessage = statsResult.message
                        )
                    }
                }
                else -> { }
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
        _detailUiState.update { it.copy(errorMessage = null) }
    }
    
    // Mock data generators
    private fun generateMockNews(symbol: String): List<NewsItem> {
        return listOf(
            NewsItem(
                id = "1",
                title = "$symbol 发布最新财报，业绩超预期",
                summary = "公司第三季度营收同比增长15%，净利润增长20%，市场反应积极。",
                source = "财经网",
                publishDate = Date(),
                relatedSymbols = listOf(symbol)
            ),
            NewsItem(
                id = "2",
                title = "分析师上调 $symbol 目标价",
                summary = "多家投行看好公司未来发展前景，上调目标价至新高。",
                source = "证券时报",
                publishDate = Date(System.currentTimeMillis() - 86400000),
                relatedSymbols = listOf(symbol)
            ),
            NewsItem(
                id = "3",
                title = "$symbol 宣布新的投资计划",
                summary = "公司计划在未来三年内投资100亿元用于技术研发和市场拓展。",
                source = "第一财经",
                publishDate = Date(System.currentTimeMillis() - 172800000),
                relatedSymbols = listOf(symbol)
            )
        )
    }
    
    private fun generateMockAIPrediction(symbol: String): AIPrediction {
        return AIPrediction(
            symbol = symbol,
            predictionText = "基于模型分析，$symbol 一周内上涨概率 65%",
            trend = TrendDirection.UP,
            confidenceScore = 0.65
        )
    }
    
    private fun generateMockAIRecommendations(): List<AIRecommendation> {
        return listOf(
            AIRecommendation(
                symbol = "AAPL",
                recommendation = "买入",
                confidence = 0.75,
                reason = "技术面突破，基本面稳健",
                targetPrice = 185.0,
                stopLossPrice = 165.0
            ),
            AIRecommendation(
                symbol = "MSFT",
                recommendation = "买入",
                confidence = 0.80,
                reason = "AI业务增长强劲",
                targetPrice = 420.0,
                stopLossPrice = 380.0
            ),
            AIRecommendation(
                symbol = "NVDA",
                recommendation = "观望",
                confidence = 0.55,
                reason = "估值偏高，等待回调",
                targetPrice = 550.0,
                stopLossPrice = 480.0
            )
        )
    }
}
