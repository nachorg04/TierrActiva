package com.example.empresas_turismo_activo.data.remote.dto

import com.squareup.moshi.Json

data class ContactoDto(
    val concejo: String?,
    val zona: String?,
    val direccion: String?,
    val cp: String?,
    val localidad: String?,
    @Json(name = "telefono") val telefonos: List<String>? = null,
    @Json(name = "email") val emails: List<String>? = null,
    val web: String? = null,
)
