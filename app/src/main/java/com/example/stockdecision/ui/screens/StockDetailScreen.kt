package com.example.stockdecision.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.stockdecision.R
import com.example.stockdecision.data.model.*
import com.example.stockdecision.ui.screens.components.PriceChart
import com.example.stockdecision.ui.theme.StockUp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.lineSeries
import java.text.SimpleDateFormat
import java.util.*

/**
 * Stock detail screen with chart, statistics, news and AI recommendations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    stock: Stock,
    currentPrice: StockPrice?,
    statistics: StockStatistics?,
    news: List<NewsItem>,
    aiPrediction: AIPrediction?,
    aiRecommendations: List<AIRecommendation>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stock.getDisplayName()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Current Price Card
            CurrentPriceCard(stock, currentPrice)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Price Chart
            if (statistics != null && statistics.historicalPrices.isNotEmpty()) {
                PriceChartSection(statistics.historicalPrices)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statistics
            if (statistics != null) {
                StatisticsSection(statistics)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // AI Prediction
            if (aiPrediction != null) {
                AIPredictionSection(aiPrediction)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // AI Recommendations
            if (aiRecommendations.isNotEmpty()) {
                AIRecommendationsSection(aiRecommendations)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // News Section
            if (news.isNotEmpty()) {
                NewsSection(news)
            }
        }
    }
}

@Composable
private fun CurrentPriceCard(stock: Stock, currentPrice: StockPrice?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stock.getDisplayName(),
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = currentPrice?.let { "%.2f".format(it.price) } ?: "--",
                    style = MaterialTheme.typography.headlineLarge
                )
                
                currentPrice?.let {
                    val returnRate = stock.calculateReturnRate(it.price)
                    Text(
                        text = "%.2f%%".format(returnRate),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (returnRate >= 0) StockUp else MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "买入: ${stock.buyPrice}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "目标: ${stock.targetPrice}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PriceChartSection(historicalPrices: List<HistoricalPrice>) {
    Text(
        text = stringResource(R.string.price_history),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    // Prepare chart data
    val modelProducer = remember { CartesianChartModelProducer.build() }
    
    LaunchedEffect(historicalPrices) {
        val prices = historicalPrices.map { it.close }
        modelProducer.tryRunTransaction {
            lineSeries { series(prices) }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
            ),
            modelProducer = modelProducer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun StatisticsSection(statistics: StockStatistics) {
    Text(
        text = stringResource(R.string.statistics),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    label = stringResource(R.string.historical_high),
                    value = "%.2f".format(statistics.historicalHigh)
                )
                StatisticItem(
                    label = stringResource(R.string.historical_low),
                    value = "%.2f".format(statistics.historicalLow)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    label = stringResource(R.string.three_month_high),
                    value = "%.2f".format(statistics.threeMonthHigh)
                )
                StatisticItem(
                    label = stringResource(R.string.three_month_low),
                    value = "%.2f".format(statistics.threeMonthLow)
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun AIPredictionSection(prediction: AIPrediction) {
    Text(
        text = stringResource(R.string.ai_prediction_title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = prediction.predictionText,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = prediction.confidenceScore.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "置信度: ${(prediction.confidenceScore * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AIRecommendationsSection(recommendations: List<AIRecommendation>) {
    Text(
        text = stringResource(R.string.weekly_recommendations),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    recommendations.forEach { rec ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = rec.symbol,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Surface(
                        color = when (rec.recommendation) {
                            "买入" -> StockUp
                            "观望" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = rec.recommendation,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = rec.reason,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun NewsSection(news: List<NewsItem>) {
    Text(
        text = stringResource(R.string.news_title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    news.forEach { item ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall
                )
                
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp),
                    maxLines = 2
                )
                
                Text(
                    text = "${item.source} · ${formatDate(item.publishDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MM-dd", Locale.getDefault())
    return formatter.format(date)
}
