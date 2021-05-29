package com.gwyro.cryptostats.domain.repository

import com.gwyro.cryptostats.data.model.LunarDayCoin
import com.gwyro.cryptostats.data.model.LunarDetailItem
import com.gwyro.cryptostats.data.model.LunarItem
import com.gwyro.cryptostats.data.model.LunarNewsItem
import com.gwyro.cryptostats.utils.Result

interface LunarServiceRepo {
    suspend fun getCryptoValue(symbols: String): Result<LunarItem>

    suspend fun getCryptoNews(): Result<LunarNewsItem>

    suspend fun getCryptoDetail(symbols: String): Result<LunarDetailItem>

    suspend fun getCryptoOfTheDay(): Result<LunarDayCoin>

}