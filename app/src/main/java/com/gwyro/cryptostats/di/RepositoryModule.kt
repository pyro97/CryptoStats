package com.gwyro.cryptostats.di

import com.gwyro.cryptostats.data.storage.SharedPrefrenceStorageImpl
import com.gwyro.cryptostats.domain.storage.SharedPreferenceStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class SharedPreference

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindSharedPreferencesStorage(impl: SharedPrefrenceStorageImpl): SharedPreferenceStorage
}