package com.mustfaibra.roffu.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore by preferencesDataStore(name = "token_preferences")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val TOKEN_TYPE = stringPreferencesKey("token_type")
    }

    val accessToken: Flow<String?>
        get() = context.tokenDataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    val tokenType: Flow<String?>
        get() = context.tokenDataStore.data.map { preferences ->
            preferences[TOKEN_TYPE]
        }

    suspend fun saveToken(accessToken: String, tokenType: String) {
        context.tokenDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[TOKEN_TYPE] = tokenType
        }
    }

    suspend fun clearToken() {
        context.tokenDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(TOKEN_TYPE)
        }
    }

    suspend fun getAuthHeader(): String? {
        val token = context.tokenDataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
        }
        val type = context.tokenDataStore.data.map { preferences ->
            preferences[TOKEN_TYPE]
        }
        
        return if (token != null && type != null) {
            "$type $token"
        } else {
            null
        }
    }
} 