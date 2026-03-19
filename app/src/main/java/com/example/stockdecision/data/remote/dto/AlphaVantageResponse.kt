package com.example.stockdecision.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Alpha Vantage Global Quote Response
 */
data class GlobalQuoteResponse(
    @SerializedName("Global Quote")
    val globalQuote: GlobalQuote?
)

data class GlobalQuote(
    @SerializedName("01. symbol")
    val symbol: String?,
    @SerializedName("02. open")
    val open: String?,
    @SerializedName("03. high")
    val high: String?,
    @SerializedName("04. low")
    val low: String?,
    @SerializedName("05. price")
    val price: String?,
    @SerializedName("06. volume")
    val volume: String?,
    @SerializedName("07. latest trading day")
    val latestTradingDay: String?,
    @SerializedName("08. previous close")
    val previousClose: String?,
    @SerializedName("09. change")
    val change: String?,
    @SerializedName("10. change percent")
    val changePercent: String?
)

/**
 * Alpha Vantage Time Series Daily Response
 */
data class TimeSeriesDailyResponse(
    @SerializedName("Meta Data")
    val metaData: MetaData?,
    @SerializedName("Time Series (Daily)")
    val timeSeries: Map<String, TimeSeriesDailyData>?
)

data class MetaData(
    @SerializedName("1. Information")
    val information: String?,
    @SerializedName("2. Symbol")
    val symbol: String?,
    @SerializedName("3. Last Refreshed")
    val lastRefreshed: String?,
    @SerializedName("4. Output Size")
    val outputSize: String?,
    @SerializedName("5. Time Zone")
    val timeZone: String?
)

data class TimeSeriesDailyData(
    @SerializedName("1. open")
    val open: String?,
    @SerializedName("2. high")
    val high: String?,
    @SerializedName("3. low")
    val low: String?,
    @SerializedName("4. close")
    val close: String?,
    @SerializedName("5. volume")
    val volume: String?
)

/**
 * Alpha Vantage API Error Response
 */
data class AlphaVantageError(
    @SerializedName("Note")
    val note: String?,
    @SerializedName("Error Message")
    val errorMessage: String?,
    @SerializedName("Information")
    val information: String?
)
