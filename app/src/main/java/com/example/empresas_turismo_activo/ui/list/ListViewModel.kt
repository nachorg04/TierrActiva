package com.example.empresas_turismo_activo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.empresas_turismo_activo.domain.model.Empresa
import com.example.empresas_turismo_activo.domain.repository.EmpresaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Consolida el flujo persistente desde Room aplicando filtros locales de nombre/localidad combinables.
 */
class ListViewModel(
    private val repository: EmpresaRepository,
) : ViewModel() {

    private val nombreFilter = MutableStateFlow("")
    private val localidadFilter = MutableStateFlow("")

    /** Listado proyectado después de filtros declarativos; se recalcula automáticamente al cambiar la BD o los filtros. */
    val empresas: StateFlow<List<Empresa>> = combine(
        repository.observeEmpresas(),
        nombreFilter,
        localidadFilter,
    ) { listado, nombre, localidad ->
        listado.filter { empresa ->
            val nombreOk =
                nombre.isBlank() ||
                    empresa.nombre.contains(nombre.trim(), ignoreCase = true)
            val localidadOk =
                localidad.isBlank() ||
                    empresa.contacto.localidad.contains(localidad.trim(), ignoreCase = true)
            nombreOk && localidadOk
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    /** Actualiza texto de coincidencia parcial contra [Empresa.nombre]. */
    fun setNombreFilter(text: String) {
        nombreFilter.value = text
    }

    /** Actualiza texto de coincidencia parcial contra la localidad de contacto persistida. */
    fun setLocalidadFilter(text: String) {
        localidadFilter.value = text
    }
}
