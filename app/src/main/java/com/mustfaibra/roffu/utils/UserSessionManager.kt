package com.mustfaibra.roffu.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun logout() {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                prefs.remove(LOGGED_USER_ID)
            }
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[LOGGED_USER_ID] != null
    }
}
