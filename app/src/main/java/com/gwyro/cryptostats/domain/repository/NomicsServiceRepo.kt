package com.gwyro.cryptostats.domain.repository

import com.gwyro.cryptostats.data.model.NomicsItem
import com.gwyro.cryptostats.utils.Result

interface NomicsServiceRepo {

    suspend fun getCryptoValue(symbols: String, currency: String): Result<List<NomicsItem>>

    suspend fun getCryptoList(convert: String): Result<List<NomicsItem>>
}