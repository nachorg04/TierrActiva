package com.example.empresas_turismo_activo.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.empresas_turismo_activo.domain.repository.EmpresaRepository

/**
 * Entrega una instancia parametrizada con el argumento estable de navegación [empresaId].
 */
class DetailViewModelFactory(
    private val repository: EmpresaRepository,
    private val empresaId: String,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(repository, empresaId) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
