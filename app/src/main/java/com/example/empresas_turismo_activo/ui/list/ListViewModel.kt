package com.example.empresas_turismo_activo.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.model.isLikelyGeocoded
import com.example.empresas_turismo_activo.data.repository.EmpresaRepository
import com.example.empresas_turismo_activo.util.GeoDistance
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

// Los dos únicos modos de ordenar nuestra lista
enum class ListaSortMode { ALPHABETIC, BY_PROXIMITY }

class ListViewModel(
    private val repository: EmpresaRepository,
    performInitialSync: Boolean = true
) : ViewModel() {

    init {
        if (performInitialSync) {
            viewModelScope.launch { repository.sincronizaEmpresas() }
        }
    }

    // =====================================================================
    // 1. LA MEMORIA (LiveData Clásico para la Interfaz)
    // =====================================================================

    private val _nombreFilter = MutableLiveData("")
    private val _categoriasFiltro = MutableLiveData<Set<String>>(emptySet())
    private val _ciudadesFiltro = MutableLiveData<Set<String>>(emptySet())
    private val _sortMode = MutableLiveData(ListaSortMode.ALPHABETIC)
    private val _userLatLng = MutableLiveData<Pair<Double, Double>?>()

    // Exponemos solo lectura para el Fragmento
    val categoriasFiltro: LiveData<Set<String>> get() = _categoriasFiltro
    val ciudadesFiltro: LiveData<Set<String>> get() = _ciudadesFiltro

    // =====================================================================
    // 2. EXTRACCIÓN DE DATOS PREDETERMINADOS (Para los menús flotantes)
    // =====================================================================

    val categoriasDisponibles: LiveData<List<String>> = repository.observaEmpresas()
        .map { listaEmpresas ->
            val categoriasUnicas = mutableSetOf<String>()
            listaEmpresas.forEach { empresa ->
                empresa.informacion.actividades.forEach { actividad ->
                    if (actividad.categoria.isNotBlank()) {
                        categoriasUnicas.add(actividad.categoria.trim())
                    }
                }
            }
            categoriasUnicas.toList().sorted()
        }
        .asLiveData(viewModelScope.coroutineContext)

    val ciudadesDisponibles: LiveData<List<String>> = repository.observaEmpresas()
        .map { listaEmpresas ->
            val ciudadesUnicas = mutableSetOf<String>()
            listaEmpresas.forEach { empresa ->
                val localidad = empresa.contacto.localidad
                if (localidad.isNotBlank()) {
                    ciudadesUnicas.add(localidad.trim())
                }
            }
            ciudadesUnicas.toList().sorted()
        }
        .asLiveData(viewModelScope.coroutineContext)

    // =====================================================================
    // 3. LA BATIDORA (El motor de filtrado y ordenación)
    // =====================================================================

    // Agrupamos los 3 filtros en un "Triple" para no superar el límite de combine
    private val filtrosCombinados = combine(
        _nombreFilter.asFlow(),
        _categoriasFiltro.asFlow(),
        _ciudadesFiltro.asFlow()
    ) { nombre, categorias, ciudades ->
        Triple(nombre, categorias, ciudades)
    }

    // Agrupamos la ordenación y el GPS en un "Pair"
    private val ordenCombinado = combine(
        _sortMode.asFlow(),
        _userLatLng.asFlow()
    ) { modo, gps ->
        Pair(modo, gps)
    }

    // Ahora sí, combinamos la Base de Datos con nuestros dos grupos
    val empresas: LiveData<List<Empresa>> = combine(
        repository.observaEmpresas(),
        filtrosCombinados,
        ordenCombinado
    ) { todasLasEmpresas, filtros, orden ->

        // Desempaquetamos los grupos para usarlos cómodamente
        val textoBuscado = filtros.first
        val categoriasMarcadas = filtros.second
        val ciudadesMarcadas = filtros.third

        val modoOrden = orden.first
        val ubicacion = orden.second

        var listaResultante = todasLasEmpresas

        // FILTRO A: Por nombre en la lupa
        if (textoBuscado.isNotBlank()) {
            listaResultante = listaResultante.filter { empresa ->
                empresa.nombre.contains(textoBuscado, ignoreCase = true)
            }
        }

        // FILTRO B: Por categorías (Si coincide alguna de las seleccionadas)
        if (categoriasMarcadas.isNotEmpty()) {
            listaResultante = listaResultante.filter { empresa ->
                empresa.informacion.actividades.any { actividad ->
                    categoriasMarcadas.any { seleccion ->
                        seleccion.equals(actividad.categoria, ignoreCase = true)
                    }
                }
            }
        }

        // FILTRO C: Por ciudades
        if (ciudadesMarcadas.isNotEmpty()) {
            listaResultante = listaResultante.filter { empresa ->
                ciudadesMarcadas.any { ciudadMarcada ->
                    ciudadMarcada.equals(empresa.contacto.localidad, ignoreCase = true)
                }
            }
        }

        // ORDENACIÓN FINAL
        if (modoOrden == ListaSortMode.BY_PROXIMITY && ubicacion != null) {
            val (uLat, uLng) = ubicacion
            listaResultante = listaResultante.sortedBy { empresa ->
                if (empresa.coordenadas.isLikelyGeocoded()) {
                    GeoDistance.metersBetween(uLat, uLng, empresa.coordenadas.lat, empresa.coordenadas.lng)
                } else {
                    Double.MAX_VALUE // Sin coordenadas, al final de la lista
                }
            }
        } else {
            listaResultante = listaResultante.sortedBy { empresa ->
                empresa.nombre.lowercase(Locale.getDefault())
            }
        }

        listaResultante
    }.asLiveData(viewModelScope.coroutineContext)

    // =====================================================================
    // 4. LOS MANDOS (Para modificar los datos desde el Fragmento)
    // =====================================================================

    fun setNombreFilter(nombre: String) { _nombreFilter.value = nombre }

    fun setCategorias(categorias: Set<String>) { _categoriasFiltro.value = categorias }

    fun setCiudades(ciudades: Set<String>) { _ciudadesFiltro.value = ciudades }

    fun setAlphabetSort() { _sortMode.value = ListaSortMode.ALPHABETIC }

    fun setProximitySort() { _sortMode.value = ListaSortMode.BY_PROXIMITY }

    fun updateUserLatLng(lat: Double, lng: Double) { _userLatLng.value = Pair(lat, lng) }
}