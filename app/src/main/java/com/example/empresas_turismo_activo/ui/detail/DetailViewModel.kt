package com.example.empresas_turismo_activo.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.repository.EmpresaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estados discretos para evitar parpadeos mientras Room responde al id de Safe Args. */
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object NotFound : DetailUiState
    data class Success(val empresa: Empresa) : DetailUiState
}

class DetailViewModel(
    private val repository: EmpresaRepository,
    private val empresaId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val empresa = repository.getEmpresaById(empresaId)
            _uiState.value =
                empresa?.let { DetailUiState.Success(it) } ?: DetailUiState.NotFound
        }
    }
}
