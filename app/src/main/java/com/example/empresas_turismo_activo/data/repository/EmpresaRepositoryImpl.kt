package com.example.empresas_turismo_activo.data.repository

import com.example.empresas_turismo_activo.data.local.dao.EmpresaDao
import com.example.empresas_turismo_activo.data.local.mapper.toDomain
import com.example.empresas_turismo_activo.data.local.mapper.toEntity
import com.example.empresas_turismo_activo.data.remote.EmpresaApiService
import com.example.empresas_turismo_activo.data.remote.mapper.toDomain
import com.example.empresas_turismo_activo.data.model.Contacto
import com.example.empresas_turismo_activo.data.model.Empresa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Combina API remota para sincronización y Room como caché observable hacia la UI.
 */
class EmpresaRepositoryImpl(
    private val empresaDao: EmpresaDao,
    private val empresaApi: EmpresaApiService,
) : EmpresaRepository {

    /** Propaga cualquier cambio en la tabla mapeándolo inmediatamente a dominio. */
    override fun observaEmpresas(): Flow<List<Empresa>> =
        empresaDao.getAllEmpresas().map { rows -> rows.map { it.toDomain() } }

    /** Extraemos la lista de contactos (ciudades) leyendo las empresas. */
    override fun observaCiudad(): Flow<List<Contacto>> =
        observaEmpresas().map { empresas -> empresas.map { it.contacto } }

    /** Convierte modelos de negocio a entidades antes de invocar el insert masivo en IO. */
    override suspend fun insertarEmpresas(empresas: List<Empresa>) {
        empresaDao.insertAll(empresas.map { it.toEntity() })
    }

    /** Consulta puntual suspendida que devuelve null si el id no está indexado en la BD. */
    override suspend fun getEmpresaId(id: String): Empresa? =
        empresaDao.getEmpresaById(id)?.toDomain()

    /**
     * Mantenemos la función original para cumplir la interfaz,
     * aunque nuestro nuevo ViewModel use filtrado en memoria.
     */
    override fun observarFiltradasEmpresas(globalQuery: String, localidadQuery: String): Flow<List<Empresa>> {
        val g = globalQuery.trim()
        val loc = localidadQuery.trim()
        return empresaDao.observeFilteredEmpresas(g, loc).map { rows -> rows.map { it.toDomain() } }
    }

    /**
     * Trae el JSON público, proyecta a dominio y reemplaza tabla local.
     */
    override suspend fun sincronizaEmpresas() {
        withContext(Dispatchers.IO) {
            try {
                val body = empresaApi.getEmpresaCatalogo()
                val entities = body.empresas.mapNotNull { dto -> dto.toDomain() }.map { it.toEntity() }
                if (entities.isNotEmpty()) {
                    empresaDao.insertAll(entities)
                }
            } catch (_: HttpException) {
                // Errores 4xx/5xx; en producción registrar con Timber o similar.
            } catch (_: IOException) {
                // Timeouts, DNS, sin red, etc.
            }
        }
    }
}