package com.gwyro.cryptostats.data.repository

import com.gwyro.cryptostats.data.api.LunarService
import com.gwyro.cryptostats.data.model.LunarDayCoin
import com.gwyro.cryptostats.data.model.LunarDetailItem
import com.gwyro.cryptostats.data.model.LunarItem
import com.gwyro.cryptostats.data.model.LunarNewsItem
import com.gwyro.cryptostats.domain.repository.LunarServiceRepo
import com.gwyro.cryptostats.utils.Result
import javax.inject.Inject

class LunarServiceRepoImpl @Inject constructor(private val lunarService: LunarService) :
    LunarServiceRepo {

    override suspend fun getCryptoValue(symbols: String): Result<LunarItem> {
        val result = lunarService.getCryptoValue(symbols)
        if (result.isSuccessful && !result.body()?.data.isNullOrEmpty()) {
            return Result.Success(result.body()!!)
        }
        return Result.Failed(Exception())
    }

    override suspend fun getCryptoNews(): Result<LunarNewsItem> {
        val result = lunarService.getCryptoNews()
        if (result.isSuccessful && !result.body()?.data.isNullOrEmpty()) {
            return Result.Success(result.body()!!)
        }
        return Result.Failed(Exception())
    }

    override suspend fun getCryptoDetail(symbols: String): Result<LunarDetailItem> {
        val result = lunarService.getCryptoDetail(symbols)
        if (result.isSuccessful && !result.body()?.data.isNullOrEmpty()) {
            return Result.Success(result.body()!!)
        }
        return Result.Failed(Exception())
    }

    override suspend fun getCryptoOfTheDay(): Result<LunarDayCoin> {
        val result = lunarService.getCryptoOfTheDay()
        if (result.isSuccessful && result.body()?.data != null) {
            return Result.Success(result.body()!!)
        }
        return Result.Failed(Exception())
    }

}