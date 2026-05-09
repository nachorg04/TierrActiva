package com.example.empresas_turismo_activo.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.launch

/**
 * Listado principal con filtros declarativos y gestión adaptable del LayoutManager para tablet frente a móvil.
 */
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: ListViewModel by viewModels {
        val repo = (requireActivity().application as TurismoApplication).empresaRepository
        ListViewModelFactory(repo)
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
        setupRecyclerSpan()
        binding.recyclerEmpresas.adapter = adapter

        binding.inputFilterNombre.doAfterTextChanged { text ->
            viewModel.setNombreFilter(text?.toString().orEmpty())
        }
        binding.inputFilterLocalidad.doAfterTextChanged { text ->
            viewModel.setLocalidadFilter(text?.toString().orEmpty())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.empresas.collect { empresas ->
                    adapter.submitList(empresas)
                }
            }
        }
    }

    /** Selecciona LinearLayout versus Grid según recurso integers (1 en móvil, 2 en sw600dp). */
    private fun setupRecyclerSpan() {
        val spanCount = resources.getInteger(R.integer.list_span_count)
        binding.recyclerEmpresas.layoutManager =
            if (spanCount > 1) {
                GridLayoutManager(requireContext(), spanCount)
            } else {
                LinearLayoutManager(requireContext())
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerEmpresas.adapter = null
        _binding = null
    }
}
