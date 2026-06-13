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
            fetchLastLocationThenSortByProximity()
        } else {
            Snackbar.make(binding.root, "Permiso de ubicación denegado", Snackbar.LENGTH_LONG).show()
            // Salvavidas visual: volvemos a marcar A-Z si deniega
            binding.chipSortProximity.isChecked = false
            binding.chipSortAlphabet.isChecked = true
        }
    }

    private val adapter = EmpresaListAdapter { empresa ->
        val action = ListFragmentDirections.actionListFragmentToDetailFragment(empresa.id)
        findNavController().navigate(action)
    }

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

        // Cajita para guardar las categorías que nos manda la base de datos
        var listaCategoriasDisponibles = emptyList<String>()

        // 1. Escuchar el buscador
        binding.inputBuscar.doAfterTextChanged { text ->
            viewModel.setNombreFilter(text?.toString().orEmpty())
            binding.recyclerEmpresas.scrollToPosition(0)
        }

        // 2. Escuchar los chips fijos de ordenación
        binding.chipSortAlphabet.isChecked = true // A-Z por defecto

        binding.chipSortAlphabet.setOnClickListener {
            binding.chipSortAlphabet.isChecked = true
            binding.chipSortProximity.isChecked = false
            viewModel.setAlphabetSort()
            binding.recyclerEmpresas.scrollToPosition(0)
        }

        binding.chipSortProximity.setOnClickListener {
            binding.chipSortProximity.isChecked = true
            binding.chipSortAlphabet.isChecked = false
            onProximityButtonClicked()
        }

        // 3. Abrir el menú flotante al pulsar en Categorías
        binding.chipFiltroCategorias.setOnClickListener {
            // El .value de LiveData puede ser null, le ponemos un fallback a emptySet()
            val seleccionesActuales = viewModel.categoriasFiltro.value ?: emptySet()
            mostrarDialogoMultiseleccion(listaCategoriasDisponibles, seleccionesActuales)
        }

        // =====================================================================
        // 4. OBSERVAR EL VIEWMODEL (VERSIÓN LIVEDATA CLÁSICA DE CLASE)
        // =====================================================================

        // A. Pintar la lista de empresas
        viewModel.empresas.observe(viewLifecycleOwner) { empresas ->
            adapter.submitList(empresas)
        }

        // B. Guardar las categorías para el menú
        viewModel.categoriasDisponibles.observe(viewLifecycleOwner) { categorias ->
            listaCategoriasDisponibles = categorias
        }

        // C. Cambiar el texto del botón si hay filtros activos
        viewModel.categoriasFiltro.observe(viewLifecycleOwner) { seleccionadas ->
            if (seleccionadas.isNullOrEmpty()) {
                binding.chipFiltroCategorias.text = "Categorías"
            } else {
                binding.chipFiltroCategorias.text = "Categorías (${seleccionadas.size})"
            }
        }
    }

    // =====================================================================
    // MENÚ FLOTANTE DE SELECCIÓN MÚLTIPLE
    // =====================================================================
    private fun mostrarDialogoMultiseleccion(todasLasCategorias: List<String>, seleccionesActuales: Set<String>) {
        val items = todasLasCategorias.toTypedArray()

        val checkedItems = BooleanArray(todasLasCategorias.size) { index ->
            seleccionesActuales.contains(todasLasCategorias[index])
        }

        val nuevasSelecciones = seleccionesActuales.toMutableSet()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar por categorías")
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                val categoriaTocada = items[which]
                if (isChecked) {
                    nuevasSelecciones.add(categoriaTocada)
                } else {
                    nuevasSelecciones.remove(categoriaTocada)
                }
            }
            .setPositiveButton("Aplicar") { _, _ ->
                viewModel.setCategorias(nuevasSelecciones)
                binding.recyclerEmpresas.scrollToPosition(0)
            }
            .setNegativeButton("Limpiar todo") { _, _ ->
                viewModel.setCategorias(emptySet())
                binding.recyclerEmpresas.scrollToPosition(0)
            }
            .show()
    }

    // =====================================================================
    // FUNCIONES DE UBICACIÓN
    // =====================================================================
    private fun onProximityButtonClicked() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            fetchLastLocationThenSortByProximity()
        } else {
            requestLocationLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun fetchLastLocationThenSortByProximity() {
        try {
            fusedClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && task.result != null) {
                    val loc = task.result
                    viewModel.updateUserLatLng(loc.latitude, loc.longitude)
                    viewModel.setProximitySort()
                    binding.recyclerEmpresas.scrollToPosition(0)
                } else {
                    Snackbar.make(binding.root, "Ubicación no disponible", Snackbar.LENGTH_SHORT).show()
                    binding.chipSortProximity.isChecked = false
                    binding.chipSortAlphabet.isChecked = true
                }
            }
        } catch (e: SecurityException) {
            // Permiso revocado repentinamente
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}