package com.mustfaibra.roffu.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryPref @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "search_history",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val type = object : TypeToken<List<String>>() {}.type

    fun saveSearchHistory(history: List<String>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, json).apply()
    }

    fun getSearchHistory(): List<String> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history_key"
    }
}