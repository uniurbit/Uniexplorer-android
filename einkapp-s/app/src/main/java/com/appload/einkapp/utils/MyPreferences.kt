package com.appload.einkapp.utils

import android.content.Context
import android.content.SharedPreferences

abstract class MyPreferences(private val prefs: String) {
    fun getPrefs(context: Context): SharedPreferences? {
        return context.getSharedPreferences(prefs, Context.MODE_PRIVATE)
    }

}