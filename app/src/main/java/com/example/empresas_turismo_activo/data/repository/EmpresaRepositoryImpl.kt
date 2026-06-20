package com.example.empresas_turismo_activo.data.repository

import com.example.empresas_turismo_activo.data.local.dao.EmpresaDao
import com.example.empresas_turismo_activo.data.local.entity.EmpresaEntity
import com.example.empresas_turismo_activo.data.model.Contacto
import com.example.empresas_turismo_activo.data.model.Coordenadas
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.model.normalizar
import com.example.empresas_turismo_activo.network.WebService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class EmpresaRepositoryImpl(
    private val empresaDao: EmpresaDao,
    private val webService: WebService,
) : EmpresaRepository {

    private val cache = MutableLiveData<List<Empresa>>(emptyList())

    override fun observaEmpresas(): Flow<List<Empresa>> = cache.asFlow()

    override fun observaCiudad(): Flow<List<Contacto>> =
        cache.asFlow().map { empresas -> empresas.mapNotNull { it.contacto } }

    override suspend fun insertarEmpresas(empresas: List<Empresa>) {
        cache.postValue(empresas)
        empresaDao.insertarTodo(empresas.map { it.aEntidad() })
    }

    override suspend fun obtenerEmpresaPorId(id: String): Empresa? =
        cache.value.firstOrNull { it.id == id }

    override fun observarFiltradasEmpresas(globalQuery: String, localidadQuery: String): Flow<List<Empresa>> {
        val g = globalQuery.trim()
        val loc = localidadQuery.trim()
        return cache.asFlow().map { empresas ->
            empresas.filter { e ->
                (g.isEmpty() || e.nombre?.contains(g, true) == true ||
                        (e.contacto?.direccion?.contains(g, true) == true) ||
                        e.informacion?.actividades?.any { a ->
                            (a.nombre?.contains(g, true) == true) || (a.categoria?.contains(g, true) == true)
                        } == true
                ) && (loc.isEmpty() || e.contacto?.localidad?.contains(loc, true) == true)
            }
        }
    }

    override suspend fun sincronizaEmpresas() {
        withContext(Dispatchers.IO) {
            try {
                val body = webService.obtenerCatalogoEmpresas()
                val empresas = body.empresas.mapNotNull { it.normalizar() }
                if (empresas.isNotEmpty()) {
                    cache.postValue(empresas)
                    empresaDao.insertarTodo(empresas.map { it.aEntidad() })
                }
            } catch (_: HttpException) { }
            catch (_: IOException) { }
        }
    }
}

private fun Empresa.aEntidad(): EmpresaEntity = EmpresaEntity(
    id = id.orEmpty(),
    nombre = nombre.orEmpty(),
    localidad = contacto?.localidad.orEmpty(),
    direccion = contacto?.direccion,
    lat = coordenadas?.lat ?: 0.0,
    lng = coordenadas?.lng ?: 0.0,
    imagenPortada = imagenPortada.orEmpty(),
)
