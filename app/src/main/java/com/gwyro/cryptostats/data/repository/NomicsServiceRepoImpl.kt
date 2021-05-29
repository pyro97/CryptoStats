package com.gwyro.cryptostats.data.repository

import com.gwyro.cryptostats.data.api.NomicsService
import com.gwyro.cryptostats.data.model.NomicsItem
import com.gwyro.cryptostats.domain.repository.NomicsServiceRepo
import com.gwyro.cryptostats.utils.Result
import javax.inject.Inject

class NomicsServiceRepoImpl @Inject constructor(private val nomicsService: NomicsService) :
    NomicsServiceRepo {

    override suspend fun getCryptoValue(
        symbols: String,
        currency: String
    ): Result<List<NomicsItem>> {
        val result = nomicsService.getCryptoValue(symbols, currency)
        if (result.isSuccessful && !result.body().isNullOrEmpty()) {
            return Result.Success(result.body()!!)
        }
        return Result.Failed(Exception())
    }

    override suspend fun getCryptoList(convert: String): Result<List<NomicsItem>> {
        val result = nomicsService.getCryptoList(convert)
        if (result.isSuccessful && !result.body().isNullOrEmpty()) {
            return Result.Success(result.body()!!)
        }
        return Result.Failed(Exception())
    }
}