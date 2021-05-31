package com.gwyro.cryptostats.di

import com.gwyro.cryptostats.data.api.LunarService
import com.gwyro.cryptostats.data.api.NomicsService
import com.gwyro.cryptostats.data.repository.LunarServiceRepoImpl
import com.gwyro.cryptostats.data.repository.NomicsServiceRepoImpl
import com.gwyro.cryptostats.domain.repository.LunarServiceRepo
import com.gwyro.cryptostats.domain.repository.NomicsServiceRepo
import com.gwyro.cryptostats.utils.RETROFIT_LUNAR
import com.gwyro.cryptostats.utils.RETROFIT_NOMICS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideOkHttpClient() =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Singleton
    @Provides
    @Named(RETROFIT_LUNAR)
    fun provideRetrofitLunar(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(LunarService.BASE_URL)
        .client(okHttpClient)
        .build()


    @Singleton
    @Provides
    @Named(RETROFIT_NOMICS)
    fun provideRetrofitNomics(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(NomicsService.BASE_URL)
        .client(okHttpClient)
        .build()


    @Provides
    @Singleton
    fun provideLunarService(@Named(RETROFIT_LUNAR) retrofit: Retrofit): LunarService = retrofit.create(
        LunarService::class.java)

    @Provides
    @Singleton
    fun provideLunarServiceRepo(lunarServiceRepoImpl: LunarServiceRepoImpl): LunarServiceRepo = lunarServiceRepoImpl

    @Provides
    @Singleton
    fun provideNomicsService(@Named(RETROFIT_NOMICS) retrofit: Retrofit): NomicsService = retrofit.create(
        NomicsService::class.java)

    @Provides
    @Singleton
    fun provideNomicsServiceRepo(nomicsServiceRepoImpl: NomicsServiceRepoImpl): NomicsServiceRepo = nomicsServiceRepoImpl

    }
