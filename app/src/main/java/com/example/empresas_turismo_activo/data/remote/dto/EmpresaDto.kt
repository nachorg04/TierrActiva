package com.example.empresas_turismo_activo.data.remote.dto

import com.squareup.moshi.Json

data class EmpresaDto(
    val id: String?,
    val nombre: String?,
    val contacto: ContactoDto?,
    val coordenadas: CoordenadasDto?,
    @Json(name = "redes_sociales") val redesSociales: Map<String, String>? = null,
    @Json(name = "imagen_portada") val imagenPortada: String?,
    val informacion: InformacionDto?,
)

