package com.gwyro.cryptostats.di

import android.content.Context
import androidx.room.Room
import com.gwyro.cryptostats.data.db.UserCryptoDao
import com.gwyro.cryptostats.data.db.UserCryptoDatabase
import com.gwyro.cryptostats.utils.DB_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    fun provideChannelDao(appDatabase: UserCryptoDatabase): UserCryptoDao {
        return appDatabase.userCryptoDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): UserCryptoDatabase {
        return Room.databaseBuilder(
            appContext,
            UserCryptoDatabase::class.java,
            DB_NAME
        ).build()
    }
}