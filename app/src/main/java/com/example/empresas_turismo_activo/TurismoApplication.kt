package com.example.empresas_turismo_activo

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.empresas_turismo_activo.data.local.db.AppDatabase
import com.example.empresas_turismo_activo.data.preferences.AppPreferences
import com.example.empresas_turismo_activo.data.repository.EmpresaRepositoryImpl
import com.example.empresas_turismo_activo.data.repository.EmpresaRepository
import com.example.empresas_turismo_activo.network.NetworkModule

class TurismoApplication : Application() {

    val appPreferences by lazy { AppPreferences(this) }

    private val database by lazy { AppDatabase.obtenerInstancia(applicationContext) }

    val empresaRepository: EmpresaRepository by lazy {
        EmpresaRepositoryImpl(database.empresaDao(), NetworkModule.webService)
    }

    override fun onCreate() {
        AppCompatDelegate.setApplicationLocales(appPreferences.listaIdiomaParaTag(appPreferences.obtenerTagIdioma()))
        AppCompatDelegate.setDefaultNightMode(appPreferences.obtenerModoTema())
        super.onCreate()
    }
}
