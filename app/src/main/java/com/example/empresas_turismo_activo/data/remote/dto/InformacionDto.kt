package com.example.empresas_turismo_activo.data.remote.dto

import com.squareup.moshi.Json

data class InformacionDto(
    val titulo: String?,
    val descripcion: String?,
    val servicios: String?,
    @Json(name = "zona_actividad") val zonaActividad: String?,
    val actividades: List<ActividadDto>? = null,
)
