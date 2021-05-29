package com.gwyro.cryptostats.domain.storage

interface SharedPreferenceStorage {
    fun putString(key: String, value: String?)
    fun putBoolean(key: String, value: Boolean?)
    fun getString(key: String): String?
    fun getBoolean(key: String): Boolean
    fun checkDefaultValues()
}