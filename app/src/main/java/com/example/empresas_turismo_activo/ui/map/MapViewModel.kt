package com.example.empresas_turismo_activo.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.repository.EmpresaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Expone catálogo reactivo de Room igual que lista; sirve solo para ubicar marcadores. */
class MapViewModel(
    repository: EmpresaRepository,
) : ViewModel() {

    val empresas: StateFlow<List<Empresa>> = repository
        .observeEmpresas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )
}
