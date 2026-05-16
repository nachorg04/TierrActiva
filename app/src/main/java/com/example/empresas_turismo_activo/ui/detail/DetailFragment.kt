package com.example.empresas_turismo_activo.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.dispose
import coil.load
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.TurismoApplication
import com.example.empresas_turismo_activo.databinding.FragmentDetailBinding
import com.example.empresas_turismo_activo.data.model.Empresa
import kotlinx.coroutines.launch

/** Destino profundo que recibe [empresaId] gracias a Safe Args desde el catálogo o el mapa. */
class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: DetailViewModel by viewModels {
        val repo = (requireActivity().application as TurismoApplication).empresaRepository
        DetailViewModelFactory(repo, args.empresaId)
    }

    private val actividadesAdapter = ActividadListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerActividades.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerActividades.adapter = actividadesAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderDetailState)
            }
        }
    }

    private fun renderDetailState(state: DetailUiState) {
        when (state) {
            DetailUiState.Loading -> renderLoading()
            DetailUiState.NotFound -> renderNotFound()
            is DetailUiState.Success -> render(state.empresa)
        }
    }

    private fun renderLoading() {
        binding.textTituloNombre.text = getString(R.string.detail_loading)
        binding.textContacto.text = ""
        binding.textZona.text = ""
        binding.textInformacionTitulo.text = ""
        binding.textDescripcion.text = ""
        binding.textSinActividades.isVisible = false
        actividadesAdapter.submitList(emptyList())
        binding.imagePortada.dispose()
    }

    private fun renderNotFound() {
        binding.imagePortada.dispose()
        binding.textTituloNombre.text = getString(R.string.detail_not_found)
        binding.textContacto.text = ""
        binding.textZona.text = ""
        binding.textInformacionTitulo.text = ""
        binding.textDescripcion.text = ""
        binding.textSinActividades.isVisible = true
        actividadesAdapter.submitList(emptyList())
    }

    private fun render(empresa: Empresa) {
        binding.textTituloNombre.text = empresa.nombre
        val contactLines = buildString {
            appendLine("${empresa.contacto.localidad}, ${empresa.contacto.concejo}")
            empresa.contacto.direccion?.let { appendLine(it) }
            if (empresa.contacto.telefonos.isNotEmpty()) {
                appendLine(empresa.contacto.telefonos.joinToString())
            }
            if (empresa.contacto.emails.isNotEmpty()) {
                appendLine(empresa.contacto.emails.joinToString())
            }
            empresa.contacto.web?.let { appendLine(it) }
            if (empresa.contacto.redesSociales.isNotEmpty()) {
                appendLine()
                empresa.contacto.redesSociales.forEach { red ->
                    appendLine("${red.plataforma}: ${red.url}")
                }
            }
        }
        binding.textContacto.text = contactLines.trim()
        binding.textZona.text = getString(R.string.detail_zona_label, empresa.informacion.zonaActividad)
        binding.textInformacionTitulo.text = empresa.informacion.titulo
        binding.textDescripcion.text = empresa.informacion.descripcion

        val actividades = empresa.informacion.actividades
        val sinActividades = actividades.isEmpty()
        binding.textSinActividades.isVisible = sinActividades
        actividadesAdapter.submitList(actividades)

        binding.imagePortada.load(empresa.imagenPortada) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_foreground)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.imagePortada.dispose()
        binding.recyclerActividades.adapter = null
        _binding = null
    }
}
