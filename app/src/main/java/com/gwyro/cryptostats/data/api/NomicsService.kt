package com.gwyro.cryptostats.data.api

import com.gwyro.cryptostats.BuildConfig
import com.gwyro.cryptostats.data.model.NomicsItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NomicsService {
    companion object {
        const val BASE_URL = "https://api.nomics.com/"
    }

    @GET(value = "/v1/currencies/ticker?key=${BuildConfig.API_KEY_NOMICS}&page=1&interval=1h")
    suspend fun getCryptoValue(
        @Query("ids") symbols: String,
        @Query("convert") currency: String
    ): Response<List<NomicsItem>>

    @GET(value = "/v1/currencies/ticker?key=${BuildConfig.API_KEY_NOMICS}&per-page=100&page=1&interval=1h")
    suspend fun getCryptoList(@Query("convert") convert: String): Response<List<NomicsItem>>
}