@file:OptIn(ExperimentalCoroutinesApi::class)
package com.example.empresas_turismo_activo.ui.list

import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.empresas_turismo_activo.CoroutineMainRule
import com.example.empresas_turismo_activo.data.model.Contacto
import com.example.empresas_turismo_activo.data.model.Actividad
import com.example.empresas_turismo_activo.data.model.Coordenadas
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.model.Informacion
import com.example.empresas_turismo_activo.data.repository.EmpresaRepository
import com.example.empresas_turismo_activo.testutil.FakeEmpresaRepository
import com.example.empresas_turismo_activo.util.GeoDistance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.test.TestScope
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/** Pruebas locales del catálogo: filtros combinables y orden por proximidad. */
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
            concejo = "Candás",
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
    fun filtros_filtraPorDireccion() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeEmpresaRepository(
            listOf(
                empresa("1", "Empresa Uno", "Llanes", 43.39, -4.75, direccion = "Calle Mayor 12"),
                empresa("2", "Empresa Dos", "Oviedo", 43.36, -5.85, direccion = "Plaza Uria 5"),
            ),
        )
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.setNombreFilter("uria")
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value.size)
            assertEquals("2", vm.empresas.value.single().id)
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
            vm.setNombreFilter("sella")
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value.size)
            assertEquals("1", vm.empresas.value.single().id)
        }
    }

    @Test
    fun filtros_filtraPorCategoriaDeActividad() = runTest(mainDispatcherRule.dispatcher) {
        val act = Actividad(nombre = "Circuito Norte", imagenUrl = "", categoria = "ciclismo")
        val fake = FakeEmpresaRepository(
            listOf(
                empresa("1", "A", "Oviedo", 43.36, -5.85, actividades = listOf(act)),
                empresa("2", "B", "Gijón", 43.532, -5.661, actividades = emptyList()),
            ),
        )
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.setNombreFilter("cicli")
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value.size)
            assertEquals("1", vm.empresas.value.single().id)
        }
    }

    @Test
    fun filtros_filtraPorNombre() = runTest(mainDispatcherRule.dispatcher) {
        val catalog = listOf(
            empresa("1", "Senderos del Norte", "Llanes", 43.39, -4.75),
            empresa("2", "Agua Viva Rafting", "Arriondas", 43.32, -5.05),
        )
        val fake = FakeEmpresaRepository(catalog)
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.setNombreFilter("raft")
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value.size)
            assertEquals("2", vm.empresas.value.single().id)
        }
    }

    @Test
    fun filtros_filtraPorLocalidad() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeEmpresaRepository(
            listOf(
                empresa("1", "A", "Gijón", 43.532, -5.661),
                empresa("2", "B", "Oviedo", 43.361, -5.849),
            ),
        )
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.setLocalidadFilter("Ovied")
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value.size)
            assertEquals("2", vm.empresas.value.single().id)
        }
    }

    @Test
    fun filtros_nombre_y_localidad_exigen_conjuncion() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeEmpresaRepository(
            listOf(
                empresa("1", "Turismo Playa", "Gijón", 43.532, -5.661),
                empresa("2", "Turismo Playa", "Oviedo", 43.361, -5.849),
                empresa("3", "OtroNombre", "Gijón", 43.532, -5.661),
            ),
        )
        val vm = ListViewModel(fake, performInitialSync = false)

        collectEmpresas(vm) {
            vm.setNombreFilter("turismo")
            vm.setLocalidadFilter("Ovied")
            advanceUntilIdle()
            assertEquals(1, vm.empresas.value.size)
            assertEquals("2", vm.empresas.value.single().id)
        }
    }

    @Test
    fun proximidad_orden_correcto_cuando_modo_actividady_ubicacion() =
        runTest(mainDispatcherRule.dispatcher) {
            val userLat = 43.4
            val userLng = -5.82
            val near = empresa("n", "Cerca", "X", lat = userLat + 0.01, lng = userLng + 0.01)
            val medium = empresa("m", "Media", "X", lat = userLat + 0.03, lng = userLng + 0.03)
            val farInvalid = empresa("f", "Lejos_sin_coord", "X", lat = 0.0, lng = 0.0)

            assertTrue(
                GeoDistance.metersBetween(userLat, userLng, near.coordenadas.lat, near.coordenadas.lng) <
                    GeoDistance.metersBetween(userLat, userLng, medium.coordenadas.lat, medium.coordenadas.lng),
            )

            val fake = FakeEmpresaRepository(listOf(medium, farInvalid, near))
            val vm = ListViewModel(fake, performInitialSync = false)

            collectEmpresas(vm) {
                vm.updateUserLatLng(userLat, userLng)
                vm.setProximitySort()
                advanceUntilIdle()

                assertEquals(listOf("n", "m", "f"), vm.empresas.value.map { it.id })
            }
        }

    @Test
    fun filtros_MockitoObserveEmpresa_flujo_se_conecta() = runTest(mainDispatcherRule.dispatcher) {
        val repo: EmpresaRepository = mock()
        val samples = listOf(empresa("1", "A", "Gijón", 43.532, -5.661))
        whenever(repo.observeEmpresas()).thenReturn(flowOf(samples))
        whenever(repo.observeFilteredEmpresas(any(), any())).thenAnswer { inv ->
            val g = inv.getArgument<String>(0).trim()
            val l = inv.getArgument<String>(1).trim()
            val filtered = samples.filter { e ->
                val gOk = g.isEmpty() ||
                    e.nombre.contains(g, ignoreCase = true) ||
                    e.contacto.direccion?.contains(g, ignoreCase = true) == true ||
                    e.informacion.actividades.any { act ->
                        act.nombre.contains(g, ignoreCase = true) ||
                            act.categoria.contains(g, ignoreCase = true)
                    }
                val lOk =
                    l.isEmpty() ||
                        e.contacto.localidad.contains(l, ignoreCase = true)
                gOk && lOk
            }
            flowOf(filtered)
        }

        val vm = ListViewModel(repo, performInitialSync = false)

        collectEmpresas(vm) {
            vm.setNombreFilter("ZZZ")
            advanceUntilIdle()
            assertEquals(0, vm.empresas.value.size)
            verify(repo, atLeastOnce()).observeFilteredEmpresas(any(), any())
        }
    }

    @Test
    fun categoriasDisponibles_unicas_case_insensitive_y_orden_alfabetico() =
        runTest(mainDispatcherRule.dispatcher) {
            val aguaMinus = Actividad(nombre = "Río", imagenUrl = "", categoria = "agua")
            val aguaMayus = Actividad(nombre = "Lago", imagenUrl = "", categoria = "AGUA")
            val tierra = Actividad(nombre = "Ruta", imagenUrl = "", categoria = "Tierra")
            val fake = FakeEmpresaRepository(
                listOf(
                    empresa("1", "A", "X", 43.4, -5.8, actividades = listOf(aguaMinus)),
                    empresa(
                        "2",
                        "B",
                        "X",
                        43.4,
                        -5.8,
                        actividades = listOf(tierra, aguaMayus),
                    ),
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
            val monte = Actividad(nombre = "Trekking", imagenUrl = "", categoria = "montaña")
            val fake = FakeEmpresaRepository(
                listOf(
                    empresa("1", "Rafting Norte", "Llanes", 43.39, -4.75, actividades = listOf(agua)),
                    empresa("2", "Rafting Sur", "Llanes", 43.39, -4.75, actividades = listOf(monte)),
                    empresa("3", "Otro", "Oviedo", 43.36, -5.85, actividades = listOf(agua)),
                ),
            )
            val vm = ListViewModel(fake, performInitialSync = false)

            collectEmpresasYCategorias(vm) {
                vm.setNombreFilter("rafting")
                advanceUntilIdle()
                assertEquals(setOf("1", "2"), vm.empresas.value.map { it.id }.toSet())

                vm.setCategoriaFiltro("agua")
                advanceUntilIdle()
                assertEquals(1, vm.empresas.value.size)
                assertEquals("1", vm.empresas.value.single().id)

                vm.setCategoriaFiltro(null)
                vm.setNombreFilter("")
                vm.setLocalidadFilter("Ovied")
                advanceUntilIdle()
                assertEquals(1, vm.empresas.value.size)
                assertEquals("3", vm.empresas.value.single().id)

                vm.setCategoriaFiltro("Montaña") // igualdad ignoreCase respecto a "montaña"
                advanceUntilIdle()
                assertEquals(0, vm.empresas.value.size)

                vm.setLocalidadFilter("Llanes")
                vm.setNombreFilter("rafting")
                advanceUntilIdle()
                vm.setCategoriaFiltro("montaña")
                advanceUntilIdle()
                assertEquals("2", vm.empresas.value.single().id)
            }
        }

}

/** Activa suscriptores de [ListViewModel.empresas] porque el flujo usa [SharingStarted.WhileSubscribed]. */
private suspend fun TestScope.collectEmpresas(vm: ListViewModel, block: suspend () -> Unit) {
    val job = backgroundScope.launch { vm.empresas.collect {} }
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
    val jobEmpresas = backgroundScope.launch { vm.empresas.collect {} }
    val jobCategorias = backgroundScope.launch { vm.categoriasDisponibles.collect {} }
    advanceUntilIdle()
    try {
        block()
    } finally {
        jobEmpresas.cancel()
        jobCategorias.cancel()
    }
}
