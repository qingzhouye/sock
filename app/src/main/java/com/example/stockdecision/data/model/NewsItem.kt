package com.example.stockdecision.data.model

import java.util.Date

/**
 * News item data class for financial news
 */
data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val publishDate: Date,
    val url: String? = null,
    val relatedSymbols: List<String> = emptyList()
)

/**
 * AI recommendation data class
 */
data class AIRecommendation(
    val symbol: String,
    val recommendation: String,  // e.g., "买入", "持有", "观望"
    val confidence: Double,      // 0.0 - 1.0
    val reason: String,
    val targetPrice: Double?,
    val stopLossPrice: Double?
)

/**
 * AI prediction data class for a specific stock
 */
data class AIPrediction(
    val symbol: String,
    val predictionText: String,  // e.g., "基于模型分析，一周内上涨概率 65%"
    val trend: TrendDirection,
    val confidenceScore: Double
)

enum class TrendDirection {
    UP, DOWN, NEUTRAL
}
