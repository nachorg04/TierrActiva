package com.example.empresas_turismo_activo.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.empresas_turismo_activo.R
import com.example.empresas_turismo_activo.databinding.ItemActividadBinding
import com.example.empresas_turismo_activo.data.model.Actividad

/** Lista de actividades con miniatura Coil alineado al JSON remoto (`imagenUrl`). */
class ActividadListAdapter : ListAdapter<Actividad, ActividadListAdapter.ActividadViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val binding =
            ItemActividadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActividadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ActividadViewHolder(
        private val binding: ItemActividadBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Actividad) {
            binding.textNombreActividad.text = item.nombre
            binding.textCategoriaActividad.text = item.categoria
            binding.imageActividad.load(item.imagenUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Actividad>() {
            override fun areItemsTheSame(a: Actividad, b: Actividad): Boolean =
                a.nombre == b.nombre && a.categoria == b.categoria && a.imagenUrl == b.imagenUrl

            override fun areContentsTheSame(a: Actividad, b: Actividad): Boolean = a == b
        }
    }
}
