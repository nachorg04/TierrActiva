package com.example.empresas_turismo_activo.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.listUiDataStore by preferencesDataStore(name = "list_ui")

/** Preferencias locales de filtros de lista y modo de vista. */
data class ListPersistedState(
    val nombreFilter: String = "",
    val localidadFilter: String = "",
    val categoriaFiltro: String? = null,
    val preferGridOnMobile: Boolean = false,
    val proximitySortSelected: Boolean = false,
)

class ListPreferences(context: Context) {

    private val appContext = context.applicationContext

    suspend fun load(): ListPersistedState {
        val p = appContext.listUiDataStore.data.first()
        return ListPersistedState(
            nombreFilter = p[KEY_FILTER_NOMBRE] ?: "",
            localidadFilter = p[KEY_FILTER_LOCALIDAD] ?: "",
            categoriaFiltro = p[KEY_FILTER_CATEGORIA]?.takeUnless { it.isBlank() },
            preferGridOnMobile = p[KEY_PREF_GRID_MOBILE] ?: false,
            proximitySortSelected = p[KEY_PROXIMITY_SORT] ?: false,
        )
    }

    suspend fun save(state: ListPersistedState) {
        appContext.listUiDataStore.edit { prefs ->
            prefs[KEY_FILTER_NOMBRE] = state.nombreFilter
            prefs[KEY_FILTER_LOCALIDAD] = state.localidadFilter
            prefs[KEY_FILTER_CATEGORIA] = state.categoriaFiltro.orEmpty()
            prefs[KEY_PREF_GRID_MOBILE] = state.preferGridOnMobile
            prefs[KEY_PROXIMITY_SORT] = state.proximitySortSelected
        }
    }

    companion object {
        private val KEY_FILTER_NOMBRE = stringPreferencesKey("filter_nombre")
        private val KEY_FILTER_LOCALIDAD = stringPreferencesKey("filter_localidad")
        private val KEY_FILTER_CATEGORIA = stringPreferencesKey("filter_categoria")
        private val KEY_PREF_GRID_MOBILE = booleanPreferencesKey("pref_grid_mobile")
        private val KEY_PROXIMITY_SORT = booleanPreferencesKey("proximity_sort")
    }
}
