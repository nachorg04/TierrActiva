package com.example.empresas_turismo_activo.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.TurismoApplication
import com.example.empresas_turismo_activo.databinding.FragmentMapBinding
import com.example.empresas_turismo_activo.domain.model.Empresa
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Mapa empresarial: marcadores por coordenadas y salto Safe Args al detalle desde InfoWindow. */
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: MapViewModel by viewModels {
        val repo = (requireActivity().application as TurismoApplication).empresaRepository
        MapViewModelFactory(repo)
    }

    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment_container) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = true

            map.setOnMarkerClickListener { false }
            map.setOnInfoWindowClickListener { marker ->
                val empresaId = marker.tag as? String ?: return@setOnInfoWindowClickListener
                val action = MapFragmentDirections.actionMapFragmentToDetailFragment(empresaId)
                findNavController().navigate(action)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.empresas.collect(::applyEmpresasToMap)
                }
            }
        }
    }

    private fun applyEmpresasToMap(list: List<Empresa>) {
        val map = googleMap ?: return
        map.clear()

        val withCoords = list.filter(::hasMeaningfulCoords)
        if (withCoords.isEmpty()) return

        val boundsBuilder = LatLngBounds.builder()
        for (empresa in withCoords) {
            val latLng = LatLng(empresa.coordenadas.lat, empresa.coordenadas.lng)
            boundsBuilder.include(latLng)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(empresa.nombre)
                    .snippet(empresa.contacto.localidad),
            )?.tag = empresa.id
        }

        val paddingPx = resources.getDimensionPixelOffset(R.dimen.map_bounds_padding)
        if (withCoords.size == 1) {
            val only = LatLng(withCoords.first().coordenadas.lat, withCoords.first().coordenadas.lng)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(only, SINGLE_MARKER_ZOOM))
        } else {
            val bounds = boundsBuilder.build()
            try {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))
            } catch (_: Exception) {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(bounds.center, SINGLE_MARKER_ZOOM),
                )
            }
        }
    }

    private fun hasMeaningfulCoords(e: Empresa): Boolean {
        val lat = e.coordenadas.lat
        val lng = e.coordenadas.lng
        if (!lat.isFinite() || !lng.isFinite()) return false
        if (abs(lat) < 1e-6 && abs(lng) < 1e-6) return false
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        googleMap = null
        _binding = null
    }

    companion object {
        private const val SINGLE_MARKER_ZOOM = 12f
    }
}
