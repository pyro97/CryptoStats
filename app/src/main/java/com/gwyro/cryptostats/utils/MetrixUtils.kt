package com.gwyro.cryptostats.utils

import android.content.Context
import com.gwyro.cryptostats.R
import java.text.NumberFormat
import java.util.*

object MetrixUtils {
        fun convertDoubleToCurrency(value: Double, currency: String): String {
        var locale: Locale = Locale.US
        if (currency == "EUR") {
            locale = Locale.ITALY
        }
        val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(locale)
            var maxFraction = 2
            try {
                if(value.toString().contains("E")){
                    maxFraction = 6
                } else if(value <= 0.1){
                    var s = value.toString().replace(".","")
                    var i = 0
                    while (i < s.length && s[i] == '0'){
                        i++
                    }
                    maxFraction = i
                }
            } catch (e: Exception){
                maxFraction = 2
            }
            numberFormat.maximumFractionDigits = maxFraction
            return numberFormat.format(value)
    }

    fun convertDoubleToCurrencyString(context: Context, value: String): String {
        return when (value.length) {
            4 -> value.substring(0, 1) + " ${context.getString(R.string.k_value)}"
            5 -> value.substring(0, 1) + " ${context.getString(R.string.k_value)}"
            6 -> value.substring(0, 1) + " ${context.getString(R.string.k_value)}"
            7 -> value.substring(0, 1) + " ${context.getString(R.string.million_value)}"
            8 -> value.substring(0, 2) + " ${context.getString(R.string.million_value)}"
            9 -> value.substring(0, 3) + " ${context.getString(R.string.million_value)}"
            10 -> value.substring(0, 1) + " ${context.getString(R.string.billion_value)}"
            11 -> value.substring(0, 2) + " ${context.getString(R.string.billion_value)}"
            12 -> value.substring(0, 3) + " ${context.getString(R.string.billion_value)}"
            13 -> value.substring(0, 1) + " ${context.getString(R.string.ultra_billion_value)}"
            14 -> value.substring(0, 2) + " ${context.getString(R.string.ultra_billion_value)}"
            15 -> value.substring(0, 3) + " ${context.getString(R.string.ultra_billion_value)}"
            16 -> value.substring(0, 1) + " ${context.getString(R.string.ultra1_billion_value)}"
            17 -> value.substring(0, 2) + " ${context.getString(R.string.ultra1_billion_value)}"
            18 -> value.substring(0, 3) + " ${context.getString(R.string.ultra1_billion_value)}"
            else -> {
                return if(value.length > 18){
                    context.getString(R.string.none)
                } else {
                    value
                }
            }
        }
    }
}