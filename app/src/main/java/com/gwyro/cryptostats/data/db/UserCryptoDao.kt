package com.gwyro.cryptostats.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserCryptoDao {

    @Insert
    suspend fun insertCrypto(cryptoItem: UserCrypto): Long

    @Update
    suspend fun updateCrypto(cryptoItem: UserCrypto): Int

    @Query("DELETE FROM CRYPTO_TABLE WHERE crypto_currency = :currency")
    suspend fun deleteCrypto(currency: String): Int

    @Query("SELECT * FROM CRYPTO_TABLE")
    suspend fun getAllCrypto() : List<UserCrypto>
}