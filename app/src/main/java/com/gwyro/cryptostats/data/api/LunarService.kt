package com.gwyro.cryptostats.data.api

import com.gwyro.cryptostats.BuildConfig
import com.gwyro.cryptostats.data.model.LunarDayCoin
import com.gwyro.cryptostats.data.model.LunarDetailItem
import com.gwyro.cryptostats.data.model.LunarItem
import com.gwyro.cryptostats.data.model.LunarNewsItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LunarService {
    companion object {
        const val BASE_URL = "https://api.lunarcrush.com/"
    }

    @GET(value = "/v2?data=assets&key=${BuildConfig.API_KEY_LUNAR}")
    suspend fun getCryptoValue(@Query("symbol") symbols: String): Response<LunarItem>

    @GET(value = "/v2?data=feeds&key=${BuildConfig.API_KEY_LUNAR}&sources=news&limit=70")
    suspend fun getCryptoNews(): Response<LunarNewsItem>

    @GET(value = "/v2?data=meta&key=${BuildConfig.API_KEY_LUNAR}&type=full")
    suspend fun getCryptoDetail(@Query("symbol") symbols: String): Response<LunarDetailItem>

    @GET(value = "/v2?data=coinoftheday&key=${BuildConfig.API_KEY_LUNAR}")
    suspend fun getCryptoOfTheDay(): Response<LunarDayCoin>
}