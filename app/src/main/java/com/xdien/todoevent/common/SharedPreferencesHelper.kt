package com.xdien.todoevent.common

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    fun saveApiUrl(url: String) {
        sharedPreferences.edit().putString(KEY_API_URL, url).apply()
    }

    fun getApiUrl(): String {
        return sharedPreferences.getString(KEY_API_URL, DEFAULT_API_URL) ?: DEFAULT_API_URL
    }

    companion object {
        private const val PREF_NAME = "todo_event_prefs"
        private const val KEY_API_URL = "api_url"
        private const val DEFAULT_API_URL = "http://192.168.31.194:5000" // Default mock server URL
    }
} 