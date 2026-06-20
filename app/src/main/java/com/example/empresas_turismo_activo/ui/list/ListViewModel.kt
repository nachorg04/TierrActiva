package com.example.empresas_turismo_activo.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.model.esProbablementeGeocodificado
import com.example.empresas_turismo_activo.data.repository.EmpresaRepository
import com.example.empresas_turismo_activo.util.GeoDistance
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

enum class ListaSortMode { ALPHABETIC, BY_PROXIMITY }

class ListViewModel(
    private val repository: EmpresaRepository,
    performInitialSync: Boolean = true,
) : ViewModel() {

    init {
        if (performInitialSync) {
            viewModelScope.launch { repository.sincronizaEmpresas() }
        }
    }

    private val _nombreFilter = MutableLiveData("")
    private val _categoriasFiltro = MutableLiveData<Set<String>>(emptySet())
    private val _ciudadesFiltro = MutableLiveData<Set<String>>(emptySet())
    private val _sortMode = MutableLiveData(ListaSortMode.ALPHABETIC)
    private val _userLatLng = MutableLiveData<Pair<Double, Double>?>(null)

    val categoriasFiltro: LiveData<Set<String>> get() = _categoriasFiltro
    val ciudadesFiltro: LiveData<Set<String>> get() = _ciudadesFiltro

    val categoriasDisponibles: LiveData<List<String>> = repository.observaEmpresas()
        .map { empresas ->
            empresas.flatMap { e -> e.informacion?.actividades.orEmpty() }
                .mapNotNull { it.categoria?.trim()?.takeIf { c -> c.isNotEmpty() } }
                .distinct().sorted()
        }
        .asLiveData(viewModelScope.coroutineContext)

    val ciudadesDisponibles: LiveData<List<String>> = repository.observaEmpresas()
        .map { empresas ->
            empresas.mapNotNull { it.contacto?.localidad?.trim()?.takeIf { l -> l.isNotEmpty() && !l.contains("?") } }
                .distinct().sorted()
        }
        .asLiveData(viewModelScope.coroutineContext)

    val empresas: LiveData<List<Empresa>> = combine(
        repository.observaEmpresas(),
        _nombreFilter.asFlow(),
        _categoriasFiltro.asFlow(),
        _ciudadesFiltro.asFlow(),
        combine(_sortMode.asFlow(), _userLatLng.asFlow(), ::Pair),
    ) { todas, texto, categorias, ciudades, (modo, ubicacion) ->

        var lista = todas

        if (texto.isNotBlank()) {
            lista = lista.filter { it.nombre?.contains(texto, ignoreCase = true) == true }
        }
        if (categorias.isNotEmpty()) {
            lista = lista.filter { empresa ->
                empresa.informacion?.actividades?.any { act ->
                    categorias.any { it.equals(act.categoria, ignoreCase = true) }
                } == true
            }
        }
        if (ciudades.isNotEmpty()) {
            lista = lista.filter { empresa ->
                ciudades.any { it.equals(empresa.contacto?.localidad, ignoreCase = true) }
            }
        }
        if (modo == ListaSortMode.BY_PROXIMITY && ubicacion != null) {
            val (uLat, uLng) = ubicacion
            lista = lista.sortedBy { empresa ->
                val c = empresa.coordenadas
                if (c?.esProbablementeGeocodificado() == true) GeoDistance.metrosEntre(uLat, uLng, c.lat ?: 0.0, c.lng ?: 0.0)
                else Double.MAX_VALUE
            }
        } else {
            lista = lista.sortedBy { (it.nombre ?: "").lowercase(Locale.getDefault()) }
        }
        lista
    }.asLiveData(viewModelScope.coroutineContext)

    fun establecerFiltroNombre(nombre: String) { _nombreFilter.value = nombre }
    fun establecerCategorias(categorias: Set<String>) { _categoriasFiltro.value = categorias }
    fun establecerCiudades(ciudades: Set<String>) { _ciudadesFiltro.value = ciudades }
    fun establecerOrdenAlfabetico() { _sortMode.value = ListaSortMode.ALPHABETIC }
    fun establecerOrdenProximidad() { _sortMode.value = ListaSortMode.BY_PROXIMITY }
    fun actualizarUbicacionUsuario(lat: Double, lng: Double) { _userLatLng.value = Pair(lat, lng) }
}
