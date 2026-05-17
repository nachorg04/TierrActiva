package com.example.empresas_turismo_activo.ui.list

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
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
import androidx.recyclerview.widget.RecyclerView
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.TurismoApplication
import com.example.empresas_turismo_activo.databinding.FragmentListBinding
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.example.empresas_turismo_activo.data.preferences.ListPersistedState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Listado principal con filtros declarativos, orden por proximidad y gestión adaptable del LayoutManager para tablet frente a móvil.
 */
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = requireNotNull(_binding)

    /** Copia para el diálogo; se actualiza al observar [ListViewModel.categoriasDisponibles]. */
    private var categoriasParaDialogo: List<String> = emptyList()

    /**
     * Tras filtros manualmente, el Recycler conserva offset; ordenar o buscar hace pensar que "no pasó nada".
     * Se marca true en acciones explícitas; el commit de DiffUtil ejecuta scroll a 0 cuando la lista refleje el nuevo estado.
     */
    private var scrollListToTopAfterNextListSubmit = false

    private var suppressFilterScrollCallbacks = false

    /** Evita disparar scroll al recuperar filtros desde DataStore/SavedState durante setText programático. */
    private var searchScrollDebouncerJob: Job? = null
    private var recyclerTouchLastY = 0f

    /** En vertical/portrait el panel flota sobre el listado; en landscape el layout usa dos columnas fijas sin overlay. */
    private fun useFiltersHeaderOverlayBehavior(): Boolean =
        resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE

    /**
     * Solo en modo vertical ([useFiltersHeaderOverlayBehavior]): el header sigue el [translationY] con el gesto vertical.
     */
    private val empresaListRecyclerScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                offsetFiltersHeaderWithListScroll(recyclerView, dy)
                syncRecyclerOverlayPadding()
            }
        }

    private val filtersHeaderLayoutChangeListener =
        View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            syncRecyclerOverlayPadding()
        }

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
            if (isAdded && _binding != null) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.location_permission_denied),
                    Snackbar.LENGTH_LONG,
                ).show()
            }
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
        binding.recyclerEmpresas.addOnScrollListener(empresaListRecyclerScrollListener)
        binding.recyclerEmpresas.setOnTouchListener(::onRecyclerTouchForHeaderOverlay)
        binding.listFiltersHeader.translationY = 0f
        binding.listFiltersHeader.addOnLayoutChangeListener(filtersHeaderLayoutChangeListener)
        binding.listFiltersHeader.post { syncRecyclerOverlayPadding() }

        if (savedInstanceState != null) {
            applyListControlsFromSnapshot(viewModel.readPersistableUiState())
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                if (_binding == null) return@launch
                val persisted = listPreferences.load()
                viewModel.restorePersisted(persisted)
                applyListControlsFromSnapshot(persisted)
            }
        }
        binding.inputFilterNombre.doAfterTextChanged { text ->
            viewModel.setNombreFilter(text?.toString().orEmpty())
            if (!suppressFilterScrollCallbacks) scheduleScrollToTopDebouncedForSearchInput()
        }
        binding.inputFilterLocalidad.doAfterTextChanged { text ->
            viewModel.setLocalidadFilter(text?.toString().orEmpty())
            if (!suppressFilterScrollCallbacks) scheduleScrollToTopDebouncedForSearchInput()
        }
        binding.buttonSortAlphabet.setOnClickListener {
            requestScrollToTopAfterNextEmpresasCommit()
            viewModel.setAlphabetSort()
        }
        binding.buttonSortProximity.setOnClickListener {
            onProximityButtonClicked()
        }

        binding.buttonFilterCategoria.setOnClickListener {
            showCategoryFilterDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.empresas.collect { empresas ->
                        adapter.submitList(empresas) {
                            if (scrollListToTopAfterNextListSubmit && _binding != null) {
                                binding.recyclerEmpresas.scrollToPosition(0)
                                scrollListToTopAfterNextListSubmit = false
                                resetFiltersHeaderTranslation()
                            }
                        }
                    }
                }
                launch {
                    viewModel.categoriasDisponibles.collect { cats ->
                        categoriasParaDialogo = cats
                    }
                }
                launch {
                    viewModel.categoriaFiltroSeleccionada.collect { seleccion ->
                        bindCategoryFilterButtonLabel(seleccion)
                    }
                }
            }
        }
    }

    private fun requestScrollToTopAfterNextEmpresasCommit() {
        scrollListToTopAfterNextListSubmit = true
    }

    private fun scheduleScrollToTopDebouncedForSearchInput() {
        searchScrollDebouncerJob?.cancel()
        searchScrollDebouncerJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(REQUEST_SCROLL_SEARCH_DEBOUNCE_MS)
            scrollListToTopAfterNextListSubmit = true
        }
    }

    private fun tabletSpanCount(): Int =
        resources.getInteger(R.integer.list_span_count)

    private fun resetFiltersHeaderTranslation() {
        if (_binding == null) return
        binding.listFiltersHeader.translationY = 0f
        syncRecyclerOverlayPadding()
    }

    /**
     * En modo overlay (portrait/tablet vertical), reserva espacio superior según la parte visible
     * del panel de filtros para que la primera tarjeta no quede tapada al estar el listado arriba.
     */
    private fun syncRecyclerOverlayPadding() {
        if (_binding == null || !useFiltersHeaderOverlayBehavior()) return
        val header = binding.listFiltersHeader
        val recycler = binding.recyclerEmpresas
        val gap = resources.getDimensionPixelSize(R.dimen.list_filters_recycler_gap)
        val headerVisibleBottom = (header.height + header.translationY).coerceAtLeast(0f).toInt()
        val top = headerVisibleBottom + gap
        if (recycler.paddingTop != top) {
            recycler.setPadding(recycler.paddingLeft, top, recycler.paddingRight, recycler.paddingBottom)
        }
    }

    /** Desplaza el panel de filtros en sync con los píxeles verticales que se desplaza el RecyclerView. */
    private fun offsetFiltersHeaderWithListScroll(recyclerView: RecyclerView, dy: Int) {
        if (_binding == null || dy == 0) return
        if (!useFiltersHeaderOverlayBehavior()) return
        val header = binding.listFiltersHeader
        applyHeaderOffsetWithMeasuredHeight(header, recyclerView, dy)
    }

    /**
     * Si el listado está en la parte superior, RecyclerView puede dejar de emitir dy útiles.
     * Capturamos el arrastre táctil para mantener el panel de filtros siempre manipulable.
     */
    private fun onRecyclerTouchForHeaderOverlay(view: View, event: MotionEvent): Boolean {
        if (_binding == null || !useFiltersHeaderOverlayBehavior()) return false
        val recycler = view as RecyclerView
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                recyclerTouchLastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaFingerY = event.y - recyclerTouchLastY
                recyclerTouchLastY = event.y
                if (deltaFingerY == 0f) return false

                val header = binding.listFiltersHeader
                val shouldDriveHeader =
                    !recycler.canScrollVertically(-1) || header.translationY != 0f
                if (shouldDriveHeader) {
                    applyHeaderOffsetWithMeasuredHeight(
                        header = header,
                        recyclerView = recycler,
                        dy = (-deltaFingerY).toInt(),
                    )
                    syncRecyclerOverlayPadding()
                }
            }
        }
        return false
    }

    private fun applyHeaderOffsetWithMeasuredHeight(header: View, recyclerView: RecyclerView, dy: Int) {
        fun applyMeasured(hPx: Int) {
            if (hPx <= 0) return
            val maxUp = -hPx.toFloat()
            header.translationY = (header.translationY - dy.toFloat()).coerceIn(maxUp, 0f)
        }
        val measured = header.height
        if (measured > 0) {
            applyMeasured(measured)
        } else {
            header.post {
                if (_binding == null) return@post
                val hPost = binding.listFiltersHeader.height
                if (recyclerView == binding.recyclerEmpresas && hPost > 0) {
                    applyMeasured(hPost)
                }
            }
        }
    }

    /** Rellena filtros y conmutadores desde un snapshot (preferencias o ViewModel tras rotación). */
    private fun applyListControlsFromSnapshot(snapshot: ListPersistedState) {
        suppressFilterScrollCallbacks = true
        binding.inputFilterNombre.setText(snapshot.nombreFilter)
        binding.inputFilterLocalidad.setText(snapshot.localidadFilter)
        suppressFilterScrollCallbacks = false
        configurePreferGridUi()
        binding.switchPreferGrid.setOnCheckedChangeListener(null)
        binding.switchPreferGrid.isChecked =
            snapshot.preferGridOnMobile && tabletSpanCount() == 1
        binding.switchPreferGrid.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPreferGridOnMobile(isChecked)
            applyRecyclerLayout(binding.switchPreferGrid.isChecked)
        }
        applyRecyclerLayout(binding.switchPreferGrid.isChecked)
        bindCategoryFilterButtonLabel(snapshot.categoriaFiltro)
    }

    private fun bindCategoryFilterButtonLabel(categoriaSeleccionada: String?) {
        binding.buttonFilterCategoria.text =
            if (categoriaSeleccionada.isNullOrBlank()) {
                getString(R.string.filter_category_button_all)
            } else {
                getString(R.string.filter_category_button_selected, categoriaSeleccionada)
            }
    }

    private fun showCategoryFilterDialog() {
        val titulos = buildList {
            add(getString(R.string.filter_category_all))
            addAll(categoriasParaDialogo)
        }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.filter_category_dialog_title)
            .setItems(titulos) { dialog, which ->
                requestScrollToTopAfterNextEmpresasCommit()
                when (which) {
                    0 -> viewModel.setCategoriaFiltro(null)
                    else -> viewModel.setCategoriaFiltro(categoriasParaDialogo[which - 1])
                }
                dialog.dismiss()
            }
            .show()
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
                    requestScrollToTopAfterNextEmpresasCommit()
                    viewModel.updateUserLatLng(location.latitude, location.longitude)
                    viewModel.setProximitySort()
                } else {
                    showLocationUnavailableSnack()
                }
            }
        } catch (_: SecurityException) {
            if (!isAdded || _binding == null) return
            Snackbar.make(
                binding.root,
                getString(R.string.location_permission_denied),
                Snackbar.LENGTH_LONG,
            ).show()
        }
    }

    private fun showLocationUnavailableSnack() {
        if (!isAdded || _binding == null) return
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
        resetFiltersHeaderTranslation()
        binding.listFiltersHeader.post { syncRecyclerOverlayPadding() }
    }

    override fun onStop() {
        val nombre = binding.inputFilterNombre.text?.toString().orEmpty()
        val loc = binding.inputFilterLocalidad.text?.toString().orEmpty()
        requireActivity().lifecycleScope.launch {
            listPreferences.save(viewModel.buildPersistSnapshot(nombre, loc))
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchScrollDebouncerJob?.cancel()
        searchScrollDebouncerJob = null
        binding.listFiltersHeader.removeOnLayoutChangeListener(filtersHeaderLayoutChangeListener)
        binding.recyclerEmpresas.setOnTouchListener(null)
        binding.recyclerEmpresas.removeOnScrollListener(empresaListRecyclerScrollListener)
        binding.recyclerEmpresas.adapter = null
        _binding = null
    }

    companion object {
        private const val REQUEST_SCROLL_SEARCH_DEBOUNCE_MS = 325L
    }
}
