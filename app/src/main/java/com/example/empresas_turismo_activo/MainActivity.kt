package com.example.empresas_turismo_activo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.empresas_turismo_activo.databinding.ActivityMainBinding

/**
 * Activity única: aloja NavHostFragment y enlaza BottomNavigationView con NavigationUI.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        setupBottomNavVisibility(navController)
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
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
