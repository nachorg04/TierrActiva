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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.TurismoApplication
import com.example.empresas_turismo_activo.databinding.FragmentListBinding
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: ListViewModel by viewModels {
        val repo = (requireActivity().application as TurismoApplication).empresaRepository
        ListViewModelFactory(repo)
    }

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private val requestLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.any { (_, ok) -> ok }) {
            obtenerUbicacionOrdenar()
        } else {
            Snackbar.make(binding.root, getString(R.string.snackbar_location_denied), Snackbar.LENGTH_LONG).show()

            binding.chipSortProximity.isChecked = false
            binding.chipSortAlphabet.isChecked = true
            pendienteSubirArriba = false
        }
    }

    private val adapter = EmpresaListAdapter { empresa ->
        val action = ListFragmentDirections.actionListFragmentToDetailFragment(empresa.id ?: return@EmpresaListAdapter)
        findNavController().navigate(action)
    }

    private var pendienteSubirArriba = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerEmpresas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEmpresas.adapter = adapter

        var listaCategoriasDisponibles = emptyList<String>()
        var listaCiudadesDisponibles = emptyList<String>()

        binding.inputBuscar.doAfterTextChanged { text ->
            viewModel.establecerFiltroNombre(text?.toString().orEmpty())
            binding.recyclerEmpresas.scrollToPosition(0)
        }

        binding.chipSortAlphabet.isChecked = true

        binding.chipSortAlphabet.setOnClickListener {
            binding.chipSortAlphabet.isChecked = true
            binding.chipSortProximity.isChecked = false
            pendienteSubirArriba = true
            viewModel.establecerOrdenAlfabetico()
        }

        binding.chipSortProximity.setOnClickListener {
            binding.chipSortProximity.isChecked = true
            binding.chipSortAlphabet.isChecked = false
            pendienteSubirArriba = true
            pulsarBotonProximidad()
        }

        binding.chipFiltroCategorias.setOnClickListener {
            val seleccionesActuales = viewModel.categoriasFiltro.value ?: emptySet()
            mostrarDialogoMultiseleccion(listaCategoriasDisponibles, seleccionesActuales)
        }

        binding.chipFiltroCiudad.setOnClickListener {
            val seleccionesActuales = viewModel.ciudadesFiltro.value ?: emptySet()
            mostrarDialogoCiudades(listaCiudadesDisponibles, seleccionesActuales)
        }

        viewModel.empresas.observe(viewLifecycleOwner) { empresas ->
            adapter.submitList(empresas) {
                if (pendienteSubirArriba) {
                    pendienteSubirArriba = false
                    binding.recyclerEmpresas.scrollToPosition(0)
                }
            }
        }

        viewModel.categoriasDisponibles.observe(viewLifecycleOwner) { categorias ->
            listaCategoriasDisponibles = categorias
        }

        viewModel.ciudadesDisponibles.observe(viewLifecycleOwner) { ciudades ->
            listaCiudadesDisponibles = ciudades
        }

        viewModel.categoriasFiltro.observe(viewLifecycleOwner) { seleccionadas ->
            if (seleccionadas.isNullOrEmpty()) {
                binding.chipFiltroCategorias.text = getString(R.string.chip_categories)
            } else {
                binding.chipFiltroCategorias.text = getString(R.string.chip_categories_count, seleccionadas.size)
            }
        }

        viewModel.ciudadesFiltro.observe(viewLifecycleOwner) { seleccionadas ->
            if (seleccionadas.isNullOrEmpty()) {
                binding.chipFiltroCiudad.text = getString(R.string.chip_city)
            } else {
                binding.chipFiltroCiudad.text = getString(R.string.chip_city_count, seleccionadas.size)
            }
        }
    }

    private fun mostrarDialogoMultiseleccion(todasLasCategorias: List<String>, seleccionesActuales: Set<String>) {
        val items = todasLasCategorias.toTypedArray()

        val checkedItems = BooleanArray(todasLasCategorias.size) { index ->
            seleccionesActuales.contains(todasLasCategorias[index])
        }

        val nuevasSelecciones = seleccionesActuales.toMutableSet()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_filter_categories_title))
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                val categoriaTocada = items[which]
                if (isChecked) {
                    nuevasSelecciones.add(categoriaTocada)
                } else {
                    nuevasSelecciones.remove(categoriaTocada)
                }
            }
            .setPositiveButton(getString(R.string.dialog_apply)) { _, _ ->
                viewModel.establecerCategorias(nuevasSelecciones)
                binding.recyclerEmpresas.scrollToPosition(0)
            }
            .setNegativeButton(getString(R.string.dialog_clear_all)) { _, _ ->
                viewModel.establecerCategorias(emptySet())
                binding.recyclerEmpresas.scrollToPosition(0)
            }
            .show()
    }

    private fun mostrarDialogoCiudades(todasLasCiudades: List<String>, seleccionesActuales: Set<String>) {
        val items = todasLasCiudades.toTypedArray()

        val checkedItems = BooleanArray(todasLasCiudades.size) { index ->
            seleccionesActuales.contains(todasLasCiudades[index])
        }

        val nuevasSelecciones = seleccionesActuales.toMutableSet()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_filter_city_title))
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                val ciudadTocada = items[which]
                if (isChecked) {
                    nuevasSelecciones.add(ciudadTocada)
                } else {
                    nuevasSelecciones.remove(ciudadTocada)
                }
            }
            .setPositiveButton(getString(R.string.dialog_apply)) { _, _ ->
                viewModel.establecerCiudades(nuevasSelecciones)
                binding.recyclerEmpresas.scrollToPosition(0)
            }
            .setNegativeButton(getString(R.string.dialog_clear_all)) { _, _ ->
                viewModel.establecerCiudades(emptySet())
                binding.recyclerEmpresas.scrollToPosition(0)
            }
            .show()
    }

    private fun pulsarBotonProximidad() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            obtenerUbicacionOrdenar()
        } else {
            requestLocationLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun obtenerUbicacionOrdenar() {
        try {
            fusedClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && task.result != null) {
                    val loc = task.result
                    viewModel.actualizarUbicacionUsuario(loc.latitude, loc.longitude)
                    viewModel.establecerOrdenProximidad()
                } else {
                    Snackbar.make(binding.root, getString(R.string.snackbar_location_unavailable), Snackbar.LENGTH_SHORT).show()
                    binding.chipSortProximity.isChecked = false
                    binding.chipSortAlphabet.isChecked = true
                    viewModel.establecerOrdenAlfabetico()
                    pendienteSubirArriba = false
                }
            }
        } catch (e: SecurityException) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
