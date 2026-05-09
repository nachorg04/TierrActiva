package com.example.empresas_turismo_activo.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.empresas_turismo_activo.domain.repository.EmpresaRepository

class MapViewModelFactory(
    private val repository: EmpresaRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(repository) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
