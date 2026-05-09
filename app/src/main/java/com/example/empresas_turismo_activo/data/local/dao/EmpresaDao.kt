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
     * Filtra por nombre comercial, localidad, zona de actividad o coincidencia textual dentro del JSON serializado de actividades
     * (nombre o categoría). Una consulta vacía devuelve todo el catálogo respetando el mismo orden alfabético.
     */
    @Query(
        """
        SELECT * FROM empresas_table
        WHERE (
            TRIM(:query) = '' OR
            nombre LIKE '%' || TRIM(:query) || '%' OR
            localidad LIKE '%' || TRIM(:query) || '%' OR
            zona_actividad LIKE '%' || TRIM(:query) || '%' OR
            actividades LIKE '%' || TRIM(:query) || '%'
        )
        ORDER BY nombre COLLATE NOCASE ASC
        """,
    )
    fun searchEmpresas(query: String): Flow<List<EmpresaEntity>>
}
