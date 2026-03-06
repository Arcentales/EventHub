package com.Arcentales.eventhub.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_prefs")

class ThemePreferences(private val context: Context) {

    companion object {
        private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }

    val dynamicColorEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DYNAMIC_COLOR_KEY] ?: false }

    val darkModeEnabled: Flow<Boolean?> = context.dataStore.data
        .map { preferences -> preferences[DARK_MODE_KEY] }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean?) {
        context.dataStore.edit { preferences ->
            if (enabled == null) {
                preferences.remove(DARK_MODE_KEY)
            } else {
                preferences[DARK_MODE_KEY] = enabled
            }
        }
    }
}
