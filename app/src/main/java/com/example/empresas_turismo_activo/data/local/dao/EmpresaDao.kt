package com.example.empresas_turismo_activo.data.local.dao

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.empresas_turismo_activo.data.local.entity.EmpresaEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a datos locales para todas las empresas almacenadas en empresas_table. */
@Dao
interface EmpresaDao {

    /**
     * Inserta o reemplaza un lote completo cuando se sincroniza catálogo; operación suspendida porque es IO.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(empresas: List<EmpresaEntity>)

    /**
     * Obtiene todas las empresas y emite nuevo valor automáticamente cuando la tabla cambia (Flow desde Room).
     */
    @Query("SELECT * FROM empresas_table ORDER BY nombre COLLATE NOCASE ASC")
    fun getAllEmpresas(): Flow<List<EmpresaEntity>>

    /** Devuelve la fila cuyo id coincide exactamente o null cuando no existe registro alguno. */
    @Query("SELECT * FROM empresas_table WHERE id = :id LIMIT 1")
    suspend fun getEmpresaById(id: String): EmpresaEntity?

    /** Solo pruebas: vacía el catálogo local. */
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @Query("DELETE FROM empresas_table")
    suspend fun clearAllCompanies()

    /**
     * Catálogo filtrado en base de datos: búsqueda global sobre nombre, dirección y JSON de actividades
     * más filtro opcional por localidad. Cadenas vacías (solo espacios) no aplican ese criterio.
     */
    @Query(
        """
        SELECT * FROM empresas_table
        WHERE (
            TRIM(:globalQuery) = '' OR
            nombre LIKE '%' || TRIM(:globalQuery) || '%' COLLATE NOCASE OR
            (direccion IS NOT NULL AND direccion LIKE '%' || TRIM(:globalQuery) || '%' COLLATE NOCASE) OR
            actividades LIKE '%' || TRIM(:globalQuery) || '%' COLLATE NOCASE
        )
        AND (
            TRIM(:localidadQuery) = '' OR
            localidad LIKE '%' || TRIM(:localidadQuery) || '%' COLLATE NOCASE
        )
        ORDER BY nombre COLLATE NOCASE ASC
        """,
    )
    fun observeFilteredEmpresas(globalQuery: String, localidadQuery: String): Flow<List<EmpresaEntity>>
}
