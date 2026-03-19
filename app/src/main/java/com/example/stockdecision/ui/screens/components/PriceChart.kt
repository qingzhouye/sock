package com.example.stockdecision.ui.screens.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.stockdecision.data.model.HistoricalPrice
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer

/**
 * Price chart component using Vico
 */
@Composable
fun PriceChart(
    historicalPrices: List<HistoricalPrice>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer.build() }
    
    LaunchedEffect(historicalPrices) {
        val prices = historicalPrices.map { it.close }
        modelProducer.tryRunTransaction {
            lineSeries { series(prices) }
        }
    }
    
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(),
        ),
        modelProducer = modelProducer,
        modifier = modifier
    )
}
