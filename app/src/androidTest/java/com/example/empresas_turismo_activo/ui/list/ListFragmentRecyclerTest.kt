package com.example.empresas_turismo_activo.ui.list

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.empresas_turismo_activo.MainActivity
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.data.local.db.AppDatabase
import com.example.empresas_turismo_activo.data.local.entity.EmpresaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso comprueba que la lista muestra RecyclerView poblado después de hidratar Room con una fila mínima.
 */
@RunWith(AndroidJUnit4::class)
class ListFragmentRecyclerTest {

    @Before
    fun seedSingleCompany(): Unit = runBlocking(Dispatchers.IO) {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val dao = AppDatabase.getInstance(ctx).empresaDao()
        dao.clearAllCompanies()
        dao.insertAll(
            listOf(
                EmpresaEntity(
                    id = "instrumented_seed_1",
                    nombre = "Empresa Instrumentada",
                    concejo = "Test",
                    direccion = null,
                    localidad = "Oviedo",
                    telefonos = emptyList(),
                    emails = emptyList(),
                    redesSociales = emptyList(),
                    web = null,
                    lat = 43.3623,
                    lng = -5.8493,
                    imagenPortada = "",
                    tituloInformacion = "Info",
                    descripcionInformacion = "Desc",
                    zonaActividad = "Asturias",
                    actividades = emptyList(),
                ),
            ),
        )
    }

    @Test
    fun listFragment_recyclerIsVisible_andHasRows() {
        ActivityScenario.launch(MainActivity::class.java).use {
            Thread.sleep(400)
            val recyclerMatcher = withId(R.id.recyclerEmpresas)
            onView(recyclerMatcher).check(matches(isDisplayed()))
            onView(recyclerMatcher).check(
                matches(
                    object : TypeSafeMatcher<android.view.View>() {
                        override fun describeTo(description: Description) {
                            description.appendText("RecyclerView debe tener al menos un ítem persistido.")
                        }

                        override fun matchesSafely(item: android.view.View): Boolean {
                            val recycler = item as RecyclerView
                            val count = recycler.adapter?.itemCount ?: 0
                            return count >= 1
                        }
                    },
                ),
            )
        }
    }
}
