package com.example.empresas_turismo_activo.data.model

import com.squareup.moshi.Json

data class Empresa(
    val id: String? = null,
    val nombre: String? = null,
    val contacto: Contacto? = null,
    val coordenadas: Coordenadas? = null,
    @Json(name = "imagen_portada") val imagenPortada: String? = null,
    val informacion: Informacion? = null,
)
