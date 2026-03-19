package com.example.stockdecision.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stockdecision.R
import com.example.stockdecision.data.model.Stock

/**
 * Stock input form component
 */
@Composable
fun StockInputForm(
    onAddStock: (Stock) -> Unit,
    modifier: Modifier = Modifier
) {
    var symbol by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var targetReturn by remember { mutableStateOf("10") }
    var targetPrice by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.stock_input_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Stock Symbol
        OutlinedTextField(
            value = symbol,
            onValueChange = { symbol = it.uppercase() },
            label = { Text(stringResource(R.string.stock_symbol)) },
            placeholder = { Text(stringResource(R.string.stock_symbol_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Buy Price and Quantity row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = buyPrice,
                onValueChange = { buyPrice = it },
                label = { Text(stringResource(R.string.buy_price)) },
                placeholder = { Text(stringResource(R.string.buy_price_hint)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )
            
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text(stringResource(R.string.quantity)) },
                placeholder = { Text(stringResource(R.string.quantity_hint)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Target Return and Target Price row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = targetReturn,
                onValueChange = { targetReturn = it },
                label = { Text(stringResource(R.string.target_return)) },
                placeholder = { Text(stringResource(R.string.target_return_hint)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )
            
            OutlinedTextField(
                value = targetPrice,
                onValueChange = { targetPrice = it },
                label = { Text(stringResource(R.string.target_price)) },
                placeholder = { Text(stringResource(R.string.target_price_hint)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add Button
        Button(
            onClick = {
                val stock = createStockFromInput(
                    symbol = symbol,
                    buyPrice = buyPrice,
                    quantity = quantity,
                    targetReturn = targetReturn,
                    targetPrice = targetPrice
                )
                if (stock != null) {
                    onAddStock(stock)
                    // Clear form
                    symbol = ""
                    buyPrice = ""
                    quantity = ""
                    targetReturn = "10"
                    targetPrice = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = symbol.isNotBlank() && buyPrice.isNotBlank() && targetPrice.isNotBlank()
        ) {
            Text(stringResource(R.string.start_monitoring))
        }
    }
}

private fun createStockFromInput(
    symbol: String,
    buyPrice: String,
    quantity: String,
    targetReturn: String,
    targetPrice: String
): Stock? {
    return try {
        Stock(
            symbol = symbol.trim(),
            buyPrice = buyPrice.toDoubleOrNull() ?: return null,
            quantity = quantity.toDoubleOrNull() ?: 0.0,
            targetReturnRate = targetReturn.toDoubleOrNull() ?: 10.0,
            targetPrice = targetPrice.toDoubleOrNull() ?: return null
        )
    } catch (e: Exception) {
        null
    }
}
