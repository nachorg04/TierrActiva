package com.example.empresas_turismo_activo

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
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
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Activity única: barra superior (idioma), NavHostFragment y BottomNavigationView.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val localePreferences: LocalePreferences
        get() = (application as TurismoApplication).localePreferences

    private val nightModePreferences: NightModePreferences
        get() = (application as TurismoApplication).nightModePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setupBottomNavVisibility(navController)
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_toolbar, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val nightItem = menu.findItem(R.id.action_night_mode)
        if (nightItem != null) {
            val isNight =
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
            val iconRes = if (isNight) R.drawable.ic_sun_24 else R.drawable.ic_moon_24
            nightItem.icon = AppCompatResources.getDrawable(this, iconRes)
            nightItem.contentDescription = getString(
                if (isNight) {
                    R.string.night_mode_content_desc_light
                } else {
                    R.string.night_mode_content_desc_dark
                },
            )
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_language -> {
                showLanguageDialog()
                return true
            }
            R.id.action_night_mode -> {
                toggleNightMode()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleNightMode() {
        lifecycleScope.launch {
            val isDark = nightModePreferences.isDarkTheme()
            val newDark = !isDark
            nightModePreferences.setDarkTheme(newDark)
            AppCompatDelegate.setDefaultNightMode(nightModePreferences.nightModeConstant(newDark))
            recreate()
        }
    }

    private fun showLanguageDialog() {
        val options = arrayOf(
            getString(R.string.language_system),
            getString(R.string.language_spanish),
            getString(R.string.language_english),
        )
        val tags = arrayOf(
            LocalePreferences.TAG_SYSTEM,
            LocalePreferences.TAG_ES,
            LocalePreferences.TAG_EN,
        )
        lifecycleScope.launch {
            val current = localePreferences.getLocaleTag()
            val checkedItem = tags.indexOf(current).takeIf { it >= 0 } ?: 0
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle(R.string.language_choose)
                .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                    val tag = tags[which]
                    lifecycleScope.launch {
                        localePreferences.setLocaleTag(tag)
                        AppCompatDelegate.setApplicationLocales(
                            localePreferences.localeListForTag(tag),
                        )
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    /** Oculta la barra inferior en detalle para recuperar alto útil de pantalla. */
    private fun setupBottomNavVisibility(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.visibility =
                when (destination.id) {
                    R.id.detailFragment -> View.GONE
                    else -> View.VISIBLE
                }
        }
    }
}
