package com.gwyro.cryptostats.domain.entities


data class CryptoItem(
    val id: Int,
    val name: String?,
    val currency: String?,
    val price: String?,
    var percent_change_24h: Double?,
    val imageUrl: String?,
    val circulating_supply: String?,
    val market_cap: String?,
    val max_supply: String?,
    var favourite: Boolean
)