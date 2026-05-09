package com.example.empresas_turismo_activo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.empresas_turismo_activo.domain.repository.EmpresaRepository

/**
 * Factory liviana porque el sistema no puede instanciar [ListViewModel] sin su repositorio inyectado.
 */
class ListViewModelFactory(
    private val repository: EmpresaRepository,
    private val performInitialSync: Boolean = true,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            return ListViewModel(repository, performInitialSync) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
