package com.example.empresas_turismo_activo.data.model

import com.squareup.moshi.Json

data class Informacion(
    val titulo: String? = null,
    val descripcion: String? = null,
    val servicios: String? = null,
    @Json(name = "zona_actividad") val zonaActividad: String? = null,
    val actividades: List<Actividad>? = null,
)
