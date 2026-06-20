package com.example.empresas_turismo_activo.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.repository.EmpresaRepository
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object NotFound : DetailUiState
    data class Success(val empresa: Empresa) : DetailUiState
}

class DetailViewModel(
    private val repository: EmpresaRepository,
    private val empresaId: String,
) : ViewModel() {

    private val _uiState = MutableLiveData<DetailUiState>(DetailUiState.Loading)
    val uiState: LiveData<DetailUiState> = _uiState

    init {
        actualizar()
    }

    fun actualizar() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val empresa = repository.obtenerEmpresaPorId(empresaId)
            _uiState.value =
                empresa?.let { DetailUiState.Success(it) } ?: DetailUiState.NotFound
        }
    }
}
