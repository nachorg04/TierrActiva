package com.example.empresas_turismo_activo.data.preferences

import android.content.Context
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.localeUiDataStore by preferencesDataStore(name = "locale_ui")

/** Persistencia del idioma de aplicación (etiqueta interna: system, es, en). */
class LocalePreferences(context: Context) {

    private val appContext = context.applicationContext

    suspend fun getLocaleTag(): String =
        appContext.localeUiDataStore.data
            .map { prefs -> prefs[KEY_LOCALE_TAG] ?: TAG_SYSTEM }
            .first()

    suspend fun setLocaleTag(tag: String) {
        appContext.localeUiDataStore.edit { prefs ->
            prefs[KEY_LOCALE_TAG] = tag
        }
    }

    fun localeListForTag(tag: String): LocaleListCompat =
        when (tag) {
            TAG_ES -> LocaleListCompat.forLanguageTags("es")
            TAG_EN -> LocaleListCompat.forLanguageTags("en")
            else -> LocaleListCompat.getEmptyLocaleList()
        }

    companion object {
        const val TAG_SYSTEM = "system"
        const val TAG_ES = "es"
        const val TAG_EN = "en"

        private val KEY_LOCALE_TAG = stringPreferencesKey("app_locale_tag")
    }
}
