package com.example.empresas_turismo_activo.ui.list

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.TurismoApplication
import com.example.empresas_turismo_activo.databinding.FragmentListBinding
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.example.empresas_turismo_activo.data.preferences.ListPersistedState
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Listado principal con filtros declarativos, orden por proximidad y gestión adaptable del LayoutManager para tablet frente a móvil.
 */
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: ListViewModel by viewModels {
        val repo = (requireActivity().application as TurismoApplication).empresaRepository
        ListViewModelFactory(repo)
    }

    private val listPreferences get() =
        (requireActivity().application as TurismoApplication).listPreferences

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private val requestLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.any { (_, ok) -> ok }) {
            fetchLastLocationThenSortByProximity()
        } else {
            Snackbar.make(
                binding.root,
                getString(R.string.location_permission_denied),
                Snackbar.LENGTH_LONG,
            ).show()
        }
    }

    private val adapter =
        EmpresaListAdapter { empresa ->
            val action =
                ListFragmentDirections.actionListFragmentToDetailFragment(empresa.id)
            findNavController().navigate(action)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerEmpresas.adapter = adapter

        if (savedInstanceState != null) {
            applyListControlsFromSnapshot(viewModel.readPersistableUiState())
        } else {
            lifecycleScope.launch {
                val persisted = listPreferences.load()
                viewModel.restorePersisted(persisted)
                applyListControlsFromSnapshot(persisted)
            }
        }
        binding.inputFilterNombre.doAfterTextChanged { text ->
            viewModel.setNombreFilter(text?.toString().orEmpty())
        }
        binding.inputFilterLocalidad.doAfterTextChanged { text ->
            viewModel.setLocalidadFilter(text?.toString().orEmpty())
        }
        binding.buttonSortAlphabet.setOnClickListener {
            viewModel.setAlphabetSort()
        }
        binding.buttonSortProximity.setOnClickListener {
            onProximityButtonClicked()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.empresas.collect { empresas ->
                    adapter.submitList(empresas)
                }
            }
        }
    }

    private fun tabletSpanCount(): Int =
        resources.getInteger(R.integer.list_span_count)

    /** Rellena filtros y conmutadores desde un snapshot (preferencias o ViewModel tras rotación). */
    private fun applyListControlsFromSnapshot(snapshot: ListPersistedState) {
        binding.inputFilterNombre.setText(snapshot.nombreFilter)
        binding.inputFilterLocalidad.setText(snapshot.localidadFilter)
        configurePreferGridUi()
        binding.switchPreferGrid.setOnCheckedChangeListener(null)
        binding.switchPreferGrid.isChecked =
            snapshot.preferGridOnMobile && tabletSpanCount() == 1
        binding.switchPreferGrid.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPreferGridOnMobile(isChecked)
            applyRecyclerLayout(binding.switchPreferGrid.isChecked)
        }
        applyRecyclerLayout(binding.switchPreferGrid.isChecked)
    }

    private fun configurePreferGridUi() {
        val tablet = tabletSpanCount() > 1
        binding.switchPreferGrid.visibility =
            if (tablet) View.GONE else View.VISIBLE
        if (tablet) {
            viewModel.setPreferGridOnMobile(false)
        }
    }

    private fun onProximityButtonClicked() {
        val ctx = requireContext()
        val hasAny = locationPermissions.any { perm ->
            ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED
        }
        if (hasAny) {
            fetchLastLocationThenSortByProximity()
            return
        }
        Snackbar.make(
            binding.root,
            getString(R.string.location_permission_rationale),
            Snackbar.LENGTH_SHORT,
        ).show()
        requestLocationLauncher.launch(locationPermissions)
    }

    private fun fetchLastLocationThenSortByProximity() {
        try {
            fusedClient.lastLocation.addOnCompleteListener(requireActivity()) { completed ->
                if (!completed.isSuccessful) {
                    showLocationUnavailableSnack()
                    return@addOnCompleteListener
                }
                val location = completed.result
                if (location != null) {
                    viewModel.updateUserLatLng(location.latitude, location.longitude)
                    viewModel.setProximitySort()
                } else {
                    showLocationUnavailableSnack()
                }
            }
        } catch (_: SecurityException) {
            Snackbar.make(
                binding.root,
                getString(R.string.location_permission_denied),
                Snackbar.LENGTH_LONG,
            ).show()
        }
    }

    private fun showLocationUnavailableSnack() {
        Snackbar.make(
            binding.root,
            getString(R.string.location_unavailable),
            Snackbar.LENGTH_LONG,
        ).show()
    }

    private fun applyRecyclerLayout(preferColumnsOnMobile: Boolean) {
        val baseSpan = tabletSpanCount()
        binding.recyclerEmpresas.layoutManager = when {
            baseSpan > 1 -> GridLayoutManager(requireContext(), baseSpan)
            preferColumnsOnMobile -> GridLayoutManager(requireContext(), 2)
            else -> LinearLayoutManager(requireContext())
        }
    }

    override fun onStop() {
        val nombre = binding.inputFilterNombre.text?.toString().orEmpty()
        val loc = binding.inputFilterLocalidad.text?.toString().orEmpty()
        runBlocking {
            listPreferences.save(viewModel.buildPersistSnapshot(nombre, loc))
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerEmpresas.adapter = null
        _binding = null
    }
}
