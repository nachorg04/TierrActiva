package com.example.empresas_turismo_activo.data.preferences

import android.content.Context
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.localeUiDataStore by preferencesDataStore(name = "locale_ui")

class LocalePreferences(private val context: Context) {

    private val TAG_LOCAL = stringPreferencesKey("app_locale_tag")

    // 1. LEER: Devuelve un Flow<String> continuo (igual que en NightModePreferences)
    fun getLocaleTag(): Flow<String> {
        return context.localeUiDataStore.data.map { preferences ->
            // Si no hay nada guardado, devolvemos un texto vacío ("") que significa "Sistema"
            preferences[TAG_LOCAL] ?: ""
        }
    }

    // 2. GUARDAR: Guarda el texto que le pasemos ("es", "en" o "")
    suspend fun setLocaleTag(tag: String) {
        context.localeUiDataStore.edit { preferences ->
            preferences[TAG_LOCAL] = tag
        }
    }

    // 3. TRADUCIR: Convierte nuestro texto simple en el formato complejo que pide Android
    fun localeListForTag(tag: String): LocaleListCompat {
        return if (tag.isEmpty() || tag == "system") {
            LocaleListCompat.getEmptyLocaleList() // Idioma del dispositivo
        } else {
            LocaleListCompat.forLanguageTags(tag) // Aplica "es" o "en"
        }
    }
}
