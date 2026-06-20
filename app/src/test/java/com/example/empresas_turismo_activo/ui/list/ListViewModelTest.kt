@file:OptIn(ExperimentalCoroutinesApi::class)
package com.example.empresas_turismo_activo.ui.list

import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.asFlow
import com.example.empresas_turismo_activo.CoroutineMainRule
import com.example.empresas_turismo_activo.data.model.Contacto
import com.example.empresas_turismo_activo.data.model.Actividad
import com.example.empresas_turismo_activo.data.model.Coordenadas
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.model.Informacion
import com.example.empresas_turismo_activo.testutil.FakeEmpresaRepository
import com.example.empresas_turismo_activo.util.GeoDistance
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.test.TestScope

class ListViewModelTest {

    @get:Rule
    val mainDispatcherRule = CoroutineMainRule()

    private fun empresa(
        id: String,
        nombre: String,
        localidad: String,
        lat: Double,
        lng: Double,
        direccion: String? = null,
        actividades: List<Actividad> = emptyList(),
    ): Empresa = Empresa(
        id = id,
        nombre = nombre,
        contacto = Contacto(
            concejo = "Cand\u00e1s",
            direccion = direccion,
            localidad = localidad,
            telefonos = emptyList(),
            emails = emptyList(),
            web = null,
            redesSociales = emptyList(),
        ),
        coordenadas = Coordenadas(lat = lat, lng = lng),
        imagenPortada = "",
        informacion = Informacion(
            titulo = "",
            descripcion = "",
            zonaActividad = "",
            actividades = actividades,
        ),
    )

