package com.example.empresas_turismo_activo.data.model

import com.squareup.moshi.Json

data class Actividad(
    val nombre: String? = null,

    @Json(name = "imagen_url")
    val imagenUrl: String? = null,

    val categoria: String? = null,
)
