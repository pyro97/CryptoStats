package com.gwyro.cryptostats.utils

import android.content.Context
import android.net.ConnectivityManager

object Utils {

    fun editLenghtTitle(value: String): String {
        if (value.length > 12) {
            var newValue = value.substring(0, 12)
            if (newValue.contains(" ")) {
                newValue = newValue.substring(0, newValue.indexOf(" "))
            }
            return newValue
        }
        return value
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val conManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val internetInfo = conManager.activeNetworkInfo
        return internetInfo != null && internetInfo.isConnected
    }
}