    @Test
    fun filtros_filtraPorNombre() = runTest(mainDispatcherRule.dispatcher) {
        val catalog = listOf(
            empresa("1", "Senderos del Norte", "Llanes", 43.39, -4.75),
            empresa("2", "Agua Viva Rafting", "Arriondas", 43.32, -5.05),
        )
        val fake = FakeEmpresaRepository(catalog)
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.establecerFiltroNombre("raft")
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value?.size)
            assertEquals("2", vm.empresas.value?.single()?.id)
        }
    }

    @Test
    fun filtros_filtraPorDireccion() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeEmpresaRepository(
            listOf(
                empresa("1", "Empresa Uno", "Llanes", 43.39, -4.75, direccion = "Calle Mayor 12"),
                empresa("2", "Empresa Dos", "Oviedo", 43.36, -5.85, direccion = "Plaza Uria 5"),
            ),
        )
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.establecerFiltroNombre("uria")
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value?.size)
            assertEquals("2", vm.empresas.value?.single()?.id)
        }
    }

    @Test
    fun filtros_filtraPorNombreDeActividad() = runTest(mainDispatcherRule.dispatcher) {
        val act = Actividad(nombre = "Rafting Sella", imagenUrl = "", categoria = "agua")
        val fake = FakeEmpresaRepository(
            listOf(
                empresa("1", "Sin marca", "Llanes", 43.39, -4.75, actividades = listOf(act)),
                empresa("2", "Otra", "Llanes", 43.39, -4.75, actividades = emptyList()),
            ),
        )
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.establecerFiltroNombre("sella")
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value?.size)
            assertEquals("1", vm.empresas.value?.single()?.id)
        }
    }

    @Test
    fun filtros_filtraPorCategoriaDeActividad() = runTest(mainDispatcherRule.dispatcher) {
        val act = Actividad(nombre = "Circuito Norte", imagenUrl = "", categoria = "ciclismo")
        val fake = FakeEmpresaRepository(
            listOf(
                empresa("1", "A", "Oviedo", 43.36, -5.85, actividades = listOf(act)),
                empresa("2", "B", "Gij\u00f3n", 43.532, -5.661, actividades = emptyList()),
            ),
        )
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.establecerFiltroNombre("cicli")
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value?.size)
            assertEquals("1", vm.empresas.value?.single()?.id)
        }
    }

    @Test
    fun filtros_filtraPorCiudad() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeEmpresaRepository(
            listOf(
                empresa("1", "A", "Gij\u00f3n", 43.532, -5.661),
                empresa("2", "B", "Oviedo", 43.361, -5.849),
            ),
        )
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.establecerCiudades(setOf("Oviedo"))
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value?.size)
            assertEquals("2", vm.empresas.value?.single()?.id)
        }
    }

    @Test
    fun filtros_nombreYciudad_exigenConjuncion() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeEmpresaRepository(
            listOf(
                empresa("1", "Turismo Playa", "Gij\u00f3n", 43.532, -5.661),
                empresa("2", "Turismo Playa", "Oviedo", 43.361, -5.849),
                empresa("3", "OtroNombre", "Gij\u00f3n", 43.532, -5.661),
            ),
        )
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.establecerFiltroNombre("turismo")
            vm.establecerCiudades(setOf("Oviedo"))
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value?.size)
            assertEquals("2", vm.empresas.value?.single()?.id)
        }
    }

    @Test
    fun proximidad_orden_correcto_cuando_modo_activo_y_ubicacion() =
        runTest(mainDispatcherRule.dispatcher) {
            val userLat = 43.4
            val userLng = -5.82
            val near = empresa("n", "Cerca", "X", lat = userLat + 0.01, lng = userLng + 0.01)
            val medium = empresa("m", "Media", "X", lat = userLat + 0.03, lng = userLng + 0.03)
            val farInvalid = empresa("f", "Lejos_sin_coord", "X", lat = 0.0, lng = 0.0)

            assertTrue(
                GeoDistance.metrosEntre(userLat, userLng, near.coordenadas?.lat ?: 0.0, near.coordenadas?.lng ?: 0.0) <
                    GeoDistance.metrosEntre(userLat, userLng, medium.coordenadas?.lat ?: 0.0, medium.coordenadas?.lng ?: 0.0),
            )

            val fake = FakeEmpresaRepository(listOf(medium, farInvalid, near))
            val vm = ListViewModel(fake, performInitialSync = false)

            collectEmpresas(vm) {
                vm.actualizarUbicacionUsuario(userLat, userLng)
                vm.establecerOrdenProximidad()
                advanceUntilIdle()

                assertEquals(listOf("n", "m", "f"), vm.empresas.value?.map { it.id })
            }
        }

    @Test
    fun categoriasDisponibles_unicas_case_insensitive_y_orden_alfabetico() =
        runTest(mainDispatcherRule.dispatcher) {
            val aguaMinus = Actividad(nombre = "R\u00edo", imagenUrl = "", categoria = "agua")
            val aguaMayus = Actividad(nombre = "Lago", imagenUrl = "", categoria = "AGUA")
            val tierra = Actividad(nombre = "Ruta", imagenUrl = "", categoria = "Tierra")
            val fake = FakeEmpresaRepository(
                listOf(
                    empresa("1", "A", "X", 43.4, -5.8, actividades = listOf(aguaMinus)),
                    empresa("2", "B", "X", 43.4, -5.8, actividades = listOf(tierra, aguaMayus)),
                ),
            )
            val vm = ListViewModel(fake, performInitialSync = false)

            collectEmpresasYCategorias(vm) {
                advanceUntilIdle()
                assertEquals(listOf("agua", "Tierra"), vm.categoriasDisponibles.value)
            }
        }

    @Test
    fun categoria_filtro_se_combina_con_busqueda_global() =
        runTest(mainDispatcherRule.dispatcher) {
            val agua = Actividad(nombre = "Kayak", imagenUrl = "", categoria = "agua")
            val monte = Actividad(nombre = "Trekking", imagenUrl = "", categoria = "monta\u00f1a")
            val fake = FakeEmpresaRepository(
                listOf(
                    empresa("1", "Rafting Norte", "Llanes", 43.39, -4.75, actividades = listOf(agua)),
                    empresa("2", "Rafting Sur", "Llanes", 43.39, -4.75, actividades = listOf(monte)),
                    empresa("3", "Otro", "Oviedo", 43.36, -5.85, actividades = listOf(agua)),
                ),
            )
            val vm = ListViewModel(fake, performInitialSync = false)

            collectEmpresasYCategorias(vm) {
                vm.establecerFiltroNombre("rafting")
                advanceUntilIdle()
                assertEquals(setOf("1", "2"), vm.empresas.value?.map { it.id }?.toSet())

                vm.establecerCategorias(setOf("agua"))
                advanceUntilIdle()
                assertEquals(1, vm.empresas.value?.size)
                assertEquals("1", vm.empresas.value?.single()?.id)

                vm.establecerCategorias(emptySet())
                vm.establecerFiltroNombre("")
                vm.establecerCiudades(setOf("Oviedo"))
                advanceUntilIdle()
                assertEquals(1, vm.empresas.value?.size)
                assertEquals("3", vm.empresas.value?.single()?.id)

                vm.establecerCategorias(setOf("monta\u00f1a"))
                advanceUntilIdle()
                assertEquals(0, vm.empresas.value?.size)

                vm.establecerCiudades(setOf("Llanes"))
                vm.establecerFiltroNombre("rafting")
                advanceUntilIdle()
                vm.establecerCategorias(setOf("monta\u00f1a"))
                advanceUntilIdle()
                assertEquals("2", vm.empresas.value?.single()?.id)
            }
        }
}

private suspend fun TestScope.collectEmpresas(vm: ListViewModel, block: suspend () -> Unit) {
    val job = backgroundScope.launch { vm.empresas.asFlow().collect {} }
    advanceUntilIdle()
    try {
        block()
    } finally {
        job.cancel()
    }
}

private suspend fun TestScope.collectEmpresasYCategorias(
    vm: ListViewModel,
    block: suspend () -> Unit,
) {
    val jobEmpresas = backgroundScope.launch { vm.empresas.asFlow().collect {} }
    val jobCategorias = backgroundScope.launch { vm.categoriasDisponibles.asFlow().collect {} }
    advanceUntilIdle()
    try {
        block()
    } finally {
        jobEmpresas.cancel()
        jobCategorias.cancel()
    }
}
