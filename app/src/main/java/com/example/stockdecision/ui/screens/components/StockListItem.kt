package com.example.stockdecision.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.stockdecision.R
import com.example.stockdecision.data.model.Stock
import com.example.stockdecision.data.model.StockPrice
import com.example.stockdecision.ui.theme.StockUp
import com.example.stockdecision.ui.theme.SuccessGreen
import com.example.stockdecision.ui.theme.WarningYellow

/**
 * Stock list item component
 */
@Composable
fun StockListItem(
    stock: Stock,
    currentPrice: StockPrice?,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val returnRate = currentPrice?.let { stock.calculateReturnRate(it.price) } ?: 0.0
    val isTriggered = stock.isTriggered || (currentPrice != null && stock.shouldTrigger(currentPrice.price))
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTriggered) 
                WarningYellow.copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stock.getDisplayName(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "买入价: ${stock.buyPrice}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status Badge
                Surface(
                    color = if (isTriggered) WarningYellow else SuccessGreen,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (isTriggered) 
                            stringResource(R.string.status_triggered) 
                        else 
                            stringResource(R.string.status_monitoring),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Price and Return Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = currentPrice?.let { 
                            stringResource(R.string.current_price, it.price)
                        } ?: "加载中...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (currentPrice != null) {
                        Text(
                            text = stringResource(R.string.current_return, returnRate),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (returnRate >= 0) StockUp else MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.target_return_label, stock.targetReturnRate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.target_price_label, stock.targetPrice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
