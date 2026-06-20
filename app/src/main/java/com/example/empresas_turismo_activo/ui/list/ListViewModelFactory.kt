package com.example.empresas_turismo_activo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.empresas_turismo_activo.data.repository.EmpresaRepository

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
