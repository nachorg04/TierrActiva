package com.example.empresas_turismo_activo.data.repository

import com.example.empresas_turismo_activo.data.model.Empresa
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de la capa de datos para acceder al catálogo de empresas; la implementación combina
 * persistencia local (Room) y sincronización remota (Retrofit).
 */
interface EmpresaRepository {

    /** Emisión continua del listado persistido ante cualquier cambio en la tabla. */
    fun observeEmpresas(): Flow<List<Empresa>>

    /** Reemplaza o inserta en bloque todas las empresas suministradas. */
    suspend fun insertEmpresas(empresas: List<Empresa>)

    /** Recupera una empresa puntual mediante su identificador o null si no existe. */
    suspend fun getEmpresaById(id: String): Empresa?

    /**
     * Listado observable filtrado en Room por búsqueda global (nombre, dirección, actividades serializadas)
     * y por localidad. Las cadenas en blanco desactivan cada criterio.
     */
    fun observeFilteredEmpresas(globalQuery: String, localidadQuery: String): Flow<List<Empresa>>

    /** Descarga el catálogo remoto y sustituye datos locales; fallos de red se absorben en la implementación. */
    suspend fun syncEmpresas()
}
