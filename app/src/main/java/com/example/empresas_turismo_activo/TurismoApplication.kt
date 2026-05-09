package com.example.empresas_turismo_activo

import android.app.Application
import com.example.empresas_turismo_activo.data.local.db.AppDatabase
import com.example.empresas_turismo_activo.data.repository.EmpresaRepositoryImpl
import com.example.empresas_turismo_activo.domain.repository.EmpresaRepository

/**
 * Punto único donde se construye el grafo liviano antes de tener un contenedor DI completo (Hilt, Koin…).
 */
class TurismoApplication : Application() {

    private val database by lazy { AppDatabase.getInstance(applicationContext) }

    /** Fuente única compartida para ViewModels tras la refactorización MVP. */
    val empresaRepository: EmpresaRepository by lazy { EmpresaRepositoryImpl(database.empresaDao()) }
}
