package com.example.empresas_turismo_activo.data.remote.dto

import com.squareup.moshi.Json

data class ActividadDto(
    val nombre: String?,
    @Json(name = "imagen_url") val imagenUrl: String?,
    val categoria: String?,
)
