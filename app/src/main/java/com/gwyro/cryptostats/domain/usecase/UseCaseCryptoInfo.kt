package com.gwyro.cryptostats.domain.usecase

import com.gwyro.cryptostats.domain.repository.LunarServiceRepo
import com.gwyro.cryptostats.domain.repository.NomicsServiceRepo
import javax.inject.Inject

class UseCaseCryptoInfo @Inject constructor(
    private val lunarService: LunarServiceRepo,
    private val nomicsService: NomicsServiceRepo
) {
    suspend fun getCryptoDetail(symbols: String) = lunarService.getCryptoDetail(symbols = symbols)

    suspend fun getCryptoOfTheDay() = lunarService.getCryptoOfTheDay()

    suspend fun getCryptoNews() = lunarService.getCryptoNews()

    suspend fun getCryptoValueLunar(symbols: String) =
        lunarService.getCryptoValue(symbols = symbols)

    suspend fun getCryptoList(convert: String) = nomicsService.getCryptoList(convert = convert)

    suspend fun getCryptoValueNomics(symbols: String, currency: String) =
        nomicsService.getCryptoValue(symbols = symbols, currency = currency)
}