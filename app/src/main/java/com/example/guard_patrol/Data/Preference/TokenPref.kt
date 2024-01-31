package com.example.guard_patrol.Data.Preference

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class TokenPref (context: Context) {
    private val prefName = "MyToken"
    private var preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences(prefName, MODE_PRIVATE)
    }

    fun getPreferences(): String{
        return preferences.getString(prefName, "") ?: ""
    }

    fun setPreferences(value:String){
        preferences.edit().putString(prefName, value).apply()
    }

    fun clearData() {
        preferences.edit().clear().apply()
    }
}