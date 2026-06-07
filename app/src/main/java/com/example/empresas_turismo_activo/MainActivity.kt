package com.example.empresas_turismo_activo

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.empresas_turismo_activo.data.preferences.LocalePreferences
import com.example.empresas_turismo_activo.data.preferences.NightModePreferences
import com.example.empresas_turismo_activo.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.max
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val localePreferences: LocalePreferences
        get() = (application as TurismoApplication).localePreferences

    private val nightModePreferences: NightModePreferences
        get() = (application as TurismoApplication).nightModePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runBlocking {
            // Idioma
            val tagIdioma = localePreferences.getLocaleTag().first()
            AppCompatDelegate.setApplicationLocales(localePreferences.localeListForTag(tagIdioma))

            // Tema Oscuro
            val modoOscuro = nightModePreferences.getModoTema().first()
            AppCompatDelegate.setDefaultNightMode(modoOscuro)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val extraTop = resources.getDimensionPixelSize(R.dimen.top_content_safe_extra)
            val top = max(systemBars.top, cutout.top) + extraTop
            v.updatePadding(
                left = systemBars.left,
                top = top,
                right = systemBars.right,
                bottom = systemBars.bottom,
            )
            windowInsets
        }

        setSupportActionBar(binding.topToolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_superior, menu)
        return true
    }

    private fun mostrarDialogoTema() {
        lifecycleScope.launch {
            val modoActual = nightModePreferences.getModoTema().first()

            val posicionSeleccionada = when (modoActual) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> 0
                AppCompatDelegate.MODE_NIGHT_NO -> 1
                AppCompatDelegate.MODE_NIGHT_YES -> 2
                else -> 0
            }

        val opciones = arrayOf("Preferencia del Sistema", "Modo Claro", "Modo Oscuro")

        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle("Elige el tema")
            .setSingleChoiceItems(opciones, posicionSeleccionada) { dialog, posicion ->
                when (posicion) {
                    0 -> aplicarTema(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    1 -> aplicarTema(AppCompatDelegate.MODE_NIGHT_NO)
                    2 -> aplicarTema(AppCompatDelegate.MODE_NIGHT_YES)
                }
                dialog.dismiss()
            }
            .show()
        }
    }

    private fun mostrarDialogoIdioma() {
        lifecycleScope.launch {
            val idiomaActual = localePreferences.getLocaleTag().first()

            val posicionSeleccionada = when (idiomaActual) {
                "es" -> 1
                "en" -> 2
                else -> 0
            }

            val opciones = arrayOf("Preferencia del Sistema", "Español", "English")

            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle("Elige el idioma")
                .setSingleChoiceItems(opciones, posicionSeleccionada) { dialog, posicion ->
                    when (posicion) {
                        0 -> aplicarIdioma("")
                        1 -> aplicarIdioma("es")
                        2 -> aplicarIdioma("en")
                    }
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.tema_menu -> {
                mostrarDialogoTema()
                true
            }

            R.id.idioma -> {
                mostrarDialogoIdioma()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun aplicarTema(modo: Int){
        lifecycleScope.launch {
            nightModePreferences.setThemeMode(modo)
            AppCompatDelegate.setDefaultNightMode(modo)
        }
    }

    private fun aplicarIdioma(idioma: String){
        lifecycleScope.launch {
            localePreferences.setLocaleTag(idioma)
            AppCompatDelegate.setApplicationLocales(localePreferences.localeListForTag(idioma))
        }
    }
}


