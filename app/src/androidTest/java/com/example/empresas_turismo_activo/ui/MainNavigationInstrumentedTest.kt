package com.example.empresas_turismo_activo.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.empresas_turismo_activo.MainActivity
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.support.InstrumentedDbFixtures
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Flujos reales de navegación: lista → detalle (Safe Args + Room) y bottom bar → mapa.
 */
@RunWith(AndroidJUnit4::class)
class MainNavigationInstrumentedTest {

    @Before
    fun seedRoom(): Unit = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentedDbFixtures.replaceCatalogWithSingleTestCompany(ctx)
    }

    @Test
    fun list_clickSeededRow_opensDetailWithSameTitle() {
        ActivityScenario.launch(MainActivity::class.java).use {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.recyclerEmpresas)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(
                        allOf(
                            withId(R.id.textNombre),
                            withText(InstrumentedDbFixtures.SEED_COMPANY_NAME),
                        ),
                    ),
                    click(),
                ),
            )
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.textTituloNombre)).check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(InstrumentedDbFixtures.SEED_COMPANY_NAME),
                    ),
                ),
            )
        }
    }

    @Test
    fun detail_pressBack_returnsToListRecycler() {
        ActivityScenario.launch(MainActivity::class.java).use {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.recyclerEmpresas)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(
                        allOf(
                            withId(R.id.textNombre),
                            withText(InstrumentedDbFixtures.SEED_COMPANY_NAME),
                        ),
                    ),
                    click(),
                ),
            )
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.textTituloNombre)).check(matches(isDisplayed()))
            pressBack()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.recyclerEmpresas)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun bottomNav_mapTab_showsMapHostContainer() {
        ActivityScenario.launch(MainActivity::class.java).use {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.mapFragment)).perform(click())
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.map_fragment_container)).check(matches(isDisplayed()))
        }
    }
}
