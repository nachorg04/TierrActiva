package com.example.empresas_turismo_activo.data.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class AppPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun obtenerModoTema(): Int =
        prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun guardarModoTema(modo: Int) {
        prefs.edit().putInt(KEY_THEME, modo).apply()
    }

    fun obtenerTagIdioma(): String =
        prefs.getString(KEY_LOCALE, "") ?: ""

    fun guardarTagIdioma(tag: String) {
        prefs.edit().putString(KEY_LOCALE, tag).apply()
    }

    fun listaIdiomaParaTag(tag: String): LocaleListCompat =
        if (tag.isEmpty() || tag == "system")
            LocaleListCompat.getEmptyLocaleList()
        else
            LocaleListCompat.forLanguageTags(tag)

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_LOCALE = "app_locale_tag"
    }
}
