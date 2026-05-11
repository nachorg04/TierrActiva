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
import com.example.empresas_turismo_activo.support.InstrumentedDbFixtures
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
    fun seedSingleCompany(): Unit = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentedDbFixtures.replaceCatalogWithSingleTestCompany(ctx)
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
