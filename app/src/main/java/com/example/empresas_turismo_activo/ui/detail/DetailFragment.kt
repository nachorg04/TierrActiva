package com.example.empresas_turismo_activo.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.dispose
import coil.load
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.TurismoApplication
import com.example.empresas_turismo_activo.databinding.FragmentDetailBinding
import com.example.empresas_turismo_activo.data.model.Contacto
import com.example.empresas_turismo_activo.data.model.Empresa

class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: DetailViewModel by viewModels {
        val repo = (requireActivity().application as TurismoApplication).empresaRepository
        DetailViewModelFactory(repo, args.empresaId)
    }

    private val actividadesAdapter = ActividadListAdapter()
    private var empresaActual: Empresa? = null

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

        binding.buttonVerMapa.setOnClickListener {
            empresaActual?.coordenadas?.let { c ->
                val bundle = Bundle().apply {
                    putFloat("focusLat", (c.lat ?: 0.0).toFloat())
                    putFloat("focusLng", (c.lng ?: 0.0).toFloat())
                }
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.listFragment, true, true)
                    .setLaunchSingleTop(true)
                    .build()
                findNavController().navigate(R.id.mapFragment, bundle, navOptions)
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner, ::renderizarEstadoDetalle)
    }

    private fun renderizarEstadoDetalle(state: DetailUiState) {
        when (state) {
            DetailUiState.Loading -> renderizarCargando()
            DetailUiState.NotFound -> renderizarNoEncontrado()
            is DetailUiState.Success -> renderizar(state.empresa)
        }
    }

    private fun renderizarCargando() {
        binding.textTituloNombre.text = getString(R.string.detail_loading)
        binding.textContacto.text = ""
        binding.textZona.text = ""
        binding.textInformacionTitulo.text = ""
        binding.textDescripcion.text = ""
        binding.textSinActividades.isVisible = false
        binding.buttonVerMapa.isVisible = false
        actividadesAdapter.submitList(emptyList())
        binding.imagePortada.dispose()
    }

    private fun renderizarNoEncontrado() {
        binding.imagePortada.dispose()
        binding.textTituloNombre.text = getString(R.string.detail_not_found)
        binding.textContacto.text = ""
        binding.textZona.text = ""
        binding.textInformacionTitulo.text = ""
        binding.textDescripcion.text = ""
        binding.textSinActividades.isVisible = true
        binding.buttonVerMapa.isVisible = false
        actividadesAdapter.submitList(emptyList())
    }

    private fun renderizar(empresa: Empresa) {
        empresaActual = empresa
        binding.buttonVerMapa.isVisible = true
        binding.textTituloNombre.text = empresa.nombre
        val c = empresa.contacto ?: return
        val i = empresa.informacion ?: return
        binding.textContacto.text = construirLineasContacto(c)
        binding.textZona.text = getString(R.string.detail_zona_label, i.zonaActividad ?: "")
        binding.textInformacionTitulo.text = i.titulo
        binding.textDescripcion.text = i.descripcion

        val actividades = i.actividades.orEmpty()
        binding.textSinActividades.isVisible = actividades.isEmpty()
        actividadesAdapter.submitList(actividades)

        binding.imagePortada.load(empresa.imagenPortada) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_foreground)
        }
    }

    private fun construirLineasContacto(c: Contacto): String = buildString {
        appendLine("${c.localidad ?: ""}, ${c.concejo ?: ""}")
        c.direccion?.let { appendLine(it) }
        c.telefonos.orEmpty().takeIf { it.isNotEmpty() }?.let { appendLine(it.joinToString()) }
        c.emails.orEmpty().takeIf { it.isNotEmpty() }?.let { appendLine(it.joinToString()) }
        c.web?.let { appendLine(it) }
        c.redesSociales.orEmpty().takeIf { it.isNotEmpty() }?.let { redes ->
            appendLine()
            redes.forEach { appendLine("${it.plataforma}: ${it.url}") }
        }
    }.trim()

    override fun onDestroyView() {
        super.onDestroyView()
        binding.imagePortada.dispose()
        binding.recyclerActividades.adapter = null
        _binding = null
    }
}
