package com.example.empresas_turismo_activo.data.repository

import com.example.empresas_turismo_activo.data.local.dao.EmpresaDao
import com.example.empresas_turismo_activo.data.local.mapper.toDomain
import com.example.empresas_turismo_activo.data.local.mapper.toEntity
import com.example.empresas_turismo_activo.domain.model.Empresa
import com.example.empresas_turismo_activo.domain.repository.EmpresaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación concreta de [EmpresaRepository] que encapsula únicamente el origen Room y expone modelos ricos.
 * Esta clase es la puerta única recomendada para ViewModels Compose o XML.
 */
class EmpresaRepositoryImpl(
    private val empresaDao: EmpresaDao,
) : EmpresaRepository {

    /** Propaga cualquier cambio en la tabla mapeándolo inmediatamente a dominio. */
    override fun observeEmpresas(): Flow<List<Empresa>> =
        empresaDao.getAllEmpresas().map { rows -> rows.map { it.toDomain() } }

    /** Convierte modelos de negocio a entidades antes de invocar el insert masivo en IO. */
    override suspend fun insertEmpresas(empresas: List<Empresa>) {
        empresaDao.insertAll(empresas.map { it.toEntity() })
    }

    /** Consulta puntual suspendida que devuelve null si el id no está indexado en la BD. */
    override suspend fun getEmpresaById(id: String): Empresa? =
        empresaDao.getEmpresaById(id)?.toDomain()

    /**
     * Búsqueda reactiva; el texto se recorta en el repositorio para que el SQL reciba cadenas normalizadas
     * y vacías activen el camino de “devolver todo” descrito en el DAO.
     */
    override fun searchEmpresas(query: String): Flow<List<Empresa>> {
        val normalized = query.trim()
        return empresaDao.searchEmpresas(normalized).map { rows -> rows.map { it.toDomain() } }
    }
}
