package com.example.empresas_turismo_activo.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.TurismoApplication
import com.example.empresas_turismo_activo.databinding.FragmentMapBinding
import com.example.empresas_turismo_activo.data.model.Empresa
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.abs

class MapFragment : Fragment() {

    private val args: MapFragmentArgs by navArgs()

    private var _binding: FragmentMapBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: MapViewModel by viewModels {
        val repo = (requireActivity().application as TurismoApplication).empresaRepository
        MapViewModelFactory(repo)
    }

    private var googleMap: GoogleMap? = null
    private var centeredOnFocus = false

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

            viewModel.empresas.observe(viewLifecycleOwner, ::aplicarEmpresasAlMapa)
        }
    }

    private fun aplicarEmpresasAlMapa(list: List<Empresa>) {
        val map = googleMap ?: return
        map.clear()

        val withCoords = list.filter(::tieneCoordenadasValidas)
        if (withCoords.isEmpty()) return

        val boundsBuilder = LatLngBounds.builder()
        for (empresa in withCoords) {
            val coords = empresa.coordenadas ?: continue
            val latLng = LatLng(coords.lat ?: 0.0, coords.lng ?: 0.0)
            boundsBuilder.include(latLng)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(empresa.nombre)
                    .snippet(empresa.contacto?.localidad),
            )?.tag = empresa.id
        }

        val paddingPx = resources.getDimensionPixelOffset(R.dimen.map_bounds_padding)

        val focusLat = args.focusLat.toDouble()
        val focusLng = args.focusLng.toDouble()
        if (!centeredOnFocus && focusLat != 0.0 && focusLng != 0.0) {
            centeredOnFocus = true
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(focusLat, focusLng), 14f))
        } else if (withCoords.size == 1) {
            val coords = withCoords.first().coordenadas ?: return
            val only = LatLng(coords.lat ?: 0.0, coords.lng ?: 0.0)
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

    private fun tieneCoordenadasValidas(e: Empresa): Boolean {
        val coords = e.coordenadas ?: return false
        val lat = coords.lat ?: return false
        val lng = coords.lng ?: return false
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
