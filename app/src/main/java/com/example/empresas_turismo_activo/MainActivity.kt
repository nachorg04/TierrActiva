package com.example.empresas_turismo_activo

import android.content.Intent
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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.empresas_turismo_activo.data.preferences.AppPreferences
import com.example.empresas_turismo_activo.databinding.ActivityMainBinding
import com.example.empresas_turismo_activo.ui.settings.AjustesActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.max
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val appPreferences: AppPreferences
        get() = (application as TurismoApplication).appPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setApplicationLocales(appPreferences.listaIdiomaParaTag(appPreferences.obtenerTagIdioma()))
        AppCompatDelegate.setDefaultNightMode(appPreferences.obtenerModoTema())

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
        val modoActual = appPreferences.obtenerModoTema()

        val posicionSeleccionada = when (modoActual) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> 0
            AppCompatDelegate.MODE_NIGHT_NO -> 1
            AppCompatDelegate.MODE_NIGHT_YES -> 2
            else -> 0
        }

        val opciones = arrayOf(
            getString(R.string.theme_option_system),
            getString(R.string.theme_option_light),
            getString(R.string.theme_option_dark),
        )

        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle(getString(R.string.theme_dialog_title))
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

    private fun mostrarDialogoIdioma() {
        val idiomaActual = appPreferences.obtenerTagIdioma()

        val posicionSeleccionada = when (idiomaActual) {
            "es" -> 1
            "en" -> 2
            else -> 0
        }

        val opciones = arrayOf(
            getString(R.string.language_system),
            getString(R.string.language_spanish),
            getString(R.string.language_english),
        )

        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle(getString(R.string.language_dialog_title))
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, AjustesActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun aplicarTema(modo: Int) {
        appPreferences.guardarModoTema(modo)
        AppCompatDelegate.setDefaultNightMode(modo)
    }

    private fun aplicarIdioma(idioma: String) {
        appPreferences.guardarTagIdioma(idioma)
        AppCompatDelegate.setApplicationLocales(appPreferences.listaIdiomaParaTag(idioma))
    }
}
