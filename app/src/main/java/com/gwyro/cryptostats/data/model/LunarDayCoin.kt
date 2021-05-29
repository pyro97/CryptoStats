package com.gwyro.cryptostats.data.model

data class LunarDayCoin(
    val data: DetailsDayCoinLunar
)

data class DetailsDayCoinLunar(
    val name: String,
    val symbol: String
)