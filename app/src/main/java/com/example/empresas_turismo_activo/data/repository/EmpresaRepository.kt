package com.example.empresas_turismo_activo.data.repository

import com.example.empresas_turismo_activo.data.model.Contacto
import com.example.empresas_turismo_activo.data.model.Empresa
import kotlinx.coroutines.flow.Flow

interface EmpresaRepository {

    fun observaEmpresas(): Flow<List<Empresa>>

    fun observaCiudad(): Flow<List<Contacto>>

    suspend fun insertarEmpresas(empresas: List<Empresa>)

    suspend fun obtenerEmpresaPorId(id: String): Empresa?

    fun observarFiltradasEmpresas(globalQuery: String, localidadQuery: String): Flow<List<Empresa>>

    suspend fun sincronizaEmpresas()
}
