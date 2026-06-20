package com.example.empresas_turismo_activo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.empresas_turismo_activo.data.local.entity.EmpresaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpresaDao {

    @Query("DELETE FROM empresas_table")
    suspend fun eliminarTodo()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodo(empresas: List<EmpresaEntity>)

    @Transaction
    suspend fun reemplazarTodo(empresas: List<EmpresaEntity>) {
        eliminarTodo()
        insertarTodo(empresas)
    }

    @Query("SELECT * FROM empresas_table ORDER BY nombre COLLATE NOCASE ASC")
    fun obtenerTodasEmpresas(): Flow<List<EmpresaEntity>>

    @Query("SELECT * FROM empresas_table WHERE id = :id LIMIT 1")
    suspend fun obtenerEmpresaPorId(id: String): EmpresaEntity?
}
