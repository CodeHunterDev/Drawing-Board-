package com.matrix.drawing.domain.utils

import android.content.Context
import android.content.SharedPreferences

object AppSharedPreference {
    private var app_preference: SharedPreferences? = null
    @JvmStatic
    fun addStringPreference(context: Context, key: String?, value: String?) {
        app_preference = context.getSharedPreferences(AppConstant.APP_SHARED_PREFERENCE, Context.MODE_PRIVATE)
        val editor = app_preference!!.edit()
        editor.putString(key, value)
        editor.apply()
    }

    @JvmStatic
    fun addIntegerPreference(context: Context, key: String?, value: Int) {
        app_preference = context.getSharedPreferences(
            AppConstant.APP_SHARED_PREFERENCE,
            Context.MODE_PRIVATE
        )
        val editor = app_preference!!.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    @JvmStatic
    fun getStringPreference(context: Context, key: String?, default_value: String?): String? {
        app_preference = context.getSharedPreferences(AppConstant.APP_SHARED_PREFERENCE, Context.MODE_PRIVATE)
        return app_preference!!.getString(key, default_value)
    }

    @JvmStatic
    fun getIntegerPreference(context: Context, key: String?, default_value: Int): Int {
        app_preference = context.getSharedPreferences(AppConstant.APP_SHARED_PREFERENCE, Context.MODE_PRIVATE)
        return app_preference!!.getInt(key, default_value)
    }
}