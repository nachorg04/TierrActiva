package com.example.empresas_turismo_activo.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.repository.EmpresaRepository
import kotlinx.coroutines.launch

class MapViewModel(
    repository: EmpresaRepository,
) : ViewModel() {

    private val _empresas = MutableLiveData<List<Empresa>>(emptyList())
    val empresas: LiveData<List<Empresa>> = _empresas

    init {
        viewModelScope.launch {
            repository.sincronizaEmpresas()
            repository.observaEmpresas().collect { _empresas.value = it }
        }
    }
}
