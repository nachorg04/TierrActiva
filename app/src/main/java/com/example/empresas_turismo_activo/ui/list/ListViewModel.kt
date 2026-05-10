package com.example.empresas_turismo_activo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.empresas_turismo_activo.data.preferences.ListPersistedState
import com.example.empresas_turismo_activo.domain.model.Empresa
import com.example.empresas_turismo_activo.domain.model.isLikelyGeocoded
import com.example.empresas_turismo_activo.domain.repository.EmpresaRepository
import com.example.empresas_turismo_activo.util.GeoDistance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi

enum class ListaSortMode { ALPHABETIC, BY_PROXIMITY }

/**
 * Lista desde Room: la búsqueda y localidad se resuelven con SQL en DAO/repositorio; aquí sólo ordenación.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModel(
    private val repository: EmpresaRepository,
    performInitialSync: Boolean = true,
) : ViewModel() {

    init {
        if (performInitialSync) {
            viewModelScope.launch {
                repository.syncEmpresas()
            }
        }
    }

    private val nombreFilter = MutableStateFlow("")
    private val localidadFilter = MutableStateFlow("")
    private val sortMode = MutableStateFlow(ListaSortMode.ALPHABETIC)
    /**
     * Última posición conocida del usuario desde Fused Location; sólo ordena cuando [sortMode]
     * es [ListaSortMode.BY_PROXIMITY] y aquí hay un par finito lat/lng.
     */
    private val userLatLng = MutableStateFlow<Pair<Double, Double>?>(null)
    private val _preferGridOnMobile = MutableStateFlow(false)
    val preferGridOnMobile: StateFlow<Boolean> = _preferGridOnMobile.asStateFlow()

    val empresas: StateFlow<List<Empresa>> = combine(nombreFilter, localidadFilter) { nombre, loc ->
        nombre to loc
    }.distinctUntilChanged()
        .flatMapLatest { (nombre, loc) ->
            combine(
                repository.observeFilteredEmpresas(nombre, loc),
                sortMode,
                userLatLng,
            ) { listadoFiltrado, mode, ubicacionUsuario ->
                ordenarLista(listadoFiltrado, mode, ubicacionUsuario)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    /** Restaura filtros persistidos antes de registrar listeners textuales. */
    fun restorePersisted(snapshot: ListPersistedState) {
        nombreFilter.value = snapshot.nombreFilter
        localidadFilter.value = snapshot.localidadFilter
        _preferGridOnMobile.value = snapshot.preferGridOnMobile
        sortMode.value =
            if (snapshot.proximitySortSelected) ListaSortMode.BY_PROXIMITY else ListaSortMode.ALPHABETIC
    }

    /** Snapshot actual para persistir al salir (texto de filtros puede venir de la vista). */
    fun buildPersistSnapshot(
        nombreFromField: String,
        localidadFromField: String,
    ): ListPersistedState =
        ListPersistedState(
            nombreFilter = nombreFromField,
            localidadFilter = localidadFromField,
            preferGridOnMobile = _preferGridOnMobile.value,
            proximitySortSelected = sortMode.value == ListaSortMode.BY_PROXIMITY,
        )

    /**
     * Estado de filtros y opciones tal como están en el ViewModel (sobrevive rotación de pantalla).
     * La vista lo usa para rellenar campos sin volver a leer preferencias, que podrían estar desactualizadas.
     */
    fun readPersistableUiState(): ListPersistedState =
        ListPersistedState(
            nombreFilter = nombreFilter.value,
            localidadFilter = localidadFilter.value,
            preferGridOnMobile = _preferGridOnMobile.value,
            proximitySortSelected = sortMode.value == ListaSortMode.BY_PROXIMITY,
        )

    fun setNombreFilter(text: String) {
        nombreFilter.value = text
    }

    fun setLocalidadFilter(text: String) {
        localidadFilter.value = text
    }

    fun setAlphabetSort() {
        sortMode.value = ListaSortMode.ALPHABETIC
    }

    fun setProximitySort() {
        sortMode.value = ListaSortMode.BY_PROXIMITY
    }

    fun updateUserLatLng(lat: Double, lng: Double) {
        userLatLng.value = Pair(lat, lng)
    }

    /** Desactiva ubicación conocida hasta la próxima lectura (p.ej. trazas de ubicación rechazadas). */
    fun clearUserLatLng() {
        userLatLng.value = null
    }

    fun setPreferGridOnMobile(pref: Boolean) {
        _preferGridOnMobile.value = pref
    }

    companion object {
        private fun ordenarLista(
            filtradas: List<Empresa>,
            mode: ListaSortMode,
            ubicacionUsuario: Pair<Double, Double>?,
        ): List<Empresa> {
            val alfabetico = Comparator<Empresa> { a, b ->
                a.nombre.lowercase(Locale.getDefault()).compareTo(b.nombre.lowercase(Locale.getDefault()))
            }
            if (mode != ListaSortMode.BY_PROXIMITY || ubicacionUsuario == null) {
                return filtradas.sortedWith(alfabetico)
            }
            val (uLat, uLng) = ubicacionUsuario
            return filtradas.sortedWith(
                compareBy<Empresa> { empresa ->
                    val c = empresa.coordenadas
                    if (!c.isLikelyGeocoded()) {
                        Double.MAX_VALUE
                    } else {
                        GeoDistance.metersBetween(uLat, uLng, c.lat, c.lng)
                    }
                }.then(alfabetico),
            )
        }
    }
}
