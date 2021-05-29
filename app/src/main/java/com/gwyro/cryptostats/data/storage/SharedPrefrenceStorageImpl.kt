package com.gwyro.cryptostats.data.storage

import android.content.Context
import androidx.preference.PreferenceManager
import com.gwyro.cryptostats.domain.storage.SharedPreferenceStorage
import com.gwyro.cryptostats.utils.KEY_CURRENCIES
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPrefrenceStorageImpl @Inject constructor(@ApplicationContext private val context: Context) :
    SharedPreferenceStorage {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun putString(key: String, value: String?) {
        if (value.isNullOrEmpty()) {
            sharedPreferences.edit().remove(key).apply()
        } else {
            sharedPreferences.edit()
                .putString(key, value)
                .apply()
        }
    }

    override fun putBoolean(key: String, value: Boolean?) {
        if (value == null) {
            sharedPreferences.edit().remove(key).apply()
        } else {
            sharedPreferences.edit()
                .putBoolean(key, value)
                .apply()
        }
    }

    override fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, true)
    }

    override fun checkDefaultValues() {
        if (getString(KEY_CURRENCIES).isNullOrEmpty()) {
            putString(KEY_CURRENCIES, "USD")
        }
    }

}