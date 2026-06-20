package com.example.empresas_turismo_activo.ui.settings
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.data.preferences.AppPreferences
import com.example.empresas_turismo_activo.databinding.ActivityAjustesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.max

class AjustesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjustesBinding
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityAjustesBinding.inflate(layoutInflater)
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        appPreferences = AppPreferences(this)

        binding.btnAjusteTema.setOnClickListener {
            mostrarDialogoTema()
        }

        binding.btnAjusteIdioma.setOnClickListener {
            mostrarDialogoIdioma()
        }
    }

    private fun aplicarTema(modo: Int) {
        appPreferences.guardarModoTema(modo)
        AppCompatDelegate.setDefaultNightMode(modo)
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

        MaterialAlertDialogBuilder(this@AjustesActivity)
            .setTitle(getString(R.string.settings_theme_dialog_title))
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

    private fun aplicarIdioma(codigoIdioma: String) {
        appPreferences.guardarTagIdioma(codigoIdioma)
        AppCompatDelegate.setApplicationLocales(appPreferences.listaIdiomaParaTag(codigoIdioma))
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

        MaterialAlertDialogBuilder(this@AjustesActivity)
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
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
