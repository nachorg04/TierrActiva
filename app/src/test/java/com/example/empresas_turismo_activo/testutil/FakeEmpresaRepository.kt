package com.example.empresas_turismo_activo.testutil

import com.example.empresas_turismo_activo.domain.model.Empresa
import com.example.empresas_turismo_activo.domain.repository.EmpresaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeEmpresaRepository(
    initial: List<Empresa>,
) : EmpresaRepository {

    private val _empresas = MutableStateFlow(initial)

    /** Permite cambiar los datos emitidos en caliente dentro de cada prueba. */
    fun setEmpresas(value: List<Empresa>) {
        _empresas.value = value
    }

    override fun observeEmpresas(): Flow<List<Empresa>> = _empresas

    override suspend fun insertEmpresas(empresas: List<Empresa>) {
        _empresas.value = empresas
    }

    override suspend fun getEmpresaById(id: String): Empresa? =
        _empresas.value.firstOrNull { it.id == id }

    override fun searchEmpresas(query: String): Flow<List<Empresa>> = _empresas

    override suspend fun syncEmpresas() {}
}
