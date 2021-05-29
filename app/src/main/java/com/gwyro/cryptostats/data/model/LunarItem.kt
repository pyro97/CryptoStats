package com.gwyro.cryptostats.data.model

data class LunarItem(
    val data: List<DataLunarItem>
)

data class DataLunarItem(
    val symbol: String,
    val percent_change_24h: Double
)