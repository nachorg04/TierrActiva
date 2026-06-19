package com.example.empresas_turismo_activo.data.model

import com.squareup.moshi.Json // <-- Importante: fíjate que ahora importamos de Moshi

/**
 * Actividad ofertada por la empresa dentro del bloque de información.
 */
data class Actividad(
    val nombre: String,

    @Json(name = "imagen_url") // <-- Anotación de Moshi
    val imagenUrl: String,

    val categoria: String,
)