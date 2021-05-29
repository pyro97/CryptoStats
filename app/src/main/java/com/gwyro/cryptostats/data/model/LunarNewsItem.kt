package com.gwyro.cryptostats.data.model

data class LunarNewsItem(
    val data: List<DataNewsLunarItem>
)

data class DataNewsLunarItem(
    val title: String,
    val thumbnail: String,
    val image: String,
    val description: String,
    val url: String,
    val name: String
)