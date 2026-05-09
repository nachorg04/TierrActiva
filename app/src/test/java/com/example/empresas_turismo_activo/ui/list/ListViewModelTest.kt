@file:OptIn(ExperimentalCoroutinesApi::class)
package com.example.empresas_turismo_activo.ui.list

import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.empresas_turismo_activo.CoroutineMainRule
import com.example.empresas_turismo_activo.domain.model.Contacto
import com.example.empresas_turismo_activo.domain.model.Coordenadas
import com.example.empresas_turismo_activo.domain.model.Empresa
import com.example.empresas_turismo_activo.domain.model.Informacion
import com.example.empresas_turismo_activo.domain.repository.EmpresaRepository
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
    ): Empresa = Empresa(
        id = id,
        nombre = nombre,
        contacto = Contacto(
            concejo = "Candás",
            direccion = null,
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
            actividades = emptyList(),
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

        val vm = ListViewModel(repo, performInitialSync = false)

        collectEmpresas(vm) {
            vm.setNombreFilter("ZZZ")
            advanceUntilIdle()
            assertEquals(0, vm.empresas.value.size)
            verify(repo).observeEmpresas()
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
