package com.gwyro.cryptostats.data.model

data class LunarItem(
    val data: MutableList<DataLunarItem>
)

data class DataLunarItem(
    val symbol: String,
    val percent_change_24h: Double
)