package com.example.empresas_turismo_activo.ui.settings
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.empresas_turismo_activo.data.preferences.LocalePreferences
import com.example.empresas_turismo_activo.data.preferences.NightModePreferences
import com.example.empresas_turismo_activo.databinding.ActivityAjustesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AjustesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjustesBinding
    private lateinit var nightModePreferences: NightModePreferences
    private lateinit var localePreferences: LocalePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAjustesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        nightModePreferences = NightModePreferences(this)
        localePreferences = LocalePreferences(this)

        binding.btnAjusteTema.setOnClickListener {
            mostrarDialogoTema()
        }

        binding.btnAjusteIdioma.setOnClickListener {
            mostrarDialogoIdioma()
        }
    }

    private fun aplicarTema(modo: Int) {
        lifecycleScope.launch {
            nightModePreferences.setThemeMode(modo)
            AppCompatDelegate.setDefaultNightMode(modo)
        }
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

            MaterialAlertDialogBuilder(this@AjustesActivity)
                .setTitle("Elige el tema visual")
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

    private fun aplicarIdioma(codigoIdioma: String) {
        lifecycleScope.launch {
            localePreferences.setLocaleTag(codigoIdioma)
            AppCompatDelegate.setApplicationLocales(localePreferences.localeListForTag(codigoIdioma))
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

            MaterialAlertDialogBuilder(this@AjustesActivity)
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
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}