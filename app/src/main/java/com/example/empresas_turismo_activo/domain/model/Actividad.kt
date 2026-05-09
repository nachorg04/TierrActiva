package com.example.empresas_turismo_activo.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Actividad ofertada por la empresa dentro del bloque de información.
 *
 * @property nombre Nombre comercial o técnico de la actividad.
 * @property imagenUrl URL de la imagen representativa asociada a la actividad.
 * @property categoria Clasificación de la actividad (senderismo, ciclismo, etc.).
 */
data class Actividad(
    val nombre: String,
    @SerializedName("imagen_url")
    val imagenUrl: String,
    val categoria: String,
)
