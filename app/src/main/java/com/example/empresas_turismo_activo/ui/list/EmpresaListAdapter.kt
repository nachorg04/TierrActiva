package com.example.empresas_turismo_activo.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.databinding.ItemEmpresaBinding
import com.example.empresas_turismo_activo.data.model.Empresa

/**
 * Lista reciclada con DiffUtil que compara identidad de negocio y contenido completo.
 * La portada se hidrata mediante Coil (corrutinas + cancelaciones automáticas al reciclar celdas).
 */
class EmpresaListAdapter(
    private val onEmpresaClick: (Empresa) -> Unit,
) : ListAdapter<Empresa, EmpresaListAdapter.EmpresaViewHolder>(EmpresaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEmpresaBinding.inflate(inflater, parent, false)
        return EmpresaViewHolder(binding, onEmpresaClick)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EmpresaViewHolder(
        private val binding: ItemEmpresaBinding,
        private val onEmpresaClick: (Empresa) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var bound: Empresa? = null

        init {
            binding.root.setOnClickListener {
                bound?.let(onEmpresaClick)
            }
        }

        /** Enlaza textos locales y programa la carga remota/desde caché con Coil. */
        fun bind(item: Empresa) {
            bound = item
            binding.textNombre.text = item.nombre
            binding.textLocalidad.text = item.contacto.localidad
            binding.imagePortada.load(item.imagenPortada) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
        }
    }

    /** Callback de equivalencia granular para reusar vistas sin parpadeos innecesarios. */
    private class EmpresaDiffCallback : DiffUtil.ItemCallback<Empresa>() {

        override fun areItemsTheSame(oldItem: Empresa, newItem: Empresa): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Empresa, newItem: Empresa): Boolean =
            oldItem == newItem
    }
}
