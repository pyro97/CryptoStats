package com.gwyro.cryptostats.data.db

import javax.inject.Inject


class UserCryptoRepo @Inject constructor(private val dao: UserCryptoDao) {

    suspend fun insertCrypto(cryptoItem: UserCrypto): Long {
        return dao.insertCrypto(cryptoItem)
    }

    suspend fun updateCrypto(cryptoItem: UserCrypto): Int {
        return dao.updateCrypto(cryptoItem)
    }

    suspend fun deleteCrypto(currency: String): Int {
        return dao.deleteCrypto(currency)
    }

    suspend fun getAllCrypto(): List<UserCrypto> {
        return dao.getAllCrypto()
    }
}