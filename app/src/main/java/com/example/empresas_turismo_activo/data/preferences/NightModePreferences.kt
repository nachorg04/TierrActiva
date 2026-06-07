package com.example.empresas_turismo_activo.data.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.nightModeDataStore by preferencesDataStore(name = "night_mode")
class NightModePreferences(private val context: Context) {

    private val PREFERENCIA = intPreferencesKey("theme_mode_preference");

    suspend fun setThemeMode(mode: Int) {
        context.nightModeDataStore.edit { preferences ->
            preferences[PREFERENCIA] = mode
        }
    }
    fun getModoTema(): Flow<Int> {
        return context.nightModeDataStore.data.map { preferences ->
            preferences[PREFERENCIA] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

        }
    }

}
