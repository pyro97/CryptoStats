package com.gwyro.cryptostats.data.model

data class LunarDetailItem(
    val data: List<DetailsLunarItem>
)

data class DetailsLunarItem(
    val short_summary: String,
    val website_link: String
)