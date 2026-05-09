package com.example.empresas_turismo_activo

import android.app.Application
import com.example.empresas_turismo_activo.data.preferences.ListPreferences
import com.example.empresas_turismo_activo.data.local.db.AppDatabase
import com.example.empresas_turismo_activo.data.remote.EmpresaApiService
import com.example.empresas_turismo_activo.data.repository.EmpresaRepositoryImpl
import com.example.empresas_turismo_activo.domain.repository.EmpresaRepository
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Punto único donde se construye el grafo liviano antes de tener un contenedor DI completo (Hilt, Koin…).
 */
class TurismoApplication : Application() {

    private val database by lazy { AppDatabase.getInstance(applicationContext) }

    /** Instancia Gson compartida para que Retrofit deserialize el mismo contrato que documenta el Gist. */
    private val gson: Gson by lazy { Gson() }

    /** Base con barra final obligatoria por contrato de Retrofit. */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GIST_RAW_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val empresaApi: EmpresaApiService by lazy {
        retrofit.create(EmpresaApiService::class.java)
    }

    /** Fuente única compartida para ViewModels tras la refactorización MVP. */
    val empresaRepository: EmpresaRepository by lazy {
        EmpresaRepositoryImpl(database.empresaDao(), empresaApi)
    }

    /** Filtros y preferencias visuales persistidos fuera del ciclo de vida del Fragment. */
    val listPreferences: ListPreferences by lazy { ListPreferences(this) }

    companion object {
        /** Host del raw de GitHub; el endpoint relativo vive en [EmpresaApiService]. */
        private const val GIST_RAW_BASE_URL = "https://gist.githubusercontent.com/"
    }
}
