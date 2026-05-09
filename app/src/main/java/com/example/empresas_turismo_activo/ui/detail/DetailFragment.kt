package com.example.empresas_turismo_activo.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import coil.dispose
import coil.load
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.TurismoApplication
import com.example.empresas_turismo_activo.databinding.FragmentDetailBinding
import com.example.empresas_turismo_activo.domain.model.Empresa
import kotlinx.coroutines.launch

/** Destino profundo que recibe [empresaId] gracias a Safe Args desde el catálogo. */
class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: DetailViewModel by viewModels {
        val repo = (requireActivity().application as TurismoApplication).empresaRepository
        DetailViewModelFactory(repo, args.empresaId)
    }

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
        binding.textActividades.text = ""
        binding.imagePortada.dispose()
    }

    private fun renderNotFound() {
        binding.imagePortada.dispose()
        binding.textTituloNombre.text = getString(R.string.detail_not_found)
        binding.textContacto.text = ""
        binding.textZona.text = ""
        binding.textInformacionTitulo.text = ""
        binding.textDescripcion.text = ""
        binding.textActividades.text = ""
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
        }
        binding.textContacto.text = contactLines.trim()
        binding.textZona.text = getString(R.string.detail_zona_label, empresa.informacion.zonaActividad)
        binding.textInformacionTitulo.text = empresa.informacion.titulo
        binding.textDescripcion.text = empresa.informacion.descripcion
        binding.textActividades.text =
            empresa.informacion.actividades.joinToString("\n") {
                "• ${it.nombre} — ${it.categoria}"
            }
        binding.imagePortada.load(empresa.imagenPortada) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_foreground)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
