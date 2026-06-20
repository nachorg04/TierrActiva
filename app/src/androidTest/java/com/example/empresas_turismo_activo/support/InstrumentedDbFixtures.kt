package com.example.empresas_turismo_activo.support

import android.content.Context
import com.example.empresas_turismo_activo.data.local.db.AppDatabase
import com.example.empresas_turismo_activo.data.local.entity.EmpresaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object InstrumentedDbFixtures {

    const val SEED_COMPANY_ID: String = "instrumented_seed_1"
    const val SEED_COMPANY_NAME: String = "Empresa Instrumentada"

    suspend fun replaceCatalogWithSingleTestCompany(context: Context) {
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.obtenerInstancia(context).empresaDao()
            dao.eliminarTodo()
            dao.insertarTodo(listOf(seedEntity()))
        }
    }

    private fun seedEntity(): EmpresaEntity =
        EmpresaEntity(
            id = SEED_COMPANY_ID,
            nombre = SEED_COMPANY_NAME,
            localidad = "Oviedo",
            direccion = null,
            lat = 43.3623,
            lng = -5.8493,
            imagenPortada = "",
        )
}
