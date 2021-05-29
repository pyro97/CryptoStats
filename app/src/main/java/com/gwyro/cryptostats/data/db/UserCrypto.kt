package com.gwyro.cryptostats.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_table")
data class UserCrypto(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "crypto_id")
    val id: Int,

    @ColumnInfo(name = "crypto_name")
    val name: String,

    @ColumnInfo(name = "crypto_currency")
    val currency: String,

    @ColumnInfo(name = "crypto_fav")
    val isFavourite: Boolean
)