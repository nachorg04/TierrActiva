package com.example.empresas_turismo_activo.testutil

import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.repository.EmpresaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeEmpresaRepository(
    initial: List<Empresa>,
) : EmpresaRepository {

    private val _empresas = MutableStateFlow(initial)

    /** Permite cambiar los datos emitidos en caliente dentro de cada prueba. */
    fun setEmpresas(value: List<Empresa>) {
        _empresas.value = value
    }

    override fun observaEmpresas(): Flow<List<Empresa>> = _empresas

    override suspend fun insertarEmpresas(empresas: List<Empresa>) {
        _empresas.value = empresas
    }

    override suspend fun getEmpresaId(id: String): Empresa? =
        _empresas.value.firstOrNull { it.id == id }

    /** Misma semántica que Room [observarFiltradasEmpresas]: nombre/dirección/actividades parseadas + localidad. */
    override fun observarFiltradasEmpresas(globalQuery: String, localidadQuery: String): Flow<List<Empresa>> {
        val g = globalQuery.trim()
        val l = localidadQuery.trim()
        return _empresas.map { catalog ->
            catalog.filter { empresa ->
                matchesGlobalPredicate(empresa, g) &&
                    (l.isEmpty() ||
                        empresa.contacto.localidad.contains(l, ignoreCase = true))
            }
        }
    }

    override suspend fun sincronizaEmpresas() {}

    private fun matchesGlobalPredicate(empresa: Empresa, q: String): Boolean {
        if (q.isEmpty()) return true
        if (empresa.nombre.contains(q, ignoreCase = true)) return true
        if (empresa.contacto.direccion?.contains(q, ignoreCase = true) == true) return true
        return empresa.informacion.actividades.any { act ->
            act.nombre.contains(q, ignoreCase = true) ||
                act.categoria.contains(q, ignoreCase = true)
        }
    }
}
