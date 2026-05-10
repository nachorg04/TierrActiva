package com.example.empresas_turismo_activo.data.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.nightModeDataStore by preferencesDataStore(name = "night_mode")

/** Preferencia claro/oscuro (no sigue al sistema: el usuario elige con la luna). */
class NightModePreferences(context: Context) {

    private val appContext = context.applicationContext

    suspend fun isDarkTheme(): Boolean =
        appContext.nightModeDataStore.data
            .map { prefs -> prefs[KEY_DARK] ?: false }
            .first()

    suspend fun setDarkTheme(enabled: Boolean) {
        appContext.nightModeDataStore.edit { prefs ->
            prefs[KEY_DARK] = enabled
        }
    }

    fun nightModeConstant(isDark: Boolean): Int =
        if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

    companion object {
        private val KEY_DARK = booleanPreferencesKey("dark_theme_enabled")
    }
}
